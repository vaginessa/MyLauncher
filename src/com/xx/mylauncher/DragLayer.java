package com.xx.mylauncher;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.xx.mylauncher.AnimatorFactory.AnimatorDragFollow1;
import com.xx.mylauncher.CellLayout.DragObjectInfo;

/**
 * 拖动层，绘制icon的拖动效果及拖动时的一些辅助效果
 * @author baoxing
 *
 */
public class DragLayer extends LinearLayout implements DragController.DragListener {
	
	private static final String TAG = "DragLayer";
	
	private DragController m_DragController;
	
	/** 更新在拖动时的辅助信息，比如拖动到哪的可以预绘制的边框，不可以放置时的红色蒙板提醒 */
	private CellLayout.DragObjectInfo m_DragObjectInfo;

	/** 测试用 */
	private Paint m_PaintTemp;
	private Paint m_PaintTemp1;
	
	private Rect m_RectTempDragFollowSelf = new Rect();
	
	/**
	 * 处理跟随动画的封装类
	 */
	private AnimatorFactory.AnimatorDragFollow1 m_AnimatorDragFollow;
	
	
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
		m_PaintTemp1 = new Paint();
		m_PaintTemp1.setAlpha(130);
		m_PaintTemp1.setStyle(Paint.Style.FILL);
	}

	//在这里绘制一些辅助效果
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		
//		updateDragInfoAssist(canvas);
		
		updateDragFollowAnimDispatchDraw(canvas);
		
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
		
		/*
		 * 绘制最后要放置的地方的预览图，推荐的是DragView的Bitmap，和拖动时一样
		 */
		if (m_DropObjectInfo!=null && m_DropObjectInfo.isInvalid && !m_DropObjectInfo.isAnimFinished) {
			int left = m_DropObjectInfo.originX;
			int top = m_DropObjectInfo.originY;
			canvas.drawBitmap(m_DropObjectInfo.dragView.getViewBitmap(), left, top, m_PaintTemp1);
			
		}
		
		
		
	}

	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		final int iWidthSelf = MeasureSpec.getSize(widthMeasureSpec);
		final int iHeightSelf = MeasureSpec.getSize(heightMeasureSpec);
		final int iChildCount = getChildCount();
		
		/*
		 * 测量子View
		 */
//		View child;
/*		for (int i=0; i<iChildCount; i++) {
			child = getChildAt(i);
			child.measure(widthMeasureSpec, heightMeasureSpec);			
		}
		*/
		setMeasuredDimension(iWidthSelf, iHeightSelf);
		
		Utils.log(TAG, "width=%d, height=%d, iWidthSelf=%d, iHeightSelf=%d", getMeasuredWidth(), getMeasuredHeight(), iWidthSelf, iHeightSelf );
	}
	
	//在这里绘制拖动的效果，即child的坐标位置不断的变化，然后requestLayout请求
	//重新绘制child在父布局中的效果
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
//		Utils.log(TAG, "onLayout()");
		
		final int iChildCount = getChildCount();
		
		
		/*
		 * 布局子View， workspace和indication和hotseat
		 */
		
