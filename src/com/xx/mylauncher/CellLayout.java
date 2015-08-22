package com.xx.mylauncher;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * 每一屏幕的布局，是一个可以容纳不同大小View的容器
 * <p>现在实现的是，可以在@{link CellLayout}中指定水平间隔和垂直间隔</p>
 * <p>指定单个格子的大小，自动根据算得到的CellLayout的宽和高计算水平格子数和垂直格子数</p>
 * @author baoxing
 * 
 */
public class CellLayout extends ViewGroup {
	
	/** 格子默认的大小 78dp */
	private  int Cell_Size_Default;
	private static final int CELL_SIZE_DEFAULT = 78;
	
	/** 格子默认的间隔 */
	private int Space_Default;
	private static final int SPACE_DEFAULT = 10;
	
	
	private static final String TAG = "CellLayout";

	private int m_iScreenWidth;
	
	private int m_iScreenHeight;
	
	private WindowManager m_WindowManager;
	
	/** 格子水平间隔 */
	private int m_iSpaceHorizatation;
	
	/** 格子垂直间隔 */
	private int m_iSpaceVertical;
	
	/** 格子的大小 */
	private int m_iCellSize;
	
	/** 水平格子数 */
	private int m_iCellHCount;
	
	/** 垂直格子数 */
	private int m_iCellVCount;
	
	/** [行数VSpan][列数HSpan] */
//	private volatile boolean m_bCellsOcupied[][];
	private boolean m_bCellsOcupied[][];
	
	/** 当前屏幕的所有child view的索引 array[cellX][cellY] */
	private View[][] m_ViewChildren;
	
	private Rect m_RectTemp = new Rect();
	
	/**Launcher 引用 *///TODO要修改
	private MainActivity m_Launcher;
	
	/** 缩放高度，和Workspace一样 */
	private static final float HEIGHT_SCALE = 3/4f;
	
	/** 当拖动一块View到CellLayout时，保存相关的信息用于绘制辅助效果 */
	private DragObjectInfo m_DragObjectInfo = new DragObjectInfo();
	
	public CellLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CellLayout(Context context) {
		this(context, null);
	}

	public CellLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}


	private void init(Context context, AttributeSet attrs) {
		DisplayMetrics metrics = new DisplayMetrics();
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.Launcher);
		
		Cell_Size_Default = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CELL_SIZE_DEFAULT, getResources().getDisplayMetrics());
		Space_Default = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, SPACE_DEFAULT, getResources().getDisplayMetrics());
		
		m_iCellSize = (int) ta.getDimension(R.styleable.Launcher_cellSize, Cell_Size_Default);
		m_iSpaceHorizatation = (int) ta.getDimension(R.styleable.Launcher_spaceHorizentation, Space_Default);
		m_iSpaceVertical = (int) ta.getDimension(R.styleable.Launcher_spaceVertical, Space_Default);
		
		//TODO 这里有一些属性不全，参考CelllInfo补齐
		
		ta.recycle();
		
		m_WindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		m_WindowManager.getDefaultDisplay().getMetrics(metrics);
		m_iScreenWidth = metrics.widthPixels;
		m_iScreenHeight = metrics.heightPixels;
		
		Utils.log(TAG, "Cell Size = %d, m_iSpaceVertical = %d, m_iSpaceHorizatation = %d", m_iCellSize, m_iSpaceHorizatation, m_iSpaceVertical);
		
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	
		final int iWidthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int iHeightMode = MeasureSpec.getMode(heightMeasureSpec);
		final int iWidthSize = MeasureSpec.getSize(widthMeasureSpec);
		final int iHeightSize = MeasureSpec.getSize(heightMeasureSpec);
		final int childCount = getChildCount();
		
		int iParentWidth;
		int iParentHeight;
		
		int iCellHCount;
		int iCellVCount;

		if (iWidthMode == MeasureSpec.EXACTLY) {
			Utils.log(TAG, "width-exactly");
			iParentWidth = iWidthSize;
		} else if (iWidthMode == MeasureSpec.AT_MOST) {
			iParentWidth = m_iScreenWidth;
		} else {
			iParentWidth = m_iScreenWidth;
		}
		
		if (iHeightMode == MeasureSpec.EXACTLY) {
			Utils.log(TAG, "height-exactly");
			iParentHeight = iHeightSize;
		} else if (iHeightMode == MeasureSpec.AT_MOST) {
			Utils.log(TAG, "height-at most");
			iParentHeight = (int) (m_iScreenHeight * HEIGHT_SCALE);
			
		} else {
			Utils.log(TAG, "height-un...");
			iParentHeight = (int) (m_iScreenHeight * HEIGHT_SCALE);
			
		}
		
		/*
		 * 排列子view，cell
		 */
		
		//这里先只考虑平分格子排序，所以设置的水平和垂直格子间隔先不考虑
		m_iCellHCount = iCellHCount = (iParentWidth - getPaddingLeft() - getPaddingRight() ) / (m_iCellSize + m_iSpaceHorizatation); 
		m_iCellVCount = iCellVCount = (iParentHeight - getPaddingTop() - getPaddingBottom() ) / (m_iCellSize + m_iSpaceVertical);
		
		m_ViewChildren = new View[iCellHCount][iCellVCount];
		
		m_bCellsOcupied = new boolean[iCellVCount][iCellHCount];
		
		for (int i=0; i<iCellVCount; i++) {
			for (int j=0; j<iCellHCount; j++) {
				m_bCellsOcupied[i][j] = false;
			}
		}
		
		Utils.log(true, TAG, "水平格子数=%d, 垂直格子数=%d", m_iCellHCount, m_iCellVCount);
		
		View child;
		for (int i = 0; i < childCount; i++) {
			child = getChildAt(i);
			CellLayout.LayoutParams lp = (LayoutParams) child.getLayoutParams();
			
			int cellHSpan = lp.cellHSpan;
			int cellVSpan = lp.cellVSpan;
			
			int width = cellHSpan * m_iCellSize + (cellHSpan - 1) * m_iSpaceHorizatation;
			int height = cellVSpan * m_iCellSize + (cellVSpan  -1) * m_iSpaceVertical;
			
			int childWidthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
			int childHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
			
			child.measure(childWidthSpec, childHeightSpec);
//			measureChildWithMargins(child, childWidthSpec, 0, childHeightSpec, 0);
			
			Utils.log(false, TAG, "child[%d] width=%d, height=%d", i, width, height);
		}
		
		
		Utils.log(TAG, "CellLayout: width=%d, height=%d", iParentWidth, iParentHeight);
		setMeasuredDimension(iParentWidth, iParentHeight);		
		
	}
	
	

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int childCounts = getChildCount();
		View child;
		
		for (int i = 0; i<childCounts; i++) {
			child = getChildAt(i);
			
			//TODO 这里需要，如何最好的组织当一个Item View按下时重置它在CellLayout的占用位置
			if (child.getVisibility() != View.VISIBLE) {
				continue;
			}
			
			CellLayout.LayoutParams lp = (LayoutParams) child.getLayoutParams();
			
			int cellX = lp.cellX;
			int cellY = lp.cellY;
			
			int left = (cellX + 1) * m_iSpaceHorizatation + cellX * m_iCellSize + lp.leftMargin + getPaddingLeft();
			int top = (cellY + 1) * m_iSpaceVertical + cellY * m_iCellSize + lp.topMargin + getPaddingTop();
			int right = left + child.getMeasuredWidth() - getPaddingRight();
			int bottom = top + child.getMeasuredHeight() - getPaddingBottom();
			
			child.layout(left, top, right, bottom);
			
			//TODO 这里要给view设置tag，内容为CellInfo
			//考虑是否维护一个子view的列表，还是直接在launcher中设置setOnLongClickListener
			//现在先往简单的方向走，后着
			
//			Utils.log(true, TAG, "left=%d, top=%d, right=%d, bottom=%d, width=%d, height=%d", left, top, right, bottom, child.getMeasuredWidth(), child.getMeasuredHeight() );
			//记录哪些单元格已经被占领了
			int cellHSpan = lp.cellHSpan;
			int cellVSpan = lp.cellVSpan;
			
			/*
			 * 添加 child view
			 */
			for (int ti=cellX; ti<=cellX+cellHSpan-1; ti++) {
				for (int tj=cellY; tj<=cellY+cellVSpan-1; tj++) {
					m_ViewChildren[ti][tj] = child;
				}
			}
			
//			Utils.log(TAG, "cellX=%d, cellY=%d, cellHSpan=%d, cellVSpan=%d", cellX, cellY, cellHSpan, cellVSpan);
			for (int ti=cellY; ti<=cellY+cellVSpan-1; ti++) {
				for (int tj=cellX; tj<=cellX+cellHSpan-1; tj++) {
					m_bCellsOcupied[ti][tj] = true;
				}
			}
		}
		
