package push;

import org.junit.Test;

import com.lsscl.app.util.AndroidNS;

public class AndroidNSTest {
	
	@Test
	public void jpushTest(){
		String userid = "000af77a233";
		String title = "标题";
		String content = "this is a notification!";
		
		AndroidNS.notification(userid, title, content);
	}

}
