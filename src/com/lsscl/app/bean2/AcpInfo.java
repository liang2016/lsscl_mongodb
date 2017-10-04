package com.lsscl.app.bean2;
/**
 * 空压机信息
 * @author yxx
 *
 */
public class AcpInfo {
	private String id;
	private String name;
	private String scopeId;
	private String scopeName;
	private boolean run;//运行状态 1运行 0：停止
	
	public String toJson(){
		String json = "{\"ID\":"+id+",\"NAME\":\""+name
				       +"\",\"SID\":"+scopeId
				       +",\"SNAME\":\""+scopeName
				       +"\",\"STATE\":"+(run?1:0)+"}";
		return json;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getScopeId() {
		return scopeId;
	}
	public void setScopeId(String scopeId) {
		this.scopeId = scopeId;
	}
	public boolean isRun() {
		return run;
	}
	public void setRun(boolean run) {
		this.run = run;
	}

	public String getScopeName() {
		return scopeName;
	}

	public void setScopeName(String scopeName) {
		this.scopeName = scopeName;
	}
	
}
