package com.xx.mylauncher;

import java.util.ArrayList;
import java.util.List;

import com.xx.mylauncher.dao.CellInfoEntity;

import android.content.Context;

public class LauncherDBManager {
	
	private LauncherDBDAO m_Dao;
	
	private static LauncherDBManager m_Instance;
	
	/*
	 * item view 的类型
	 * cellType
	 */
	public static final int SHORT_CUT = 1;
	public static final int WIDGET = 2;
	/*
	 * item view放的位置Workspace/HotSeat
	 * cellLocation
	 */
	public static final int WORKSPACE = 1;
	public static final int HOTSEAT = 2;

	private static final String TAG = "LauncherDBManager";
	
	
	private LauncherDBManager(Context context) {
		m_Dao = LauncherDBDAO.getInstance(context);
	}
	
	
	public static synchronized LauncherDBManager getInstance(Context context) {
		if (m_Instance == null) {
			m_Instance = new LauncherDBManager(context);
		}
		
		return m_Instance;
	}
	
	/**
	 * 添加widget
	 * @param cellInfo
	 * @param cellX
	 * @param cellY
	 * @param cellHSpan
	 * @param cellVSpan
	 * @param appWidgetId
	 */
	public void addWidget(final CellInfo cellInfo) {
		final CellInfoEntity entity = new CellInfoEntity();
		entity.setCellX(cellInfo.getCellX());
		entity.setCellY(cellInfo.getCellY());
		entity.setCellHSpan(cellInfo.getCellHSpan());
		entity.setCellVSpan(cellInfo.getCellVSpan());
		entity.setHotseatCellX(cellInfo.getHotSeatCellX());
		entity.setHotseatCellY(cellInfo.getHotSeatCellY());
		entity.setWidgetid(cellInfo.getWidgetId());
		entity.setScreen(cellInfo.getScreen());
		entity.setCellType(WIDGET);
		entity.setCellLocation(WORKSPACE);
		long id = m_Dao.insertCellInfo(entity);
		
		cellInfo.setId(id);
	}
	
	/**
	 * 添加应用程序
	 * @param cellInfo
	 * @param appInfo
	 */
	public void addShortCut(final CellInfo cellInfo, final AppInfo appInfo) {
		final CellInfoEntity entity = new CellInfoEntity();
		entity.setCellHSpan(cellInfo.getCellHSpan());
		entity.setCellLocation(WORKSPACE);
		entity.setCellType(SHORT_CUT);
		entity.setCellVSpan(cellInfo.getCellVSpan());
		entity.setCellX(cellInfo.getCellX());
		entity.setCellY(cellInfo.getCellY());
		entity.setClsName(appInfo.getClsName());
		entity.setIconName(cellInfo.getIconName());
		entity.setLabelName(appInfo.getLabelName());
		entity.setPkgName(appInfo.getPkgName());
		entity.setScreen(cellInfo.getScreen());
		entity.setHotseatCellX(cellInfo.getHotSeatCellX());
		entity.setHotseatCellY(cellInfo.getHotSeatCellY());
		entity.setWidgetid(cellInfo.getWidgetId());
		
		long id = m_Dao.insertCellInfo(entity);
		
		cellInfo.setId(id);
	}
	
	/**
	 * 删除一个item view
	 * @param id
	 */
	public void deleteCell(long id) {
		m_Dao.deleteCellInfoById(id);
	}
	
	/**
	 * 更新拖动后的信息
	 * @param cellInfo
	 */
	public void updateDragInfo(final CellInfo cellInfo) {
		final long id = cellInfo.getId();
		final CellInfoEntity entity = m_Dao.loadCellInfoEntity(id);
		
		entity.setCellHSpan(cellInfo.getCellHSpan());
		entity.setCellLocation(cellInfo.getLocation()==CellInfo.CellLocation.WORKSPACE ? WORKSPACE : HOTSEAT);
		entity.setCellType(cellInfo.getType()==CellInfo.CellType.SHORT_CUT ? SHORT_CUT : WIDGET);
		entity.setCellVSpan(cellInfo.getCellVSpan());
		entity.setCellX(cellInfo.getCellX());
		entity.setCellY(cellInfo.getCellY());
		entity.setHotseatCellX(cellInfo.getHotSeatCellX());
		entity.setHotseatCellY(cellInfo.getHotSeatCellY());
		entity.setScreen(cellInfo.getScreen());
		entity.setWidgetid(cellInfo.getWidgetId());
		
		Utils.log(TAG, "cellX=%d, cellY=%d, cellHSpan=%d, cellVSpan=%d, cellHotseatX=%d, cellHotseatY=%d", 
				cellInfo.getCellX(), cellInfo.getCellY(), cellInfo.getCellHSpan(), cellInfo.getCellVSpan(), cellInfo.getHotSeatCellX(), cellInfo.getHotSeatCellY());
		
		m_Dao.updateCellInfoEntity(entity);
	}
	
	/**
	 * 加载桌面上所有的内容
	 * @return
	 */
	public List<CellInfoEntity> loadAllItemViews() {
		List<CellInfoEntity> list;
		list = m_Dao.loadAllCellInfos();
		
		return list;
	}
	
	/**
	 * 根据id返回一条记录
	 * @param id
	 * @return
	 */
	public CellInfoEntity loadEntity(long id) {
		return m_Dao.loadCellInfoEntity(id);
	}
	
	
}
