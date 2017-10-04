package com.lsscl.app.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.LinkedMap;

import com.lsscl.app.bean.PointsWithin24MsgBody;
import com.lsscl.app.bean.QC;
import com.lsscl.app.bean.RSP;
import com.lsscl.app.dao.AppDao;
import com.lsscl.app.util.StringUtil;
import com.serotonin.mango.DataTypes;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.PointValueDao;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.view.text.TextRenderer;
import com.serotonin.mango.vo.DataPointVO;

/**
 * 24小时内点统计
 * 
 * @author yxx
 * 
 */
public class PointsWithin24HDao extends AppDao {

	@Override
	public RSP getRSP(QC qc) {
		String stime = qc.getMsgBody().get("STIME");
		if(stime==null)return null;
		Date now = new Date(Long.parseLong(stime));
		long gt = now.getTime();
		long le = gt + 24 * 60 * 60 * 1000;

		RSP rsp = new RSP(qc.getMsgId());
		String pid = qc.getMsgBody().get("PID");
		int pointId = Integer.parseInt(pid);
		DataPointVO pv = new DataPointDao().getDataPoint(pointId);
		TextRenderer tr = pv.getTextRenderer();
		List<PointValueTime> pvts = pointValueDao.getPointValuesBetween(pointId, gt, le);
		String unit = getUnitByPid(pointId);
		PointsWithin24MsgBody msgBody = new PointsWithin24MsgBody();
		msgBody.setTitle("");
		msgBody.setSubTitle(unit);
		msgBody.setPoints(pvts);
		int type = pvts.size()>0?pvts.get(0).getValue().getDataType():0;
		msgBody.setDataType(type);
		msgBody.setImageType(qc.getMsgBody().get("IMGTYPE"));
		rsp.setMsgBody(msgBody);
		// 生成统计图表
		return rsp;
	}

	private String buildSql(int pointId,long le, long gt) {
		String sql = "select id,pointValue,dataType,ts from pointValues_"+pointId+" where ts > "+gt+" and ts < "+le;
		System.out.println(sql);
		return sql;
	}
}
