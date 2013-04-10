package com.siberhus.orm.hibernate.dialect;

import org.hibernate.dialect.Oracle10gDialect;

public class TableNameSequenceOracle10gDialect extends Oracle10gDialect {

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
