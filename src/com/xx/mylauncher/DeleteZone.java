package com.xx.mylauncher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class DeleteZone extends FrameLayout implements DropTarget, DragController.DragListener {

	private static final String TAG = "DeleteZone";
	
	private int m_iViewHeight;
	
	private int m_iViewWidth;
	private static final int DIMEN_VIEW_HEIGHT = Constant.DIMEN_DEFAULT_DELETEZONE_HEIGHT;

	private int m_iScreenWidth;
	
	private Drawable m_DrawableBg;
	
	private Bitmap m_BitmapBg;
	
	private Paint m_PaintHint;
	
	private Rect m_RectTemp = new Rect();
	
	private boolean m_bIsHintTrashcan;
	
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
		m_DrawableBg = ta.getDrawable(R.styleable.DeleteZone_trashcan);
		
		ta.recycle();
		
		initImage();
		
		ColorMatrix colorMatrix = new ColorMatrix(new float[]{  
		        1.5F, 1.5F, 1.5F, 0, -1,  
		        1.5F, 1.5F, 1.5F, 0, -1,  
		        1.5F, 1.5F, 1.5F, 0, -1,  
		        0, 0, 0, 1, 0,  
		}); 
		
		m_PaintHint = new Paint();
		m_PaintHint.setColor(Color.RED);
//		m_PaintHint.setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.DARKEN));  
//		m_PaintHint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
		
//		test();
	}

	private void test() {
		postDelayed(new Runnable() {
			
			@Override
			public void run() {
				showTrashcan();
			}
		}, 4000);
		
		postDelayed(new Runnable() {
			
			@Override
			public void run() {
				dismissTrashcan();
			}
		}, 8000);
	}

	private void initImage() {
		if (m_DrawableBg == null) {
			m_DrawableBg = getResources().getDrawable(R.drawable.trashcan);
		}
		
		if (m_DrawableBg instanceof BitmapDrawable) {
			m_BitmapBg = ((BitmapDrawable)m_DrawableBg).getBitmap();
		}
		
		if (m_BitmapBg == null ) {
			m_BitmapBg = BitmapFactory.decodeResource(getResources(), R.drawable.trashcan);
		}
		
		final int iDstWidth = m_BitmapBg.getWidth();
		final int iDstHeight = m_BitmapBg.getHeight();
		
		if (iDstWidth>m_iViewWidth || iDstHeight>m_iViewHeight) {
			m_BitmapBg = Bitmap.createScaledBitmap(m_BitmapBg, Math.min(iDstWidth, m_iViewWidth), Math.min(iDstHeight, m_iViewHeight), true);	
			
		}
		
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
		final int iLeft = m_iViewWidth / 2 - m_BitmapBg.getWidth() / 2;
		final int iTop = m_iViewHeight / 2 - m_BitmapBg.getHeight() / 2;
		
		m_RectTemp.set(iLeft, iTop, iLeft+m_BitmapBg.getWidth(), iTop+m_BitmapBg.getHeight() );
		
		if (m_bIsAnimShow) {
			if (!m_bIsHintTrashcan) {
				canvas.drawBitmap(m_BitmapBg, iLeft, m_iCurTrashcanHeight, null);	
			} else {
				canvas.drawBitmap(m_BitmapBg, iLeft, m_iCurTrashcanHeight, m_PaintHint);
			}
		
		}
		
	}
	
	/*
	 * 它的出现是滑下来的，退出是滑上去的
	 */
	private ValueAnimator m_ValueAnimator;
	private static final long ANIM_DURATION = 600;

	private int m_iCurTrashcanHeight;
	private boolean m_bIsAnimShow = false;
	
	public void showTrashcan() {
		if (m_ValueAnimator == null) {
			m_ValueAnimator = new ValueAnimator();
			m_ValueAnimator.setDuration(ANIM_DURATION);
			m_ValueAnimator.setFloatValues(0f, 1f);
		}
		m_ValueAnimator.removeAllListeners();
		m_ValueAnimator.removeAllUpdateListeners();
		m_ValueAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				super.onAnimationStart(animation);
				m_bIsAnimShow = true;
			}
		});
		m_ValueAnimator.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				final int iFinalY = m_iViewHeight / 2 - m_BitmapBg.getHeight() / 2;
				final int iOriginY = -m_BitmapBg.getHeight();
				final float iProcess = (Float) animation.getAnimatedValue();
				m_iCurTrashcanHeight = (int) ((iFinalY - iOriginY) * iProcess) + iOriginY;
				
				invalidate();
			}
		});
		
		m_ValueAnimator.start();
	}
	
	public void dismissTrashcan() {
		if (m_ValueAnimator == null) {
			return;
		}
		
		m_ValueAnimator.removeAllListeners();
		m_ValueAnimator.removeAllUpdateListeners();
		m_ValueAnimator.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				final int iOriginY = m_iViewHeight / 2 - m_BitmapBg.getHeight() / 2;
				final int iFinalY = -m_BitmapBg.getHeight();
				final float iProcess = (Float) animation.getAnimatedValue();
				m_iCurTrashcanHeight = (int) ((iFinalY - iOriginY) * iProcess) + iOriginY;
				
				invalidate();
			}
		});
		m_ValueAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				super.onAnimationStart(animation);
				m_bIsAnimShow = true;
			}
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				m_bIsAnimShow = false;
				m_ValueAnimator = null;
			}
			@Override
			public void onAnimationCancel(Animator animation) {
				super.onAnimationCancel(animation);
				m_bIsAnimShow = false;
				m_ValueAnimator = null;
			}
		});
		
		m_ValueAnimator.start();
	}
	
	

	@Override
	public void onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		Utils.log(TAG, "onDrop");
		/*
		 * 可以做一些删除的效果
		 */
		
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		Utils.log(TAG, "onDragEnter");
		m_bIsHintTrashcan = false;
		
	}
    /**
     * 拖动进入{@link DropTarget} 时回调
	 * @param source	从哪里拖动过来的，拖动源
	 * @param x	到父View的偏移量{@link DropTarget}，如workspace
	 * @param y	到父View的偏移量{@link DropTarget}，如workspace
	 * @param xOffset	到View本身的偏移量，即到长按下的View的偏移量
	 * @param yOffset
	 * @param dragView	拖动的View，绘制表现层在DragLayer中
	 * @param dragInfo	拖动的View所携带的信息
     */

	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		boolean bIsHint = isHintTrashcan(x, y);
		invalidate();
	}
	
	public boolean isHintTrashcan(int xOffsetParent, int yOffsetParent) {
		boolean bIsHint = m_RectTemp.contains(xOffsetParent, yOffsetParent);

		m_bIsHintTrashcan = bIsHint;
		return bIsHint;
	}
	

	@Override
	public void onDragExit(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		Utils.log(TAG, "onDragExit");
		m_bIsAnimShow = false;
		m_bIsHintTrashcan = false;
		
	}

	@Override
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		
		boolean r =  isHintTrashcan(x, y);
		
		Utils.toastAndlogcat(getContext(), TAG, "%s", r ? "可以删除" : "不可以删除");
		
		return r;
	}

	@Override
	public boolean isDropEnable() {
		return true;
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
//				View view = (View) objLastView;
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

	@Override
	public void onDragStart(DragSource source, Object info, int dragAction) {
		showTrashcan();
	}

	@Override
	public void onDragEnd() {
		dismissTrashcan();
	}
	
	
	
}
