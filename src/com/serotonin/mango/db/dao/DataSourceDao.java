/*
    LssclM2M - http://www.lsscl.com
    Copyright (C) 2006-2011 Lsscl ES Technologies Inc.
     
    
     
     
     
     

     
     
     
     

     
    
 */
package com.serotonin.mango.db.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.sql.DataSource;

import org.apache.commons.collections.map.HashedMap;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.lsscl.app.util.StringUtil;
import com.serotonin.db.spring.ExtendedJdbcTemplate;
import com.serotonin.db.spring.GenericRowMapper;
import com.serotonin.db.spring.GenericTransactionCallback;
import com.serotonin.mango.Common;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.rt.event.type.EventType;
import com.serotonin.mango.util.ChangeComparable;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.ResultData;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.dataSource.modbus.ModbusIpDataSourceVO;
import com.serotonin.mango.vo.event.PointEventDetectorVO;
import com.serotonin.util.SerializationHelper;
import com.serotonin.util.StringUtils;
import com.serotonin.web.i18n.LocalizableMessage;

public class DataSourceDao extends BaseDao {
	private static final String DATA_SOURCE_SELECT = "select id, xid, name, data,factoryId from dataSources ";

	public DataSourceDao() {
		super();
	}

	public DataSourceDao(DataSource dataSource) {
		super(dataSource);
	}

	public List<DataSourceVO<?>> getDataSources() {
		User user = Common.getUser();
		List<DataSourceVO<?>> dss = null;
		if (user == null || user.getCurrentScope() == null) {
			dss = query(DATA_SOURCE_SELECT, new DataSourceRowMapper());
		} else {
			dss = query(DATA_SOURCE_SELECT + " where factoryId =? ",
					new Object[] { user.getCurrentScope().getId() },
					new DataSourceRowMapper());
		}
		Collections.sort(dss, new DataSourceNameComparator());
		return dss;
	}

	// 根据数据类型查询数据源
	public List<DataSourceVO<?>> getDataSourcesForPort(int dataType) {
		User user = Common.getUser();
		List<DataSourceVO<?>> dss = null;
		dss = query(DATA_SOURCE_SELECT + " where dataSourceType =? ",
				new Object[] { dataType }, new DataSourceRowMapper());
		Collections.sort(dss, new DataSourceNameComparator());
		return dss;
	}

	public List<Map<String, Object>> getDataSourceByMap(
			Map<String, Object> queryParam) {
		List<Map<String, Object>> maps = null;
		maps = query(buildQuery(queryParam), null, new ResultData());

		String port = (String) queryParam.get("port");
		if (port != null) {
			List<Map<String, Object>> ports = new ArrayList<Map<String, Object>>();
			for (Map<String, Object> m : maps) {
				ModbusIpDataSourceVO mv = (ModbusIpDataSourceVO) m.get("data");
				if (port.equals(mv.getPort() + "")) {
					ports.add(m);
				}
			}
			maps = ports;
		}
		return maps;
	}

	private String buildQuery(Map<String, Object> queryParam) {
		StringBuilder sb = new StringBuilder(
				"select id,name,scopeId,scopename,address,data from ("
						+ "select d.id, d.xid, d.name, d.data,d.dataSourceType,"
						+ "s.id as 'scopeId',s.scopename,s.address,"
						+ "row_number()over (order by d.id) as 'row' from dataSources d "
						+ "left join scope s on d.factoryId = s.id where 1=1 " +
						"and dataSourceType=3");

		String port = (String) queryParam.get("port");
		if (port != null && !"".equals(port)) {
			sb.append(") as t1 ");
			return sb.toString();
		}
		/**
		 * 条件查询
		 */
		String name = (String) queryParam.get("name");
		if (name != null && !"".equals(name)) {
			sb.append(" and (name like '%" + name + "%' or scopename like '%"
					+ name + "%') ");
		}
		sb.append(") as t1");
		/**
		 * 分页
		 */
		Integer page = (Integer) queryParam.get("page");
		Integer pageSize = (Integer) queryParam.get("pageSize");
		page = page != null ? page : 1;
		pageSize = pageSize != null ? pageSize : 10;
		int startIndex = (page - 1) * pageSize;
		int endIndex = startIndex + pageSize;
		sb.append(" where row>" + startIndex + " and row<=" + endIndex);
		
		return sb.toString();
	}

