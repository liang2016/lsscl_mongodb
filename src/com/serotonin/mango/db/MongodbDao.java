package com.serotonin.mango.db;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.Mongo;

public class MongodbDao {
	public static DB getDB(){
		Mongo mongo = null;
		try {
			mongo = new Mongo("192.168.1.100", 27017);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return mongo.getDB("test");
	}

}
