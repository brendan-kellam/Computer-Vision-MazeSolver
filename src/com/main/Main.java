package com.main;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.utils.Converters;

import com.main.graphics.Analysis;
import com.main.graphics.FrameView;
import com.main.input.Keyboard;
import com.main.util.Vector2i;
import com.main.util.Vector3i;



public class Main implements Runnable {
 
	//load opencv
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
	
	public static final Vector2i CAMERA_RESOLUTION = new Vector2i(1280, 720);
	
	
	//boolean indicating program and clock run
	private boolean running; 
	
	//main thread
	private Thread mainInstance;
	
	//opencv video capture (I.E camera)
	private VideoCapture capture;
	
	//matrix defining camera feed
	private Mat feed;
	
	//keyboard class
	private Keyboard keyboard;
	
	//Analysis class
	private Analysis graphicalAnalysis;
	
	
	//minimum and maximum area for a completion node
	public final int MIN_COMPLETION_NODE_AREA = 1000;
	public final int MAX_COMPLETION_NODE_AREA = 1700;
	
	
	
	//-- Create colour extremities --
	Scalar lower = new Scalar(0, 90, 169);
 	Scalar upper = new Scalar(180, 218, 255);

  	
 	//set lower and upper start node.
 	//start rgb = (180, 190, 215)
 	//Scalar lowerStartNode = new Scalar(101, 11, 158);
 	//Scalar upperStartNode = new Scalar(161, 255, 221);
 	
 	//id = 6
 	Scalar lowerStartNode = new Scalar(101, 38, 146);
 	Scalar upperStartNode = new Scalar(114, 124, 255);
 	
 	//id = 8
 	Scalar lowerEndNode = new Scalar(140, 90, 80);
 	Scalar upperEndNode = new Scalar(180, 150, 113);
 	
 	
 	//the start and end positions of the maze
 	private Vector2i start, end;
 	
	
	//Frameviews
	private FrameView rangeDisplay;
	private FrameView feedDisplay;
		
    public Main() {
 
        System.out.println("Running OpenCV " + Core.VERSION);
        
        Scanner reader = new Scanner(System.in);
        System.out.println("Opencv operation / analysis? (0)/(1)");
        int op = reader.nextInt();
        
        if(op == 0){
        	initWindow();
        	start();
        }else if(op == 1){
        	graphicalAnalysis = new Analysis();
        }
        
        
    }
    
    public void initWindow(){
    	capture = new VideoCapture(0);
        if(!capture.isOpened()) return;
        
        
        feedDisplay = new FrameView("Feed");
        keyboard = new Keyboard();
        feedDisplay.getWindow().addKeyListener(keyboard);
        
        rangeDisplay = new FrameView("Range Display");
        
        
    }
    
    public void update(){
    	keyboard.update();
    	
    	feed = new Mat();
		Mat median = new Mat();
		Mat hsvImage = new Mat();
		Mat finalImage = new Mat();
		
		Mat startNodeFilter = new Mat();
		Mat endNodeFilter = new Mat();
					
		
		//get image data from camera
     	capture.read(feed);
     	
     	
     	
     	/**
     	 * --- Handles direct maze processing ---
     	 */
     	//apply median blur and convert image to hsv
     	//feed.convertTo(feed, -1, 1, 10);
     	Imgproc.medianBlur(feed, median, 3);
     	Imgproc.cvtColor(median, hsvImage, Imgproc.COLOR_BGR2HSV);

     	
     	//get maze threshold
     	Core.inRange(hsvImage, lower, upper, finalImage);

     	
     	//apply blur to the maze threshold image
     	Imgproc.GaussianBlur(finalImage, finalImage, new Size(9, 9), 2, 2);
     	rangeDisplay.update(finalImage);
     	
     	
     	/**
     	 * --- Handles completion-node processing ---
     	 */
     	
     	//handle start node:
     	Core.inRange(hsvImage.clone(), lowerStartNode, upperStartNode, startNodeFilter);  

     	
     	Vector2i[] startPosition = getContourPositions(startNodeFilter, true);
     	
     	
     	if(startPosition != null && startPosition.length == 1){
     		Vector2i contour = startPosition[0];
     		start = new Vector2i(contour.getX(), contour.getY());
     		//System.out.println("Start: " + contour.getX() + " | " + contour.getY());
     	}
     	
     	/** --------- **/
     	
     	//handle end node:
     	Core.inRange(hsvImage.clone(), lowerEndNode, upperEndNode, endNodeFilter);

     	//get possible end position locations
     	Vector2i[] endPosition = getContourPositions(endNodeFilter, true);

     	//array must not be null and its length must be 1
     	if(endPosition != null && endPosition.length == 1){
     		Vector2i contour = endPosition[0];
     		end = new Vector2i(contour.getX(), contour.getY());
     		//System.out.println("End: " + contour.getX() + " | " + contour.getY());
     	}
     	
     	int size = (int)finalImage.total() * finalImage.channels();
     	byte[] data = new byte[size];     	
     	finalImage.get(0, 0, data);
     
     	//update displays
     	feedDisplay.update(feed);
     	
     	if(keyboard.enter && start != null && end != null){
     		System.out.println("capture");
     		
     		
     		//capture the processed image
     		Highgui.imwrite("/Users/poptart/Documents/java/Computer Vision/Test/res/test.jpg", finalImage);
     		
     		//create a new analysis instance
     		new Analysis(start, end);
     		
     		closeFrames();
     		//stop the main thread
     		running = false;
     	}
     	
     	//reset start and end positions
     	start = null;
     	end = null;
     	
	}
    
