package com.xx.mylauncher;

import java.lang.ref.SoftReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * 壁纸业务类
 * @author baoxing
 *
 */
public class MWallerpaperManager {

	private static MWallerpaperManager m_Instance;
	
	private Bitmap m_BitmapWallerpaper;
	
	private SoftReference<Context> m_SrContext;
	
	private int iScreenWidth;
	private int iScreenHeight;
	
	private Workspace m_Workspace;
	
	private MWallerpaperManager(final Context context, final Workspace workspace) {
		initRes(context, workspace);
		
	}

	private void initRes(Context context, Workspace workspace) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();;
		wm.getDefaultDisplay().getMetrics(outMetrics);
		iScreenWidth = outMetrics.widthPixels;
		iScreenHeight = outMetrics.heightPixels;
		
		this.m_Workspace = workspace;
		m_SrContext = new SoftReference<Context>(context);
	}
	
	public static synchronized MWallerpaperManager getInstance(Context context, Workspace workspace) {
		if (m_Instance == null) {
			m_Instance = new MWallerpaperManager(context, workspace);
		}
		
		return m_Instance;
	}
	
	public Bitmap getWallerpaper() {
		Bitmap bitmap = m_BitmapWallerpaper;
		
		if (bitmap == null) {
			final Workspace workspace = this.m_Workspace;
			final int iScreenCounts = workspace.getScreenCounts();
			final Context context = m_SrContext.get();
			final int iBitmapWidth = iScreenCounts * iScreenWidth;
			final int iBitmapHeight = iScreenHeight;
			
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.wallerpaper);
			bitmap = Bitmap.createScaledBitmap(bitmap, iBitmapWidth, iBitmapHeight, true);
			
			m_BitmapWallerpaper = bitmap;
		}
		
		return bitmap;
	}
	
	
	public void initLayoutWallerpaper() {
//		final DragLayer
	}
	
	
	
}
