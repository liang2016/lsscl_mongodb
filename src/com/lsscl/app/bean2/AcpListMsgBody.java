package com.lsscl.app.bean2;

import java.util.ArrayList;
import java.util.List;

import com.lsscl.app.bean.MsgBody;

public class AcpListMsgBody extends MsgBody{
	private List<AcpInfo> acps = new ArrayList<AcpInfo>();
	
	@Override
	public String toJSON() {
		String json = "\"MSGBODY\":[";
		for(AcpInfo info:acps){
			json += info.toJson()+",";
		}
		if(acps.size()>0)json = json.substring(0,json.lastIndexOf(","));
		json +="]";
		return json;
	}

	public List<AcpInfo> getAcps() {
		return acps;
	}

	public void setAcps(List<AcpInfo> acps) {
		this.acps = acps;
	}
}
