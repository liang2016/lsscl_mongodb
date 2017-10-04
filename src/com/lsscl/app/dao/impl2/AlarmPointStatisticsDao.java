package com.lsscl.app.dao.impl2;

import java.util.List;
import java.util.Map;

import com.lsscl.app.bean.QC;
import com.lsscl.app.bean.RSP;
import com.lsscl.app.bean2.AlarmPointStatisticsMsgBody;
import com.lsscl.app.dao.AppDao;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.vo.ResultData;

public class AlarmPointStatisticsDao extends AppDao{
	// select top 10 * from pointValues_10007 where ts >= 1432194689342 order by ts 
	// select top 10 * from pointValues_10007 where ts < 1432194689342 order by ts desc
	@Override
	public RSP getRSP(QC qc) {
		RSP rsp = new RSP(qc.getMsgId());
		String pid = qc.getMsgBody().get("PID");
		String time = qc.getMsgBody().get("TIME");
		String type = qc.getMsgBody().get("IMGTYPE");
		String count = qc.getMsgBody().get("COUNT");
		String table = "pointValues_"+pid;
		if(isTableExist(table)){
			String unit = getUnitByPid(Integer.valueOf(pid));
			long ltime = Long.valueOf(time);
			List<PointValueTime> pvts = pointValueDao.getPointValuesAt(Integer.valueOf(pid), ltime,Integer.valueOf(count));
			String sql = " select * from (select top "+count+" * from "+table+" where ts >= ? order by ts) as t1 " +
								"union all select * from (select top "+count+" * from "+table+" where ts < ? order by ts desc ) as t2";
			List<Map<String,Object>> points = ejt.query(sql, new Object[] {time,time},new ResultData());
			System.out.println(pvts.size());
			AlarmPointStatisticsMsgBody msgBody = new AlarmPointStatisticsMsgBody();
			msgBody.setPoints(pvts);
			msgBody.setSubTitle(unit);
			msgBody.setImageType(type);
			rsp.setMsgBody(msgBody);
		}
		return rsp;
	}

}