	public int getCountByMap(Map<String, Object> queryParam) {
		StringBuilder sb = new StringBuilder(
				"select count(id) from ("
						+ "select d.id, d.xid, d.name, d.data,d.dataSourceType,"
						+ "s.id as 'scopeId',s.scopename,s.address,"
						+ "row_number()over (order by d.id) as 'row' from dataSources d "
						+ "left join scope s on d.factoryId = s.id "
						+ ") as t1 where dataSourceType=3 ");

		String port = (String) queryParam.get("port");
		if (port != null && !"".equals(port)) {
			return queryForObject(sb.toString(), null, Integer.class,0);
		}
		
		/**
		 * 条件查询
		 */
		String name = (String) queryParam.get("name");
		if (name != null && !"".equals(name)) {
			sb.append(" and (name like '%" + name + "%' or scopename like '%"
					+ name + "%') ");
		}
		return queryForObject(sb.toString(), null, Integer.class,0);
	}

	static class DataSourceNameComparator implements
			Comparator<DataSourceVO<?>> {
		public int compare(DataSourceVO<?> ds1, DataSourceVO<?> ds2) {
			if (StringUtils.isEmpty(ds1.getName()))
				return -1;
			return ds1.getName().compareToIgnoreCase(ds2.getName());
		}
	}

	public DataSourceVO<?> getDataSource(int id) {
		return queryForObject(DATA_SOURCE_SELECT + " where id=?",
				new Object[] { id }, new DataSourceRowMapper(), null);
	}

	public DataSourceVO<?> getDataSource(String xid) {
		return queryForObject(DATA_SOURCE_SELECT + " where xid=?",
				new Object[] { xid }, new DataSourceRowMapper(), null);
	}

