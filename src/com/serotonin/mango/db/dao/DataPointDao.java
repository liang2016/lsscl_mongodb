/*
    LssclM2M - http://www.lsscl.com
    Copyright (C) 2006-2011 Lsscl ES Technologies Inc.
     
    
     
     
     
     

     
     
     
     

     
    
 */
package com.serotonin.mango.db.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.serotonin.mango.db.change.TableHelper;
import javax.sql.DataSource;

import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.db.IntValuePair;
import com.serotonin.db.spring.ExtendedJdbcTemplate;
import com.serotonin.db.spring.GenericRowMapper;
import com.serotonin.mango.Common;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.rt.event.type.EventType;
import com.serotonin.mango.vo.DataPointExtendedNameComparator;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.UserComment;
import com.serotonin.mango.vo.bean.PointHistoryCount;
import com.serotonin.mango.vo.event.PointEventDetectorVO;
import com.serotonin.mango.vo.hierarchy.PointFolder;
import com.serotonin.mango.vo.hierarchy.PointHierarchy;
import com.serotonin.mango.vo.hierarchy.PointHierarchyEventDispatcher;
import com.serotonin.mango.vo.link.PointLinkVO;
import com.serotonin.util.SerializationHelper;
import com.serotonin.util.Tuple;
import com.serotonin.mango.vo.acp.ACPSystemMembersVO;

public class DataPointDao extends BaseDao {
    public DataPointDao() {
        super();
    }

    public DataPointDao(DataSource dataSource) {
        super(dataSource);
    }

    //
    //
    // Data Points
    //
    public String generateUniqueXid() {
        return generateUniqueXid(DataPointVO.XID_PREFIX, "dataPoints");
    }

    public boolean isXidUnique(String xid, int excludeId) {
        return isXidUnique(xid, excludeId, "dataPoints");
    }

    public String getExtendedPointName(int dataPointId) {
        DataPointVO vo = getDataPoint(dataPointId);
        if (vo == null)
            return "?";
        return vo.getExtendedName();
    }

    private static final String DATA_POINT_SELECT = "select dp.id, dp.xid, dp.dataSourceId, dp.data, ds.name, " //
            + "ds.xid, ds.dataSourceType ,ds.factoryId " //
            + "from dataPoints dp join dataSources ds on ds.id = dp.dataSourceId ";

    public List<DataPointVO> getDataPoints(Comparator<DataPointVO> comparator, boolean includeRelationalData) {
    	List<DataPointVO> dps = query(DATA_POINT_SELECT, new DataPointRowMapper());
        if (includeRelationalData)
            setRelationalData(dps);
        if (comparator != null)
            Collections.sort(dps, comparator);
        return dps;
    }
    
    /**
     * 根据当前工厂查询所有点
     * @param factoryId 工厂ID 
     * @param comparator 排序对象
     * @param includeRelationalData 是否关联其他信息
     * @return 点的集合
     */
    public List<DataPointVO> getDataPoints(int factoryId,Comparator<DataPointVO> comparator, boolean includeRelationalData) {
    	List<DataPointVO> dps = query(DATA_POINT_SELECT+" where ds.factoryId=? ",new Object[]{factoryId}, new DataPointRowMapper());
        if (includeRelationalData)
            setRelationalData(dps);
        if (comparator != null)
            Collections.sort(dps, comparator);
        return dps;
    }

    public List<DataPointVO> getDataPoints(int dataSourceId, Comparator<DataPointVO> comparator) {
        List<DataPointVO> dps = query(DATA_POINT_SELECT + " where dp.dataSourceId=?", new Object[] { dataSourceId },
                new DataPointRowMapper());
        setRelationalData(dps);
        if (comparator != null)
            Collections.sort(dps, comparator);
        return dps;
    }

    public DataPointVO getDataPoint(int id) {
        DataPointVO dp = queryForObject(DATA_POINT_SELECT + " where dp.id=?", new Object[] { id },
                new DataPointRowMapper(), null);
        setRelationalData(dp);
        return dp;
    }

    public DataPointVO getDataPoint(String xid) {
        DataPointVO dp = queryForObject(DATA_POINT_SELECT + " where dp.xid=?", new Object[] { xid },
                new DataPointRowMapper(), null);
        setRelationalData(dp);
        return dp;
    }

