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
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import uk.ac.kcl.cch.rdb2java.dynData.IDataInserter;

public class DBDataInserter implements IDataInserter {
	
	private String tableName;
	private PreparedStatement stmt1 = null;
	private PreparedStatement stmt2 = null;
	private Vector<String> colNames;
	private DBServices service;

	public DBDataInserter(String tableName, DBServices service){
		this.tableName = tableName;
		colNames = new Vector<String>();
		this.service = service;
	}
	
	public int doInsert(Map data){
		int rslt = 0;
		try {
			if(stmt1 == null)makeupStatements(data);
			loadParameters(data);
			stmt1.executeUpdate();
			ResultSet rs = stmt2.executeQuery();
			rs.next();
			rslt = rs.getInt(1);
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rslt;
	}

	private void makeupStatements(Map data) throws SQLException{
		Iterator it = data.keySet().iterator();
		StringBuffer cols = new StringBuffer();
		StringBuffer qmarks = new StringBuffer();
		String connector = "";
		while(it.hasNext()){
			String colName = (String)it.next();
			colNames.add(colName);
			cols.append(connector+colName);
			qmarks.append(connector+"?");
			connector = ",";
		}
		String stmtText = "INSERT INTO "+tableName+ "("+cols.toString()+") VALUES ("+qmarks.toString()+")";
		stmt1 = DBServices.getConnection().prepareStatement(stmtText);
		stmt2 = DBServices.getConnection().prepareStatement(DBServices.getNewKeyQuery(tableName));
	}
	
	private void loadParameters(Map data) throws SQLException{
		int i = 0;
		for(String colName: colNames){
			Object item = data.get(colName);
			if(item == null)throw new SQLException("Column name not found in data: "+colName);
			i++;
			if(item instanceof Integer)stmt1.setInt(i, (Integer)item);
			else if(item instanceof String)stmt1.setString(i, (String)item);
			else if(item instanceof Date)stmt1.setDate(i, (Date)item);
			else if(item instanceof Time)stmt1.setTime(i, (Time)item);
			else if(item instanceof Timestamp)stmt1.setTimestamp(i, (Timestamp)item);
			else throw new SQLException("Unexpected datatype found in data: "+colName);
		}
	}
}
