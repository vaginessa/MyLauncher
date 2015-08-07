package com.xx.mylauncher;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * launcher中的Workspace，它的Child是{@link CellLayout}<br/>
 * Workspace是DragSource，也是DragTarget（TODO）<br/>
 * 推荐layout_width和layout_height为match_parent<br/>
 * 子View-{@link CellLayout}的这两个属性也写为match_parent，可以满足多屏条件<br/>
 * 
 * @author baoxing
 * 
 */
public class Workspace extends PagedView  implements DragSource, DropTarget{

	private static final String TAG = "Workspace";

	private static final boolean DEBUG = true;

	private static final float HEIGHT_SCALE = 5/6f;
	
	/** 屏幕的宽度 */
	private int m_iScreenWidth;
	
	/** 屏幕的高度 */
	private int m_iScreenHeight;
	
	private WindowManager m_WindowManager;
	
	/** 拖动绘制层 */
	private DragLayer m_DragLayer;
	
	/** 拖动处理模型 */
	private DragController m_DragController;
	
	/** 保存有多少个屏幕 */
	private List<CellLayout> m_ListCellLayout = new ArrayList<CellLayout>();
	
	/** 当前所处的屏幕 */
//	private int m_iCurScreen = 0;
	
	public Workspace(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public Workspace(Context context) {
		this(context, null);
	}

	public Workspace(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context) {
		final DisplayMetrics dm = new DisplayMetrics();
		
		m_WindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		m_WindowManager.getDefaultDisplay().getMetrics(dm);
		m_iScreenHeight = dm.heightPixels;
		m_iScreenWidth = dm.widthPixels;
		
		log("screenHeight=%d, screenWidth=%d", m_iScreenHeight, m_iScreenWidth);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		final int iXWidth = MeasureSpec.getSize(widthMeasureSpec);
		final int iXMode = MeasureSpec.getMode(widthMeasureSpec);
		final int iYHeight = MeasureSpec.getSize(heightMeasureSpec);
		final int iYMode =MeasureSpec.getMode(heightMeasureSpec);
		
		final int iChildCount = getChildCount();
		
		int iParentWidth;
		int iParentHeight;
		
		/*
		 * 测量高度
		 */
		if (iYMode == MeasureSpec.AT_MOST) {
			iParentHeight = (int) Math.min(iYHeight, m_iScreenHeight*HEIGHT_SCALE);
			
		} else if (iYMode == MeasureSpec.EXACTLY) {
			iParentHeight = iYHeight;
			
		} else {
			iParentHeight = (int) Math.min(iYHeight, m_iScreenHeight*HEIGHT_SCALE);
			
		}
		/*
		 * 测量宽度，就是屏幕的宽度 * 子View的个数(多少个屏幕)，不考虑其它情况
		 * 
		 */
		iParentWidth = m_iScreenWidth * iChildCount;
		
		/*
		 * 测量子控件 CellLayout
		 */
		View child;
		int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(m_iScreenWidth, MeasureSpec.EXACTLY);
		int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(iParentHeight, MeasureSpec.EXACTLY);
		for (int i=0; i<iChildCount; i++) {
			child = getChildAt(i);
			child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
		}
		
		log("iParentWidth[Workspace的宽度]=%d, iParentHeight[Workspace的高度]=%d", iParentWidth, iParentHeight);
		log("screen height = %d, screen width = %d", m_iScreenHeight, m_iScreenWidth);
		setMeasuredDimension(iParentWidth, iParentHeight);
		
	}

	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int childCount = getChildCount();
		int top = 0;
		int left = 0;
		int right = 0;
		int bottom = 0;
		
		m_ListCellLayout.clear();
		
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			int width = child.getMeasuredWidth();
			int height = child.getMeasuredHeight();
			
			bottom = height;
			right = left + width;
			
			
			Utils.log(TAG, "child[%d]  's top=%d, left=%d, right=%d, bottom=%d", i, top, left, right, bottom);
			child.layout(left, top, right, bottom);
			
			left += width;
			
			if (child instanceof CellLayout) {
				m_ListCellLayout.add((CellLayout)child);
			}
			
		}
		
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (m_DragController != null) {
			m_DragController.setWindowToken(getWindowToken());	
		}
		
	}
	
	
	public void setCellLayoutLongPressListener(View.OnLongClickListener l) {
		for (CellLayout item : m_ListCellLayout) {
			item.setOnLongClickListener(l);
		}
	}
	
	private void log(String msg) {
		if (DEBUG) {
			Log.i(TAG, msg);
		}
		
	}
	
	private void log(String format, Object...objects) {
		if (DEBUG) {
			String msg = String.format(format, objects);
			Log.i(TAG, msg);
		}
	}
	
	
	public CellLayout getCurScreen() {
		return m_ListCellLayout.get(m_iCurScreen);
	}

	@Override
	public void setDragController(DragController dragger) {
		m_DragController = dragger;
	}

	@Override
	public void setDragLayer(DragLayer dragLayer) {
		m_DragLayer = dragLayer;
	}
	
	@Override
	public void onDropCompleted(View dropTargetView, View dragView, Object itemInfo, int rawX, int rawY, int iOffX, int iOffy, boolean success) {
		// TODO Auto-generated method stub
		Utils.log(TAG, "onDropCompleted");
		if (m_ListCellLayout.size() <= 0) {
			return;
		}
		final CellLayout curCellLayout = m_ListCellLayout.get(m_iCurScreen);
		
		curCellLayout.onDropCompleted(dropTargetView, dragView, itemInfo, rawX, rawY, iOffX, iOffy, success);
	}

		
	
	/*
	 * 可以放置时调用的函数
	 * (non-Javadoc)
	 * @see com.xx.mylauncher.DropTarget#onDrop(com.xx.mylauncher.DragSource, int, int, int, int, com.xx.mylauncher.DragView, java.lang.Object)
	 */
	@Override
	public void onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		Utils.log(TAG, "onDrop");
		if (m_ListCellLayout.size() <= 0) {
			return;
		}
		final CellLayout curCellLayout = m_ListCellLayout.get(m_iCurScreen);
		if (source == this) {	//source instanceof Workspace ...
			curCellLayout.onDrop(source, x, y, xOffset, yOffset, dragView, dragInfo);
		}
		
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		Utils.log(TAG, "onDragEnter");
		
	}

	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		
		if (m_ListCellLayout.size() <= 0) {
			return;
		}
		
		final CellLayout curCellLayout = m_ListCellLayout.get(m_iCurScreen);
		if (source == this) {
			curCellLayout.onDragOver(source, x, y, xOffset, yOffset, dragView, dragInfo);
		}
		
	}

	@Override
	public void onDragExit(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		Utils.log(TAG, "onDragExit");
	}
	
	/*
	 * @param source	从哪里拖动过来的，拖动源
	 * @param x				到父View的偏移量{@link DropTarget}，如workspace
	 * @param y				到父View的偏移量{@link DropTarget}，如workspace
	 * @param xOffset	到View本身的偏移量，即到长按下的View的偏移量
	 * @param yOffset
	 * @param dragView	拖动的View，绘制表现层在DragLayer中
	 * @param dragInfo	拖动的View所携带的信息
	 */
	@Override	
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
//		Utils.log(TAG, "acceptDrop");
		if (m_ListCellLayout.size() <= 0) {
			return false;
		}
		final CellLayout curCellLayout = m_ListCellLayout.get(m_iCurScreen);
		/*
		 * 根据所在的移动坐标值判断是否有足够大的空间去给放置
		 * 
		 */
		if (source == this) {
			return curCellLayout.acceptDrop(source, x, y, xOffset, yOffset, dragView, dragInfo);
			
		}
		
		return false;
	}

	@Override
	protected int getScreenCounts() {
		return m_ListCellLayout.size();
	}	
	
	@Override
	protected int getWorkspaceWidth() {
		final int iCounts = m_ListCellLayout.size();
		final int iUnitSize = m_iScreenWidth;
		
		return (iCounts-1)*iUnitSize;
	};
	
	@Override
	protected MainActivity getLauncher() {
		return m_DragController.getLauncher();
	}
	
}
