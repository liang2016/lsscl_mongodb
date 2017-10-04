package com.serotonin.mango.db.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mongodb.DBObject;

public class PointsInstance {
	private Map<Integer, List<DBObject>> points1 = new HashMap<Integer, List<DBObject>>();
	private Map<Integer,List<DBObject>> points2 = new HashMap<Integer,List<DBObject>>();
	private boolean first = true;

	public void addPoint(int cid, DBObject point) {
		Map<Integer,List<DBObject>> points = first?points1:points2;
		List<DBObject> ps = points.get(cid);
		if (ps != null) {
			ps.add(point);
		} else {
			ps = new ArrayList<DBObject>();
			ps.add(point);
		}
		points.put(cid, ps);
	}

	static class SingletonHolder {
		static PointsInstance task = new PointsInstance();
	}

	private PointsInstance() {
	}

	public static PointsInstance getInstance() {
		return SingletonHolder.task;
	}

	public Map<Integer, List<DBObject>> getPoints() {
		first = !first;
		return !first?points1:points2;
	}
	
	public int getIndex(){
		return !first?1:2;
	}
	
	public boolean isFirst() {
		return first;
	}

	public void setFirst(boolean first) {
		this.first = first;
	}

	public void clearOldData(){
		if(!first){
			points1 = new HashMap<Integer, List<DBObject>>(); 
			System.out.println("clear points1...");
		}else{
			points2 = new HashMap<Integer, List<DBObject>>();
			System.out.println("clear points2...");
		}
	}
}
