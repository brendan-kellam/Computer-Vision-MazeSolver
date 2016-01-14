package com.main.pathfinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.main.graphics.Cluster;
import com.main.util.Vector2i;

/**
 * Handles the instantiation, computation and termination of the A* pathfinding algorithm
 * 
 * <h1> Outline of operation: </h1>
 * 
 * <ul>
 * 	<li>The Star.java constructor initially accepts a clusterMap array, as well as its width and height (NOTE: See PathHandler.java)</li>
 * 	<li>Upon being called, the A* algorithm processes the cluster data. Notes:</li>
 * 	<ul>
 * 		<li>The algorithm treats each <b>null</b> cluster as a valid location</li>
 * 		<li>All <b>valid clusters</b> are seen as obstacles</li>
 * 	</ul>
 * 	<li>The algorithm then returns either: </li>
 * 	<ul>
 * 		<li><b>null - </b>If the path could not be found</li>
 * 		<li><b>path arrayList - </b>If the path was found</li>
 * 	</ul>
 * </ul>
 * <b>NOTE:</b> It is important to remember all cluster operations are in <b>clusterMap array location format</b> 
 */

public class Star {

	//clusterMap holds all clusters
	private Cluster[] clusterMap;
	
	private int width, height;
	
	//takes in two node opjects, and compares them
	private Comparator<Node> nodeSorter = new Comparator<Node>(){ 
		public int compare(Node n0, Node n1) {
			if(n1.fCost < n0.fCost) return +1; //move n1 up in index
			if(n1.fCost > n0.fCost) return -1; //move n1 down in index
			return 0;							//if the nodes are the same
		}
	};
	
	//constructor
	public Star(Cluster[] clusterMap, int width, int height){
		
		//Instantiate cluster map
		this.clusterMap = clusterMap;
		
		//Instantiate width and height
		this.width = width;
		this.height = height;
	}
	
	//Finds a new path given a start and finish
	public List<Node> findPath(Vector2i start, Vector2i end){
		
		List<Node> openList = new ArrayList<Node>(); //every tile that is being considered
		List<Node> closedList = new ArrayList<Node>(); //after a tile has been processed, it will be added here
		
		//the current node
		Node current = new Node(start, null, 0, start.getDistance(end));
		openList.add(current);
		
		//if the start is already at the goal, simply return the openList
		if(start.equals(end)){
			return openList;
		}
		
		//while the openList is still occupied
		while(openList.size() > 0){
			
			Collections.sort(openList, nodeSorter); //sorts our openList from lowest fCost to highest
			current = openList.get(0); //gets the starting node, the one with the lowest cost, determined by the comparator
			
			//if the current tile is the goal
			if(current.cluster.equals(end)){
				List<Node> path = new ArrayList<Node>();
				while(current.parent != null){ //retrases steps from the finish to the start
					path.add(current); //adds each node back into the path arraylist
					
					//re-sets the parent
					current = current.parent;
				}
				
				//clears the open and closed list
				openList.clear();
				closedList.clear();
				return path;
			}
			
			openList.remove(current); //remove the current node from the open list and add it to the closed
			closedList.add(current);
			for(int i = 0; i < 9; i++){ //checks all nodes, 4 is the middle
				if(i == 4) continue;
				//if(i % 2 == 0) continue;
				
				//gets the x and y coordinate of the given point
				int x = current.cluster.getX();
				int y = current.cluster.getY();
				
				
				//produces a quadrant-checking like system, starting from the top left, reaching the bottom right
				int xi = ((i % 3) - 1);
				int yi = ((i / 3) - 1);
				
				//System.out.println((xi + x) + " " + (yi + y));


				if(!isValidLocation(xi + x, yi + y)){
					continue;
				}
								
				Vector2i a = new Vector2i(x + xi, y + yi); //tile in vector form
				double gCost = current.gCost + (current.cluster.getDistance(a) == 1 ? 1 : 0.95); //gets the distance from the middle
				double hCost = a.getDistance(end); //determines the h cost
				
				//creates a new node using this data
				Node node = new Node(a, current, gCost, hCost);
				
				//if the node is not worth adding to the openlist due to it being both in the closed list and having a high gcost
				if(vecInList(closedList, a) && gCost >= node.gCost) continue;
				
				//if the node is not in the closed list and has a lower gcost, add it to the openlist
				if(!vecInList(openList, a) || gCost < node.gCost) openList.add(node);
			}
		}
		
		//if no path is found, clear the closed list and return null
		closedList.clear();
		
		return null;
	}
	
	public Cluster getCluster(int x, int y){
		
		if(x < 0 || x >= width || y < 0 || y >= height)
			return null;
		
		//return the given cluster
		return clusterMap[x + y * width];		
	}
	
	public boolean isValidLocation(int x, int y){
		if(x < 0 || x >= width || y < 0 || y >= height){
			return false;
		}
		
		if(clusterMap[x + y * width] != null) return false;
		else return true;
	}
	
	//determines if a vector is in a list
	private boolean vecInList(List<Node> list, Vector2i vector){
		
		//loop the list
		for(Node n : list){
			
			//return true if the vector is contained
			if(n.cluster.equals(vector)) return true;
		}
		//return false if not
		return false;
	}
	
	
		
}
