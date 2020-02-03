/*******************************************************************************
 * Copyright (c) 2007 John Bradley
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
import java.sql.Statement;
import java.util.Iterator;

import org.osgi.framework.Version;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.model.GlobalData;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.ResourceQuery;
import uk.ac.kcl.cch.rdb2java.dynData.BaseQuery;

/**
 * This class provides a mechanism to update the structure of the backing
 * Pliny database whenever a new version of the Pliny software is installed.
 * 
 * @author John Bradley
 *
 */
public class DbVersionManager {

	private PlinyPlugin plugin = null;
	private Version update1ID = new Version("0.8.1");
	private Version update2ID = new Version("0.9.0");
	
	/**
	 * creates an instance of the manager for the current plugin.
	 * The DBServices will run this manager as a part of its startup processing.
	 * @param plugin
	 */
	public DbVersionManager(PlinyPlugin plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * checks the backing database for its version, compares it to the Plugin's version
	 * and updates the DB as necessary.
	 *
	 */
	
	public void run(){
		String pluginVersionString = (String)plugin.getBundle().getHeaders().get("Bundle-Version");
		Version pluginVersion = new Version(pluginVersionString);
		
		Version dbVersion = GlobalData.getDbVersion();
		if(pluginVersion.equals(dbVersion))return;
		
		//if(update1ID.isGreaterThan(dbVersion))dbVersion = doUpdate1();
		if(update1ID.compareTo(dbVersion)> 0)dbVersion = doUpdate1();
		/* insert update processes here */
		if(update2ID.compareTo(dbVersion)> 0)dbVersion = doUpdate2();
		
		GlobalData.setDbVersion(pluginVersion);
	}
	
	private void doUpdate(String sql){
		   Connection con = DBServices.getConnection();
	       Statement stmt1;
	       try {
	    	 stmt1 = con.createStatement();
	         stmt1.executeUpdate(sql);
	         stmt1.close();
	       } catch( Exception e) {
	    	   throw new RuntimeException("Could not update DB: "+sql);
	       } finally {
		      DBServices.returnConnection(con);
	       }
	}

	private Version doUpdate1() {
	   doUpdate("alter table LOType add column sourceRoleKey INT not null default 0");
	   doUpdate("alter table LOType add column targetRoleKey INT not null default 0");

	   GlobalData.setDbVersion(update1ID);
	   return update1ID;
	}
	
	private void fixWebUrls(){
		ResourceQuery q = new ResourceQuery();
		//q.setWhereString("objectTypeKey=2");
		q.addConstraint("objectTypeKey", BaseQuery.FilterEQUAL, 2);
		Iterator it = q.executeQuery().iterator();
		while(it.hasNext()){
			Resource r = (Resource)it.next();
			String fullId = r.getIdentifier();
			fullId = fullId.replaceAll("#.*", "");
			if(!fullId.equals(r.getIdentifier())){
				r.setIdentifiers(fullId);
			}
		}
	}
	
	private Version doUpdate2() {
		doUpdate("alter table Resource add column creationDate Date not null default '2007-01-01'");
		doUpdate("alter table Resource add column creationTime Time not null default '00:01'");
		
		fixWebUrls();
		
		GlobalData.setDbVersion(update2ID);
		return update2ID;
	}

}
