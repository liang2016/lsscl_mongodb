package com.lsscl.app.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 空压机详情
 * 
 * @author yxx
 * 
 */
public class CompressorDetailsMsgBody extends MsgBody implements Serializable {
	private static final long serialVersionUID = 2524183064989703805L;

	private String compressorId;
	private String compressorName;// 空压机名
	private String time;// 服务器反馈时间
	private Map<String, Map<String,String>> dataPoints = new HashMap<String, Map<String,String>>();
	private String exhausTemperature;
	private String exhausPressure;
	private String current;
	private int currentId;//电流属性点id
	private int exhausTemperatureId;//排气温度属性点id
	private int exhausPressureId;//排气压力属性点id
	private int version;// 协议版本

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getCurrent() {
		return current;
	}

	public void setCurrent(String current) {
		this.current = current;
	}

	public String getExhausPressure() {
		return exhausPressure;
	}

	public void setExhausPressure(String exhausPressure) {
		this.exhausPressure = exhausPressure;
	}

	public String getExhausTemperature() {
		return exhausTemperature;
	}

	public void setExhausTemperature(String exhausTemperature) {
		this.exhausTemperature = exhausTemperature;
	}

	public String getCompressorId() {
		return compressorId;
	}

	public void setCompressorId(String compressorId) {
		this.compressorId = compressorId;
	}

	public String getCompressorName() {
		return compressorName;
	}

	public void setCompressorName(String compressorName) {
		this.compressorName = compressorName;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public Map<String, Map<String, String>> getDataPoints() {
		return dataPoints;
	}

	public void setDataPoints(Map<String, Map<String, String>> dataPoints) {
		this.dataPoints = dataPoints;
	}

	public String toJSON() {
		StringBuilder json = new StringBuilder();
		json.append("\"MSGBODY\":{");
		json.append("\"COMPRESSORNAME\":\"" + this.compressorName + "\",");
		json.append("\"TIME\":\"" + this.time + "\",");
		json.append("\"EXHAUSTEMPERATURE\":\"" + this.exhausTemperature + "\",");
		json.append("\"EXHAUSPRESSURE\":\"" + this.exhausPressure + "\",");
		json.append("\"EXHAUSTEMPERATUREID\":" + this.exhausTemperatureId + ",");
		json.append("\"EXHAUSPRESSUREID\":" + this.exhausPressureId + ",");
		json.append("\"CURRENTID\":" + this.currentId + ",");
		json.append("\"CURRENT\":\"" + this.current+ "\"");
		Set<String> keys = this.dataPoints.keySet();
		if (version==0) {
			for (String key : keys) {
				Map<String,String>obj = this.dataPoints.get(key);
				json.append(",\"" + key + "\":\"" + obj.get("value")
						+ "\"");
			}
		} else if(version==1){// 有顺序的点信息
			json.append(",\"POINTS\":[");
			for (String key : keys) {
				Map<String,String>obj = this.dataPoints.get(key);
				json.append("{\"PID\":\"" + key + "\",\"VALUE\":\"" + obj.get("value")
						+ "\",\"STATE\":"+obj.get("state")+"},");
			}
			if (keys.size() > 0) {
				json.deleteCharAt(json.lastIndexOf(","));
			}
			json.append("]");
		}
		json.append("}");
		return json.toString();
	}

	@Override
	public String toString() {
		return "CompressorDetailsMsgBody [compressorId=" + compressorId
				+ ", compressorName=" + compressorName + ", time=" + time
				+ ", dataPoints=" + dataPoints + "]";
	}

	public int getCurrentId() {
		return currentId;
	}

	public void setCurrentId(int currentId) {
		this.currentId = currentId;
	}

	public int getExhausTemperatureId() {
		return exhausTemperatureId;
	}

	public void setExhausTemperatureId(int exhausTemperatureId) {
		this.exhausTemperatureId = exhausTemperatureId;
	}

	public int getExhausPressureId() {
		return exhausPressureId;
	}

	public void setExhausPressureId(int exhausPressureId) {
		this.exhausPressureId = exhausPressureId;
	}

}
