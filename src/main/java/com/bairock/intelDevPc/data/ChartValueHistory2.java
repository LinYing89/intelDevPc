package com.bairock.intelDevPc.data;

public class ChartValueHistory2 {

	private String name;
	private float y;
	
	public ChartValueHistory2() {}
	
	public ChartValueHistory2(String name, float y) {
		this.name = name;
		this.y = y;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public float getY() {
		return y;
	}
	public void setY(float y) {
		this.y = y;
	}
	
	
}
