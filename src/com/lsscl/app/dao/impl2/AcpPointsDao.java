package com.lsscl.app.dao.impl2;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.lsscl.app.bean.QC;
import com.lsscl.app.bean.RSP;
import com.lsscl.app.bean2.AcpInfo;
import com.lsscl.app.bean2.AcpPointsMsgBody;
import com.lsscl.app.bean2.PointValue;
import com.lsscl.app.dao.AppDao;
import com.lsscl.app.util.AcpConfig;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.PointValueDao;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.web.taglib.Functions;

public class AcpPointsDao extends AppDao {
	private PointValueDao pointValueDao = new PointValueDao();
	private DataPointDao dataPointDao = new DataPointDao();

	@Override
	public RSP getRSP(QC qc) {
		RSP rsp = new RSP(qc.getMsgId());
		String acpId = qc.getMsgBody().get("AID");
		rsp = new RSP(qc.getMsgId());

		Map<String, String> acpInfo = getAcpInfoById(acpId);
		List<Map<String, Object>> list = ejt
				.query(acp_attr2, new Object[] { acpId },
						new com.serotonin.mango.vo.ResultData());
		AcpPointsMsgBody msg = new AcpPointsMsgBody();
		AcpInfo acp = new AcpInfo();
		acp.setId(acpId);
		acp.setName(acpInfo.get("name"));
		long now = new Date().getTime();
		/**
		 * 读取点值
		 */
		for (Map<String, Object> m : list) {
			String name = (String) m.get("name");
			if (isBasicPointName(name))
				continue;
			PointValue acpPoint = new PointValue();
			Object pid = m.get("pointId");
			acpPoint.setPid(pid+"");
			acpPoint.setPname(name);
			Integer dpid = (Integer) pid;
//			if(!isTableExist("pointValues_"+dpid))continue;
			DataPointVO dpv = dataPointDao.getDataPoint(dpid);
			PointValueTime pvt2 = Common.ctx.getRuntimeManager().getDataPoint(dpid).getPointValue();
			PointValueTime pvt = pointValueDao.getLatestPointValue(dpid);
			if(pvt==null)continue;
			acpPoint.setRun((now-pvt.getTime())<2*60*1000);
			acpPoint.setValue(Functions.getRenderedText(dpv, pvt));
			
			msg.getPoints().add(acpPoint);
		}
		// 基本属性点：电流、压力、温度
		String data1 = getPointValueByAcpIdAndName(acpId,
				AcpConfig.EXHAUSTEMPERATURE);
		msg.setTemperatureValue(data1 == null ? " " : data1);
		// 排气压力
		data1 = getPointValueByAcpIdAndName(acpId, AcpConfig.EXHAUSPRESSURE);
		msg.setPressureValue(data1 == null ? " " : data1);

		// 电流
		String current = getPointValueByAcpIdAndName(acpId, AcpConfig.CURRENT);
		data1 = current;
		msg.setCurrentValue(data1 == null ? "   " : data1);
		msg.setCurrentId(getPidByAcpIdAndName(acpId, AcpConfig.CURRENT) + "");
		msg.setPressureId(getPidByAcpIdAndName(acpId, AcpConfig.EXHAUSPRESSURE)
				+ "");
		msg.setTemperatureId(getPidByAcpIdAndName(acpId,
				AcpConfig.EXHAUSTEMPERATURE) + "");
		rsp.setMsgBody(msg);

		return rsp;
	}

}
