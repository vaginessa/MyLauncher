package com.xx.mylauncher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * 拖动层，绘制icon的拖动效果及拖动时的一些辅助效果
 * @author baoxing
 *
 */
public class DragLayer extends FrameLayout {
	
	private static final String TAG = "DragLayer";
	
	private DragController m_DragController;
	
	/** 更新在拖动时的辅助信息，比如拖动到哪的可以预绘制的边框，不可以放置时的红色蒙板提醒 */
	private CellLayout.DragObjectInfo m_DragObjectInfo;

	/** 测试用 */
	private Paint m_PaintTemp;
	
	public DragLayer(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DragLayer(Context context) {
		this(context, null);
		
	}
	
	public DragLayer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initRes(context);
	}

	
	private void initRes(Context context) {
		m_PaintTemp = new Paint();
		m_PaintTemp.setFlags(Paint.ANTI_ALIAS_FLAG);
		m_PaintTemp.setStyle(Paint.Style.STROKE);
		m_PaintTemp.setStrokeWidth(3);
		m_PaintTemp.setColor(Color.BLUE);
	}

	//在这里绘制一些辅助效果
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		
		/*
		 * 更新在拖动时的辅助信息，比如拖动到哪的可以预绘制的边框，不可以放置时的红色蒙板提醒
		 */
		if (m_DragObjectInfo != null) {
			if (m_DragObjectInfo.isInvalid) {
				//只先测试可以放置的效果
				if (m_DragObjectInfo.canDrop) {
//					Utils.log(TAG, "dispatch-绘制拖动效果");
					Utils.log(TAG, "canDrag");
					int left = m_DragObjectInfo.x;
					int top = m_DragObjectInfo.y;
					int right = left + m_DragObjectInfo.width;
					int bottom = top + m_DragObjectInfo.height;
					
					canvas.drawRect(left, top, right, bottom, m_PaintTemp);
					
					m_DragObjectInfo.reset();
				}
			}
		}
		
		
	}
	
	//这里可能不用使用，自己的布局？不用
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	//在这里绘制拖动的效果，即child的坐标位置不断的变化，然后requestLayout请求
	//重新绘制child在父布局中的效果
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
//		Utils.log(TAG, "onLayout()");
		
		final int iChildCount = getChildCount();
		
		View child;
		DragLayer.LayoutParams lp;
		ViewGroup.LayoutParams params;
		
		for (int i=0; i<iChildCount; i++) {
			child = getChildAt(i);
			params =  child.getLayoutParams();
			
			if (params instanceof DragLayer.LayoutParams) {
				lp = (LayoutParams) params;
				
				if (lp.customPosition) {
					int l = lp.x;
					int t = lp.y;
					int r = l + child.getMeasuredWidth();
					int b = t + child.getMeasuredHeight();
					
					child.layout(l, t, r, b);	
//					Utils.log(true, TAG, "拖动icon的坐标：(%d, %d), (%d, %d)", l, t, r, b);
				}
			}

		}
		
	}
	
	/**
	 * 更新在拖动时的辅助信息，比如拖动到哪的可以预绘制的边框，不可以放置时的红色蒙板提醒
	 * @param info
	 */
	public void updateDragPreEffect(CellLayout.DragObjectInfo info) {
		Utils.log(TAG,"%s-info.isInvalid=%b, isInCell=%b, canDrop=%b", "updateDragPreEffect", info.isInvalid, info.isInCell, info.canDrop);
		m_DragObjectInfo = info;
		invalidate();	//call dispatchDraw
	}
	
	
	public void setDragController(DragController dragController) {
		m_DragController = dragController;
		
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return m_DragController.onInterceptTouchEvent(ev);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return m_DragController.onTouchEvent(event);
	}
	
	public static class LayoutParams extends FrameLayout.LayoutParams {
		
		public int x; 
		public int y;
		public boolean customPosition = false;

		public LayoutParams(Context arg0, AttributeSet arg1) {
			super(arg0, arg1);
			// TODO Auto-generated constructor stub
		}

		public LayoutParams(int arg0, int arg1) {
			super(arg0, arg1);
		}

		public LayoutParams(android.view.ViewGroup.LayoutParams arg0) {
			super(arg0);
		}
		
		public LayoutParams() {
			super(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			
		}
		
	} 
	
	@Override
	public android.widget.FrameLayout.LayoutParams generateLayoutParams(
			AttributeSet attrs) {
		return new DragLayer.LayoutParams(getContext(), attrs);
	}
	
	@Override
	protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
		return p instanceof DragLayer.LayoutParams;
	}
	
	@Override
	protected android.view.ViewGroup.LayoutParams generateLayoutParams(
			android.view.ViewGroup.LayoutParams p) {
		return new DragLayer.LayoutParams(p);
	}
	
	@Override
	protected android.widget.FrameLayout.LayoutParams generateDefaultLayoutParams() {
		return new DragLayer.LayoutParams();
	}
	
	
}
