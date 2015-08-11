package com.xx.mylauncher;

import android.content.Intent;
import android.view.View;

/**
 * 单元格View的信息实体类
 * 
 * @author baoxing
 * 
 */
public class CellInfo {

	/**
	 * 单元格类型，目前包括快捷图标和 widget
	 * 
	 * @author baoxing
	 * 
	 */
	static enum CellType {
		SHORT_CUT, WIDGET
	}
	
	/**
	 * Item View 所处的位置
	 * @author baoxing
	 *
	 */
	static enum CellLocation {
		WORKSPACE, HOTSEAT
	}

	/** 代表的Item View */
	private View view;

	/** Intent信息 */
	private Intent intent;

	// TODO 应该包括其它可以保存到持久层的非对象信息，用来打开程序activity
	// class package ??
	//相应的也应该在自定义属性中补上
	
	/** 数据库中的id */
	private long id;

	/** 格子水平位置 */
	private int cellX;

	/** 格子垂直位置 */
	private int cellY;

	/** 格子X轴数量 */
	private int cellHSpan;

	/** 格子Y轴数量 */
	private int cellVSpan;

	/** 格子对应的名字 */
	private String iconName;

	/** 格子所代表的类型 */
	private CellType type;

	/** 所在的屏幕 */
	private int screen;
	
	/** 代表HotSeat中的水平格子位置 */
	private int hotSeatCellX;
	
	/** 代表HotSeat中的垂直格子位置 */
	private int hotSeatCellY;
	
	/** 所处的位置 */
	private CellLocation location;
	
	/** 指定的Widget Id */
	private int widgetId;
	
	/** 包名，如果有的话 */
	private String pkgName;
	
	public CellInfo() {

	}
	/** 代表的Item View */
	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	public Intent getIntent() {
		return intent;
	}

	public void setIntent(Intent intent) {
		this.intent = intent;
	}

	public int getCellX() {
		return cellX;
	}

	public void setCellX(int cellX) {
		this.cellX = cellX;
	}

	public int getCellY() {
		return cellY;
	}

	public void setCellY(int cellY) {
		this.cellY = cellY;
	}

	public int getCellHSpan() {
		return cellHSpan;
	}

	public void setCellHSpan(int cellHSpan) {
		this.cellHSpan = cellHSpan;
	}

	public int getCellVSpan() {
		return cellVSpan;
	}

	public void setCellVSpan(int cellVSpan) {
		this.cellVSpan = cellVSpan;
	}

	public String getIconName() {
		return iconName;
	}

	public void setIconName(String iconName) {
		this.iconName = iconName;
	}

	/**
	 * 单元格类型，目前包括快捷图标和 widget
	 * @return
	 */
	public CellType getType() {
		return type;
	}

	public void setType(CellType type) {
		this.type = type;
	}
	
	/**
	 * 返回所处的是第几个屏幕的
	 * @return
	 */
	public int getScreen() {
		return this.screen;
	}
	
	/**
	 * 设置所处的是第几个屏幕
	 * @param screen
	 */
	public void setScreen(int screen) {
		this.screen = screen;
	}
	
	public int getHotSeatCellX() {
		return hotSeatCellX;
	}

	public void setHotSeatCellX(int hotSeatCellX) {
		this.hotSeatCellX = hotSeatCellX;
	}

	public int getHotSeatCellY() {
		return hotSeatCellY;
	}

	public void setHotSeatCellY(int hotSeatCellY) {
		this.hotSeatCellY = hotSeatCellY;
	}
	public CellLocation getLocation() {
		return location;
	}
	
	public void setLocation(CellLocation location) {
		this.location = location;
	}
	
	public int getWidgetId() {
		return widgetId;
	}
	public void setWidgetId(int widgetId) {
		this.widgetId = widgetId;
	}
	
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public String getPkgName() {
		return pkgName;
	}
	
	public void setPkgName(String pkgName) {
		this.pkgName = pkgName;
	}
	
	@Override
	public String toString() {
		return "CellInfo [view=" + view + ", intent=" + intent + ", id=" + id
				+ ", cellX=" + cellX + ", cellY=" + cellY + ", cellHSpan="
				+ cellHSpan + ", cellVSpan=" + cellVSpan + ", iconName="
				+ iconName + ", type=" + type + ", screen=" + screen
				+ ", hotSeatCellX=" + hotSeatCellX + ", hotSeatCellY="
				+ hotSeatCellY + ", location=" + location + ", widgetId="
				+ widgetId + ", pkgName=" + pkgName + "]";
	}

}
