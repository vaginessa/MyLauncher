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

	/** 代表的View */
	private View view;

	/** Intent信息 */
	private Intent intent;

	// TODO 应该包括其它可以保存到持久层的非对象信息，用来打开程序activity
	// class package ??
	//相应的也应该在自定义属性中补上

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

	
	public CellInfo() {

	}

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

	public CellType getType() {
		return type;
	}

	public void setType(CellType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "CellInfo [view=" + view + ", intent=" + intent + ", cellX="
				+ cellX + ", cellY=" + cellY + ", cellHSpan=" + cellHSpan
				+ ", cellVSpan=" + cellVSpan + ", iconName=" + iconName
				+ ", type=" + type + "]";
	}

	
	
}
