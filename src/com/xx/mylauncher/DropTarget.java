package com.xx.mylauncher;

import android.graphics.Rect;

/**
 * 定义拖动目的接口
 * @author baoxing
 *
 */
public interface DropTarget {
	
	/**
	 * 可以放置时调用的函数
	 * @param source	从哪里拖动过来的，拖动源
	 * @param x	到父View的偏移量{@link DropTarget}，如workspace
	 * @param y	到父View的偏移量{@link DropTarget}，如workspace
	 * @param xOffset	到View本身的偏移量，即到长按下的View的偏移量
	 * @param yOffset
	 * @param dragView	拖动的View，绘制表现层在DragLayer中
	 * @param dragInfo	拖动的View所携带的信息
	 */
    void onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo);
    
    /**
     * 拖动进入{@link DropTarget} 时回调
	 * @param source	从哪里拖动过来的，拖动源
	 * @param x	到父View的偏移量{@link DropTarget}，如workspace
	 * @param y	到父View的偏移量{@link DropTarget}，如workspace
	 * @param xOffset	到View本身的偏移量，即到长按下的View的偏移量
	 * @param yOffset
	 * @param dragView	拖动的View，绘制表现层在DragLayer中
	 * @param dragInfo	拖动的View所携带的信息
     */
    void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo);

    /**
     * 拖动移动时回调
     * @param source
     * @param x
     * @param y
     * @param xOffset
     * @param yOffset
     * @param dragView
     * @param dragInfo
     */
    void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo);

    /**
     * 拖动离开{@link DropTarget} 时回调
     * @param source
     * @param x
     * @param y
     * @param xOffset
     * @param yOffset
     * @param dragView
     * @param dragInfo
     */
    void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo);

    /**
     * 是否可以放置
	 * @param source	从哪里拖动过来的，拖动源
	 * @param x	到父View的偏移量{@link DropTarget}，如workspace
	 * @param y	到父View的偏移量{@link DropTarget}，如workspace
	 * @param xOffset	到View本身的偏移量，即到长按下的View的偏移量
	 * @param yOffset
	 * @param dragView	拖动的View，绘制表现层在DragLayer中
	 * @param dragInfo	拖动的View所携带的信息
     * @return
     */
    boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo);
    
    
    
    
    
    /*
     * 在View中实现
     */
    
    void getHitRect(Rect outRect);
    void getLocationOnScreen(int[] loc);
    int getLeft();
    int getTop();
    
    
    
}
