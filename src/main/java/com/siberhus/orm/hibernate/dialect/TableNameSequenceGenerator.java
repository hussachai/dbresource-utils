package com.siberhus.orm.hibernate.dialect;

import java.util.Properties;

import org.hibernate.dialect.Dialect;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.id.SequenceGenerator;
import org.hibernate.type.Type;

/**
 * http://community.jboss.org/wiki/CustomSequences
 * 
 * @author hussachai
 *
 */
public class TableNameSequenceGenerator extends SequenceGenerator {
	
	
	@Override
	public void configure(final Type type, final Properties params,
			final Dialect dialect) {
		if (params.getProperty(SEQUENCE) == null
				|| params.getProperty(SEQUENCE).length() == 0) {
			String tableName = params
					.getProperty(PersistentIdentifierGenerator.TABLE);
			
			if (tableName != null){
				//Check naming convention of table
				if(Character.isLowerCase(tableName.charAt(0))){
					params.setProperty(SEQUENCE, "seq_" + tableName.toLowerCase());
				}else{
					params.setProperty(SEQUENCE, "SEQ_" + tableName.toUpperCase());
				}
			}
		}
		super.configure(type, params, dialect);
	}
}

