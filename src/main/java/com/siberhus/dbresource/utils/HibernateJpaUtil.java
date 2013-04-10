package com.siberhus.dbresource.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.HibernateException;
import org.hibernate.ejb.Ejb3Configuration;

import com.siberhus.commons.util.IConfigurator;

public class HibernateJpaUtil {
	
	private static final Map<String, ThreadLocal<EntityManager>> ENTITY_MANAGER_MAP 
		= new HashMap<String, ThreadLocal<EntityManager>>();
	
	private static final Map<String, Ejb3Configuration> CONFIG_MAP 
		= new HashMap<String, Ejb3Configuration>();
	
	private static final Map<String, EntityManagerFactory> ENTITY_MANAGER_FACTORY_MAP 
		= new HashMap<String, EntityManagerFactory>();
	
	public synchronized static Ejb3Configuration initializeConfiguration(String persistenceUnitName
			, IConfigurator<Ejb3Configuration> configurator) throws HibernateException {
		Ejb3Configuration config = configurator.configure();
		CONFIG_MAP.put(persistenceUnitName, config);
		return config;
	}
	
	public static Ejb3Configuration initializeConfiguration(
			IConfigurator<Ejb3Configuration> configurator) throws HibernateException {
		return initializeConfiguration(null, configurator);
	}
	
	/**
	 * Build the configuration from an entity manager name and given the appropriate 
	 * extra properties. Those properties override the one get through 
	 * the peristence.xml file. If the persistence unit name is not found or does 
	 * not match the Persistence Provider, null is returned This method is used in 
	 * a non managed environment.
	 * 
	 * @param persistenceUnitName
	 * @param integration
	 * @return configured Ejb3Configuration or null if no persistence unit match
	 * @throws HibernateException
	 */
	public synchronized static Ejb3Configuration initializeConfiguration(String persistenceUnitName, Map integration) throws HibernateException{
		Ejb3Configuration config = new Ejb3Configuration();
		config.configure(persistenceUnitName, integration);
		CONFIG_MAP.put(persistenceUnitName, config);
		return config;
	}
	
	/**
	 * 
	 * @param persistenceUnitName
	 * @param properties
	 * @param persistentClasses
	 * @param annot
	 * @return
	 * @throws HibernateException
	 */
	public synchronized static Ejb3Configuration initializeConfiguration(String persistenceUnitName
			,Properties properties, Class[] persistentClasses, boolean annot) throws HibernateException{
		Ejb3Configuration config = new Ejb3Configuration();
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
	 * @param annot
	 * @return
	 * @throws HibernateException
	 */
	public static Ejb3Configuration initializeConfiguration(
			Properties properties, Class[] persistentClasses, boolean annot) throws HibernateException{
		return initializeConfiguration(null, properties, persistentClasses, annot);
	}
	
	/**
	 * 
	 * @param persistenceUnitName
	 * @return
	 * @throws HibernateException
	 */
	public synchronized static EntityManagerFactory getEntityManagerFactory(String persistenceUnitName) throws HibernateException{
		EntityManagerFactory entityManagerFactory = ENTITY_MANAGER_FACTORY_MAP.get(persistenceUnitName);
		if(entityManagerFactory==null){
			Ejb3Configuration config = CONFIG_MAP.get(persistenceUnitName);
			if(config==null){
				throw new IllegalStateException("Persistence unit name: "+persistenceUnitName+" has not been initialized!");
			}
			entityManagerFactory = config.buildEntityManagerFactory();
			ENTITY_MANAGER_FACTORY_MAP.put(persistenceUnitName,entityManagerFactory);
		}
		return entityManagerFactory;
	}
	
	/**
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static EntityManagerFactory getEntityManagerFactory() throws HibernateException{
		
		return getEntityManagerFactory(null);
	}
	
	/**
	 * Gets EntityManager for current thread. When finished, 
	 * users must return session using {@link #closeEntityManager() closeEntityManager()} method.
	 * This method compatible with getEntityManagerFactory().getCurrentEntityManager() 
	 * or getEntityManagerFactory(null).getCurrentEntityManager();
	 * where hibernate.current_session_context_class property is thread.
	 * 
	 * @return EntityManager for current thread.
	 * @throws HibernateException if there is an error opening a new session.
	 */
	public static EntityManager getEntityManager(String persistenceUnitName) throws HibernateException{
		ThreadLocal<EntityManager> threadLocal = getThreadLocalVariable(persistenceUnitName);
		EntityManager entityManager = threadLocal.get();
		if(entityManager==null || !entityManager.isOpen()){
			entityManager = getEntityManagerFactory(persistenceUnitName).createEntityManager();
			threadLocal.set(entityManager);
		}
		return entityManager;
	}
	
	/**
	 * 
	 * @return
	 * @throws HibernateException
	 */
	public static EntityManager getEntityManager() throws HibernateException{
		return getEntityManager(null);
	}
	
	/**
	 * Closes the EntityManager. Users must call this method after calling
	 * {@link #getEntityManager() getEntityManager()}.
	 * @throws HibernateException if session has problem closing.
	 */
	public static void closeEntityManager(String persistenceUnitName) throws HibernateException{
		ThreadLocal<EntityManager> threadLocal = getThreadLocalVariable(persistenceUnitName);
		EntityManager entityManager = threadLocal.get();
		if(entityManager!=null){
			entityManager.close();
			threadLocal.remove();
		}
	}
	
	/**
	 * 
	 * @throws HibernateException
	 */
	public static void closeEntityManager() throws HibernateException{
		closeEntityManager(null);
	}
	
	/**
	 * 
	 * @throws HibernateException
	 */
	public static void beginTransaction(String persistenceUnitName) throws HibernateException{
		getEntityManager(persistenceUnitName).getTransaction().begin();
	}
	
	/**
	 * 
	 * @throws HibernateException
	 */
	public static void beginTransaction() throws HibernateException{
		getEntityManager(null).getTransaction().begin();
	}
	
	/**
	 * 
	 * @throws HibernateException
	 */
	public static void commitTransaction(String persistenceUnitName) throws HibernateException{
		getEntityManager(persistenceUnitName).getTransaction().commit();
	}
	
	/**
	 * 
	 * @throws HibernateException
	 */
	public static void commitTransaction() throws HibernateException{
		getEntityManager(null).getTransaction().commit();
	}
	
	/**
	 * 
	 * @throws HibernateException
	 */
	public static void rollbackTransaction(String persistenceUnitName) throws HibernateException{
		getEntityManager(persistenceUnitName).getTransaction().rollback();
	}
	
	/**
	 * 
	 * @throws HibernateException
	 */
	public static void rollbackTransaction() throws HibernateException{
		getEntityManager(null).getTransaction().rollback();
	}
	
	/**
	 * 
	 * @param persistenceUnitName
	 * @return
	 */
	private static synchronized ThreadLocal<EntityManager> getThreadLocalVariable(String persistenceUnitName){
		ThreadLocal<EntityManager> threadLocal = ENTITY_MANAGER_MAP.get(persistenceUnitName);
		if(threadLocal==null){
			threadLocal = new ThreadLocal<EntityManager>();
			ENTITY_MANAGER_MAP.put(persistenceUnitName, threadLocal);
		}
		return threadLocal;
	}
	
}