	class DataSourceRowMapper implements GenericRowMapper<DataSourceVO<?>> {
		public DataSourceVO<?> mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			DataSourceVO<?> ds = (DataSourceVO<?>) SerializationHelper
					.readObject(rs.getBlob(4).getBinaryStream());
			ds.setId(rs.getInt(1));
			ds.setXid(rs.getString(2));
			ds.setName(rs.getString(3));
			ds.setFactoryId(rs.getInt(5));
			return ds;
		}
	}

	public String generateUniqueXid() {
		return generateUniqueXid(DataSourceVO.XID_PREFIX, "dataSources");
	}

	public boolean isXidUnique(String xid, int excludeId) {
		return isXidUnique(xid, excludeId, "dataSources");
	}

	// getDataSourceIds
	public List<Integer> getDataSourceIds(int factory) {
		return queryForList("select id from dataSources where factoryId=?",
				new Object[] { factory }, Integer.class);
	}

	public void saveDataSource(final DataSourceVO<?> vo) {
		// Decide whether to insert or update.
		if (vo.getId() == Common.NEW_ID)
			insertDataSource(vo);
		else
			updateDataSource(vo);
	}

	private void insertDataSource(final DataSourceVO<?> vo) {
		vo.setId(doInsert(
				"insert into dataSources (xid, name, dataSourceType, data,factoryId) values (?,?,?,?,?)",
				new Object[] { vo.getXid(), vo.getName(), vo.getType().getId(),
						SerializationHelper.writeObject(vo), vo.getFactoryId() },
				new int[] { Types.VARCHAR, Types.VARCHAR, Types.INTEGER,
						Types.BLOB, Types.INTEGER }));

//		AuditEventType.raiseAddedEvent(AuditEventType.TYPE_DATA_SOURCE, vo);
	}

	@SuppressWarnings("unchecked")
	private void updateDataSource(final DataSourceVO<?> vo) {
		DataSourceVO<?> old = getDataSource(vo.getId());
		ejt.update(
				"update dataSources set xid=?, name=?, data=? where id=?",
				new Object[] { vo.getXid(), vo.getName(),
						SerializationHelper.writeObject(vo), vo.getId() },
				new int[] { Types.VARCHAR, Types.VARCHAR, Types.BLOB,
						Types.INTEGER });

		AuditEventType.raiseChangedEvent(AuditEventType.TYPE_DATA_SOURCE, old,
				(ChangeComparable<DataSourceVO<?>>) vo);
	}

	public void deleteDataSource(final int dataSourceId) {
		DataSourceVO<?> vo = getDataSource(dataSourceId);
		final ExtendedJdbcTemplate ejt2 = ejt;
		if (vo != null) {
			getTransactionTemplate().execute(
					new TransactionCallbackWithoutResult() {
						@Override
						protected void doInTransactionWithoutResult(
								TransactionStatus status) {
							// new
							// MaintenanceEventDao().deleteMaintenanceEventsForDataSource(dataSourceId);
							new DataPointDao().deleteDataPoints(dataSourceId);
							ejt2.update(
									"delete from eventHandlers where eventTypeId="
											+ EventType.EventSources.DATA_SOURCE
											+ " and eventTypeRef1=?",
									new Object[] { dataSourceId });
							ejt2.update(
									"delete from dataSourceUsers where dataSourceId=?",
									new Object[] { dataSourceId });
							ejt2.update("delete from dataSources where id=?",
									new Object[] { dataSourceId });
							// TODO：这里要删除数据源下的数据点
							new DataPointDao().deleteDataPoints(dataSourceId);
						}
					});

			AuditEventType.raiseDeletedEvent(AuditEventType.TYPE_DATA_SOURCE,
					vo);
		}
	}

	public void copyPermissions(final int fromDataSourceId,
			final int toDataSourceId) {
		final List<Integer> userIds = queryForList(
				"select userId from dataSourceUsers where dataSourceId=?",
				new Object[] { fromDataSourceId }, Integer.class);

		ejt.batchUpdate("insert into dataSourceUsers values (?,?)",
				new BatchPreparedStatementSetter() {
					@Override
					public int getBatchSize() {
						return userIds.size();
					}

					@Override
					public void setValues(PreparedStatement ps, int i)
							throws SQLException {
						ps.setInt(1, toDataSourceId);
						ps.setInt(2, userIds.get(i));
					}
				});
	}

	public int copyDataSource(final int dataSourceId, final int factoryId,
			final ResourceBundle bundle) {
		return getTransactionTemplate().execute(
				new GenericTransactionCallback<Integer>() {
					@Override
					public Integer doInTransaction(TransactionStatus status) {
						DataPointDao dataPointDao = new DataPointDao();

						DataSourceVO<?> dataSource = getDataSource(dataSourceId);

						// Copy the data source.
						DataSourceVO<?> dataSourceCopy = dataSource.copy();
						dataSourceCopy.setId(Common.NEW_ID);
						dataSourceCopy.setXid(generateUniqueXid());
						dataSourceCopy.setEnabled(false);
						dataSourceCopy.setName(StringUtils.truncate(
								LocalizableMessage.getMessage(bundle,
										"common.copyPrefix",
										dataSource.getName()), 40));
						dataSourceCopy.setFactoryId(factoryId);// 复制后仍属于这个工厂
						saveDataSource(dataSourceCopy);

						// Copy permissions.
						copyPermissions(dataSource.getId(),
								dataSourceCopy.getId());

						// Copy the points.
						for (DataPointVO dataPoint : dataPointDao
								.getDataPoints(dataSourceId, null)) {
							DataPointVO dataPointCopy = dataPoint.copy();
							dataPointCopy.setId(Common.NEW_ID);
							dataPointCopy.setXid(dataPointDao
									.generateUniqueXid());
							dataPointCopy.setName(dataPoint.getName());
							dataPointCopy.setDataSourceId(dataSourceCopy
									.getId());
							dataPointCopy.setEnabled(dataPoint.isEnabled());
							dataPointCopy.getComments().clear();

							dataPointCopy.setDeviceName(dataSourceCopy
									.getName());

							// Copy the event detectors
							for (PointEventDetectorVO ped : dataPointCopy
									.getEventDetectors()) {
								ped.setId(Common.NEW_ID);
								ped.njbSetDataPoint(dataPointCopy);
							}

							dataPointDao.saveDataPoint(dataPointCopy);

							// Copy permissions
							dataPointDao.copyPermissions(dataPoint.getId(),
									dataPointCopy.getId());
						}

						return dataSourceCopy.getId();
					}
				});
	}
}
