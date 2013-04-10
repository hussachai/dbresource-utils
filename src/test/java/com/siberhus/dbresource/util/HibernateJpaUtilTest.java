package com.siberhus.dbresource.util;

import java.util.Properties;

import javax.persistence.EntityManager;

import org.junit.Test;

import com.siberhus.commons.properties.PropertiesUtil;
import com.siberhus.dbresource.utils.HibernateJpaUtil;

public class HibernateJpaUtilTest {
	
	@Test
	public void testInitializeConfiguration()throws Exception{
		
		
	}
	
	public static void main(String[] args)throws Exception {
		Properties props = PropertiesUtil.create("classpath:hibernate.properties");
		HibernateJpaUtil.initializeConfiguration(null,props,new Class[]{
			Person.class
		},true);
		for(int i=0;i<1000;i++){
			Thread t = new Thread(){
				public void run(){
					EntityManager em = HibernateJpaUtil.getEntityManager();
					em.find(Person.class, 1L);
					em.close();
				}
			};
			t.start();
			t.join();
		}
//		Thread.sleep(1000*10);
	}
}
