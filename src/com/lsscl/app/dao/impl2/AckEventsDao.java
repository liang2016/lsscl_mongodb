package com.lsscl.app.dao.impl2;

import com.lsscl.app.bean.QC;
import com.lsscl.app.bean.RSP;
import com.lsscl.app.dao.AppDao;
import com.serotonin.mango.db.dao.EventDao;

public class AckEventsDao extends AppDao{
	private EventDao eventDao = new EventDao();
	private static final String SQL = "select id from users where phone = ? and password = ?";
	@Override
	public RSP getRSP(QC qc) {
		initData(qc);
		RSP rsp = new RSP(qc.getMsgId());
		String eids = qc.getMsgBody().get("EIDS");
		String []ids = {};
		if(eids==null)
			return error(rsp,"参数错误");
		ids = eids.split("-");
		int userId = ejt.queryForInt(SQL, new Object[]{qcPhoneNo,qcPassword},0);
		if(userId==0)
			return error(rsp,"用户名或密码错误");
		for(String id:ids){
			int eventId = Integer.valueOf(id);
			long time = System.currentTimeMillis();
			eventDao.ackEvent(eventId, time, userId, 0);
		}
		updateEventStatistics();
		return rsp;
	}


}
