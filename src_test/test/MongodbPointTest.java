package test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoOptions;
import com.serotonin.mango.db.dao.BaseDao;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.vo.DataPointVO;

public class MongodbPointTest extends BaseDao {
	private List<DBObject> pvts = new ArrayList<DBObject>();
	private DB db;

	@Before
	public void init() throws UnknownHostException {
		DataPointDao dataPointDao = new DataPointDao();
		MongoOptions options = new MongoOptions();
		options.autoConnectRetry = true;
		options.connectionsPerHost = 2000;
		options.maxWaitTime = 50000;
		options.socketTimeout = 0;
		options.connectTimeout = 15000;
		options.threadsAllowedToBlockForConnectionMultiplier = 200;
		Mongo mongo = new Mongo("192.168.1.117", options);
		db = mongo.getDB("test9");
		List<DataPointVO> points = dataPointDao.getDataPointIds(4724);
		for (DataPointVO p : points) {
			DBObject obj = new BasicDBObject(getDataPoint(p.getId()));
			pvts.add(obj);
		}
	}

	@Test
	public void test1() throws InterruptedException {
		List<DBObject> pvts = new ArrayList<DBObject>();
		DataPointDao dataPointDao = new DataPointDao();
		MongoOptions options = new MongoOptions();
		MongodbPointTest test = new MongodbPointTest();
		options.autoConnectRetry = true;
		options.connectionsPerHost = 2000;
		options.maxWaitTime = 50000;
		options.socketTimeout = 0;
		options.connectTimeout = 15000;
		options.threadsAllowedToBlockForConnectionMultiplier = 200;
		System.setProperty("MONGO.POOLSIZE", String.valueOf(10000));
		Mongo mongo;
		List<DataPointVO> points = dataPointDao.getDataPointIds(4724);
		for (DataPointVO p : points) {
			DBObject obj = new BasicDBObject(test.getDataPoint(p.getId()));
			pvts.add(obj);
		}
		final List<List<DBObject>> dataSources = new ArrayList<List<DBObject>>();
		for (int i = 0; i < 7000; i++) {
			List<DBObject> dataSource = new ArrayList<DBObject>();
			for (DBObject obj : pvts) {

				DBObject insertPoint = new BasicDBObject();
				insertPoint.put("pid", i);
				insertPoint.put("pointValue", obj.get("pointValue"));
				insertPoint.put("dataType", obj.get("dataType"));
				insertPoint.put("ts", obj.get("ts"));
				dataSource.add(insertPoint);
			}
			dataSources.add(dataSource);
		}

		try {
			mongo = new Mongo("192.168.1.117", options);
			db = mongo.getDB("test11");
			for (int i = 0; i < 4; i++) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						System.out.println(Thread.currentThread());
						/*
						 * for (int j = 0; j < dataSources.size(); j++) {
						 * List<DBObject> obj; try { obj =
						 * deepCopy(dataSources.get(j)); DBCollection collection
						 * = db.getCollection("p_" + j / 10);
						 * collection.insert(obj); } catch (IOException e) {
						 * e.printStackTrace(); } catch (ClassNotFoundException
						 * e) { e.printStackTrace(); } }
						 */
						System.out.println("time:" + System.currentTimeMillis());
					}
				}).start();

			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void getCollections() {
		Set<String> names = db.getCollectionNames();
	}

	public Map<String, Object> getDataPoint(int dpid) {
		String sql = "select top 1 * from pointValues_" + dpid
				+ " order by ts desc";
		Map<String, Object> point = queryForObject(sql, null,
				new com.serotonin.mango.vo.ResultData(),
				new HashMap<String, Object>());
		point.put("pid", dpid);
		return point;
	}

	public static List deepCopy(List src) throws IOException,
			ClassNotFoundException {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(byteOut);
		out.writeObject(src);

		ByteArrayInputStream byteIn = new ByteArrayInputStream(
				byteOut.toByteArray());
		ObjectInputStream in = new ObjectInputStream(byteIn);
		List dest = (List) in.readObject();
		return dest;
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("输入数据源数量、循环次数");
			return;
		}
		int dataSourceCount = Integer.valueOf(args[0]);
		int insertCount = Integer.valueOf(args[1]);
		System.out.println("数据源数量：" + dataSourceCount + ",循环次数：" + insertCount);
		List<DBObject> pvts = new ArrayList<DBObject>();
		DB db = null;
		DataPointDao dataPointDao = new DataPointDao();
		MongodbPointTest test = new MongodbPointTest();
		MongoOptions options = new MongoOptions();
		options.autoConnectRetry = true;
		options.connectionsPerHost = 2000;
		options.maxWaitTime = 50000;
		options.socketTimeout = 0;
		options.connectTimeout = 15000;
		options.threadsAllowedToBlockForConnectionMultiplier = 200;
		System.setProperty("MONGO.POOLSIZE", String.valueOf(10000));
		Mongo mongo;
		List<DataPointVO> points = dataPointDao.getDataPointIds(4724);
		for (DataPointVO p : points) {
			DBObject obj = new BasicDBObject(test.getDataPoint(p.getId()));
			pvts.add(obj);
		}
		List<List<DBObject>> dataSources = new ArrayList<List<DBObject>>();
		for (int i = 0; i < dataSourceCount; i++) {
			List<DBObject> dataSource = new ArrayList<DBObject>();
			for (DBObject obj : pvts) {

				DBObject insertPoint = new BasicDBObject();
				insertPoint.put("p", i);
				insertPoint.put("v", obj.get("pointValue"));
				insertPoint.put("d", obj.get("dataType"));
				insertPoint.put("t", obj.get("ts"));
				dataSource.add(insertPoint);
			}
			dataSources.add(dataSource);
		}

		try {
			MongoDBDao mongoDBDao = MongoDBDaoImpl.getMongoDBDaoImplInstance();
			db = mongoDBDao.getDb("c6");
            int n = 0;
			for (int i = 0; i < insertCount; i++) {
				List<List<DBObject>> finalInsert = new ArrayList<List<DBObject>>();
				List<DBObject> objs = new ArrayList<DBObject>();
				for (int j = 0; j < dataSources.size(); j++) {
					
					List<DBObject> obj = deepCopy(dataSources.get(j));

//					DBCursor cursor = collection.find()
//							.sort(new BasicDBObject("ts", -1)).limit(1);
//					if (j == 0) {
//						if (cursor.hasNext()) {
//							System.out.println("first--" + i + "-->"
//									+ cursor.next());
//						}
//					} else if (j == dataSourceCount - 1) {
//						if (cursor.hasNext()) {
//							System.out.println("last--" + i + "-->"
//									+ cursor.next());
//						}
//					}
//					if (cursor != null)
//						cursor.close();

				}
				
				System.out.println("start insert "+System.currentTimeMillis())
				 ; 
//				int n = 0;
				for(List<DBObject> list:finalInsert){
				 DBCollection collection =
				 MongoDBDaoImpl.getMongoDBDaoImplInstance
				  ().getDb("C6").getCollection("pointValues_"+(n++));
				  collection.insert(list); }
				 
				System.out.println(i + "--->" + System.currentTimeMillis());
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
