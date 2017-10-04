package com.lsscl.app.bean;

import java.util.Date;

import com.lsscl.app.util.StringUtil;

/**
 * 登录用户信息
 * @author yxx
 *
 */
public class LoginUser {
	private int userId;
	private String userName;
	private String phoneno;
	private String email;
	private int scopeId;
	private String scopeName;
	private long lastLogin;
	private long login;
	private boolean online;
	private String token;
	private String token2;//android 需要两个参数发送通知
	private int deviceType;
	private String notificationType;//推送类型：
	private String deviceVersion;
	private long lastRspTime;
	
	public boolean isAndroidDevice(){
		return deviceType == 1;
	}
	
	public String getNotificationType() {
		return notificationType;
	}
	public void setNotificationType(String notificationType) {
		this.notificationType = notificationType;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPhoneno() {
		return phoneno;
	}
	public void setPhoneno(String phoneno) {
		this.phoneno = phoneno;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public int getScopeId() {
		return scopeId;
	}
	public void setScopeId(int scopeId) {
		this.scopeId = scopeId;
	}
	public String getScopeName() {
		return scopeName;
	}
	public void setScopeName(String scopeName) {
		this.scopeName = scopeName;
	}
	public String getLastLogin() {
		if(lastLogin==0)return "";
		return StringUtil.formatDate(new Date(lastLogin), "yyyy-MM-dd HH:mm:ss");
	}
	public void setLastLogin(long lastLogin) {
		this.lastLogin = lastLogin;
	}
	public String getLogin() {
		if(login==0)return "";
		return StringUtil.formatDate(new Date(login), "yyyy-MM-dd HH:mm:ss");
	}
	public String getLastRspTime() {
		if(lastRspTime==0)return "";
		return StringUtil.formatDate(new Date(lastRspTime), "yyyy-MM-dd HH:mm:ss");
	}
	public void setLogin(long login) {
		this.login = login;
	}
	public boolean isOnline() {
		return online;
	}
	public void setOnline(boolean online) {
		this.online = online;
	}
	@Override
	public String toString() {
		return "LoginUser [userId=" + userId + ", userName=" + userName
				+ ", phoneno=" + phoneno + ", email=" + email + ", scopeId="
				+ scopeId + ", scopeName=" + scopeName + ", lastLogin="
				+ lastLogin + ", login=" + login + ", online=" + online + "]";
	}
	public long getLoginTime() {
		return login;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public int getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(int deviceType) {
		this.deviceType = deviceType;
	}
	public String getDeviceVersion() {
		return deviceVersion;
	}
	public void setDeviceVersion(String deviceVersion) {
		this.deviceVersion = deviceVersion;
	}
	public String getToken2() {
		return token2;
	}
	public void setToken2(String token2) {
		this.token2 = token2;
	}

	

	public void setLastRspTime(long lastRspTime) {
		this.lastRspTime = lastRspTime;
	}
	
}
