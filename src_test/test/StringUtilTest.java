package test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import com.lsscl.app.util.StringUtil;

public class StringUtilTest {
	@Test
	public void testFormat(){
//		System.out.println("hello".substring(0, 10));
		System.out.println(StringUtil.formatNumber("3.1455926", "0.00"));
	}

	@Test
	public void testTime(){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		System.out.println("morning:"+format.format(new Date(getTimesmorning())));
		System.out.println("month:"+format.format(new Date(getMonthTime())));
		System.out.println("yesterday:"+format.format(new Date(getYesterdayTime())));
	}
	/**
     * 获取零点的时间戳
     * @return
     */
    public static long getMorningTime(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /**
     * 获取月初时间戳
     * @return
     */
    public static long getMonthTime(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DAY_OF_MONTH,1);
        return cal.getTimeInMillis();
    }

    /**
     * 获取月初时间戳
     * @return
     */
    public static long getYesterdayTime(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, -24);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}
