package com.xx.mylauncher;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;

/**
 * 应用程序信息实体类
 * @author baoxing
 *
 */
public class AppInfo {
	
	/** 应用程序标签名 */
	private String labelName;
	
	/** 应用程序包名 */
	private String pkgName;
	
	/** 应用程序主Activity */
	private String clsName;
	
	/** 应用程序图标 */
	private Drawable icon;
	
	public AppInfo() {}

	public String getLabelName() {
		return labelName;
	}

	public void setLabelName(String labelName) {
		this.labelName = labelName;
	}

	public String getPkgName() {
		return pkgName;
	}

	public void setPkgName(String pkgName) {
		this.pkgName = pkgName;
	}

	public String getClsName() {
		return clsName;
	}

	public void setClsName(String clsName) {
		this.clsName = clsName;
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}
	
	public Intent getIntent() {
		ComponentName cName = new ComponentName(pkgName, clsName);
		Intent intent = new Intent();
		intent.setComponent(cName);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		return intent;
	}

	@Override
	public String toString() {
		return "AppInfo [labelName=" + labelName + ", pkgName=" + pkgName
				+ ", clsName=" + clsName + ", icon=" + icon + "]\n";
	}
	
	
}
