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

import org.eclipse.draw2d.geometry.Rectangle;

public class LinkableObject extends BaseObject
implements IAuthorityListItem, ILoadableFromResultSet, IHasLoType {
   static protected LinkableObjectQuery myCache = new LinkableObjectQuery();

   protected int linkableObjectKey;
   private int typeKey;
   private LOType loType;
   private String position;
   private int displPageNo;
   private int surrPageNo;
   protected int displayedInKey;
   private Resource displayedIn;
   private int surrogateForKey;
   private Resource surrogateFor;
   private boolean isOpen;
   private boolean showingMap;

public static final String TYPEKEY_PROP = "LinkableObject.typeKey";
public static final String POSITION_PROP = "LinkableObject.position";
public static final String DISPLPAGENO_PROP = "LinkableObject.displPageNo";
public static final String SURRPAGENO_PROP = "LinkableObject.surrPageNo";
public static final String DISPLAYEDINKEY_PROP = "LinkableObject.displayedInKey";
public static final String SURROGATEFORKEY_PROP = "LinkableObject.surrogateForKey";
public static final String ISOPEN_PROP = "LinkableObject.isOpen";
public static final String SHOWINGMAP_PROP = "LinkableObject.showingMap";
public static final String LINKEDFROM_PROP = "LinkableObject.linkedFrom";
public static final String LINKEDTO_PROP = "LinkableObject.linkedTo";

   protected void setEmpty(){
     linkableObjectKey = 0;
     typeKey = 1;
     loType = null;
     position = "";
     displPageNo = 0;
     surrPageNo = 0;
     displayedInKey = 0;
     displayedIn = null;
     surrogateForKey = 0;
     surrogateFor = null;
     isOpen = true;
     showingMap = true;

   }

   public static String getSelectEntities(){
       return
       "LinkableObject.linkableObjectKey,LinkableObject.typeKey,LinkableObject.position,LinkableObject.displPageNo,LinkableObject.surrPageNo,LinkableObject.displayedInKey,LinkableObject.surrogateForKey,LinkableObject.isOpen,LinkableObject.showingMap";
   }
   
   public static String getTableJoins(){
       return "";
   }
   
   public static String getSQLFrom(){
       return "LinkableObject";
   }

   protected String getKeyName(){
       return "linkableObjectKey";
   }

   protected String getTableName(){
       return "LinkableObject";
   }

   public int loadFromResultSet(ResultSet rs)throws SQLException{
      try {
      linkableObjectKey = rs.getInt(1);
      typeKey = rs.getInt(2);
      loType = null;
      position = rs.getString(3);
      displPageNo = rs.getInt(4);
      surrPageNo = rs.getInt(5);
      displayedInKey = rs.getInt(6);
      displayedIn = null;
      surrogateForKey = rs.getInt(7);
      surrogateFor = null;
      isOpen = rs.getString(8).equals("Y");
      showingMap = rs.getString(9).equals("Y");
      } catch (Exception e){
		e.printStackTrace();
      }     
      return 9;
}

   // this constructor should only be used within the Query object.

   public LinkableObject(boolean realEmpty){
     super();
     setEmpty();
     if(!realEmpty){
        doInsert();
        if(myCache == null)myCache = new LinkableObjectQuery();
        myCache.addNewItem(this.linkableObjectKey, this);
        Rdb2javaPlugin.getDataServer().notifyCreate("LinkableObject", this);
     }
   }

   public LinkableObject(){
     super();
     setEmpty();
     doInsert();
     if(myCache == null)myCache = new LinkableObjectQuery();
     myCache.addNewItem(this.linkableObjectKey, this);
     Rdb2javaPlugin.getDataServer().notifyCreate("LinkableObject", this);
   }

   static public LinkableObject getItem(int key){
      if(myCache == null)myCache = new LinkableObjectQuery();
      return (LinkableObject)myCache.getObject(key);
   }

// code for standard getters

   public int getLinkableObjectKey(){return linkableObjectKey;}
   public LOType getLoType(){
       if(typeKey <= 0)return null;
       if(loType == null)loType = LOType.getItem(typeKey);
       return loType;
   }
   public String getPosition(){return position;}
   public int getDisplPageNo(){return displPageNo;}
   public int getSurrPageNo(){return surrPageNo;}
   public Resource getDisplayedIn(){
       if(displayedInKey <= 0)return null;
       if(displayedIn == null)displayedIn = Resource.getItem(displayedInKey);
       return displayedIn;
   }
   public Resource getSurrogateFor(){
       if(surrogateForKey <= 0)return null;
       if(surrogateFor == null)surrogateFor = Resource.getItem(surrogateForKey);
       return surrogateFor;
   }
   public boolean getIsOpen(){return isOpen;}
   public boolean getShowingMap(){return showingMap;}

// code for standard setters

   public void setLoType(LOType item){
     if((typeKey == 0) && (item == null))return;
     if((item != null) && (item.getALID()==typeKey))return;
     LOType oldItem = null;
     if(typeKey != 0){
        oldItem = getLoType();
        getLoType().getLinkableObjects().remove(this);
        getLoType().signalChangeLinkableObjects(this,null);
     }
     typeKey = 0;
     if(item != null){
        typeKey = item.getALID(); 
        item.getLinkableObjects().add(this);
        item.signalChangeLinkableObjects(null,this);
     }
     loType=item;
     updateDBint("typeKey",typeKey);
     firePropertyChange(TYPEKEY_PROP, oldItem, this);
   }
   public void setPosition(String parm){
     position = parm;
     updateDBString("position",parm);
     firePropertyChange(POSITION_PROP);
   }
   public void setDisplPageNo(int parm){
     displPageNo = parm;
     updateDBint("displPageNo",parm);
     firePropertyChange(DISPLPAGENO_PROP);
   }
   public void setSurrPageNo(int parm){
     surrPageNo = parm;
     updateDBint("surrPageNo",parm);
     firePropertyChange(SURRPAGENO_PROP);
   }
   public void setDisplayedIn(Resource item){
     if((displayedInKey == 0) && (item == null))return;
     if((item != null) && (item.getALID()==displayedInKey))return;
     Resource oldItem = null;
     if(displayedInKey != 0){
        oldItem = getDisplayedIn();
        getDisplayedIn().getMyDisplayedItems().remove(this);
        getDisplayedIn().signalChangeMyDisplayedItems(this,null);
     }
     displayedInKey = 0;
     if(item != null){
        displayedInKey = item.getALID(); 
        item.getMyDisplayedItems().add(this);
        item.signalChangeMyDisplayedItems(null,this);
     }
     displayedIn=item;
     updateDBint("displayedInKey",displayedInKey);
     firePropertyChange(DISPLAYEDINKEY_PROP, oldItem, this);
   }
   public void setSurrogateFor(Resource item){
     if((surrogateForKey == 0) && (item == null))return;
     if((item != null) && (item.getALID()==surrogateForKey))return;
     Resource oldItem = null;
     if(surrogateForKey != 0){
        oldItem = getSurrogateFor();
        if(oldItem != null){ // if test added jb when nullpointerexception appeared for next line
           getSurrogateFor().getMySurrogates().remove(this);
           getSurrogateFor().signalChangeMySurrogates(this,null);
        }
     }
     surrogateForKey = 0;
     if(item != null){
        surrogateForKey = item.getALID(); 
        item.getMySurrogates().add(this);
        item.signalChangeMySurrogates(null,this);
     }
     surrogateFor=item;
     updateDBint("surrogateForKey",surrogateForKey);
     firePropertyChange(SURROGATEFORKEY_PROP, oldItem, this);
   }
   public void setIsOpen(boolean parm){
     isOpen = parm;
     updateDBboolean("isOpen",parm);
     firePropertyChange(ISOPEN_PROP);
   }
   public void setShowingMap(boolean parm){
     showingMap = parm;
     updateDBboolean("showingMap",parm);
     firePropertyChange(SHOWINGMAP_PROP);
   }

// code for saving back to the DB
   
   // private static PreparedStatement stmt1 = null, stmt2 = null;

   /*private void loadParameters(PreparedStatement stmt) throws SQLException {
      try {
        stmt.setInt(1, typeKey);
        stmt.setString(2, position);
        stmt.setInt(3, displPageNo);
        stmt.setInt(4, surrPageNo);
        stmt.setInt(5, displayedInKey);
        stmt.setInt(6, surrogateForKey);
        stmt.setString(7, isOpen?"Y":"N");
        stmt.setString(8, showingMap?"Y":"N");
      } catch (Exception e){
		e.printStackTrace();
      }     
   }*/
   
   protected void loadParameters(Map map){
	   map.put("typeKey", typeKey);
	   map.put("position", position);
	   map.put("displPageNo", displPageNo);
	   map.put("surrPageNo", surrPageNo);
	   map.put("displayedInKey", displayedInKey);
	   map.put("surrogateForKey", surrogateForKey);
	   map.put("isOpen", isOpen?"Y":"N");
	   map.put("showingMap", showingMap?"Y":"N");
   }

   public void deleteMe(){
      if(linkableObjectKey <= 0) return;
      myCache.getMyCache().deleteItem(linkableObjectKey);
      Rdb2javaPlugin.getDataServer().doDelete("LinkableObject", "linkableObjectKey", linkableObjectKey);
      Rdb2javaPlugin.getDataServer().notifyDelete("LinkableObject", this);
      linkableObjectKey = 0;
   }

   public void reIntroduceMe(){
      if(linkableObjectKey > 0)return;
      doInsert();
      myCache.getMyCache().addNewItem(linkableObjectKey, this);
      Rdb2javaPlugin.getDataServer().notifyCreate("LinkableObject", this);
   }

// code for results via foreign keys and intersection sets
   
   protected void addEmptyFKRefLists(){
	   linkedFrom = new FKReferenceList();
	   linkedTo = new FKReferenceList();
   }

   private FKReferenceList linkedFrom = null;
   //private static LinkQuery linkedFromQuery = null;
   private static IPersistentQuery linkedFromQuery = null;

   public FKReferenceList getLinkedFrom(){
	   if(linkableObjectKey == 0)return FKReferenceList.EMPTY_FKREFERENCELIST;
	   if(linkedFromQuery == null){
		   linkedFromQuery = Rdb2javaPlugin.getDataServer().makePersistentQuery(new LinkQuery(), "fromLink");
	   }
	   if(linkedFrom == null)linkedFrom = new FKReferenceList(
			   linkedFromQuery, linkableObjectKey);
	   return linkedFrom;
   }
   public void signalChangeLinkedFrom(Link oldItem, Link newItem){
	   firePropertyChange(LINKEDFROM_PROP,oldItem,newItem);
   }

   private FKReferenceList linkedTo = null;
   private static IPersistentQuery linkedToQuery = null;

   public FKReferenceList getLinkedTo(){
	   if(linkableObjectKey == 0)return FKReferenceList.EMPTY_FKREFERENCELIST;
	   if(linkedToQuery == null){
		   linkedToQuery = Rdb2javaPlugin.getDataServer().makePersistentQuery(new LinkQuery(), "toLink");
	   }
	   if(linkedTo == null)linkedTo = new FKReferenceList(
			   linkedToQuery, linkableObjectKey);
	   return linkedTo;
   }
   public void signalChangeLinkedTo(Link oldItem, Link newItem){
	   firePropertyChange(LINKEDTO_PROP,oldItem,newItem);
   }


// code for handling foreign key references to this entity/class

   public void assignMyReferencesTo(int to){
       doFkAssignment("Link","fromLink",to);
       doFkAssignment("Link","toLink",to);
   }
   
   public void assignMyReferencesTo(LinkableObject assignee){
       assignMyReferencesTo(assignee.getLinkableObjectKey());
   }


// code for extra variables and methods
   
//EM{LinkableObject-getKeys
   public int getLoTypeKey(){return typeKey;}
   public int getDisplayedInKey(){return displayedInKey;}
   public int getSurrogateForKey(){return surrogateForKey;}
//EM}

//EM{linkableObject-LinkHolder
   /*
    * The "cut" action requires that selected object be removed
    * which included removing the connection between this
    * containerSurrogate and its linked note -- done by setLinkTo(null).
    * If a containerSurrogate is subsequently pasted back in elsewhere,
    * the container link has to be restored.  This is the purpose of
    * the following bit of code -- to store the linked container so that it can
    * be reconnected later.  Note, for it to work, "storeContainer()" must
    * be explicitly called before setNote(null).
    */
   
   protected Resource heldSurrogate = null;
   public Resource getHeldSurrogate(){
	   return heldSurrogate;
   }
   public void storeSurrogate(){heldSurrogate = getSurrogateFor();}
   
   protected Resource heldDisplayedIn = null;
   public Resource getHeldDisplayedIn(){
	   return heldDisplayedIn;
   }
   
   public void storeDisplayedIn(){heldDisplayedIn = this.getDisplayedIn();}
   
   protected LOType heldLoType = null;
   public LOType getHeldLoType(){
	   return heldLoType;
   }
   public void storeLoType(){heldLoType = this.getLoType();}
   
   public void backupAndClearResLinks(){
	   heldSurrogate = getSurrogateFor();
	   heldDisplayedIn = getDisplayedIn();
	   heldLoType = getLoType();
	   setSurrogateFor(null);
	   setDisplayedIn(null);
	   setLoType(null);
   }
   
   public void restoreResLinks(){
	   setLoType(heldLoType);
	   setSurrogateFor(heldSurrogate);
	   setDisplayedIn(heldDisplayedIn);
   }
//EM}

//EM{linkableObject-linkList
   /*
    * This material is used to support the handling of links during a drag and
    * drop that takes the material from one holding resource to another.  It is
    * therefore built in the Orphaning command OrphanLinkableObjectsCommand
    * and processed when the object is re-introduced in AddLinkableObjectsCommand
    */
   Vector linkData = null;
   public Vector getLinkData(){
	   return linkData;
   }
   
   public void setLinkData(Vector linkData){
	   this.linkData = linkData;
   }
//EM}

//EM{linkableObject-point
   public void setDisplayRectangle(Rectangle r){
	   String posString = "rect:";
	   posString += r.x+","+r.y+","+r.width+","+r.height;
	   this.setPosition(posString);
   }
   
   public Rectangle getDisplayRectangle(){
	   String posString = getPosition().trim();
	   if((posString == null) || (posString.equals("")))
			   return new Rectangle(0,0,0,0);
	   String[] parts = posString.split(":");
	   if(!parts[0].equals("rect"))return null;
	   String[] numbs = parts[1].split(",");
	   if(numbs.length != 4)return null;
	   return new Rectangle(
			   Integer.parseInt(numbs[0]) /* x */,
			   Integer.parseInt(numbs[1]) /* y */,
			   Integer.parseInt(numbs[2]) /* width */,
			   Integer.parseInt(numbs[3]) /* height */);
   }
//EM}

//EM{linkableObject-setResourceKeys
   public void setResourceKeys(int surrogate, int displayedIn){
	   if(linkableObjectKey == 0){
		   surrogateForKey = surrogate;
		   displayedInKey = displayedIn;
		   return;
	   }
	   setSurrogateFor(Resource.getItem(surrogate));
	   setDisplayedIn(Resource.getItem(displayedIn));
   }
//EM}

//EM{linkableObject-showMap
   public boolean shouldShowMap(){
	   if(surrogateForKey == 0)return false;
	   return getShowingMap();
   }
//EM}

// code for toString

   private String displayKey(){
	  Resource surr = this.getSurrogateFor();
	  Resource disp = this.getDisplayedIn();
      return "Surr:"+surr+", disp: "+disp;
   }

   public String toString(){
      return "LinkableObject: "+displayKey()+"("+getLinkableObjectKey()+")";
   }
   
   public String getALItem() {
       if(getLinkableObjectKey() <= 1) return "";
       return displayKey();
   }
   
   public int getALID() {
       return getLinkableObjectKey();
   }
   
   protected void setALID(int key){linkableObjectKey = key;}
   
}
