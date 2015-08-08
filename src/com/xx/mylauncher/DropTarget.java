package com.xx.mylauncher;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewParent;

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
    
    /**
     * 是否开启了放置功能
     * @return
     */
    boolean isDropEnable();
    
    /**
     * 这个是扩展用的，是因为没写出满足条件的布局（即DragLayer[Workspace, HotSeat(DeleteZone)]），如果去实现这样直接的<br/>
     * 嵌套布局，则使用{@link #getHitRect(Rect)} 更方便，但是该函数同样适用。Drop-in<br/>
     * 
     * 如何实现这个：<br/>
     * (1)遍历搜索【深度优先】<br/>
     * (2)尝试 view.getParent来判断，可行的话，就简单了<br/>
     * 第二种方法可行
     * 
     * @param outRect
     */
    void getHitRectRefDragLayer(Rect outRect, DropTarget dropTarget);
    
    /*
     * 在View中实现
     */
    
    void getHitRect(Rect outRect);
    void getLocationOnScreen(int[] loc);
    int getLeft();
    int getTop();
    ViewParent getParent();
    
    
}
