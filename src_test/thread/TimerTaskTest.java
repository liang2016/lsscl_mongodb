package thread;

import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.lsscl.app.util.StringUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.serotonin.mango.db.dao.PointsInstance;

public class TimerTaskTest {
	
	public void test(){
		new Timer()
		.schedule(
				new TimerTask(){
			@Override
			public void run() {
				System.out.println(StringUtil.formatDate(new Date(), "HH:mm:ss"));
				System.out.println(Thread.currentThread());
			}}, 1000,2000);
	}
	public static void main(String[] args) {
		for(int i=0;i<100;i++){
			new Thread(new Runnable() {
				@Override
				public void run() {
					DBObject obj = new BasicDBObject();
					obj.put("pid", Thread.currentThread()+"");
					PointsInstance.getInstance().addPoint(new Random().nextInt(100), obj);
				}
			}).start();
		}
		new Timer()
		.schedule(
				new TimerTask(){
			@Override
			public void run() {
				System.out.println(PointsInstance.getInstance().getPoints().size());
				System.out.println(PointsInstance.getInstance().getPoints());
			}}, 1,2000);
		
	}
}
