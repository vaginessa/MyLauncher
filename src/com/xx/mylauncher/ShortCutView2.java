package com.xx.mylauncher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class ShortCutView2 extends FrameLayout {

	private ImageView m_ImgView;
	
	private TextView m_TxvLabel;

	public ShortCutView2(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ShortCutView2(Context context) {
		this(context, null);
	}


	public ShortCutView2(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initRes(context);
	}

	private void initRes(Context context) {
		View view = View.inflate(getContext(), R.layout.layout_launcher_item, null);
		m_ImgView = (ImageView) view.findViewById(R.id.image_button);
		m_TxvLabel = (TextView) view.findViewById(R.id.txv_label);
		
		addView(view);
	}
	
	public void setIcon(Drawable icon) {
		m_ImgView.setImageDrawable(icon);
	}
	
	public void setIcon(Bitmap bitmap) {
		m_ImgView.setImageBitmap(bitmap);
	}
	
	public void setIconBackground(Drawable drawable) {
		m_ImgView.setBackground(drawable);
	}
	
	public void setLabel(String label) {
		m_TxvLabel.setText(label);
	}
	
	public void setViewContentVisiblity(int visibility) {
		m_ImgView.setVisibility(visibility);
		m_TxvLabel.setVisibility(visibility);
	}
	
	public void setLabelVisibility(int visibility) {
		m_TxvLabel.setVisibility(visibility);
	}
	

}
