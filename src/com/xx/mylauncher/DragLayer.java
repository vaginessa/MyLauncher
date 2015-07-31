package com.xx.mylauncher;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
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
		
		/*
		 * DragView滑回指定位置
		 */
		if (m_DropObjectInfo != null) {
			if (m_DropObjectInfo.isInvalid) {
				int left = m_DropObjectInfo.curX;
				int top = m_DropObjectInfo.curY;

				canvas.drawBitmap(m_DropObjectInfo.dragView.getViewBitmap(), left, top, null);
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
	
	/** 用来处理DragView移动到指定位置的属性动画 */
	private ValueAnimator m_DragViewAnim;
	
	/** 移动的时间 */
	private static final long DRAGVIEW_SCROLL_TIME = 4000;
	
	/** 用来表示DragView滑动效果的相关信息 */
	private CellLayout.DropObjectInfo m_DropObjectInfo;
	
	/**
	 * 当释放DragView时，使DragView平滑移动到所处的位置
	 * @param info
	 */
	public void updateDragViewToOriPoint(final CellLayout.DropObjectInfo info) {
		if (m_DropObjectInfo != null) {
			if (!m_DropObjectInfo.isAnimFinished) {
				m_DragViewAnim.end();
			}
		}
		
		m_DropObjectInfo = info;
		
		if (m_DragViewAnim == null) {
			m_DragViewAnim = new ValueAnimator();
			m_DragViewAnim.setDuration(DRAGVIEW_SCROLL_TIME);
		}
		m_DragViewAnim.setFloatValues(0f, 1f);
		m_DragViewAnim.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				if (m_DropObjectInfo.dragView != null) {
					removeView(m_DropObjectInfo.dragView);
					m_DropObjectInfo.dragView = null;
				}
				
				m_DropObjectInfo.itemView.setVisibility(View.VISIBLE);
				m_DropObjectInfo.animEnd();
				
				requestLayout();
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				
			}
		});
		m_DragViewAnim.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				//这部分的计算应放到TypeE..中去的，但是，这样方便
				float process = (Float) animation.getAnimatedValue();
				int allprocessX = m_DropObjectInfo.originX - m_DropObjectInfo.finalX;
				int allProcessY = m_DropObjectInfo.originY - m_DropObjectInfo.finalY;
				int curX = (int) (m_DropObjectInfo.finalX + allprocessX * process);
				int curY = (int) (m_DropObjectInfo.finalY + allProcessY * process);
				m_DropObjectInfo.curX = curX;
				m_DropObjectInfo.curY = curY;
				
				invalidate();	//to call dispatchDraw, simple to clear mind
			}
		});
		m_DragViewAnim.start();
		
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