    /**
     * Note: This function returns a <b>vector2i array</b>. The represented components are:
     * Note: If no contours are found, the return will be <b>null</b>
     */
    private Vector2i[] getContourPositions(Mat sample, boolean draw){
    	
    	//set contour positions array to null initially
    	Vector2i[] contourPositions = null;
    	
    	//create dilate and erode components
		Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(8, 8));
		Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 1));
		
		//apply two erodes to sample image
		Imgproc.erode(sample, sample, erodeElement);
		Imgproc.erode(sample, sample, erodeElement);

		//apply two dilates to sample image
		Imgproc.dilate(sample, sample, dilateElement);
		Imgproc.dilate(sample, sample, dilateElement);

     	
		//Instantiate a contours list and hierarchy matrix for finding contours
     	List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
     	Mat hierarchy = new Mat();
     	
     	
     	//clone completionNodes matrix
     	//Note: this is a important step and program will be sub-optimal without.
     	Mat sampleCopy = sample.clone();
     	
     	//find contours in the nodeCopy matrix
     	Imgproc.findContours(sampleCopy, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
     	     	     	
     	
     	//check if any contours exist
     	if (hierarchy.size().height > 0 && hierarchy.size().width > 0) {
     		   
     			
     			//valid contours arraylist
     			List<Vector2i> contourList = new ArrayList<Vector2i>();
     			
     	        //loop contour list
     			for(int idx = 0; idx < contours.size(); idx++){
     				Mat contour = contours.get(idx);
     				
     				//get area of contour
     				double area = Imgproc.contourArea(contour);
     				
     				//filter out invalid contour data through area analysis
     				if(area > MIN_COMPLETION_NODE_AREA && area < MAX_COMPLETION_NODE_AREA){
     					     					
     					//get the moment from the given contour
     					Moments moment = Imgproc.moments(contour, false);
     					
     					//calculate the x and y pos of the contour
     					int x = (int) (moment.get_m10() / moment.get_m00());
     			        int y = (int) (moment.get_m01() / moment.get_m00());
     			        
     			        
     			        //add new position vector to contour list
     			        contourList.add(new Vector2i(x, y));
     			        
     			        //if drawing is permitted
     			        if(draw){
	     			        //draw the contour on the main feed
	         				Imgproc.drawContours(feed, contours, idx, new Scalar(255, 230, 0));
     			        }
         				
     				}
     				
     				
     			}
     			
     		
     			
     		//convert contourList into a standard array
     		contourPositions = new Vector2i[contourList.size()];
     		contourList.toArray(contourPositions);
     	}
     	
     	//return the vector2i array
    	return contourPositions;
    }
	

	@Override
	public void run() {
		long lastTime = System.nanoTime();
		long timer = System.currentTimeMillis();
		final double ns = 1000000000.0 / 30.0;
		double delta = 0;
		int frames = 0;
		int updates = 0;
		while(running){ //game loop
			long now = System.nanoTime();
			delta += (now-lastTime) / ns;
			lastTime = now;
			while (delta>=1){
				update(); //restricted to 60 fps	
				render();
				updates++;
				delta--;
			}
			//render(); // unrestricted
			frames++;
			
			if(System.currentTimeMillis() - timer > 1000){
				timer += 1000;

				updates = 0;
				frames = 0;
			}
		}
 		
		stop();
	}

	//Currently not used
	public void render(){

	}
	
	public synchronized void start(){ //synchronized is used to prevent overlaps with threads
		running = true;
		mainInstance = new Thread(this, "Display"); //this thread will be attached to this instance of Game
		mainInstance.start();
	}
	
	//closes all display frames
	private synchronized void closeFrames(){
		rangeDisplay.closeWindow();
		feedDisplay.closeWindow();
	}
		
	
	public synchronized void stop(){ //stops the thread (used for applets)
		running = false;
		try {
			mainInstance.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
    
    public static void main(String[] args) {
    	new Main();
    }
    
}
