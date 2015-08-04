package com.xx.mylauncher;

import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnLongClickListener, View.OnClickListener {
	
	private static final String TAG = "MainActivity";
	
	private DragLayer m_DragLayer;

	private Workspace m_Workspace;
	
	private CellLayout m_CellLayout;
	
	private DragController m_DragController;
	
	private AppManager m_AppManager;

	private WidgetManager m_WidgetManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tt);
		
		m_DragLayer = (DragLayer) findViewById(R.id.dragLayout);
		m_Workspace = (Workspace) findViewById(R.id.workspace);
		m_CellLayout = (CellLayout) findViewById(R.id.celllayout);
		m_DragController = new DragController(this, this );
		
		m_DragLayer.setDragController(m_DragController);
		m_DragController.registerDropTarget(m_Workspace);	//别忘记注册添加
		m_CellLayout.setLauncher(this);	//TODO 先这样添加
		m_Workspace.setDragController(m_DragController);
		m_Workspace.setDragLayer(m_DragLayer);
		m_Workspace.setOnLongClickListener(this);
		
		
		m_AppManager = AppManager.getInstance(this);
		m_WidgetManager = WidgetManager.getInstance(this, this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (m_WidgetManager != null) {
			m_WidgetManager.onResume();
		}
		
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (m_WidgetManager != null) {
			m_WidgetManager.onStop();
		}
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.exit(0);
	}
	
	/**
	 * 添加应用程序到桌面
	 * @param appInfo
	 */
	private void addViewInScreen(final AppInfo appInfo) {
		ShortCutView2 shortcutView = new ShortCutView2(getBaseContext());
		shortcutView.setIcon(appInfo.getIcon());
		shortcutView.setLabel(appInfo.getLabelName());
		
		int cellHSpan = 1;
		int cellVSpan = 1;
		List<int[]> result = m_CellLayout.isAcceptAddChild(cellHSpan, cellVSpan);
		
		if (result.size() > 0) {
			int[] item = result.get(new Random().nextInt(result.size()));
			addViewInCellLayout(item[0], item[1], cellHSpan, cellVSpan, appInfo, shortcutView);
		} else {
			Toast.makeText(MainActivity.this, getResources().getString(R.string.launcher_no_much_space), Toast.LENGTH_SHORT).show();
		}
		
		
	}
	
	/**
	 * 添加应用程序到launcher中
	 * @param cellX
	 * @param cellY
	 * @param cellHSpan
	 * @param cellVSpan
	 * @param appInfo
	 * @param view
	 */
	private void addViewInCellLayout(int cellX, int cellY, int cellHSpan, int cellVSpan, final AppInfo appInfo, final View view) {
		CellInfo cellInfo = new CellInfo();
		cellInfo.setCellHSpan(cellHSpan);
		cellInfo.setCellVSpan(cellVSpan);
		cellInfo.setCellX(cellX);
		cellInfo.setCellY(cellY);
		cellInfo.setIconName(appInfo.getLabelName());
		cellInfo.setIntent(appInfo.getIntent());
		cellInfo.setType(CellInfo.CellType.SHORT_CUT);
		cellInfo.setView(view);
		view.setTag(cellInfo);
		CellLayout.LayoutParams lp = new CellLayout.LayoutParams();
		lp.cellX = cellX;
		lp.cellY = cellY;
		lp.cellHSpan = cellHSpan;
		lp.cellVSpan = cellVSpan;
		view.setLayoutParams(lp);
		view.setFocusable(true);
		view.setOnLongClickListener(this);
		view.setOnClickListener(this);
		m_CellLayout.addView(view);
	}
	
	/**
	 * 添加Widget到Launcher中
	 * @param widgetView
	 */
	private void addWidgetInCellLayout(final View widgetView, final AppWidgetProviderInfo info, int cellX, int cellY, int cellHSpan, int cellVSpan) {
		CellInfo cellInfo = new CellInfo();
		cellInfo.setCellHSpan(cellHSpan);
		cellInfo.setCellHSpan(cellHSpan);
		cellInfo.setCellVSpan(cellVSpan);
		cellInfo.setCellX(cellX);
		cellInfo.setCellY(cellY);
		cellInfo.setIconName("");
		cellInfo.setType(CellInfo.CellType.WIDGET);
		cellInfo.setView(widgetView);
		widgetView.setTag(cellInfo);
		CellLayout.LayoutParams lp = new CellLayout.LayoutParams();
		lp.cellX = cellX;
		lp.cellY = cellY;
		lp.cellHSpan = cellHSpan;
		lp.cellVSpan = cellVSpan;
		widgetView.setLayoutParams(lp);
//		widgetView.setFocusable(true);
		widgetView.setOnLongClickListener(this);
		
		m_CellLayout.addView(widgetView);
	}
	
	
	void addViewInScreen(final View widgetView, final AppWidgetProviderInfo info) {
		final int iCellSize = m_CellLayout.getCellSize();
		final int iHSpace = m_CellLayout.getHorizontalSpace();
		final int iVSpace = m_CellLayout.getVerticalSpace();
		final int iWidth = info.minWidth;
		final int iHeight = info.minHeight;
		
//		final DisplayMetrics dm = new DisplayMetrics();		这样写是不对的
		final DisplayMetrics dm = this.getResources().getDisplayMetrics();
		final int iUnitSize  = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, dm);
		final int iOffSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, dm); 
