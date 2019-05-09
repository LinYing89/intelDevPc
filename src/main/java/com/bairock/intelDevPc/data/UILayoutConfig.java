package com.bairock.intelDevPc.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class UILayoutConfig {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	// 宫格布局
	public static final int LAYOUT_GRID = 0;
	// 列表布局
	public static final int LAYOUT_LIST = 1;

	// 状态设备布局方式, 0宫格, 1列表
	@Column(columnDefinition = "int default 0", nullable = false)
	private int stateDevLayout = LAYOUT_GRID;
	// 数值设备布局方式, 0宫格, 1列表
	@Column(columnDefinition = "int default 1", nullable = false)
	private int valueDevLayout = LAYOUT_LIST;

	// 主界面SplitPane divider位置
	@Column(columnDefinition = "double default 0.7", nullable = false)
	private double dividerRoot = 0.7;
	// 主界面设备SplitPane divider位置
	@Column(columnDefinition = "double default 0.7", nullable = false)
	private double dividerDevice = 0.7;
	// 主界面工具视图SplitPane divider位置
	@Column(columnDefinition = "double default 0.5", nullable = false)
	private double dividerView = 0.5;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getStateDevLayout() {
		return stateDevLayout;
	}

	public void setStateDevLayout(int stateDevLayout) {
		this.stateDevLayout = stateDevLayout;
	}

	public int getValueDevLayout() {
		return valueDevLayout;
	}

	public void setValueDevLayout(int valueDevLayout) {
		this.valueDevLayout = valueDevLayout;
	}

	public double getDividerRoot() {
		return dividerRoot;
	}

	public void setDividerRoot(double dividerRoot) {
		this.dividerRoot = dividerRoot;
	}

	public double getDividerDevice() {
		return dividerDevice;
	}

	public void setDividerDevice(double dividerDevice) {
		this.dividerDevice = dividerDevice;
	}

	public double getDividerView() {
		return dividerView;
	}

	public void setDividerView(double dividerView) {
		this.dividerView = dividerView;
	}

}
