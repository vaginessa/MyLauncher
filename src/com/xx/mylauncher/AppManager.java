package com.xx.mylauncher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

/**
 * 应用程序信息相关业务类
 * @author baoxing
 *
 */
public class AppManager {

	private static AppManager m_AppManager;
	
	/** 所有的应用程序 */
	public static final int FILTER_ALL_APP = 1;
	
	/** 系统程序 */
	public static final int FILTER_SYSTEM_APP = 2;
	
	/** 第三方应用程序 */
	public static final int FILTER_THIRD_APP = 3;
	
	/** 安装在sdcard中的应用程序 */
	public static final int FILTER_SDCARD_APP = 4;
	
	/** 所有的应用程序 */
	private List<AppInfo> m_ListAllApp;
	
	/** 系统程序 */
	private List<AppInfo>  m_ListSystemApp;
	
	/** 第三方应用程序 */
	private List<AppInfo> m_ListThirdApp;
	
	/** 安装在sdcard中的程序 */
	private List<AppInfo> m_ListSdcardApp;
	
	private Context m_Context;
	
	private AppManager(Context context) {
		m_Context = context;
	}
	
	public static synchronized AppManager getInstance(Context context) {
		if (m_AppManager == null) {
			m_AppManager = new AppManager(context);
		}
		
		return m_AppManager;
	}
	
	/**
	 * 返回指定的应用程序信息
	 * @param filter
	 * @return
	 */
	private List<AppInfo> queryAppInfo(Context context, int filter) {
		final PackageManager pm = context.getPackageManager();
		final List<ApplicationInfo> listAppcations = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		Collections.sort(listAppcations, new ApplicationInfo.DisplayNameComparator(pm) );
		List<AppInfo> appInfos = new ArrayList<AppInfo>();
		
		switch (filter) {
		case FILTER_ALL_APP:
			for (ApplicationInfo app : listAppcations) {
				appInfos.add(getAppInfo(app, pm) );
			}
			break;
		case FILTER_SYSTEM_APP:
			for (ApplicationInfo app : listAppcations) {
				if ( (app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
					appInfos.add(getAppInfo(app, pm) );
				}
			}
			break;
		case FILTER_THIRD_APP:
			for (ApplicationInfo app : listAppcations) {
				if ( (app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
					appInfos.add(getAppInfo(app, pm) );					
				}
			}
			break;
		case FILTER_SDCARD_APP:
			for (ApplicationInfo app : listAppcations) {
				if ((app.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
					appInfos.add(getAppInfo(app, pm) );
				}
			}
			break;
		default:
			throw new IllegalArgumentException(String.format("传入的参数错误. [filter=%d]", filter) );
				
		}
		
		return appInfos;
	}
	
	private AppInfo getAppInfo(ApplicationInfo app, PackageManager pm) {
		AppInfo appInfo = new AppInfo();
		CharSequence labelName = app.loadLabel(pm);
		String clsName = getClsName(app.packageName, pm);
		if (labelName == null) {
			labelName = clsName;
		}
		appInfo.setLabelName((String)labelName);
		appInfo.setIcon(app.loadIcon(pm) );
		appInfo.setPkgName(app.packageName);
		appInfo.setClsName(clsName);	
		return appInfo;
	}
	
	/**
	 * 根据包名获取主类名
	 * @param packageName
	 * @return
	 */
	private String getClsName(final String packageName, final PackageManager pm) {
		String clsName = "";
		
		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setPackage(packageName);
		List<ResolveInfo> infoList = pm.queryIntentActivities(intent, 0);
		if (infoList!=null && infoList.size()>0) {
			ResolveInfo info = infoList.get(0);
			if (info != null) {
				clsName = info.activityInfo.name;
			}
		}
		
		return clsName;
	}
	
	public List<AppInfo> getApp(int filter) {
		List<AppInfo> lists = null;
		switch (filter) {
		case FILTER_ALL_APP:
			break;
		case FILTER_SDCARD_APP:
			break;
		case FILTER_SYSTEM_APP:
			break;
		case FILTER_THIRD_APP:
			lists = getThirdApp();
			break;
		default:
			throw new IllegalArgumentException(String.format("参数错误. [filter=%d]", filter) );
		}
		
		return lists;
	}
	
	private List<AppInfo> getThirdApp() {
		if (m_ListThirdApp == null) {
			m_ListThirdApp = new ArrayList<AppInfo>();
			m_ListThirdApp = queryAppInfo(m_Context, FILTER_THIRD_APP);					
		}
		return m_ListThirdApp;
	}
	
	
	
}
