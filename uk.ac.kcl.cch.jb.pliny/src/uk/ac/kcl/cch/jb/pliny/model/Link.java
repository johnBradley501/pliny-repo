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

import org.eclipse.draw2d.geometry.Rectangle;

import uk.ac.kcl.cch.rdb2java.Rdb2javaPlugin;
import uk.ac.kcl.cch.rdb2java.dynData.*;
import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.data.rdb.DBServices;


public class Link extends BaseObject
implements IAuthorityListItem, ILoadableFromResultSet,IHasLoType, IHasAttribute {
   static protected LinkQuery myCache = new LinkQuery();

   protected int linkKey;
   private String attributes;
   private int fromLinkKey;
   private LinkableObject fromLink;
   private int toLinkKey;
   private LinkableObject toLink;
   private int typeKey;
   private LOType loType;

public static final String ATTRIBUTES_PROP = "Link.attributes";
public static final String FROMLINKKEY_PROP = "Link.fromLinkKey";
public static final String TOLINKKEY_PROP = "Link.toLinkKey";
public static final String TYPEKEY_PROP = "Link.typeKey";

   protected void setEmpty(){
     linkKey = 0;
     attributes = "";
     fromLinkKey = 0;
     fromLink = null;
     toLinkKey = 0;
     toLink = null;
     typeKey = 0;
     loType = null;

   }

   public static String getSelectEntities(){
       return
       "Link.linkKey,Link.attributes,Link.fromLink,Link.toLink,Link.typeKey";
   }
   
   public static String getTableJoins(){
       return "";
   }
   
   public static String getSQLFrom(){
       return "Link";
   }

   protected String getKeyName(){
       return "linkKey";
   }

   protected String getTableName(){
       return "Link";
   }

   public int loadFromResultSet(ResultSet rs)throws SQLException{
      try {
      linkKey = rs.getInt(1);
      attributes = rs.getString(2);
      fromLinkKey = rs.getInt(3);
      fromLink = null;
      toLinkKey = rs.getInt(4);
      toLink = null;
      typeKey = rs.getInt(5);
      loType = null;
      } catch (Exception e){
		e.printStackTrace();
      }     
      return 5;
}

   // this constructor should only be used within the Query object.

   public Link(boolean realEmpty){
     super();
     setEmpty();
     if(!realEmpty){
        doInsert();
        if(myCache == null)myCache = new LinkQuery();
        myCache.addNewItem(this.linkKey, this);
        Rdb2javaPlugin.getDataServer().notifyCreate("Link", this);
     }
   }

   public Link(){
     super();
     setEmpty();
     doInsert();
     if(myCache == null)myCache = new LinkQuery();
     myCache.addNewItem(this.linkKey, this);
     Rdb2javaPlugin.getDataServer().notifyCreate("Link", this);
   }

   static public Link getItem(int key){
      if(myCache == null)myCache = new LinkQuery();
      return (Link)myCache.getObject(key);
   }

// code for standard getters

   public int getLinkKey(){return linkKey;}
   public String getAttributes(){return attributes;}
   public LinkableObject getFromLink(){
       if(fromLinkKey <= 0)return null;
       if(fromLink == null)fromLink = LinkableObject.getItem(fromLinkKey);
       return fromLink;
   }
   public LinkableObject getToLink(){
       if(toLinkKey <= 0)return null;
       if(toLink == null)toLink = LinkableObject.getItem(toLinkKey);
       return toLink;
   }
   public LOType getLoType(){
       if(typeKey <= 0)return null;
       if(loType == null)loType = LOType.getItem(typeKey);
       return loType;
   }

// code for standard setters

   public void setAttributes(String parm){
     attributes = parm;
     updateDBString("attributes",parm);
     firePropertyChange(ATTRIBUTES_PROP);
   }
   public void setFromLink(LinkableObject item){
     if((fromLinkKey == 0) && (item == null))return;
     if((item != null) && (item.getALID()==fromLinkKey))return;
     LinkableObject oldItem = null;
     if(fromLinkKey != 0){
        oldItem = getFromLink();
        getFromLink().getLinkedFrom().remove(this);
        getFromLink().signalChangeLinkedFrom(this,null);
     }
     fromLinkKey = 0;
     if(item != null){
        fromLinkKey = item.getALID(); 
        item.getLinkedFrom().add(this);
        item.signalChangeLinkedFrom(null,this);
     }
     fromLink=item;
     updateDBint("fromLink",fromLinkKey);
     firePropertyChange(FROMLINKKEY_PROP, oldItem, this);
   }
   public void setToLink(LinkableObject item){
     if((toLinkKey == 0) && (item == null))return;
     if((item != null) && (item.getALID()==toLinkKey))return;
     LinkableObject oldItem = null;
     if(toLinkKey != 0){
        oldItem = getToLink();
        getToLink().getLinkedTo().remove(this);
        getToLink().signalChangeLinkedTo(this,null);
     }
     toLinkKey = 0;
     if(item != null){
        toLinkKey = item.getALID(); 
        item.getLinkedTo().add(this);
        item.signalChangeLinkedTo(null,this);
     }
     toLink=item;
     updateDBint("toLink",toLinkKey);
     firePropertyChange(TOLINKKEY_PROP, oldItem, this);
   }
   public void setLoType(LOType item){
     if((typeKey == 0) && (item == null))return;
     if((item != null) && (item.getALID()==typeKey))return;
     LOType oldItem = null;
     if(typeKey != 0){
        oldItem = getLoType();
        getLoType().getLinks().remove(this);
        getLoType().signalChangeLinks(this,null);
     }
     typeKey = 0;
     if(item != null){
        typeKey = item.getALID(); 
        item.getLinks().add(this);
        item.signalChangeLinks(null,this);
     }
     loType=item;
     updateDBint("typeKey",typeKey);
     firePropertyChange(TYPEKEY_PROP, oldItem, this);
   }

// code for saving back to the DB

   protected void loadParameters(Map map){
	   map.put("attributes", attributes);
	   //map.put("fromLinkKey", fromLinkKey);
	   //map.put("toLinkKey", toLinkKey);
	   map.put("fromLink", fromLinkKey);
	   map.put("toLink", toLinkKey);
	   map.put("typeKey", typeKey);
   }

   public void deleteMe(){
      if(linkKey <= 0) return;
      myCache.getMyCache().deleteItem(linkKey);
      Rdb2javaPlugin.getDataServer().doDelete("Link", "linkKey", linkKey);
      Rdb2javaPlugin.getDataServer().notifyDelete("Link", this);
      linkKey = 0;
   }

   public void reIntroduceMe(){
      if(linkKey > 0)return;
      doInsert();
      myCache.getMyCache().addNewItem(linkKey, this);
      Rdb2javaPlugin.getDataServer().notifyCreate("Link", this);
   }

// code for results via foreign keys and intersection sets

   
   protected void addEmptyFKRefLists(){
	   // nothing needed here.
   }



// code for extra variables and methods

//EM{link-storeLinks
   /*
    * this code stores links so that they can be subsequently reinstated.
    * it assumes that the linked objects are the same objects to be reinstated,
    * but perhaps having been given new keys in the DB
    */
   
   LinkableObject fromBackup, toBackup;
   private LOType loTypeBackup;
   
   public void backupAndClearLinks(){
	   fromBackup = getFromLink();
	   toBackup =getToLink();
	   loTypeBackup = getLoType();
	   setFromLink(null);
	   setToLink(null);
	   setLoType(null);
   }
   public void restoreLinks(){
	   setLoType(loTypeBackup);
	   setFromLink(fromBackup);
	   setToLink(toBackup);
   }
//EM}
   
//EM{Link-getKeys
   public int getFromLinkKey(){return fromLinkKey;}
   public int getToLinkKey(){return toLinkKey;}
   public int getLoTypeKey(){return typeKey;}
//EM}
   
//EM{Link-attributes
   private AttributedObjectHandler objectHandler = null;

   private AttributedObjectHandler getHandler(){
	   if(objectHandler == null){
		   objectHandler = new AttributedObjectHandler(this,"link");
	   }
	   return objectHandler;
   }
   
   public String getString(String propName){
	   return getHandler().getString(propName);
   }
   
   public int getInt(String propName){
	   return getHandler().getInt(propName);
   }
   
   public float getFloat(String propName){
	   return getHandler().getFloat(propName);
   }
   
   public Rectangle getRectangle(String propName){
	   return getHandler().getRectangle(propName);
   }
   
   public void updateString(String propName, String value){
	   getHandler().updateString(propName, value);
   }
   
   public void updateInt(String propName, int value){
	   getHandler().updateInt(propName, value);
   }
   
   public void updateFloat(String propName, float value){
	   getHandler().updateFloat(propName, value);
   }
   
   public void updateRectangle(String propName, Rectangle r){
	   getHandler().updateRectangle(propName, r);
   }
   
   public void setFromAttr(String attr){
	   getHandler().updateString("From", attr);
   }
   
   public void setToAttr(String attr){
	   getHandler().updateString("To", attr);
   }

   public String getFromAttr(){
	   return getHandler().getString("From");
   }
   
   public String getToAttr(){
	   return getHandler().getString("To");
   }
 //EM}

// code for toString

   private String displayKey(){
      return getAttributes();
   }

   public String toString(){
      return "Link: "+displayKey()+"("+getLinkKey()+")";
   }
   
   public String getALItem() {
       if(getLinkKey() <= 1) return "";
       return displayKey();
   }
   
   public int getALID() {
       return getLinkKey();
   }
   
   protected void setALID(int val){linkKey = val;}
}
