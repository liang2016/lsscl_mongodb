package com.lsscl.app.bean;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.lsscl.app.util.StringUtil;
import com.serotonin.db.spring.GenericRowMapper;

/**
 * 区域报警事件
 * 
 * @author yxx
 * 
 */
public class ScopeEvent {
	private int id;
	private Long cTime;
	private String message;
	private int pointId;// 点id
	private String pointName;
	private int acpId;
	private String acpName;// 空压机id
	private String fName;// 工厂名称
	private String scopeId;// 区域id
	private int ackUserId;//确认用户id
	private long ackTs;//确认时间
	private long rtnTs;//结束时间
	private int rtnCause;//是否结束
	private int alarmLevel;//设定级别 ：信息、提醒、警告、报警
	private int typeRef2;//
	
	public String toJSON() {
		StringBuilder sb = new StringBuilder();
		sb.append("{\"FNAME\":\"" + fName + "\",");
		sb.append("\"ID\":" + this.id + ",");
		sb.append("\"SID\":" + this.scopeId + ",");
		String msg = message != null ? message.replace("\\", "\\\\")
				.replace("\"", "\\\"").replace("\r", "\\r")
				.replace("\n", "\\n") : "";
		sb.append("\"MSG\":\"" + msg + "\",");
		sb.append("\"PI\":"+pointId+",");
		sb.append("\"SI\":"+typeRef2+",");//数据源id
		sb.append("\"PN\":\""+(pointName!=null?pointName:"")+"\",");
		sb.append("\"AI\":"+acpId+",");
		sb.append("\"AN\":\""+(acpName!=null?acpName:"")+"\",");
		sb.append("\"UI\":"+ackUserId+",");
		sb.append("\"AT\":"+ackTs+",");
		sb.append("\"RT\":"+rtnTs+",");
		sb.append("\"RC\":"+rtnCause+",");
		sb.append("\"L\":"+alarmLevel+",");
		sb.append("\"CT\":\"" + cTime + "\"}");
		
		return sb.toString();
	}
	
	
	
	public int getTypeRef2() {
		return typeRef2;
	}



	public void setTypeRef2(int typeRef2) {
		this.typeRef2 = typeRef2;
	}



	public int getAlarmLevel() {
		return alarmLevel;
	}


	public void setAlarmLevel(int alarmLevel) {
		this.alarmLevel = alarmLevel;
	}


	public long getRtnTs() {
		return rtnTs;
	}

	public void setRtnTs(long rtnTs) {
		this.rtnTs = rtnTs;
	}

	public int getRtnCause() {
		return rtnCause;
	}

	public void setRtnCause(int rtnCause) {
		this.rtnCause = rtnCause;
	}

	public long getAckTs() {
		return ackTs;
	}

	public void setAckTs(long ackTs) {
		this.ackTs = ackTs;
	}

	public int getAckUserId() {
		return ackUserId;
	}

	public void setAckUserId(int ackUserId) {
		this.ackUserId = ackUserId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getfName() {
		return fName;
	}

	public void setfName(String fName) {
		this.fName = fName;
	}

	public Long getcTime() {
		return cTime;
	}

	public void setcTime(Long cTime) {
		this.cTime = cTime;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}


	public int getPointId() {
		return pointId;
	}

	public void setPointId(int pointId) {
		this.pointId = pointId;
	}

	public String getPointName() {
		return pointName;
	}

	public void setPointName(String pointName) {
		this.pointName = pointName;
	}

	public int getAcpId() {
		return acpId;
	}

	public void setAcpId(int acpId) {
		this.acpId = acpId;
	}

	public String getAcpName() {
		return acpName;
	}

	public void setAcpName(String acpName) {
		this.acpName = acpName;
	}

	public String getScopeId() {
		return scopeId;
	}

	public void setScopeId(String scopeId) {
		this.scopeId = scopeId;
	}



	@Override
	public String toString() {
		return "ScopeEvent [id=" + id + ", cTime=" + cTime + ", message="
				+ message + ", pointId=" + pointId + ", pointName=" + pointName
				+ ", acpId=" + acpId + ", acpName=" + acpName + ", fName="
				+ fName + ", scopeId=" + scopeId + "]\n";
	}
	
}