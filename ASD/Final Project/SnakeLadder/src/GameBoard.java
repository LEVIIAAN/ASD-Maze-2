import java.util.*;

public class GameBoard {
    private final int rows, cols;
    private final int totalSquares;
    private final Map<Integer, List<Integer>> adjacencyList;
    private final Map<Integer, Link> links;
    private final Set<Integer> starPositions;
    private final Set<Integer> primePositions;
    private final Map<Integer, Integer> pointNodes;

    public GameBoard(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.totalSquares = rows * cols;
        this.adjacencyList = new HashMap<>();
        this.links = new HashMap<>();
        this.starPositions = new HashSet<>();
        this.primePositions = new HashSet<>();
        this.pointNodes = new HashMap<>();

        initializeBoard();
    }

    private void initializeBoard() {
        buildGraph();
        generateRandomLinks();
        generateStarPositions();
        generatePrimePositions();
        generateRandomPoints();
    }

    private void buildGraph() {
        for (int i = 1; i <= totalSquares; i++) {
            adjacencyList.put(i, new ArrayList<>());
            if (i < totalSquares) adjacencyList.get(i).add(i + 1);
        }
    }

    private void generateRandomLinks() {
        Random rand = new Random();
        int count = 0;
        while (count < 5) {
            int from = rand.nextInt(totalSquares - 20) + 2;
            int to = from + rand.nextInt(11) + 5;
            if (to < totalSquares && !links.containsKey(from) && !links.containsKey(to)) {
                links.put(from, new Link(from, to));
                count++;
            }
        }
    }

    private void generateStarPositions() {
        for (int i = 5; i < totalSquares; i += 5) starPositions.add(i);
    }

    private void generatePrimePositions() {
        // [FIX] Menggunakan nama method baru calculatePrime
        for (int i = 2; i <= totalSquares; i++) {
            if (calculatePrime(i)) {
                primePositions.add(i);
            }
        }
    }

    private void generateRandomPoints() {
        Random rand = new Random();
        int[] values = {10, 20, 50, 100};
        int count = 0;
        while (count < 10) {
            int pos = rand.nextInt(totalSquares - 2) + 2;
            if (!links.containsKey(pos) && !starPositions.contains(pos) && !pointNodes.containsKey(pos)) {
                pointNodes.put(pos, values[rand.nextInt(values.length)]);
                count++;
            }
        }
    }

    // ==========================================================
    // [FIX] RENAMED METHOD: Menghitung Matematika Prima
    // ==========================================================
    private boolean calculatePrime(int n) {
        if (n < 2) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;
        for (int i = 3; i * i <= n; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }

    // ==========================================================
    // ALGORITMA PATHFINDING (DIJKSTRA)
    // ==========================================================
    public List<Integer> findShortestPath(int start, int end) {
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> parent = new HashMap<>();

        for (int i = 1; i <= totalSquares; i++) dist.put(i, Integer.MAX_VALUE);
        dist.put(start, 0);
        pq.offer(new int[]{start, 0});
        parent.put(start, null);

        while (!pq.isEmpty()) {
            int[] current = pq.poll();
            int u = current[0];
            int d = current[1];

            if (d > dist.get(u)) continue;
            if (u == end) return reconstructPath(parent, start, end);

            for (int step = 1; step <= 6; step++) {
                int v = u + step;
                if (v > totalSquares) continue;
                if (links.containsKey(v)) v = links.get(v).getTo(); // Follow link

                if (dist.get(u) + 1 < dist.get(v)) {
                    dist.put(v, dist.get(u) + 1);
                    parent.put(v, u);
                    pq.offer(new int[]{v, dist.get(v)});
                }
            }
        }
        return new ArrayList<>();
    }

    private List<Integer> reconstructPath(Map<Integer, Integer> parent, int start, int end) {
        List<Integer> path = new ArrayList<>();
        Integer curr = end;
        while (curr != null) {
            path.add(0, curr);
            curr = parent.get(curr);
        }
        return path;
    }

    // --- GETTERS ---
    public int getTotalSquares() { return totalSquares; }
    public Map<Integer, Link> getLinks() { return links; }

    // Cek apakah posisi ini adalah Bintang
    public boolean hasStar(int pos) { return starPositions.contains(pos); }

    // Cek apakah posisi ini adalah Prima (Menggunakan Set)
    // Ini aman sekarang karena calculatePrime punya nama berbeda
    public boolean isPrime(int pos) { return primePositions.contains(pos); }

    public int collectPoint(int pos) {
        // Hapus node dari map, dan simpan nilainya ke variabel 'val'
        Integer val = pointNodes.remove(pos);

        // Jika val tidak null (berarti ada poin), kembalikan val.
        // Jika null (tidak ada poin), kembalikan 0.
        return val != null ? val : 0;
    }
    public Map<Integer, Integer> getPointsMap() { return pointNodes; }
}