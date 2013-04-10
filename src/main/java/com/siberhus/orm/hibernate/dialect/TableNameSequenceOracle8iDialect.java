package com.siberhus.orm.hibernate.dialect;

import org.hibernate.dialect.Oracle8iDialect;

public class TableNameSequenceOracle8iDialect extends Oracle8iDialect {

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
