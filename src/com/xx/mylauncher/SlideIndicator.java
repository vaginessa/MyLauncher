package com.xx.mylauncher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

/**
 * 多屏的滑动指示
 * 暂时只实现一种，即圆点的滑动指示，效果可参看360桌面
 * @author baoxing
 *
 */
public class SlideIndicator extends View {

	/** 圆圈个数 */
	private int m_iNumbers;

	/** 圆圈的半径 */
	private int m_iRadius;
	
	/** 圆圈背景色 */
	private int m_iBgColor;
	
	/** 圆圈前景色 */
	private int m_iFgColor;
	
	/** 圆圈间间隔 */
	private int m_iSpace;
	
	/** 该控件的宽度 */
	private int m_iWidth;
	
	/** 该控件的高度 */
	private int m_iHeight;
	
	/** 背景色画笔 */
	private Paint m_PaintBg;
	
	/** 前景色画笔 */
	private Paint m_paintFg;
	
	private int m_iScreenWidth;
	
	private int m_iScreenHeight;
	
	private ValueAnimator m_ValueAnimator;
	/** 是否启用动画 */
	private boolean m_bIsAnim = false;;
	/** 当启动动画时{@link #m_bIsAnim}，该值表示动画中的圆圈的x坐标 */
	private float m_fCurX;
	/** 滑动的动画时间 */
	private static final long ANIMATOR_DURATION = 500;
	
	/** 当前指示的是第几个 */
	private int m_iCurInditor = 0;
	
	/** 上一次指示的是第几个 */
	private int m_iLastInditor = 0;
	
	public SlideIndicator(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlideIndicator(Context context) {
		this(context, null);
	}
	
	
	public SlideIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initRes(context, attrs);
	}

	private void initRes(Context context, AttributeSet attrs) {
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SlideIndicator);
		m_iRadius = (int) ta.getDimension(R.styleable.SlideIndicator_radius, 10);
		m_iSpace = (int) ta.getDimension(R.styleable.SlideIndicator_space, 10);
		m_iNumbers = ta.getInt(R.styleable.SlideIndicator_numbers, 5);
		m_iBgColor = ta.getColor(R.styleable.SlideIndicator_background_color, 0xff000000);
		m_iFgColor = ta.getColor(R.styleable.SlideIndicator_foreground_color, 0xffffffff);
		
		ta.recycle();
		
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(dm);
		m_iScreenWidth = dm.widthPixels;
		m_iScreenHeight = dm.heightPixels;
		
		m_PaintBg = new Paint(Paint.ANTI_ALIAS_FLAG);
		m_PaintBg.setStyle(Paint.Style.FILL);
		m_PaintBg.setColor(m_iBgColor);
		m_PaintBg.setDither(true);
		
		m_paintFg = new Paint(Paint.ANTI_ALIAS_FLAG);
		m_paintFg.setStyle(Paint.Style.FILL);
		m_paintFg.setColor(m_iFgColor);
		m_paintFg.setDither(true);
		
		m_ValueAnimator = new ValueAnimator();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int iWidthSize = MeasureSpec.getSize(widthMeasureSpec);
		final int iWidthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int iHeightSize = MeasureSpec.getSize(heightMeasureSpec);
		final int iHeightMode = MeasureSpec.getMode(heightMeasureSpec);
		
		int iWidth, iHeight;
		
		if (iWidthMode == MeasureSpec.EXACTLY) {
			iWidth = iWidthSize;
		} else if (iWidthMode == MeasureSpec.AT_MOST) {
			iWidth = Math.min(iWidthSize, m_iScreenWidth);
		} else {
			iWidth = m_iScreenWidth;
		}
		
		if (iHeightMode == MeasureSpec.EXACTLY) {
			iHeight = iHeightSize;
			
		} else if (iHeightMode == MeasureSpec.AT_MOST) {
			iHeight = Math.min(iHeightSize, m_iRadius*3);
			
		} else {
			iHeight = m_iRadius*3;
		}
		
		m_iWidth = iWidth;
		m_iHeight = iHeight;
		
