package com.lsscl.app.dao;

import com.lsscl.app.dao.impl.ChangePwdDao;
import com.lsscl.app.dao.impl.CompressorAtrrNamesDao;
import com.lsscl.app.dao.impl.CompressorDetailsDao;
import com.lsscl.app.dao.impl.CompressorListDao;
import com.lsscl.app.dao.impl.ContactUsDao;
import com.lsscl.app.dao.impl.GetAllScopesDao;
import com.lsscl.app.dao.impl.MobileIndexDao;
import com.lsscl.app.dao.impl.MobileLoginDao;
import com.lsscl.app.dao.impl.MobileLogoutDao;
import com.lsscl.app.dao.impl.PointsStatisticsDao;
import com.lsscl.app.dao.impl.PointsWithin24HDao;
import com.lsscl.app.dao.impl.ScopeAlarmListDao;
import com.lsscl.app.dao.impl.ScopeIndexDao;
import com.lsscl.app.dao.impl.ScopeListDao;
import com.lsscl.app.dao.impl2.AckEventsDao;
import com.lsscl.app.dao.impl2.AcpListDao;
import com.lsscl.app.dao.impl2.AcpPointsDao;
import com.lsscl.app.dao.impl2.AlarmPointStatisticsDao;
import com.lsscl.app.dao.impl2.PointsIn24HDao;
import com.lsscl.app.dao.impl2.ScopeStatisticsDao;

public class AppDaoBuilder {

	public static final String ChangePwdApi = "PwdChange";
	public static final String LoginApi = "MobileLogin";
	public static final String LogoutApi = "MobileLogout";
	public static final String AckEvents = "AckEvents";
	public static AppDao create(String msgId){
		if(LoginApi.equals(msgId)) return new MobileLoginDao();
		if("MobileIndex".equals(msgId)) return new MobileIndexDao();
		if("AcpList".equals(msgId))return new AcpListDao();
		if("AcpPoints".equals(msgId))return new AcpPointsDao();
		if("ContactUs".equals(msgId))return new ContactUsDao();
		if(LogoutApi.equals(msgId))return new MobileLogoutDao();
		if("ScopeList".equals(msgId))return new ScopeListDao();
		if("ScopeIndex".equals(msgId))return new ScopeIndexDao();
		if("ScopeAlarmList".equals(msgId))return new ScopeAlarmListDao();
		if(ChangePwdApi.equals(msgId))return new ChangePwdDao();
		if("PointsWithin24H".equals(msgId))return new PointsWithin24HDao();
		if("PointsStatistics".equals(msgId))return new PointsStatisticsDao();
		if("ScopeStatistics".equals(msgId))return new ScopeStatisticsDao();
		if("AlarmPointStatistics".equals(msgId))return new AlarmPointStatisticsDao();
		if(AckEvents.equals(msgId))return new AckEventsDao();
		//老api
		if("CompressorAttrNames".equals(msgId))return new CompressorAtrrNamesDao();
		if("GetAllScopes".equals(msgId))return new GetAllScopesDao();
		if("CompressorList".equals(msgId))return new CompressorListDao();
		if("CompressorDetails".equals(msgId))return new CompressorDetailsDao();
		if("PointsIn24H".equals(msgId))return new PointsIn24HDao();
		return null;
	}
}
