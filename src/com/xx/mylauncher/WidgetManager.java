package com.xx.mylauncher;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * Widget管理类
 * @author baoxing
 *
 */
public class WidgetManager {
	
	private static WidgetManager m_WidgetManager;
	
	private AppWidgetHost m_WidgetHost;
	
	private AppWidgetManager m_AppWidgetManager;
	
	private MainActivity m_Launcher;
	
	private static final int HOSTID = 0x100;

	private static final int REQUEST_ADD_WIDGET = 1;

	private static final int REQUEST_CREATE_WIDGET = 2;

	private static final String TAG = "WidgetManagerTAG";
	
	private WidgetManager(Context context, MainActivity launcher) {
		m_Launcher = launcher;
//		m_WidgetHost = new AppWidgetHost(context, HOSTID);
		m_WidgetHost = new LauncherAppWidgetHost(context, HOSTID);
		m_AppWidgetManager = AppWidgetManager.getInstance(context);
	}
	
	/**
	 * 传入的
	 * @param context
	 * @return
	 */
	public static synchronized WidgetManager getInstance(Context context, MainActivity launcher) {
		if (m_WidgetManager == null) {
			m_WidgetManager = new WidgetManager(context, launcher);
		}
		return m_WidgetManager;
	}
	
	
	public void onResume() {
		m_WidgetHost.startListening();
	}
	
	public void onStop() {
		m_WidgetHost.stopListening();
	}
	
	
	/**
	 * 弹出系统选择Widget对话框
	 */
	public void selectWidgets() {
		Utils.log(TAG, "selecctWidgets");
		
		int widgetId = m_WidgetHost.allocateAppWidgetId();
		Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
		pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		
		m_Launcher.startActivityForResult(pickIntent, REQUEST_ADD_WIDGET);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Utils.log(TAG, "requestCode=%d, resultCode=%d", requestCode, resultCode);
		
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case REQUEST_ADD_WIDGET:
				addWidget(data);
				break;
			case REQUEST_CREATE_WIDGET:
				createWidget(data);
				break;
			default:
				break;
			}
		} else if (requestCode==REQUEST_CREATE_WIDGET && resultCode==Activity.RESULT_CANCELED && data!=null) {	//啥意思
			int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
			if (appWidgetId != -1) {
				m_WidgetHost.deleteAppWidgetId(appWidgetId);
			}
		}
		
	}
	
	private void addWidget(Intent data) {
		Utils.log(TAG, "addWidget");
		
		int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
		AppWidgetProviderInfo appWiddget = m_AppWidgetManager.getAppWidgetInfo(appWidgetId);
		
		Utils.log( TAG, "configure:"+appWiddget.configure);
		
		if (appWiddget.configure != null) {
			Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
			intent.setComponent(appWiddget.configure);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			
			m_Launcher.startActivityForResult(intent, REQUEST_CREATE_WIDGET);
		} else {
			onActivityResult(REQUEST_CREATE_WIDGET, Activity.RESULT_OK, data);
		}
		
	}
	
	private void createWidget(Intent data) {
		Utils.log(TAG, "createWidget");
		
		int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
		AppWidgetProviderInfo appWidget = m_AppWidgetManager.getAppWidgetInfo(appWidgetId);
		View hostView = m_WidgetHost.createView((Context)m_Launcher, appWidgetId, appWidget);
		
		m_Launcher.addViewInScreen(hostView, appWidget);
		
		Utils.log(TAG, String.format("%s, width=%d, height=%d", "add", appWidget.minWidth, appWidget.minHeight), Toast.LENGTH_SHORT);	
	
	}
	
}