    class DataPointRowMapper implements GenericRowMapper<DataPointVO> {
        public DataPointVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            DataPointVO dp;
            try {
                dp = (DataPointVO) SerializationHelper.readObject(rs.getBlob(4).getBinaryStream());
            }
            catch (ShouldNeverHappenException e) {
                dp = new DataPointVO();
                dp.setName("Point configuration lost. Please recreate.");
                dp.defaultTextRenderer();
            }
            dp.setId(rs.getInt(1));
            dp.setXid(rs.getString(2));
            dp.setDataSourceId(rs.getInt(3));

            // Data source information.
            dp.setDataSourceName(rs.getString(5));
            dp.setDataSourceXid(rs.getString(6));
            dp.setDataSourceTypeId(rs.getInt(7));

            // The spinwave changes were not correctly implemented, so we need to handle potential errors here.
            if (dp.getPointLocator() == null) {
                // Use the data source tpe id to determine what type of locator is needed.
                dp.setPointLocator(new DataSourceDao().getDataSource(dp.getDataSourceId()).createPointLocator());
            }
            dp.setFactoryId(rs.getInt(8));
            return dp;
        }
    }

    private void setRelationalData(List<DataPointVO> dps) {
        for (DataPointVO dp : dps)
            setRelationalData(dp);
    }

    private void setRelationalData(DataPointVO dp) {
        if (dp == null)
            return;
        setEventDetectors(dp);
        setPointComments(dp);
    }

    public void saveDataPoint(final DataPointVO dp) {
        getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                // Decide whether to insert or update.
                if (dp.getId() == Common.NEW_ID) {
                    insertDataPoint(dp);
                    //create table
                 // test create table
    				TableHelper th = new TableHelper();
    				int pointId=dp.getId();
    				String[] sql = th.createTableSQL(pointId);
    				th.runSql(sql,pointId);
                    // Reset the point hierarchy so that the new point gets included.
                    cachedPointHierarchy = null;
                }
                else
                    updateDataPoint(dp);
            }
        });
    }

    void insertDataPoint(final DataPointVO dp) {
        // Create a default text renderer
        if (dp.getTextRenderer() == null)
            dp.defaultTextRenderer();

        // Insert the main data point record.
        dp.setId(doInsert("insert into dataPoints (xid, dataSourceId, data) values (?,?,?)", new Object[] {
                dp.getXid(), dp.getDataSourceId(), SerializationHelper.writeObject(dp) }, new int[] { Types.VARCHAR,
                Types.INTEGER, Types.BLOB }));

        // Save the relational information.
        saveEventDetectors(dp);

//        AuditEventType.raiseAddedEvent(AuditEventType.TYPE_DATA_POINT, dp);
    }

    void updateDataPoint(final DataPointVO dp) {
        DataPointVO old = getDataPoint(dp.getId());

        if (old.getPointLocator().getDataTypeId() != dp.getPointLocator().getDataTypeId())
            // Delete any point values where data type doesn't match the vo, just in case the data type was changed.
            // Only do this if the data type has actually changed because it is just really slow if the database is
            // big or busy.
            new PointValueDao().deletePointValuesWithMismatchedType(dp.getId(), dp.getPointLocator().getDataTypeId());

        // Save the VO information.
        updateDataPointShallow(dp);

        AuditEventType.raiseChangedEvent(AuditEventType.TYPE_DATA_POINT, old, dp);

        // Save the relational information.
        saveEventDetectors(dp);
    }

    public void updateDataPointShallow(final DataPointVO dp) {
        ejt.update("update dataPoints set xid=?, data=? where id=?",
                new Object[] { dp.getXid(), SerializationHelper.writeObject(dp), dp.getId() }, new int[] {
                        Types.VARCHAR, Types.BLOB, Types.INTEGER });
    }

    public void deleteDataPoints(final int dataSourceId) {
        List<DataPointVO> old = getDataPoints(dataSourceId, null);
        for (DataPointVO dp : old)
            beforePointDelete(dp.getId());

        for (DataPointVO dp : old)
            deletePointHistory(dp.getId());

        getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
            @SuppressWarnings("synthetic-access")
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                List<Integer> pointIds = queryForList("select id from dataPoints where dataSourceId=?",
                        new Object[] { dataSourceId }, Integer.class);
                if (pointIds.size() > 0)
                    deleteDataPointImpl(createDelimitedList(new HashSet<Integer>(pointIds), ",", null));
            }
        });

        for (DataPointVO dp : old)
            AuditEventType.raiseDeletedEvent(AuditEventType.TYPE_DATA_POINT, dp);
    }

    public void deleteDataPoint(final int dataPointId) {
        DataPointVO dp = getDataPoint(dataPointId);
        if (dp != null) {
            beforePointDelete(dataPointId);
            deletePointHistory(dataPointId);
            getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    deleteDataPointImpl(Integer.toString(dataPointId));
                }
            });

            AuditEventType.raiseDeletedEvent(AuditEventType.TYPE_DATA_POINT, dp);
        }
    }

    private void beforePointDelete(int dataPointId) {
        for (PointLinkVO link : new PointLinkDao().getPointLinksForPoint(dataPointId))
            Common.ctx.getRuntimeManager().deletePointLink(link.getId());
    }
