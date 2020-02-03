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

import uk.ac.kcl.cch.rdb2java.Rdb2javaPlugin;
import uk.ac.kcl.cch.rdb2java.dynData.*;
import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.data.rdb.DBServices;


public class Plugin extends BaseObject
implements IAuthorityListItem, ILoadableFromResultSet {
   static protected PluginQuery myCache = new PluginQuery();

   protected int pluginKey;
   private String idString;

public static final String IDSTRING_PROP = "Plugin.idString";
public static final String OBJECTTYPES_PROP = "Plugin.objectTypes";

   protected void setEmpty(){
     pluginKey = 0;
     idString = "";

   }

   public static String getSelectEntities(){
       return
       "Plugin.pluginKey,Plugin.idString";
   }
   
   public static String getTableJoins(){
       return "";
   }
   
   public static String getSQLFrom(){
       return "Plugin";
   }

   protected String getKeyName(){
       return "pluginKey";
   }

   protected String getTableName(){
       return "Plugin";
   }

   public int loadFromResultSet(ResultSet rs)throws SQLException{
      try {
      pluginKey = rs.getInt(1);
      idString = rs.getString(2);
      } catch (Exception e){
		e.printStackTrace();
      }     
      return 2;
}

   // this constructor should only be used within the Query object.

   public Plugin(boolean realEmpty){
     super();
     setEmpty();
     if(!realEmpty){
        doInsert();
        if(myCache == null)myCache = new PluginQuery();
        myCache.addNewItem(this.pluginKey, this);
        Rdb2javaPlugin.getDataServer().notifyCreate("Plugin", this);
     }
   }

   public Plugin(){
     super();
     setEmpty();
     doInsert();
     if(myCache == null)myCache = new PluginQuery();
     myCache.addNewItem(this.pluginKey, this);
     Rdb2javaPlugin.getDataServer().notifyCreate("Plugin", this);
   }

   static public Plugin getItem(int key){
      if(myCache == null)myCache = new PluginQuery();
      return (Plugin)myCache.getObject(key);
   }

// code for standard getters

   public int getPluginKey(){return pluginKey;}
   public String getIdString(){return idString;}

// code for standard setters

   public void setIdString(String parm){
     idString = parm;
     updateDBString("idString",parm);
     firePropertyChange(IDSTRING_PROP);
   }

// code for saving back to the DB

   protected void loadParameters(Map map){
	   map.put("idString", idString);
   }

   public void deleteMe(){
      if(pluginKey <= 0) return;
      myCache.getMyCache().deleteItem(pluginKey);
      Rdb2javaPlugin.getDataServer().doDelete("Plugin", "pluginKey", pluginKey);
      Rdb2javaPlugin.getDataServer().notifyDelete("Plugin", this);
      pluginKey = 0;
   }

   public void reIntroduceMe(){
      if(pluginKey > 0)return;
      doInsert();
      myCache.getMyCache().addNewItem(pluginKey, this);
      Rdb2javaPlugin.getDataServer().notifyCreate("Plugin", this);
   }

// code for results via foreign keys and intersection sets
   
   protected void addEmptyFKRefLists(){
	   objectTypes = new FKReferenceList();
   }

   private FKReferenceList objectTypes = null;
   private static IPersistentQuery objectTypesQuery = null;

   public FKReferenceList getObjectTypes(){
	   if(pluginKey == 0)return FKReferenceList.EMPTY_FKREFERENCELIST;
	   if(objectTypesQuery == null)
		   objectTypesQuery = Rdb2javaPlugin.getDataServer().makePersistentQuery(new ObjectTypeQuery(), "pluginKey");
	   if(objectTypes == null)objectTypes = new FKReferenceList(objectTypesQuery, pluginKey);
	     //new ObjectTypeQuery(), "pluginKey="+pluginKey);
	   return objectTypes;
   }
   public void signalChangeObjectTypes(ObjectType oldItem, ObjectType newItem){
	   firePropertyChange(OBJECTTYPES_PROP,oldItem,newItem);
   }


// code for handling foreign key references to this entity/class

   public void assignMyReferencesTo(int to){
       doFkAssignment("ObjectType","pluginKey",to);
   }
   
   public void assignMyReferencesTo(Plugin assignee){
       assignMyReferencesTo(assignee.getPluginKey());
   }


// code for extra variables and methods

//EM{plugin-lookups
   private static Hashtable byIds = new Hashtable();
   
   public static Plugin findFromId(String id){
	   if(byIds.containsKey(id))return (Plugin)byIds.get(id);
	   PluginQuery q = new PluginQuery();
	   //q.setWhereString("idString=?");
	   //q.addQueryParam(id);
	   q.addConstraint("idString", BaseQuery.FilterEQUAL, id);
	   Vector rslt = q.executeQuery();
	   if(rslt.size() == 0){
		   Plugin newPlugin = new Plugin(true);
		   newPlugin.setIdString(id);
		   newPlugin.reIntroduceMe();
		   byIds.put(id, newPlugin);
		   return newPlugin;
	   }
	   byIds.put(id, rslt.get(0));
	   return (Plugin)rslt.get(0);
   }
//EM}

// code for toString

   private String displayKey(){
      return getIdString();
   }

   public String toString(){
      return "Plugin: "+displayKey()+"("+getPluginKey()+")";
   }
   
   public String getALItem() {
       if(getPluginKey() <= 1) return "";
       return displayKey();
   }
   
   public int getALID() {
       return getPluginKey();
   }
   
   protected void setALID(int val){pluginKey = val;}
}
