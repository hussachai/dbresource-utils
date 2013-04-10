package com.siberhus.orm.hibernate.dialect;

import org.hibernate.dialect.PostgreSQLDialect;


public class TableNameSequencePostgreSQLDialect extends PostgreSQLDialect {
	
	/**
	 * Get the native identifier generator class.
	 * 
	 * @return TableNameSequenceGenerator.
	 */
	@Override
	public Class<?> getNativeIdentifierGeneratorClass() {
		return TableNameSequenceGenerator.class;
	}
	
}

