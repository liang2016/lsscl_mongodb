/*
    LssclM2M - http://www.lsscl.com
    Copyright (C) 2006-2011 Lsscl ES Technologies Inc.
     
    
     
     
     
     

     
     
     
     

     
    
 */
package com.serotonin.mango.db.dao;

import java.util.List;

import javax.sql.DataSource;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.serotonin.db.DaoUtils;
import com.serotonin.mango.Common;

public class BaseDao extends DaoUtils {
	public static SQLServerDataSource ds = new SQLServerDataSource();
	static{
		//117
		String host = "localhost";
		String user = "sa";
		String password = "lssclM2M";
		String dbname = "irM2M2_IBox";
		//lsscl
//		host = "www.lsscl.com";
//		user = "lsscl";
//		password = "123456";
		
//		host = "10.192.206.66";
//		dbname = "irM2M2_IBox";
//		password = "lssclM2M";
		ds.setURL("jdbc:sqlserver://"+host+":1433; DatabaseName="+dbname);
		ds.setUser(user);
		ds.setPassword(password);
		
	}
    /**
     * Public constructor for code that needs to get stuff from the database.
     */
    public BaseDao() {
//		super(ds);
		super(Common.ctx.getDatabaseAccess().getDataSource());
    }

    protected BaseDao(DataSource dataSource) {
        super(dataSource);
    }

    //
    // Convenience methods for storage of booleans.
    //
    protected static String boolToChar(boolean b) {
        return b ? "Y" : "N";
    }

    protected static boolean charToBool(String s) {
        return "Y".equals(s);
    }

    protected void deleteInChunks(String sql, List<Integer> ids) {
        int chunk = 1000;
        for (int i = 0; i < ids.size(); i += chunk) {
            String idStr = createDelimitedList(ids, i, i + chunk, ",", null);
            ejt.update(sql + " (" + idStr + ")");
        }
    }

    //
    // XID convenience methods
    //
    protected String generateUniqueXid(String prefix, String tableName) {
        String xid = Common.generateXid(prefix);
        while (!isXidUnique(xid, -1, tableName))
            xid = Common.generateXid(prefix);
        return xid;
    }

    protected boolean isXidUnique(String xid, int excludeId, String tableName) {
        return ejt.queryForInt("select count(*) from " + tableName + " where xid=? and id<>?", new Object[] { xid,
                excludeId }) == 0;
    }
}
