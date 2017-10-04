package com.lsscl.app.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.lsscl.app.dao.AppsettingDao;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.DataSourceDao;
import com.serotonin.mango.db.dao.UserDao;
import com.serotonin.mango.db.dao.acp.ACPDao;
import com.serotonin.mango.db.dao.scope.ScopeDao;
import com.serotonin.mango.vo.Appacpinfo;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.acp.ACPTypeVO;
import com.serotonin.mango.vo.acp.ACPVO;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.dataSource.modbus.ModbusIpDataSourceVO;
import com.serotonin.mango.vo.scope.ScopeVO;
import com.serotonin.mango.vo.scope.TradeVO;

public class Test2000 {

	private ScopeDao scopeDao = new ScopeDao();
	private DataSourceDao dataSourceDao = new DataSourceDao();
	private AppsettingDao appSettingDao = new AppsettingDao();
	private DataPointDao dpDao = new DataPointDao();

	@Test
	public void disableOldDataSources() throws Exception {
		disableDataSources();
	}

	public void disableDataSources() {
		List<DataSourceVO<?>> dataSources = dataSourceDao.getDataSources();
		for (DataSourceVO<?> dataSource : dataSources) {
			dataSource.setEnabled(false);
			System.out.println("disable " + dataSource.getName() + "...");
			dataSourceDao.saveDataSource(dataSource);
		}
	}

	/**
	 * 添加工厂
	 * 
	 * @throws Exception
	 */
	@Test
	public void addScopesAndFactories() throws Exception {
		double lon = 30.259244;
		double lat = 120.219375;
		ScopeVO parentScope = new ScopeVO();
		int pid = addInitScope();
		parentScope.setId(pid);
		for (int i = 0; i < 10; i++) {
			ScopeVO scope = new ScopeVO();
			scope.setAddress("区域" + i);
			scope.setLat(lon);
			scope.setLon(lat);
			scope.setDescription("这是区域" + i);
			scope.setEnlargenum(5);
			scope.setScopename("区域" + i);
			scope.setParentScope(parentScope);
			scope.setScopetype(2);
			addScope(scope);
			for (int j = 0; j < 10; j++) {
				addFactory(scope, i, j);
			}
		}
	}

	public void addAppAcps() {
		List<DataSourceVO<?>> dataSources = dataSourceDao.getDataSources();
		System.out.println("dataSource size:"+dataSources.size());
		for (DataSourceVO<?> dataSource : dataSources) {
			int fid = dataSource.getFactoryId();
			if (fid > 292) {
				Appacpinfo acp = new Appacpinfo();
				int dsid = dataSource.getId();
				acp.setScopeId(fid);
				acp.setName(dataSource.getName());
				acp.setType("SG-CC");
				acp.setPower(55.0f);
				acp.setRatedPressure(8.0f);
				acp.setSerialNumber(dataSource.getId() + "");
				int appacpid = appSettingDao.addAcp(acp);
				DataPointDao dpDao = new DataPointDao();
				List<DataPointVO> points = new ArrayList<DataPointVO>();
				points = dpDao.getDataPointIds(dsid);
				for (DataPointVO p : points) {
					String pointName = getSimplePointName(p);
					appSettingDao.addPoint(pointName, p.getId() + "", appacpid+"");
				}
				System.out.println("add acp:"+acp.getName());
			}
		}
	}
	private String getSimplePointName(DataPointVO p) {
    	String acpName = appSettingDao.getAcpNameByPid(p.getId());
    	int length = acpName.length();
    	length = length>0? length+1:length;
		acpName =  p.getName().substring(length);
		if("主机排气温度".equals(acpName))acpName = "电流";
		if("机组排气压力".equals(acpName))acpName = "排气压力";
		if("机组排气温度".equals(acpName))acpName = "排气温度";
		return acpName;
	}
	/**
	 * 平量添加数据源
	 */
	public void addDataSources() {
		System.out.println("add DataSources.....................");
		int dataSourceCount = 399;
		int iport = 21602;
		int updatePeriods = 20; // 更新期间
		int updatePeriodType = 1; // 秒
		boolean quantize = false; // 量化
		int timeout = 5000; // 超时 (毫秒)
		int retries = 3; // 重试
		boolean contiguousBatches = false; // 仅临近的节点
		boolean createSlaveMonitorPoints = false; // 创建从站通信检测点
		int maxReadBitCount = 2000; // 最大读取数(位)
		int maxReadRegisterCount = 125; // 最大读取数(寄存器)
		int maxWriteRegisterCount = 120; // 最大写入数(寄存器)
		String transportType = "UDP"; // 传输类型
		String host = "127.0.0.1"; // 主机
		boolean encapsulated = true; // 封装277
		System.out.println("-----------------");
		int n = 0;
		while (true) {
			for (int sid = 291; sid < 712; sid++) {
				if (iport == 21602 && sid == 293)
					continue;
				ScopeVO sv = scopeDao.findById(sid);
				int type = sv.getScopetype();
				if (type == 3) {// 工厂
					saveModbusIpDataSource(sid, iport + "", updatePeriods,
							updatePeriodType, quantize, timeout, retries,
							contiguousBatches, createSlaveMonitorPoints,
							maxReadBitCount, maxReadRegisterCount,
							maxWriteRegisterCount, transportType, host, iport,
							encapsulated);
					iport++;
					n++;
					if (n > dataSourceCount)
						break;
				}
			}
			if (n > dataSourceCount)
				break;
		}
	}

