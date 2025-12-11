public class Main {

    public static void main(String[] args) {
        Node C = new Node("C",null,null);
        Node B = new Node("B",C,null);
        Node E = new Node("E",null,null);
        Node F = new Node("F",null,null);
        Node D = new Node("D",E,F);
        Node A = new Node("A",B,D);

        Tree myBT = new Tree(A);
        myBT.preOrderTraversal(myBT.root);
        System.out.println();

    }
}
