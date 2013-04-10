package com.siberhus.dbresource.util;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class Model<T> {
	
	public T findById(Object id){
		return null;
	}
	
}
