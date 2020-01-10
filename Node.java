import java.util.*;

public class Node {

	Node parent;
	int[] camerasPosition;
	LinkedList<Node> children = new LinkedList<Node>();
	int f;
	double heuristic;
	int cost;//number of cameras
	
	public Node(int[] cameras, int heuristic , Node parent){
		this.parent= parent;
		camerasPosition = cameras;
		cost = camerasPosition.length/2;
		f= heuristic+(camerasPosition.length/2);
	}
	
	public Node(int[] cameras, double heuristic , Node parent){
		this.parent= parent;
		this.heuristic = heuristic;
		camerasPosition = cameras;
	}
	
	public void addChild(Node child) {
		children.add(child);
	}
	
	public int getF() {
		return f;
	}
	
	public int[] getCameras() {
		return camerasPosition;
	}
	
	public LinkedList<int[]> printPath() {
		
		LinkedList<int[]> list = new LinkedList<int[]>();
		list.add(this.camerasPosition);
		Node current = this.parent;
		while(current!=null) {
			 list.add(current.camerasPosition);
			 current=current.parent;
		}
		
		return list;
	}
}
