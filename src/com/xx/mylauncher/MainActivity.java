package com.xx.mylauncher;

import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Random;

import com.xx.mylauncher.CellLayout.LayoutParams;
import com.xx.mylauncher.dao.CellInfoEntity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity  extends LauncherBaseActivity
								implements View.OnLongClickListener, View.OnClickListener, View.OnTouchListener {
	
	private static final String TAG = "MainActivity";
	
	private DragLayer m_DragLayer;

	private Workspace m_Workspace;
	
	private DragController m_DragController;
	
	private HotSeat m_HotSeat;
	
	private AppManager m_AppManager;

	private WidgetManager m_WidgetManager;
	
	/** 多屏滑动指示器 */
	private SlideIndicator m_SlideIndicator;
	
	private DeleteZone m_DeleteZone;
	
	private LauncherDBManager m_LauncherDBManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tt);
		
		m_DragLayer = (DragLayer) findViewById(R.id.dragLayout);
		m_Workspace = (Workspace) findViewById(R.id.workspace);
		m_HotSeat = (HotSeat) findViewById(R.id.hotseat);
		m_SlideIndicator = (SlideIndicator) findViewById(R.id.slideIndicator);
		m_DeleteZone = (DeleteZone) findViewById(R.id.deletezone);
		m_DragController = new DragController(this, this );
		
		m_DragLayer.setDragController(m_DragController);
		m_DragController.registerDropTarget(m_Workspace);	//别忘记注册添加
		m_DragController.registerDropTarget(m_HotSeat);
		m_DragController.registerDropTarget(m_DeleteZone);
		m_DragController.registerDragListener(m_DeleteZone);
		m_DragController.registerDragListener(m_DragLayer);
//		m_CellLayout.setLauncher(this);	//TODO 先这样添加
		m_Workspace.setDragController(m_DragController);
		m_Workspace.setDragLayer(m_DragLayer);
