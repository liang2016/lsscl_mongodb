package com.serotonin.mango.db.change;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.db.spring.ExtendedJdbcTemplate;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.MSSQLAccess;
import com.serotonin.mango.db.dao.DataSourceDao;
import com.serotonin.mango.vo.dataSource.DataSourceVO;

public class CopyOfTableHelper {
	private static final Log LOG = LogFactory.getLog(CopyOfTableHelper.class);
	private final String TABLE_PREFIX = "pointValues_";

	/**
	 * 根据数据点id获取表名
	 * 
	 * @param dataPointId
	 *            数据点id
	 * @return 表名
	 */
	public String getTableName(int dataPointId) {
		return TABLE_PREFIX + dataPointId;
	}

	/**
	 * 创建表的sql脚本
	 * 
	 * @param dataPointId
	 *            数据点id
	 * @return sql脚本
	 */
	public String[] createTableSQL(int dataPointId) {
		String tableName = getTableName(dataPointId);
		String sql[] = {
				"create table "
						+ tableName
						+ " (id bigint not null identity,dataType int not null,pointValue float,ts bigint not null, primary key (id));",
				"create index pointValuesIdx2 on " + tableName + " (ts);" };
		return sql;
	}

	public void runSql(String[] script, int pointId) {
		OutputStream out = System.out;
		try {
			new MSSQLAccess(null).runScript(script, out);
			LOG.info("Info create table pointValues_" + pointId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOG.info("Failed to create table pointValues_" + pointId);
			e.printStackTrace();
		}
		try {
			out.flush();
			out.close();
		} catch (IOException e) {
			LOG.info("Failed to out close");
			e.printStackTrace();
		}
	}
	
}
