package com.xx.mylauncher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * 自定义拖动的View，它被添加到DragLayer层中绘制
 * @author baoxing
 *
 */
public class DragView extends View {

	private static final String TAG = "DragView";

	/** 绘制层 */
	private DragLayer m_DragLayer;
	
	/** 偏移量X，即按下的点到item view的内偏移 */
	private int m_iOffsetX;
	
	/** 偏移量Y，即按下的点到item view的内偏移 */
	private int m_iOffsetY;
	
	private DragLayer.LayoutParams m_LayoutParams;
	
	/** View的宽度 */
	private int m_iViewWidth;
	
	/** View的高度 */
	private int m_iViewHeight;
	
	private Paint m_Paint;
	
	private Bitmap m_ViewBitmap;
	
	/** 状态栏高度 */
	private int m_iStatusHeight;
	
	public DragView(Context context, Bitmap bitmap, MainActivity launcher, 
				int locXRefDragLayer, int locYRefDragLayer, int rawX, int rawY) {
		super(context);
		
		m_iOffsetX = rawX - locXRefDragLayer;
		m_iOffsetY = rawY - locYRefDragLayer;
		m_DragLayer = launcher.getDragLayer();
		m_iViewWidth = bitmap.getWidth();
		m_iViewHeight = bitmap.getHeight();				
		
		m_ViewBitmap = Bitmap.createBitmap(bitmap);
		
		initRes(context);
		
		Utils.log(TAG, "m_iViewWidth=%d, m_iViewHeight=%d", m_iViewWidth, m_iViewHeight);
//		Utils.toast(getContext(), "m_iViewWidth=%d, m_iViewHeight=%d", m_iViewWidth, m_iViewHeight);
	}
	
	private void initRes(Context context) {
		m_iStatusHeight = Utils.getStatusHeight(getContext());
		
		m_Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		m_Paint.setColor(Color.YELLOW);
		m_Paint.setAlpha(140);
		m_Paint.setStyle(Paint.Style.FILL);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		//先这样
		setMeasuredDimension(m_iViewWidth, m_iViewHeight);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		//画呀画
		int r = Math.min(m_iViewWidth, m_iViewHeight);				
		canvas.drawCircle(r/2, r/2, r/2, m_Paint);
		
	}
	
	public void show(int rawX, int rawY) {
		final DragLayer.LayoutParams lp = new DragLayer.LayoutParams();
		lp.x = rawX - m_iOffsetX;
//		lp.y = rawY - m_iOffsetY;
		lp.y = rawY - m_iOffsetY - m_iStatusHeight;
		lp.width = m_iViewWidth;
		lp.height = m_iViewHeight;
		lp.customPosition = true;
		setLayoutParams(lp);
		m_LayoutParams = lp;
		
		m_DragLayer.addView(this);
	}
	
	public void move(int rawX, int rawY) {
		final DragLayer.LayoutParams lp = m_LayoutParams;
		int x = rawX - m_iOffsetX;
//		int y = rawY - m_iOffsetY;
		int y = rawY - m_iOffsetY - m_iStatusHeight;
		lp.x = x;
		lp.y = y;
		m_DragLayer.requestLayout();
	}
	
	public void remove() {
		if (m_DragLayer != null) {
//			m_DragLayer.removeView(this);	//不要在这里处理，交给onDropComplete回调函数处理
																		//可能会发生动画没有结束，但已经remove掉的情况
		}
	}
	
	public Bitmap getViewBitmap() {
		return m_ViewBitmap;
	}
	
	public void clearResource() {
		if (m_ViewBitmap != null) {
			m_ViewBitmap.recycle();
			m_ViewBitmap = null;
		}
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		clearResource();
	}
	
	
	
	
}
