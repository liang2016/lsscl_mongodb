package test;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.DataSourceDao;
import com.serotonin.mango.db.dao.PointValueDao;

public class MongodbTest {
	private DataPointDao dao;
	private PointValueDao pvDao;
	private DataSourceDao dataSourceDao;
	private DB mongodb;

	@Before
	public void init() throws UnknownHostException {
		SQLServerDataSource dataSource = new SQLServerDataSource();
		String host = "192.168.1.117";
		String user = "sa";
		String password = "asus_admin1";
		dataSource.setURL("jdbc:sqlserver://" + host
				+ ":1433; DatabaseName=LssclDB");
		dataSource.setUser(user);
		dataSource.setPassword(password);
		dao = new DataPointDao(dataSource);
		// pvDao = new PointValueDao(dataSource);
		dataSourceDao = new DataSourceDao(dataSource);

		Mongo mongo = new Mongo("192.168.1.117", 27017);
		mongodb = mongo.getDB("c4");
	}

	@Test
	public void createCollection() {
		
		System.out.println("size:"+mongodb.getCollectionNames().size());
	}

	@Test
	public void insertPoints() {

		Random random = new Random();
		long time = 0;
		System.out.println(System.currentTimeMillis());
//		for (int n = 0; n < 10000; n++) {

			List<DBObject> points = new ArrayList<DBObject>();
			for (int i = 0; i < 20000 * 30; i++) {
				DBObject point = new BasicDBObject();
				point.put("p", random.nextInt(100) + 100000);
				point.put("d", random.nextInt(3));
				point.put("v", random.nextDouble() * 1000);
				point.put("t", System.currentTimeMillis());
				points.add(point);
			}
			long s = System.currentTimeMillis();
			DBCollection collection = mongodb.getCollection("p_" + 1);
			collection.insert(points);
			time += System.currentTimeMillis() - s;
//		}
		System.out.println(System.currentTimeMillis());
		System.out.println("插入时间：" + time);
	}

	public static void main(String[] args) {
		final SimpleDateFormat faFormat = new SimpleDateFormat("hh:mm:ss");
		final DB mongodb = MongoDBDaoImpl.getMongoDBDaoImplInstance().getDb(
				"c4");
		Random random = new Random();
		long time = 0;
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					for (int i = 0; i < 10000; i++) {
						DBCollection collection = mongodb.getCollection("p_"
								+ i);
						if (collection != null) {
							long s = System.currentTimeMillis();
							DBCursor cursor = collection.find().sort(
									new BasicDBObject("t", -1)).limit(1);
							if (cursor.hasNext()) {
								Object obj = cursor.next();
								long t = System.currentTimeMillis()-s;
								if(t>110)System.out.println("p_"+i+"---->"+t+"  time:"+faFormat.format(new Date()));
							}
						}
					}
				}
			}
		}).start();
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						System.out.println("--------size-------"+mongodb.getCollectionNames().size());
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		System.out.println(System.currentTimeMillis());
		for (int n = 0; n < 10000; n++) {

			List<DBObject> points = new ArrayList<DBObject>();
			for (int i = 0; i < 4320 * 30 / 24; i++) {
				DBObject point = new BasicDBObject();
				point.put("p", random.nextInt(100) + 100000);
				point.put("d", random.nextInt(3));
				point.put("v", random.nextDouble() * 1000);
				point.put("t", System.currentTimeMillis());
				points.add(point);
			}
			long s = System.currentTimeMillis();
			DBCollection collection = mongodb.getCollection("p_" + n);
			collection.insert(points);
			time += System.currentTimeMillis() - s;
		}
		System.out.println(System.currentTimeMillis());
		System.out.println("插入时间：" + time);

	}
}
