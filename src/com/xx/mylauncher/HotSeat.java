package com.xx.mylauncher;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;

import com.xx.mylauncher.CellLayout.DragObjectInfo;
import com.xx.mylauncher.CellLayout.DropObjectInfo;

/**
 * HotSeat，屏幕中固定的部分
 * 有五个cell格子
 * 五个 item view {@link ShortCutView2}，隐藏 label，平分控件宽度，居中显示
 * 中间的固定
 * @author baoxing
 *
 */
public class HotSeat extends ViewGroup implements DropTarget, DragSource {

	private static final String TAG = "HotSeat";

	private boolean m_bIsDropEnable = true;
	
	private static final float HEIGHT_SCALE = 1/6f;
	
	private int m_iScreenWidth;
	
	/** 控件宽度 */
	private int m_iHotSeatWidth;
	
	/** 控件高度 */
	private int m_iHotSeatHeight;
	
	/** 格子的宽度 */
	private int m_iCellWidth;
	
	/** 格子的高度 */
	private int m_iCellHeight;
	
	/** 高度的内偏移补偿量 */
	private int m_iHeightOffsetTop = 0;
	
	private int m_iHeightOffsetBottom = 0;
	
	/** 单元格数量 */
	private static final int CELL_COUNTS = 5;
	
	/** 格子间的间隔 */
	private int m_iSpace;
	
	/** HotSeat的两边的间隔 */
	private int m_iEdgeSpace;
	
	/** default cell space */
	private static final int DIMEN_SPACE = 5; 	//dp
	
	private static final int DIMEN_EDGE_SPACE = 10;	//dp
	
	private static final int DIMEN_HOTSEAT_HEIGHT = Constant.DIMEN_DEFAULT_HOTSEAT_HEIGHT;
	
	/** 指示那个格子固定 */
	private static final int FIXED_NUM = 2;
	
	/** 哪种方式计算格子大小 */
	private int m_iTypeCaculator;
	private static final int TYPE_VERTICAL = 1;
	private static final int TYPE_HORIZONTAL = 2;
	
	/** 摆放HotSeat内容的左边偏移量 */
	private int m_iOffsetLeft;
	
	/** 用来标志是否占用，true：占用 */
	private boolean m_bOccupied[] = new boolean[CELL_COUNTS];
	
	private Rect m_TempRect = new Rect();
	
	/** 拖拽层 */
	private DragLayer m_DragLayer;
	
	private MainActivity m_Launcher;
	