	private void saveModbusIpDataSource(int fid, String name,
			int updatePeriods, int updatePeriodType, boolean quantize,
			int timeout, int retries, boolean contiguousBatches,
			boolean createSlaveMonitorPoints, int maxReadBitCount,
			int maxReadRegisterCount, int maxWriteRegisterCount,
			String transportType, String host, int port, boolean encapsulated) {
		// A new data source
		ModbusIpDataSourceVO ds = (ModbusIpDataSourceVO) DataSourceVO
				.createDataSourceVO(3);
		ds.setXid(new DataSourceDao().generateUniqueXid());
		ds.setId(Common.NEW_ID);
		ds.setName(name);
		ds.setFactoryId(fid);
		ds.setUpdatePeriods(updatePeriods);
		ds.setUpdatePeriodType(updatePeriodType);
		ds.setQuantize(quantize);
		ds.setTimeout(timeout);
		ds.setRetries(retries);
		ds.setContiguousBatches(contiguousBatches);
		ds.setCreateSlaveMonitorPoints(createSlaveMonitorPoints);
		ds.setMaxReadBitCount(maxReadBitCount);
		ds.setMaxReadRegisterCount(maxReadRegisterCount);
		ds.setMaxWriteRegisterCount(maxWriteRegisterCount);
		ds.setTransportTypeStr(transportType);
		ds.setHost(host);
		ds.setPort(port);
		ds.setEncapsulated(encapsulated);
		ds.setEnabled(true);
		// DataPointDao dataPointDao=new DataPointDao();
		dataSourceDao.saveDataSource(ds);
		System.out.println("dataSource:" + ds.getName());
		saveAcp(ds.getId(), port, fid);
	}

	private void saveAcp(int dsid, int port, int sid) {
		ACPDao acpDao = new ACPDao();
		ACPVO acpvo = new ACPVO();
		acpvo.setAcpname(port + "");
		acpvo.setXid(port + "");
		ACPTypeVO acpTypeVO = new ACPTypeVO();
		acpTypeVO.setId(24);
		acpvo.setAcpTypeVO(acpTypeVO);
		acpvo.setOffset(40001);
		acpvo.setType(0);
		acpvo.setVolume(110);
		acpvo.setPressure(8);
		acpvo.setFactoryId(sid);
		acpDao.save(acpvo);
	}

	private void addFactory(ScopeVO parentScope, int i, int j) {
		TradeVO tradeVO = new TradeVO();
		tradeVO.setId((i % 4) + 1);
		double lat = 30.259244;
		double lon = 120.219375;
		ScopeVO factory = new ScopeVO();
		factory.setAddress("工厂" + i + "_" + j);
		factory.setDescription("这是工厂" + i + "_" + j);
		factory.setEnlargenum(4);
		factory.setLat(lat);
		factory.setLon(lon);
		factory.setParentScope(parentScope);
		factory.setScopename("工厂" + i + "_" + j);
		factory.setScopetype(3);
		factory.setTradeVO(tradeVO);
		factory.setCode(+i + "_" + j + System.currentTimeMillis());
		addScope(factory);
	}

	private int addScope(ScopeVO scope) {
		int newId = scopeDao.save(scope);
		scope.setId(newId);
		// 添加管理员
		// New database. Create a default user.
		User user = new User();
		user.setId(Common.NEW_ID);
		user.setUsername(scope.getScopename() + "admin");
		user.setPassword(Common.encrypt("admin"));
		user.setAdmin(true);
		if (scope.isDisabled()) {
			user.setDisabled(true);
		}
		user.setDisabled(false);
		// 用户注册范围
		user.setHomeScope(scope);
		new UserDao().saveUser(user);
		return newId;
	}

	public int addInitScope() {
		double lat = 30.259244;
		double lon = 120.219375;
		ScopeVO scope = new ScopeVO();
		scope.setAddress("测试区域");
		scope.setLat(lon);
		scope.setLon(lat);
		scope.setDescription("这是区域");
		scope.setEnlargenum(5);
		scope.setScopename("测试区域");
		ScopeVO parentScope = new ScopeVO();
		parentScope.setId(1);
		scope.setParentScope(parentScope);
		scope.setScopetype(1);
		int newId = addScope(scope);
		return newId;
	}

}
