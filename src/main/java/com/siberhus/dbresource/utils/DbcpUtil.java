package com.siberhus.dbresource.utils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

import com.siberhus.commons.properties.BeanPropertyUtils;
import com.siberhus.commons.properties.ConfigurationException;

public class DbcpUtil {

	private static final String PROPERTY_PREFIX = "dbcp.";
	
	private static final Map<String, ThreadLocal<Connection>> CONNECTION_MAP 
		= new HashMap<String, ThreadLocal<Connection>>();
	
	private static final Map<String, DataSource> DATA_SOURCE_MAP 
		= new HashMap<String, DataSource>();
	
	/**
	 * 
	 * @param databaseName
	 * @param properties
	 * @throws Exception
	 */
	public static synchronized void initializedDataSource(String databaseName, Properties properties) throws Exception{
		if(properties==null){
			throw new IllegalArgumentException("Properties cannot be null");
		}
		BasicDataSource dataSource = new BasicDataSource();
		if(!properties.containsKey(PROPERTY_PREFIX+"driverClassName")){
			throw new ConfigurationException("Missing required property: "+PROPERTY_PREFIX+"driverClassName");
		}
		if(!properties.containsKey(PROPERTY_PREFIX+"url")){
			throw new ConfigurationException("Missing required property: "+PROPERTY_PREFIX+"url");
		}
		if(!properties.containsKey(PROPERTY_PREFIX+"username")){
			throw new ConfigurationException("Missing required property: "+PROPERTY_PREFIX+"username");
		}
		if(!properties.containsKey(PROPERTY_PREFIX+"password")){
			throw new ConfigurationException("Missing required property: "+PROPERTY_PREFIX+"password");
		}
		BeanPropertyUtils.setUpBeanProperties(properties, dataSource, PROPERTY_PREFIX);
		DATA_SOURCE_MAP.put(databaseName, dataSource);
	}
	
	/**
	 * 
	 * @param properties
	 * @throws Exception
	 */
	public static void initializedDataSource(Properties properties) throws Exception{
		initializedDataSource(null, properties);
	}
	
	/**
	 * 
	 * @param propsFile
	 * @param encoding
	 * @throws IOException
	 */
	public static void writeConfigurationProperties(File propsFile, String encoding) throws IOException{
		BeanPropertyUtils.writeBeanProperties(BasicDataSource.class
				, true, PROPERTY_PREFIX, propsFile, encoding);
	}
	
	/**
	 * 
	 * @param databaseName
	 * @return
	 */
	public static synchronized DataSource getDataSource(String databaseName) {
		DataSource dataSource = DATA_SOURCE_MAP.get(databaseName);
		if(dataSource==null){
			throw new IllegalStateException("Database name:"+databaseName+" has not been initialized!");
		}
		return dataSource;
	}
	
	/**
	 * 
	 * @return
	 */
	public static DataSource getDataSource() {
		return getDataSource();
	}
	
	/**
	 * 
	 * @param databaseName
	 * @return
	 * @throws SQLException
	 */
	public static Connection getConnection(String databaseName) throws SQLException{
		DataSource dataSource = getDataSource(databaseName);
		ThreadLocal<Connection> threadLocal = getThreadLocalVariable(databaseName);
		Connection connection = threadLocal.get();
		if(connection==null){
			connection = dataSource.getConnection();
			threadLocal.set(connection);
		}
		return connection;
	}
	
	/**
	 * 
	 * @return
	 * @throws SQLException
	 */
	public static Connection getConnection() throws SQLException{
		return getConnection(null);
	}
	
	/**
	 * 
	 * @param databaseName
	 * @throws SQLException
	 */
	public static void closeConnection(String databaseName) throws SQLException{
		ThreadLocal<Connection> threadLocal = getThreadLocalVariable(databaseName);
		Connection connection = threadLocal.get();
		if(connection!=null){
			threadLocal.remove();
			connection.close();
		}
	}
	
	/**
	 * 
	 * @throws SQLException
	 */
	public static void closeConnection() throws SQLException{
		closeConnection(null);
	}
	
	/**
	 * 
	 * @param databaseName
	 */
	public static void closeConnectionQuietly(String databaseName){
		try{
			closeConnection(databaseName);
		}catch(Exception e){}
	}
	
	/**
	 * 
	 */
	public static void closeConnectionQuietly(){
		try{
			closeConnection(null);
		}catch(Exception e){}
	}
	
	/**
	 * 
	 * @param databaseName
	 * @return
	 */
	private static synchronized ThreadLocal<Connection> getThreadLocalVariable(String databaseName){
		ThreadLocal<Connection> threadLocal = CONNECTION_MAP.get(databaseName);
		if(threadLocal==null){
			threadLocal = new ThreadLocal<Connection>();
			CONNECTION_MAP.put(databaseName, threadLocal);
		}
		return threadLocal;
	}
	
	public static void main(String[] args) throws Exception {
		Properties prop = new Properties();
		prop.setProperty("dbcp.driverClassName", "org.hsqldb.jdbcDriver");
		prop.setProperty("dbcp.url", "jdbc:hsqldb:file:hsqldb/example.db");
		prop.setProperty("dbcp.username", "sa");
		prop.setProperty("dbcp.password", "");
		DbcpUtil.initializedDataSource("demo", prop);
		System.out.println(DbcpUtil.getDataSource("demo"));
		System.out.println(DbcpUtil.getConnection("demo"));
//		C3p0Util.writeConfigurationProperties(null, null);
	}
}
