package com.xx.mylauncher;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * 实际处理拖动操作的模型
 * @author baoxing
 *
 */
public class DragController {
	
	private static final String TAG = "DragController";
	
	/** 震动的持续时间 */
	private static final long VIBRATE_DURATION = 35;
	
	private InputMethodManager m_InputMethodManager;
	
	private Context m_Context;
	
	private IBinder m_WindowToken;
	
	/** 对拖放过程感兴趣的监听者 */
	private List<DragController.DragListener> m_DragListeners = new ArrayList<DragController.DragListener>();
	
	/** 可以放置的抽象区域 */
	private List<DropTarget> m_DropTargetList = new ArrayList<DropTarget>();
	
	/** 当前的DragView */
	private DragView m_DragViewCur;
	
	//TODO 要改动
	/**Launcher引用 */
	private MainActivity m_Launcher;
	
	private Vibrator m_Vibrator;
	
	/**是否处于拖动状态 */
	private boolean m_bIsDragging;
	
	private int m_iRawX, m_iRawY;
	
	/** 按下的点到该View{长按触发的}的top-left偏移量 */
	private int m_iOffItemViewX, m_iOffItemViewY;
	
	
	/** <strong>temp</strong>，存放长按View在 整个屏幕中的坐标值，包括状态栏；也存放到父View{@link DropTarget} 的偏移量 */
	private int[] m_iArrayCoordinatesTemp = new int[2];
	
	/** <strong>temp</strong>，可能只是程序的优化上作用 */
	private Rect m_RectTemp = new Rect();
	
	/** 用来记录上一次的DropTarget引用 */
	private DropTarget m_DropTargetLast;
	
	/**从哪里拖动过来，拖动源 */
	private DragSource m_DragSource;
	
	
	/**
	 * 
	 * @param context	这个参数有要转为Activity，所以不要传入Appcalition Cotnext
	 * @param launcher
	 */
	public DragController(Context context, MainActivity launcher) {
		m_Launcher = launcher;
		m_Context = context;
		initRes(context);
	}
	
	
	private void initRes(Context context) {
		// TODO Auto-generated method stub
		m_Vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		
	}

	/**
	 * 开始拖动，得到拖动View的bitmap
	 * @param view
	 * @param dragSource
	 * @param dragInfo
	 * @param dragAction
	 */
	public void startDrag(View view, DragSource dragSource, Object dragInfo, int dragAction) {
		Utils.log(TAG, "startDrag1()");
		
		Bitmap b = getViewBitmap(view);
		
		if (b == null) {
			//out of memory?
			//啥意思，取不到
			return;
		}
		
		int[] loc = m_iArrayCoordinatesTemp;
		view.getLocationOnScreen(loc);		//得到长按view在整个屏幕中的坐标值(left, top)，包括状态栏
		int screenX = loc[0];
		int screenY = loc[1];

		startDrag(b, screenX, screenY, dragSource, dragInfo, dragAction);
		
		b.recycle();
		
		if (dragAction == Constant.DRAG_MOVE) {
			view.setVisibility(View.GONE);
		}
		
	} 
	
