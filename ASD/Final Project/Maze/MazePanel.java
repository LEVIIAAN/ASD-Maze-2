import javax.swing.*;
import java.awt.*;
import java.util.*;
import javax.swing.Timer;

public class MazePanel extends JPanel {
    // Variabel Ukuran (Tidak final agar bisa diresize)
    private int rows = 25;
    private int cols = 35;
    private final int CELL_SIZE = 25;
    private Sound stepSound;
    private Sound winSound;
    private Cell[][] grid;
    private Cell startCell, endCell;
    private boolean isWorking = false;

    private JLabel statusLabel;
    private Timer uiTimer;
    private long startTime;

    public MazePanel(JLabel statusLabel) {
        this.statusLabel = statusLabel;
        updatePanelSize();
        this.setBackground(Color.DARK_GRAY);

        // LOAD SOUND (Pastikan path file sesuai dengan lokasi di komputer Anda)
        // Gunakan path absolut atau relatif. Contoh relatif:
        stepSound = new Sound("sounds/pop.wav");
        winSound = new Sound("sounds/win.wav");

        initGrid();
        SwingUtilities.invokeLater(() -> generateNewMaze(false));
    }

    // --- FITUR RESIZE ---
    public void resizeMaze(int newRows, int newCols) {
        if (isWorking) return;
        this.rows = newRows;
        this.cols = newCols;

        updatePanelSize();
        generateNewMaze(false);
    }

    private void updatePanelSize() {
        this.setPreferredSize(new Dimension(cols * CELL_SIZE, rows * CELL_SIZE));
        this.revalidate();
    }

