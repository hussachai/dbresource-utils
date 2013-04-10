package com.siberhus.dbresource.utils;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JpaUtil {
	
	private static final Map<String, ThreadLocal<EntityManager>> ENTITY_MANAGER_MAP 
		= new HashMap<String, ThreadLocal<EntityManager>>();
	
	private static final Map<String, EntityManagerFactory> ENTITY_MANAGER_FACTORY_MAP 
		= new HashMap<String, EntityManagerFactory>();
	
	private static String defaultPersistenceUnitName = null;
	
	public synchronized static void  initializeEntityManagerFactory(String persistenceUnitName,
			IConfigurator<EntityManagerFactory> configurator, boolean defaultPersistenceUnit){
		if(defaultPersistenceUnit){
			JpaUtil.defaultPersistenceUnitName = persistenceUnitName;
		}
		EntityManagerFactory entityManagerFactory = configurator.configure();
		ENTITY_MANAGER_FACTORY_MAP.put(persistenceUnitName, entityManagerFactory);
	}
	
	public synchronized static void initializeEntityManagerFactory(String persistenceUnitName
			, Map properties, boolean defaultPersistenceUnit) {
		if(defaultPersistenceUnit){
			JpaUtil.defaultPersistenceUnitName = persistenceUnitName;
		}
		EntityManagerFactory entityManagerFactory = null;
		if(properties!=null){
			entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName,properties);
		}else{
			entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
		}
		ENTITY_MANAGER_FACTORY_MAP.put(persistenceUnitName, entityManagerFactory);
	}
	
	public static void initializeEntityManagerFactory(String persistenceUnitName
			, boolean defaultPersistenceUnit) {
		initializeEntityManagerFactory(persistenceUnitName
				, (Map)null, defaultPersistenceUnit);
	}
	
	/**
	 * 
	 * @param persistenceUnitName
	 * @return
	 */
	public synchronized static EntityManagerFactory getEntityManagerFactory(String persistenceUnitName){
		EntityManagerFactory entityManagerFactory = ENTITY_MANAGER_FACTORY_MAP.get(persistenceUnitName);
		if(entityManagerFactory==null){
			throw new IllegalStateException("Persistence unit name: "+persistenceUnitName+" has not been initialized!");
		}
		return entityManagerFactory;
	}
	
	/**
	 * 
	 * @return
	 */
	public static EntityManagerFactory getEntityManagerFactory(){
		
		return getEntityManagerFactory(JpaUtil.defaultPersistenceUnitName);
	}
	
	/**
	 * Gets EntityManager for current thread. When finished, 
	 * users must return session using {@link #closeEntityManager() closeEntityManager()} method.
	 * This method compatible with getEntityManagerFactory().getCurrentEntityManager() 
	 * or getEntityManagerFactory(null).getCurrentEntityManager();
	 * 
	 * @return EntityManager for current thread.
	 */
	public static EntityManager getEntityManager(String persistenceUnitName) {
		ThreadLocal<EntityManager> threadLocal = getThreadLocalVariable(persistenceUnitName);
		EntityManager entityManager = threadLocal.get();
		if(entityManager==null){
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
	public static EntityManager getEntityManager() {
		return getEntityManager(JpaUtil.defaultPersistenceUnitName);
	}
	
	/**
	 * Closes the EntityManager. Users must call this method after calling
	 * {@link #getEntityManager() getEntityManager()}.
	 */
	public static void closeEntityManager(String persistenceUnitName) {
		ThreadLocal<EntityManager> threadLocal = getThreadLocalVariable(persistenceUnitName);
		EntityManager entityManager = threadLocal.get();
		if(entityManager!=null){
			entityManager.close();
			threadLocal.remove();
		}
	}
	
	/**
	 * 
	 * @
	 */
	public static void closeEntityManager() {
		closeEntityManager(JpaUtil.defaultPersistenceUnitName);
	}
	
	/**
	 * 
	 * @
	 */
	public static void beginTransaction(String persistenceUnitName) {
		getEntityManager(persistenceUnitName).getTransaction().begin();
	}
	
	/**
	 * 
	 * @
	 */
	public static void beginTransaction() {
		getEntityManager(JpaUtil.defaultPersistenceUnitName).getTransaction().begin();
	}
	
	/**
	 * 
	 * @
	 */
	public static void commitTransaction(String persistenceUnitName) {
		getEntityManager(persistenceUnitName).getTransaction().commit();
	}
	
	/**
	 * 
	 * @
	 */
	public static void commitTransaction() {
		getEntityManager(JpaUtil.defaultPersistenceUnitName).getTransaction().commit();
	}
	
	/**
	 * 
	 * @
	 */
	public static void rollbackTransaction(String persistenceUnitName) {
		getEntityManager(persistenceUnitName).getTransaction().rollback();
	}
	
	/**
	 * 
	 * @
	 */
	public static void rollbackTransaction() {
		getEntityManager(JpaUtil.defaultPersistenceUnitName).getTransaction().rollback();
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
