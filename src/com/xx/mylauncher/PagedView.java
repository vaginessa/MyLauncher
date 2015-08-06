package com.xx.mylauncher;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Scroller;

/**
 * 负责多屏滑动
 * @author baoxing
 *
 */
public abstract class PagedView extends ViewGroup {

	private static final String TAG = "PagedView";

	/** 当前所处的屏幕 */
	protected int m_iCurScreen = 0;
	
	/** 用来记录当可以fling时的一个容差量 */
	private float m_iTotalMoveX;
	
	/** 上一次的坐标值 */
	private float m_iLastX, m_iLastY;
	
	/** 用来跟踪当前按下的手指id */
	private int m_iCurPointId;
	
	/** 无效的指针Id */
	private static final int INVALID_POINT_ID = -1;
	
	/** 速度跟踪器 */
	private VelocityTracker m_VelocityTracker;
	
	
	/** 闲置状态 */
	protected static final int TOUCH_STATE_REST = 0;
	/** 滚动状态 */
	protected static final int TOUCH_STATE_SCROLING = 1;
	
	/** 当前Workspace所处的状态 */
	protected int m_iCurTouchState = TOUCH_STATE_REST;
	
	/** 是否滑动翻页的距离量 */
	private int m_iPagingTouchSlop;
	
	/** 是否滑动了的距离量 */
	private int m_iTouchSlop;
	
	/** 用来控制页面屏幕的滑动 */
	private Scroller m_Scroller;
	
	/** 屏幕的宽度 */
	private int m_iScreenWidth;
	
	
	private static final boolean m_bEnablePagingTouchSlop = false;
	
	public PagedView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PagedView(Context context) {
		this(context, null);
	}
	
