package com.xx.mylauncher;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.widget.Toast;

public class Utils {
	
	
	private static final boolean DEBUG = true;

	public static void log(String tag, String msg) {
		if (DEBUG) {
			Log.i(tag, msg);
		}
		
	}
	
	public static void log(String tag, String format, Object...objects) {
		if (DEBUG) {
			String msg = String.format(format, objects);
			Log.i(tag, msg);
		}
	}	
	
	public static void log(boolean debug, String tag, String format, Object...objects) {
		if (DEBUG && debug) {
			String msg = String.format(format, objects);
			Log.i(tag, msg);
		}
	}	
	
	
	public static void log(String tag, Throwable tr,  String format, Object...objects) {
		String msg = String.format(format, objects);
		Log.e(tag, msg, tr);
	}
	
	public static void toast(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
		
	}
	
	public static void toast(Context context, String format, Object...msg) {
		Toast.makeText(context, String.format(format, msg), Toast.LENGTH_LONG).show();
		
	}
	
	public static void toastAndlogcat(Context context, String tag, String format, Object...msg) {
		toast(context, format, msg);
		log(tag, format, msg);
	}
	
	public static void toastAndlogcat(Context context, String tag, String msg) {
		toast(context, msg);
		log(tag, msg);
	}
	
	private static int m_iStatusHeight = -1;
	
	/**
	 * 获取状态栏的高度
	 * @param context
	 * @return
	 */
	public static int getStatusHeight(Context context){
		if (m_iStatusHeight == -1) {
			int statusHeight = 0;
			Rect localRect = new Rect();
			((Activity) context).getWindow().getDecorView()
					.getWindowVisibleDisplayFrame(localRect);
			statusHeight = localRect.top;
			if (0 == statusHeight) {
				Class<?> localClass;
				try {
					localClass = Class.forName("com.android.internal.R$dimen");
					Object localObject = localClass.newInstance();
					int i5 = Integer.parseInt(localClass
							.getField("status_bar_height").get(localObject)
							.toString());
					statusHeight = context.getResources()
							.getDimensionPixelSize(i5);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			m_iStatusHeight = statusHeight;
		}
     
        return m_iStatusHeight;
    }
	
	
	
}
