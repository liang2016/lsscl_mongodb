/*
    LssclM2M - http://www.lsscl.com
    Copyright (C) 2006-2011 Lsscl ES Technologies Inc.
     
    
     
     
     
     

     
     
     
     

     
    
 */
package com.serotonin.mango.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.db.spring.ExtendedJdbcTemplate;
import com.serotonin.mango.Common;

/**
 *  
 */
abstract public class BasePooledAccess extends DatabaseAccess {
	private final Log log = LogFactory.getLog(BasePooledAccess.class);
	protected BasicDataSource dataSource;

	public BasePooledAccess(ServletContext ctx) {
		super(ctx);
	}

	@Override
	protected void initializeImpl(String propertyPrefix) {
		log.info("Initializing pooled connection manager");
//		dataSource = new BasicDataSource();
//		dataSource.setDriverClassName(getDriverClassName());
//		dataSource.setUrl(getUrl(propertyPrefix));
//		dataSource.setUsername(Common.getEnvironmentProfile().getString(
//				propertyPrefix + "db.username"));
//		dataSource.setPassword(getDatabasePassword(propertyPrefix));
//		dataSource.setMaxActive(Common.getEnvironmentProfile().getInt(
//				propertyPrefix + "db.pool.maxActive", 200));
//		dataSource.setMaxIdle(Common.getEnvironmentProfile().getInt(
//				propertyPrefix + "db.pool.maxIdle", 30));
		//117
		String host = "localhost";
		String user = "sa";
		String password = "lssclM2M";
		//lsscl
//		host = "www.lsscl.com";
//		user = "lsscl";
//		password = "123456";
		
		dataSource = new BasicDataSource();
		dataSource.setDriverClassName(getDriverClassName());
		dataSource.setUrl("jdbc:sqlserver://"+host+":1433; DatabaseName=irm2m2_IBox");
		dataSource.setUsername(user);
		dataSource.setPassword(password);
		dataSource.setMaxActive( 2000);
		dataSource.setMaxIdle( 2000);
	}

	protected String getUrl(String propertyPrefix) {
		return Common.getEnvironmentProfile().getString(
				propertyPrefix + "db.url");
	}

	abstract protected String getDriverClassName();

	@Override
	public void runScript(String[] script, OutputStream out) {
		ExtendedJdbcTemplate ejt = new ExtendedJdbcTemplate();
		ejt.setDataSource(dataSource);

		StringBuilder statement = new StringBuilder();

		for (String line : script) {
			// Trim whitespace
			line = line.trim();

			// Skip comments
			if (line.startsWith("--"))
				continue;

			statement.append(line);
			statement.append(" ");
			if (line.endsWith(";")) {
				// Execute the statement
				ejt.execute(statement.toString());
				statement.delete(0, statement.length() - 1);
			}
		}
	}

	protected void createSchema(String scriptFile) {
		BufferedReader in = new BufferedReader(new InputStreamReader(ctx
				.getResourceAsStream(scriptFile)));

		List<String> lines = new ArrayList<String>();
		try {
			String line;
			while ((line = in.readLine()) != null)
				lines.add(line);

			String[] script = new String[lines.size()];
			lines.toArray(script);
			runScript(script, null);
		} catch (IOException ioe) {
			throw new ShouldNeverHappenException(ioe);
		} finally {
			try {
				in.close();
			} catch (IOException ioe) {
				log.warn("", ioe);
			}
		}
	}

	@Override
	public void terminate() {
		log.info("Stopping database");
		try {
			dataSource.close();
		} catch (SQLException e) {
			log.warn("", e);
		}
	}

	@Override
	public DataSource getDataSource() {
		return dataSource;
	}

	@Override
	public File getDataDirectory() {
		return null;
	}
}
