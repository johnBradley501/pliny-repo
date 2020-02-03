/*******************************************************************************
 * Copyright (c) 2007, 2012 John Bradley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Bradley - initial API and implementation
 *     John Bradley - modified to support IDataServer approach to data management
 *******************************************************************************/
package uk.ac.kcl.cch.jb.pliny.model;

import java.io.*;
import java.util.*;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.osgi.framework.Version;

import uk.ac.kcl.cch.rdb2java.Rdb2javaPlugin;
import uk.ac.kcl.cch.rdb2java.dynData.*;
import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.data.rdb.DBServices;


public class GlobalData extends BaseObject
implements IAuthorityListItem, ILoadableFromResultSet {
   static protected GlobalDataQuery myCache = new GlobalDataQuery();

   protected int globalDataKey;
   private int typeVal;
   private int numbVal;
   private String strVal;

public static final String TYPEVAL_PROP = "GlobalData.typeVal";
public static final String NUMBVAL_PROP = "GlobalData.numbVal";
public static final String STRVAL_PROP = "GlobalData.strVal";

   protected void setEmpty(){
     globalDataKey = 0;
     typeVal = 0;
     numbVal = 0;
     strVal = "";

   }

   public static String getSelectEntities(){
       return
       "GlobalData.globalDataKey,GlobalData.typeVal,GlobalData.numbVal,GlobalData.strVal";
   }
   
   public static String getTableJoins(){
       return "";
   }
   
   public static String getSQLFrom(){
       return "GlobalData";
   }

   protected String getKeyName(){
       return "globalDataKey";
   }

   protected String getTableName(){
       return "GlobalData";
   }

   public int loadFromResultSet(ResultSet rs)throws SQLException{
      try {
      globalDataKey = rs.getInt(1);
      typeVal = rs.getInt(2);
      numbVal = rs.getInt(3);
      strVal = rs.getString(4);
      } catch (Exception e){
		e.printStackTrace();
      }     
      return 4;
}

   // this constructor should only be used within the Query object.

   public GlobalData(boolean realEmpty){
     super();
     setEmpty();
     if(!realEmpty){
        doInsert();
        if(myCache == null)myCache = new GlobalDataQuery();
        myCache.addNewItem(this.globalDataKey, this);
        Rdb2javaPlugin.getDataServer().notifyCreate("GlobalData", this);
     }
   }

   public GlobalData(){
     super();
     setEmpty();
     doInsert();
     if(myCache == null)myCache = new GlobalDataQuery();
     myCache.addNewItem(this.globalDataKey, this);
     Rdb2javaPlugin.getDataServer().notifyCreate("GlobalData", this);
   }

   static public GlobalData getItem(int key){
      if(myCache == null)myCache = new GlobalDataQuery();
      return (GlobalData)myCache.getObject(key);
   }

// code for standard getters

   public int getGlobalDataKey(){return globalDataKey;}
   public int getTypeVal(){return typeVal;}
   public int getNumbVal(){return numbVal;}
   public String getStrVal(){return strVal;}

// code for standard setters

   public void setTypeVal(int parm){
     typeVal = parm;
     updateDBint("typeVal",parm);
     firePropertyChange(TYPEVAL_PROP);
   }
   public void setNumbVal(int parm){
     numbVal = parm;
     updateDBint("numbVal",parm);
     firePropertyChange(NUMBVAL_PROP);
   }
   public void setStrVal(String parm){
     strVal = parm;
     updateDBString("strVal",parm);
     firePropertyChange(STRVAL_PROP);
   }
   
   protected void addEmptyFKRefLists(){
	   // nothing needed here.
   }

// code for saving back to the DB
   
   protected void loadParameters(Map map){
	   map.put("typeVal", typeVal);
	   map.put("numbVal", numbVal);
	   map.put("strVal", strVal);
   }

   public void deleteMe(){
      if(globalDataKey <= 0) return;
      myCache.getMyCache().deleteItem(globalDataKey);
      Rdb2javaPlugin.getDataServer().doDelete("GlobalData", "globalDataKey", globalDataKey);
      Rdb2javaPlugin.getDataServer().notifyDelete("GlobalData", this);
      globalDataKey = 0;
   }

   public void reIntroduceMe(){
      if(globalDataKey > 0)return;
      doInsert();
      myCache.getMyCache().addNewItem(globalDataKey, this);
      Rdb2javaPlugin.getDataServer().notifyCreate("GlobalData", this);
   }

// code for results via foreign keys and intersection sets




// code for extra variables and methods

//EM{globalData-dbVersion
   private static final int dbVersionKey = 1;
   private static GlobalData dbVersion = null;
   
   private static GlobalData getDbVersionRecord(){
	   if(dbVersion != null)return dbVersion;
	   GlobalDataQuery q = new GlobalDataQuery();
	   //q.setWhereString("typeVal="+dbVersionKey);
	   q.addConstraint("typeVal", BaseQuery.FilterEQUAL, dbVersionKey);
	   Vector rslt = q.executeQuery();
	   
	   /*if(rslt == null){
		   throw new RuntimeException("No Global Data available: this should never happen.");
		   
		   // we assume here that the DB engine is Derby 
		   String defGlobals = "create table GlobalData ("+
				   "globalDataKey SMALLINT not null generated by default as identity,"+
				   "typeVal SMALLINT not null default 0,"+
				   "numbVal INT not null default 0,"+
				   "strVal VARCHAR(255) not null default '',"+
				   " primary key (globalDataKey)"+
				")";
		   Connection con = DBServices.getConnection();
	       Statement stmt1;
	       try {
	    	 stmt1 = con.createStatement();
	         stmt1.executeUpdate(defGlobals);
	         stmt1.close();
	       } catch( Exception e) {
	    	   throw new RuntimeException("Could not update DB: "+defGlobals);
	       }

		   DBServices.returnConnection(con); 
	   }*/
	   if(rslt == null || rslt.size() == 0){
		   dbVersion = new GlobalData(true);
		   dbVersion.setTypeVal(dbVersionKey);
		   dbVersion.setStrVal("0.0.0");
		   dbVersion.reIntroduceMe();
		   return dbVersion;
	   }
	   else if(rslt.size() == 1)dbVersion = (GlobalData)rslt.get(0);
	   return dbVersion;
   }
   
   public static String getDbVersionStr(){
	   GlobalData data = getDbVersionRecord();
	   return data.getStrVal();
   }
   
   public static Version getDbVersion(){
	   return new Version(getDbVersionStr());
   }
   
   public static void setDbVersion(String version){
	   GlobalData data = getDbVersionRecord();
	   data.setStrVal(version.trim());
   }
   
   public static void setDbVersion(Version version){
	   setDbVersion(version.toString());
   }
   
   public static void setupVersion(String version){
	    GlobalData gd = new GlobalData(true);
	    gd.setTypeVal(dbVersionKey);
	    gd.setStrVal(version);
	    gd.reIntroduceMe();
/*
	    try {
	    	Statement stmt = conn.createStatement();
	        String sql = "INSERT INTO GlobalData (typeVal, strVal) VALUES ("+dbVersionKey+",\'"+version+"\')";
    		stmt.executeUpdate(sql);
    		stmt.close();


	    } catch (Exception e){
	    	e.printStackTrace();
	    } */
   }
//EM}

// code for toString

   private String displayKey(){
      return getNumbVal()+"/ "+getStrVal();
   }

   public String toString(){
      return "GlobalData: "+displayKey()+"("+getGlobalDataKey()+")";
   }
   
   public String getALItem() {
       if(getGlobalDataKey() <= 1) return "";
       return displayKey();
   }
   
   public int getALID() {
       return getGlobalDataKey();
   }
   
   protected void setALID(int id){globalDataKey = id;}
}