//ok
    void deletePointHistory(int dataPointId) {
        Object[] p = new Object[] {};
        long min = ejt.queryForLong("select min(ts) from pointValues_"+dataPointId, p);
        long max = ejt.queryForLong("select max(ts) from pointValues_"+dataPointId, p);
        deletePointHistory(dataPointId, min, max);
    }
//ok
    void deletePointHistory(int dataPointId, long min, long max) {
        while (true) {
            try {
                ejt.update("delete from pointValues_"+dataPointId+" where ts <= ?", new Object[] { max });
                break;
            }
            catch (UncategorizedSQLException e) {
                if ("The total number of locks exceeds the lock table size".equals(e.getSQLException().getMessage())) {
                    long mid = (min + max) >> 1;
                    deletePointHistory(dataPointId, min, mid);
                    min = mid;
                }
                else
                    throw e;
            }
        }
    }

    void deleteDataPointImpl(String dataPointIdList) {
    	String pointsSql=changePointString(dataPointIdList);
        dataPointIdList = "(" + dataPointIdList + ")";
        //删除空压机成员表中关系
        ejt.update("delete from aircompressor_members where dpid in "+dataPointIdList);
        ejt.update("delete from eventHandlers where eventTypeId=" + EventType.EventSources.DATA_POINT
                + " and eventTypeRef1 in " + dataPointIdList);
        ejt.update("delete from userComments where commentType=2 and typeKey in " + dataPointIdList);
        ejt.update("delete from pointEventDetectors where dataPointId in " + dataPointIdList);
        ejt.update("delete from dataPointUsers where dataPointId in " + dataPointIdList);
        ejt.update("delete from watchListPoints where dataPointId in " + dataPointIdList);
        ejt.update("delete from dataPoints where id in " + dataPointIdList);
        ejt.update("drop table " +pointsSql);

        cachedPointHierarchy = null;
    }
	private static String changePointString(String points) {
		String point[] = points.split(",");
		String sql="";
		for (int i = 0; i < point.length; i++) {
			if(i<point.length&&i!=0)
				sql+=",";
			sql+="pointValues_"+point[i];
		}
		return sql;
	}
    //
    //
    // Event detectors
    //
    public int getDataPointIdFromDetectorId(int pedId) {
        return ejt.queryForInt("select dataPointId from pointEventDetectors where id=?", new Object[] { pedId });
    }

    public String getDetectorXid(int pedId) {
        return queryForObject("select xid from pointEventDetectors where id=?", new Object[] { pedId }, String.class,
                null);
    }

    public int getDetectorId(String pedXid, int dataPointId) {
        return ejt.queryForInt("select id from pointEventDetectors where xid=? and dataPointId=?", new Object[] {
                pedXid, dataPointId }, -1);
    }

    public String generateEventDetectorUniqueXid(int dataPointId) {
        String xid = Common.generateXid(PointEventDetectorVO.XID_PREFIX);
        while (!isEventDetectorXidUnique(dataPointId, xid, -1))
            xid = Common.generateXid(PointEventDetectorVO.XID_PREFIX);
        return xid;
    }

    public boolean isEventDetectorXidUnique(int dataPointId, String xid, int excludeId) {
        return ejt.queryForInt("select count(*) from pointEventDetectors where dataPointId=? and xid=? and id<>?",
                new Object[] { dataPointId, xid, excludeId }) == 0;
    }

    private void setEventDetectors(DataPointVO dp) {
        dp.setEventDetectors(getEventDetectors(dp));
    }

    private List<PointEventDetectorVO> getEventDetectors(DataPointVO dp) {
        return query(
                "select id, xid, alias, detectorType, alarmLevel, stateLimit, duration, durationType, binaryState, " //
                        + "  multistateState, changeCount, alphanumericState, weight " //
                        + "from pointEventDetectors " //
                        + "where dataPointId=? " // 
                        + "order by id", new Object[] { dp.getId() }, new EventDetectorRowMapper(dp));
    }

    class EventDetectorRowMapper implements GenericRowMapper<PointEventDetectorVO> {
        private final DataPointVO dp;

        public EventDetectorRowMapper(DataPointVO dp) {
            this.dp = dp;
        }

        public PointEventDetectorVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            PointEventDetectorVO detector = new PointEventDetectorVO();
            int i = 0;
            detector.setId(rs.getInt(++i));
            detector.setXid(rs.getString(++i));
            detector.setAlias(rs.getString(++i));
            detector.setDetectorType(rs.getInt(++i));
            detector.setAlarmLevel(rs.getInt(++i));
            detector.setLimit(rs.getDouble(++i));
            detector.setDuration(rs.getInt(++i));
            detector.setDurationType(rs.getInt(++i));
            detector.setBinaryState(charToBool(rs.getString(++i)));
            detector.setMultistateState(rs.getInt(++i));
            detector.setChangeCount(rs.getInt(++i));
            detector.setAlphanumericState(rs.getString(++i));
            detector.setWeight(rs.getDouble(++i));
            detector.njbSetDataPoint(dp);
            return detector;
        }
    }

    private void saveEventDetectors(DataPointVO dp) {
        // Get the ids of the existing detectors for this point.
        final List<PointEventDetectorVO> existingDetectors = getEventDetectors(dp);

        // Insert or update each detector in the point.
        for (PointEventDetectorVO ped : dp.getEventDetectors()) {
            if (ped.getId() < 0) {
                // Insert the record.
                ped.setId(doInsert(
                        "insert into pointEventDetectors "
                                + "  (xid, alias, dataPointId, detectorType, alarmLevel, stateLimit, duration, durationType, "
                                + "  binaryState, multistateState, changeCount, alphanumericState, weight) "
                                + "values (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        new Object[] { ped.getXid(), ped.getAlias(), dp.getId(), ped.getDetectorType(),
                                ped.getAlarmLevel(), ped.getLimit(), ped.getDuration(), ped.getDurationType(),
                                boolToChar(ped.isBinaryState()), ped.getMultistateState(), ped.getChangeCount(),
                                ped.getAlphanumericState(), ped.getWeight() }, new int[] { Types.VARCHAR,
                                Types.VARCHAR, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.DOUBLE,
                                Types.INTEGER, Types.INTEGER, Types.VARCHAR, Types.INTEGER, Types.INTEGER,
                                Types.VARCHAR, Types.DOUBLE }));
                AuditEventType.raiseAddedEvent(AuditEventType.TYPE_POINT_EVENT_DETECTOR, ped);
            }
            else {
                PointEventDetectorVO old = removeFromList(existingDetectors, ped.getId());

                ejt.update(
                        "update pointEventDetectors set xid=?, alias=?, alarmLevel=?, stateLimit=?, duration=?, "
                                + "  durationType=?, binaryState=?, multistateState=?, changeCount=?, alphanumericState=?, "
                                + "  weight=? " + "where id=?",
                        new Object[] { ped.getXid(), ped.getAlias(), ped.getAlarmLevel(), ped.getLimit(),
                                ped.getDuration(), ped.getDurationType(), boolToChar(ped.isBinaryState()),
                                ped.getMultistateState(), ped.getChangeCount(), ped.getAlphanumericState(),
                                ped.getWeight(), ped.getId() }, new int[] { Types.VARCHAR, Types.VARCHAR,
                                Types.INTEGER, Types.DOUBLE, Types.INTEGER, Types.INTEGER, Types.VARCHAR,
                                Types.INTEGER, Types.INTEGER, Types.VARCHAR, Types.DOUBLE, Types.INTEGER });

                AuditEventType.raiseChangedEvent(AuditEventType.TYPE_POINT_EVENT_DETECTOR, old, ped);
            }
        }

        // Delete detectors for any remaining ids in the list of existing detectors.
        for (PointEventDetectorVO ped : existingDetectors) {
            ejt.update("delete from eventHandlers " + "where eventTypeId=" + EventType.EventSources.DATA_POINT
                    + " and eventTypeRef1=? and eventTypeRef2=?", new Object[] { dp.getId(), ped.getId() });
            ejt.update("delete from pointEventDetectors where id=?", new Object[] { ped.getId() });

            AuditEventType.raiseDeletedEvent(AuditEventType.TYPE_POINT_EVENT_DETECTOR, ped);
        }
    }

    private PointEventDetectorVO removeFromList(List<PointEventDetectorVO> list, int id) {
        for (PointEventDetectorVO ped : list) {
            if (ped.getId() == id) {
                list.remove(ped);
                return ped;
            }
        }
        return null;
    }

    public void copyPermissions(final int fromDataPointId, final int toDataPointId) {
        final List<Tuple<Integer, Integer>> ups = query(
                "select userId, permission from dataPointUsers where dataPointId=?", new Object[] { fromDataPointId },
                new GenericRowMapper<Tuple<Integer, Integer>>() {
                    @Override
                    public Tuple<Integer, Integer> mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Tuple<Integer, Integer>(rs.getInt(1), rs.getInt(2));
                    }
                });

        ejt.batchUpdate("insert into dataPointUsers values (?,?,?)", new BatchPreparedStatementSetter() {
            @Override
            public int getBatchSize() {
                return ups.size();
            }

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, toDataPointId);
                ps.setInt(2, ups.get(i).getElement1());
                ps.setInt(3, ups.get(i).getElement2());
            }
        });
    }

    //
    //
    // Point comments
    //
    private static final String POINT_COMMENT_SELECT = UserCommentRowMapper.USER_COMMENT_SELECT
            + "where uc.commentType= " + UserComment.TYPE_POINT + " and uc.typeKey=? " + "order by uc.ts";

    private void setPointComments(DataPointVO dp) {
        dp.setComments(query(POINT_COMMENT_SELECT, new Object[] { dp.getId() }, new UserCommentRowMapper()));
    }

    //
    //
    // Point hierarchy
    //
    static PointHierarchy cachedPointHierarchy;

    public PointHierarchy getPointHierarchy() {
        if (cachedPointHierarchy == null) {
            final Map<Integer, List<PointFolder>> folders = new HashMap<Integer, List<PointFolder>>();

            // Get the folder list.
            ejt.query("select id, parentId, name from pointHierarchy", new RowCallbackHandler() {
                public void processRow(ResultSet rs) throws SQLException {
                    PointFolder f = new PointFolder(rs.getInt(1), rs.getString(3));
                    int parentId = rs.getInt(2);
                    List<PointFolder> folderList = folders.get(parentId);
                    if (folderList == null) {
                        folderList = new LinkedList<PointFolder>();
                        folders.put(parentId, folderList);
                    }
                    folderList.add(f);
                }
            });

            // Create the folder hierarchy.
            PointHierarchy ph = new PointHierarchy();
            addFoldersToHeirarchy(ph, 0, folders);

            // Add data points.
            List<DataPointVO> points = getDataPoints(DataPointExtendedNameComparator.instance, false);
            for (DataPointVO dp : points)
                ph.addDataPoint(dp.getId(), dp.getPointFolderId(), dp.getExtendedName());

            cachedPointHierarchy = ph;
        }

        return cachedPointHierarchy;
    }

    
