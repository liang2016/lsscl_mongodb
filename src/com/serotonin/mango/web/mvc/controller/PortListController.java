package com.serotonin.mango.web.mvc.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import com.lsscl.app.util.StringUtil;
import com.serotonin.mango.db.dao.DataSourceDao;

public class PortListController extends ParameterizableViewController {
	private DataSourceDao dao = new DataSourceDao();
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Map<String, Object> model = new HashMap<String, Object>();
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		String port = request.getParameter("port");
		Map<String, Object> queryParam = new HashMap<String, Object>();
		String pageString = request.getParameter("page");
		String pageSizeString = request.getParameter("pageSizeString");
		String name = request.getParameter("name");
		int page = pageString == null ? 1 : Integer.valueOf(pageString);

		int pageSize = pageSizeString == null ? 10 : Integer
				.valueOf(pageSizeString);

		queryParam.put("pageSize", pageSize);
		queryParam.put("name", name);
		queryParam.put("port", port);
		int count = dao.getCountByMap(queryParam);
		int totalPage = count % pageSize == 0 ? count / pageSize : count
				/ pageSize + 1;
		if (!"".equals(port))
			totalPage = 1;
		if (page > totalPage)
			page = totalPage;
		if (page < 1)
			page = 1;
		queryParam.put("page", page);
		StringUtil.trimMap(queryParam);
		queryParam.put("totalPage", totalPage);
		List<Map<String, Object>> ports = dao.getDataSourceByMap(queryParam);
		model.put("query", queryParam);
		model.put("ports", ports);
        return new ModelAndView(getViewName(), model);
	}
}
