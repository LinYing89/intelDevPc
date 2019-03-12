package com.bairock.intelDevPc.data;

/**
 * 历史纪录去向中的异常区域
 * @author 44489
 *
 */
public class ChartPlotBands {

	private String color = "#FFC1C1";
	private int from;
	private int to;
	
	public ChartPlotBands() {}
	
	public ChartPlotBands(int from, int to) {
		this.from = from;
		this.to = to;
	}
	
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public int getFrom() {
		return from;
	}
	public void setFrom(int from) {
		this.from = from;
	}
	public int getTo() {
		return to;
	}
	public void setTo(int to) {
		this.to = to;
	}
	
}
