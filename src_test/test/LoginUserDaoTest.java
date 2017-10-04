package test;

import java.util.List;

import org.junit.Test;

import com.lsscl.app.bean.LoginUser;
import com.lsscl.app.dao.LoginUserDao;


public class LoginUserDaoTest {
	LoginUserDao dao = new LoginUserDao();
	@Test
	public void getById()throws Exception{
		LoginUser user = dao.getById(5);
		System.out.println(user);
	}

	@Test
	public void getAll()throws Exception{
		List<LoginUser> users = dao.getAll();
		System.out.println(users.size());
	}
}
