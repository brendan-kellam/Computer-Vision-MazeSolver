package com.main.util;

public class Vector3i {
	
	//x, y and z components
	private int x, y, z;
	
	//constructors:
	public Vector3i(){
		set(0, 0, 0);
	}
	
	public Vector3i(Vector3i vector){
		set(vector.x, vector.y, vector.z);
	}
	
	public Vector3i(int x, int y, int z){
		set(x, y, z);
	}
	
	//set the x, y and z components
	public void set(int x, int y, int z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	//return the x component
	public int getX(){
		return x;
	}
	
	//return the y component
	public int getY(){
		return y;
	}
	
	//return the z component
	public int getZ(){
		return z;
	}
	
	//Performs addition of two vectors
	public Vector3i add(Vector3i vector){
		this.x += vector.x;
		this.y += vector.y;
		this.z += vector.z;
		return this;
	}
	
	//Performs subtraction of two vectors
	public Vector3i subtract(Vector3i vector){
		this.x -= vector.x;
		this.y -= vector.y;
		this.z -= vector.z;
		return this;
	}
	
	//sets the x
	public Vector3i setX(int x){
		this.x = x;
		return this;
	}
	
	//sets the y
	public Vector3i setY(int y){
		this.y = y;
		return this;
	}
	
	//sets the z
	public Vector3i setZ(int z){
		this.z = z;
		return this;
	}
	
	//shift the vector
	public Vector3i translate(int xOff, int yOff, int zOff){
		x += xOff;
		y += yOff;
		z += zOff;
		return this;
	}
	
	//checks if two vectors are equal
	//NOTE: overides Object.equals();
	public boolean equals(Object object){
		if(!(object instanceof Vector3i)) return false;
		Vector3i vec = (Vector3i) object;
		if(vec.getX() == this.getX() && vec.getY() == this.getY() && vec.getZ() == this.getZ()) return true;
		return false;
	}
	
	//returns the distance between two vectors
	public double getDistance(Vector3i vector){
		double dx = getX() - vector.getX();
		double dy = getY() - vector.getY();		
		double dz = getZ() - vector.getZ();
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	//fits two vectors to a mutliple of eachother
	public void fitToMultiple(Vector3i target, int mul){
		
		//set temporary x and y values
		int xcount = target.getX();
		int ycount = target.getY();
		int zcount = target.getZ();
		
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
		
		
		//repeat for z component:
		if(zcount > getZ()){
			while(zcount > getZ()){
				zcount -= mul;
			}
		}else{
			while(zcount < getZ()){
				zcount += mul;
			}
		}
		setZ(zcount);
		
	}
}
