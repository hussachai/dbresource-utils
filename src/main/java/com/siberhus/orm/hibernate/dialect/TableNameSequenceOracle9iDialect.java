package com.siberhus.orm.hibernate.dialect;

import org.hibernate.dialect.Oracle9iDialect;

public class TableNameSequenceOracle9iDialect extends Oracle9iDialect {

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
