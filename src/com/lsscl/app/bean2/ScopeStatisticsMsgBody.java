package com.lsscl.app.bean2;

import com.lsscl.app.bean.MsgBody;

public class ScopeStatisticsMsgBody extends MsgBody{
	private int open;
	private int close;
	private String power;
	
	@Override
	public String toJSON() {
		String json = "\"MSGBODY\":{\"OPEN\":"+open
				            +",\"CLOSE\":"+close
				            +",\"POWER\":\""+power+"\"}";
		return json;
	}
	
	public int getOpen() {
		return open;
	}
	public void setOpen(int open) {
		this.open = open;
	}
	public int getClose() {
		return close;
	}
	public void setClose(int close) {
		this.close = close;
	}
	public String getPower() {
		return power;
	}
	public void setPower(String power) {
		this.power = power;
	}
}
