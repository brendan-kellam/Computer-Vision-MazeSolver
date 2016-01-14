package com.main.graphics;

import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.*;

import org.opencv.core.Mat;

public class FrameView {
	
	private JLabel display_component;
	
	private JFrame window;
	private JPanel layout;
	
	//width and height of frame
	private int width = 1280, height = 720;
	
	private String title;
	
	
	BufferedImage image;
	
	public FrameView(String title){
		this.title = title;
		init();	
	}
	
	public FrameView(String title, int width, int height){
		this.width = width;
		this.height = height;
		init();
	}
	
	//initialize frame
	private void init(){
		window = new JFrame(title);
        layout = new JPanel();
        layout.setLayout(new FlowLayout());
        window.getContentPane().add(layout);
        
        image =  new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        display_component = new JLabel(new ImageIcon(image));
        
        layout.add(display_component);
        window.pack();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);
        window.setVisible(true);        
	}
	
	//update frame
	public void update(Mat matrix){
		image = Mat2BufferedImage(matrix);
     	display_component.setIcon(new ImageIcon(image));
     	display_component.repaint();
	}
	
	//convert matrix to buffered image
	private BufferedImage Mat2BufferedImage(Mat m){
		 
	    int type = BufferedImage.TYPE_BYTE_GRAY;
	    if ( m.channels() > 1 ) {
	        type = BufferedImage.TYPE_3BYTE_BGR;
	    }
	    int bufferSize = m.channels()*m.cols()*m.rows();
	    byte [] b = new byte[bufferSize];
	    m.get(0,0,b); // get all the pixels
	    BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
	    final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
	    System.arraycopy(b, 0, targetPixels, 0, b.length);  
	    return image;
	}
	
	public JFrame getWindow(){
		return window;
	}
	
	public void closeWindow(){
		window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
	}
	
}
