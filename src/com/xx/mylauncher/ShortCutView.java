package com.xx.mylauncher;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 桌面上的icon item项的view，先用简单的来表示
 * 
 * @author baoxing
 * 
 */
public class ShortCutView extends LinearLayout {

	private ImageButton m_Icon;
	
	private TextView m_Lable;
	
	/** ImageButtom 大小 */
	private static final int WIDTH = 48;	//dp
	
	/** ImageButton 大小 */
	private static final int HEIGHT = 48; 	
	
	private static final int MARGIN_BOTTOM = 5;
	
	/** ImageButton大小 */
	private int m_Width, m_Height;
	
	private int m_MarginBottom;
	
	public ShortCutView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ShortCutView(Context context) {
		this(context, null);
	}

	public ShortCutView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initRes(context);
	}

	private void initRes(Context context) {
		setOrientation(VERTICAL);
		setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT) );
		setGravity(Gravity.CENTER);
		
		m_Width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, WIDTH, new DisplayMetrics());
		m_Height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, HEIGHT, new DisplayMetrics());
		m_MarginBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MARGIN_BOTTOM, new DisplayMetrics());
		
		final ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(m_Width, m_Height);
		lp.setMargins(0, 0, 0, m_MarginBottom);
		m_Icon = new ImageButton(context);
		
		m_Icon.setLayoutParams(lp );
		m_Lable = new TextView(context);
		m_Lable.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		m_Lable.setTextColor(Color.WHITE);
		
		addView(m_Icon);
		addView(m_Lable);		
	}
	
	public void setIcon(Drawable icon) {
		m_Icon.setImageDrawable(icon);
	}
	
	public void setLable(String lableName) {
		m_Lable.setText(lableName);
	}

}
