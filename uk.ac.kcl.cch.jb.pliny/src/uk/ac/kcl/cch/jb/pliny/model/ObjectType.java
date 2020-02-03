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

/**
 *
 * @author bradley (autogenerated)
 * @version 
 */
package uk.ac.kcl.cch.jb.pliny.model;

import java.io.*;
import java.util.*;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import uk.ac.kcl.cch.rdb2java.Rdb2javaPlugin;
import uk.ac.kcl.cch.rdb2java.dynData.*;
import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewDescriptor;

import uk.ac.kcl.cch.jb.pliny.data.rdb.DBServices;
import uk.ac.kcl.cch.jb.pliny.dnd.IResourceExtensionProcessor;
import uk.ac.kcl.cch.jb.pliny.dnd.IResourceExtensionProcessorSource;

public class ObjectType extends BaseObject
implements IAuthorityListItem, ILoadableFromResultSet,INamedObject, IResourceExtensionProcessorSource {
   static protected ObjectTypeQuery myCache = new ObjectTypeQuery();

   protected int objectTypeKey;
   private String name;
   private int pluginKey;
   private Plugin plugin;
   private String idString;
   private String editorId;
   private String iconId;

public static final String NAME_PROP = "ObjectType.name";
public static final String PLUGINKEY_PROP = "ObjectType.pluginKey";
public static final String IDSTRING_PROP = "ObjectType.idString";
public static final String EDITORID_PROP = "ObjectType.editorId";
public static final String ICONID_PROP = "ObjectType.iconId";
public static final String RESOURCES_PROP = "ObjectType.resources";

   protected void setEmpty(){
     objectTypeKey = 0;
     name = "";
     pluginKey = 0;
     plugin = null;
     idString = "";
     editorId = "";
     iconId = "";

   }

   public static String getSelectEntities(){
       return
       "ObjectType.objectTypeKey,ObjectType.name,ObjectType.pluginKey,ObjectType.idString,ObjectType.editorId,ObjectType.iconId";
   }
   
   public static String getTableJoins(){
       return "";
   }
   
   public static String getSQLFrom(){
       return "ObjectType";
   }

   protected String getKeyName(){
       return "objectTypeKey";
   }

   protected String getTableName(){
       return "ObjectType";
   }

   public int loadFromResultSet(ResultSet rs)throws SQLException{
      try {
      objectTypeKey = rs.getInt(1);
      name = rs.getString(2);
      pluginKey = rs.getInt(3);
      plugin = null;
      idString = rs.getString(4);
      editorId = rs.getString(5);
      iconId = rs.getString(6);
      } catch (Exception e){
		e.printStackTrace();
      }     
      return 6;
}

   // this constructor should only be used within the Query object.

   public ObjectType(boolean realEmpty){
     super();
     setEmpty();
     if(!realEmpty){
        doInsert();
        if(myCache == null)myCache = new ObjectTypeQuery();
        myCache.addNewItem(this.objectTypeKey, this);
        Rdb2javaPlugin.getDataServer().notifyCreate("ObjectType", this);
     }
   }

   public ObjectType(){
     super();
     setEmpty();
     doInsert();
     if(myCache == null)myCache = new ObjectTypeQuery();
     myCache.addNewItem(this.objectTypeKey, this);
     Rdb2javaPlugin.getDataServer().notifyCreate("ObjectType", this);
   }

   static public ObjectType getItem(int key){
      if(myCache == null)myCache = new ObjectTypeQuery();
      return (ObjectType)myCache.getObject(key);
   }

// code for standard getters

   public int getObjectTypeKey(){return objectTypeKey;}
   public String getName(){return name;}
   public Plugin getPlugin(){
       if(pluginKey <= 0)return null;
       if(plugin == null)plugin = Plugin.getItem(pluginKey);
       return plugin;
   }
   public String getIdString(){return idString;}
   public String getEditorId(){return editorId;}
   public String getIconId(){return iconId;}

// code for standard setters

   public void setName(String parm){
     name = parm;
     updateDBString("name",parm);
     firePropertyChange(NAME_PROP);
   }
   public void setPlugin(Plugin item){
     if((pluginKey == 0) && (item == null))return;
     if((item != null) && (item.getALID()==pluginKey))return;
     Plugin oldItem = null;
     if(pluginKey != 0){
        oldItem = getPlugin();
        getPlugin().getObjectTypes().remove(this);
        getPlugin().signalChangeObjectTypes(this,null);
     }
     pluginKey = 0;
     if(item != null){
        pluginKey = item.getALID(); 
        item.getObjectTypes().add(this);
        item.signalChangeObjectTypes(null,this);
     }
     plugin=item;
     updateDBint("pluginKey",pluginKey);
     firePropertyChange(PLUGINKEY_PROP, oldItem, this);
   }
   public void setIdString(String parm){
     idString = parm;
     updateDBString("idString",parm);
     firePropertyChange(IDSTRING_PROP);
   }
   public void setEditorId(String parm){
     editorId = parm;
     updateDBString("editorId",parm);
     firePropertyChange(EDITORID_PROP);
   }
   public void setIconId(String parm){
     iconId = parm;
     updateDBString("iconId",parm);
     firePropertyChange(ICONID_PROP);
   }

// code for saving back to the DB

   protected void loadParameters(Map map){
       map.put("name", name);
       map.put("pluginKey", pluginKey);
       map.put("idString", idString);
       map.put("editorId", editorId);
       map.put("iconId", iconId);
   }

   public void deleteMe(){
      if(objectTypeKey <= 0) return;
      myCache.getMyCache().deleteItem(objectTypeKey);
      Rdb2javaPlugin.getDataServer().doDelete("ObjectType", "objectTypeKey", objectTypeKey);
      Rdb2javaPlugin.getDataServer().notifyDelete("ObjectType", this);
      objectTypeKey = 0;
   }

   public void reIntroduceMe(){
      if(objectTypeKey > 0)return;
      doInsert();
      myCache.getMyCache().addNewItem(objectTypeKey, this);
      Rdb2javaPlugin.getDataServer().notifyCreate("ObjectType", this);
   }

// code for results via foreign keys and intersection sets
   
   protected void addEmptyFKRefLists(){
	   resources = new FKReferenceList();
   }

   private FKReferenceList resources = null;
   private static IPersistentQuery resourcesQuery = null;

   public FKReferenceList getResources(){
	   if(objectTypeKey == 0)return FKReferenceList.EMPTY_FKREFERENCELIST;
	   if(resourcesQuery == null)
		   resourcesQuery = Rdb2javaPlugin.getDataServer().makePersistentQuery(new ResourceQuery(), "objectTypeKey");
	   if(resources == null)resources = new FKReferenceList(resourcesQuery, objectTypeKey);
	     //new ResourceQuery(), "objectTypeKey="+objectTypeKey);
	   return resources;
   }
   public void signalChangeResources(Resource oldItem, Resource newItem){
	   firePropertyChange(RESOURCES_PROP,oldItem,newItem);
   }


// code for handling foreign key references to this entity/class

   public void assignMyReferencesTo(int to){
       doFkAssignment("Resource","objectTypeKey",to);
   }
   
   public void assignMyReferencesTo(ObjectType assignee){
       assignMyReferencesTo(assignee.getObjectTypeKey());
   }


// code for extra variables and methods

//EM{ObjectType-DnD
   private IResourceExtensionProcessor dropTargetProcessor;
   
   public IResourceExtensionProcessor getDropTargetProcessor(){
	   return dropTargetProcessor;
   }
   
   public void setDropTargetProcessor(IResourceExtensionProcessor module){
	   dropTargetProcessor = module;
   }
//EM}

//EM{objectType-iconlookup
   public Image getIconImage(){
	   ImageRegistry imageRegistry = PlinyPlugin.getDefault().getImageRegistry();
	   ImageDescriptor myImageDescriptor;
	   String myId = getIconId();
	   if((myId == null) || (myId.trim().equals(""))){
		   if(getEditorId().trim().equals(""))return null;
		   myId = "editor:"+getEditorId().trim();
	   }
	   Image theImage = imageRegistry.get(myId);
	   if(theImage != null)return theImage;
	   
	   int pos = myId.indexOf(':');
	   String type = myId.substring(0, pos);
	   String id = myId.substring(pos+1);
	   if(type.equals("editor")){
	      IEditorDescriptor ed = PlatformUI.getWorkbench().getEditorRegistry().findEditor(id);
	      if(ed == null)return null;
	      myImageDescriptor = ed.getImageDescriptor();
	   } else {
		  IViewDescriptor vd = PlatformUI.getWorkbench().getViewRegistry().find(id);
		  if(vd == null)return null;
		  myImageDescriptor = vd.getImageDescriptor();
	   }
	   if(myImageDescriptor == null)return null;
	   theImage = myImageDescriptor.createImage();
	   imageRegistry.put(myId, theImage);
	   return theImage;
   }
//EM}

//EM{objectType-lookups
   private static Hashtable byIds = new Hashtable();
   
   public static ObjectType findFromIds(String pluginID, String editorId){
	   String theId = pluginID.trim()+"\t"+editorId.trim();
	   if(byIds.containsKey(theId))return (ObjectType)byIds.get(theId);
	   Plugin plugIn = Plugin.findFromId(pluginID);
	   if(plugIn == null)return null;
	   ObjectTypeQuery q = new ObjectTypeQuery();
	   //q.setWhereString("pluginKey="+plugIn.getPluginKey()+" and editorId=?");
	   //q.addQueryParam(editorId);
	   q.addConstraint("pluginKey", BaseQuery.FilterEQUAL, plugIn.getPluginKey());
	   q.addConstraint("editorId", BaseQuery.FilterEQUAL, editorId);
	   Vector rslt = q.executeQuery();
	   if(rslt.size() == 0)return null;
	   byIds.put(theId, rslt.get(0));
	   return (ObjectType)rslt.get(0);
   }
   
   private static Hashtable byEditorIds = new Hashtable();
   
   public static ObjectType findFromEditorId(String editorId){
	   if(byEditorIds.containsKey(editorId))return (ObjectType)byEditorIds.get(editorId);
	   ObjectTypeQuery q = new ObjectTypeQuery();
	   //q.setWhereString("editorId=?");
	   //q.addQueryParam(editorId);
	   q.addConstraint("editorId", BaseQuery.FilterEQUAL, editorId);
	   Vector rslt = q.executeQuery();
	   if(rslt.size() == 0)return null;
	   byEditorIds.put(editorId, rslt.get(0));
	   return (ObjectType)rslt.get(0);
   }
//EM}

// code for toString

   private String displayKey(){
      return getName()+"/ "+getIdString()+"/ "+getEditorId()+"/ "+getIconId();
   }

   public String toString(){
      return "ObjectType: "+displayKey()+"("+getObjectTypeKey()+")";
   }
   
   public String getALItem() {
       if(getObjectTypeKey() <= 1) return "";
       return displayKey();
   }
   
   public int getALID() {
       return getObjectTypeKey();
   }
   
   protected void setALID(int val){objectTypeKey = val;}
}