	private int[] m_iArrayTempCoor = new int[2];
	
	
	public HotSeat(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HotSeat(Context context) {
		this(context, null);
	}
	
	public HotSeat(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initRes(context, attrs);
	}
	/**
	 * 初始化资源
	 * @param context
	 */
	private void initRes(Context context, AttributeSet set) {
		Utils.log(TAG, "initRes");
		/*
		 * 获取资源
		 */
		final int iDimenSpace = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DIMEN_SPACE, context.getResources().getDisplayMetrics());
		final int iDimenEdgeSpace = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DIMEN_EDGE_SPACE, context.getResources().getDisplayMetrics());
		final int iDimenHotseatHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DIMEN_HOTSEAT_HEIGHT, context.getResources().getDisplayMetrics());
		
		TypedArray ta = context.obtainStyledAttributes(set, R.styleable.HotSeat);
		m_iHeightOffsetTop = (int) ta.getDimension(R.styleable.HotSeat_hotseat_offset_top, 0);
		m_iHeightOffsetBottom = (int) ta.getDimension(R.styleable.HotSeat_hotseat_offset_bottom, 0);
		m_iSpace = (int) ta.getDimension(R.styleable.HotSeat_hotseat_space, iDimenSpace);
		m_iEdgeSpace = (int) ta.getDimension(R.styleable.HotSeat_hotseat_edge_space, iDimenEdgeSpace);
		m_iHotSeatHeight = (int) ta.getDimension(R.styleable.HotSeat_hotseat_height, iDimenHotseatHeight);
		
		ta.recycle();
		
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		m_iScreenWidth = outMetrics.widthPixels;
		
		for (int i=0; i<CELL_COUNTS; i++) {
			m_bOccupied[i] = false;
		}
		
		
		loadAllAppView();
	}
	
	/**
	 * 添加All App ICON
	 */
	private void loadAllAppView() {
		Utils.log(TAG, "loadAllAppView");
		
		post(new Runnable() {
			
			@Override
			public void run() {
				Utils.log(TAG, "loadAllAppView run()");
				ShortCutView2 item = new ShortCutView2(getContext());
				Drawable pressDrawable = getContext().getResources().getDrawable(R.drawable.ic_allapps_pressed);					
				Drawable normalDrawable = getContext().getResources().getDrawable(R.drawable.ic_allapps);
				StateListDrawable stateListDrawable = Utils.getStateListDrawable(getContext(), normalDrawable, pressDrawable);
				item.setIconBackground(stateListDrawable);
				item.setLabelVisibility(View.GONE);
//				item.setIcon(normalDrawable);
				item.setClickable(true);
				CellInfo cellInfo = new CellInfo();
				cellInfo.setHotSeatCellX(FIXED_NUM);
				cellInfo.setHotSeatCellY(0);
				cellInfo.setView(item);
				item.setTag(cellInfo);
				m_bOccupied[FIXED_NUM] = true;
				
				addView(item);
			/*	//test
				addView(getView(0));
				addView(getView(1));
				addView(getView(3));
				addView(getView(4));*/
				
				requestLayout();
			}
		});
	}

	private View getView(int x) {
		ShortCutView2 item = new ShortCutView2(getContext());
		Drawable pressDrawable = getContext().getResources().getDrawable(R.drawable.ic_allapps_pressed);					
		Drawable normalDrawable = getContext().getResources().getDrawable(R.drawable.ic_allapps);
		StateListDrawable stateListDrawable = Utils.getStateListDrawable(getContext(), normalDrawable, pressDrawable);
		item.setIconBackground(stateListDrawable);
		item.setLabelVisibility(View.GONE);
		item.setIcon(normalDrawable);
		item.setClickable(true);
		item.setBackgroundColor(Color.RED);	//TODO
		CellInfo cellInfo = new CellInfo();
		cellInfo.setHotSeatCellX(x);
		cellInfo.setHotSeatCellY(0);
		cellInfo.setView(item);
		item.setTag(cellInfo);
		
		return item;
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int iChildCount = getChildCount();
		if (iChildCount<=0) {
			Utils.log(TAG, "onLayout-no childs");
			return;
		}
		
		Utils.log(TAG, "onLayout, child counts=%d", iChildCount);
		
		final int iViewWidth = m_iHotSeatWidth;
		final int iHeightOffsetTop = m_iHeightOffsetTop;
		final int iCellWidth = m_iCellWidth;
		final int iCellHeight = m_iCellHeight;
		final int iCellSpace = m_iSpace;
		final int iOffsetLeft = m_iOffsetLeft;
		
		int left = 0;
		int top = 0;
		int right;
		int bottom;
		
		View child;
		CellInfo cellInfo;
		
		for (int i = 0; i < iChildCount; i++) {
			child = getChildAt(i);
			Object oTemp = child.getTag();
			
			cellInfo = (CellInfo) oTemp;
			int cellX = cellInfo.getHotSeatCellX();
			int cellY = cellInfo.getHotSeatCellY(); // it must be 0
			cellY = 0;		

			left = cellX * iCellWidth + cellX * iCellSpace + iOffsetLeft;
			top = cellY * iCellHeight + cellY * iCellSpace + iHeightOffsetTop;
			right = left + child.getMeasuredWidth();
			bottom = top + child.getMeasuredHeight();

			child.layout(left, top, right, bottom);
			Utils.log(TAG, "iCellWidth=%d", iCellWidth);
			Utils.log(TAG, "[%d, %d]-[%d, %d], iCellWidth=%d, iCellHeight=%d, iOffsetLeft=%d",
					left, top, right, bottom, iCellWidth, iCellHeight, iOffsetLeft);
		}
		
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		/*
		 * 测量自己的大小
		 */
		final int iParentWidthAction = MeasureSpec.getMode(widthMeasureSpec);
		final int iParentWidthSize = MeasureSpec.getSize(widthMeasureSpec);
		final int iParentHeightAction = MeasureSpec.getMode(heightMeasureSpec);
		final int iParentheightSize = MeasureSpec.getSize(heightMeasureSpec);
		final int iScreenWidth = m_iScreenWidth;
		
		int iSelfWidth;
		int iSelfHeight;
		
		if ( (iParentWidthAction == MeasureSpec.AT_MOST) || (iParentWidthSize==MeasureSpec.UNSPECIFIED) ) {
			Utils.log(TAG, "onMeausre-width-at_most||unspecified");
			iSelfWidth = iScreenWidth;
			
		} else {
			Utils.log(TAG, "onMeasure-width-excexty");
			iSelfWidth = iParentWidthSize;
		} 
		
		/*
		 * 这里有个地方很奇怪，当拖动时，HotSeat的高度会变得很小(8)。这是为什么？？
		 * 现在的做法是直接求得剩余的大小，设置进去
		 */
		iSelfHeight = m_iHotSeatHeight;
		
		setMeasuredDimension(iSelfWidth, iSelfHeight);
		
		final int iViewWidth = getMeasuredWidth();
		final int iViewHeight = getMeasuredHeight();
		
		Utils.log(TAG, "[onMeasure]控件的: width=%d, height=%d", iSelfWidth, iSelfHeight);
		
		m_iHotSeatWidth = iViewWidth;
		m_iHotSeatHeight = iViewHeight;
		m_iCellHeight = iViewHeight - m_iHeightOffsetBottom - m_iHeightOffsetTop;
		m_iCellWidth = m_iCellHeight;
		
		final int iChildCount = getChildCount();
		final int iCellSpace = m_iSpace;
		final int iEdgeSpace = m_iEdgeSpace;
		
		int iCellWidth = m_iCellWidth;
		int iCellHeight = m_iCellHeight;
		
		/*
		 * 以高为宽的话，计算总的内容；如果最后的结果大于总的宽度
		 * 则换另一种写法，已总的宽度为总量来计算单元格的宽度
		 */
		final int iContentWidth = CELL_COUNTS * iCellWidth + (CELL_COUNTS-1) * iCellSpace;
		m_iTypeCaculator = TYPE_VERTICAL;
		m_iOffsetLeft = (iViewWidth - iContentWidth) / 2;
		
		if (iContentWidth > iViewWidth) {
			//重新计算
			Utils.log(TAG, "重新计算");
			m_iTypeCaculator = TYPE_HORIZONTAL;
			m_iOffsetLeft = iEdgeSpace;
			
			int iTotalSpace = (CELL_COUNTS-1) * iCellSpace + iEdgeSpace * 2;
			iCellWidth = m_iCellWidth = (iViewWidth - iTotalSpace) / CELL_COUNTS;
			iCellHeight = m_iCellHeight = iCellWidth;
			m_iHeightOffsetTop = (iViewHeight-iCellHeight) / 2;
		}
		
		Utils.log(TAG, "onMeasure-iChildCount=%d, iCellWidth=%d, iCellHeight=%d", iChildCount, iCellWidth, iCellHeight);
		
		View child;
		int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(m_iCellWidth, MeasureSpec.EXACTLY);
		int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(m_iCellHeight, MeasureSpec.EXACTLY);
		
		for (int i=0; i<iChildCount; i++) {
			child = getChildAt(i);
			child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
		}
		
	}
	
	
	private int getSpecifyHeight() {
		int iStatusHegiht = Utils.getStatusHeight(getContext());
		int iScaleHeight = (int) (m_iScreenWidth * HEIGHT_SCALE);
		
		Utils.log(TAG, "iStautsHeight=%d, iScaleHeight=%d, iScreenWidth=%d, HEIGHT_SCLAE=%f", iStatusHegiht, iScaleHeight, m_iScreenWidth, HEIGHT_SCALE);
		return iScaleHeight + iStatusHegiht;
	}
	
	/*
	 * Setter / Getter
	 */
	public void setIsDropEnable(boolean isDropEnable) {
		this.m_bIsDropEnable = isDropEnable;
	}
	
	/**
	 * 当拖拽开始时，清除它所占的单元格标志位
	 * @param cellInfo
	 */
	public void clearFlagOcupid(CellInfo cellInfo) {
		m_bOccupied[cellInfo.getHotSeatCellX()] = false;
	}

	public void flagOcuped(CellInfo cellInfo) {
		m_bOccupied[cellInfo.getHotSeatCellX()] = true;
	}
	
	@Override
	public void setDragController(DragController dragger) {
		// TODO Auto-generated method stub
		
	}

	public void setLauncher(MainActivity launcher) {
		this.m_Launcher = launcher;
	}
	
	@Override
	public void setDragLayer(DragLayer dragLayer) {
		this.m_DragLayer = dragLayer;
	}

	/*
	 * 是DragSource的接口，注意理解
	 * (non-Javadoc)
	 * @see com.xx.mylauncher.DragSource#onDropCompleted(android.view.View, android.view.View, java.lang.Object, int, int, int, int, boolean)
	 */
	@Override
	public void onDropCompleted(View dropTargetView, View dragView,
			Object itemInfo, int rawX, int rawY, int iOffX, int iOffy,
			boolean success) {
		
		final boolean bIsDropInSelf = dropTargetView instanceof HotSeat;
		
		if (bIsDropInSelf) {
			onDropCompletedInSelf(dragView, itemInfo, success);
			
		} else if (dropTargetView instanceof Workspace) {
			if (!success) {
				onDropCompletedInSelf(dragView, itemInfo, success);
				
			} else {
				/*
				 * 交给dropTargetView的onDrop中处理滑动
				 */
			}
			
		} else if (dropTargetView instanceof DeleteZone) {
			if (success) {
				final CellInfo cellInfo = (CellInfo) itemInfo;
				
				m_DragLayer.removeView(dragView);
				removeView(cellInfo.getView() );
				
			} else {
				onDropCompletedInSelf(dragView, itemInfo, success);
				
			}
			
		} else {
			onDropCompletedInSelf(dragView, itemInfo, success);
		}
		
		if (success && !(dropTargetView instanceof DeleteZone) ) {
			final CellInfo cellInfo = (CellInfo) itemInfo;
			m_Launcher.getLauncherDBManager().updateDragInfo(cellInfo);
		}
		
		m_DragLayer.swapItemOnComplete(this, success);
	}
	
	/**
	 * 
	 * @param dragView
	 * @param itemInfo
	 * @param success
	 */
	private void onDropCompletedInSelf(View dragView, Object itemInfo, boolean success) {
		/*
		 * 使DragView滑回原来的位置或新的位置
		 */
		final DropObjectInfo info = new DropObjectInfo();
		final CellInfo cellInfo = (CellInfo) itemInfo;
		final DragObjectInfo dragInfo = m_CurDragObjectInfo;
		final int[] iArrayTempOff = {0, 0};
		final boolean[] bArrayOccupid = m_bOccupied;
		
		adjustToDragLayer(iArrayTempOff, dragView, getContext(), true);
		info.finalX = iArrayTempOff[0];
		info.finalY = iArrayTempOff[1];
		info.dragView = (DragView) dragView;
		info.itemView = cellInfo.getView();
		
		if (!success) {
			adjustToDragLayer(iArrayTempOff, cellInfo.getView(), getContext(), true);
			info.originX = iArrayTempOff[0];
			info.originY = iArrayTempOff[1];
			info.canDrop = false;
			
		} else {
			info.originX = dragInfo.x;
			info.originY = dragInfo.y;
			info.canDrop = true;
			
		}
		
		bArrayOccupid[cellInfo.getHotSeatCellX()] = true;
		info.init();
		
		m_DragLayer.updateDragViewToOriPoint(info);
	}

	@Override
	public void onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		Utils.log(TAG, "onDrop");
		
		final DragObjectInfo dragObjectInfo = m_CurDragObjectInfo;
		final boolean bIsFromSelf = source instanceof HotSeat; 
		final boolean[] bArrayOccupied = m_bOccupied;
		final CellInfo cellInfo = (CellInfo) dragInfo;
		final int iHotSeatCellY = 0;
		final int iHotSeatCellX = getCellXAccordingCoordinateRefParent(x, y);
		
		if (iHotSeatCellX == -1) {
			Utils.log(TAG, "这里是出错的，不过应该不会有这种情况发生");	//应该throw Exception
			throw new IllegalStateException("状态异常，当onDrop时表明可以接受，但是这时再计算时却出错.");
		}
		
		bArrayOccupied[iHotSeatCellX] = true;
		cellInfo.setHotSeatCellX(iHotSeatCellX);
		cellInfo.setHotSeatCellY(iHotSeatCellY);
		cellInfo.setLocation(CellInfo.CellLocation.HOTSEAT);
		
		if (bIsFromSelf) {
			/*
			 * 交给dragsource去接手滑动
			 */
			
		} else if (source instanceof Workspace) {
			/*
			 * 更改属性，原source remove child view，dropTaget add child view
			 * 处理occupied，接手滑动
			 */
			final ShortCutView2 itemView = (ShortCutView2) cellInfo.getView();
			final Workspace workspace= (Workspace) source;
			workspace.getCurCellLayout().removeView(itemView);
			itemView.setLabelVisibility(View.GONE);
//			itemView.setVisibility(View.INVISIBLE);
			addView(itemView);
			requestLayout();
			
			final DropObjectInfo dropSliceInfo = new DropObjectInfo();
			final int[] iArrayTemoOff = {0,0};
			
			dropSliceInfo.dragView = dragView;
			dropSliceInfo.itemView = cellInfo.getView();
			adjustToDragLayer(iArrayTemoOff, dragView, getContext(), true);
			dropSliceInfo.finalX = iArrayTemoOff[0];
			dropSliceInfo.finalY = iArrayTemoOff[1];
			
			/*
			 * 不这样写的原因是requestLayout不是立即同步执行的
			 * 这里有知识点不了解
			 */
//			adjustToDragLayer(iArrayTemoOff, cellInfo.getView(), getContext(), true);
//			dropSliceInfo.originX = iArrayTemoOff[0];
//			dropSliceInfo.originY = iArrayTemoOff[1];
			dropSliceInfo.originX = dragObjectInfo.x;
			dropSliceInfo.originY = dragObjectInfo.y;
			dropSliceInfo.canDrop = true;
			
			dropSliceInfo.init();
			
			
			Utils.log(TAG, dropSliceInfo.toString());
			m_DragLayer.updateDragViewToOriPoint(dropSliceInfo);
		}
		
	}
	
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
			coordnates[1] -= iStatusHeight;
		}
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		Utils.log(TAG, "onDragEnter");
		CellInfo cellInfo = (CellInfo) dragInfo;
		if (cellInfo.getType() == CellInfo.CellType.WIDGET) {
			/*
			 * 把widget拖动到HotSeat
			 * 给DragView设置红色的蒙板
			 */
			//TODO
			
		}
		
	}

	private DragObjectInfo m_CurDragObjectInfo = new DragObjectInfo();
	
	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		/*
		 * 通知DragLayer绘制提示框
		 */
		final CellInfo cellInfo = (CellInfo) dragInfo;
		final DragObjectInfo dragObjectInfo = m_CurDragObjectInfo;
		final boolean[] bArrayOccupid = m_bOccupied;
		
		dragObjectInfo.reset();
		dragObjectInfo.itemView = cellInfo.getView();
		dragObjectInfo.dragView = dragView;
		
		final int iTargetHotSeatCellX = getCellXAccordingCoordinateRefParent(x, y);
		if (iTargetHotSeatCellX > -1) {	
			final boolean bCanDrop = !bArrayOccupid[iTargetHotSeatCellX] && (cellInfo.getType()!=CellInfo.CellType.WIDGET);
			dragObjectInfo.isInCell = true;
			dragObjectInfo.isInvalid = true;
			dragObjectInfo.canDrop = bCanDrop;
//			adapterCoordinateToDragLayer(dragObjectInfo, x, y, xOffset, yOffset);
			adapterCoordinateToDragLayer(dragObjectInfo, iTargetHotSeatCellX);
			if (!bCanDrop) {
				int[] iArrayOccupidItem = new int[]{iTargetHotSeatCellX, 0};
				dragObjectInfo.flagOcupiedList.add(iArrayOccupidItem);
			}
		} else {	//在间隔中
			dragObjectInfo.isInCell = false;
			dragObjectInfo.isInvalid = false;
			dragObjectInfo.canDrop = false;
		}
		
		m_DragLayer.updateDragPreEffect(dragObjectInfo);
	}
	
	/**
	 * 根据相对于父控件 {@link HotSeat}的坐标转为相对于屏幕的坐标{@link DragLayer}。如果DragLayer不是整个屏幕，则有错<br/>
	 * 并把坐标值和大小赋值给DragObjectInfo
	 * @param dragObjectInfo
	*  @param x	到父View的偏移量{@link DropTarget}，如workspace
	 * @param y	到父View的偏移量{@link DropTarget}，如workspace
	 */
	private void adapterCoordinateToDragLayer(final DragObjectInfo dragObjectInfo, int x, int y, int xOffset, int yOffset) {
		final int iCellHSpan = 1;
		final int iCellVSpan = 1;
		final int iCellWidth = m_iCellWidth;
		final int iCellHeight = m_iCellHeight;
		final int iSpace = m_iSpace;
		
		getHitRectRefDragLayer(m_TempRect, this);
		
		final int iLeft = m_TempRect.left + x - xOffset;
		final int iTop = m_TempRect.top + y - yOffset;
		
		dragObjectInfo.x = iLeft;
		dragObjectInfo.y = iTop;
		dragObjectInfo.width = iCellWidth * iCellHSpan + (iCellHSpan-1) * iSpace;
		dragObjectInfo.height = iCellHeight * iCellVSpan + (iCellVSpan-1) * iSpace;
	}

	private void adapterCoordinateToDragLayer(final DragObjectInfo dragObjectInfo, int whichCellX) {
		final int iOffsetLeft = m_iOffsetLeft;
		final int iSpace = m_iSpace;
		final int iHeightOffsetTop = m_iHeightOffsetTop;
		final int iCellWidth = m_iCellWidth;
		final int iCellHeight = m_iCellHeight;
		final int iCellY = 0;
		
		final int iLeft = whichCellX * iCellWidth + whichCellX * iSpace + iOffsetLeft;
		final int iTop = iCellY * iCellHeight + iCellY * iSpace + iHeightOffsetTop;
		
		getHitRectRefDragLayer(m_TempRect, this);
		
		dragObjectInfo.curX = dragObjectInfo.x = iLeft + m_TempRect.left;
		dragObjectInfo.curY = dragObjectInfo.y = iTop + m_TempRect.top;
		dragObjectInfo.width = iCellWidth;
		dragObjectInfo.height = iCellHeight;
	}
	
	
	@Override
	public void onDragExit(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		Utils.log(TAG, "onDragExit");
	}

	@Override
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		final CellInfo cellInfo = (CellInfo) dragInfo;
		if (cellInfo.getType() == CellInfo.CellType.WIDGET) {
			return false;
		}
		
		int iTargetHotSeatCellX = getCellXAccordingCoordinateRefParent(x, y);
		
		boolean bAcceptDrop = (iTargetHotSeatCellX!=-1) && !m_bOccupied[iTargetHotSeatCellX];
		
