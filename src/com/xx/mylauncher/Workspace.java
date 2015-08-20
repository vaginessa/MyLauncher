package com.xx.mylauncher;

import java.util.ArrayList;
import java.util.List;

import com.xx.mylauncher.CellInfo.CellLocation;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
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

	private boolean m_bIsDropEnable = true;
	
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
	
	private int m_iDeleteZoneHeight;
	private int m_iHotSeatHeight;
	private int m_iViewHeight;
	
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
		init(context, attrs);
	}
	
	private void init(Context context, AttributeSet set) {
		final DisplayMetrics dm = new DisplayMetrics();
		
		m_WindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		m_WindowManager.getDefaultDisplay().getMetrics(dm);
		m_iScreenHeight = dm.heightPixels;
		m_iScreenWidth = dm.widthPixels;
		
		m_iDeleteZoneHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Constant.DIMEN_DEFAULT_DELETEZONE_HEIGHT, context.getResources().getDisplayMetrics());
		m_iHotSeatHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Constant.DIMEN_DEFAULT_HOTSEAT_HEIGHT, context.getResources().getDisplayMetrics());
		
		TypedArray ta = context.obtainStyledAttributes(set, R.styleable.Launcher);
		m_iDeleteZoneHeight = (int) ta.getDimension(R.styleable.Launcher_w_deletezone_height, m_iDeleteZoneHeight);
		m_iHotSeatHeight = (int) ta.getDimension(R.styleable.Launcher_w_hotseat_height, m_iHotSeatHeight);				
		ta.recycle();

		m_iViewHeight = m_iScreenHeight - m_iDeleteZoneHeight - m_iHotSeatHeight - Utils.getStatusHeight(getContext());
		
		Utils.log(TAG, "m_iViewHeight=%d, m_iDeleteZoneHeight=%d, m_iHotSeatHeight=%d", m_iViewHeight, m_iDeleteZoneHeight, m_iHotSeatHeight);
		log("screenHeight=%d, screenWidth=%d", m_iScreenHeight, m_iScreenWidth);
	}
	
	private String tag = TAG + "onMeasure";
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
			Utils.log(tag, "at most");
			iParentHeight = (int) Math.min(iYHeight, m_iScreenHeight*HEIGHT_SCALE);
//			iParentHeight = iYHeight;
			
		} else if (iYMode == MeasureSpec.EXACTLY) {
			Utils.log(tag, "exactly");
			iParentHeight = iYHeight;
			
		} else {
			Utils.log(tag, "un..");
			iParentHeight = (int) Math.min(iYHeight, m_iScreenHeight*HEIGHT_SCALE);
			
		}
		Utils.log(tag, "iYHeight=%d", iYHeight);
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
//		int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(iParentHeight, MeasureSpec.EXACTLY);
		int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(m_iViewHeight, MeasureSpec.EXACTLY);
		for (int i=0; i<iChildCount; i++) {
			child = getChildAt(i);
			child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
		}
		
//		setMeasuredDimension(iParentWidth, iParentHeight);
		/*
		 * 为什么要指定特定的值
		 * 因为，当使用系统自带的测量体系时，当拖动图标时，它不断的测量，但是在最终结果出来前，
		 * 它的值不是单一的，所以workspace的大小会改变，导致效果不对
		 */
		setMeasuredDimension(iParentWidth, m_iViewHeight);
		
		
		log("iParentWidth[Workspace的宽度]=%d, iParentHeight[Workspace的高度]=%d, iViewHeight=%d", iParentWidth, iParentHeight, m_iViewHeight);
		log("screen height = %d, screen width = %d", m_iScreenHeight, m_iScreenWidth);
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
	
	
	public void setCellLayoutAttachLauncher(MainActivity launcher) {
		for (CellLayout item : m_ListCellLayout) {
			item.setLauncher(launcher);
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
	
	/**
	 * 返回当前所处的屏幕
	 * @return
	 * @deprecated 使用 {@link #getCurCellLayout()}，函数的意义更清晰
	 */
	public CellLayout getCurScreen() {
		return m_ListCellLayout.get(m_iCurScreen);
	}
	
	/**
	 * 返回当前所处的CellLayout
	 * @return
	 */
	public CellLayout getCurCellLayout() {
		return m_ListCellLayout.get(m_iCurScreen);
	}
	
	/**
	 * 返回指定的屏幕
	 * @param whichCellLayout
	 * @return
	 */
	public CellLayout getSpecifyCellLayout(int whichCellLayout) {
		return m_ListCellLayout.get(whichCellLayout);
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
		
		if (success && !(dropTargetView instanceof DeleteZone) ) {
			final CellInfo cellInfo = (CellInfo) itemInfo;
			m_DragController.getLauncher().getLauncherDBManager().updateDragInfo(cellInfo);
		}
		
		final boolean success1 = success;
			
		m_DragLayer.swapItemOnComplete(Workspace.this, success1);
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
		
		curCellLayout.onDrop(source, x, y, xOffset, yOffset, dragView, dragInfo);
		
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		Utils.log(TAG, "onDragEnter");
		
		if (m_ListCellLayout.size() <= 0) {
			return;
		}
		
		final CellLayout curCellLayout = m_ListCellLayout.get(m_iCurScreen);
		
		curCellLayout.onDragEnter(source, x, y, xOffset, yOffset, dragView, dragInfo);
	}

	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		
		if (m_ListCellLayout.size() <= 0) {
			return;
		}
		
		final CellLayout curCellLayout = m_ListCellLayout.get(m_iCurScreen);
		
		curCellLayout.onDragOver1(source, x, y, xOffset, yOffset, dragView, dragInfo);
	}

	@Override
	public void onDragExit(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		Utils.log(TAG, "onDragExit");
		
		if (m_ListCellLayout.size() <= 0) {
			return;
		}
		
		final CellLayout curCellLayout = m_ListCellLayout.get(m_iCurScreen);
		
		curCellLayout.onDragExit(source, x, y, xOffset, yOffset, dragView, dragInfo);
		
		
	}
	
	@Override	
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		if (m_ListCellLayout.size() <= 0) {
			return false;
		}
		final CellLayout curCellLayout = m_ListCellLayout.get(m_iCurScreen);
		/*
		 * 根据所在的移动坐标值判断是否有足够大的空间去给放置
		 * 
		 */
		return curCellLayout.acceptDrop1(source, x, y, xOffset, yOffset, dragView, dragInfo);
	}

	
	@Override
	public boolean isDropEnable() {
		return m_bIsDropEnable;
	}
	
	
	@Override
	public void getHitRectRefDragLayer(Rect outRect, DropTarget dropTarget) {
		final int iMax = 1000;
		
		ViewParent viewParent = dropTarget.getParent();
		Object objLastView = dropTarget;
		for (int i=0; i<iMax; i++) {
			if (viewParent instanceof DragLayer) {
//				Utils.log(TAG, "getHitRectRefDragLayer 计算好了. i=%d", i);
				ViewGroup view = (ViewGroup) objLastView;
				view.getHitRect(outRect);
				break;
			}
			
			objLastView = viewParent;
			viewParent = viewParent.getParent();
			
			if (i>=iMax) {
				Utils.log(TAG, "[getHitRectRefDragLayer] 这个方法写错了!!!!");
				throw new IllegalArgumentException(String.format("布局层次超过了%d层，请优化或修改最大值", iMax));
			}
		}//end for
		
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
