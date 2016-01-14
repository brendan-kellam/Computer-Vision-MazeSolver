package com.main.graphics;

/**
 * Represents pixel clusters used in the analysis stage. <br>
 * 
 * <b>Attributes: </b>
 * <ul>
 * 	<li>Cluster's hold absolute x and y positions relative to the origin (0,0)[top left corner]</li>
 * 	<li>Cluster's hold pixel data relating to the original image data</li>
 * 	<li>Cluster's have a percentage yield value that rates the clusters validity. (<b>See:</b> Analysis.java)
 * </ul>
 *
 * <b>General Desc.: </b>
 * <ul>
 * 	<li>Clusters are used as precise representations from the real world. They allow pixel data to be converted into tangible and
 * navigable information.</li>
 * </ul>
 */

public class Cluster {
	
	private int x, y;
	
	//the percentage yield of the cluster
	private double yield;
	
	//the original pixels of the cluster found
	private int[] pixels;
	
	public Cluster(int x, int y, double yield, int[] pixels){
		this.x = x;
		this.y = y;
		this.yield = yield;
		this.pixels = pixels;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public double getYield(){
		return yield;
	}
	
	public int[] getPixels(){
		return pixels;
	}
	
}
