package com.xx.mylauncher;

import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.xx.mylauncher.dao.CellInfoEntity;
import com.xx.mylauncher.dao.CellInfoEntityDao;
import com.xx.mylauncher.dao.DaoMaster;
import com.xx.mylauncher.dao.DaoMaster.DevOpenHelper;
import com.xx.mylauncher.dao.DaoSession;

import de.greenrobot.dao.query.DeleteQuery;
import de.greenrobot.dao.query.QueryBuilder;

/**
 * 数据库管理，增删改查
 * @author baoxing
 *
 */
public class LauncherDBDAO {

	private static final String TAG = "LauncherDBManager";
	
	private static DaoSession m_DaoSession;
	private static Context m_Context;
	private static LauncherDBDAO m_Instance;
	private SQLiteDatabase m_Db;
	
	private LauncherDBDAO(Context context) {
		if (m_DaoSession == null) {
			DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(context.getApplicationContext(), "launcher-db", null);
			m_Db = devOpenHelper.getWritableDatabase();
			m_DaoSession = new DaoMaster(m_Db).newSession();
		}
	}
	
	public static synchronized LauncherDBDAO getInstance(Context context ) {
		if (m_Instance == null) {
			m_Instance = new LauncherDBDAO(context);
			m_Context = context;
		}
		
		QueryBuilder.LOG_SQL = true;
		QueryBuilder.LOG_VALUES = true;
		
		return m_Instance;
	}
	
	/**
	 * 加入了一个格子
	 * @param cellInfoEntity
	 */
	public long insertCellInfo(CellInfoEntity cellInfoEntity) {
		CellInfoEntityDao dao = m_DaoSession.getCellInfoEntityDao();
		
		Utils.log(TAG, "insert a new cell");
		return dao.insert(cellInfoEntity);
	}
	
	/**
	 * 删除了一个格子
	 */
	public void deleteCellInfo(CellInfoEntity cellInfoEntity) {
		CellInfoEntityDao dao = m_DaoSession.getCellInfoEntityDao();
		dao.delete(cellInfoEntity);
		Utils.log(TAG, "delete a  cell");
	}
	
	public void deleteCellInfoByPkgName(String pkgName) {
		CellInfoEntityDao dao = m_DaoSession.getCellInfoEntityDao();
		QueryBuilder<CellInfoEntity> qb = dao.queryBuilder();
		DeleteQuery<CellInfoEntity> bd = qb.where(CellInfoEntityDao.Properties.PkgName.eq(pkgName) ).buildDelete();
		bd.executeDeleteWithoutDetachingEntities();
		
		Utils.log(TAG, "delete a cell by pkgName");
	}
	
	public void deleteCellInfoById(Long id) {
		CellInfoEntityDao dao = m_DaoSession.getCellInfoEntityDao();
		dao.deleteByKey(id);
		
		Utils.log(TAG, "elete a cell by id");
	}
	
	/**
	 * 插入或更新一个格子信息
	 * @param cellInfoEntity
	 */
	public long updateOrInsert(CellInfoEntity cellInfoEntity) {
		CellInfoEntityDao dao = m_DaoSession.getCellInfoEntityDao();
		return dao.insertOrReplace(cellInfoEntity);
	}
	
	public void updateCellInfoEntity(CellInfoEntity cellInfoEntity) {
		CellInfoEntityDao dao = m_DaoSession.getCellInfoEntityDao();
		dao.update(cellInfoEntity);
	}
	
	/**
	 * 获取所有的格子信息
	 * @return
	 */
	public List<CellInfoEntity> queryAllCells() {
		CellInfoEntityDao dao = m_DaoSession.getCellInfoEntityDao();
		List<CellInfoEntity> list = dao.queryBuilder().list();
		
		Utils.log(TAG, "全部格子：%s", list.toString() );
		return list;
	}
	
	public CellInfoEntity queryCellInfoEntityByPkgName(String pkgName) {
		CellInfoEntityDao dao = m_DaoSession.getCellInfoEntityDao();
		List<CellInfoEntity> list = dao.queryBuilder().where(CellInfoEntityDao.Properties.PkgName.eq(pkgName) ).list();
		if (list!=null && list.size()>0) {
			if (list.size()>1) {
				Utils.logE(TAG, "这里说明包名不唯一，会导致查询错误!");
			}
			return list.get(0);
		}
		
		return null;
	}
	
	/**
	 * 根据id加载一条记录
	 * @param id
	 * @return
	 */
	public CellInfoEntity loadCellInfoEntity(long id) {
		CellInfoEntityDao dao = m_DaoSession.getCellInfoEntityDao();
		CellInfoEntity cellInfoEntity = dao.load(id);
		
		return cellInfoEntity;
	}
	
	/**
	 * 加载所有的格子信息
	 * @return
	 */
	public List<CellInfoEntity> loadAllCellInfos() {
		CellInfoEntityDao dao = m_DaoSession.getCellInfoEntityDao();
		
		return dao.loadAll();
	}
	
}
