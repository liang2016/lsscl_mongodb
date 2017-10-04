package test;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;

public class MongoDBManager {
	static private MongoDBManager instance; // 唯一实例
	//	public static final String DB_NAME = "lab";
	//  public static final String MESSAGE_COLLECTION = "email";   

	static synchronized public  MongoDBManager getInstance(final String ip, int port, int poolSize) throws UnknownHostException {
		if (instance == null) {
			instance = new MongoDBManager(ip,port,poolSize);
		}		
		return instance;
	}

	private MongoDBManager() {
	}
	
	private MongoDBManager(final String ip, int port, int poolSize) throws UnknownHostException {
		init(ip,port,poolSize);
	}

	public DB getDB(String dbname) {
		return mongo.getDB(dbname);
	}

	private Mongo mongo;

	public void init(final String ip, int port, int poolSize)
			throws java.net.UnknownHostException {
		System.setProperty("MONGO.POOLSIZE", String.valueOf(poolSize));
		if (mongo == null) {
			MongoOptions options = new MongoOptions();
			options.autoConnectRetry = true;
			options.connectionsPerHost = poolSize;

			ServerAddress serverAddress = new ServerAddress(ip, port);
			mongo = new Mongo(serverAddress, options);
		}
	}
}