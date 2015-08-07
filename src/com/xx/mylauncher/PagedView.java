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
	
	/** 滑动的X轴方向的补偿量，Launcher中的源码的这个思路很好。这种思维怎么培养. */
	private int m_iScrollXCompasation = 0;
	
	private static final boolean m_bEnablePagingTouchSlop = false;
	
	/** 获得允许执行一个fling手势动作的最大速度值 */
	private int m_iMaximumVelocity;
	
	/** fling手势发生的一个距离容差量 */
	private static final int MIN_LENGTH_FOR_FLING = 25;
	
	/** 滑动的速度要大于该阈值，才进行屏幕切换 */
	private static final int VELOCITY_THRESHOLD = 500;
	
	/** 滑动到另一屏幕的时间的上限 */
	private static final int SCREEN_SCROLL_DURATION_UP = 500;
	
	/** 滑动到另一屏幕时间的下限 */
	private static final int SCREEN_SCROLL_DURATION_DOWN = 200;
	
	/** 多屏滑动指示器 */
	private SlideIndicator m_SlideIndicator;
	
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
		m_iMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
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
					
					scrollBy((int)fDeltaX, 0);
					
				}
				
			} else {
				adjustWhetherScroll(event);
			}
			
			
			break;
		case MotionEvent.ACTION_UP:
			//TODO 跟踪速度
			final int iActivityPointId = m_iCurPointId;
			final int iPointerIndex = event.findPointerIndex(iActivityPointId);
			final float iX = event.getX(iPointerIndex);
			final VelocityTracker velocityTracker = m_VelocityTracker;
			velocityTracker.computeCurrentVelocity(1000, m_iMaximumVelocity);
			
			/*
			 * X轴的速度，px/s；正值手指往右滑，负值往左滑。
			 */
			int iVelocityX = (int) velocityTracker.getXVelocity(iActivityPointId);
			Utils.log(TAG+"Velocity", "iVelocityX[x轴的速度]=%d", iVelocityX);
			boolean isFling =( Math.abs(iVelocityX)>VELOCITY_THRESHOLD) && (m_iTotalMoveX>MIN_LENGTH_FOR_FLING); 
			if (isFling) {
				//fling to another screen
				int direction = iVelocityX>0 ? 0 : 1;
				flingToAnthorScreen(direction, iVelocityX);
			} else {
				//scroll to nearest screen
				int direction = iVelocityX>0 ? 0 : 1;
				sliceToNeaestScreen();
			}
			
			m_iCurTouchState = TOUCH_STATE_REST;
			m_iCurPointId = INVALID_POINT_ID;
			releaseVelocityTracker();
			
			Utils.log(TAG, "getScrollX=%d, getCurrX=%d, getFinalX=%d", getScrollX(), m_Scroller.getCurrX(), m_Scroller.getFinalX());
			break;
		case MotionEvent.ACTION_CANCEL:
			if (m_iCurTouchState == TOUCH_STATE_SCROLING) {
				//TODO 
				//滑向最近的屏幕
				sliceToNeaestScreen();
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
	 * fling到另一个屏幕
	 * @param direction	0为手指往右滑，屏幕左移，1为屏幕右移
	 * @param velocity
	 */
	private void flingToAnthorScreen(int direction, int velocity) {
		final int iPrev = 0;
		final int iNext = 1;
		final int iTotalScreen = getScreenCounts();
		final int iXCompasaton = m_iScrollXCompasation;
		int iScrollDuration = getDurationAcorrdingVelocity(velocity);
		
		Utils.log(TAG, "屏幕%s滑，m_iCurScreen=%d, iXcompasation=%d, iScrollDuration=%d", direction==iPrev?"左":"右", m_iCurScreen, iXCompasaton, iScrollDuration);
		
		if ( (direction==iPrev) && (m_iCurScreen==0)) {
			Utils.log(TAG, "最左边");
			return;
		}
		
		if ( (direction==iNext) && (m_iCurScreen==iTotalScreen-1)) {
			Utils.log(TAG, "最右边");
			return;
		}
		
		if (direction == iPrev) {
			m_iCurScreen -= 1;
			sliceOrfingToScreen(m_iCurScreen, iScrollDuration);
		} else {
			m_iCurScreen += 1;
			sliceOrfingToScreen(m_iCurScreen, iScrollDuration);
		}
		
		invalidate();
	}
	
	/**
	 * 根据速度来映射滑动时间
	 * @param velocity
	 */
	private int getDurationAcorrdingVelocity(int velocity) {
		//TODO 这里更理想的是，加入要滑动距离这个参数，就像Launcher中处理的一样
		final int x2 = m_iMaximumVelocity;
		final int x1 = VELOCITY_THRESHOLD;
		final int y2 = SCREEN_SCROLL_DURATION_DOWN;
		final int y1 = SCREEN_SCROLL_DURATION_UP;

		velocity = Math.abs(velocity);
		
		int iDuration = ((velocity-x1) / (x2-x1) ) * (y2-y1) + y1;
		
		return iDuration;
	}
	
	/**
	 * 滑向最近的屏幕
	 * @param direction 0为手指往右滑，屏幕左移，1为屏幕右移
	 */
	private void sliceToNeaestScreen() {
		final String tag = TAG + "slice";
		final int iCurScrollX = getScrollX();
		final int iCurScreenLeft = m_iCurScreen*getScreenWidth();
		final int iThesold = getScreenWidth()  / 3;
		final int iDuration = SCREEN_SCROLL_DURATION_DOWN;

		Utils.log(tag, "slice to neaset screen. iCurSceen=%d, iCurScrollX=%d, iCurScreenLeft=%d", m_iCurScreen, iCurScrollX, iCurScreenLeft);
		
		
		if (iCurScreenLeft-iCurScrollX > iThesold) {
			//上一页
			if (m_iCurScreen <= 0) {
				return;
			}
			m_iCurScreen -= 1;
			sliceOrfingToScreen(m_iCurScreen, iDuration);
		} else if (iCurScrollX-iCurScreenLeft > iThesold) {
			//下一页
			if (m_iCurScreen >= getScreenCounts()-1) {
				return;
			}
			m_iCurScreen += 1;
			sliceOrfingToScreen(m_iCurScreen, iDuration);
		} else {
			//当前页
			sliceOrfingToScreen(m_iCurScreen, iDuration);
		}
		
		
	}
	
	private void sliceOrfingToScreen(int whichPage, int duration) {
		Utils.log(TAG, "sliceOrfingToScreen");
		final int iFinalX = whichPage * getScreenWidth();
		int iXCompasation = m_iScrollXCompasation;
		int iDx = iFinalX - iXCompasation;
		
		m_Scroller.startScroll(iXCompasation, 0, iDx, 0, duration);
		
		invalidate();
	}
	
	@Override
	public void scrollBy(int x, int y) {
		scrollTo(m_iScrollXCompasation+x, y);
	}

	@Override
	public void scrollTo(int x, int y) {
		final int iMaxValue = getWorkspaceWidth();
		m_iScrollXCompasation = x;
		if (x<0) {
			m_iScrollXCompasation = 0;
			super.scrollTo(0, y);
			updateInditor(0);
		} else if (x>iMaxValue ) {
			m_iScrollXCompasation = iMaxValue;
			super.scrollTo(iMaxValue, y);
			updateInditor(iMaxValue);
		} else {
			super.scrollTo(x, y);
			updateInditor(x);
		}
	}
	
	/**
	 * 更新指示器
	 * @param scrollX
	 */
	private void updateInditor(int scrollX) {
		final int iScreenWidth = getScreenWidth();
		
		if (m_SlideIndicator == null) {
			m_SlideIndicator = getLauncher().getSlideIndicator();
		}
		m_SlideIndicator.updateInditor(scrollX, iScreenWidth);
	}

	@Override
	public void computeScroll() {
		super.computeScroll();
		if (m_Scroller.computeScrollOffset() ) {
			scrollTo(m_Scroller.getCurrX(), 0);
			invalidate();
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
	
	private int getScreenWidth() {
		return m_iScreenWidth;
	}
	
	
	/**一共有多少个屏幕 */
	protected abstract int getScreenCounts();
	/** 得到Workspace的总宽度 */
	protected abstract int getWorkspaceWidth();
	
	protected abstract MainActivity getLauncher();
	
	
	
	
	
	
	
	
	
	
	

}