//		Utils.debugCellLayoutChildren(TAG+"children", m_ViewChildren);
	}
	
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		
		return super.onInterceptTouchEvent(ev);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		return super.onTouchEvent(event);
	}
	
	
	
	
	/**
	 * 在指定位置是否可以添加子View
	 * @param cellX	格子数的X轴起点
	 * @param cellY
	 * @param cellHSpan		水平格子数所占的数量
	 * @param cellVSpan
	 * @return
	 */
	public boolean isAcceptAddChildAtPosition(int cellX, int cellY, int cellHSpan, int cellVSpan) {
		boolean result = true;
		
		for (int ti=cellY; ti<=cellY+cellVSpan-1; ti++) {
			for (int tj=cellX; tj<=cellX+cellHSpan-1; tj++) {
				if (m_bCellsOcupied[ti][tj]) {
					result = false;
					break;
				}
			}
			
		}
		
		return result;
	}
	
	
	/**
	 * 是否可以添加到该CellLayout中，如果可以返回所有的可以添加的cellX 和 cellY坐标，
	 * 即格子的起点坐标
	 * @param view	要添加的View
	 * @return 返回所有的可以添加的cellX 和 cellY坐标, 元素只中的 item[0]=cellX, item[1]=cellY
	 */
	public List<int[]> isAcceptAddChild(View view) {
		android.view.ViewGroup.LayoutParams lp = view.getLayoutParams();

		CellLayout.LayoutParams params = (LayoutParams) lp;
		
		return isAcceptAddChild(params);
	}
	
	/**
	 * 是否可以添加到该CellLayout中，如果可以返回所有的可以添加的cellX 和 cellY坐标，
	 * 即格子的起点坐标
	 * @param params		view的LayoutParams
	 * @return	返回所有的可以添加的cellX 和 cellY坐标, 元素只中的 item[0]=cellX, item[1]=cellY
	 */
	public List<int[]> isAcceptAddChild(CellLayout.LayoutParams params) {
		if (!(params instanceof CellLayout.LayoutParams) ) {
			throw new IllegalArgumentException(String.format("cause:%s", "View的LayoutParams类型错误，必须设置为CellLayout.LayoutParams类型"));
		}

		int cellHSpan = params.cellHSpan;
		int cellVSpan = params.cellVSpan;
		
		return isAcceptAddChild(cellHSpan, cellVSpan);
		
	}
	
	/**
	 * 是否可以添加到该CellLayout中，如果可以返回所有的可以添加的cellX 和 cellY坐标，
	 * 即格子的起点坐标
	 * @param cellHSpan		格子水平所占的数量
	 * @param cellVSpan
	 * @return 返回所有的可以添加的cellX 和 cellY坐标, 元素只中的 item[0]=cellX, item[1]=cellY
	 */
	public List<int[]> isAcceptAddChild(int cellHSpan, int cellVSpan) {
		
		final boolean[][] bCellSet = m_bCellsOcupied;
		final int iHCellCount = m_iCellHCount;
		final int iVCellCount = m_iCellVCount;
		
		Utils.log(true, TAG, "iHCellCount=%d, iVCellCount=%d", iHCellCount, iVCellCount);
		
		//TODO
		//可以考虑是否加入所占的格子的数量大于屏幕总的格子数
		//忽略的话，可能会得到想要的显示，但不完整
		if (cellHSpan < 1 || cellVSpan < 1 ) {
			throw new IllegalArgumentException(
					String.format("cause:%s. cellHSpan=%d, cellVSpan=%d", 
							"参数异常，格子所占的数量不能小于1",  cellHSpan, cellVSpan) );
		}
		
		//这里是难点了，怎么使用算法检索出来
		//使用枚举查找
		List<int[]> result = new ArrayList<int[]>();
		
		NEXT_LINE:
		for (int i=0; i<iVCellCount; i++) {
			
			NEXT_POINT:
			for (int j=0; j<iHCellCount; j++) {
				
				for (int m=i; m<i+cellVSpan; m++) {
					for (int n=j; n<j+cellHSpan; n++) {
						
						if (n>iHCellCount-1 || m>iVCellCount-1) {	// 超出了数组范围
							continue NEXT_LINE;
						}
						
						if (bCellSet[m][n] == true) {
							continue NEXT_POINT;
						}
						
						if (m>=i+cellVSpan-1 && n>=j+cellHSpan-1) {
							int[] item = new int[2];
							item[0] = j;
							item[1] = i;
							result.add(item);
						}
						
					}
					
				}
				
			}
			
		}
		
		return result;
	}
	
	public void clearFlagsOcupied(int cellX, int cellY, int cellHSpan, int cellVSpan) {
		for (int ti=cellY; ti<=cellY+cellVSpan-1; ti++) {
			for (int tj=cellX; tj<=cellX+cellHSpan-1; tj++) {
				m_bCellsOcupied[ti][tj] = false;
				
//				debugBooleanArray();
			}
		}
	}
	
	public void clearFlagsOcupid(final CellInfo cellInfo) {
		clearFlagsOcupied(cellInfo.getCellX(), cellInfo.getCellY(), cellInfo.getCellHSpan(), cellInfo.getCellVSpan());
	}
	
	public void flagOcupied(int cellX, int cellY, int cellHSpan, int cellVSpan) {
		for (int ti=cellY; ti<=cellY+cellVSpan-1; ti++) {
			for (int tj=cellX; tj<=cellX+cellHSpan-1; tj++) {
				m_bCellsOcupied[ti][tj] = true;
				
//				debugBooleanArray();
			}
		}
	}
	
	public void flagOcuped(final CellInfo cellInfo) {
		final int cellX = cellInfo.getCellX();
		final int cellY = cellInfo.getCellY();
		final int cellHSpan = cellInfo.getCellHSpan();
		final int cellVSpan = cellInfo.getCellVSpan();
		
		flagOcupied(cellX, cellY, cellHSpan, cellVSpan);
	}
	
   /**
    * @see DragSource#onDropCompleted(View, View, Object, int, int, int, int, boolean) 
    */
	public void onDropCompleted(View dropTargetView, View dragView, Object itemInfo, int rawX, int rawY, int iOffX, int iOffy, boolean success) {
		Utils.log(TAG, "onDropCompleted. success=%b", success);
		
		final boolean bIsDropSelf = dropTargetView instanceof Workspace;
		
		if (bIsDropSelf) {
			onDropCompletedInSelf(dragView, itemInfo, success);
			
		} else if (dropTargetView instanceof HotSeat) {
			if (!success) {
				onDropCompletedInSelf(dragView, itemInfo, success);
				
			} else {
				/*
				 * 交给dropTargetView的onDrop中取处理滑动
				 */
			}
		} else if (dropTargetView instanceof DeleteZone) {
			/*
			 * delete view
			 */
			if (success) {
				final CellInfo cellInfo = (CellInfo) itemInfo;
				final int whichCellLayout = cellInfo.getScreen();
				final CellLayout cellLayout = m_Launcher.getWorkspace().getSpecifyCellLayout(whichCellLayout);
				
				m_Launcher.getDragLayer().removeView(dragView);
				cellLayout.removeView(cellInfo.getView());
				
			} else {
				onDropCompletedInSelf(dragView, itemInfo, success);
				
			}
		} else {
			onDropCompletedInSelf(dragView, itemInfo, success);
		}
		
	}

	/**
	 * 当拖放结束时，item view 是回到自身，即拖拽源和拖放目的地一样
	 * @param dragView
	 * @param itemInfo
	 * @param success
	 */
	private void onDropCompletedInSelf(View dragView, Object itemInfo,
			boolean success) {
		/*
		 * 使DragView平滑移动到原来的位置或新的位置，Item View设置可见，draglayer-invalid
		 */
		final DropObjectInfo info = new DropObjectInfo();
		final CellInfo cellInfo = (CellInfo) itemInfo;
		final DragObjectInfo dragInfo = m_DragObjectInfo;
		final int[] iArrayTempOff = { 0, 0 };

		adjustToDragLayer(iArrayTempOff, dragView, getContext(), true);
		info.finalX = iArrayTempOff[0];
		info.finalY = iArrayTempOff[1];

		info.dragView = (DragView) dragView;
		info.itemView = cellInfo.getView();

		adjustToDragLayer(iArrayTempOff, cellInfo.getView(), getContext(), true);

		if (!success) {
			// 移动回原来的位置
			info.originX = iArrayTempOff[0];
			info.originY = iArrayTempOff[1];
			info.cellX = cellInfo.getCellX();
			info.cellY = cellInfo.getCellY();
			info.cellHSpan = cellInfo.getCellHSpan();
			info.cellVSpan = cellInfo.getCellVSpan();
			info.canDrop = false;
		} else {
			// 移动到新的位置
			info.canDrop = true;
			info.originX = dragInfo.x; // TODO relative to draglayer take a
										// cause
			info.originY = dragInfo.y;
			info.cellX = dragInfo.cellX;
			info.cellY = dragInfo.cellY;
			info.cellHSpan = dragInfo.cellHSpan;
			info.cellVSpan = dragInfo.cellVSpan;
		}
		info.init();

		Utils.log(TAG, "offset draglayer-offLeft=%d, offTop=%d",
				iArrayTempOff[0], iArrayTempOff[1]);
		Utils.log(TAG, info.toString());

		flagOcupied(info.cellX, info.cellY, info.cellHSpan, info.cellVSpan);
		m_Launcher.getDragLayer().updateDragViewToOriPoint(info);
	}
	
	/** temp */
	private int[] m_iArrayTempCoor = new int[2];
	/**
	 * 返回View的相对于DragLayer的坐标值
	 * @param coordnates coordnates[0]=left, coordnates[1]=top，返回值
	 * @param view
	 * @param context
	 * @param subStatus	是否减去状态栏的高度，一般设为true，减去其高度
	 */
	public void adjustToDragLayer(int[] coordnates, View view, Context context, boolean subStatus) {
		if (view == null) {
			return;
		}
		
		if (coordnates == null) {
			throw new NullPointerException();
		}
		
		final DragLayer dragLayer = m_Launcher.getDragLayer();
		
		int iOffLeft = 0;
		int iOffTop = 0;
		coordnates[0] = 0;
		coordnates[1] = 0;
		
		final int[] iArrayTempCoor = m_iArrayTempCoor; 
		view.getLocationOnScreen(iArrayTempCoor);
		
		iOffLeft = iArrayTempCoor[0] - dragLayer.getLeft();
		iOffTop = iArrayTempCoor[1] - dragLayer.getTop();
		
		coordnates[0] += iOffLeft;
		coordnates[1] += iOffTop;
		
		if (subStatus) {
			int iStatusHeight = Utils.getStatusHeight(context);
			coordnates[1] -= iStatusHeight;
		}
	}
	
	/**
	 * 可以放置时调用的函数
	 * @see com.xx.mylauncher.DropTarget#onDrop(com.xx.mylauncher.DragSource, int, int, int, int, com.xx.mylauncher.DragView, java.lang.Object)
	 */
	public void onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		Utils.log(TAG, "onDrop");
		
		final boolean bIsDragSourceSelf = source instanceof Workspace;
		final CellInfo cellInfo = (CellInfo) dragInfo;
		final View itemView = cellInfo.getView();
		final CellLayout.LayoutParams lp = (LayoutParams) itemView.getLayoutParams();
		final DragObjectInfo dragObjectInfo = m_DragObjectInfo;
		
		lp.cellX = m_DragObjectInfo.cellX;
		lp.cellY = m_DragObjectInfo.cellY;
		lp.cellHSpan = m_DragObjectInfo.cellHSpan;
		lp.cellVSpan = m_DragObjectInfo.cellVSpan;
		
		cellInfo.setLocation(CellInfo.CellLocation.WORKSPACE);
		
		// 设置占用的格子
		for (int ti = lp.cellY; ti <= lp.cellY + lp.cellVSpan - 1; ti++) {
			for (int tj = lp.cellX; tj <= lp.cellX + lp.cellHSpan - 1; tj++) {
				m_bCellsOcupied[ti][tj] = true;
			}
		}
		// requestLayout();

		if (bIsDragSourceSelf) {
			/*
			 * 叫给dragsource取处理滑动
			 */
			
		} else if (source instanceof HotSeat) {
			/*
			 * 接手滑动
			 */
			final HotSeat hotSeat = (HotSeat) source;
			final ShortCutView2 viewShortCut = (ShortCutView2) itemView;
			hotSeat.removeView(viewShortCut);
			viewShortCut.setLabelVisibility(View.VISIBLE);
//			viewShortCut.setVisibility(View.INVISIBLE);
			addView(viewShortCut);
			requestLayout();
			
			final DropObjectInfo dropSliceInfo = new DropObjectInfo();
			final int[] iArrayTemoOff = {0, 0};
			
			
			dropSliceInfo.dragView = dragView;
			dropSliceInfo.itemView = cellInfo.getView();
			dropSliceInfo.cellX = cellInfo.getCellX();
			dropSliceInfo.cellY = cellInfo.getCellY();
			dropSliceInfo.cellHSpan = cellInfo.getCellHSpan();
			dropSliceInfo.cellVSpan = cellInfo.getCellVSpan();
			
			adjustToDragLayer(iArrayTemoOff, dragView, getContext(), true);
			dropSliceInfo.finalX = iArrayTemoOff[0];
			dropSliceInfo.finalY = iArrayTemoOff[1];
//			adjustToDragLayer(iArrayTemoOff, cellInfo.getView(), getContext(), true);
//			dropSliceInfo.originX = iArrayTemoOff[0];
//			dropSliceInfo.originY = iArrayTemoOff[1];
			dropSliceInfo.originX = dragObjectInfo.x;
			dropSliceInfo.originY = dragObjectInfo.y;
			
			dropSliceInfo.canDrop = true;
			
			dropSliceInfo.init();
			
			m_Launcher.getDragLayer().updateDragViewToOriPoint(dropSliceInfo);
		}
		
	}
	
	/*
	public void onDragOver(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		acceptDrop(source, x, y, xOffset, yOffset, dragView, dragInfo);
	}
	*/
	
	public void onDragEnter(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		Utils.log(TAG, "onDragEnter");
		
		initDragObjectInfo(dragInfo);
		
		initFollowDragObject(dragView);
	}

	private void initDragObjectInfo(final Object dragInfo) {
		final CellInfo cellInfo = (CellInfo) dragInfo;
		m_DragObjectInfo = new DragObjectInfo();
		final DragObjectInfo dragObjectInfo = m_DragObjectInfo;
		dragObjectInfo.reset();
		dragObjectInfo.preCellX = cellInfo.getCellX();
		dragObjectInfo.preCellY = cellInfo.getCellY();
	}

	private void initFollowDragObject(DragView dragView) {
		m_Launcher.getDragLayer().initFollowDragObject();
		m_DragObjectInfo.clearFollowDragObjectQueue();
	}
	
	public void onDragExit(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		Utils.log(TAG, "onDragExit");
	}

    /**
     * 是否可以放置
	 * @param source	从哪里拖动过来的，拖动源
	 * @param x	到父View的偏移量{@link DropTarget}，如workspace
	 * @param y	到父View的偏移量{@link DropTarget}，如workspace
	 * @param xOffset	到View本身的偏移量，即到长按下的View的偏移量
	 * @param yOffset
	 * @param dragView	拖动的View，绘制表现层在DragLayer中
	 * @param dragInfo	拖动的View所携带的信息
     * @return
     */
	public void onDragOver1(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		
		final DragObjectInfo dragObjectInfo = m_DragObjectInfo;
		final CellInfo cellInfo = (CellInfo) dragInfo;
		
		 /* 求相对于item view本身的格子级偏移量，左上角为[0, 0] */
		final int[] iArrayCellRefItemView = new int[2];	//int[0] = cellX, int[1] = cellY
		
		/* 求按下的点相对于droptarget 的格子级偏移量 */
		final int[] iArrayCellRefDropTarget = new int[2];
		
		calcCellRefItemView(iArrayCellRefItemView, xOffset, yOffset);
		calcCellRefDropTarget(iArrayCellRefDropTarget, x, y);
		final boolean bIsCanDrop = calcDragViewMapDropTarget(iArrayCellRefItemView, iArrayCellRefDropTarget, dragObjectInfo, dragInfo);

		if (bIsCanDrop) {
			cellInfo.setCellX(dragObjectInfo.cellX);
			cellInfo.setCellY(dragObjectInfo.cellY);
			adjustCoors(dragObjectInfo, dragInfo);
		}
		
		dragObjectInfo.canDrop = bIsCanDrop;
		
		/*
		 * 赋值
		 */
		dragObjectInfo.dragView = dragView;
		dragObjectInfo.itemView = cellInfo.getView();
		dragObjectInfo.isInCell = true;
		dragObjectInfo.isInvalid = true;
		
		boolean b1 = (source instanceof HotSeat) && !bIsCanDrop;
		
		if (!b1 && bIsCanDrop) {
			dragObjectInfo.offerDragObjectInQueue((DragObjectInfo) dragObjectInfo.clone() );
		}
		
//		m_Launcher.getDragLayer().updateDragPreEffect(dragObjectInfo);
		m_Launcher.getDragLayer().updateDragFollowDrag(dragObjectInfo);
		m_Launcher.getDragLayer().updateSwapItem(dragObjectInfo);
	}
	
	/**
	 * 计算Item View是否可以完全映射，并且该位置没有占用，可以放置
	 * 占用的单元格保存在dragObjectInfo的列表中
	 * @param iArrayCellRefItemView
	 * @param iArrayCellRefDropTarget
	 * @param dragObjectInfo
	 * @param dragInfo
	 * @return	true，可以放置，占用列表为空；<br/>
	 * 					false，不可以占用，占用列表有值，或者移出了边界
	 */
	private boolean calcDragViewMapDropTarget(int[] iArrayCellRefItemView,
			int[] iArrayCellRefDropTarget, DragObjectInfo dragObjectInfo,
			Object dragInfo) {
		
		final String tag = TAG + "calc";
		/*
		 * 得到item view 的左上原点相对droptarget的格子坐标
		 */
		final CellInfo cellInfo = (CellInfo) dragInfo;
		final int iNHSpan = cellInfo.getCellHSpan();
		final int iNVSpan = cellInfo.getCellVSpan();
		final int iNHSpanCount = m_iCellHCount;
		final int iNVSpanCount = m_iCellVCount;
		final boolean[][] bArrayOcupid = m_bCellsOcupied;
		final int iOffsetCellXOri = iArrayCellRefItemView[0];
		final int iOffsetCellYOri = iArrayCellRefItemView[1];
		final int iCellXRefDropTarget = iArrayCellRefDropTarget[0] - iOffsetCellXOri;
		final int iCellYRefDropTarget = iArrayCellRefDropTarget[1] - iOffsetCellYOri;
		
		boolean bIsOutBound1 = (iCellXRefDropTarget < 0) || (iCellYRefDropTarget < 0);
		boolean bIsOutBound2 = (iCellXRefDropTarget+iNHSpan>iNHSpanCount) || (iCellYRefDropTarget+iNVSpan>iNVSpanCount);
		boolean bIsOutBound = bIsOutBound1 || bIsOutBound2;

		if (!bIsOutBound) {
			dragObjectInfo.flagOcupiedList.clear();
			for (int i=iCellYRefDropTarget; i<iCellYRefDropTarget+iNVSpan; i++) {
				for (int j=iCellXRefDropTarget; j<iCellXRefDropTarget+iNHSpan; j++ ) {
					if (bArrayOcupid[i][j]) {
						int[] bArrOcupyItem = new int[2];
						bArrOcupyItem[0] = j;
						bArrOcupyItem[1] = i;
						dragObjectInfo.flagOcupiedList.add(bArrOcupyItem);
						
					}
				}
				
			}
			
		}
		
		boolean result = dragObjectInfo.flagOcupiedList.isEmpty() && !bIsOutBound;
		
		/*
		 * 赋值
		 */
		if (result) {
			dragObjectInfo.cellX = iCellXRefDropTarget;
			dragObjectInfo.cellY = iCellYRefDropTarget;
		}

		dragObjectInfo.cellHSpan = iNHSpan;
		dragObjectInfo.cellVSpan = iNVSpan;
		dragObjectInfo.cellXPress = iArrayCellRefDropTarget[0];
		dragObjectInfo.cellYPress = iArrayCellRefDropTarget[1];
		
		Utils.log(tag, "cellX=%d, cellY=%d, cellXRef=%d, cellYRef=%d, iNHspan=%d, iNVspan=%d bIsOutBound1=%b, bIsOutBound2=%b\nflagOcupid=[%s], " +
				"isEmpy=%b, result=%b",
				iArrayCellRefItemView[0], iArrayCellRefItemView[1], iCellXRefDropTarget, iCellYRefDropTarget, iNHSpan, iNVSpan, bIsOutBound1, bIsOutBound2, 
					dragObjectInfo.getflagOcupiedListStr(), dragObjectInfo.flagOcupiedList.isEmpty(), result);
		debugBooleanArray();

		
		return result;
	}

	/**
	 * 求移动中的点相对于droptarget 的格子级偏移量
	 * @param iArrayCellRefDropTarget	a[0]=cellX, a[1]=cellY
	 * @param x
	 * @param y
	 */
	private void calcCellRefDropTarget(final int[] iArrayCellRefDropTarget, int x,
			int y) {
		final int iCellSize = m_iCellSize;
		final int iHSpace = m_iSpaceHorizatation;
		final int iVSpace = m_iSpaceVertical;
		final int iNHorizaton = m_iCellHCount;
		final int iNVertical = m_iCellVCount;
		final int iUnitSizeX = iCellSize + iHSpace;
		final int iUnitSizeY = iCellSize + iVSpace;
		int iCellX;
		int iCellY;
		
		//cellX
		if (x <= iHSpace/2) {
			iCellX = 0;
		} else if (x >= (iHSpace/2 + iUnitSizeX*iNHorizaton) ) {
			iCellX = iNHorizaton-1;
		} else {
			iCellX = (x-iHSpace/2) / iUnitSizeX;
		}
		//cellY
		if (y <= iVSpace/2) {
			iCellY = 0;
		} else if (y >= (iVSpace/2 + iUnitSizeY*iNVertical) ) {
			iCellY = iNVertical;
		} else {
			iCellY = (y-iVSpace/2) / iUnitSizeY;
		}
		
		iArrayCellRefDropTarget[0] = iCellX;
		iArrayCellRefDropTarget[1] = iCellY;
	}

	/**
	 * 求相对于item view本身的格子级偏移量，左上角为[0, 0]
	 * 保存在传入的数组中
	 * @param iArrayCellRefItemView	a[0]=cellX, a[1]=cellY	
	 * @param xOffset	相对于view的偏移量
	 * @param yOffset
	 * @param dragInfo
	 */
	private void calcCellRefItemView(final int[] iArrayCellRefItemView, final int xOffset,
			final int yOffset) {
		final int iCellSize = m_iCellSize;
		final int iHSpace = m_iSpaceHorizatation;
		final int iVSpace = m_iSpaceVertical;
		final int iHUnitSize = iCellSize + iHSpace / 2;
		final int iVUnitSize = iCellSize + iVSpace / 2;
		
		int iCellX = xOffset / iHUnitSize;
		int iCellY = yOffset / iVUnitSize;
		
		iArrayCellRefItemView[0] = iCellX;
		iArrayCellRefItemView[1] = iCellY;
	}

	public boolean acceptDrop1(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		
		final DragObjectInfo dragObjectInfo = m_DragObjectInfo;
		final boolean bCanDrop = dragObjectInfo.canDrop;
		
		Utils.log(TAG, "acceptDrop1, %b", bCanDrop);
		
		return bCanDrop;
	}
	
	/*
	
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {

		final int iCellWidth = m_iCellSize;
		final int iHSpace = m_iSpaceHorizatation;
		final int iVSpace = m_iSpaceVertical;
		final int iHSpanCount = m_iCellHCount + (iHSpace / iCellWidth) + 1; // 考虑间隔比格子大的情况
		final int iVSpanCount = m_iCellVCount + (iVSpace / iCellWidth) + 1;
		final int iHSpanNum = m_iCellHCount;
		final int iVSpanNum = m_iCellVCount;

		final DragObjectInfo dragObjectInfo = m_DragObjectInfo;
		m_DragObjectInfo.reset();

		// TODO
		// 注意DragView是拖动的View，不是原来的Item View，因为可能会改动DragView来满足效果
		CellInfo cellInfo = (CellInfo) dragInfo;
		int iItemViewWidth = cellInfo.getCellHSpan() * iCellWidth
				+ (cellInfo.getCellHSpan() - 1) * iHSpace;
		int iItemViewHeight = cellInfo.getCellVSpan() * iCellWidth
				+ (cellInfo.getCellVSpan() - 1) * iVSpace;

		if (!((xOffset >= 0 && xOffset <= iItemViewWidth) && (yOffset >= 0 && yOffset <= iItemViewHeight))) {
			Utils.log(
					TAG,
					"超出边界.xOffset=%d, yOffset=%d, iItemViewWidth=%d, iItemViewHeight=%d",
					xOffset, yOffset, iItemViewWidth, iItemViewHeight);
		}

		if ((xOffset >= 0 && xOffset <= iItemViewWidth)
				&& (yOffset >= 0 && yOffset <= iItemViewHeight)) {
			// Utils.log(TAG, "满足边界条件");
			
			 * 相对于自身的格子级偏移量
			 
			int cellHLocRefSelf = xOffset / iCellWidth; // 这里如果间隔比格子大，则计算错误
														// //TODO
			int cellVLocRefSelf = yOffset / iCellWidth;

			
			 * 相对于CellLayout的格子级偏移量 这里和怎么放置子View的逻辑密切相关，如果设置padding/margin属性的话
			 
			x += getPaddingLeft();
			y += getPaddingTop();

			int cellHLocRefParent = 0;
			int cellVLocRefParent = 0;

			int iTempDistance = 0;

			for (int i = 0; i < iHSpanCount; i++) {
				iTempDistance += iHSpace;
				if (iTempDistance > x) {
					// 在间隔中
					dragObjectInfo.isInvalid = false;
					dragObjectInfo.isInCell = false;
					return false;
				}

				iTempDistance += iCellWidth;
				if (iTempDistance >= x) {
					// 找到了
					dragObjectInfo.isInvalid = true;
					dragObjectInfo.isInCell = true;
					dragObjectInfo.cellXPress = cellHLocRefParent;
					break;
				}
				cellHLocRefParent++;
			} // end for

			iTempDistance = 0;

			for (int i = 0; i < iVSpanCount; i++) {
				iTempDistance += iVSpace;
				if (iTempDistance > y) {
					// 在间隔中
					dragObjectInfo.isInvalid = false;
					dragObjectInfo.isInCell = false;
					return false;
				}

				iTempDistance += iCellWidth;
				if (iTempDistance >= y) {
					// 找到了
					dragObjectInfo.isInvalid = true;
					dragObjectInfo.isInCell = true;
					dragObjectInfo.cellYPress = cellVLocRefParent;
					break;
				}

				cellVLocRefParent++;
			} // end for

			// Utils.log(TAG, "开始映射");

			
			 * 映射到CellLayout中去 如果有不能完全映射的情况，则不绘制
			 
			int iOffLeftArea = cellHLocRefParent - cellHLocRefSelf;
			int iOffTopArea = cellVLocRefParent - cellVLocRefSelf;
			int iHTemp1 = cellInfo.getCellHSpan() - 1 - cellHLocRefSelf;
			int iVTemp1 = cellInfo.getCellVSpan() - 1 - cellVLocRefSelf;
			int iOffRightArea = cellHLocRefParent + iHTemp1;
			int iOffBottomArea = cellVLocRefParent + iVTemp1;

			// 超出边界
			if (iOffLeftArea < 0 || iOffTopArea < 0
					|| iOffRightArea >= iHSpanNum
					|| iOffBottomArea >= iVSpanNum) {
				Utils.log(TAG, "超出边界");
				dragObjectInfo.isInvalid = false;
				return false;
			}

			dragObjectInfo.itemView = cellInfo.getView();
			dragObjectInfo.dragView = dragView;

			// 可以完全映射
			dragObjectInfo.cellX = iOffLeftArea;
			dragObjectInfo.cellY = iOffTopArea;
			dragObjectInfo.cellHSpan = cellInfo.getCellHSpan();
			dragObjectInfo.cellVSpan = cellInfo.getCellVSpan();

			
			boolean r1 = flagCellsOcupied(dragObjectInfo);

			if (r1) {
				cellInfo.setCellX(dragObjectInfo.cellX);
				cellInfo.setCellY(dragObjectInfo.cellY);
			}
			
			adjustCoors(dragObjectInfo, cellInfo);

			// invalidate(); //postInvalidate();
			m_Launcher.getDragLayer().updateDragPreEffect(dragObjectInfo);

			return r1;
		} // 有效的坐标区域，end if

		return false;
	}
	
	
	*/
	
	/**
	 * 计算相对于CellLayout的坐标值和宽度
	 * @param info
	 */
	private void adjustCoors(final DragObjectInfo info,  final Object dragInfo) {
		if (!(dragInfo instanceof CellInfo) ) {
			throw new IllegalArgumentException("参数类型错误，传入的参数不是CellInfo类型");
		}
		final CellInfo cellInfo = (CellInfo) dragInfo;
		final CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cellInfo.getView().getLayoutParams();
		final int cellX = info.cellX;
		final int cellY = info.cellY;
		final DropTarget dropTarget = m_Launcher.getWorkspace();
		final Rect outRect = m_RectTemp;
		
		m_Launcher.getWorkspace().getHitRectRefDragLayer(outRect, dropTarget); 
		
		int left = (cellX + 1) * m_iSpaceHorizatation + cellX * m_iCellSize + lp.leftMargin + getPaddingLeft() + outRect.left;
		int top = (cellY + 1) * m_iSpaceVertical + cellY * m_iCellSize + lp.topMargin + getPaddingTop() + outRect.top;
		int width = cellInfo.getView().getMeasuredWidth();
		int height = cellInfo.getView().getMeasuredHeight();
		
		info.curX = info.x = left;
		info.curY = info.y = top;
		info.width = width;
		info.height = height;
		
	}
	
	/**
	 * 标记哪些单元格被占用，并返回是否全部都没有占用，即是否可以放置
	 * @param info
	 * @return true：全部都没有被占用
	 */
	private boolean flagCellsOcupied(final DragObjectInfo info) {
		final int cellX  = info.cellX;
		final int cellY = info.cellY;
		final int cellHSpan = info.cellHSpan;
		final int cellVSpan = info.cellVSpan;
		final List<int[]> list = info.flagOcupiedList;
		final boolean[][] bArrayOcupied = m_bCellsOcupied;
		
		boolean r = true;
		
		info.canDrop = true;
		
		for (int i=cellY; i<cellY+cellVSpan; i++) {
			for (int j=cellX; j<cellX+cellHSpan; j++) {
				if (bArrayOcupied[i][j] ) {
					int[] item = new int[2];
					item[0] = i;
					item[1] = j;
					list.add(item);
					
					Utils.log(true, TAG, "单元格占用(%d, %d)", i, j);
					info.canDrop = false;
					r = false;
				}
				
			}
			
		}
		
//		debugBooleanArray();
//		Utils.log(true, TAG, "%s", info.toString());
		
		return r;
	}
	
	//TODO fix it
	public void setLauncher(MainActivity launcher) {
		m_Launcher = launcher;
	}
	
	/**
	 * 返回格子的大小
	 * @return
	 */
	public int getCellSize() {
		return m_iCellSize;
	}
	
	/**
	 * 返回格子水平的间隔大小
	 * @return
	 */
	public int getHorizontalSpace() {
		return m_iSpaceHorizatation;
	}
	
	/**
	 * 返回格子垂直的间隔大小
	 * @return
	 */
	public int getVerticalSpace() {
		return m_iSpaceVertical;
	}
	
	/** 返回所有的子view */
	public View[][] getAllChildrenView() {
		return m_ViewChildren;
	}
	
	private void debugBooleanArray() {
		final boolean[][] bArray = m_bCellsOcupied;
		final int iH = m_iCellHCount;
		final int iV = m_iCellVCount;
		
		StringBuilder sb = new StringBuilder();
		
		for (int i=0; i<iV; i++) {
			for (int j=0; j<iH; j++) {
				sb.append(String.valueOf(bArray[i][j]) + "  " );
			}
			sb.append("\n");
		}
		
		Utils.log(TAG+"ocupid", sb.toString());
	}
	
	/**
	 * 当释放拖动的View时，保存的一些绘制信息
	 * (1) DragView平滑回到原来位置的信息
	 * (2)
	 * (3)
	 * @author baoxing
	 *
	 */
	static class DropObjectInfo {
		/** 释放时的屏幕坐标值 */
		public int finalX, finalY;
		
		/** 放置Item View 的屏幕坐标值，更扩展的正确说法是相对于DragLayer */
		public int originX, originY;
		
		/** 动画过程中的坐标值top, left */
		public int curX, curY;
		
		/** 拖动的View */
		public DragView dragView;
		
		/**Item View */
		public View itemView;
		
		/** 格子位置*/
		public int cellX, cellY;
		
		/** 格子大小 */
		public int cellHSpan, cellVSpan;

		/** 是否需要绘制 */
		public boolean isInvalid;
		
		/** 动画效果是否完成 */
		public boolean isAnimFinished;
		
		/** 是否可以放置 */
		public boolean canDrop;
		
		public void init() {
			isInvalid = true;
			isAnimFinished = false;
		}
		
		public void animEnd() {
			isInvalid = false;
			isAnimFinished = true;
		}
		
		public void reset() {
			isInvalid = false;
			isAnimFinished = false;
		}
		
		@Override
		public String toString() {
			return "DropObjectInfo [finalX=" + finalX + ", finalY=" + finalY
					+ ", originX=" + originX + ", originY=" + originY
					+ ", dragView=" + dragView + ", itemView=" + itemView
					+ ", cellX=" + cellX + ", cellY=" + cellY + ", cellHSpan="
					+ cellHSpan + ", cellVSpan=" + cellVSpan + "]";
		}
		
		
	}
	
	/**
	 * 当拖动一块View到CellLayout时，保存相关的信息用于绘制辅助效果
	 * @author baoxing
	 *
	 */
	static class DragObjectInfo implements Cloneable {
		
		/** 是否重绘制，记得结束后置为reset */
		public boolean isInvalid;
		
		/** 是否在单元格中，false则是在间隔中 */
		public boolean isInCell;
		
		/** 是否可以放置到该位置 */
		public volatile boolean canDrop;
		
		/** 最初的位置 */
		public int preCellX, preCellY;
		
		/** 映射到CellLayout中的信息 */
		public int cellX, cellY, cellHSpan, cellVSpan;
		
		/**获取按下的坐标映射到CellLayout中的cellX和cellY */
		public int cellXPress, cellYPress;
		
		/** 相对于CellLayout的左上角坐标和大小 */
		public int x, y, width, height;
		
		/** 跟随动画时的当前值 */
		public int curX, curY;
		
		/** 记录那些单元格被占用了, int[0]=cellX, int[1]=cellY */
		public List<int[]> flagOcupiedList = new ArrayList<int[]>();
		
		/** item view */
		public View itemView;
		
		/** drag view */
		public View dragView;
		
		/** 保存跟随动画的每一个格子 */
		private Queue<DragObjectInfo> followDragObjectInfoQueue = new LinkedList<CellLayout.DragObjectInfo>();
		/** 队列中最后一个元素 */
		private DragObjectInfo lastInQueue;
//		public List<DragObjectInfo> followDragObjectInfoList = new ArrayList<CellLayout.DragObjectInfo>();
		
		public void reset() {
			isInvalid = false;
			isInCell = false;
			flagOcupiedList.clear();
		}
		
		public void initAnim() {
			this.isInvalid = true;
			this.isInCell = true;
		}
		
		public void updateIsValid() {
			isInvalid = false;
		}

		
		public void clearFollowDragObjectQueue() {
			this.followDragObjectInfoQueue.clear();
			this.lastInQueue = null;
		}
		
		public int getFollowQueueSize() {
			return this.followDragObjectInfoQueue.size();
		}
		
		/**
		 * 入队，跟随的格子
		 * @param object
		 */
		public void offerDragObjectInQueue(DragObjectInfo object) {
			Utils.log(TAG+2, "offer element");
			
			if (lastInQueue == null) {
				lastInQueue = object;
				this.followDragObjectInfoQueue.offer(object);
				
			} else {
				/*
				 * 判断是否已经加入过同一个格子
				 */
				boolean bIsTheCell = lastInQueue.equalTheSameCell(object);
				if (!bIsTheCell) {
					this.followDragObjectInfoQueue.offer(object);
					this.lastInQueue = object;
//					this.lastInQueue = (DragObjectInfo) object.clone();
				}
				
			}
			
		}
		
		/**
		 * 出队
		 * @return 如果队列为空，返回null
		 */
		public DragObjectInfo pollFollowDragObjectInfoQueue() {
			return this.followDragObjectInfoQueue.poll();
		}
		
		public Queue<DragObjectInfo> getFollowQueue() {
			return this.followDragObjectInfoQueue;
		}
		
		public void debugFollowDragObjectQueue() {
			String tag = TAG + "2";
			Utils.log(tag, "===========================================");
			Utils.log(tag, "size=%d", this.followDragObjectInfoQueue.size() );
			for (DragObjectInfo item : this.followDragObjectInfoQueue) {
				Utils.log(tag, "cellX=%d, cellY=%d, cellHSpan=%d, cellVSpan=%d",
						item.cellX, item.cellY, item.cellHSpan, item.cellVSpan);
			}
			Utils.log(tag, "===========================================");
		}
		
		
		
		/**
		 * 判断Drag对象是否为同一个格子
		 * @param object
		 * @return
		 */
		public boolean equalTheSameCell(DragObjectInfo object) {
			return (this.cellX==object.cellX) && (this.cellY==object.cellY)
						&& (this.cellHSpan==object.cellHSpan) && (this.cellVSpan==object.cellVSpan);
			
		}
		
		/**
		 * 判断是否有移动到另一个格子，用pressCellX和pressCellY来判断，用于swap item 回退
		 * {@link #equalTheSameCell(DragObjectInfo)} 是用来设计跟随的，不适用
		 * 
		 * @param object
		 * @return true：移动到另一格子
		 */
		public boolean adjustMoveAnotherCell(final DragObjectInfo object) {
			boolean r = (this.cellXPress == object.cellXPress)
					&& (this.cellYPress == object.cellYPress);

			return !r;
		}
		
		
		@Override
		protected Object clone() {
			DragObjectInfo cloned = new DragObjectInfo();
			try {
				cloned = (DragObjectInfo) super.clone();
				cloned.flagOcupiedList = null;
				cloned.followDragObjectInfoQueue = null;
				cloned.lastInQueue = null;
				
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			
			return cloned;
		}
		
		
		@Override
		public String toString() {
			return "DragObjectInfo [isInvalid=" + isInvalid + ", isInCell="
					+ isInCell + ", canDrop=" + canDrop + ", cellX=" + cellX
					+ ", cellY=" + cellY + ", cellHSpan=" + cellHSpan
					+ ", cellVSpan=" + cellVSpan + ", cellXPress=" + cellXPress
					+ ", cellYPress=" + cellYPress + ", x=" + x + ", y=" + y
					+ ", width=" + width + ", height=" + height
					+ ", flagOcupiedList=[" + getflagOcupiedListStr() + "]";
		}
		
		public String getflagOcupiedListStr() {
			StringBuilder sb = new StringBuilder();
			for (int[] item : flagOcupiedList) {
				int x = item[0];
				int y = item[1];
				sb.append(String.format("[%d,%d]", x, y));
			}
			
			return sb.toString();
		}
		
	}
	
	
	/**
	 * 自定义的LayoutParams属性，继承ViewGroup.MarginLayoutParams，所以可以
	 * 获得外边距margins，暂时不用到该属性
	 * @author baoxing
	 *
	 */
	public static class LayoutParams extends ViewGroup.MarginLayoutParams {
		
		/** top-left坐标的X轴起点 */
		public int cellX;
		
		/** top-left坐标的Y轴起点 */
		public int cellY;
		
		/** 水平方向占了多少格子数 */
		public int cellHSpan;
		
		/** 垂直方向占了多少格子数 */
		public int cellVSpan;
		

		public LayoutParams(Context context, AttributeSet attrs) {
			super(context, attrs);
			TypedArray ta = null;
			try {
				ta = context.obtainStyledAttributes(attrs, R.styleable.Launcher);
				cellX = ta.getInt(R.styleable.Launcher_cellX, 0);
				cellY = ta.getInt(R.styleable.Launcher_cellY, 0);
				cellHSpan = ta.getInt(R.styleable.Launcher_cellHSpan, 1);
				cellVSpan = ta.getInt(R.styleable.Launcher_cellVSpan, 1);
				
				
			} finally {
				if (ta != null) {
					ta.recycle();
				}
				
			}
			
		}

		public LayoutParams(int width, int height) {
			super(width, height);
			cellHSpan = 1;
			cellVSpan = 1;
		}

		public LayoutParams(MarginLayoutParams params) {
			super(params);
			if (params instanceof LayoutParams) {
				LayoutParams lp = (LayoutParams) params;
				this.cellX = lp.cellX;
				this.cellY = lp.cellY;
				this.cellHSpan = lp.cellHSpan;
				this.cellVSpan = lp.cellVSpan;
			}
		}
		
		public LayoutParams() {
			this(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
			cellHSpan = 1;
			cellVSpan = 1;
		}

		public LayoutParams(android.view.ViewGroup.LayoutParams params) {
			super(params);
			cellHSpan = 1;
			cellVSpan = 1;
		}
		
		
	}

	@Override
	public android.view.ViewGroup.LayoutParams generateLayoutParams(
			AttributeSet attrs) {
		return new CellLayout.LayoutParams(getContext(), attrs);
	}
	
	@Override
	protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
		return p instanceof CellLayout.LayoutParams;
	}
	
	@Override
	protected android.view.ViewGroup.LayoutParams generateLayoutParams(
			android.view.ViewGroup.LayoutParams p) {
		return new CellLayout.LayoutParams(p);
	}
	
	@Override
	protected android.view.ViewGroup.LayoutParams generateDefaultLayoutParams() {
		return new CellLayout.LayoutParams();
	}
	
	

}