//		Utils.toast(this, "iUnitSize=%d, iOffSize=%d", iUnitSize, iOffSize);
		/*
		 * 经测试，这是对的即要符合规范，即widget中的大小
		 * size = 70dp * 格子数 - 30dp
		 */
		int iHSpan = (int) Math.ceil( (iWidth+iOffSize) / iUnitSize);	
		int iVSpan = (int ) Math.ceil( (iHeight+iOffSize) / iUnitSize);
		
		
		List<int[]> result = m_CellLayout.isAcceptAddChild(iHSpan, iVSpan);
		
		if (result.size() > 0) {
			int[] item = result.get(new Random().nextInt(result.size()) );
			addWidgetInCellLayout(widgetView, info, item[0], item[1], iHSpan, iVSpan);
		} else {
			Toast.makeText(MainActivity.this, getResources().getString(R.string.launcher_no_much_space), Toast.LENGTH_SHORT).show();
		}
		
		Utils.log(TAG, "添加的Widget: iHSpan=%d, iVSpan=%d", iHSpan, iVSpan);
	}
	
	
	
	
	
	private void addViewInCellLayout(int cellX, int cellY, int cellHSpan, int cellVSpan) {
		View v = new View(this);
		CellInfo cellInfo = new CellInfo();
		cellInfo.setCellHSpan(cellHSpan);
		cellInfo.setCellVSpan(cellVSpan);
		cellInfo.setCellX(cellX);
		cellInfo.setCellY(cellY);
		cellInfo.setIconName("icon name");
		cellInfo.setIntent(new Intent());
		CellInfo.CellType type = cellHSpan > 1 || cellVSpan>1 ? CellInfo.CellType.WIDGET : CellInfo.CellType.SHORT_CUT;
		cellInfo.setType(type);
		cellInfo.setView(v);
		v.setTag(cellInfo);	//设置cellInfo
		v.setBackgroundColor(Color.RED);
		CellLayout.LayoutParams lp = new CellLayout.LayoutParams();
		lp.cellX = cellX;
		lp.cellY = cellY;
		lp.cellHSpan = cellHSpan;
		lp.cellVSpan = cellVSpan;
		v.setLayoutParams(lp);
		v.setFocusable(true);
		v.setOnLongClickListener(this);
		
		m_CellLayout.addView(v);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Utils.log(TAG, "requestCode=%d, resultCode=%d", requestCode, resultCode);
		Utils.log(TAG, "m_WidgetManager=%s", m_WidgetManager==null?"null":"not null");
		if (m_WidgetManager != null) {
			m_WidgetManager.onActivityResult(requestCode, resultCode, data);
		}
		
	}
	
	
	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
	    if (!v.isInTouchMode()) {
	        Utils.log (TAG, "isInTouchMode returned false. Try touching the view again.");
	        return false;
	     }
		
		Utils.log(true, TAG, "%s.\nview info: %s", "onLongClick", v.toString());
		
		Object tag = v.getTag();
		
		if (tag != null) {
			Utils.log(true, TAG, "%s", tag.toString());
		}
		
		if (tag instanceof CellInfo) {
			CellInfo cellInfo = (CellInfo) tag;
			Utils.log(true, TAG, "图标项或widget项. Type is %s", cellInfo.getType().name() );
			
			m_DragController.startDrag(v, m_Workspace, v.getTag(), Constant.DRAG_MOVE);
			
			return true;
			
		} else {
			Utils.log(true, TAG, "%s", "桌面空白项，弹出item列表对话框");
			showAddItemDialog(MainActivity.this);
		}
		
		
		return false;
	}
	
	
	@Override
	public void onClick(View v) {
		Object object = v.getTag();
		if (object instanceof CellInfo) {
			CellInfo cellInfo = (CellInfo) object;
			Utils.safetyStartActivity(cellInfo.getIntent(), this);
		}
		
	} 
	
	
	
	/** 选择哪种类型的item 对话框 */
	private AlertDialog m_DialogSelItem;
	
	/** 自定义的对话框，用来显示应用程序 */
	private AlertDialog m_DialogAppItem;
	
	/** 应用程序数据源 */
	private List<AppInfo> m_ListAppInfoDialog;
	
	/** 应用程序item适配器 */
	private DiaglogAppItemAdapter m_DiaglogAppItemAdapter;
	
	/**
	 * 显示添加哪种类型 {shortcut/widget} 的item到launcher桌面上
	 */
	private void showAddItemDialog(final Context context) {
		if (m_DialogSelItem == null) {
			final AlertDialog.Builder build = new AlertDialog.Builder(context);
			final String[] content = context.getResources().getStringArray(R.array.launcher_itemdialog_content);
			build.setTitle(context.getResources().getString(R.string.launcher_dailog_sel_title));
			build.setItems(content, new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == 0) { //应用程序
						showAppItemDialog(context);
					} else if ( which == 1) {	//widget
						m_WidgetManager.selectWidgets();
					}
				}
				
			});
			m_DialogSelItem = build.create();
		}
		m_DialogSelItem.show();
	}
	
	/**
	 * 显示all app items
	 * @param context
	 * @param data	数据源
	 */
	private void showAppItemDialog(final Context context) {
		if (m_DialogAppItem ==  null) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(context);
			final View view = View.inflate(context, R.layout.dialog_appitem_view, null);
			final GridView gridView = (GridView) view.findViewById(R.id.gridview);
			m_ListAppInfoDialog = m_AppManager.getApp(AppManager.FILTER_THIRD_APP);
			
			m_DiaglogAppItemAdapter = new DiaglogAppItemAdapter(context, m_ListAppInfoDialog);
			gridView.setOnItemClickListener(new AppItemClickListener(m_ListAppInfoDialog, MainActivity.this) );
			gridView.setAdapter(m_DiaglogAppItemAdapter);
			
			builder.setView(view);

			m_DialogAppItem = builder.create();
			
		}
		
		m_DialogAppItem.show();
	}
	
	
	
	
	public DragLayer getDragLayer() {
		return m_DragLayer;
	}
	
	public Workspace getWorkspace() {
		return m_Workspace;
	}
	
	/**
	 * 获取显示所有程序的自定义对话框
	 * @return
	 */
	public Dialog getDialogAppItems() {
		return m_DialogAppItem;
	}
	
	
	/**
	 * 监听app item项的点击
	 * @author baoxing
	 *
	 */
	static class AppItemClickListener implements AdapterView.OnItemClickListener {
		private List<AppInfo> m_Data;
		private MainActivity m_Launcher;
		
		public AppItemClickListener(List<AppInfo> data, MainActivity launcher) {
			m_Data = data;
			m_Launcher = launcher;
		}
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			final AppInfo appInfo = m_Data.get(position);
			m_Launcher.addViewInScreen(appInfo);
			m_Launcher.getDialogAppItems().dismiss();
		}
		
	}	
	
	/**
	 * 显示所有的app item的gridview的适配器
	 * @author baoxing
	 *
	 */
	static class DiaglogAppItemAdapter extends BaseAdapter {

//		private SoftReference<Context> m_Context;		//TODO 这样里是否有必要这样处理
		private Context m_Context;
		
		/** 数据源 */
		private List<AppInfo> m_ListData;
		
		public DiaglogAppItemAdapter(Context context, List<AppInfo> listData) {
//			m_Context = new SoftReference<Context>(context);
			m_Context = context;
			m_ListData = listData;
		}
		
		@Override
		public int getCount() {
			return m_ListData.size();
		}

		@Override
		public Object getItem(int position) {
			return m_ListData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			ViewHolder holder; 
			
			if (view == null) {
				view = View.inflate(m_Context, R.layout.dialog_appitem_gridview_item, null);
				holder = new ViewHolder(view);
				view.setTag(holder);
			}
			final AppInfo appInfo = m_ListData.get(position);
			holder = (ViewHolder) view.getTag();
			holder.icon.setImageDrawable(appInfo.getIcon() );
			holder.label.setText(appInfo.getLabelName() );
			
			return view;
		}
		static class ViewHolder {
			public ImageView icon;
			public TextView label;
			
			public ViewHolder(View view) {
				icon = (ImageView) view.findViewById(R.id.image_button);
				label = (TextView) view.findViewById(R.id.txv_label);
			}
			
		}
		
	}

	
	
	
}
