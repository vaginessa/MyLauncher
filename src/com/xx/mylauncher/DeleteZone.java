package com.xx.mylauncher;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

public class DeleteZone extends View {

	private int m_iViewHeight;
	
	private int m_iViewWidth;
	private static final int DIMEN_VIEW_HEIGHT = Constant.DIMEN_DEFAULT_DELETEZONE_HEIGHT;

	private int m_iScreenWidth;
	
	
	public DeleteZone(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DeleteZone(Context context) {
		this(context, null);
	}

	public DeleteZone(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initRes(context, attrs);
	}

	private void initRes(Context context, AttributeSet attrs) {
		final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		
		m_iScreenWidth = outMetrics.widthPixels;
		m_iViewWidth = m_iScreenWidth;
		m_iViewHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DIMEN_VIEW_HEIGHT, context.getResources().getDisplayMetrics());
		
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DeleteZone);
		m_iViewHeight = (int) ta.getDimension(R.styleable.DeleteZone_deletezone_height, m_iViewHeight);
		
		ta.recycle();
	}
	
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(m_iViewWidth, m_iViewHeight);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		/*
		 * 
		 */
		
		
	}
	
	
	
	
	
}