	public PagedView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		initRes(context);
	}

	
	private void initRes(Context context) {
		final DisplayMetrics metrics = context.getResources().getDisplayMetrics();	//dont new one
		final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(metrics);
		m_iScreenWidth = metrics.widthPixels;
		
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		m_iTouchSlop = configuration.getScaledTouchSlop();
		m_iPagingTouchSlop = configuration.getScaledPagingTouchSlop();
		m_Scroller = new Scroller(context);
		
		Utils.log(TAG, "m_iTouchSlop=%d, m_iPagingTouchSlop=%d", m_iTouchSlop, m_iPagingTouchSlop);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub

	}
	
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		Utils.log(TAG, "oninterceptTouchEvent");
		
		acquireVelocityTrackerAndAddMovement(ev);
		
		if (getChildCount() <= 0) {
			Utils.log(TAG, "no childs");
			 return super.onInterceptTouchEvent(ev);
		}
		
		final int action = ev.getAction();
		
		if ( (action==MotionEvent.ACTION_MOVE) && (m_iCurTouchState==TOUCH_STATE_SCROLING) ) {
			Utils.log(TAG, "return true");
			return true;
		}
		
		switch (action&MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_MOVE:
			if (m_iCurPointId != INVALID_POINT_ID) {
				adjustWhetherScroll(ev);
				break;
			}
		case MotionEvent.ACTION_DOWN:
			final float iX = ev.getX();
			final float iY = ev.getY();
			m_iLastX = iX;
			m_iLastY = iY;
			m_iCurPointId = ev.getPointerId(0);
			m_iTotalMoveX = 0;
			m_iCurTouchState = TOUCH_STATE_REST;
			
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			/*
			 * 最后一根手指释放，计算是否足够fling
			 */
			m_iCurTouchState = TOUCH_STATE_REST;
			m_iCurPointId = INVALID_POINT_ID;
			releaseVelocityTracker();
			break;
		case MotionEvent.ACTION_POINTER_UP:
			/*
			 * 当另一根手指释放时触发
			 * 切换当前的手指索引指针
			 */
			onSecondaryPointerUp(ev);
			releaseVelocityTracker();
			break;
		}
		
		Utils.log(TAG, "return=%b, m_iCurTouchState=%d", m_iCurTouchState!=TOUCH_STATE_REST, m_iCurTouchState);
		return m_iCurTouchState != TOUCH_STATE_REST;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Utils.log(TAG, "onTouchEvent");
		if (getChildCount() <=0 ) {
			Utils.log(TAG, "no childs");
			return super.onTouchEvent(event);
		}
		
		acquireVelocityTrackerAndAddMovement(event);
		
		final int action = event.getAction();
		
		switch (action&MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			m_iTotalMoveX = 0;
			m_iLastX = event.getX();
			m_iCurPointId = event.getPointerId(0);
			
			break;
		case MotionEvent.ACTION_MOVE:
			if (m_iCurTouchState == TOUCH_STATE_SCROLING) {
				final int pointerIndex = event.findPointerIndex(m_iCurPointId);
				final float fX = event.getX(pointerIndex);
				final float fDeltaX = m_iLastX - fX;
				m_iTotalMoveX += Math.abs(fDeltaX);
				
				if (Math.abs(fDeltaX) > 1.0f) {
					Utils.log(TAG, "onTouchEvent-ACTION_MOVE: 滑呀滑");
					m_iLastX = fX;
					final boolean isInBounds = isInWorkspaceBoundLimit(getScrollX(), fDeltaX>=0 ? 1:0);
					if (isInBounds) {
						scrollBy((int)fDeltaX, 0);	
					}
					
				}
				
			} else {
				adjustWhetherScroll(event);
			}
			
			
			break;
		case MotionEvent.ACTION_UP:
			//TODO 跟踪速度
			m_iCurTouchState = TOUCH_STATE_REST;
			m_iCurPointId = INVALID_POINT_ID;
			releaseVelocityTracker();
			
			Utils.log(TAG, "getScrollX=%d, getCurrX=%d, getFinalX=%d", getScrollX(), m_Scroller.getCurrX(), m_Scroller.getFinalX());
			break;
		case MotionEvent.ACTION_CANCEL:
			if (m_iCurTouchState == TOUCH_STATE_SCROLING) {
				//TODO 
				//滑向最近的屏幕
			}
			m_iCurTouchState = TOUCH_STATE_REST;
			m_iCurPointId = INVALID_POINT_ID;
			releaseVelocityTracker();
			break;
		case MotionEvent.ACTION_POINTER_UP:
			onSecondaryPointerUp(event);
			break;
		}
		
		return true;
	}
	
	/**
	 * 判断滚动（移动时）是否在Workspace的边界内
	 * @param scrollX
	 * @param direction	0，向左；1，向右
	 * @return true，在范围内
	 */
	private boolean isInWorkspaceBoundLimit(int curX, int direction) {
		boolean result = true;
		final int iWorkspaceWidth = getWorkspaceWidth();
		final int left = 0;
		final int right = 1;
		
		if ( (direction==left && curX<=0) || (direction==right && curX>=iWorkspaceWidth) ) {
			result = false;
		}
		
		return result;
	}
	
	
	@Override
	public void computeScroll() {
		super.computeScroll();
		if (m_Scroller.computeScrollOffset() ) {
			scrollTo( getScrollX(), 0);
		}
		
		
	}
	
	
	
	
	
	
	private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
        MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		final int pointerId = ev.getPointerId(pointerIndex);
		if (pointerId == m_iCurPointId) {
			/*
			 * 主的拖放手指离开
			 */
			final int newPointerIndex = pointerIndex==0 ? 1 : 0;
			m_iLastX = ev.getX(newPointerIndex);
			m_iLastY = ev.getY(newPointerIndex);
			m_iCurPointId = ev.getPointerId(newPointerIndex);
			if (m_VelocityTracker != null) {
				m_VelocityTracker.clear();
			}
		}
		
	}
	
	/**
	 * 释放VelocityTracker
	 */
	private void releaseVelocityTracker() {
		if (m_VelocityTracker != null) {
			m_VelocityTracker.recycle();
			m_VelocityTracker = null;
		}
	}
	
	/**
	 * 判断是否应该进入滚动的状态
	 * @param ev
	 */
	private void adjustWhetherScroll(MotionEvent ev) {
		Utils.log(TAG, "adjustWhetherScroll()");
		final int pointerIndx = ev.findPointerIndex(m_iCurPointId);
		if (pointerIndx == -1) {
			return;
		}
		
		final float iX = ev.getX(pointerIndx);
		final float iY = ev.getY(pointerIndx);
		
		boolean bIsMoveH = Math.abs(m_iLastX-iX) > m_iTouchSlop ? true : false;
		boolean bIsMoveV = Math.abs(m_iLastY-iY) > m_iTouchSlop ? true : false;
		boolean bIsMovePage = Math.abs(m_iLastX-iX) > m_iPagingTouchSlop ? true : false;
		
		if (bIsMoveH || bIsMoveV || bIsMovePage) {
			if (m_bEnablePagingTouchSlop?bIsMovePage:bIsMoveH) {
				Utils.log(TAG, "adjustWhether-进入滚动状态");
				
				m_iCurTouchState = TOUCH_STATE_SCROLING;
				m_iTotalMoveX = Math.abs(m_iLastX-iX);
				
			}
			
			cancelCurScreenLongPress();
		}
		
	}

	/**
	 * 取消当前屏幕的长按
	 */
	private void cancelCurScreenLongPress() {
		Utils.log(TAG, "cancelLongPress()");
		/*
		 * 取消子View的长按，这个方法的作用具体是什么不是很了解
		 */
		View curPage = getCurScreen(m_iCurScreen);
		if (curPage != null) {
			curPage.cancelLongPress();
		}
	}

	private View getCurScreen(int curScreen) {
		return getChildAt(curScreen);
	}

	
	private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
		if (m_VelocityTracker == null) {
			m_VelocityTracker = VelocityTracker.obtain();
		}
		m_VelocityTracker.addMovement(ev);
	}
	
	/*
	 * http://mobile.51cto.com/hot-316799.htm
	 * http://blog.csdn.net/ohehehou/article/details/9124101
	 * http://blog.csdn.net/chenshaoyang0011/article/details/7845434
	 * http://blog.csdn.net/chenjie19891104/article/details/7014649
	 * 
	 */
	
	
	/**一共有多少个屏幕 */
	protected abstract int getScreenCounts();
	/** 得到Workspace的总宽度 */
	protected abstract int getWorkspaceWidth();
	
	
	
	
	
	
	
	
	
	
	
	
	

}