    public void initGrid() {
        grid = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new Cell(r, c, CELL_SIZE);
            }
        }
        repaint();
    }

    private void generateTerrain() {
        Random rand = new Random();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c].visited && !grid[r][c].isStart && !grid[r][c].isEnd) {
                    int chance = rand.nextInt(100);
                    if (chance < 10) grid[r][c].cost = 10;      // 10% Water
                    else if (chance < 30) grid[r][c].cost = 5;  // 20% Mud
                    else grid[r][c].cost = 1;                   // 70% Grass
                }
            }
        }
        repaint();
    }

    public void generateNewMaze(boolean animate) {
        if (isWorking) return;
        initGrid();

        statusLabel.setText("Generating Maze...");
        new Thread(() -> {
            try {
                generateMazeRecursiveBacktracker(animate);
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> statusLabel.setText("Error generating maze!"));
            }
        }).start();
    }

    public void runAlgorithm(String type) {
        if (isWorking) return;
        resetSolverVisuals();

        if (type.equals("BFS")) new Thread(this::solveBFS).start();
        else if (type.equals("DFS")) new Thread(this::solveDFS).start();
        else if (type.equals("Dijkstra")) new Thread(this::solveDijkstra).start();
        else if (type.equals("A*")) new Thread(this::solveAStar).start();
    }

    // --- ALGORITMA DIJKSTRA ---
    private void solveDijkstra() {
        prepareAlgo("Dijkstra");
        PriorityQueue<Cell> pq = new PriorityQueue<>(Comparator.comparingInt(c -> c.gCost));
        startCell.gCost = 0; startCell.fCost = 0;
        pq.add(startCell);
        Color algoColor = new Color(255, 140, 0);

        while (!pq.isEmpty()) {
            Cell current = pq.poll();
            if (current.searched) continue;
            current.searched = true; current.searchColor = algoColor;

            if (current == endCell) { finishAlgo("Dijkstra", true); return; }

            for (Cell neighbor : getValidMoves(current)) {
                if (!neighbor.searched) {
                    int newCost = current.gCost + neighbor.cost;
                    if (newCost < neighbor.gCost) {
                        neighbor.gCost = newCost; neighbor.fCost = newCost; neighbor.parent = current;
                        pq.add(neighbor);
                    }
                }
            }
            animateStep(current);
        }
        finishAlgo("Dijkstra", false);
    }

    // --- ALGORITMA A* ---
    private void solveAStar() {
        prepareAlgo("A*");
        PriorityQueue<Cell> pq = new PriorityQueue<>();
        startCell.gCost = 0;
        startCell.hCost = Math.abs(startCell.row - endCell.row) + Math.abs(startCell.col - endCell.col);
        startCell.fCost = startCell.gCost + startCell.hCost;
        pq.add(startCell);
        Color algoColor = new Color(138, 43, 226);

        while (!pq.isEmpty()) {
            Cell current = pq.poll();
            if (current.searched) continue;
            current.searched = true; current.searchColor = algoColor;

            if (current == endCell) { finishAlgo("A*", true); return; }

            for (Cell neighbor : getValidMoves(current)) {
                if (!neighbor.searched) {
                    int tentativeG = current.gCost + neighbor.cost;
                    if (tentativeG < neighbor.gCost) {
                        neighbor.gCost = tentativeG;
                        neighbor.hCost = Math.abs(neighbor.row - endCell.row) + Math.abs(neighbor.col - endCell.col);
                        neighbor.fCost = neighbor.gCost + neighbor.hCost;
                        neighbor.parent = current;
                        pq.add(neighbor);
                    }
                }
            }
            animateStep(current);
        }
        finishAlgo("A*", false);
    }

    // --- BFS & DFS ---
    private void solveBFS() {
        prepareAlgo("BFS");
        Queue<Cell> queue = new LinkedList<>();
        queue.add(startCell); startCell.searched = true;
        while(!queue.isEmpty()) {
            Cell c = queue.poll();
            if(c == endCell) { finishAlgo("BFS", true); return; }
            for(Cell n : getValidMoves(c)) {
                if(!n.searched) { n.searched=true; n.searchColor=Color.CYAN; n.parent=c; queue.add(n); }
            }
            animateStep(c);
        }
        finishAlgo("BFS", false);
    }

    private void solveDFS() {
        prepareAlgo("DFS");
        Stack<Cell> stack = new Stack<>();
        stack.push(startCell); startCell.searched = true;
        while(!stack.isEmpty()) {
            Cell c = stack.pop();
            if(c == endCell) { finishAlgo("DFS", true); return; }
            ArrayList<Cell> nList = getValidMoves(c); Collections.shuffle(nList);
            for(Cell n : nList) {
                if(!n.searched) { n.searched=true; n.searchColor=Color.MAGENTA; n.parent=c; stack.push(n); }
            }
            animateStep(c);
        }
        finishAlgo("DFS", false);
    }

    // --- GENERATOR LOGIC ---
    private void generateMazeRecursiveBacktracker(boolean animate) {
        isWorking = true;
        Stack<Cell> stack = new Stack<>();
        Cell current = grid[0][0];
        current.visited = true;
        stack.push(current);

        while (!stack.isEmpty()) {
            Cell next = getUnvisitedNeighbor(stack.peek());
            if (next != null) {
                next.visited = true;
                removeWalls(stack.peek(), next);
                stack.push(next);
                if (animate) animateStep(next);
            } else {
                stack.pop();
                if (animate) animateStep(current);
            }
        }
        startCell = grid[0][0]; endCell = grid[rows-1][cols-1];
        startCell.isStart = true; endCell.isEnd = true;
        generateTerrain();
        isWorking = false;
        SwingUtilities.invokeLater(() -> statusLabel.setText("Maze Ready!"));
        repaint();
    }

    // --- HELPERS ---
    private void prepareAlgo(String name) { isWorking=true; SwingUtilities.invokeLater(() -> startLiveTimer(name)); }

    private void finishAlgo(String name, boolean found) {
        stopLiveTimer(); isWorking = false;
        long time = System.currentTimeMillis() - startTime;
        if(found) {
            // MAINKAN SUARA MENANG
            if (winSound != null) winSound.play();

            int calcCost = 0; Cell t = endCell; while(t!=null){ calcCost+=t.cost; t=t.parent; }
            final int finalCost = calcCost;
            reconstructPath();
            SwingUtilities.invokeLater(() -> statusLabel.setText("<html><center><b>"+name+" Done!</b><br/>Time: "+time+"ms Cost: "+finalCost+"</center></html>"));
        } else {
            SwingUtilities.invokeLater(() -> statusLabel.setText(name + " Failed!"));
        }
    }

    private void animateStep(Cell c) {
        c.isHead = true;
        repaint();

        // MAINKAN SUARA STEP
        // Cek agar tidak terlalu berisik (opsional), tapi play() langsung juga oke
        if (stepSound != null) stepSound.play();

        sleep(60);
        c.isHead = false;
    }
    private void sleep(int m) { try { Thread.sleep(m); } catch (Exception e){} }
    private void reconstructPath() { Cell t=endCell; while(t!=null) { t.isPath=true; t=t.parent; repaint(); sleep(15); } }

    private void startLiveTimer(String n) {
        startTime = System.currentTimeMillis(); if(uiTimer!=null) uiTimer.stop();
        uiTimer = new Timer(50, e -> statusLabel.setText("<html><center>"+n+" Running...<br/>"+(System.currentTimeMillis()-startTime)+" ms</center></html>"));
        uiTimer.start();
    }
    private void stopLiveTimer() { if(uiTimer!=null) uiTimer.stop(); }

    public void resetSolverVisuals() { if(startCell==null)return; for(int r=0;r<rows;r++)for(int c=0;c<cols;c++)grid[r][c].resetForSolver(); repaint(); }

    private void removeWalls(Cell a, Cell b) {
        int dx = a.col - b.col; int dy = a.row - b.row;
        if (dx == 1) { a.walls[3] = false; b.walls[1] = false; }
        if (dx == -1) { a.walls[1] = false; b.walls[3] = false; }
        if (dy == 1) { a.walls[0] = false; b.walls[2] = false; }
        if (dy == -1) { a.walls[2] = false; b.walls[0] = false; }
    }
    private Cell getUnvisitedNeighbor(Cell c) {
        ArrayList<Cell> n = new ArrayList<>();
        int r = c.row; int col = c.col;
        if (r>0 && !grid[r-1][col].visited) n.add(grid[r-1][col]);
        if (r<rows-1 && !grid[r+1][col].visited) n.add(grid[r+1][col]);
        if (col>0 && !grid[r][col-1].visited) n.add(grid[r][col-1]);
        if (col<cols-1 && !grid[r][col+1].visited) n.add(grid[r][col+1]);
        if (n.isEmpty()) return null;
        return n.get(new Random().nextInt(n.size()));
    }
    private ArrayList<Cell> getValidMoves(Cell c) {
        ArrayList<Cell> m = new ArrayList<>();
        int r = c.row; int col = c.col;
        if (!c.walls[0] && r>0) m.add(grid[r-1][col]);
        if (!c.walls[1] && col<cols-1) m.add(grid[r][col+1]);
        if (!c.walls[2] && r<rows-1) m.add(grid[r+1][col]);
        if (!c.walls[3] && col>0) m.add(grid[r][col-1]);
        return m;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (grid != null) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (grid[r][c] != null) grid[r][c].draw(g);
                }
            }
        }
    }
}