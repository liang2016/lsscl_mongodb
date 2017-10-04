package test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoOptions;
import com.mongodb.WriteResult;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.vo.DataPointVO;

public class ThreadTest {
	public static void main(String[] args) {
		List<DBObject> pvts = new ArrayList<DBObject>();
		DataPointDao dataPointDao = new DataPointDao();
		MongoOptions options = new MongoOptions();
		MongodbPointTest test = new MongodbPointTest();
		options.autoConnectRetry = true;
		options.connectionsPerHost = 1000;
		options.maxWaitTime = 50000;
		options.socketTimeout = 0;
		options.connectTimeout = 15000;
		options.threadsAllowedToBlockForConnectionMultiplier = 200;
		System.setProperty("MONGO.POOLSIZE", String.valueOf(1500));
		Mongo mongo;
		List<DataPointVO> points = dataPointDao.getDataPointIds(4724);
		for (DataPointVO p : points) {
			DBObject obj = new BasicDBObject(test.getDataPoint(p.getId()));
			pvts.add(obj);
		}
		final List<List<DBObject>> dataSources = new ArrayList<List<DBObject>>();
		for (int i = 0; i < 1; i++) {
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
			mongo = new Mongo("192.168.1.201:30000", options);
			final DB db = mongo.getDB("c1");
			for (int i = 0; i < 1; i++) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						System.out.println(Thread.currentThread());
						
//						for (int j = 0; j < dataSources.size(); j++) {
							List<DBObject> obj = new ArrayList<DBObject>();
							try {
								for(int n=0;n<20000;n++){
								obj.addAll(deepCopy(dataSources.get(0)));
								}
								System.out.println("star:"+System.currentTimeMillis());
								DBCollection collection = db.getCollection("p_001"
										);
								WriteResult wr = collection.insert(obj);
								System.out.println(wr);
							} catch (IOException e) {
								e.printStackTrace();
							} catch (ClassNotFoundException e) {
								e.printStackTrace();
							}
//						}
						System.out.println("time:" + System.currentTimeMillis());
						System.out.println("count:"+dataSources.size());
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
}
