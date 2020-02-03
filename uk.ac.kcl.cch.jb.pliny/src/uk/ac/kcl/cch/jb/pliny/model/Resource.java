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
import java.sql.Time;
import java.sql.ResultSet;
import java.sql.SQLException;

import uk.ac.kcl.cch.rdb2java.Rdb2javaPlugin;
import uk.ac.kcl.cch.rdb2java.dynData.*;
import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;

import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.part.FileEditorInput;
import uk.ac.kcl.cch.jb.pliny.browser.BrowserEditorInput;
import uk.ac.kcl.cch.jb.pliny.data.rdb.DBServices;
import uk.ac.kcl.cch.jb.pliny.editors.IInputCanContainResource;
import uk.ac.kcl.cch.jb.pliny.editors.IPageSettableEditorPart;
import uk.ac.kcl.cch.jb.pliny.editors.NoteEditorInput;
import uk.ac.kcl.cch.jb.pliny.editors.ResourceEditorInput;

public class Resource extends BaseObject
implements IAuthorityListItem, ILoadableFromResultSet,INamedObject, IHasAttribute {
   static protected ResourceQuery myCache = new ResourceQuery();

   protected int resourceKey;
   private String fullName;
   private String initChar;
   private int objectTypeKey;
   private ObjectType objectType;
   private String identifier;
   private String idStart;
   private String attributes;
   private Date creationDate;
   private Time creationTime;

public static final String FULLNAME_PROP = "Resource.fullName";
public static final String INITCHAR_PROP = "Resource.initChar";
public static final String OBJECTTYPEKEY_PROP = "Resource.objectTypeKey";
public static final String IDENTIFIER_PROP = "Resource.identifier";
public static final String IDSTART_PROP = "Resource.idStart";
public static final String ATTRIBUTES_PROP = "Resource.attributes";
public static final String CREATIONDATE_PROP = "Resource.creationDate";
public static final String CREATIONTIME_PROP = "Resource.creationTime";
public static final String MYSOURCEROLES_PROP = "Resource.mySourceRoles";
public static final String MYSURROGATES_PROP = "Resource.mySurrogates";
public static final String FAVOURITES_PROP = "Resource.favourites";
public static final String MYTARGETROLES_PROP = "Resource.myTargetRoles";
public static final String MYDISPLAYEDITEMS_PROP = "Resource.myDisplayedItems";

   protected void setEmpty(){
     resourceKey = 0;
     fullName = "";
     initChar = "";
     objectTypeKey = 0;
     objectType = null;
     identifier = "";
     idStart = "";
     attributes = "";
     creationDate = new Date(System.currentTimeMillis());
     creationTime = new Time(System.currentTimeMillis());

   }

   public static String getSelectEntities(){
       return
       "Resource.resourceKey,Resource.fullName,Resource.initChar,Resource.objectTypeKey,Resource.identifier,Resource.idStart,Resource.attributes,Resource.creationDate,Resource.creationTime";
   }
   
   public static String getTableJoins(){
       return "";
   }
   
   public static String getSQLFrom(){
       return "Resource";
   }

   protected String getKeyName(){
       return "resourceKey";
   }

   protected String getTableName(){
       return "Resource";
   }

   public int loadFromResultSet(ResultSet rs)throws SQLException{
      try {
      resourceKey = rs.getInt(1);
      fullName = rs.getString(2);
      initChar = rs.getString(3);
      objectTypeKey = rs.getInt(4);
      objectType = null;
      identifier = rs.getString(5);
      idStart = rs.getString(6);
      attributes = rs.getString(7);
      creationDate = rs.getDate(8);
      creationTime = rs.getTime(9);
      } catch (Exception e){
		e.printStackTrace();
      }     
      return 9;
}

   // this constructor should only be used within the Query object.

   public Resource(boolean realEmpty){
     super();
     setEmpty();
     if(!realEmpty){
        doInsert();
        if(myCache == null)myCache = new ResourceQuery();
        myCache.addNewItem(this.resourceKey, this);
        Rdb2javaPlugin.getDataServer().notifyCreate("Resource", this);
     }
   }

   public Resource(){
     super();
     setEmpty();
     doInsert();
     if(myCache == null)myCache = new ResourceQuery();
     myCache.addNewItem(this.resourceKey, this);
     Rdb2javaPlugin.getDataServer().notifyCreate("Resource", this);
   }

   static public Resource getItem(int key){
      if(myCache == null)myCache = new ResourceQuery();
      return (Resource)myCache.getObject(key);
   }

// code for standard getters

   public int getResourceKey(){return resourceKey;}
   public String getFullName(){return fullName;}
   public String getInitChar(){return initChar;}
   public ObjectType getObjectType(){
       if(objectTypeKey <= 0)return null;
       if(objectType == null)objectType = ObjectType.getItem(objectTypeKey);
       return objectType;
   }
   public String getIdentifier(){return identifier;}
   public String getIdStart(){return idStart;}
   public String getAttributes(){return attributes;}
   public Date getCreationDate(){return creationDate;}
   public Time getCreationTime(){return creationTime;}

// code for standard setters

   public void setFullName(String parm){
     fullName = parm;
     updateDBString("fullName","Resource",parm);
     firePropertyChange(FULLNAME_PROP);
   }
   public void setInitChar(String parm){
     initChar = parm;
     updateDBString("initChar","Resource",parm);
     firePropertyChange(INITCHAR_PROP);
   }
   public void setObjectType(ObjectType item){
     if((objectTypeKey == 0) && (item == null))return;
     if((item != null) && (item.getALID()==objectTypeKey))return;
     ObjectType oldItem = null;
     if(objectTypeKey != 0){
        oldItem = getObjectType();
        getObjectType().getResources().remove(this);
        getObjectType().signalChangeResources(this,null);
     }
     objectTypeKey = 0;
     if(item != null){
        objectTypeKey = item.getALID(); 
        item.getResources().add(this);
        item.signalChangeResources(null,this);
     }
     objectType=item;
     updateDBint("objectTypeKey","Resource",objectTypeKey);
     firePropertyChange(OBJECTTYPEKEY_PROP, oldItem, this);
   }
   public void setIdentifier(String parm){
     identifier = parm;
     updateDBString("identifier","Resource",parm);
     firePropertyChange(IDENTIFIER_PROP);
   }
   public void setIdStart(String parm){
     idStart = parm;
     updateDBString("idStart","Resource",parm);
     firePropertyChange(IDSTART_PROP);
   }
   public void setAttributes(String parm){
     attributes = parm;
     updateDBString("attributes","Resource",parm);
     firePropertyChange(ATTRIBUTES_PROP);
   }
   public void setCreationDate(Date parm){
     creationDate = parm;
     updateDBDate("creationDate","Resource",parm);
     firePropertyChange(CREATIONDATE_PROP);
   }
   public void setCreationTime(Time parm){
     creationTime = parm;
     updateDBTime("creationTime","Resource",parm);
     firePropertyChange(CREATIONTIME_PROP);
   }

// code for saving back to the DB

	 protected void doInsert(){
		 if(myInserter == null)
			 myInserter = Rdb2javaPlugin.getDataServer().makeDataInserter("Resource"); // needed because of Note's super referende here  jb
		 HashMap parmMap = new HashMap();
		 loadResourceParameters(parmMap);
		 boolean noteSeparate = Rdb2javaPlugin.getDataServer().isNoteInSeparateEntity();
		 if((!noteSeparate) && this instanceof Note)((Note)this).loadParameters(parmMap);

		 int keyval = myInserter.doInsert(parmMap);
		 setALID(keyval);
		 addEmptyFKRefLists();
	 }

	 private void loadResourceParameters(Map map){
       map.put("fullName", fullName);
       map.put("initChar", initChar);
       map.put("objectTypeKey", objectTypeKey);
       map.put("identifier", identifier);
       map.put("idStart", idStart);
       map.put("attributes", attributes);
       map.put("creationDate", creationDate);
       map.put("creationTime", creationTime);
   }
   
   protected void loadParameters(Map map){
	   loadResourceParameters(map);
   }

   public void deleteMe(){
      if(resourceKey <= 0) return;
      myCache.getMyCache().deleteItem(resourceKey);
      Rdb2javaPlugin.getDataServer().doDelete("Resource", "resourceKey", resourceKey);
      Rdb2javaPlugin.getDataServer().notifyDelete("Resource", this);
      resourceKey = 0;
   }

   public void reIntroduceMe(){
      if(resourceKey > 0)return;
      doInsert();
      myCache.getMyCache().addNewItem(resourceKey, this);
      Rdb2javaPlugin.getDataServer().notifyCreate("Resource", this);
   }

// code for results via foreign keys and intersection sets
   
   protected void addEmptyFKRefLists(){
	   mySourceRoles = new FKReferenceList();
	   mySurrogates = new FKReferenceList();
	   favourites = new FKReferenceList();
	   myTargetRoles = new FKReferenceList();
	   myDisplayedItems = new FKReferenceList();
   }

   private FKReferenceList mySourceRoles = null;
   private static IPersistentQuery mySourceRolesQuery = null;

   public FKReferenceList getMySourceRoles(){
	   if(resourceKey == 0)return FKReferenceList.EMPTY_FKREFERENCELIST;
	   if(mySourceRolesQuery == null)
		   mySourceRolesQuery = Rdb2javaPlugin.getDataServer().makePersistentQuery(new LOTypeQuery(), "sourceRoleKey");
	   if(mySourceRoles == null)mySourceRoles = new FKReferenceList(mySourceRolesQuery, resourceKey);
	     //new LOTypeQuery(), "sourceRoleKey="+resourceKey);
	   return mySourceRoles;
   }
   public void signalChangeMySourceRoles(LOType oldItem, LOType newItem){
	   firePropertyChange(MYSOURCEROLES_PROP,oldItem,newItem);
   }

   private FKReferenceList mySurrogates = null;
   private static IPersistentQuery mySurrogatesQuery = null;

   public FKReferenceList getMySurrogates(){
	   if(resourceKey == 0)return FKReferenceList.EMPTY_FKREFERENCELIST;
	   if(mySurrogatesQuery == null){
		   mySurrogatesQuery = Rdb2javaPlugin.getDataServer().makePersistentQuery(new LinkableObjectQuery(), "surrogateForKey");
	   }
	   if(mySurrogates == null)mySurrogates = new FKReferenceList(mySurrogatesQuery, resourceKey);
	     //new LinkableObjectQuery(), "surrogateForKey="+resourceKey);
	   return mySurrogates;
   }
   public void signalChangeMySurrogates(LinkableObject oldItem, LinkableObject newItem){
	   firePropertyChange(MYSURROGATES_PROP,oldItem,newItem);
   }

   private FKReferenceList favourites = null;
   private static IPersistentQuery favouritesQuery = null;

   public FKReferenceList getFavourites(){
	   if(resourceKey == 0)return FKReferenceList.EMPTY_FKREFERENCELIST;
	   if(favouritesQuery == null)
		   favouritesQuery = Rdb2javaPlugin.getDataServer().makePersistentQuery(new FavouriteQuery(), "favouriteResource");
	   if(favourites == null)favourites = new FKReferenceList(favouritesQuery, resourceKey);
	     //new FavouriteQuery(), "favouriteResource="+resourceKey);
	   return favourites;
   }
   public void signalChangeFavourites(Favourite oldItem, Favourite newItem){
	   firePropertyChange(FAVOURITES_PROP,oldItem,newItem);
   }

   private FKReferenceList myTargetRoles = null;
   private static IPersistentQuery myTargetRolesQuery = null;

   public FKReferenceList getMyTargetRoles(){
	   if(resourceKey == 0)return FKReferenceList.EMPTY_FKREFERENCELIST;
	   if(myTargetRolesQuery == null)
		   myTargetRolesQuery = Rdb2javaPlugin.getDataServer().makePersistentQuery(new LOTypeQuery(), "targetRoleKey");
	   if(myTargetRoles == null)myTargetRoles = new FKReferenceList(myTargetRolesQuery, resourceKey);
	     //new LOTypeQuery(), "targetRoleKey="+resourceKey);
	   return myTargetRoles;
   }
   public void signalChangeMyTargetRoles(LOType oldItem, LOType newItem){
	   firePropertyChange(MYTARGETROLES_PROP,oldItem,newItem);
   }

   private FKReferenceList myDisplayedItems = null;
   private static IPersistentQuery myDisplayedItemsQuery = null;

   public FKReferenceList getMyDisplayedItems(){
	   if(resourceKey == 0)return FKReferenceList.EMPTY_FKREFERENCELIST;
	   if(myDisplayedItemsQuery == null)
		   myDisplayedItemsQuery = Rdb2javaPlugin.getDataServer().makePersistentQuery(new LinkableObjectQuery(), "displayedInKey");
	   if(myDisplayedItems == null)myDisplayedItems = new FKReferenceList(myDisplayedItemsQuery, resourceKey);
	   return myDisplayedItems;
   }
   public void signalChangeMyDisplayedItems(LinkableObject oldItem, LinkableObject newItem){
	   firePropertyChange(MYDISPLAYEDITEMS_PROP,oldItem,newItem);
   }


// code for handling foreign key references to this entity/class

   public void assignMyReferencesTo(int to){
       doFkAssignment("LinkableObject","displayedInKey",to);
       doFkAssignment("LinkableObject","surrogateForKey",to);
       doFkAssignment("Favourite","favouriteResource",to);
       doFkAssignment("LOType","sourceRoleKey",to);
       doFkAssignment("LOType","targetRoleKey",to);
   }
   
   public void assignMyReferencesTo(Resource assignee){
       assignMyReferencesTo(assignee.getResourceKey());
   }


// code for extra variables and methods

//EM{resource-currentPage
   public int getCurrentPage(){
	   return 0;
   }
   
   public void setCurrentPage(int currentPage){
	   
   }
   
   public FKReferenceList getMyPagedDisplayedItems(){
	   return getMyDisplayedItems();
   }
//EM}

//EM{resource-editorinput
   public static String EditorInput2idString(IEditorInput editorInput){
	   if(editorInput instanceof IFileEditorInput){
		   IFile ifile = ((IFileEditorInput)editorInput).getFile();
		   return "file:"+ifile.getFullPath().toString();
	   }
	   if(editorInput instanceof BrowserEditorInput){
		   URL theUrl = ((BrowserEditorInput)editorInput).getUrl();
		   if(theUrl != null)
			   return "url:"+theUrl.toExternalForm();
	   }
	   
	   // code copies from org.eclipse.ui.part.EditorInputTransfer#writeEditorInput
	   
	   IPersistableElement element = editorInput.getPersistable();
	   if(element == null)return null;
	   XMLMemento memento = XMLMemento.createWriteRoot("IEditorInput");
	   element.saveState(memento);
	   memento.putString("factoryID",element.getFactoryId());
       //convert memento to String
       StringWriter writer = new StringWriter();
       try {
		  memento.save(writer);
	      writer.close();
	   } catch (IOException e) {
		  // TODO Auto-generated catch block
		  e.printStackTrace();
		  return null;
	   }
	   String abbrevMem = writer.toString();
	   int pos = abbrevMem.indexOf("<IEditorInput ")+14;
	   //abbrevMem = abbrevMem.replaceFirst("$[.\\r\\n]*?\\<IEditorInput ", "");
	   abbrevMem = abbrevMem.substring(pos);
	   return "memento:"+abbrevMem;
   }
   
   public static IEditorInput idString2EditorInput(String id){
	   int pos =id.indexOf(':');
	   if(pos < 0)return null;
	   String type = id.substring(0,pos);
	   String value = id.substring(pos+1);
	   if(type.equals("file")){
		   IPath path = new Path(value);
		   IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		   IFile theFile = myWorkspaceRoot.getFile(path);
		   //IFile theFile = myWorkspaceRoot.getFileForLocation(path);
		   return new FileEditorInput(theFile);
	   } else if(type.equals("url")){
		   URL theURL = null;
		   try {
			  theURL = new URL(value);
		   } catch (MalformedURLException e) {
			  e.printStackTrace();
		   }
		   return new BrowserEditorInput(theURL);
	   } else if(type.equals("note")){
		   int key = Integer.parseInt(value);
		   //Note theNote = Note.getNoteItem(key);
		   Resource theResource = Resource.getItem(key);
		   if(theResource == null)return null;
		   if(!(theResource instanceof Note))return null;
		   return new NoteEditorInput((Note)theResource);
	   } else if(type.equals("resource")){
		   int key = Integer.parseInt(value);
		   Resource theResource = Resource.getItem(key);
		   if(theResource == null)return null;
		   return new ResourceEditorInput(theResource);
	   }
	   
	   // code copies from org.eclipse.ui.part.EditorInputTransfer#readEditorInput
	   // code to locate factory comes from org.eclipse.ui.internal.EditorReference#getRestoredInput

	   StringReader stringReader = new StringReader("<IEditorInput "+value);
	   XMLMemento memento;
	try {
		memento = XMLMemento.createReadRoot(stringReader);
	} catch (WorkbenchException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return null;
	}
	   if(memento == null)return null;
	   
       String factoryID = null;
       if (memento != null) {
           factoryID = memento
                   .getString("factoryID" /*IWorkbenchConstants.TAG_FACTORY_ID */);
       }
       if (factoryID == null)return null;

	   
       IElementFactory factory = PlatformUI.getWorkbench().getElementFactory(
               factoryID);
       if (factory != null) {
           IAdaptable adaptable = factory.createElement(memento);
           if (adaptable != null && (adaptable instanceof IEditorInput)) {
               return  (IEditorInput) adaptable;
           }
       }
       return null;
   }
//EM}

//EM{resource-find
   public void setIdentifiers(IEditorInput editorInput){
	   setIdentifiers(EditorInput2idString(editorInput));
   }

   public void setIdentifiers(String id){
	   setIdentifier(id);
	   String idStart = id;
	   if(idStart.length() > 100)idStart = (new String(id)).substring(0,100);
	   this.setIdStart(idStart);
   }
   
   public static Resource find(ObjectType obj, String idString){
	   ResourceQuery q = new ResourceQuery();
	   //q.setWhereString("objectTypeKey="+obj.getObjectTypeKey()+
		//	   " and idStart=?");
	   String idInit = idString;
	   if(idInit.length() > 100)idInit = (new String(idString)).substring(0,100);
	   //q.addQueryParam(idInit);
	   q.addConstraint("objectTypeKey", BaseQuery.FilterEQUAL, obj.getObjectTypeKey());
	   q.addConstraint("idStart", BaseQuery.FilterEQUAL, idInit);
	   Vector rslt = q.executeQuery();
	   if(rslt.size() == 0)return null;
	   for(int i = 0; i < rslt.size(); i++){
		   Resource trial = (Resource)rslt.get(i);
		   if(trial.getIdentifier().equals(idString))return trial;
	   }
	   return null;
   }

   public static Resource find(ObjectType obj, IEditorInput input){
	   return find(obj, EditorInput2idString(input));
   }
   
   public void openEditor(IWorkbenchPage page, int pageNo) throws PartInitException{
	   if(getObjectType() == null)return;
	   String editorId = getObjectType().getEditorId();
	   if((editorId == null) || (editorId.trim().equals("")))return ;
	   String ident = this.getIdentifier();
	   if((ident == null) || (ident.equals("")))ident = "resource:"+this.getALID();
	   IEditorInput input = idString2EditorInput(ident);
	   if(input instanceof IInputCanContainResource){
		   ((IInputCanContainResource)input).setMyResource(this);
	   }
	   if(input instanceof ResourceEditorInput){
		   ((ResourceEditorInput)input).setPageNo(pageNo);
	   }
	   if(input == null)return;
	   IEditorPart part = page.openEditor(input, editorId.trim());
	   if((part instanceof IPageSettableEditorPart) && pageNo > 0){
		   ((IPageSettableEditorPart)part).turnToPage(pageNo);
	   }
   }
   
   public void openEditor(IWorkbenchPage page) throws  PartInitException{
	   openEditor(page, 0);
   }
   
   public void closeMyEditor(IWorkbenchPage page, boolean askSave){
	    IEditorInput input = Resource.idString2EditorInput(getIdentifier());
	    IEditorPart editor = page.findEditor(input);
	    if(editor == null)return;
	    page.closeEditor(editor, askSave);
   }
//EM}

//EM{resource-getUrl
   public String getUrl(){
	   String identifier = getIdentifier();
	   if(identifier == null || !identifier.startsWith("url:"))return null;
	   return identifier.substring(4);
   }
//EM}

//EM{resource-isDeletable
   public boolean isDeletable(){
		/* This resource is deletable if (a) it has no other
		 * surrogates that points to it, (b) there is no editor currently
		 * open that refers to it, (c) it has no objects that it "contains"
		 * itself, (d) it is not among the favourites, and
		 * (e) it is not used as a Role in LOTypes */
		if(getMySurrogates().getCount() != 0) return false;
		Set openIds = PlinyPlugin.getDefault().getOpenNotesIds();
		if(openIds.contains(new Integer(getALID()))) return false;
		if(getMyDisplayedItems().getCount() != 0)return false;
		if(isFavourite())return false;
		if(getMySourceRoles().getCount() != 0)return false;
		if(this.getMyTargetRoles().getCount() != 0)return false;
		return true;
   }
//EM}

//EM{resource-isFavourite
   public boolean isFavourite(){
	   return getFavourites().getCount() > 0;
   }
//EM}

//EM{resource-name
   public static final String NAME_PROP = "Resource.name";
   
   public String getName(){
	   return getFullName();
   }
   
   public static String getInitChar(String input){
	   if(input == null)return " ";
	   input = input.replaceAll("[^0-9\\p{L}]","");
	   if(input.length() == 0)return " ";
	   return input.substring(0,1).toUpperCase(Locale.getDefault()); //Locale.getDefault()
   }
   
   public void setName(String name){
	   String oldName = fullName;
	   if(name == null || name.equals("")){
		   initChar = " ";
		   fullName = "";
	   } else {
	      fullName = name.trim();
	      if(fullName.equals(""))initChar = " ";
	      else 
	    	  initChar = getInitChar(fullName);
	   }
	   updateDBName(fullName, initChar);
	   firePropertyChange(NAME_PROP, oldName, this);
	   ResourceNameChangeAnnouncmentService.getService().announceNameChange(this, oldName);
   }
	   
	 protected void updateDBName(String fullName, String init){
		  if(getALID() <= 0)return; // zero means that object is not currently in the DB.
		  Rdb2javaPlugin.getDataServer().doResourceFullnameAndInitUpdate(fullName, init, getALID());
	 }
//EM}

//EM{resource-objectType
   public int getObjectTypeKey(){
	   return objectTypeKey;
   }
   
   public boolean canDisplayMap(){
	   return false;
   }
//EM}
   
//EM{resource-cutcopytext
   public String getCutCopyText(){
	   return this.getFullName();
   }
//EM}
   
//EM{resource-makeNewDisplayedNote
   public NoteLucened makeNewDisplayedNote(){
	   int count = this.getMyDisplayedItems().getCount();
	   NoteLucened rslt = new NoteLucened(true);
	   rslt.setName(this.getName()+": Note "+(count+1));
	   rslt.reIntroduceMe();
	   return rslt;
   }
   
   public void introduceNewDisplayNote(NoteLucened theNote){
	   if(theNote.getName().trim().length() == 0){
	      int count = this.getMyDisplayedItems().getCount();
	      theNote.setName(this.getName()+": Note "+(count+1));
	   }
	   theNote.reIntroduceMe();
	   
   }
//EM}
   
//EM{resource-getResourceFile
   public File getResourceFile(){
	   return null;
   }
//EM}

// code for toString

   private String displayKey(){
      return getFullName()+"/ "+getIdentifier();
   }

   public String toString(){
      return "Resource: "+displayKey()+"("+getResourceKey()+")";
   }
   
   public String getALItem() {
       if(getResourceKey() <= 1) return "";
       return displayKey();
   }
   
   public int getALID() {
       return getResourceKey();
   }
   
   protected void setALID(int val){resourceKey = val;}
}