/**
 * get scopeId PointHierarchy 
 * @param scopeId
 * @return
 */
    public PointHierarchy getPointHierarchy(int scopeId) {
    	cachedPointHierarchy=null;
        if (cachedPointHierarchy == null) {
            final Map<Integer, List<PointFolder>> folders = new HashMap<Integer, List<PointFolder>>();

            // Get the folder list.
            ejt.query("select id, parentId, name from pointHierarchy where scopeId=?",new Object[]{scopeId}, new RowCallbackHandler() {
                public void processRow(ResultSet rs) throws SQLException {
                    PointFolder f = new PointFolder(rs.getInt(1), rs.getString(3));
                    int parentId = rs.getInt(2);
                    List<PointFolder> folderList = folders.get(parentId);
                    if (folderList == null) {
                        folderList = new LinkedList<PointFolder>();
                        folders.put(parentId, folderList);
                    }
                    folderList.add(f);
                }
            });

            // Create the folder hierarchy.
            PointHierarchy ph = new PointHierarchy();
            addFoldersToHeirarchy(ph, 0, folders);

            // Add data points.
            List<DataPointVO> points = getDataPoints(scopeId,DataPointExtendedNameComparator.instance, false);
            for (DataPointVO dp : points)
                ph.addDataPoint(dp.getId(), dp.getPointFolderId(), dp.getExtendedName());

            cachedPointHierarchy = ph;
        }

        return cachedPointHierarchy;
    }
    
    private void addFoldersToHeirarchy(PointHierarchy ph, int parentId, Map<Integer, List<PointFolder>> folders) {
        List<PointFolder> folderList = folders.remove(parentId);
        if (folderList == null)
            return;

        for (PointFolder f : folderList) {
            ph.addPointFolder(f, parentId);
            addFoldersToHeirarchy(ph, f.getId(), folders);
        }
    }

    public void deleteScopeHierarchy(int scopeId) {
    	 ejt.update("delete from pointHierarchy where scopeid=?",new Object[]{scopeId});
	}
    
    public void savePointHierarchy(final PointFolder root) {
    	final  int scopeId=root.getScopeId();
        final ExtendedJdbcTemplate ejt2 = ejt;
        getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
             
            	// Dump the hierarchy table.
                ejt2.update("delete from pointHierarchy where scopeid=?",new Object[]{scopeId});
                
                // Save the point folders.
                savePointFolder(root, 0,scopeId);
            }
        });

        cachedPointHierarchy = null;
        cachedPointHierarchy = getPointHierarchy(root.getScopeId());
        PointHierarchyEventDispatcher.firePointHierarchySaved(root);
    }
    void savePointFolder(PointFolder folder, int parentId,int scopeId) {
        // Save the folder.
        if (folder.getId() == Common.NEW_ID)
            folder.setId(doInsert("insert into pointHierarchy (parentId, name,scopeId) values (?,?,?)", new Object[] { parentId,
                    folder.getName(),scopeId}));
        else if (folder.getId() != 0){
        	ejt.update("delete from pointHierarchy where id=?",new Object[]{folder.getId()});
        	ejt.update("set IDENTITY_INSERT pointHierarchy ON ");
        	ejt.update("insert into pointHierarchy (id, parentId, name,scopeId) values (?,?,?,?)", new Object[] { folder.getId(),
                    parentId, folder.getName() ,scopeId});
        	ejt.update("set IDENTITY_INSERT pointHierarchy off ");
            }

        // Save the subfolders
        for (PointFolder sf : folder.getSubfolders()){
            savePointFolder(sf, folder.getId(),scopeId);
        }
        // Update the folder references in the points.
        DataPointVO dp;
        for (IntValuePair p : folder.getPoints()) {
            dp = getDataPoint(p.getKey());
            // The point may have been deleted while editing the hierarchy.
            if (dp != null) {
                dp.setPointFolderId(folder.getId());
                updateDataPointShallow(dp);
            }
        }
    }
