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
import java.sql.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.ResourceQuery;
import uk.ac.kcl.cch.rdb2java.dynData.BaseQuery;

/**
 * the item in the Resource Explorer's Date-oriented tree display that tracks items
 * that are created today. 
 * 
 *
 * @author John Bradley
 *
 */

public class DatedTodayItem implements IResourceExplorerItem, PropertyChangeListener {
	
	protected IResourceExplorerItem parent;
	protected Date today = null;
	protected Vector items = null;
	protected Map itemMap = new TreeMap();
	private Set listenedItems = new HashSet();
	protected IResourceTreeDisplayer myView = null;
	protected int count = -1;
	
	public DatedTodayItem(IResourceTreeDisplayer myView, IResourceExplorerItem parent, Date today){
		this.parent = parent;
		this.myView = myView;
		this.today = today;
	}
	
	public DatedTodayItem(IResourceTreeDisplayer myView, IResourceExplorerItem parent){
	    this.parent = parent;
	    this.myView = myView;
	    this.today = new Date(System.currentTimeMillis());
	}

	public boolean canModify() {
		return false;
	}

	public void dispose() {
		if(items == null)return;
		Iterator it = items.iterator();
		while(it.hasNext()){
			ResourceExplorerResourceItem item = (ResourceExplorerResourceItem)it.next();
			item.dispose();
		}
		it = listenedItems.iterator();
		while(it.hasNext()){
			Resource r = (Resource)it.next();
			r.removePropertyChangeListener(this);
		}
	}

	public Object getAssociatedObject() {
		return null;
	}

	public List getChildren() {
		if(items == null)buildItems();
		return items;
	}

	public Image getIcon() {
		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
	}

	public int getNumberChildren() {
		if(items == null)buildItems();
		return items.size();
	}

	public int getPageNumber() {
		return 0;
	}

	public IResourceExplorerItem getParent() {
		return parent;
	}

	public String getText() {
		return "Today";
	}

	public boolean hasChildren() {
		//if(items == null)buildItems();
		//return items.size() > 0;
		return getNumberChildren() > 0;
	}

	public void setText(String name) {
		// nothing to do here, text no changeable
	}
	
	protected void buildItems(){
		//buildItems("creationDate >='"+today.toString()+"'");
		buildItems("creationDate",BaseQuery.FilterGREATER_THAN_OR_EQUAL, today);
	}
	
	private Long getTimeKey(Resource resource){
		return new Long(-resource.getCreationTime().getTime());
	}
	
	protected void buildItems(String attrName, String op, Date value){
		items = new Vector();
		ResourceQuery q = new ResourceQuery();
		//q.addQueryParam(today);
		//q.setWhereString(whereString);
		q.addConstraint(attrName, op, value);
		q.addOrder(attrName, BaseQuery.OrderASC);
		q.addOrder("creationTime", BaseQuery.OrderDESC);
		q.addOrder("fullName", BaseQuery.OrderASC);
		//q.setOrderString("Resource.creationTime desc, Resource.fullName");
		Vector rslt = q.executeQuery();
		Iterator it = rslt.iterator();
		while(it.hasNext()){
			Resource resource = (Resource)it.next();
			ResourceExplorerResourceItem item = new ResourceExplorerResourceItem(myView, this, resource);
			items.add(item);
			itemMap.put(getTimeKey(resource), item);
		}
	}

	public void removeResource(Resource resource){
		if(items == null){
			if(count > 0)--count;
			return;
		}
		Long resourceKey = getTimeKey(resource);
		ResourceExplorerResourceItem item = 
			(ResourceExplorerResourceItem)itemMap.get(resourceKey);
		if(item == null)return;
		item.dispose();
		itemMap.remove(resourceKey);
		items.clear();
		items.addAll(itemMap.values());
		count = items.size();
	}

	public void addResource(Resource resource){
		if(items == null){
			if(count >= 0)++count;
			return;
		}
		ResourceExplorerResourceItem item = new ResourceExplorerResourceItem(myView, this, resource);
		itemMap.put(getTimeKey(resource), item);
		items.clear();
		items.addAll(itemMap.values());
		count = items.size();
		if(resource.getObjectType() == null){
			resource.addPropertyChangeListener(this);
			listenedItems.add(resource);
		}
	}
	
	public Date getDate(){
		return today;
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		String msgName = arg0.getPropertyName();
		if(msgName==Resource.OBJECTTYPEKEY_PROP){
			Resource r = (Resource)arg0.getNewValue();
			r.removePropertyChangeListener(this);
			listenedItems.remove(r);
			myView.getMyViewer().refresh(this);
		}
		
	}

}