/*		
		View childLauncher;
		int iLeft = 0;
		int iTop = 0;
		int iRight;
		int iBottom = 0;
		for (int i=0; i<iChildCount; i++) {
			childLauncher = getChildAt(i);
		
			int width;
			int height;
			if ( childLauncher instanceof Workspace ) {
				width = childLauncher.getMeasuredWidth();
				height = childLauncher.getMeasuredHeight();
				iRight = width;
				iBottom = height;
				
				childLauncher.layout(iLeft, iTop, iRight, iBottom);
				Utils.log(TAG, "layout-workspace[(%d, %d), (%d, %d)]). [width=%d, height=%d]", iLeft, iTop, iRight, iBottom, width, height);
			}
			if (childLauncher instanceof SlideIndicator ) {
				width = childLauncher.getMeasuredWidth();
				height = childLauncher.getMeasuredHeight();
				iTop = iBottom;
				iRight = width;
				iBottom += height;
				
				childLauncher.layout(iLeft, iTop, iRight, iBottom);
				Utils.log(TAG, "layout-slideIndicator[(%d, %d), (%d, %d)]). [width=%d, height=%d]", iLeft, iTop, iRight, iBottom, width, height);
			}
			
		}
		*/
		
		
		/*
		 * 拖动的dragview绘画提示，这个不应该放在这里
		 * 是由 DragView#move()方法中过来的，强烈更改至dispatchDraw中去绘画
		 */
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
	private static final long DRAGVIEW_SCROLL_TIME = 500;
	
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
				m_DropObjectInfo.dragView.setVisibility(View.INVISIBLE);

			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				if (m_DropObjectInfo.dragView != null) {
					removeView(m_DropObjectInfo.dragView);
//					m_DropObjectInfo.dragView = null;
				}
		
				m_DropObjectInfo.animEnd();
				m_DropObjectInfo.itemView.setVisibility(View.VISIBLE);
				
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
	
	/**
	 * 更新跟随动画，从CellLayout中调用
	 * @param dragObjectInfo
	 */
	public void updateDragFollowDrag(final DragObjectInfo dragObjectInfo) {
		m_DragObjectInfo = dragObjectInfo;
		m_AnimatorDragFollow.updateDragFollow(dragObjectInfo);
	}
	
	/**
	 * 更新跟随动画，从AnimatorFactory中调用
	 * @param info
	 */
	public void updateDragFollowAnim(CellLayout.DragObjectInfo info) {
		Utils.log(TAG, "updateDragFollowAnim");
		m_DragObjectInfo = info;
		invalidate();
	}
	
	public void updateDragFollowAnim(CellLayout.DragObjectInfo info, Rect invalidRect) {
		Utils.log(TAG, "updateDragFollowAnim");
		m_DragObjectInfo = info;
		invalidate(invalidRect);
	}
	
	/**
	 * 更新跟随动画
	 * @param canvas
	 */
	private void updateDragFollowAnimDispatchDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		final DragObjectInfo dragObject = m_DragObjectInfo;
		
		if (dragObject != null) {
			if (dragObject.isInvalid) {
				if (dragObject.canDrop) {
					final DragView dragView = (DragView) m_DragObjectInfo.dragView;
					int left = dragObject.curX;
					int top = dragObject.curY;
					int right = left + dragObject.width;
					int bottom = top + dragObject.height;
					
//					canvas.drawRect(left, top, right, bottom, m_PaintTemp);
					canvas.drawBitmap(dragView.getViewBitmap(), left, top, null);
					
					dragObject.reset();	
					
					m_RectTempDragFollowSelf.set(left, top, right, bottom);
					
					invalidate(m_RectTempDragFollowSelf);
					
				}
			}
		}
		
		
	}
	
	/**
	 * 初始化FollowDragObject
	 */
	public void initFollowDragObject() {
		/*
		 *初始化动画
		 */
		Utils.log(TAG, "initFollowDragObject");
		m_AnimatorDragFollow = new AnimatorDragFollow1(m_DragController.getLauncher(), this);
	}
	
	public void endAndClearFollowDragObject() {
		m_AnimatorDragFollow.endDragFollowAnim();
	}
	
	
	/**
	 * 更新在拖动时的辅助信息，比如拖动到哪的可以预绘制的边框，不可以放置时的红色蒙板提醒
	 */
	private void updateDragInfoAssist(Canvas canvas) {
		
		if (m_DragObjectInfo != null) {
			
			if (m_DragObjectInfo.isInvalid) {
				if (m_DragObjectInfo.canDrop) {
					// 只先测试可以放置的效果
					// Utils.log(TAG, "dispatch-绘制拖动效果");
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
	
	public static class LayoutParams extends LinearLayout.LayoutParams {
		
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
	public android.widget.LinearLayout.LayoutParams generateLayoutParams(
			AttributeSet attrs) {
		return new DragLayer.LayoutParams(getContext(), attrs);
	}
	
	@Override
	protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
		return p instanceof DragLayer.LayoutParams;
	}
	
	@Override
	protected android.widget.LinearLayout.LayoutParams generateLayoutParams(
			android.view.ViewGroup.LayoutParams p) {
		return new DragLayer.LayoutParams(p);
	}
	
	@Override
	protected android.widget.LinearLayout.LayoutParams generateDefaultLayoutParams() {
		return new DragLayer.LayoutParams();
	}

	@Override
	public void onDragStart(DragSource source, Object info, int dragAction) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDragEnd() {
		if (m_AnimatorDragFollow != null) {
			m_AnimatorDragFollow.endDragFollowAnim();
		}
		
	}
	
	
}
