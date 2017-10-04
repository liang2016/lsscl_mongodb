package test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.lsscl.app.bean.PointsStatisticsMsgBody;
import com.lsscl.app.bean.PointsWithin24MsgBody;
import com.lsscl.app.bean.QC;
import com.lsscl.app.bean.RSP;
import com.lsscl.app.bean2.AlarmPointStatisticsMsgBody;
import com.lsscl.app.dao.AppDao;
import com.lsscl.app.dao.AppDaoImpl;
import com.lsscl.app.dao.LoginUserDao;
import com.lsscl.app.dao.impl.CompressorAtrrNamesDao;
import com.lsscl.app.dao.impl.CompressorDetailsDao;
import com.lsscl.app.dao.impl.CompressorListDao;
import com.lsscl.app.dao.impl.ContactUsDao;
import com.lsscl.app.dao.impl.GetAllScopesDao;
import com.lsscl.app.dao.impl.MobileIndexDao;
import com.lsscl.app.dao.impl.MobileLoginDao;
import com.lsscl.app.dao.impl.PointsStatisticsDao;
import com.lsscl.app.dao.impl.PointsWithin24HDao;
import com.lsscl.app.dao.impl.ScopeAlarmListDao;
import com.lsscl.app.dao.impl.ScopeIndexDao;
import com.lsscl.app.dao.impl.ScopeListDao;
import com.lsscl.app.dao.impl.ScopeTreeDao;
import com.lsscl.app.dao.impl2.AcpListDao;
import com.lsscl.app.dao.impl2.AcpPointsDao;
import com.lsscl.app.dao.impl2.AlarmPointStatisticsDao;
import com.lsscl.app.dao.impl2.PointsIn24HDao;
import com.lsscl.app.dao.impl2.ScopeStatisticsDao;
import com.lsscl.app.util.StringUtil;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.serotonin.bacnet4j.service.acknowledgement.GetAlarmSummaryAck.AlarmSummary;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.PointValueDao;
import com.serotonin.mango.db.dao.UserDao;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.web.taglib.Functions;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class AppDaoTest {
	private AppDao dao = new AppDaoImpl();

	@Before
	public void init() {
		SQLServerDataSource dataSource = new SQLServerDataSource();
		dataSource
				.setURL("jdbc:sqlserver://192.168.1.116:1433; DatabaseName=lssclDB");
		dataSource.setUser("sa");
		dataSource.setPassword("123456");
	}

	@Test
	public void updateScopeStatistics() {
		dao.updateStatistics();
	}

	@Test
	public void scopeStatisticsTest() {
		dao = new ScopeStatisticsDao();
		QC qc = new QC();
		qc.setMsgId("ScopeStatistics");
		qc.getMsgBody().put("SCOPEID", "6");
		qc.getMsgBody().put("PHONENO", "15067118176");
		RSP rsp = dao.getRSP(qc);
		System.out.println(rsp.toJSON());
	}

	@Test
	public void isRootScope() {

		boolean b = dao.isRootScope("18753098391", "180");
		System.out.println(b);
	}

	@Test
	public void acpPointsTest() {
		dao = new AcpPointsDao();
		QC qc = new QC();
		qc.setMsgId("AcpPoints");
		qc.getMsgBody().put("AID", "21");
		qc.getMsgBody().put("PAGE", "4");
		qc.getMsgBody().put("PAGESIZE", "2");
		RSP rsp = dao.getRSP(qc);
		System.out.println(rsp.toJSON());
	}

	@Test
	public void acpListDaoTest() {
		dao = new AcpListDao();
		QC qc = new QC();
		qc.getMsgBody().put("SCOPEID", "6");
		qc.getMsgBody().put("PAGE", "1");
		qc.getMsgBody().put("PAGESIZE", "5");
		qc.getMsgBody().put("PHONENO", "15067118170");
		RSP rsp = dao.getRSP(qc);
		System.out.println(rsp.toJSON());
		System.out.println("hello".substring(0));
	}

	@Test
	public void mobleIndex() {
		dao = new MobileIndexDao();
		QC qc = new QC();
		qc.getMsgBody().put("SCOPEID", "181");
		RSP rsp = dao.getRSP(qc);
		System.out.println(rsp.toJSON());
	}

	@Test
	public void mobileLogin() {
		QC qc = new QC();
		qc.getMsgBody().put("PHONENO", "15067118176");
		qc.getMsgBody().put("PASSWORD", "123");
		RSP rsp = new MobileLoginDao().getRSP(qc);
		System.out.println(rsp.toJSON());
	}

	@Test
	public void compressorDetails() {
		long now = System.currentTimeMillis();
		QC qc = new QC();
		qc.setMsgId("CompressorDetails");
		qc.getMsgBody().put("SCOPEID", "181");
		qc.getMsgBody().put("COMPRESSORID", "16");
		qc.getMsgBody().put("VERSION", "1");
		dao = new CompressorDetailsDao();
		RSP rsp = dao.getRSP(qc);
		System.out.println(rsp.toJSON());
		System.out.println(System.currentTimeMillis() - now);
		// dao.getPointData(112449);
		// new DataPointDao().getDataPoint(112449);
	}

	@Test
	public void compressorAttrNames() {
		QC qc = new QC();
		qc.setMsgId("CompressorAttrNames");
		qc.getMsgBody().put("SCOPEID", "181");

		RSP rsp = new CompressorAtrrNamesDao().getRSP(qc);
		System.out.println(rsp.toJSON());
	}

	@Test
	public void compressorList() {
		QC qc = new QC();
		qc.setMsgId("compressorList");
		qc.getMsgBody().put("SCOPEID", "181");
		RSP rsp = new CompressorListDao().getRSP(qc);
		System.out.println(rsp.toJSON());
	}

	@Test
	public void contactInfo() {
		QC qc = new QC();
		qc.setMsgId("");
		qc.getMsgBody().put("PHONENO", "15067118176");
		qc.getMsgBody().put("PASSWORD", "123");
		qc.getMsgBody().put("CONTACTTEXT", "我的反馈");
		qc.getMsgBody().put("COMPRESSORID", "284");
		RSP rsp = new ContactUsDao().getRSP(qc);
		System.out.println(rsp);
	}

	@Test
	public void ScopeList() {
		QC qc = new QC();
		qc.setMsgId("ScopeList");
		qc.getMsgBody().put("SCOPEID", "6");
		qc.getMsgBody().put("PHONENO", "15067118176");
		qc.getMsgBody().put("PASSWORD", "123");
		qc.getMsgBody().put("ISROOT", "2");
		dao = new ScopeListDao();
		RSP rsp = dao.getRSP(qc);
		System.out.println(rsp.toJSON());
	}

	/**
	 * 区域报警列表测试
	 */
	@Test
	public void ScopeAlarmList() {
		QC qc = new QC();
		qc.setMsgId("ScopeAlarmList");
		qc.getMsgBody().put("SCOPEID", "1");
		qc.getMsgBody().put("LEVEL", "0");
		qc.getMsgBody().put("STARTINDEX", "0");
		qc.getMsgBody().put("PAGESIZE", "20");
		qc.getMsgBody().put("PHONENO", "15067118176");
		dao = new ScopeAlarmListDao();
		RSP rsp = dao.getRSP(qc);
		System.out.println(rsp.toJSON());
	}

	@Test
	public void getScopeEvents() {
		long after = 1432541521000L;
		Map<String, Set<String>> maps = dao.getScopeEvents(after);
		System.out.println(maps);
	}

	@Test
	public void ScopeIndex() {
		QC qc = new QC();
		qc.setMsgId("ScopeIndex");
		qc.getMsgBody().put("SCOPEID", "1");
		RSP rsp = new ScopeIndexDao().getRSP(qc);
		System.out.println(rsp.toJSON());
	}

	@Test
	public void sendMail() {
		// dao.sendMail("15067118171","123");
	}

	/**
	 * 报警线程
	 */
	@Test
	public void events() {
		List<Integer> ids = new DataPointDao().getDataPointIds();
		int i = 0;
		for (Integer id : ids) {
			DataPointVO dp = new DataPointDao().getDataPoint(id);
			if ("common.dataTypes.binary".equals(dp.getDataTypeMessage()
					.getKey())) {// 二进制数据
				i++;
				dao.saveMobileEvent(id);

			}
		}
	}

	private void toXml(RSP rsp) {
		try {

			Configuration cfg = new Configuration();
			cfg.setDirectoryForTemplateLoading(new File("src"));
			Template temp = cfg.getTemplate("RSP.xml");
			Map root = new HashMap();
			root.put("rsp", rsp);
			/* 合并数据模型和模版 */
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Writer out = new OutputStreamWriter(baos);
			temp.process(root, out);
			out.flush();
			System.out.println(baos.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TemplateException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void makeEvent() {
		int scopeId = 11;
		dao.makeEvent(scopeId, 1);
	}

	@Test
	public void testPhonesByPid() {
		Set set = new HashSet();
		set.add("6974ac11 870e09fa 00e2238e 8cfafc7d 2052e342 182f5b57 fabca445 42b72e1b");
		set.add("6974ac11 870e09fa 00e2238e 8cfafc7d 2052e342 182f5b57 fabca445 42b72e1b");
		System.out.println(set.size());
	}

	@Test
	public void testScopeTree() {
		QC qc = new QC();
		qc.setMsgId("ScopeList");
		qc.getMsgBody().put("SCOPEID", "1");
		qc.getMsgBody().put("PHONENO", "15067118176");
		qc.getMsgBody().put("PASSWORD", "111");
		dao = new ScopeTreeDao();
		RSP rsp = dao.getRSP(qc);
		System.out.println(rsp.toJSON());
	}

	@Test
	public void testUserDao() {
		System.out.println(UserDao.getUsersAndParentUsers);
	}

	@Test
	public void getAllScopes() {
		QC qc = new QC();
		qc.setMsgId("GetAllScopes");
		qc.getMsgBody().put("PHONENO", "15067118176");
		qc.getMsgBody().put("VERSION", "0");
		dao = new GetAllScopesDao();
		RSP rsp = dao.getRSP(qc);
		System.out.println(rsp.toJSON());
	}

	@Test
	public void pointsWith24H() throws FileNotFoundException, ParseException {
		QC qc = new QC();
		qc.setMsgId("PointsWith24H");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String stime = dateFormat.parse("2015-07-31").getTime() + "";
		qc.getMsgBody().put("PID", "13854");
		qc.getMsgBody().put("STIME", stime);
		qc.getMsgBody().put("VERSION", "0");
		qc.getMsgBody().put("IMGTYPE", "bar");// 统计图类型
		System.out.println(qc.toString());
		// dao = new PointsWithin24HDao();
		// RSP rsp = dao.getRSP(qc);
		// PointsWithin24MsgBody msgBody = (PointsWithin24MsgBody)
		// rsp.getMsgBody();
		// System.out.println(msgBody.toJson());
		// FileOutputStream fos = new FileOutputStream(new File("D:/a.png"));
		// msgBody.toImage(fos);
	}

	@Test
	public void pointsIn24H() {
		try {
			QC qc = new QC();
			qc.setMsgId("PointsWith24H");
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			// String stime = dateFormat.parse("2015-07-31").getTime()+"";
			qc.getMsgBody().put("PID", "112517");
			qc.getMsgBody().put("STIME", "1438585300000");
			qc.getMsgBody().put("VERSION", "0");
			qc.getMsgBody().put("IMGTYPE", "bar");// 统计图类型
			System.out.println(qc.toString());
			dao = new PointsIn24HDao();
			RSP rsp = dao.getRSP(qc);
			PointsWithin24MsgBody msgBody = (PointsWithin24MsgBody) rsp
					.getMsgBody();
			byte[] result = rsp.toJSON().getBytes();
			System.out.println(msgBody.toJSON());
			// System.out.println(rsp.toJSON());
			System.out.println(result.length);
			// System.out.println("gzip"+new String(StringUtil.gZip(result)));

			byte[] bytes = StringUtil.unGZip(StringUtil.gZip(result));
			System.out.println("unGzip:" + new String(bytes, "utf-8"));
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		// FileOutputStream fos = new FileOutputStream(new File("D:/a.png"));
		// msgBody.toImage(fos);
	}


	@Test
	public void pointsStatistics() throws FileNotFoundException, ParseException {
		QC qc = new QC();
		qc.setMsgId("pointsStatistics");
		qc.getMsgBody().put("PID", "112450");
		qc.getMsgBody().put("COUNT", "100");
		qc.getMsgBody().put("IMGTYPE", "bar");// 统计图类型
		dao = new PointsStatisticsDao();
		RSP rsp = dao.getRSP(qc);
		long s = System.currentTimeMillis();
		PointsStatisticsMsgBody msgBody = (PointsStatisticsMsgBody) rsp
				.getMsgBody();
		System.out.println("time:" + (System.currentTimeMillis() - s));
		FileOutputStream fos = new FileOutputStream(new File(
				"D:/PointsStatisticsMsgBody.png"));
		msgBody.toImage(fos);
	}

	@Test
	public void alarmPointStatistics() throws FileNotFoundException,
			ParseException {
		QC qc = new QC();
		qc.setMsgId("alarmPointStatistics");
		qc.getMsgBody().put("PID", "181974");
		qc.getMsgBody().put("TIME", "1437875730113");
		qc.getMsgBody().put("IMGTYPE", "line");// 统计图类型
		qc.getMsgBody().put("COUNT", "3");
		dao = new AlarmPointStatisticsDao();
		RSP rsp = dao.getRSP(qc);
		long s = System.currentTimeMillis();
		AlarmPointStatisticsMsgBody msgBody = (AlarmPointStatisticsMsgBody) rsp
				.getMsgBody();
		System.out.println("time:" + (System.currentTimeMillis() - s));
		FileOutputStream fos = new FileOutputStream(new File(
				"D:/alarmPointStatistics.png"));
		msgBody.toImage(fos);
	}

	@Test
	public void loginUserDaoTets() {
		LoginUserDao dao = new LoginUserDao();
		System.out.println(dao.isOffine("f"));
	}

	@Test
	public void testPoint() {
		DataPointDao dao = new DataPointDao();
		PointValueDao pvDao = new PointValueDao();

		DataPointVO dpv = dao.getDataPoint(20122);
		PointValueTime pvt = pvDao.getLatestPointValue(20122);
		System.out.println(pvt.getStringValue());
		System.out.println(Functions.getRenderedText(dpv, pvt));
	}
}
