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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class LOType extends BaseObject
implements IAuthorityListItem, ILoadableFromResultSet {
   static protected LOTypeQuery myCache = new LOTypeQuery();

   protected int loTypeKey;
   private String name;
   private int titleForeColourInt;
   private int titleBackColourInt;
   private int bodyForeColourInt;
   private int bodyBackColourInt;
   private int sourceRoleKey;
   private Resource sourceRole;
   private int targetRoleKey;
   private Resource targetRole;

public static final String NAME_PROP = "LOType.name";
public static final String TITLEFORECOLOURINT_PROP = "LOType.titleForeColourInt";
public static final String TITLEBACKCOLOURINT_PROP = "LOType.titleBackColourInt";
public static final String BODYFORECOLOURINT_PROP = "LOType.bodyForeColourInt";
public static final String BODYBACKCOLOURINT_PROP = "LOType.bodyBackColourInt";
public static final String SOURCEROLEKEY_PROP = "LOType.sourceRoleKey";
public static final String TARGETROLEKEY_PROP = "LOType.targetRoleKey";
public static final String LINKS_PROP = "LOType.links";
public static final String LINKABLEOBJECTS_PROP = "LOType.linkableObjects";

   protected void setEmpty(){
     loTypeKey = 0;
     name = "";
     titleForeColourInt = 0;
     titleBackColourInt = 0;
     bodyForeColourInt = 0;
     bodyBackColourInt = 0;
     sourceRoleKey = 0;
     sourceRole = null;
     targetRoleKey = 0;
     targetRole = null;

   }

   public static String getSelectEntities(){
       return
       "LOType.loTypeKey,LOType.name,LOType.titleForeColour,LOType.titleBackColour,LOType.bodyForeColour,LOType.bodyBackColour,LOType.sourceRoleKey,LOType.targetRoleKey";
   }
   
   public static String getTableJoins(){
       return "";
   }
   
   public static String getSQLFrom(){
       return "LOType";
   }

   protected String getKeyName(){
       return "loTypeKey";
   }

   protected String getTableName(){
       return "LOType";
   }

   public int loadFromResultSet(ResultSet rs)throws SQLException{
      try {
      loTypeKey = rs.getInt(1);
      name = rs.getString(2);
      titleForeColourInt = rs.getInt(3);
      titleBackColourInt = rs.getInt(4);
      bodyForeColourInt = rs.getInt(5);
      bodyBackColourInt = rs.getInt(6);
      sourceRoleKey = rs.getInt(7);
      sourceRole = null;
      targetRoleKey = rs.getInt(8);
      targetRole = null;
      } catch (Exception e){
		e.printStackTrace();
      }     
      return 8;
}

   // this constructor should only be used within the Query object.

   public LOType(boolean realEmpty){
     super();
     setEmpty();
     if(!realEmpty){
        doInsert();
        if(myCache == null)myCache = new LOTypeQuery();
        myCache.addNewItem(this.loTypeKey, this);
        Rdb2javaPlugin.getDataServer().notifyCreate("LOType", this);
     }
   }

   public LOType(){
     super();
     setEmpty();
     doInsert();
     if(myCache == null)myCache = new LOTypeQuery();
     myCache.addNewItem(this.loTypeKey, this);
     Rdb2javaPlugin.getDataServer().notifyCreate("LOType", this);
   }

   static public LOType getItem(int key){
      if(myCache == null)myCache = new LOTypeQuery();
      return (LOType)myCache.getObject(key);
   }

// code for standard getters

   public int getLoTypeKey(){return loTypeKey;}
   public String getName(){return name;}
   public int getTitleForeColourInt(){return titleForeColourInt;}
   public int getTitleBackColourInt(){return titleBackColourInt;}
   public int getBodyForeColourInt(){return bodyForeColourInt;}
   public int getBodyBackColourInt(){return bodyBackColourInt;}
   public Resource getSourceRole(){
       if(sourceRoleKey <= 0)return null;
       if(sourceRole == null)sourceRole = Resource.getItem(sourceRoleKey);
       return sourceRole;
   }
   public Resource getTargetRole(){
       if(targetRoleKey <= 0)return null;
       if(targetRole == null)targetRole = Resource.getItem(targetRoleKey);
       return targetRole;
   }

// code for standard setters

   public void setName(String parm){
     name = parm;
     updateDBString("name",parm);
     firePropertyChange(NAME_PROP);
   }
   public void setTitleForeColourInt(int parm){
     titleForeColourInt = parm;
     updateDBint("titleForeColour",parm);
     firePropertyChange(TITLEFORECOLOURINT_PROP);
   }
   public void setTitleBackColourInt(int parm){
     titleBackColourInt = parm;
     updateDBint("titleBackColour",parm);
     firePropertyChange(TITLEBACKCOLOURINT_PROP);
   }
   public void setBodyForeColourInt(int parm){
     bodyForeColourInt = parm;
     updateDBint("bodyForeColour",parm);
     firePropertyChange(BODYFORECOLOURINT_PROP);
   }
   public void setBodyBackColourInt(int parm){
     bodyBackColourInt = parm;
     updateDBint("bodyBackColour",parm);
     firePropertyChange(BODYBACKCOLOURINT_PROP);
   }
   public void setSourceRole(Resource item){
     if((sourceRoleKey == 0) && (item == null))return;
     if((item != null) && (item.getALID()==sourceRoleKey))return;
     Resource oldItem = null;
     if(sourceRoleKey != 0){
        oldItem = getSourceRole();
        getSourceRole().getMySourceRoles().remove(this);
        getSourceRole().signalChangeMySourceRoles(this,null);
     }
     sourceRoleKey = 0;
     if(item != null){
        sourceRoleKey = item.getALID(); 
        item.getMySourceRoles().add(this);
        item.signalChangeMySourceRoles(null,this);
     }
     sourceRole=item;
     updateDBint("sourceRoleKey",sourceRoleKey);
     firePropertyChange(SOURCEROLEKEY_PROP, oldItem, this);
   }
   public void setTargetRole(Resource item){
     if((targetRoleKey == 0) && (item == null))return;
     if((item != null) && (item.getALID()==targetRoleKey))return;
     Resource oldItem = null;
     if(targetRoleKey != 0){
        oldItem = getTargetRole();
        getTargetRole().getMyTargetRoles().remove(this);
        getTargetRole().signalChangeMyTargetRoles(this,null);
     }
     targetRoleKey = 0;
     if(item != null){
        targetRoleKey = item.getALID(); 
        item.getMyTargetRoles().add(this);
        item.signalChangeMyTargetRoles(null,this);
     }
     targetRole=item;
     updateDBint("targetRoleKey",targetRoleKey);
     firePropertyChange(TARGETROLEKEY_PROP, oldItem, this);
   }

// code for saving back to the DB

   protected void loadParameters(Map map) {
        map.put("name", name);
        map.put("titleForeColour", titleForeColourInt);
        map.put("titleBackColour", titleBackColourInt);
        map.put("bodyForeColour", bodyForeColourInt);
        map.put("bodyBackColour", bodyBackColourInt);
        map.put("sourceRoleKey", sourceRoleKey);
        map.put("targetRoleKey", targetRoleKey);
   }

   public void deleteMe(){
      if(loTypeKey <= 0) return;
      myCache.getMyCache().deleteItem(loTypeKey);
      Rdb2javaPlugin.getDataServer().doDelete("LOType", "loTypeKey", loTypeKey);
      Rdb2javaPlugin.getDataServer().notifyDelete("LOType", this);
      loTypeKey = 0;
   }

   public void reIntroduceMe(){
      if(loTypeKey > 0)return;
      doInsert();
      myCache.getMyCache().addNewItem(loTypeKey, this);
      Rdb2javaPlugin.getDataServer().notifyCreate("LOType", this);
   }

// code for results via foreign keys and intersection sets
   
   
   protected void addEmptyFKRefLists(){
	   links = new FKReferenceList();
	   linkableObjects = new FKReferenceList();
   }

   private FKReferenceList links = null;
   private static IPersistentQuery linkQuery = null;

   public FKReferenceList getLinks(){
	   if(loTypeKey == 0)return FKReferenceList.EMPTY_FKREFERENCELIST;
	   if(linkQuery == null)
		   linkQuery = Rdb2javaPlugin.getDataServer().makePersistentQuery(new LinkQuery(), "typeKey");
	   if(links == null)links = new FKReferenceList(linkQuery, loTypeKey);
	     //new LinkQuery(), "typeKey="+loTypeKey);
	   return links;
   }
   public void signalChangeLinks(Link oldItem, Link newItem){
	   firePropertyChange(LINKS_PROP,oldItem,newItem);
   }

   private FKReferenceList linkableObjects = null;
   private static IPersistentQuery linkableObjQuery = null;

   public FKReferenceList getLinkableObjects(){
	   if(loTypeKey == 0)return FKReferenceList.EMPTY_FKREFERENCELIST;
	   if(linkableObjQuery == null)
		   linkableObjQuery = Rdb2javaPlugin.getDataServer().makePersistentQuery(new LinkableObjectQuery(), "typeKey");
	   if(linkableObjects == null)linkableObjects = new FKReferenceList(linkableObjQuery, loTypeKey);
	     //new LinkableObjectQuery(), "typeKey="+loTypeKey);
	   return linkableObjects;
   }
   public void signalChangeLinkableObjects(LinkableObject oldItem, LinkableObject newItem){
	   firePropertyChange(LINKABLEOBJECTS_PROP,oldItem,newItem);
   }


// code for handling foreign key references to this entity/class

   public void assignMyReferencesTo(int to){
       doFkAssignment("LinkableObject","typeKey",to);
       doFkAssignment("Link","typeKey",to);
   }
   
   public void assignMyReferencesTo(LOType assignee){
       assignMyReferencesTo(assignee.getLoTypeKey());
   }


// code for extra variables and methods

//EM{LoType-colour
   protected RGB intToRgb(int colour){
	   int blue = colour & 255;
	   colour = colour >> 8;
	   int green = colour & 255;
	   colour = colour >> 8;
	   int red = colour & 255;
	   return new RGB(red, green, blue);
   }
   
   protected int rgbToInt(RGB colour){
	   int rslt = colour.blue;
	   rslt = rslt | colour.green << 8;
	   rslt = rslt | colour.red << 16;
	   return rslt;
   }

   public RGB getTitleForeColourRGB(){return intToRgb(getTitleForeColourInt());}
   public RGB getTitleBackColourRGB(){return intToRgb(getTitleBackColourInt());}
   public RGB getBodyForeColourRGB(){return intToRgb(getBodyForeColourInt());}
   public RGB getBodyBackColourRGB(){return intToRgb(getBodyBackColourInt());}
   
   public static String rgbToString(RGB rgb){
	   return rgb.red+","+rgb.green+","+rgb.blue;
   }
   
   public static RGB stringToRGB(String string){
	   String[] colours = string.split(",");
	   return new RGB(Integer.parseInt(colours[0]), 
			   Integer.parseInt(colours[1]),
			   Integer.parseInt(colours[2]));
   }

   private Color titleForeColour = null;
   private String titleForeColourID = "tfc-";
   
   public Color getTitleForeColour(){
	   if(titleForeColour == null)
		   titleForeColour = PlinyPlugin.getColour(titleForeColourID+getSavedID(), 
				                                   getTitleForeColourRGB());
	   return titleForeColour;
   }
   
   public void setTitleForeColourRGB(RGB parm){
	   titleForeColour = null;
	   myColourIcon = null;
	   PlinyPlugin.updateColour(titleForeColourID+getSavedID(), parm);
	   setTitleForeColourInt(rgbToInt(parm));
   }

   private Color titleBackColour = null;
   private String titleBackColourID = "tbc-";
   
   public Color getTitleBackColour(){
	   if(titleBackColour == null)
		   titleBackColour = PlinyPlugin.getColour(titleBackColourID+getSavedID(), 
				                                   getTitleBackColourRGB());
	   return titleBackColour;
   }
   
   public void setTitleBackColourRGB(RGB parm){
	   titleBackColour = null;
	   myColourIcon = null;
	   PlinyPlugin.updateColour(titleBackColourID+getSavedID(), parm);
	   setTitleBackColourInt(rgbToInt(parm));
   }
 
   private Color bodyForeColour = null;
   private String bodyForeColourID = "bfc-";
   
   public Color getBodyForeColour(){
	   if(bodyForeColour == null)
		   bodyForeColour = PlinyPlugin.getColour(bodyForeColourID+getSavedID(), 
				                                   getBodyForeColourRGB());
	   return bodyForeColour;
   }
   
   public void setBodyForeColourRGB(RGB parm){
	   bodyForeColour = null;
	   PlinyPlugin.updateColour(bodyForeColourID+getSavedID(), parm);
	   setBodyForeColourInt(rgbToInt(parm));
   }

   
   private Color bodyBackColour = null;
   private String bodyBackColourID = "bbc-";
   
   public Color getBodyBackColour(){
	   if(bodyBackColour == null)
		   bodyBackColour = PlinyPlugin.getColour(bodyBackColourID+getSavedID(), 
				                                   getBodyBackColourRGB());
	   return bodyBackColour;
   }
   
   public void setBodyBackColourRGB(RGB parm){
	   bodyBackColour = null;
	   PlinyPlugin.updateColour(bodyBackColourID+getSavedID(), parm);
	   setBodyBackColourInt(rgbToInt(parm));
   }
//EM}

//EM{LOType-colourIcon
   private Image myColourIcon = null;
   private Image myBackColourIcon = null;
   
   private Image getColourIconImp(Color fore, Color back, String type){
	   Image rslt = new Image(Display.getCurrent(),16,16);
	   GC gc = new GC(rslt);
	   gc.setForeground(fore);
	   gc.setBackground(back);
	   gc.fillRectangle(rslt.getBounds());
	   Point extent = gc.stringExtent("T");
	   gc.drawString("T", (16-extent.x)/2, (16-extent.y)/2);
	   gc.dispose();
	   PlinyPlugin.getDefault().getImageRegistry().remove("LoType-icon"+type+getSavedID());
	   //PlinyPlugin.getDefault().getImageRegistry().put("LoType-icon"+type+getSavedID(),myColourIcon);
	   PlinyPlugin.getDefault().getImageRegistry().put("LoType-icon"+type+getSavedID(),rslt);
	   return rslt;
   }
   
   public Image getColourIcon(){
	   if(myColourIcon != null)return myColourIcon;
	   myColourIcon = getColourIconImp(getTitleForeColour(),getTitleBackColour(),"main");
	   return myColourIcon;
   }
   
   public Image getBackColorIcon(){
	   if(myBackColourIcon != null)return myBackColourIcon;
	   myBackColourIcon = getColourIconImp(getBodyForeColour(),getBodyBackColour(),"back");
	   return myBackColourIcon;
   }
//EM}

//EM{LoType-standardTypes
   public static final int MAX_UNDELETABLE_TYPES = 2;
   
   private static LOType defaultType = null;
   
   public static LOType getDefaultType(){
	   if(defaultType == null)defaultType = getItem(1);
	   return defaultType;
   }
   
   private static LOType bibRefType = null;
   
   public static LOType getBibRefType(){
	   if(bibRefType == null)bibRefType = getItem(2);
	   return bibRefType;
   }
   
   private static LOType currentType = null;
   
   public static final String NEW_CURRENT_EVENT = "currentTypeManager.newType";
   
   public static class CurrentTypeManager extends PropertyChangeObject {
	   public void notifyNewCurrent(LOType oldType, LOType newType){
		   firePropertyChange(NEW_CURRENT_EVENT, oldType, newType);
	   }
   }
   
   private static CurrentTypeManager currentTypeManager = new CurrentTypeManager();
   public static CurrentTypeManager getCurrentTypeManager(){
	   return currentTypeManager;
   }
   
   public static LOType getCurrentType(){
	   if(currentType ==  null)currentType = getDefaultType();
	   return currentType;
   }
   
   public static void setCurrentType(LOType newType){
	   LOType oldType = currentType;
	   currentType = newType;
	   currentTypeManager.notifyNewCurrent(oldType, newType);
   }
//EM}

// code for toString

   private String displayKey(){
      return getName();
   }

   public String toString(){
      return "LOType: "+displayKey()+"("+getLoTypeKey()+")";
   }
   
   public String getALItem() {
       if(getLoTypeKey() <= 1) return "";
       return displayKey();
   }
   
   public int getALID() {
       return getLoTypeKey();
   }
   
   protected void setALID(int key){loTypeKey = key;}
}
