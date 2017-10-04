package com.lsscl.app.bean2;

import java.util.ArrayList;
import java.util.List;

import com.lsscl.app.bean.MsgBody;

/**
 * 空压机点详情
 * @author yxx
 *
 */
public class AcpPointsMsgBody extends MsgBody{
	private List<PointValue> points = new ArrayList<PointValue>();
	private String currentId;
	private String currentValue;
	private String temperatureId;
	private String temperatureValue;
	private String pressureId;
	private String pressureValue;
	private AcpInfo acp;
	
	@Override
	public String toJSON() {
		String json = "\"MSGBODY\":{";
		if(acp!=null){
			json += "\"ACP\":"+acp.toJson()+",";
		}
		if(currentId!=null&&currentValue!=null){
			json += "\"CID\":"+currentId+",\"CVALUE\":\""+currentValue+"\",";
		}
		if(temperatureId!=null&&temperatureValue!=null){
			json += "\"TID\":"+temperatureId+",\"TVALUE\":\""+temperatureValue+"\",";
		}
		if(pressureId!=null&&pressureValue!=null){
			json += "\"PID\":"+pressureId+",\"PVALUE\":\""+pressureValue+"\",";
		}
		json += "\"POINTS\":[";
		for(PointValue pv:points){
			json += pv.toJson()+",";
		}
		if(points.size()>0)json = json.substring(0, json.lastIndexOf(","));
		json += "]}";
		return json;
	}

	public List<PointValue> getPoints() {
		return points;
	}

	public void setPoints(List<PointValue> points) {
		this.points = points;
	}

	public String getCurrentId() {
		return currentId;
	}

	public void setCurrentId(String currentId) {
		this.currentId = currentId;
	}

	public String getCurrentValue() {
		return currentValue;
	}

	public void setCurrentValue(String currentValue) {
		this.currentValue = currentValue;
	}

	public String getTemperatureId() {
		return temperatureId;
	}

	public void setTemperatureId(String temperatureId) {
		this.temperatureId = temperatureId;
	}

	public String getTemperatureValue() {
		return temperatureValue;
	}

	public void setTemperatureValue(String temperatureValue) {
		this.temperatureValue = temperatureValue;
	}

	public String getPressureId() {
		return pressureId;
	}

	public void setPressureId(String pressureId) {
		this.pressureId = pressureId;
	}

	public String getPressureValue() {
		return pressureValue;
	}

	public void setPressureValue(String pressureValue) {
		this.pressureValue = pressureValue;
	}

	public AcpInfo getAcp() {
		return acp;
	}

	public void setAcp(AcpInfo acp) {
		this.acp = acp;
	}
	
}
