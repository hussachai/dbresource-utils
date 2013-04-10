package com.siberhus.dbresource.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;

import com.siberhus.commons.util.IConfigurator;

public class HibernateUtil {
	
	private static final Map<String, ThreadLocal<Session>> SESSION_MAP 
		= new HashMap<String, ThreadLocal<Session>>();
	
	private static final Map<String, Configuration> CONFIG_MAP 
		= new HashMap<String, Configuration>();
	
	private static final Map<String, SessionFactory> SESSION_FACTORY_MAP 
		= new HashMap<String, SessionFactory>();
	
	/**
	 * 
	 * @param persistenceUnitName
	 * @param configurator
	 * @return
	 * @throws HibernateException
	 */
	public synchronized static Configuration initializeConfiguration(String persistenceUnitName
			, IConfigurator<Configuration> configurator) throws HibernateException {
		
		Configuration config = configurator.configure();
		CONFIG_MAP.put(persistenceUnitName, config);
		return config;
	}
	
	/**
	 * 
	 * @param configurator
	 * @return
	 * @throws HibernateException
	 */
	public static Configuration initializeConfiguration(
			IConfigurator<Configuration> configurator) throws HibernateException {
		
		return initializeConfiguration(null, configurator);
	}
	
	/**
	 * Use the mappings and properties specified in the given application resource. 
	 * The format of the resource is defined in hibernate-configuration-3.0.dtd. 
	 * The resource is found via getConfigurationInputStream(resource). 
	 * @param persistenceUnitName
	 * @param resource
	 * @return
	 */
	public synchronized static Configuration initializeConfiguration(
			String persistenceUnitName, File configFile) throws HibernateException{
		
		AnnotationConfiguration config = new AnnotationConfiguration();
		if(configFile!=null){
			config.configure(configFile);
		}else{
			config.configure();
		}
		CONFIG_MAP.put(persistenceUnitName, config);
		return config;
	}
	
	/**
	 * 
	 * @param configFile
	 * @return
	 * @throws HibernateException
	 */
	public static Configuration initializeConfiguration(File configFile) throws HibernateException{
		
		return initializeConfiguration(null, configFile);
	}
	
	/**
	 * 
	 * @param persistenceUnitName
	 * @param properties
	 * @param persistentClasses
	 * @return
	 * @throws HibernateException
	 */
	public synchronized static Configuration initializeConfiguration(String persistenceUnitName
			,Properties properties, Class[] persistentClasses, boolean annot) throws HibernateException{
		
		AnnotationConfiguration config = new AnnotationConfiguration();
		if(properties!=null){
			config.setProperties(properties);
		}
		if(persistentClasses!=null){
			if(annot){
				for(Class persistentClass : persistentClasses){
					config.addAnnotatedClass(persistentClass);
				}
			}else{
				for(Class persistentClass : persistentClasses){
					config.addClass(persistentClass);
				}
			}
		}
		CONFIG_MAP.put(persistenceUnitName, config);
		return config;
	}
	
	/**
	 * 
	 * @param properties
	 * @param persistentClasses
	 * @return
	 * @throws HibernateException
	 */
	public static Configuration initializeConfiguration(Properties properties
			,Class[] persistentClasses, boolean annot) throws HibernateException {
		
		return initializeConfiguration(null, properties, persistentClasses, annot);
	}
	
	/**
	 * 
	 * @param persistenceUnitName
	 * @return
	 * @throws HibernateException
	 */
	public synchronized static SessionFactory getSessionFactory(String persistenceUnitName) throws HibernateException{
		SessionFactory sessionFactory = SESSION_FACTORY_MAP.get(persistenceUnitName);
		if(sessionFactory==null){
			Configuration config = CONFIG_MAP.get(persistenceUnitName);
			if(config==null){
				throw new IllegalStateException("Persistence unit name: "+persistenceUnitName+" has not been initialized!");
			}
			sessionFactory = config.buildSessionFactory();
			SESSION_FACTORY_MAP.put(persistenceUnitName,sessionFactory);
		}
		return sessionFactory;
	}
	
	/**
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static SessionFactory getSessionFactory() throws HibernateException{
		
		return getSessionFactory(null);
	}
	
	/**
	 * Gets Hibernate Session for current thread. When finished, 
	 * users must return session using {@link #closeSession() closeSession()} method.
	 * This method compatible with getSessionFactory().getCurrentSession() or getSessionFactory(null).getCurrentSession();
	 * where hibernate.current_session_context_class property is thread.
	 * 
	 * @return Hibernate Session for current thread.
	 * @throws HibernateException if there is an error opening a new session.
	 */
	public static Session getSession(String persistenceUnitName) throws HibernateException{
		ThreadLocal<Session> threadLocal = getThreadLocalVariable(persistenceUnitName);
		Session session = threadLocal.get();
		if(session==null){
			session = getSessionFactory(persistenceUnitName).openSession();
			threadLocal.set(session);
		}
		return session;
	}
	
	/**
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static Session getSession() throws HibernateException{
		return getSession(null);
	}
	
	/**
	 * Closes the Hibernate Session. Users must call this method after calling
	 * {@link #getSession() getSession()}.
	 * @throws HibernateException if session has problem closing.
	 */
	public static void closeSession(String persistenceUnitName) throws HibernateException{
		ThreadLocal<Session> threadLocal = getThreadLocalVariable(persistenceUnitName);
		Session session = threadLocal.get();
		if(session!=null){
			session.close();
			threadLocal.remove();
		}
	}
	
	/**
	 * 
	 * @throws HibernateException
	 */
	public static void closeSession() throws HibernateException{
		closeSession(null);
	}
	
	/**
	 * 
	 * @throws HibernateException
	 */
	public static void beginTransaction(String persistenceUnitName) throws HibernateException{
		getSession(persistenceUnitName).getTransaction().begin();
	}
	
	/**
	 * 
	 * @throws HibernateException
	 */
	public static void beginTransaction() throws HibernateException{
		getSession(null).getTransaction().begin();
	}
	
	/**
	 * 
	 * @throws HibernateException
	 */
	public static void commitTransaction(String persistenceUnitName) throws HibernateException{
		getSession(persistenceUnitName).getTransaction().commit();
	}
	
	/**
	 * 
	 * @throws HibernateException
	 */
	public static void commitTransaction() throws HibernateException{
		getSession(null).getTransaction().commit();
	}
	
	/**
	 * 
	 * @throws HibernateException
	 */
	public static void rollbackTransaction(String pesistenceUnitName) throws HibernateException{
		getSession(pesistenceUnitName).getTransaction().rollback();
	}
	
	/**
	 * 
	 * @throws HibernateException
	 */
	public static void rollbackTransaction() throws HibernateException{
		getSession(null).getTransaction().rollback();
	}
	
	/**
	 * 
	 * @param persistenceUnitName
	 * @return
	 */
	private static synchronized ThreadLocal<Session> getThreadLocalVariable(String persistenceUnitName){
		ThreadLocal<Session> threadLocal = SESSION_MAP.get(persistenceUnitName);
		if(threadLocal==null){
			threadLocal = new ThreadLocal<Session>();
			SESSION_MAP.put(persistenceUnitName, threadLocal);
		}
		return threadLocal;
	}
	
	static class T extends Thread{
		public void run(){
			System.out.println( HibernateUtil.getSession("simple").connection() );
		}
	}
	
}
