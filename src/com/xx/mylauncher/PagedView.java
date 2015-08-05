package com.xx.mylauncher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

/**
 * 负责多屏滑动
 * @author baoxing
 *
 */
public class PagedView extends ViewGroup {

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
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		m_iTouchSlop = configuration.getScaledTouchSlop();
		m_iPagingTouchSlop = configuration.getScaledPagingTouchSlop();
		
		Utils.log(TAG, "m_iTouchSlop=%d, m_iPagingTouchSlop=%d", m_iTouchSlop, m_iPagingTouchSlop);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub

	}
	
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
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
			//TODO 跟踪速度
			break;
		case MotionEvent.ACTION_POINTER_UP:
			/*
			 * 切换当前的手指索引指针
			 */
			break;
		}
		
		return m_iCurTouchState != TOUCH_STATE_REST;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (getChildCount() <=0 ) {
			Utils.log(TAG, "no childs");
			return super.onTouchEvent(event);
		}
		Utils.log(TAG, "滑呀滑");
		
		return true;
	}
	
	
	
	
	/**
	 * 判断是否应该进入滚动的状态
	 * @param ev
	 */
	private void adjustWhetherScroll(MotionEvent ev) {
		Utils.log(TAG, "adjustWhetherScroll()");
		final float iX = ev.getX();
		final float iY = ev.getY();
		
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
