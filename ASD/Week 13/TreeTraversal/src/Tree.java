public class Tree{
    Node root;

    public Tree(Node r){
        this.root = r;
    }

    public void preOrderTraversal(Node r){
        System.out.print(r.data+" ");
        if(r.left !=null){
            preOrderTraversal(r.left);
        }
        if(r.right !=null){
            preOrderTraversal(r.right);
        }


    }
    public void inOrderTraversal(Node r){
        if (r.left != null) {
            inOrderTraversal(r.left);
        }
        System.out.print(r.data + " ");
        if (r.right != null) {
            inOrderTraversal(r.right);
        }
    }
    public void postOrderTraversal(Node r){
        if (r.left != null){
            postOrderTraversal(r.left);
        }
        if (r.right != null){
            postOrderTraversal(r.right);
        }
        System.out.print(r.data + " ");
    }
}