//???????
    public List<PointHistoryCount> getTopPointHistoryCounts() {
        List<PointHistoryCount> counts = query(
                "select dataPointId, count(*) from pointValues group by dataPointId order by 2 desc",
                new GenericRowMapper<PointHistoryCount>() {
                    @Override
                    public PointHistoryCount mapRow(ResultSet rs, int rowNum) throws SQLException {
                        PointHistoryCount c = new PointHistoryCount();
                        c.setPointId(rs.getInt(1));
                        c.setCount(rs.getInt(2));
                        return c;
                    }
                });

        List<DataPointVO> points = getDataPoints(DataPointExtendedNameComparator.instance, false);

        // Collate in the point names.
        for (PointHistoryCount c : counts) {
            for (DataPointVO point : points) {
                if (point.getId() == c.getPointId()) {
                    c.setPointName(point.getExtendedName());
                    break;
                }
            }
        }

        return counts;
    }
    /**
     * 根据空压机ID和数据源ID查询该数据源下该空压机的所有点信息的集合
     * @param acpId 空压机编号
     * @param dataSourceId 数据源编号
     * @return 点信息的集合
     */
    public List<DataPointVO> getDataPointByAcpId(int acpId,int dataSourceId) {
    	List<DataPointVO> result =query(
    			DATA_POINT_SELECT+"where dp.dataSourceId = ? and dp.id in (SELECT DPID FROM AIRCOMPRESSOR_MEMBERS WHERE ACID =?) " , 
    			new Object[] {dataSourceId,acpId}, 
    			new DataPointRowMapper()
    	);
    	setRelationalData(result);
    	return result;
    }

    /**
     * 查询某个数据源下不属于任何空压机的数据点的集合
     * @param dataSourceId 数据源ID
     * @return 数据点的集合
     */
    public List<DataPointVO> getDataPointWhitoutAcp(int dataSourceId,int[] acpIds){
    	StringBuffer tempSql = new StringBuffer("where dp.dataSourceId = ? ");
    	if(acpIds.length!=0){
    		tempSql.append(" and dp.id not in (SELECT DPID FROM AIRCOMPRESSOR_MEMBERS WHERE ACID IN ( ");
    		for(int i=0;i<acpIds.length;i++){
    			tempSql.append(acpIds[i]);
    			if(i+1<acpIds.length)tempSql.append(",");
    		}
    		tempSql.append("))");
    	}
    	List<DataPointVO> result =query(
    			DATA_POINT_SELECT+tempSql.toString(), 
    			new Object[] {dataSourceId}, 
    			new DataPointRowMapper()
    	);
    	setRelationalData(result);
    	return result;
    }

    //根据系统编号，统计参数编号+已知成员为数据点在系统成员表中查找点的ID
    public static final String SELECT_DPID_FROM_ACSID_AND_SPID = " select memberId from aircompressor_system_members where acsid = ? and membertype = ? and spid = ? "; 
    
    /**
     * 根据系统编号，统计参数编号+已知成员为数据点在系统成员表中查找点的ID
     * @param acsid 系统编号
     * @param spid 统计参数编号
     * @return 数据点的编号
     */
    public int getPointFromAcpSystemMember(int acsid,int spid){
    	List<Integer> dpids = queryForList(SELECT_DPID_FROM_ACSID_AND_SPID,new Object[]{
    			acsid,ACPSystemMembersVO.MemberTypes.POINT,spid    			
    	},Integer.class);
    	if(dpids!=null&&dpids.size()>0){
    		return dpids.get(0);
    	}else{
    		return -1;
    	}
    }
    
    public static final String SELECT_DPID_FROM_ACPID_AND_SPID = " select dpid from aircompressor_members where acid = ? and spid = ? ";
    
    /**
     * 根据空压机编号，空压机统计参数编号 查找数据点的ID
     * @param acpid 空压机编号
     * @param spid 统计参数编号
     * @return 数据点编号
     */
    public int getPointFromAcpMember(int acpid,int spid){
    	List<Integer> dpids = queryForList(SELECT_DPID_FROM_ACPID_AND_SPID,new Object[]{
    			acpid,spid    			
    	},Integer.class);
    	if(dpids!=null&&dpids.size()>0){
    		return dpids.get(0);
    	}else{
    		return -1;
    	}
    }
    
    /**
     * 是否自动生成
     * @param dpid 点的ID
     * @return 是否自动生成
     */
    public boolean isAutoGeneration(int dpid){
    	String sql = " select count(*) from aircompressor_members where dpid = ? ";
    	int result = ejt.queryForInt(sql,new Object[]{dpid},0);
    	if(result==0) return false;
    	else return true;
    }
    /**
     * 根据点的ID获取对应的统计参数ID
     */
    public Integer getStatisticsParamBydpId(int dpid){
    	String sql = " select spid from aircompressor_members where dpid = ? ";
    	List<Integer> spids = queryForList(sql,new Object[]{dpid},Integer.class);
    	if(spids!=null&&spids.size()>0){
    		return spids.get(0);
    	}else{
    		return null;
    	}
    }
    /**
   * 是否为WarningCode统计参数对应的点
   */
  public boolean isWarningCode(int dpid){
  	String sql = " select dataSourceType from dataSources where id=(select dataSourceId from dataPoints where id=?) ";
  	int dataType = ejt.queryForInt(sql,new Object[]{dpid},0);
  	if(dataType==9){
  		return true;
  	}else{
  		return false;
  	}
   }
  /**
   * 获得所有数据点的id
   * @return
   */
  public List<Integer> getDataPointIds(){
	  String sql = " select id from datapoints";
  	  List<Integer> spids = queryForList(sql,new Object[]{},Integer.class);
  	  return spids;
  }
/**
 * 查询一个数据源下的所有数据点
 * @param datasourceId
 * @return
 */
  public  List<DataPointVO> getDataPointIds(int datasourceId){
	  String sql =DATA_POINT_SELECT+ " where dataSourceId=?";
	  List<DataPointVO> spids = query(sql,new Object[]{datasourceId},new DataPointRowMapper());
  	  return spids;
  }
}
