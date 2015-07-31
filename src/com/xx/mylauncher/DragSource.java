package com.xx.mylauncher;

import android.view.View;

/**
 * 定义拖动源接口
 * @author baoxing
 *
 */
public interface DragSource {
	
	/**
	 * 设置控制模型层，实际控制拖动处理
	 * @param dragger	控制处理接口
	 */
    void setDragController(DragController dragger);
    
    /**
     * 设置拖放绘制层
     * @param dragLayer
     */
    void setDragLayer(DragLayer dragLayer);
    
    /**
     * 拖放结束回调
     * @param dropTargetView	放置的目标View
     * @param dragView				拖动的View
     * @param itemInfo				拖动的View携带的信息
     * @param rawX						释放时的rawX
     * @param rawY						 释放时的rawY
     * @param iOffDragLayerX	原来Item View的相对于父View{DropTarget: workspace}的坐标值X，绘制时需要转为相对于DragLayer的坐标
     * @param iOffDragLayerY	原来Item View的相对于父View{DropTarget: workspace}的坐标值Y
     * @param success					是否放置成功
     */
    void onDropCompleted(View dropTargetView, View dragView, Object itemInfo, int rawX, int rawY, int iOffX, int iOffy, boolean success);
	
}
