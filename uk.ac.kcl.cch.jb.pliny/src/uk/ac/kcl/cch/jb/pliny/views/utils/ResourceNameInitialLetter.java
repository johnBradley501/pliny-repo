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
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import uk.ac.kcl.cch.jb.pliny.model.ObjectType;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.ResourceQuery;
import uk.ac.kcl.cch.rdb2java.dynData.BaseQuery;
import uk.ac.kcl.cch.rdb2java.dynData.PropertyChangeObject;

/**
 * this class is part of the Resource Explorer data model and keeps track
 * of Pliny 
 * {@link uk.ac.kcl.cch.jb.pliny.model.ResourceQuery Resource}s of a certain 
 * {@link uk.ac.kcl.cch.jb.pliny.model.ObjectType ObjectType} with a specified intial letter.
 * It works with a {@link ResourceNameManager} which keeps track of the
 * Resources and notifies an instance of this class when a change in its
 * Resources have happened.
 * <p>
 * This object implements a 'lazy' management model -- only loading its
 * Resources when it is actually opened by the user.  Once opened, it
 * tracks changes in the names of the Resources it manages and notifies
 * its ResourceNameManager whenever a number change happens.
 * 
 * @see ResourceNameManager
 * 
 * @author John Bradley
 */
public class ResourceNameInitialLetter extends PropertyChangeObject
implements PropertyChangeListener {
	private Character letter;
	private SortedMap resources;
	private int count;
	private ResourceNameManager manager;
	private ObjectType myType;

	public ResourceNameInitialLetter(Character c, ResourceNameManager manager, ObjectType myType) {
		letter = c;
		resources = null;
		this.manager = manager;
		this.myType = myType;
		count = 0;
	}

	public ResourceNameInitialLetter(String c, ResourceNameManager manager, ObjectType myType) {
		letter = new Character(c.charAt(0));
		resources = null;
		this.manager = manager;
		this.myType = myType;
		count = 0;
	}
	
	public void setCount(int count){
		this.count = count;
	}
	
	public int getCount(){
		return count;
	}
	
	public boolean resourcesLoaded(){
		return resources != null;
	}
	
	public String getLetter(){
		return letter.toString();
	}
	
	public String getText(){
		if(letter.toString().trim().equals(""))return "[No Name] ("+count+")";
		return letter+" ("+count+")";
	}
	
	public Iterator getChildrenIterator(){
		if(resources == null)loadResources();
		return resources.values().iterator();
	}
	
	private String buildKey(String name, int id){
		return name.replaceAll("[^\\w ]","")+"/"+id;
	}
	
	private String buildKey(String name, Resource resource){
		return buildKey(name, resource.getSavedID());
	}
	
	private String buildKey(Resource resource){
		// see key construction in "moveResource" below.
		return buildKey(resource.getFullName(), resource.getSavedID());
	}

	private void loadResources() {
		resources = new TreeMap(ResourceNameManager.getMyCollator());

		ResourceQuery q = new ResourceQuery();
		//q.setWhereString("initChar=? and objectTypeKey="+myType.getALID());
		//q.addQueryParam(letter.toString());
		q.addConstraint("initChar", BaseQuery.FilterEQUAL,letter.toString());
		q.addConstraint("objectTypeKey", BaseQuery.FilterEQUAL, myType.getALID());
		Vector rslt = q.executeQuery();
		Iterator it = rslt.iterator();
		while(it.hasNext()){
			Resource resource = (Resource)it.next();
			resource.addPropertyChangeListener(this);
			resources.put(buildKey(resource), resource);
		}
		count = resources.size();
	}
	
	public void removeResource(Resource resource){
		resource.removePropertyChangeListener(this);
		if(resources != null){
		   resources.remove(buildKey(resource));
		   count = resources.size();
		} else {
			count--;
			if(count < 0)count = 0;
		}
	}
	
	public void removeResource(String oldName, Resource resource){
		resource.removePropertyChangeListener(this);
		if(resources != null){
			String oldKey = buildKey(oldName, resource);
			resources.remove(oldKey);
			count = resources.size();
		} else {
			count--;
			if(count < 0)count = 0;
		}
	}
	
	public void addResource(Resource resource){
		resource.addPropertyChangeListener(this);
		if(resources != null){
			resources.put(buildKey(resource),resource);
			count = resources.size();
		} else {
			count++;
		}
	}
	
	public void moveResource(Resource resource, String oldName){
		String oldKey = buildKey(oldName,resource.getSavedID());
		if(resources != null){
		   resources.remove(oldKey);
		   resources.put(buildKey(resource), resource);
		   count = resources.size();
		}
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		String name = arg0.getPropertyName();
        if(name == Resource.NAME_PROP)manager.propertyChange(arg0);		
	}
	
	public void doInitLetterPropertyChangeFire(Object item1, Object item2){
		firePropertyChange(ResourceNameManager.INITLETTER_EVENT, item1, item2);
	}
}
