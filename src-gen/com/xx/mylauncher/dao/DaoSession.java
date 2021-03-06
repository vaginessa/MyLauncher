package com.xx.mylauncher.dao;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;

import com.xx.mylauncher.dao.CellInfoEntity;

import com.xx.mylauncher.dao.CellInfoEntityDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see de.greenrobot.dao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig cellInfoEntityDaoConfig;

    private final CellInfoEntityDao cellInfoEntityDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        cellInfoEntityDaoConfig = daoConfigMap.get(CellInfoEntityDao.class).clone();
        cellInfoEntityDaoConfig.initIdentityScope(type);

        cellInfoEntityDao = new CellInfoEntityDao(cellInfoEntityDaoConfig, this);

        registerDao(CellInfoEntity.class, cellInfoEntityDao);
    }
    
    public void clear() {
        cellInfoEntityDaoConfig.getIdentityScope().clear();
    }

    public CellInfoEntityDao getCellInfoEntityDao() {
        return cellInfoEntityDao;
    }

}
