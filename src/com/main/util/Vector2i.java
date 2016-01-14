package com.main.util;

public class Vector2i {
	
	//x and y component
	private int x, y;
	
	//constructors:
	public Vector2i(){
		set(0, 0);
	}
	
	public Vector2i(Vector2i vector){
		set(vector.x, vector.y);
	}
	
	public Vector2i(int x, int y){
		set(x, y);
	}
	
	//set the x and y components
	public void set(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	//return the x component
	public int getX(){
		return x;
	}
	
	//return the y component
	public int getY(){
		return y;
	}
	
	//Performs addition of two vectors
	public Vector2i add(Vector2i vector){
		this.x += vector.x;
		this.y += vector.y;
		return this;
	}
	
	//Performs subtraction of two vectors
	public Vector2i subtract(Vector2i vector){
		this.x -= vector.x;
		this.y -= vector.y;
		return this;
	}
	
	//sets the x
	public Vector2i setX(int x){
		this.x = x;
		return this;
	}
	
	//sets the y
	public Vector2i setY(int y){
		this.y = y;
		return this;
	}
	
	//shift the vector
	public Vector2i translate(int xOff, int yOff){
		x += xOff;
		y += yOff;
		return this;
	}
	
	//checks if two vectors are equal
	//NOTE: overides Object.equals();
	public boolean equals(Object object){
		if(!(object instanceof Vector2i)) return false;
		Vector2i vec = (Vector2i) object;
		if(vec.getX() == this.getX() && vec.getY() == this.getY()) return true;
		return false;
	}
	
	//returns the distance between two vectors
	public double getDistance(Vector2i vector){
		double dx = getX() - vector.getX();
		double dy = getY() - vector.getY();		
		return Math.sqrt(dx * dx + dy * dy);
	}

	//fits two vectors to a mutliple of eachother
	public void fitToMultiple(Vector2i target, int mul){
		
		//set temporary x and y values
		int xcount = target.getX();
		int ycount = target.getY();
		
		//handles if xcount is greater
		if(xcount > getX()){
			
			//shift xcount down by the specified  multiple
			while(xcount > getX()){
				xcount -= mul;
			}
		}else{
			
			//shift xcount up by the specified multiple
			while(xcount < getX()){
				xcount += mul;
			}
		}
		
		//set the x
		setX(xcount);
		
		//repeat for y component:
		if(ycount > getY()){
			while(ycount > getY()){
				ycount -= mul;
			}
		}else{
			while(ycount < getY()){
				ycount += mul;
			}
		}
		setY(ycount);
		
	}
}