	/**
	 * 
	 * @param b	View的视图
	 * @param screenX	view{长按}在整个屏幕中的位置，left
	 * @param screenY	view{长按}在整个屏幕中的位置，top
	 * @param source		从哪里来，拖放源
	 * @param dragInfo	view{长按}携带的信息
	 * @param dragAction	{@link Constant#DRAG_COPY} 和 {@link Constant#DRAG_MOVE}
	 */
	public void startDrag(Bitmap b, int screenX, int screenY, DragSource source, Object dragInfo, int dragAction) {
		Utils.log(TAG, "startDrag2()");
		
		//Hide soft keybord if visible
		if (m_InputMethodManager == null) {
			m_InputMethodManager = (InputMethodManager) m_Context.getSystemService(Context.INPUT_METHOD_SERVICE);
		}
		m_InputMethodManager.hideSoftInputFromWindow(m_WindowToken, 0);
		
		if (m_DragListeners != null) {
			for (DragListener l : m_DragListeners) {
				l.onDragStart(source, dragInfo, dragAction);
			}
		}
		
		m_iOffItemViewX = m_iRawX - screenX;
		m_iOffItemViewY = m_iRawY - screenY;
		
		m_bIsDragging = true;
		m_DragSource = source;
		
		//TODO 是否选择这个位置，要重新考虑
		if ( (dragInfo instanceof CellInfo) && (source instanceof Workspace) ) {
			CellInfo cellInfo = (CellInfo) dragInfo;
			m_Launcher.getWorkspace().getCurScreen().clearFlagsOcupied(cellInfo.getCellX(), 
					cellInfo.getCellY(), cellInfo.getCellHSpan(), cellInfo.getCellVSpan());		
		}
		
		if ( (source instanceof HotSeat) && (dragInfo instanceof CellInfo) ) {
			CellInfo cellInfo = (CellInfo) dragInfo;
			m_Launcher.getHotSeat().clearFlagOcupid(cellInfo);
		} 
		
		m_Vibrator.vibrate(VIBRATE_DURATION);
		DragView dragView = m_DragViewCur = new DragView(m_Context, b, m_Launcher, screenX, screenY, m_iRawX, m_iRawY);
		dragView.setTag(dragInfo);		//TODO
		dragView.show(m_iRawX, m_iRawY);
	}
	
	
	/**
	 * 提取该View的Bitmap资源
	 * @param view
	 * @return
	 */
	private Bitmap getViewBitmap(View view) {
		view.clearFocus();
		view.setPressed(false);
		
		boolean willNotCache = view.willNotCacheDrawing();
		view.setWillNotCacheDrawing(false);
		
		//Reset the drawing cache background color to fully transparent
		//for the duration of this operation
		int color = view.getDrawingCacheBackgroundColor();
		float alpha = view.getAlpha();
		view.setDrawingCacheBackgroundColor(0);	
		view.setAlpha(1.0f);
		
		if (color != 0) {
			view.destroyDrawingCache();
			
		}
		
		view.buildDrawingCache();
		Bitmap cacheBitmap = view.getDrawingCache();
		if (cacheBitmap == null) {
			Utils.log(TAG, new RuntimeException(), "failed getViewBitmap(%s)", view.toString() );
			return null;
		}
		
		Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);
		
		//Restore the view
		view.destroyDrawingCache();
		view.setAlpha(alpha);
		view.setWillNotCacheDrawing(willNotCache);
		view.setDrawingCacheBackgroundColor(color);
		
		return bitmap;
	}

	//TODO
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		
		if (action == MotionEvent.ACTION_DOWN) {
			m_iRawX = (int) ev.getRawX();
			m_iRawY  = (int) ev.getRawY();
		}
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			Utils.log(TAG, "onInterceptToucherEvent-action_down");
			
			m_DropTargetLast = null;
			
			break;
		case MotionEvent.ACTION_MOVE:
