package com.lsscl.app.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.lsscl.app.bean.LoginUser;
import com.lsscl.app.dao.LoginUserDao;
import com.serotonin.mango.Common;
import com.serotonin.mango.vo.User;

public class AppUsersServlet extends HttpServlet {
	private LoginUserDao dao = new LoginUserDao();
	

	/**
	 * The doGet method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = Common.getUser(request);
		if(user==null||!user.isAdmin()){
			response.sendRedirect("/login.htm");
			return;
		}
		List<LoginUser>users = dao.getAll();
		request.setAttribute("users", users);
		request.getRequestDispatcher("/WEB-INF/jsp/appUser.jsp").forward(
				request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
	}

}
