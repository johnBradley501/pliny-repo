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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.swt.graphics.Image;

import uk.ac.kcl.cch.rdb2java.dynData.BaseObject;

/**
 * provides an abstract base class that provides functionality shared by many of
 * the classes that make up the Resource Explorer's data model.
 * <p>Implementors can provide a tracking event that, when it occurs
 * triggers this item's <code>#propertyChange</code> that will both
 * request a recreation of this item's children, and then will ask the
 * associated TreeViewer to redisplay.
 * 
 * @author John Bradley
 *
 */
abstract public class ResourceExplorerItemBase 
implements IResourceExplorerItem, PropertyChangeListener{
	
	protected Vector myChildren;
	protected IResourceTreeDisplayer myView;
	private String trackingProperty;
	BaseObject baseObject;

	public ResourceExplorerItemBase(IResourceTreeDisplayer myView, BaseObject obj, String trackingProperty) {
		super();
		this.myView = myView;
		this.myChildren = null;
		this.trackingProperty = trackingProperty;
		this.baseObject = obj;
		if(obj != null)
		   obj.addPropertyChangeListener(this);
	}
	
	protected BaseObject getBaseObject(){
		return baseObject;
	}
	
	private void removeMyChildren(){
		if(myChildren == null)return;
		Iterator it = myChildren.iterator();
		while(it.hasNext())
			((IResourceExplorerItem)it.next()).dispose();
		myChildren = null;
	}
	
	protected void setBaseObject(BaseObject obj){
		if(baseObject != null)baseObject.removePropertyChangeListener(this);
		baseObject = obj;
		baseObject.addPropertyChangeListener(this);
		removeMyChildren();
	}
	
	public IResourceTreeDisplayer getMyView(){
		return myView;
	}
	
	public void dispose(){
		if(baseObject != null)
		   baseObject.removePropertyChangeListener(this);
		removeMyChildren();
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		String pName = arg0.getPropertyName();
		if(pName.equals(trackingProperty)){
			updateMyChildren();
			myView.getMyViewer().refresh(this);
		}
	}
	
	private Object getId(Object item){
		if(item == null)
			throw new RuntimeException("ResourceExplorerItemBase: updateMyChildren has null object");
		if(item instanceof BaseObject)return new Integer(((BaseObject)item).getALID());
		if(item instanceof ResourceNameInitialLetter)
			return ((ResourceNameInitialLetter)item).getLetter();
		if(item instanceof String)return item;
		throw new RuntimeException("ResourceExplorerItemBase: updateMyChildren"+
				" unexpected object found:"+item);
	}

	protected void updateMyChildren() {
		Hashtable oldChildren = new Hashtable();
		Iterator it = getChildrenList().iterator();
		while(it.hasNext()){
			IResourceExplorerItem item = (IResourceExplorerItem)it.next();
			Object obj = item.getAssociatedObject();
			oldChildren.put(getId(obj), item);
		}
		it = createListIterator();
		myChildren = new Vector();
		while(it.hasNext()){
			Object obj = it.next();
			Object key = getId(obj);
			if(oldChildren.containsKey(key)){
				myChildren.add(oldChildren.get(key));
				oldChildren.remove(key);
			} else {
				Object child = makeChild(obj);
				if(child != null)
				  myChildren.add(child);
			}
		}
		it = oldChildren.values().iterator();
		while(it.hasNext()){
			IResourceExplorerItem item = (IResourceExplorerItem)it.next();
			item.dispose();
		}
	}

	public Object getAssociatedObject() {
		return baseObject;
	}

	public abstract String getText();
	public abstract void setText(String name);
	public abstract boolean canModify();
	public abstract Image getIcon();
	public abstract IResourceExplorerItem getParent();
	
	protected List getChildrenList(){
		if(myChildren == null){
			Iterator it = createListIterator();
			myChildren = new Vector();
			while(it.hasNext()){
				Object rslt = makeChild(it.next());
				if(rslt != null)
				   myChildren.add(rslt);
			}
		}
		return myChildren;
	}
	
    public int getNumberChildren(){
    	return getChildrenList().size();
    }

	public boolean hasChildren() {
		return getNumberChildren() != 0;
	}

	public List getChildren() {
		return getChildrenList();
	}
	
	public abstract Iterator createListIterator();

	public abstract IResourceExplorerItem makeChild(Object item);

    public int getPageNumber(){
    	return 0;
    }

}
