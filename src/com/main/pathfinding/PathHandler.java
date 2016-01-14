package com.main.pathfinding;
import java.util.List;

import com.main.graphics.Analysis;
import com.main.graphics.Cluster;
import com.main.util.Vector2i;

/**
 * Determines a valid path dependent on the inputed data via the analysis computation
 * 
 * <h1> Outlined Steps: </h1>
 * <ul>
 * 	<li>PathHandler initially accepts a ArrayList of valid clusters, computed via the Analysis.java file</li>
 * 	<li>The extremities of the clusters is determined, producing a maxima for both x and y</li>
 * 	<li>The valid cluster ArrayList is then converted into a <b>array,</b> called a <b>cluster map:</b></li>
 * 	<ul>
 * 		<li>The cluster map array will have the extremities retaining to the previously mentioned maxima and minima</li>
 * 		<li>The valid clusters will be added with a new <i>relative space</i> coordinate format (I.E if the absolute minima cluster had
 * 		a location of x: 715, y: 640, its new <i>relative space</i> location would be x: 0, y: 0) </li>
 * 			<ul><li><b>NOTE:</b> This coordinate format does not affect the valid cluster's <b>original location</b>, this information is 
 * 			retained within <b>Cluster.java</b></li></ul>
 * 		<li>Along with valid clusters, <b>null</b> data will be added at locations where no valid clusters exist</li>
 * 	</ul>
 * 
 * 	<li><b>Note:</b> It is important to remember the above cluster map array <b>only</b> applies to pathfinding, and upon final computation,
 * 	the pathfinding <i>data</i> will be converted back into conventional <b>cluster</b> format</li>
 * 	<li>Following the computation of the path, a list of clusters representing said </li>
 * </ul>
 */

public class PathHandler {
	
	//valid cluster list
	private List<Cluster> validClusters;
	
	//the start and end cluster locations
	private Vector2i start;
	private Vector2i end;
	
	//the relative start and end locations (based on clusterMap location format)
	private Vector2i relStart;
	private Vector2i relEnd;
	
	//the minimum and maximum valid cluster locations
	private Vector2i minimum;
	private Vector2i maximum;
	
	//clusterMap used for pathfinding purposes
	private Cluster[] clusterMap;
	
	Vector2i relativeMin;
	
	//width and height of clusterMap
	int width, height;

	
	
	//constructor
	public PathHandler(List<Cluster> validClusters){
		
		//instantiate valid clusters
		this.validClusters = validClusters;
		
	}
	
	
	//determines a new path from a start vector to a end vector
	//on completion, the function either returns:
		//1. a null value, indicating no path was found
		//2. a Cluster[] array, with each cluster representing a location node. 
	//NOTE: The clusters are represented in original location format, and therefore can be directly rendered to the screen
	public Cluster[] determinePath(Vector2i start, Vector2i end){
		
		//instantiate start and end nodes
		this.relStart = this.start = start;
		this.relEnd = this.end = end;
		
		
		//determine the minima and maxima of the valid clusters
		determineExtremities();
		
		//create a new clusterMap
		buildClusterMap();
		
		//create a new instance of the A* algorithm
		Star star = new Star(this.clusterMap, width, height);
		
		//calculate a new path between the relativeStart and relativeEnd vectors
		List<Node> path = star.findPath(relStart, relEnd);
		
		//if the path is null, then return null
		if(path == null){
			System.out.println("No Path Found!");
			return null;
		}
		
		//---Node to cluster conversion---//
		
		//create a new cluster based path for conversion from node -> cluster
		Cluster[] clusterPath = new Cluster[path.size()];
		
		//set a arbitrary increment
		int count = 0;	
		
		//loop the path arraylist
		for(Node node : star.findPath(relStart, relEnd)){
			
			//get the x and y location of each node (in relative clusterMap format)
			int x = node.cluster.getX();
			int y = node.cluster.getY();
			
			//create a new cluster instance with x and y coordinates (in original location format)
			clusterPath[count] = new Cluster((x+ relativeMin.getX())*Analysis.SIZE , (y+ relativeMin.getY())*Analysis.SIZE, 1.0, null);
			
			//increment counter
			count++;
		}
		
		//return the clusterPath
		return clusterPath;
	
	}
	
	//build a new clusterMap
	private void buildClusterMap(){
		
		//determine the width and height of the clusterMap
		//NOTE: the width and height represent the "cluster count" across the x and y axis's respectively. For example, a valid
		//dimension could be: 14 clusters by 10 clusters
		width = (maximum.getX() - minimum.getX())/Analysis.SIZE + 1;
		height = (maximum.getY() - minimum.getY())/Analysis.SIZE + 1;
		
		//create a new clusterMap array
		clusterMap = new Cluster[width*height];

		
		//relativeMin is the minimum cluster location in clusterMap location format
		relativeMin = new Vector2i(minimum.getX()/Analysis.SIZE, minimum.getY()/Analysis.SIZE);
		
		
		System.out.println("WIDTH: " + width + " | HEIGHT: " + height);
		
		
		//loop all valid clusters
		for(Cluster cluster : validClusters){
			
			//convert the location of the cluster into the clusterMap location space
			int x = cluster.getX()/Analysis.SIZE - relativeMin.getX();
			int y = cluster.getY()/Analysis.SIZE - relativeMin.getY();
			
			//System.out.println(cluster.getX() + " " + cluster.getY());
			//System.out.println(x + " " + y);
			
			//add valid cluster to cluster map
			clusterMap[x + y * width] = cluster;
			
		}
		
		//convert the starting and ending nodes to clusterMap location format
		relStart.set(start.getX()/Analysis.SIZE - relativeMin.getX(), start.getY()/Analysis.SIZE - relativeMin.getY());
		relEnd.set(end.getX()/Analysis.SIZE - relativeMin.getX(), end.getY()/Analysis.SIZE - relativeMin.getY());
		
		System.out.println("Start: " + relStart.getX() + " " + relStart.getY());
		System.out.println("End  : " + relEnd.getX() + " " + relEnd.getY());
		
		Cluster c = clusterMap[relEnd.getX() + relEnd.getY() * width];

	}
	
	
	//determine the extremities relating to the valid clusters
	private void determineExtremities(){
		
		//create new minimum and maximums
		//uses first element in validClusters as default
		Vector2i min = new Vector2i(validClusters.get(0).getX(), validClusters.get(0).getY());
		Vector2i max = new Vector2i(validClusters.get(0).getX(), validClusters.get(0).getY());
		
		Cluster tempStart = new Cluster(start.getX(), start.getY(), 1.0, null);
		Cluster tempEnd = new Cluster(end.getX(), end.getY(), 1.0, null);
		validClusters.add(tempStart);
		validClusters.add(tempEnd);
		
		//loop valid cluster list
		for(Cluster cluster : validClusters){
			
			//get the location of each cluster
			int x = cluster.getX();
			int y = cluster.getY();
			
			//check lower bounds for x
			if(x < min.getX()) min.setX(x);
				
			//check upper bounds for x
			else if(x > max.getX()) max.setX(x);
			
			
			//check lower bounds for y
			if(y < min.getY()) min.setY(y);
				
			//check upper bounds for y
			else if (y > max.getY()) max.setY(y);
			
		}
		
		validClusters.remove(tempStart);
		validClusters.remove(tempEnd);
		
		this.minimum = min;
		this.maximum = max;
				
		System.out.println("MIN: " + min.getX() + " " + min.getY());
		System.out.println("MAX: " + max.getX() + " " + max.getY());
		
	}
	
}