//			Utils.log(TAG, "onInterceptToucherEvent-action_move");
			
			break;
		case MotionEvent.ACTION_UP:
			Utils.log(TAG, "onInterceptToucherEvent-action_up");
			if (m_bIsDragging) {
				drop(m_iRawX, m_iRawY);
			}
			endDrag();		//这里有个的疑问是，当没有拖拽的时候，如果但是它的子View有消费该事件（事实上也有），它将被调用
			
			break;
		case MotionEvent.ACTION_CANCEL:
			cancelDrag();
			break;
		}
		
		return m_bIsDragging;
	}
	
	//TODO
	public boolean onTouchEvent(MotionEvent ev) {

		if (!m_bIsDragging) {
			return false;
		}
		
		final int action = ev.getAction();
		final int screenX = (int) ev.getRawX();
		final int screenY = (int) ev.getRawY();
		
		m_iRawX = (int) ev.getRawX();
		m_iRawY = (int) ev.getRawY();
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			Utils.log(TAG, "onTouchEvent()-action_down");
			
			break;
			
		case MotionEvent.ACTION_MOVE:
			m_DragViewCur.move((int)ev.getRawX(), (int)ev.getRawY() );
			
			//拖动到哪个DropTarget区域了，或者没有
			//Workspace，DeleteZone，HotSeat
			final int[] coordinates = m_iArrayCoordinatesTemp;
			final DropTarget findDropTarget = findDropTarget(screenX, screenY, coordinates);	
			
			if (findDropTarget != null) {
				//TODO
				
				if (m_DropTargetLast == findDropTarget) {
					findDropTarget.onDragOver(m_DragSource, coordinates[0], coordinates[1], m_iOffItemViewX, m_iOffItemViewY, m_DragViewCur, m_DragViewCur.getTag() );
					
				} else {
					if (m_DropTargetLast != null) {
						m_DropTargetLast.onDragExit(m_DragSource, coordinates[0], coordinates[1], m_iOffItemViewX, m_iOffItemViewY, m_DragViewCur, m_DragViewCur.getTag() );
					}
					findDropTarget.onDragEnter(m_DragSource, coordinates[0], coordinates[1], m_iOffItemViewX, m_iOffItemViewY, m_DragViewCur, m_DragViewCur.getTag() );
				}
				
			} else { //不在DropTarget区域
				if (m_DropTargetLast != null) {
					m_DropTargetLast.onDragExit(m_DragSource, coordinates[0], coordinates[1], m_iOffItemViewX, m_iOffItemViewY, m_DragViewCur, m_DragViewCur.getTag() );
				}
				
			}
			
			m_DropTargetLast = findDropTarget;
			
			break;
			
		case MotionEvent.ACTION_UP:
			Utils.log(TAG, "onTouchEvent()-action_up");
			if (m_bIsDragging) {
				drop(screenX, screenY);
			}
			endDrag();
			break;
			
		case MotionEvent.ACTION_CANCEL:
			Utils.log(TAG, "onTouchEvent()-action_cancel");
			cancelDrag();
			break;
		}
		
		return true;
	}
	
	/**
	 * 查找拖动到哪一个DropTarget区域
	 * @param screenX	屏幕绝对坐标x
	 * @param screenY
	 * @param dropCoordinates	用来存储到父View{DropTarget}的偏移量
	 * @return
	 */
	private DropTarget findDropTarget(int screenX, int screenY, int[] dropCoordinates) {
		final Rect r = m_RectTemp;
		final List<DropTarget> dropTargetList = m_DropTargetList;
		
		for (int i=0; i<dropTargetList.size(); i++) {
			final DropTarget dropTarget = dropTargetList.get(i);
			
			if (!dropTarget.isDropEnable()) {
				continue;
			}
			
			//TODO 这里如果DragLayer不是在最左上角，可能会出错
			//改用了不出错的方法 - 2015/8/8
			//计算DragLayer的top_left 的偏移量?? 好像不会，通过画图分析
			//一般，Launcher中的DragLayer和Workspace是match_parent，即是最左上角的
			/*
			 * getHitRect方法是得到该View在父布局{DrapLayer}中的对角坐标，前提是该View是DrapLayer的直接child view
			 * 如果有titlebar则 (0, 0)是从titlebar下面开始记起的
			 */
			dropTarget.getHitRect(r);
			/*
			 * 下面这个是一步到位了？？越写，有些乱了
			 */
//			dropTarget.getHitRectRefDragLayer(r, dropTarget);
			
			/*
			 * getLocationOnScreen方法是得到该View在屏幕中的绝对坐标，包括状态栏的高度
			 */
			dropTarget.getLocationOnScreen(dropCoordinates);
			
			/*
			 * 这一步之后，r的对角坐标为屏幕的绝对坐标值了
			 */
			r.offset(dropCoordinates[0]-dropTarget.getLeft(), dropCoordinates[1]-dropTarget.getTop() );
			
			if (r.contains(screenX, screenY) ) {
				/*
				 * 下面是计算坐标到父View{DropTarget}的偏移量，并保存到该二维数组中去
				 */
				dropCoordinates[0] = screenX - dropCoordinates[0];
				dropCoordinates[1] = screenY - dropCoordinates[1];
				
				return dropTarget;
			}
			
		}
		
		return null;
	} 

	private void cancelDrag() {
		Utils.log(TAG, "cancelDrag()");
		if (m_bIsDragging) {
			if (m_DropTargetLast != null) {
				m_DropTargetLast.onDragExit(m_DragSource, m_iArrayCoordinatesTemp[0], m_iArrayCoordinatesTemp[1], m_iOffItemViewX, m_iOffItemViewY, m_DragViewCur, m_DragViewCur.getTag() );
				if (m_DragSource != null) {
					m_DragSource.onDropCompleted(null, m_DragViewCur, m_DragViewCur.getTag(), m_iRawX, m_iRawY, m_iArrayCoordinatesTemp[0], m_iArrayCoordinatesTemp[1], false);
				}
			}
			
		}
		
		
		endDrag();
	}


	private void endDrag() {
		Utils.log(TAG, "endDrag");
		if (m_bIsDragging) {
			m_bIsDragging = false;
		}
		
		for (DragListener l : m_DragListeners) {
			l.onDragEnd();
		}
		
		//参考解释看DragView的remove方法
/*		if (m_DragViewCur != null) {
			m_DragViewCur.remove();
		}*/
		
	}


	private boolean drop(int rawX, int rawY) {
		Utils.log(TAG, "drop()");
		final int[] coordinates = m_iArrayCoordinatesTemp;
		final DropTarget findDropTarget = findDropTarget(rawX, rawY, coordinates);
		
		//是否可以放置
		boolean bIsDrop = false;
		
		if (findDropTarget != null) {
			findDropTarget.onDragExit(m_DragSource, coordinates[0], coordinates[1], m_iOffItemViewX, m_iOffItemViewY, m_DragViewCur, m_DragViewCur.getTag() );
			if (findDropTarget.acceptDrop(m_DragSource, coordinates[0], coordinates[1], m_iOffItemViewX, m_iOffItemViewY, m_DragViewCur, m_DragViewCur.getTag()) ) {
				findDropTarget.onDrop(m_DragSource, coordinates[0], coordinates[1], m_iOffItemViewX, m_iOffItemViewY, m_DragViewCur, m_DragViewCur.getTag() );
				bIsDrop = true;
			} else {
				bIsDrop = false;
			}
			
		}
		
		m_DragSource.onDropCompleted((View)findDropTarget, m_DragViewCur, m_DragViewCur.getTag(), rawX, rawY, coordinates[0], coordinates[1], bIsDrop);
		
		return bIsDrop;
	}


	public void setWindowToken(IBinder windowToken) {
		m_WindowToken = windowToken;
	}

	
	public void registerDragListener(DragListener l) {
		m_DragListeners.add(l);
	}

	public void unregisterDragListener(DragListener l) {
		m_DragListeners.remove(l);
	}
	
	public void registerDropTarget(DropTarget dropTarget) {
		m_DropTargetList.add(dropTarget);
	}
	
	public void unregisterDropTarget(DropTarget dropTarget) {
		m_DropTargetList.remove(dropTarget);
	}
	
	
	
	public MainActivity getLauncher() {
		return m_Launcher;
	}
	

	/**
	 * 提供对拖动操作感兴趣的接口
	 * @author baoxing
	 *
	 */
	public interface DragListener {
		
		/**
		 * 拖动开始
		 * @param source	拖动源
		 * @param info	拖动View所携带的信息
		 * @param dragAction	拖动的操作 {@link Constant#DRAG_COPY} 和 {@link Constant#DRAG_MOVE}
		 */
        void onDragStart(DragSource source, Object info, int dragAction);		
        
        /**
         * 拖动结束
         */
        void onDragEnd();
	}
	
	
	
	
}
