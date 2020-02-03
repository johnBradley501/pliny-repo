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

package uk.ac.kcl.cch.jb.pliny.views.utils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Collator;
import java.util.Iterator;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

import uk.ac.kcl.cch.jb.pliny.model.ObjectType;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.rdb2java.Rdb2javaPlugin;
import uk.ac.kcl.cch.rdb2java.dynData.CountItem;
import uk.ac.kcl.cch.rdb2java.dynData.PropertyChangeObject;

/**
 * manages the Resource items associated with
 * a particular
 * {@link uk.ac.kcl.cch.jb.pliny.model.ObjectType ObjectType} by working with their names
 * to handle the grouping of resources by the initial letter of their name
 * and creating the required set of {@link ResourceNameInitialLetter} objects.
 * <p>
 * It then tracks changes in the resources associated with its ObjectType
 * so that new items are added to or removed from the proper {@link ResourceNameInitialLetter}
 * as needed.
 * 
 * @author John Bradley
 *
 */
public class ResourceNameManager extends PropertyChangeObject
implements PropertyChangeListener{
	
	public static final String INITLETTER_EVENT = "ResourceNameManager.InitialLetter";
	public static final String NEW_INITLETTER_EVENT = "ResourceNameManager.NewInitialLetter";
	
	private static Collator myCollator = null;
	private SortedMap initialLetters = null;
	
	public static Collator getMyCollator(){
		if(myCollator != null)return myCollator;
		myCollator = Collator.getInstance(Locale.getDefault());
		//myCollator.setStrength(Collator.PRIMARY);
		myCollator.setStrength(Collator.SECONDARY);
		return myCollator;
	}
	
	private ObjectType myType;
	
	public ResourceNameManager(ObjectType type) {
		super();
		myType = type;
		buildInitialLetters();
		myType.addPropertyChangeListener(this);
	}
/*
	public class ResourceNameGroupManager implements PropertyChangeListener{
		
		Hashtable managers = new Hashtable();
		private Set typeWaiters = new HashSet();
		
		public ResourceNameGroupManager(){
			PlinyPlugin.getDBServicesInstance().addPropertyChangeListener(this);
		}
		
		public void dispose(){
			NoteMan2Plugin.getDBServicesInstance().removePropertyChangeListener(this);
			Iterator it = managers.keySet().iterator();
			while(it.hasNext()){
				ObjectType type = (ObjectType)it.next();
				ResourceNameManager manager = (ResourceNameManager)managers.get(type);
				manager.dispose();
			}
			it = typeWaiters.iterator();
			while(it.hasNext()){
				Resource waiter = (Resource)it.next();
				waiter.removePropertyChangeListener(this);
			}
		}
		
		public void addManager(ResourceNameManager manager){
			ObjectType type = manager.getMyObjectType();
			managers.put(type, manager);
		}
		
		private void passOnEvent(Resource resource, PropertyChangeEvent arg0){
			ObjectType type = resource.getObjectType();
			if(type == null)return;
			ResourceNameManager manager = (ResourceNameManager)managers.get(type);
			if(manager == null)return;
			manager.propertyChange(arg0);
		}

		public void propertyChange(PropertyChangeEvent arg0) {
			String name = arg0.getPropertyName();
			if(name.equals("Create-Resource")){
				Resource newResource = (Resource)arg0.getNewValue();
				if(newResource.getObjectType() == null){
					newResource.addPropertyChangeListener(this);
					typeWaiters.add(newResource);
				} else passOnEvent(newResource, arg0);
			} else if(name.equals("Delete-Resource")){
				Resource oldResource = (Resource)arg0.getOldValue();
				passOnEvent(oldResource, arg0);
			} else if(name == Resource.OBJECTTYPEKEY_PROP){
				Resource theResource = (Resource)arg0.getNewValue();
				theResource.removePropertyChangeListener(this);
				typeWaiters.remove(theResource);
				passOnEvent(theResource, arg0);
			}
		}
	}
*/	
	
	public void dispose(){
		myType.removePropertyChangeListener(this);
		Iterator it1 = initialLetters.values().iterator();
		while(it1.hasNext()){
			ResourceNameInitialLetter obj = (ResourceNameInitialLetter)it1.next();
			if(obj.resourcesLoaded()){
				Iterator it2 = obj.getChildrenIterator();
				while(it2.hasNext()){
					Resource resource = (Resource)it2.next();
					resource.removePropertyChangeListener(this);
				}
			}
		}
	}
	
	public ObjectType getMyObjectType(){
		return myType;
	}

	private void buildInitialLetters() {
		initialLetters = new TreeMap(getMyCollator());
		//Connection conn = PlinyPlugin.getDefault().getConnection();
		//String query = "select initChar, count(resourceKey) from Resource "+
		//               "where objectTypeKey="+myType.getALID()+" group by initChar";
		Iterator<CountItem> it = Rdb2javaPlugin.getDataServer().
		   queryCount("initChar", "Resource", "resourceKey", "objectTypeKey",myType.getALID(),"asc").iterator();
		while(it.hasNext()){
			CountItem data = it.next();
			String initChar = (String)data.getIndex();
			if(initChar.length() == 0)initChar = " ";
			int count = data.getCount();
			ResourceNameInitialLetter newOne = new ResourceNameInitialLetter(initChar, this, myType);
			newOne.setCount(count);
			initialLetters.put(initChar,newOne);
		}
		//try {
		//	Statement stmt = conn.createStatement();
		//	ResultSet rs = stmt.executeQuery(query);
		//	while(rs.next()){
		//		String initChar = rs.getString(1);
		//		if(initChar.length() == 0)initChar = " ";
		//		int count = rs.getInt(2);
		//		ResourceNameInitialLetter newOne = new ResourceNameInitialLetter(initChar, this, myType);
		//		newOne.setCount(count);
		//		initialLetters.put(initChar,newOne);
		//	}
		//	stmt.close();
		//} catch (SQLException e) {
		//	// TODO Auto-generated catch block
		//	e.printStackTrace();
		//	return;
		//} finally {
		//	PlinyPlugin.getDefault().returnConnection(conn);
		//}
	}
	
	public Iterator getInitialLetterIterator(){
		if(initialLetters == null)buildInitialLetters();

		return initialLetters.values().iterator();
	}
	
	public int getNumberInitialLetters(){
		return initialLetters.size();
	}
	
	public void removeInitialLetter(String letter){
		if(initialLetters == null)return;
		initialLetters.remove(letter);
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		String name = arg0.getPropertyName();
		if(name == ObjectType.RESOURCES_PROP)
			if(arg0.getOldValue() != null)
				handleDeleteResource((Resource)arg0.getOldValue());
			else handleNewResource((Resource)arg0.getNewValue());
		else if(name == Resource.NAME_PROP){
			String oldName = (String)arg0.getOldValue();
			Resource theResource = (Resource)arg0.getNewValue();
			handleNameChange(oldName, theResource);
		}
		
	}
	
	private String getInitialCharacter(Resource resource){
		String initCharStr = resource.getInitChar();
		if(initCharStr == null || initCharStr.length() == 0)initCharStr = " ";
		return initCharStr;
		//return new Character(initCharStr.charAt(0));
	}

	private ResourceNameInitialLetter getInitialLetterItem(Resource resource){
		return (ResourceNameInitialLetter)initialLetters.get(getInitialCharacter(resource));
	}

	private void handleDeleteResource(Resource oldResource) {
		ResourceNameInitialLetter item = getInitialLetterItem(oldResource);
		if(item == null)return;
		item.removeResource(oldResource);
		//this.firePropertyChange(INITLETTER_EVENT, null, item);
		item.doInitLetterPropertyChangeFire(null, item);
	}
	
	private ResourceNameInitialLetter getOrMakeItem(Resource resource){
		ResourceNameInitialLetter item = getInitialLetterItem(resource);
		if(item == null){
			String newCharKey = getInitialCharacter(resource);
			item = new ResourceNameInitialLetter(newCharKey, this, myType);
			initialLetters.put(newCharKey, item);
			this.firePropertyChange(NEW_INITLETTER_EVENT, null, item);
		}
		return item;
	}

	private void handleNewResource(Resource newResource) {
		ResourceNameInitialLetter item = getOrMakeItem(newResource);
		item.addResource(newResource);
		//this.firePropertyChange(INITLETTER_EVENT, null, item);
		item.doInitLetterPropertyChangeFire(null, item);
	}

	private void handleNameChange(String oldName, Resource theResource) {
		String oldInit = " ";
		ResourceNameInitialLetter init = getOrMakeItem(theResource);
		if(oldName == null || oldName.length() == 0); // do nothing
		else oldInit = Resource.getInitChar(oldName);
		if(oldInit.equals(getInitialCharacter(theResource))){
			if(init == null)return;
			init.moveResource(theResource, oldName);
			//this.firePropertyChange(INITLETTER_EVENT, null, init);
			init.doInitLetterPropertyChangeFire(null, init);
		} else {
			ResourceNameInitialLetter oldInitItem = (ResourceNameInitialLetter)initialLetters.get(oldInit);
			if(oldInitItem != null){
			   oldInitItem.removeResource(oldName,theResource);
			   //this.firePropertyChange(INITLETTER_EVENT, null, oldInitItem);
			   oldInitItem.doInitLetterPropertyChangeFire(null, oldInitItem);
			}
			init.addResource(theResource);
			//this.firePropertyChange(INITLETTER_EVENT, null, init);
			init.doInitLetterPropertyChangeFire(null, init);
		}
		
	}

}
