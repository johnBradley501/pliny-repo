/*******************************************************************************
 * Copyright (c) 2009 John Bradley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Bradley - initial API and implementation
 *******************************************************************************/
package uk.ac.kcl.cch.jb.pliny.views.utils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.ResourceQuery;
import uk.ac.kcl.cch.rdb2java.dynData.BaseQuery;

/**
 * the root object for the Resource Explorer's title search oriented tree display. This
 * item is, thus, like other tree model data displayed by the Resource Explorer, a
 * {@link uk.ac.kcl.cch.jb.pliny.views.utils.IResourceExplorerItem IResourceExplorerItem} 
 * although unlike the other tabs, this one is dynamic and includes Resources based on the
 * user's search string.
 * <p>The search searches {@link uk.ac.kcl.cch.jb.pliny.model.Resource Resource}s, so the
 * children of this root object are always a list of chosen resources.
 * 
 * @author John Bradley
 *
 */
public class ResourceExplorerSelectedListManager implements
		IResourceExplorerItem {
	
	private ResourceListProvider listProvider = null;
	private IResourceTreeDisplayer myView;
	Vector theList = null;

	public ResourceExplorerSelectedListManager(IResourceTreeDisplayer myView){
		this(myView,null);
	}
	
	public ResourceExplorerSelectedListManager(IResourceTreeDisplayer myView, String startingString){
		this.myView = myView;
		listProvider = new ResourceListProvider(startingString);
	}

	private class ResourceListProvider {
		private String string;
		private List theList = null;
		
		public ResourceListProvider(String string){
			setString(string);
		}
		
		public void setString(String string){
			if(string == null)this.string = null;
			else this.string = string.trim();
			theList = null;
		}
		
		public String getString(){return this.string;}
		
		public List getList(){
			if(theList != null)return theList;
			if(string == null || string.length()<2)return Collections.EMPTY_LIST;
			ResourceQuery q = new ResourceQuery();
			//String stringLower = string.toLowerCase();
			//q.setWhereString("LOWER(Resource.fullName) like '"+stringLower+"%'");
			q.addConstraint("fullName", BaseQuery.FilterSTARTS, string);
			//q.setOrderString("LOWER(fullName)");
			q.addOrder("fullname", BaseQuery.OrderASC_LOWER);
			theList = q.executeQuery();
			return theList;
		}
		
		public void refresh(){
			theList = null;
			getList();
		}
		
		public void updateList(List newList){
			theList = newList;
		}
	}

	public boolean canModify() {
		return false;
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public Object getAssociatedObject() {
		return null;
	}

	public List getChildren() {
		if(theList == null){
		List qr = listProvider.getList();
			myView.getMyViewer().getTree().setEnabled(qr.size() > 0);
			theList = new Vector();
			Iterator it = qr.iterator();
			while(it.hasNext()){
				Resource r = (Resource)it.next();
				theList.add(new ResourceExplorerResourceItem(myView, ResourceExplorerSelectedListManager.this, r));
			}
		}
		// TODO Auto-generated method stub
		return theList;
	}

	public Image getIcon() {
		return null;
	}

	public int getNumberChildren() {
		// TODO Auto-generated method stub
		return getChildren().size();
	}

	public int getPageNumber() {
		return 0;
	}

	public IResourceExplorerItem getParent() {
		// this is a root, and has no parent   J.B.
		return null;
	}

	public String getText() {
		return "root";
	}

	public boolean hasChildren() {
		// TODO Auto-generated method stub
		return getChildren().size() > 0;
	}

	public void setText(String name) {
		// do nothing here    j.b.
	}

	public void setSearchString(String string) {
		theList = null;
		listProvider.setString(string);
		myView.getMyViewer().refresh();
	}
	
	public String getSearchString(){
		return listProvider.getString();
	}
	
	public void refresh(){
		theList = null;
		listProvider.refresh();
	}

	
	TreeMap orderer = null;

	public void removeResource(Resource r) {
		Iterator it = theList.iterator();
		while(it.hasNext()){
			ResourceExplorerResourceItem item = (ResourceExplorerResourceItem)it.next();
			if(item.getResource().getALID() == r.getALID()){
				//theList.remove(item);
				it.remove(); // avoids java.util.ConcurrentModificationException exception: see http://www.noppanit.com/how-to-deal-with-java-util-concurrentmodificationexception-with-arraylist/
				myView.getMyViewer().refresh();
			}
		}
	}
	
	private boolean listContainsResource(Resource r){
		Iterator it = theList.iterator();
		while(it.hasNext()){
			ResourceExplorerResourceItem item = (ResourceExplorerResourceItem)it.next();
			if(item.getResource().getALID() == r.getALID())return true;
		}
		return false;
	}

	public void addResource(Resource r) {
		if(listContainsResource(r))return;
		ResourceExplorerResourceItem newItem = new ResourceExplorerResourceItem(myView, this, r);
		// if(listProvider.getList().contains(r))return;
		buildOrderer();
		orderer.put(r.getFullName().toLowerCase(),newItem);
		rebuildList();
		myView.getMyViewer().refresh();
	}

	private void buildOrderer() {
		orderer = new TreeMap();
		Iterator it = theList.iterator();
		while(it.hasNext()){
			ResourceExplorerResourceItem item = (ResourceExplorerResourceItem)it.next();
			Resource r = item.getResource();
			orderer.put(r.getFullName().toLowerCase(), item);
		}
	}

	private void rebuildList() {
		Vector result = new Vector();
		result.addAll(orderer.values());
		listProvider.updateList(result);
		theList = result;
		myView.getMyViewer().refresh();
	}
}
