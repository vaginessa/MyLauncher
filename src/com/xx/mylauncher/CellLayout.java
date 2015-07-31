package com.xx.mylauncher;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;

/**
 * 每一屏幕的布局，是一个可以容纳不同大小View的容器
 * <p>现在实现的是，可以在@{link CellLayout}中指定水平间隔和垂直间隔</p>
 * <p>指定单个格子的大小，自动根据算得到的CellLayout的宽和高计算水平格子数和垂直格子数</p>
 * @author baoxing
 * 
 */
public class CellLayout extends ViewGroup {
	
	/** 格子默认的大小 50dp */
	private  int Cell_Size_Default;
	private static final int CELL_SIZE_DEFAULT = 50;
	
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
	private boolean m_bCellsOcupied[][];
	
	/**Launcher 引用 *///TODO要修改
	private MainActivity m_Launcher;
	
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
			iParentWidth = iWidthSize;
		} else if (iWidthMode == MeasureSpec.AT_MOST) {
			iParentWidth = m_iScreenWidth;
		} else {
			iParentWidth = m_iScreenWidth;
		}
		
		if (iHeightMode == MeasureSpec.EXACTLY) {
			iParentHeight = iHeightSize;
		} else if (iHeightMode == MeasureSpec.AT_MOST) {
			iParentHeight = m_iScreenHeight / 8 * 7;
			
		} else {
			iParentHeight = m_iScreenHeight / 8 * 7;
			
		}
		
		//这里先只考虑平分格子排序，所以设置的水平和垂直格子间隔先不考虑
		m_iCellHCount = iCellHCount = (iParentWidth - getPaddingLeft() - getPaddingRight() ) / (m_iCellSize + m_iSpaceHorizatation); 
		m_iCellVCount = iCellVCount = (iParentHeight - getPaddingTop() - getPaddingBottom() ) / (m_iCellSize + m_iSpaceVertical);
		
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
			
