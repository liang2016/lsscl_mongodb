package run;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.DataSourceDao;
import com.serotonin.mango.db.dao.MongoUtil;
import com.serotonin.mango.db.dao.PointValueDao;
import com.serotonin.mango.db.dao.PointsInstance;
import com.serotonin.mango.db.dao.acp.ACPDao;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.acp.ACPVO;
import com.serotonin.mango.vo.dataSource.DataSourceVO;

public class PointsRevover {
	
	private SqlServerPointValueDao serverPointValueDao = new SqlServerPointValueDao();;
	private PointValueDao pointValueDao = new PointValueDao();
	private DataSourceDao dataSourceDao = new DataSourceDao();
	private DataPointDao dataPointDao = new DataPointDao();
	
	
	public void start(){
		System.out.println("start...");
		//1、获取数据源
		List<DataSourceVO<?>>dataSources = dataSourceDao.getDataSources();
		dataSources = dataSources.subList(0, 10);
		//2、获取点数据恢复时间
		for(DataSourceVO<?>dataSource:dataSources){
			startRevover(dataSource);
		}
		
	}

	/**
	 * 开启线程恢复数据
	 * @param dataSource
	 */
	private void startRevover(final DataSourceVO<?> dataSource) {
		List<DataPointVO> dataPoints = dataPointDao.getDataPointIds(dataSource.getId()); 
//		checkProcess(dataSource,dataPoints);
		System.out.println("正在恢复数据源："+dataSource.getName());
		recoverPoints(dataPoints);
	}
	//进度查询
	private void checkProcess(DataSourceVO<?> dataSource,
			List<DataPointVO> dataPoints) {
		double d = 0;
		long totalCount = 0;
		long recoveredCount = 0;
		long now = new Date().getTime();
		for(DataPointVO dp:dataPoints){
			//获取mongodb的最老时间
			int pid = dp.getId();
			List<Integer>pids = new ArrayList<Integer>();
			pids.add(pid);
			long from = pointValueDao.getMinTimeByDpid(0, now, pid);
			long to = serverPointValueDao.getEndTime(pid);
			long pTotalCount = serverPointValueDao.getCount(pid);
			long pRecoveredCount = pointValueDao.getCountBetween(pid,from,to);
			totalCount += pTotalCount;
			recoveredCount = pRecoveredCount;
		}
		System.out.println("数据源："+dataSource.getName()+" 进度："+(recoveredCount/(totalCount+0.0))+" ("+recoveredCount+"/"+totalCount+")");
	}
	
	/**
	 * 恢复数据
	 */
	private void recoverPoints(List<DataPointVO> dataPoints){
		boolean b = false;
		for(DataPointVO dataPoint:dataPoints){
			List<Integer> pids = new ArrayList<Integer>();
			pids.add(dataPoint.getId());
			long t = pointValueDao.getStartTime(pids);
			b = recoverPoint(t,dataPoint);
		}
		if(b)recoverPoints(dataPoints);
	}

	private void insertPointTask() {
		long count = 0;
		Map<Integer, List<DBObject>> points = PointsInstance.getInstance().getPoints();
		for(Integer cid:points.keySet()){
			DBCollection collection = MongoUtil.getColl("pointValues_"+cid);
			List<DBObject> list = points.get(cid);
			if(list!=null){
				collection.insert(list);
				count += list.size();
			}
		}
		System.out.println("-----插入 "+count+" 条数据----");
		
//		PointsInstance.clearPoints();		
	}

	private boolean recoverPoint(long to, DataPointVO dataPoint) {
		List<Map<String,Object>> pvts = serverPointValueDao.getLimitedPoint(dataPoint.getId(),to,300000);
		boolean b = pvts.size()>0;
		if(b){
			int lastIndex = pvts.size()-1;
			String etime = getFormatTime((Long)pvts.get(0).get("ts"));
			String stime = getFormatTime((Long)pvts.get(lastIndex).get("ts"));
			System.out.println("正在恢复点："+dataPoint.getName()+",id="+dataPoint.getId()+" ("+stime+"~"+etime+")");
		}else{
			System.out.println("点："+dataPoint.getName()+",id="+dataPoint.getId()+" 完成");
			return b;
		}
		int pid = dataPoint.getId();
		for(Map<String,Object> pvt:pvts){
			int dataType = (Integer) pvt.get("dataType");
			double value = (Double) pvt.get("pointValue");
			long time = (Long) pvt.get("ts");
			String svalue = (String) pvt.get("textPointValueShort");
			pointValueDao.insertPoint(pid, dataType, value, time, svalue);
		}
		insertPointTask();
		return b;
	}

	private long getZeroTime(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		String s = sdf.format(new Date(time));
		try {
			return sdf.parse(s).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return time;
	}

	private long getRecoverTime(List<DataPointVO> dataPoints) {
		long time = System.currentTimeMillis();
		//找到恢复起点
		for(DataPointVO dataPoint:dataPoints){
			int pid = dataPoint.getId();
			List<Integer> pids = new ArrayList<Integer>();
			pids.add(pid);
			long ptime = pointValueDao.getStartTime(pids);
			long etime = serverPointValueDao.getEndTime(pid);
			System.out.println("sqlserver:time:"+getFormatTime(etime));
			if(etime<ptime)ptime = etime;
			if(ptime < time){
				boolean hasPoints = serverPointValueDao.getPointValueAfter(pid,ptime)!=null;
				if(hasPoints)time = ptime;
			}
		}
		return time;
	}

	private String getFormatTime(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(new Date(time));
	};
	private String getFormatTime(long time,String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(new Date(time));
	};

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new PointsRevover().start();
//		long time = new PointValueDao().getMinTimeByDpid(0, new Date().getTime(), 112645);
//		System.out.println(time);
	}

}