//		m_Workspace.setOnLongClickListener(this);
		m_HotSeat.setDragLayer(m_DragLayer);
		m_HotSeat.setLauncher(this);
		m_DeleteZone.setLauncher(this);
		
		m_LauncherDBManager = LauncherDBManager.getInstance(this);
		
		m_Workspace.post(new Runnable() {
			
			@Override
			public void run() {
				m_Workspace.setCellLayoutLongPressListener(MainActivity.this);
				m_Workspace.setCellLayoutAttachLauncher(MainActivity.this);
			}
		});
		
		
		m_AppManager = AppManager.getInstance(this);
		m_WidgetManager = WidgetManager.getInstance(this, this);
		
		
		initRes();
		
	}

	private void initRes() {
		ViewConfiguration viewConfiguration = ViewConfiguration.get(this);
		iLimitDistanceScrollFlag = viewConfiguration.getScaledTouchSlop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (m_WidgetManager != null) {
			m_WidgetManager.onResume();
		}
		
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			loadAndAdapterItemViews();			
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
		final CellLayout curCellLayout = m_Workspace.getCurCellLayout();
		
		ShortCutView2 shortcutView = new ShortCutView2(getBaseContext());
		shortcutView.setIcon(appInfo.getIcon());
		shortcutView.setLabel(appInfo.getLabelName());
		
		int cellHSpan = 1;
		int cellVSpan = 1;
		List<int[]> result = curCellLayout.isAcceptAddChild(cellHSpan, cellVSpan);
		
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
		final CellLayout curCellLayout = m_Workspace.getCurCellLayout();
		final int iCurScreen = m_Workspace.getCurScreenIndicator();
				
		CellInfo cellInfo = new CellInfo();
		cellInfo.setScreen(iCurScreen);
		cellInfo.setCellHSpan(cellHSpan);
		cellInfo.setCellVSpan(cellVSpan);
		cellInfo.setCellX(cellX);
		cellInfo.setCellY(cellY);
		cellInfo.setIconName(appInfo.getLabelName());
		cellInfo.setIntent(appInfo.getIntent());
		cellInfo.setType(CellInfo.CellType.SHORT_CUT);
		cellInfo.setLocation(CellInfo.CellLocation.WORKSPACE);
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
		view.setOnTouchListener(this);
//		m_CellLayout.addView(view);
		curCellLayout.addView(view);
		m_LauncherDBManager.addShortCut(cellInfo, appInfo);
	}
	
	/**
	 * 加载应用程序/Widget到屏幕中，从数据中加载
	 * @param cellInfo
	 * @param view
	 */
	private void addViewInCellLayout(final CellInfo cellInfo, final View view) {
		Utils.log(TAG, "load workspace item view");
		
		final int iScreen = cellInfo.getScreen();
		final CellLayout cellLayout = m_Workspace.getSpecifyCellLayout(iScreen);
		cellInfo.setView(view);
		view.setTag(cellInfo);
//		
//		if (cellInfo.getType() == CellInfo.CellType.SHORT_CUT) {
//			view.post(new ViewPostSetDrawableBackground(this, view) );	
//		}
//		
		CellLayout.LayoutParams lp = new CellLayout.LayoutParams();
		lp.cellX = cellInfo.getCellX();
		lp.cellY = cellInfo.getCellY();
		lp.cellHSpan = cellInfo.getCellHSpan();
		lp.cellVSpan = cellInfo.getCellVSpan();
		view.setLayoutParams(lp);
		view.setFocusable(true);
		
		view.setOnClickListener(this);
		view.setOnTouchListener(this);
		view.setOnLongClickListener(this);

		cellLayout.flagOcuped(cellInfo);
		cellLayout.addView(view);
	}
	
	/**
	 * 加载应用程序到HotSeat中，从数据库中加载
	 * @param cellInfo
	 * @param view
	 */
	private void addViewInHotSeat(final CellInfo cellInfo, final View view) {
		Utils.log(TAG, "load hotseat item view");
		
		final HotSeat hotSeat = m_HotSeat;
		cellInfo.setView(view);
		view.setTag(cellInfo);
//		view.post(new ViewPostSetDrawableBackground(this, view) );
		CellLayout.LayoutParams lp = new CellLayout.LayoutParams();
		lp.cellX = cellInfo.getCellX();
		lp.cellY = cellInfo.getCellY();
		lp.cellHSpan = cellInfo.getCellHSpan();
		lp.cellVSpan = cellInfo.getCellVSpan();
		view.setLayoutParams(lp);
		
		view.setFocusable(true);
		view.setOnLongClickListener(this);
		view.setOnClickListener(this);
		view.setOnTouchListener(this);
		
		hotSeat.flagOcuped(cellInfo);
		hotSeat.addView(view);
	}
	
	
	/**
	 * 添加Widget到Launcher中
	 * @param widgetView
	 */
	private void addWidgetInCellLayout(final View widgetView, final AppWidgetProviderInfo info, int cellX, int cellY, int cellHSpan, int cellVSpan, final int widgetId) {
		final CellLayout curCellLayout = m_Workspace.getCurCellLayout();
		final int iCurScreen = m_Workspace.getCurScreenIndicator();
		
		CellInfo cellInfo = new CellInfo();
		cellInfo.setScreen(iCurScreen);
		cellInfo.setCellHSpan(cellHSpan);
		cellInfo.setCellHSpan(cellHSpan);
		cellInfo.setCellVSpan(cellVSpan);
		cellInfo.setCellX(cellX);
		cellInfo.setCellY(cellY);
		cellInfo.setIconName("");
		cellInfo.setType(CellInfo.CellType.WIDGET);
		cellInfo.setLocation(CellInfo.CellLocation.WORKSPACE);
		cellInfo.setView(widgetView);
		cellInfo.setWidgetId(widgetId);
		widgetView.setTag(cellInfo);
		CellLayout.LayoutParams lp = new CellLayout.LayoutParams();
		lp.cellX = cellX;
		lp.cellY = cellY;
		lp.cellHSpan = cellHSpan;
		lp.cellVSpan = cellVSpan;
		widgetView.setLayoutParams(lp);
//		widgetView.setFocusable(true);
		widgetView.setOnLongClickListener(this);
		
		curCellLayout.addView(widgetView);
		m_LauncherDBManager.addWidget(cellInfo);
		
	}
	
	/**
	 * 添加Widget
	 * @param widgetView
	 * @param info
	 * @param appWidgetId
	 */
	void addViewInScreen(final View widgetView, final AppWidgetProviderInfo info, final int appWidgetId) {
		final CellLayout curCellLayout = m_Workspace.getCurCellLayout();
		
		final int iCellSize = curCellLayout.getCellSize();
		final int iHSpace = curCellLayout.getHorizontalSpace();
		final int iVSpace = curCellLayout.getVerticalSpace();
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
		
		List<int[]> result = curCellLayout.isAcceptAddChild(iHSpan, iVSpan);
		
		if (result.size() > 0) {
			int[] item = result.get(new Random().nextInt(result.size()) );
			addWidgetInCellLayout(widgetView, info, item[0], item[1], iHSpan, iVSpan, appWidgetId);
		} else {
			m_WidgetManager.deleteAppWidgetId(appWidgetId);
			Toast.makeText(MainActivity.this, getResources().getString(R.string.launcher_no_much_space), Toast.LENGTH_SHORT).show();
		}
		
		Utils.log(TAG, "添加的Widget: iHSpan=%d, iVSpan=%d", iHSpan, iVSpan);
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
		
		if (tag instanceof CellInfo) {
			CellInfo cellInfo = (CellInfo) tag;
			Utils.log(true, TAG, "图标项或widget项. Type is %s", cellInfo.getType().name() );
			
			final DragSource dragSource;
			if (cellInfo.getLocation() == CellInfo.CellLocation.WORKSPACE ) {
				dragSource = m_Workspace;
				
			} else if (cellInfo.getLocation() == CellInfo.CellLocation.HOTSEAT) {
				dragSource = m_HotSeat;
				
			} else {
				dragSource = null;
			}
			
			m_DragController.startDrag(v, dragSource, v.getTag(), Constant.DRAG_MOVE);
			
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
			if (cellInfo.getType() == CellInfo.CellType.SHORT_CUT) {
				v.post(new ViewOnClickRunnable(this, v));
			}
			
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
	
	public DragController getDragController() {
		return m_DragController;
	}
	
	public SlideIndicator getSlideIndicator() {
		return m_SlideIndicator;
	}
	
	public HotSeat getHotSeat() {
		return m_HotSeat;
	}
	
	public LauncherDBManager getLauncherDBManager() {
		return m_LauncherDBManager;
	}
	
	public WidgetManager getWidgetManager() {
		return m_WidgetManager;
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

	private boolean m_bIsLoaded = false;
	
	/**
	 * 加载所有的item view
	 */
	private void loadAndAdapterItemViews() {
		if (m_bIsLoaded) {
			return;
		}
		
		Utils.log(TAG, "loadAndAdapterItemViews");
		
		m_bIsLoaded = true;
		final List<CellInfoEntity> list = m_LauncherDBManager.loadAllItemViews();
		
		Utils.log(TAG, list.toString());
		
		List<CellInfo> resultList = null;
		if (list != null) {
			//dto
			resultList = Utils.convertDbinfosToCellInfo(list);
		}
		
		if (resultList != null ) {
			//adapter
			PackageManager pm = getPackageManager();
			for (CellInfo cellInfo : resultList) {
				final CellInfo.CellLocation location = cellInfo.getLocation();
				final CellInfo.CellType type = cellInfo.getType();
				
				if (location == CellInfo.CellLocation.WORKSPACE) {
					if (type == CellInfo.CellType.SHORT_CUT) {
						// 根据包名获取icon
						final ShortCutView2 itemView = new ShortCutView2(this);
						itemView.setIcon(m_AppManager.getIcon(cellInfo.getPkgName(), pm));
						itemView.setLabel(cellInfo.getIconName());
						
						addViewInCellLayout(cellInfo, itemView);
						
					} else if (type == CellInfo.CellType.WIDGET) {
						final int widgetId = cellInfo.getWidgetId();
						final View widgetView = m_WidgetManager.getWidgetView(widgetId);
						addViewInCellLayout(cellInfo, widgetView);
						
					}
					
				} else if (location == CellInfo.CellLocation.HOTSEAT) {
					if (type == CellInfo.CellType.SHORT_CUT) {
						final ShortCutView2 itemView = new ShortCutView2(this);
						itemView.setIcon(m_AppManager.getIcon(cellInfo.getPkgName(), pm));
						itemView.setLabel(cellInfo.getIconName());
						itemView.setLabelVisibility(View.GONE);
						
						addViewInHotSeat(cellInfo, itemView);
					} else if (type == CellInfo.CellType.WIDGET) {
						Utils.logE(TAG, "不应该出现这种情况");
					}
					
				}
				
			}	//end for
			
			
		}	//end not null
		
		
	}	//end func
	
	
	
	static class ViewOnClickRunnable implements Runnable {
		
		private static final long ANIM_DURATION = 250;
		
		private SoftReference<Context> m_SrContext;
		private View m_View;
		
		public ViewOnClickRunnable(Context context, View view) {
			m_SrContext = new SoftReference<Context>(context);
			m_View = view;
		}
		
		@Override
		public void run() {
			AnimatorSet animSet= new AnimatorSet();
			ObjectAnimator anim1 = ObjectAnimator.ofFloat(m_View, "scaleX", 1.0f, 0.8f, 1.0f);
			ObjectAnimator anim2 = ObjectAnimator.ofFloat(m_View, "scaleY", 1.0f, 0.8f, 1.0f);
			animSet.setDuration(ANIM_DURATION);
			animSet.setInterpolator(new BounceInterpolator() );
			animSet.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					if (m_View instanceof ShortCutView2) {
						final CellInfo cellInfo = (CellInfo) m_View.getTag();
						Utils.safetyStartActivity(cellInfo.getIntent(), m_SrContext.get() );	
					}
					
				}
			});
			
			if (m_ViewOnTouchRunnable != null) {
				m_View.removeCallbacks(m_ViewOnTouchRunnable);
			}
			
			animSet.playTogether(anim1, anim2);
			animSet.start();
		}
		
	}
	
	static class ViewOnTouchRunnable implements Runnable {
		
		private static final long ANIM_DURATION = 250;
		
		private SoftReference<Context> m_SrContext;
		private View m_View;
		
		public ViewOnTouchRunnable(Context context, View view) {
			m_SrContext = new SoftReference<Context>(context);
			m_View = view;
		}
		
		@Override
		public void run() {
			AnimatorSet animSet= new AnimatorSet();
			ObjectAnimator anim1 = ObjectAnimator.ofFloat(m_View, "scaleX", 1.0f, 0.87f, 1.0f);
			ObjectAnimator anim2 = ObjectAnimator.ofFloat(m_View, "scaleY", 1.0f, 0.87f, 1.0f);
			animSet.setDuration(ANIM_DURATION);
			animSet.setInterpolator(new BounceInterpolator() );
			animSet.addListener(new AnimatorListenerAdapter() {
			});
			
			animSet.playTogether(anim1, anim2);
			animSet.start();
		}
		
	}
	
	
	private static ViewOnTouchRunnable m_ViewOnTouchRunnable;
	private int iLimitDistanceScrollFlag;
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		final int iAction = event.getAction();
		
		if (iAction == MotionEvent.ACTION_DOWN ) {
			m_ViewOnTouchRunnable = new ViewOnTouchRunnable(this, v);
			v.postDelayed(m_ViewOnTouchRunnable, 100);
			
		}
		
		
		
		return false;
	}
	
	
}