//			Utils.log(TAG, "cellX=%d, cellY=%d, cellHSpan=%d, cellVSpan=%d", cellX, cellY, cellHSpan, cellVSpan);
			for (int ti=cellY; ti<=cellY+cellVSpan-1; ti++) {
				for (int tj=cellX; tj<=cellX+cellHSpan-1; tj++) {
					m_bCellsOcupied[ti][tj] = true;
				}
			}
		}
		
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
	
	public void flagOcupied(int cellX, int cellY, int cellHSpan, int cellVSpan) {
		for (int ti=cellY; ti<=cellY+cellVSpan-1; ti++) {
			for (int tj=cellX; tj<=cellX+cellHSpan-1; tj++) {
				m_bCellsOcupied[ti][tj] = true;
				
//				debugBooleanArray();
			}
		}
	}
	
   /**
    * @see DragSource#onDropCompleted(View, View, Object, int, int, int, int, boolean) 
    */
	public void onDropCompleted(View dropTargetView, View dragView, Object itemInfo, int rawX, int rawY, int iOffX, int iOffy, boolean success) {
		
			/*
			 * 使DragView平滑移动到原来的位置或新的位置，Item View设置可见，draglayer-invalid
			 */
		final DropObjectInfo info = new DropObjectInfo();
		final CellInfo cellInfo = (CellInfo) itemInfo;
		final DragObjectInfo dragInfo = m_DragObjectInfo;
		final int[] iArrayTempOff = {0, 0};
		
		adjustToDragLayer(iArrayTempOff, dragView, getContext(), true);
		info.finalX = iArrayTempOff[0];
		info.finalY = iArrayTempOff[1];
		
		info.dragView = (DragView) dragView;
		info.itemView = cellInfo.getView();
		
		adjustToDragLayer(iArrayTempOff, cellInfo.getView(), getContext(), true);
		
		if (!success) {
			//移动回原来的位置
			info.originX = iArrayTempOff[0];
			info.originY = iArrayTempOff[1];
			info.cellX = cellInfo.getCellX();
			info.cellY = cellInfo.getCellY();
			info.cellHSpan = cellInfo.getCellHSpan();
			info.cellVSpan = cellInfo.getCellVSpan();
			info.canDrop = false;
		} else {
			//移动到新的位置
			info.canDrop = true;
			info.originX = dragInfo.x;	//TODO relative to draglayer take a cause
			info.originY = dragInfo.y;
			info.cellX = dragInfo.cellX;
			info.cellY = dragInfo.cellY;
			info.cellHSpan = dragInfo.cellHSpan;
			info.cellVSpan = dragInfo.cellVSpan;
		}
		info.init();
		
		Utils.log(TAG, "offset draglayer-offLeft=%d, offTop=%d", iArrayTempOff[0], iArrayTempOff[1] );
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
	private void adjustToDragLayer(int[] coordnates, View view, Context context, boolean subStatus) {
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
			coordnates[1] += iStatusHeight;
		}
	}
	
	/**
	 * 可以放置时调用的函数
	 * @see com.xx.mylauncher.DropTarget#onDrop(com.xx.mylauncher.DragSource, int, int, int, int, com.xx.mylauncher.DragView, java.lang.Object)
	 */
	public void onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		
		if (dragInfo instanceof CellInfo) {
			final CellInfo cellInfo = (CellInfo) dragInfo;
			final View itemView = cellInfo.getView();
			final CellLayout.LayoutParams lp = (LayoutParams) itemView.getLayoutParams();
			if (m_DragObjectInfo != null) {
				lp.cellX = m_DragObjectInfo.cellX;
				lp.cellY = m_DragObjectInfo.cellY;
				lp.cellHSpan = m_DragObjectInfo.cellHSpan;
				lp.cellVSpan = m_DragObjectInfo.cellVSpan;
				itemView.setVisibility(View.VISIBLE);
				
				//设置占用的格子
				for (int ti=lp.cellY; ti<=lp.cellY+lp.cellVSpan-1; ti++) {
					for (int tj=lp.cellX; tj<=lp.cellX+lp.cellHSpan-1; tj++) {
						m_bCellsOcupied[ti][tj] = true;
					}
				}
				
				requestLayout();
			}
		}
	}
	
	public void onDragOver(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		acceptDrop(source, x, y, xOffset, yOffset, dragView, dragInfo);
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
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		
		final int iCellWidth = m_iCellSize;
		final int iHSpace = m_iSpaceHorizatation;
		final int iVSpace = m_iSpaceVertical;
		final int iHSpanCount = m_iCellHCount + (iHSpace / iCellWidth) + 1;	//考虑间隔比格子大的情况
		final int iVSpanCount = m_iCellVCount + (iVSpace / iCellWidth) + 1;
		final int iHSpanNum = m_iCellHCount;
		final int iVSpanNum = m_iCellVCount;
		
		final DragObjectInfo dragObjectInfo = m_DragObjectInfo;
		m_DragObjectInfo.reset();
		
		if (! (dragInfo instanceof CellInfo)) {
			Utils.log(TAG, "fuckdddd");
		}
		
		//TODO
		//注意DragView是拖动的View，不是原来的Item View，因为可能会改动DragView来满足效果
		if (dragInfo instanceof CellInfo) {
			CellInfo cellInfo = (CellInfo) dragInfo;
			int iItemViewWidth = cellInfo.getCellHSpan() * iCellWidth + (cellInfo.getCellHSpan()-1) * iHSpace;
			int iItemViewHeight = cellInfo.getCellVSpan() * iCellWidth + (cellInfo.getCellVSpan()-1) * iVSpace;
			
			if (! ((xOffset>=0 && xOffset<=iItemViewWidth) && (yOffset>=0 && yOffset<=iItemViewHeight) ) ) {
				Utils.log(TAG, "超出边界.xOffset=%d, yOffset=%d, iItemViewWidth=%d, iItemViewHeight=%d", xOffset, yOffset, iItemViewWidth, iItemViewHeight );
			}
			
			if ((xOffset>=0 && xOffset<=iItemViewWidth) && (yOffset>=0 && yOffset<=iItemViewHeight) ) {
//				Utils.log(TAG, "满足边界条件");
				/*
				 * 相对于自身的格子级偏移量
				 */
				int cellHLocRefSelf = xOffset / iCellWidth;	//这里如果间隔比格子大，则计算错误	//TODO
				int cellVLocRefSelf = yOffset / iCellWidth;
				
				/*
				 * 相对于CellLayout的格子级偏移量
				 * 这里和怎么放置子View的逻辑密切相关，如果设置padding/margin属性的话
				 */
				x += getPaddingLeft();
				y += getPaddingTop();
				
				int cellHLocRefParent = 0;
				int cellVLocRefParent = 0;
				
				int iTempDistance = 0;
				
				for (int i=0; i<iHSpanCount; i++ ) {
					iTempDistance += iHSpace;
					if (iTempDistance > x) {
						//在间隔中
						dragObjectInfo.isInvalid = false;
						dragObjectInfo.isInCell = false;
						return false;
					}
					
					iTempDistance += iCellWidth;
					if (iTempDistance >= x) {
						//找到了
						dragObjectInfo.isInvalid = true;
						dragObjectInfo.isInCell = true;
						dragObjectInfo.cellXPress = cellHLocRefParent;
						break;
					}
					cellHLocRefParent++;
				} //end for
				
				iTempDistance = 0;
				
				for (int i=0; i<iVSpanCount; i++) {
					iTempDistance += iVSpace;
					if (iTempDistance > y) {
						//在间隔中
						dragObjectInfo.isInvalid = false;
						dragObjectInfo.isInCell = false;
						return false;
					}
					
					iTempDistance += iCellWidth;
					if (iTempDistance >= y) {
						//找到了
						dragObjectInfo.isInvalid = true;
						dragObjectInfo.isInCell = true;
						dragObjectInfo.cellYPress = cellVLocRefParent;
						break;
					} 
					
					cellVLocRefParent++;
				}	//end for
				
//				Utils.log(TAG, "开始映射");
				
				/*
				 * 映射到CellLayout中去
				 * 如果有不能完全映射的情况，则不绘制
				 */
				int iOffLeftArea = cellHLocRefParent - cellHLocRefSelf;
				int iOffTopArea = cellVLocRefParent - cellVLocRefSelf;
				int iHTemp1 = cellInfo.getCellHSpan() - 1 - cellHLocRefSelf;
				int iVTemp1 = cellInfo.getCellVSpan() - 1- cellVLocRefSelf;
				int iOffRightArea = cellHLocRefParent + iHTemp1;
				int iOffBottomArea = cellVLocRefParent + iVTemp1;
				
				//超出边界
				if (iOffLeftArea<0 || iOffTopArea<0 || iOffRightArea>=iHSpanNum || iOffBottomArea>=iVSpanNum ) {
					Utils.log(TAG, "超出边界");
					dragObjectInfo.isInvalid = false;
					return false;
				}
				
				dragObjectInfo.itemView = cellInfo.getView();
				
				//可以完全映射
				dragObjectInfo.cellX = iOffLeftArea;
				dragObjectInfo.cellY = iOffTopArea;
				dragObjectInfo.cellHSpan = cellInfo.getCellHSpan();
				dragObjectInfo.cellVSpan = cellInfo.getCellVSpan();
				
				boolean r1 = flagCellsOcupied(dragObjectInfo);
				
				adjustCoors(dragObjectInfo, cellInfo);
				
//				invalidate();	//postInvalidate();
				m_Launcher.getDragLayer().updateDragPreEffect(dragObjectInfo);
				
				return r1;
			}	//有效的坐标区域，end if 
			
		}
		
		
		return false;
	}
	
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
		
		int left = (cellX + 1) * m_iSpaceHorizatation + cellX * m_iCellSize + lp.leftMargin + getPaddingLeft();
		int top = (cellY + 1) * m_iSpaceVertical + cellY * m_iCellSize + lp.topMargin + getPaddingTop();
		int width = cellInfo.getView().getMeasuredWidth();
		int height = cellInfo.getView().getMeasuredHeight();
		
		info.x = left;
		info.y = top;
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
		
		Utils.log(TAG, sb.toString());
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
		
		/** 原来Item View 的屏幕坐标值，更扩展的正确说法是相对于DragLayer */
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
	static class DragObjectInfo {
		
		/** 是否重绘制，记得结束后置为reset */
		public boolean isInvalid;
		
		/** 是否在单元格中，false则是在间隔中 */
		public boolean isInCell;
		
		/** 是否可以放置到该位置 */
		public boolean canDrop;
		
		/** 映射到CellLayout中的信息 */
		public int cellX, cellY, cellHSpan, cellVSpan;
		
		/**获取按下的坐标映射到CellLayout中的cellX和cellY */
		public int cellXPress, cellYPress;
		
		/** 相对于CellLayout的左上角坐标和大小 */
		public int x, y, width, height;
		
		/** 记录那些单元格被占用了, int[0]=cellX, int[1]=cellY */
		public List<int[]> flagOcupiedList = new ArrayList<int[]>();
		
		/** item view */
		public View itemView;
		
		public void reset() {
			isInvalid = false;
			canDrop = false;
			isInCell = false;
			flagOcupiedList.clear();
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
		
		private String getflagOcupiedListStr() {
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
