/*******************************************************************************
 * Copyright (c) 2012 John Bradley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Bradley - initial API and implementation
 *******************************************************************************/
package uk.ac.kcl.cch.jb.pliny.data.rdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.Vector;

import uk.ac.kcl.cch.rdb2java.Rdb2javaPlugin;
import uk.ac.kcl.cch.rdb2java.dynData.BaseQuery;
import uk.ac.kcl.cch.rdb2java.dynData.IPersistentQuery;

public class DBPersistentQuery implements IPersistentQuery {

	private BaseQuery myQuery;
	protected PreparedStatement queryStmt = null;
	protected PreparedStatement countStmt = null;
	private DBServices myService;
	
	public DBPersistentQuery(DBServices myService, BaseQuery bq, String foreignKeyName){
		this.myQuery = bq;
		this.myService = myService;
		myQuery.addConstraintParam(foreignKeyName, BaseQuery.FilterEQUAL);
	}
	
	public Vector executeQuery(int ID){
		Vector results = new Vector();
		try {
			if(queryStmt == null)queryStmt = myService.makeStatement(myQuery);
			queryStmt.setInt(1,ID);
			ResultSet rs = queryStmt.executeQuery();

			while(rs.next()){
				int currKey = rs.getInt(1);
				Object d = myQuery.getObject(currKey);
				results.add(d);
			}
		} catch( Exception e) {
			e.printStackTrace(System.out);
		}
		return results;
	}
	
	public int executeCount(int ID){
	     int count = 0;
	     try {
	       if(countStmt == null){
	    	   countStmt = myService.makeStatement(myQuery, true);
	       }
	       countStmt.setInt(1,ID);
	       ResultSet rs = countStmt.executeQuery();

	       while(rs.next()){
	    	  count = rs.getInt(1);
	       }
	     } catch( Exception e) {
	       e.printStackTrace(System.out);
	     } 
	     return count;
	}
	
}
