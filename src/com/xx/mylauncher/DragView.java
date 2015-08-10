package com.xx.mylauncher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
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
	
	/** 删除的Bitmap效果 */
	private Bitmap m_DeleteBitmap;
	private Paint m_PaintDelete;
	
	private boolean m_BInDeleteZone = false;
	
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
		
		m_PaintDelete = new Paint();
		m_PaintDelete.setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.DARKEN));
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		//先这样
		if (m_BInDeleteZone) {
			if (m_DeleteBitmap != null) {
				setMeasuredDimension(m_DeleteBitmap.getWidth(), m_DeleteBitmap.getHeight());	
			} else {
				setMeasuredDimension(m_iViewWidth, m_iViewHeight);	
			}
		} else {
			setMeasuredDimension(m_iViewWidth, m_iViewHeight);	
		}
		
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		//画呀画
		if (!m_BInDeleteZone) {
			canvas.drawBitmap(m_ViewBitmap, 0, 0, m_Paint);	
		} else {
			canvas.drawBitmap(m_DeleteBitmap, 0, 0, m_PaintDelete);
		}
		
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
//		return m_ViewBitmap;
		return Utils.getViewBitmap(this);
	}
	
	public void onDragInDeleteZone() {
		final Bitmap dragViewBitmap = getViewBitmap();
		final int iBitmapWidth = dragViewBitmap.getWidth();
		final int iBitmapHeight = dragViewBitmap.getHeight();
		final float iWidthScale = 1.5f;
		final float iHeightScale = 1.5f;
		m_DeleteBitmap = Bitmap.createScaledBitmap(getViewBitmap(), (int)(iBitmapWidth*iWidthScale), (int)(iBitmapHeight*iHeightScale), true);
		
		m_BInDeleteZone = true;
		requestLayout();
		invalidate();
	}
	
	public void onDragOutDeleteZone() {
		m_BInDeleteZone = false;
		requestLayout();
		invalidate();
	}
	
	
	public void clearResource() {
		if (m_ViewBitmap != null) {
			m_ViewBitmap.recycle();
			m_ViewBitmap = null;
		}
		
		if (m_DeleteBitmap != null) {
			m_DeleteBitmap.recycle();
			m_DeleteBitmap = null;
		}
		
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		clearResource();
	}
	
	
	
	
}
