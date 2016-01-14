package com.main.graphics;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.main.Main;
import com.main.pathfinding.PathHandler;
import com.main.util.Vector2i;


/**
 * Handles analysis from gathered image of maze.
 * 
 * <h1>Details of Maze Analysis:</h1> <br>
 * 
 * <ol>
 * 	<li>Obtain BufferedImage from resource folder. Convert this buffered image into a array pixel raster.</li><br>
 * 
 * 	<li>Scan in n*n clusters, starting from the origin (0, 0)[top left]. For each cluster:</li>
 * <ul>
 * 	<li>Check each individual pixel for colour data.</li>
 * 	<li>All pixels != 0xff000000 then cluster marked as valid (<b>Note:</b> 100% pixel requirement may be shifted)</li>
 * 	<li>If marked as invalid, cluster is discarded.</li>
 * </ul><br>
 * 
 * <li>Valid clusters are added to a array list to be further processed.</li><br>
 * 
 * <li>Reconstruct Mazed based of validated clusters. [<b>Note:</b> Further plans need to be made]</li>
 *  
 * </ol>
 **/
public class Analysis {

	private String path = "/test.jpg";
	
	private int imageH, imageW;
	
	//represents the cluster size 
	public final static int SIZE = 5;
	
	//the required pixel yield for a cluster to be marked as valid
	private final double REQUIREDYIELD = 0.75;
	
	//rasterized pixel buffer
	private int[] pixels;
	
	//list of validated clusters
	private List<Cluster> validClusters = new ArrayList<Cluster>();
	
	//pixel buffer for the reconstructed clusters
	private int[] refinedPixels;
	
	//the reconstructed clusters in bufferedImage format
	BufferedImage reconstructedImage;
	
	
	private Vector2i start, end;
	
	
	public Analysis(){
		init();
	}
	
	public Analysis(Vector2i start, Vector2i end){
		this.start = start;
		this.end = end;
		init();
	}
	
	//initialize analysis
	private void init(){
		System.out.println("Running analysis v1.0");

		
		//fit the start and end node points to the cluster multiple size
		start.fitToMultiple(Main.CAMERA_RESOLUTION, SIZE);
		end.fitToMultiple(Main.CAMERA_RESOLUTION, SIZE);
		
		System.out.println("Start --  x: " + start.getX() + " | y: " + start.getY());
		System.out.println("End   --  x: " + end.getX() + " | y: " + end.getY());
		
		
		//obtain the processed image
		obtainImage();
		
		System.out.println("Width: " + imageW + " | Height: " + imageH);
		splitByClusters();
		
		
		PathHandler pathHandler = new PathHandler(validClusters);

		Cluster[] path = pathHandler.determinePath(start, end);
		
		
		
		for(Cluster cluster : path){
			
			System.out.println(cluster.getX() + ", " + cluster.getY());
			
			validClusters.add(cluster);
		}
		
		
		reconstructClusters();
	}
	
	private void reconstructClusters(){
		
		refinedPixels = new int[imageW * imageH];
		
		for(int i = 0; i < refinedPixels.length; i++){
			refinedPixels[i] = 0xff000000;
		}
		
		for(Cluster cluster : validClusters){
			
			int x = cluster.getX();
			int y = cluster.getY();
			
			//get the x and y extremities for the cluster
			int relSizeX = x + SIZE;
			int relSizeY = y + SIZE;
			
			for(int ya = y; ya < relSizeY; ya++){
				//the relative position in the cluster
				int relY = ya - y;
				
				for(int xa = x; xa < relSizeX; xa++){
					int relX = xa - x;
					
					refinedPixels[xa + ya * imageW] = 0xffffffff;
					//refinedPixels[xa + ya * imageW] = cluster.getPixels()[relX + relY * size];
				
				}
			}
			
		}
	
		reconstructedImage = new BufferedImage(imageW, imageH, BufferedImage.TYPE_INT_RGB);
		reconstructedImage.setRGB(0, 0, imageW, imageH, refinedPixels, 0, imageW);
		
		try {
			File output = new File("/Users/poptart/Documents/java/Computer Vision/Test/res/refined.jpg");
			ImageIO.write(reconstructedImage, "jpg", output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * <b> Considerations: </b>
	 * <ul>
	 * 	<li>The last pixel in the x and y components <b>is not</b> checked</li>
	 * 	<li>The yield for each cluster will lower by a value of 0.4 for each discarded pixel. (<b>Note:</b> This assumes current metrics I.E: size = 5)</li>
	 * </ul>
	 */
	private void splitByClusters(){
		
		//the scan area must be n pixels less than the border
		int maxX = imageW - SIZE;
		int maxY = imageH - SIZE;
		
		//----outward scan----
		for(int y = 0; y <= maxY; y+=SIZE){
			
			for(int x = 0; x <= maxX; x+=SIZE){
				
				
				//NOTE: Commeted code bellow adds single cluster to start and end locations
				/*
				if(x == start.getX() && y == start.getY()){
					Cluster cluster = new Cluster(x, y, 1.0, null);
					validClusters.add(cluster);
				}
				
				if(x == end.getX() && y == end.getY()){
					Cluster cluster = new Cluster(x, y, 1.0, null);
					validClusters.add(cluster);
				}*/
				
				//get the x and y extremities for the cluster
				int relSizeX = x + SIZE;
				int relSizeY = y + SIZE;
				
				int count = 0;
				int[] onscreenPixels = new int[SIZE*SIZE];
				
				//----inward scan----
				for(int ya = y; ya < relSizeY; ya++){
					
					//the relative position in the cluster
					int relY = ya - y;
					
					for(int xa = x; xa < relSizeX; xa++){
						int relX = xa - x;
						
						int pixel = pixels[xa + ya * imageW];
						onscreenPixels[relX + relY * SIZE] = pixel;
						
														
						int r = (pixel & 0xff0000) >> 16;
						int g = (pixel & 0xff00) >> 8;
						int b = (pixel & 0xff);
						
						if(r > 230 && g > 230 && b > 230){
							count++;
						}
					}
				}
				
				//calculate yield
				double yield = count / (SIZE*SIZE*1.0);
				//System.out.println(yield);
				
				//the percentage yield of a given pixel cluster must be >= requiredYield%
				if(yield >= REQUIREDYIELD){
					System.out.println("valid cluster -  x: " + x + " | y: " + y);
					System.out.println(yield*100 + "%");
					
					//create a new cluster with the data
					Cluster cluster = new Cluster(x, y, yield, onscreenPixels);
					validClusters.add(cluster);
					
					
					
				}
				
				
				//System.out.println(x + " " + y);
			}
		}
		
	}
	
	private void obtainImage(){
		try{
			BufferedImage image = ImageIO.read(Analysis.class.getResource(path));
			imageW = image.getWidth();
			imageH = image.getHeight();
			pixels = new int[imageW*imageH];
			image.getRGB(0, 0, imageW, imageH, pixels, 0, imageW);
		} catch(IOException e){
			e.printStackTrace();
		}
		
	}
		
		
}