//		Utils.toastAndlogcat(getContext(), TAG, "%s, targetHotSeatCellX=%d", bAcceptDrop?"不占用":"占用", iTargetHotSeatCellX);
		Utils.log(TAG, "%s, targetHotSeatCellX=%d", bAcceptDrop?"不占用":"占用", iTargetHotSeatCellX);
		
		return bAcceptDrop;
	}
	
	/**
	 * 根据相对于父控件的坐标偏移值，返回对应的是第几个单元格；如果没有对应，返回-1
	 * @param x 相对于父控件的坐标值	HotSeat
	 * @param y 相对于父控件的坐标值	HotSeat
	 * @return
	 */
	private int getCellXAccordingCoordinateRefParent(int x, int y) {
		final int iOffsetLeft = m_iOffsetLeft;
		final int iHeightOffsetTop = m_iHeightOffsetTop;
		final int iCellWidth = m_iCellWidth;
		final int iCellHeight = m_iCellHeight;
		final int iSpace = m_iSpace;
		
		int iTargetHotSeatCellX = -1;
		
		int left, top, right, bottom;
		Rect r = new Rect();
		for (int i=0; i<CELL_COUNTS; i++) {
			left = iOffsetLeft + i * iCellWidth + i * iSpace;
			top = iHeightOffsetTop;
			right = left + iCellWidth;
			bottom = top + iCellHeight;
			r.set(left, top, right, bottom);
			if (r.contains(x, y) ) {
				iTargetHotSeatCellX = i;
				break;
			}
		}
		
		return iTargetHotSeatCellX;
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
//				Utils.log(TAG, "getHitRectRefDragLayer 计算好了");
				ViewGroup view = (ViewGroup) objLastView;
				view.getHitRect(outRect);
				break;
			}
			
			objLastView = viewParent;
			viewParent = viewParent.getParent();
			
			if (i >= iMax) {
				/*
				 * 这里是输出log，还是抛出异常
				 */
				throw new IllegalArgumentException(String.format("布局层次超过了%d层，请优化或修改最大值", iMax));
			}
			
		}//end for
		
	}
	
}
