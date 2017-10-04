package com.lsscl.app.dao.impl;

import java.util.List;

import com.lsscl.app.bean.PointsStatisticsMsgBody;
import com.lsscl.app.bean.QC;
import com.lsscl.app.bean.RSP;
import com.lsscl.app.dao.AppDao;
import com.serotonin.mango.DataTypes;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.PointValueDao;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.view.text.TextRenderer;
import com.serotonin.mango.vo.DataPointVO;

public class PointsStatisticsDao extends AppDao {

	@Override
	public RSP getRSP(QC qc) {
		RSP rsp = new RSP(qc.getMsgId());
		String pid = qc.getMsgBody().get("PID");
		String count = qc.getMsgBody().get("COUNT");
		int pointId = Integer.parseInt(pid);
		DataPointVO pv = new DataPointDao().getDataPoint(pointId);
		List<PointValueTime> pvts = pointValueDao.getLatestPointValues(pointId, Integer.valueOf(count));
		String unit = getUnitByPid(pointId);
		Integer type = 0;
		if(pvts.size()!=0)type = pvts.get(0).getValue().getDataType();
		
		PointsStatisticsMsgBody msgBody = new PointsStatisticsMsgBody();
		msgBody.setTitle("");
		msgBody.setSubTitle(unit);
		msgBody.setPoints(pvts);
		msgBody.setDataType(type);
		msgBody.setImageType(qc.getMsgBody().get("IMGTYPE"));
		rsp.setMsgBody(msgBody);

		return rsp;
	}

}
