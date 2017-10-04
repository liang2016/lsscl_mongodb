package test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.serotonin.db.MappedRowCallback;
import com.serotonin.mango.db.dao.PointValueDao;
import com.serotonin.mango.rt.dataImage.IdPointValueTime;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.types.NumericValue;

public class PointValueDaoTest {
	private PointValueDao pointValueDao = new PointValueDao();
	@Test
	public void getPointValues(){
		List<PointValueTime> list = pointValueDao.getPointValues(112449, 1413770916000L);
		System.out.println(list.get(48));
	}
	
	@Test
	public void getPointValuesForWarnCode(){
		PointValueTime pvt = pointValueDao.getPointValuesForWarnCode(112437,1413770916000L);
		System.out.println(pvt.getTime());
	}

	@Test
	public void getPointValuesBetween(){
		List<PointValueTime> list = pointValueDao.getPointValuesBetween(112437, 1413770916000L,1413771116000L);
		for(int i=0;i<list.size();i++){
			PointValueTime pvt = list.get(i);
			System.out.println(pvt.getTime());
		}
	}
	
	@Test
	public void getPointValuesBetween2(){
		List<Integer>dataPointIds = new ArrayList<Integer>();
		dataPointIds.add(112449);
//		dataPointIds.add(112448);
		MappedRowCallback<IdPointValueTime> callback = new MappedRowCallback<IdPointValueTime>() {
			@Override
			public void row(IdPointValueTime pvt, int rowIndex) {
				System.out.println(pvt+","+rowIndex);
			}
		};
		long time = new Date().getTime();
		IdPointValueTime pvt = new IdPointValueTime(1,new NumericValue(10.1),time-100000000L);
		callback.row(pvt, 0);
		callback.row(pvt, 1);
		pointValueDao.getPointValuesBetween(dataPointIds, 1411897008000L, 1411897418000L, callback);
	}
	
	@Test
	public void listTest(){
		long time = new Date().getTime();
		IdPointValueTime pvt = new IdPointValueTime(1,new NumericValue(10.1),time-10000L);
		IdPointValueTime pvt1 = new IdPointValueTime(1,new NumericValue(10.2),time-15000L);
		IdPointValueTime pvt2 = new IdPointValueTime(1,new NumericValue(10.3),time-8000L);
		IdPointValueTime pvt3 = new IdPointValueTime(1,new NumericValue(10.4),time-7000L);
		IdPointValueTime pvt4 = new IdPointValueTime(1,new NumericValue(10.5),time-6000L);
		IdPointValueTime pvt5 = new IdPointValueTime(1,new NumericValue(10.6),time-18000L);
		List<IdPointValueTime> pvts = new ArrayList<IdPointValueTime>();
		pvts.add(pvt);pvts.add(pvt1);
		pvts.add(pvt2);pvts.add(pvt3);
		pvts.add(pvt4);pvts.add(pvt5);
		System.out.println(pvts);
	}
	@Test
	public void getEarliestTime(){
		 PointValueDao pointValueDao = new PointValueDao();
		 System.out.println(pointValueDao.getEarliestTime());
	}
}
