package com.serotonin.mango.db.dao;

import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lsscl.app.util.AcpConfig;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;


/**
 * to see:http://www.mongodb.org/display/DOCS/Java+Driver+Concurrency
 * Mongo工具类:设计为单例模式，每当月份发生变化，数据库连接名称就会发生变化，这是业务规则
 * 因 MongoDB的Java驱动是线程安全的，对于一般的应用，只要一个Mongo实例即可，Mongo有个内置的连接池（池大小默认为10个）。
 * 对于有大量写和读的环境中，为了确保在一个Session中使用同一个DB时，我们可以用以下方式保证一致性：
 *	 DB mdb = mongo.getDB('dbname');
 *	 mdb.requestStart();
 *	 // 业务代码
 *	 mdb.requestDone();
 * DB和DBCollection是绝对线程安全的
 * @author wujintao
 */
public class MongoUtil{
	
	private static Mongo mongo;
	private static DBCollection coll;
	private static Log log = LogFactory.getLog(MongoUtil.class);
	private static DB db;
	
	static{
		try {
		      MongoOptions options = new MongoOptions();
                      options.autoConnectRetry = true;
                      options.connectionsPerHost = 1000;
                      options.maxWaitTime = 5000;
                      options.socketTimeout = 0;
                      options.connectTimeout = 30000;
                      options.slaveOk = true;
                      options.threadsAllowedToBlockForConnectionMultiplier = 100;
			//事实上，Mongo实例代表了一个数据库连接池，即使在多线程的环境中，一个Mongo实例对我们来说已经足够了
            String host = AcpConfig.env.getProperty("mongodb.host");
            System.out.println("host:"+host);
			mongo = new Mongo(host,options);
		} catch (UnknownHostException e) {
			log.info("get mongo instance failed");
		}
	}
	
	public static DB getDB(){
		if(db==null){
			String dbName = AcpConfig.env.getProperty("mongodb.dbname");
			System.out.println("mongo db name:"+dbName);
			db = mongo.getDB(dbName);
		}
		return db;
	}
	
	
	public static Mongo getMong(){
		return mongo;
	}
	
	public static DBCollection getColl(String collname){
		return getDB().getCollection(collname);
	}
	
}