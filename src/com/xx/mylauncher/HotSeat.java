package com.xx.mylauncher;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * HotSeat，屏幕中固定的部分
 * 有五个cell格子
 * 五个 item view {@link ShortCutView2}，隐藏 label，平分控件宽度，居中显示
 * 中间的固定
 * @author baoxing
 *
 */
public class HotSeat extends ViewGroup {

	private static final String TAG = "HotSeat";

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
	
	/** default cell space */
	private static final int DIMEN_SPACE = 10; 	//10dp
	
	/** 指示那个格子固定 */
	private static final int FIXED_NUM = 2;
	
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
		TypedArray ta = context.obtainStyledAttributes(set, R.styleable.HotSeat);
		m_iHeightOffsetTop = (int) ta.getDimension(R.styleable.HotSeat_hotseat_offset_top, 0);
		m_iHeightOffsetBottom = (int) ta.getDimension(R.styleable.HotSeat_hotseat_offset_bottom, 0);
		m_iSpace = (int) ta.getDimension(R.styleable.HotSeat_hotseat_space, iDimenSpace);

		ta.recycle();
		
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
				item.setIcon(normalDrawable);
				CellInfo cellInfo = new CellInfo();
				cellInfo.setHotSeatCellX(FIXED_NUM);
				cellInfo.setHotSeatCellY(0);
				cellInfo.setView(item);
				item.setTag(cellInfo);
				
				addView(item);
				requestLayout();
			}
		});
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int iChildCount = getChildCount();
		if (iChildCount<=0) {
			Utils.log(TAG, "onLayout-no childs");
			return;
		}
		
		Utils.log(TAG, "onLayout, child counts=%d", iChildCount);
		
		final int iViewWidth = getMeasuredWidth();
		final int iViewHeight = getMeasuredHeight();
		
		Utils.log(TAG, "控件的: width=%d, height=%d", iViewWidth, iViewHeight);
		
		m_iHotSeatWidth = iViewWidth;
		m_iHotSeatHeight = iViewHeight;
		m_iCellHeight = iViewHeight - m_iHeightOffsetBottom - m_iHeightOffsetTop;
		m_iCellWidth = m_iCellHeight;
		
		final int iHeightOffsetTop = m_iHeightOffsetTop;
		final int iCellWidth = m_iCellWidth;
		final int iCellHeight = m_iCellHeight;
		final int iCellSpace = m_iSpace;
		final int iContentWidth = CELL_COUNTS * iCellWidth + (CELL_COUNTS-1) * iCellSpace;
		final int iOffsetLeft = (iViewWidth - iContentWidth) / 2;
		
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

			left = cellX * iCellWidth + cellX * iCellSpace;
			top = cellY * iCellHeight + cellY * iCellSpace + iHeightOffsetTop;
			right = left + iCellWidth;
			bottom = top + iCellHeight;

			child.layout(left+iOffsetLeft, top, right, bottom);
			Utils.log(TAG, "add child view (%d, %d), (%d, %d), child width=%d, height=%d, measureWidth=%d, measureHeight=%d", 
					left+iOffsetLeft, top, right, bottom, child.getWidth(), child.getHeight(), child.getMeasuredWidth(),child.getMeasuredHeight() );
		}
		
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int iChildCount = getChildCount();
		final int iCellWidth = m_iCellWidth;
		final int iCellHeight = m_iCellHeight;
		
		Utils.log(TAG, "onMeasure-iChildCount=%d, iCellWidth=%d, iCellHeight=%d", iChildCount, iCellWidth, iCellHeight);
		
		View child;
		int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(iCellWidth, MeasureSpec.EXACTLY);
		int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(iCellHeight, MeasureSpec.EXACTLY);
		
		for (int i=0; i<iChildCount; i++) {
			child = getChildAt(i);
			child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
		}
		
	}
	
	
}
