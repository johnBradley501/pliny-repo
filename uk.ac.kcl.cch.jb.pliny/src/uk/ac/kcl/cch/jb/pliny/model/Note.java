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
import java.sql.Timestamp;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import uk.ac.kcl.cch.rdb2java.Rdb2javaPlugin;
import uk.ac.kcl.cch.rdb2java.dynData.*;
import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.dnd.ClipboardHandler;
import uk.ac.kcl.cch.jb.pliny.editors.NoteEditorInput;


public class Note extends Resource implements IAuthorityListItem, ILoadableFromResultSet {
   static protected NoteQuery myCache = new NoteQuery();

   private String content;
   private Timestamp tStamp;

public static final String CONTENT_PROP = "Note.content";
public static final String TSTAMP_PROP = "Note.tStamp";

   protected void setEmpty(){
     super.setEmpty();
     content = "";
     tStamp = new Timestamp((new java.util.Date()).getTime());
   }

   public static String getSelectEntities(){
       return Resource.getSelectEntities()+
       ",Note.content,Note.tStamp";
   }
   
   public static String getTableJoins(){
       String sJoin = Resource.getTableJoins();
       String connector = "";
       if(!sJoin.equals(""))connector = " AND ";
       return sJoin+connector+"Resource.resourceKey=Note.resourceKey";
   }
   
   public static String getSQLFrom(){
       return Resource.getSQLFrom()+",Note";
   }

   protected String getKeyName(){
       return "resourceKey";
   }

   protected String getTableName(){
       return "Note";
   }

   public int loadFromResultSet(ResultSet rs)throws SQLException{
      int offset = super.loadFromResultSet(rs);
      try {
      content = rs.getString("content"); //offset+1);
      tStamp = rs.getTimestamp("tStamp"); //offset+2);
      } catch (Exception e){
		e.printStackTrace();
      }     
      return offset+2;
}

   // this constructor should only be used within the Query object.

   public Note(boolean realEmpty){
     super(realEmpty);
     //setEmpty();
     if(!realEmpty){
        //doInsert();
        if(myCache == null)myCache = new NoteQuery();
        myCache.addNewItem(this.resourceKey, this);
     }
   }

   public Note(){
     super();
     //setEmpty();
     //doInsert();
     if(myCache == null)myCache = new NoteQuery();
     myCache.addNewItem(this.resourceKey, this);
   }


   static public Note getNoteItem(int key){
      if(myCache == null)myCache = new NoteQuery();
      return (Note)myCache.getObject(key);
	  //return (Note)Resource.getItem(key);
   }


// code for standard getters

   public String getContent(){return content;}
   public Timestamp getTStamp(){return tStamp;}

// code for standard setters

   public void setContent(String parm){
     content = parm;
	 boolean noteSeparate = Rdb2javaPlugin.getDataServer().isNoteInSeparateEntity();
     updateDBString("content",noteSeparate?"Note":"Resource",parm);
     firePropertyChange(CONTENT_PROP);
   }
   public void setTStamp(Timestamp parm){
     tStamp = parm;
	 boolean noteSeparate = Rdb2javaPlugin.getDataServer().isNoteInSeparateEntity();
     updateDBTimestamp("tStamp",noteSeparate?"Note":"Resource",parm);
     firePropertyChange(TSTAMP_PROP);
   }

// code for saving back to the DB

   protected void loadParameters(Map map){
	   boolean noteSeparate = Rdb2javaPlugin.getDataServer().isNoteInSeparateEntity();
	   //if(!noteSeparate)super.loadParameters(map);
       map.put("content", content);
       map.put("tStamp", tStamp);
       if(noteSeparate)map.put("resourceKey", resourceKey);
   }
   
	 private IDataInserter noteInserter = null;
	 
	 protected void doInsert(){
		 super.doInsert();
		 if(!Rdb2javaPlugin.getDataServer().isNoteInSeparateEntity())return;
		 if(noteInserter == null)
			 noteInserter = Rdb2javaPlugin.getDataServer().makeDataInserter(getTableName());
		 HashMap parmMap = new HashMap();
		 loadParameters(parmMap);
		 int keyval = noteInserter.doInsert(parmMap);
		 //setALID(keyval);
	 }

   public void deleteMe(){
      if(resourceKey <= 0) return;
      myCache.getMyCache().deleteItem(resourceKey);
      int heldKey = getResourceKey();
      super.deleteMe();
      resourceKey = 0; // perhaps already done with super.deleteMe()  .jb
      if(!Rdb2javaPlugin.getDataServer().isNoteInSeparateEntity())return;
      Rdb2javaPlugin.getDataServer().doDelete("Note", "resourceKey", heldKey);
   }

   public void reIntroduceMe(){
      if(resourceKey > 0)return;
      doInsert();
      myCache.getMyCache().addNewItem(resourceKey, this);
      Resource.myCache.addNewItem(resourceKey, this);
      Rdb2javaPlugin.getDataServer().notifyCreate("Resource", this);
   }

// code for results via foreign keys and intersection sets



// code for extra variables and methods

//EM{note-editorStuff
   public static final String NOTE_EDITOR_ID = "uk.ac.kcl.cch.jb.pliny.noteEditor";
   private static ObjectType noteObjectType = null;
   
   public void openEditor(IWorkbenchPage page) throws PartInitException{
	   NoteEditorInput theInput = new NoteEditorInput(this);
	   page.openEditor(theInput, NOTE_EDITOR_ID);
   }
   
   public static ObjectType getNoteObjectType(){
	   if(noteObjectType == null)
	      noteObjectType = ObjectType.findFromEditorId(NOTE_EDITOR_ID);
	   return noteObjectType;
   }
//EM}
   
//EM{note-getIdentifier
   public String getIdentifier(){
	   return "note:"+getALID();
   }

//EM}
   
//EM{note-editor-sash
   public int getSashPosition(){
	   String attr = getAttributes();
	   if((attr == null) || (attr.equals("")))return 50;
	   String[] parts = attr.trim().split(":");
	   if(!parts[0].equals("sash"))return 50;
	   return Integer.parseInt(parts[1]);
   }
   
   public void setSashPosition(int val){
	   setAttributes("sash:"+val);
   }
   
//EM}
   
// EM{note-cutcopytext
   public String getCutCopyText(){
	   StringBuffer buf = new StringBuffer();
	   
	   buf.append(getFullName());
	   String contents = this.getContent();
	   if(contents != null && contents.length() > 0){
		   // buf.append(ClipboardHandler.generateSeparator());
		   buf.append(System.getProperty("line.separator"));
		   buf.append(contents);
	   }
	   return buf.toString();
   }
//EM}


// code for interface AuthorityListItem and toString

   private String displayKey(){
      return getName();
   }

   public String toString(){
      return "Note: "+displayKey()+"("+getResourceKey()+")";
   }
   
   public String getALItem() {
       if(getResourceKey() <= 1) return "";
       return displayKey();
   }
   
   public int getALID() {
       return getResourceKey();
   }
}