		setMeasuredDimension(iWidth, iHeight);
	}
	
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		/*
		 * hua ya hua
		 */
		canvas.drawColor(Color.TRANSPARENT);	//透明
		
		int iMidHeight = m_iHeight / 2;
	
		for (int i=0; i<m_iNumbers; i++) {
			canvas.drawCircle(getDrawStartX(i), iMidHeight, m_iRadius, m_PaintBg);
		}
		
		if (!m_bIsAnim) {
			canvas.drawCircle(getDrawStartX(m_iCurInditor), iMidHeight, m_iRadius, m_paintFg);
		} else {
			canvas.drawCircle(m_fCurX, iMidHeight, m_iRadius, m_paintFg);
		}
		
	}
	
	/**
	 * 根据第几个位置，返回它的绘画开始点，x坐标
	 * @param position
	 * @return
	 */
	private float getDrawStartX(int position) {
		final int iNumbers = m_iNumbers;
		int iMidWidht = m_iWidth /2;
		int iStartX;
		int iHalfNums;
		if (iNumbers % 2 == 0) {
			iHalfNums = iNumbers / 2;
			iStartX = iMidWidht - m_iSpace /2 - iHalfNums*2*m_iRadius - (iHalfNums-1)*m_iSpace + m_iRadius;
		} else {
			iHalfNums = (iNumbers-1) / 2;
			iStartX = iMidWidht - m_iRadius - iHalfNums*2*m_iRadius - iHalfNums*m_iSpace + m_iRadius;
		}
		
		int iUnitSpace = m_iRadius*2 + m_iSpace;
		
		float result = iStartX + position*iUnitSpace;
		
		return result;
	}
	
	
	/**
	 * 设置当前指示的第几个，在UI线程中调用
	 * @param m_iCurInditor	当前指示的是第几个
	 * @param isAnim	是否启动动画平滑
	 */
	public void setCurInditor(int m_iCurInditor, boolean isAnim) {
		/*
		 * 这里犯这个错误，绝大部分的原因是变量的命名问题，又debug了一会儿
		 */
//		m_iLastInditor = m_iCurInditor;
		m_iLastInditor = this.m_iCurInditor;
		this.m_iCurInditor = m_iCurInditor;
		updateIndicator(isAnim);
	}
	
	/**
	 * 更新控件
	 * @param isAnim
	 */
	private void updateIndicator(boolean isAnim) {
		if (!isAnim) {
			invalidate();
			return;
		}
		
		if (m_ValueAnimator.isRunning()) {
			m_ValueAnimator.cancel();
		}
		
		m_ValueAnimator.setFloatValues(1, 100);
		m_ValueAnimator.setDuration(ANIMATOR_DURATION);
		m_ValueAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationCancel(Animator animation) {
				super.onAnimationCancel(animation);
			}
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				m_bIsAnim = false;
			}
			@Override
			public void onAnimationStart(Animator animation) {
				super.onAnimationStart(animation);
				m_bIsAnim = true;
			}
		});
		m_ValueAnimator.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float fProcess = (Float) animation.getAnimatedValue();
				final float fOrigin = getDrawStartX(m_iLastInditor);
				final float fFinal = getDrawStartX(m_iCurInditor);
				final float fTotal = fFinal - fOrigin;
				m_fCurX = fOrigin + fProcess * fTotal;
				
				invalidate();
			}
		});
		
		m_ValueAnimator.start();
	}

	public int getM_iNumbers() {
		return m_iNumbers;
	}

	public void setM_iNumbers(int m_iNumbers) {
		this.m_iNumbers = m_iNumbers;
	}

	public int getM_iRadius() {
		return m_iRadius;
	}

	public void setM_iRadius(int m_iRadius) {
		this.m_iRadius = m_iRadius;
	}

	public int getM_iBgColor() {
		return m_iBgColor;
	}

	public void setM_iBgColor(int m_iBgColor) {
		this.m_iBgColor = m_iBgColor;
		m_PaintBg.setColor(m_iBgColor);
	}

	public int getM_iFgColor() {
		return m_iFgColor;
	}

	public void setM_iFgColor(int m_iFgColor) {
		this.m_iFgColor = m_iFgColor;
		m_paintFg.setColor(m_iFgColor);
	}

	public int getM_iSpace() {
		return m_iSpace;
	}

	public void setM_iSpace(int m_iSpace) {
		this.m_iSpace = m_iSpace;
	}

	public int getM_iWidth() {
		return m_iWidth;
	}

	public int getM_iHeight() {
		return m_iHeight;
	}

	/**
	 * 得到当前指示的是第几个
	 * @return
	 */
	public int getM_iCurInditor() {
		return m_iCurInditor;
	}


	
}
