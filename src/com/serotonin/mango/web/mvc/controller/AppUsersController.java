package com.serotonin.mango.web.mvc.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import com.lsscl.app.bean.LoginUser;
import com.lsscl.app.dao.LoginUserDao;

public class AppUsersController extends ParameterizableViewController {
	private LoginUserDao dao = new LoginUserDao();
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Map<String, Object> model = new HashMap<String, Object>();
		List<LoginUser>users = dao.getAll();
		model.put("users", users);
        return new ModelAndView(getViewName(), model);
	}
}
