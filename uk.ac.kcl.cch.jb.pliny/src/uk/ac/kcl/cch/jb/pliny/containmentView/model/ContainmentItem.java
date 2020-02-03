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

package uk.ac.kcl.cch.jb.pliny.containmentView.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.rdb2java.dynData.PropertyChangeObject;

/**
 * this model class for the ContainmentView corresponds to the boxes
 * in the graph that, in turn, correspond to the 
 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource Resource}s that are
 * connected to the main resource the display is centered around.
 * All containmentItems are owned by a {@link ContainmentSet} 
 * which manages the set of items that are displayed
 * by the Containment View that spread out from the central starting Resource.
 * <p>
 * A ContainmentItem also manages its Links to other ContainmentItems
 * that present the containment information.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.containmentView.ContainmentView
 * 
 * @author John Bradley
 */
public class ContainmentItem extends PropertyChangeObject
implements PropertyChangeListener{
	
    private Resource myResource;
    private Set fromList = new HashSet();
    private Set toList = new HashSet();
    private ContainmentSet containmentSet;
    //private boolean isInteriorItem = false;
    private boolean showingAllParents = false, showingAllChildren = false;
	private IncludedTypeManager typeManager = null;
    
    private boolean doIt = true;
    
    /**
     * constructs an instance of a ContainmentItem for
     * <code>myResource</code>, which is an item which needs to appear
     * within the current <code>containmentSet</code>.
     * 
     * @param myResource Resource which corresponds to this Item.
     * @param containmentSet holder for this item.
     * @param typeManager the current IncludedTypeManager
     */
	public ContainmentItem(Resource myResource, ContainmentSet containmentSet,
			IncludedTypeManager typeManager) {
		this.myResource = myResource;
		this.typeManager = typeManager;
		this.containmentSet = containmentSet;
		if(myResource != null)
		   myResource.addPropertyChangeListener(this);
	}
	
	private void disposeLinks(Set set){
		Iterator it = set.iterator();
		while(it.hasNext()){
			ContainmentLink link =(ContainmentLink)it.next();
			link.dispose();
		}
	}
	
	public void dispose(){
		myResource.removePropertyChangeListener(this);
		disposeLinks(fromList);
		disposeLinks(toList);
	}
	
	/**
	 * reports where this Item is currently also displaying its
	 * Parent items and links to them.
	 * 
	 * @return <code>true</code> if showing parent items.
	 */
	
	public boolean getShowingAllParents(){
		return showingAllParents;
	}
	
	
	/**
	 * reports where this Item is currently also displaying its
	 * Child items and links to them.
	 * 
	 * @return <code>true</code> if showing child items.
	 */

	public boolean getShowingAllChildren(){
		return showingAllChildren;
	}
	
	/**
	 * invoked to generate the current list of child items
	 * for this current item.
	 *
	 */
	
	public void buildChildList(){
		showingAllChildren = true;
		containmentSet.recordChildExpand(myResource);

		
		Iterator it = myResource.getMyDisplayedItems().iterator();
		while(it.hasNext()){
			LinkableObject lo = (LinkableObject)it.next();
			Resource res = lo.getSurrogateFor();
			//if(res != null && (lo.getLoType().getALID() != LOType.getBibRefType().getALID())){
			if(res != null){
				if(typeManager.isIncluded(lo.getLoType())){
					ContainmentItem item = containmentSet.getItem(res);
					ContainmentLink theLink = containmentSet.getLink(this, item);
					theLink.setLinkableObject(lo);
					if(!fromList.contains(theLink)){
					   item.fromList.add(theLink);
					   item.firePropertyChange(Resource.MYSURROGATES_PROP);
					   toList.add(theLink);
					}
					if(containmentSet.isParentExpanded(res) && !item.getShowingAllParents())
						item.buildParentList();
					if(containmentSet.isChildExpanded(res) && !item.getShowingAllChildren())
						item.buildChildList();
				} else containmentSet.addUndisplayedLinkableObject(lo);
			}
		}
		/*
		ResourceQuery q = new ResourceQuery();
		q.setFromString("LinkableObject");
		q.setWhereString("LinkableObject.surrogateForKey=Resource.resourceKey "+
				"and LinkableObject.displayedInKey="+myResource.getALID());
		Vector containedResources = q.executeQuery();
		Iterator it2 = containedResources.iterator();
		while(it.hasNext()){
			Resource res = (Resource)it.next();
			ContainmentItem item = containmentSet.getItem(res);
			ContainmentLink theLink = containmentSet.getLink(this, item);
			if(!fromList.contains(theLink)){
			   item.fromList.add(theLink);
			   item.firePropertyChange(Resource.MYSURROGATES_PROP);
			   toList.add(theLink);
			}
		}
		*/
        firePropertyChange(Resource.MYDISPLAYEDITEMS_PROP);
 	}
	
	
	/**
	 * invoked to generate the current list of parent items
	 * for this current item.
	 *
	 */
	
	public void buildParentList(){
		showingAllParents = true;
		containmentSet.recordParentExpand(myResource);
		
		Iterator it = myResource.getMySurrogates().iterator();
		while(it.hasNext()){
			LinkableObject lo = (LinkableObject)it.next();
			if(lo.getSurrogateFor() != null){
				Resource res = lo.getDisplayedIn();
				if(res == null){
					// JB shouldn't happen!
					// JB add logging here.
				} else {
					if(typeManager.isIncluded(lo.getLoType())){
						ContainmentItem item = containmentSet.getItem(res);
						ContainmentLink theLink = containmentSet.getLink(item,this);
						theLink.setLinkableObject(lo);
						item.toList.add(theLink);
						item.firePropertyChange(Resource.MYDISPLAYEDITEMS_PROP);
						this.fromList.add(theLink);
						if(containmentSet.isParentExpanded(res) && !item.getShowingAllParents())
							item.buildParentList();
						if(containmentSet.isChildExpanded(res) && !item.getShowingAllChildren())
							item.buildChildList();
					}
					else containmentSet.addUndisplayedLinkableObject(lo);
				}

			}
		}
        /*
		ResourceQuery q = new ResourceQuery();
		q.setFromString("LinkableObject");
		q.setWhereString("LinkableObject.displayedInKey=Resource.resourceKey "+
				"and LinkableObject.surrogateForKey="+myResource.getALID());
		Vector containerResources = q.executeQuery();
		Iterator it = containerResources.iterator();
		while(it.hasNext()){
			Resource res = (Resource)it.next();
			ContainmentItem item = containmentSet.getItem(res);
			ContainmentLink theLink = containmentSet.getLink(item,this);
			item.toList.add(theLink);
			item.firePropertyChange(Resource.MYDISPLAYEDITEMS_PROP);
			this.fromList.add(theLink);
		}
		*/
		firePropertyChange(Resource.MYSURROGATES_PROP);
	}

	/**
	 * convenience method to build both child and parent items
	 *
	 */
	
	public void buildLinks(){
		buildParentList();
		buildChildList();
		//System.out.println("buildLinks "+printSets());
	}
	
	public Resource getResource(){
		return myResource;
	}
	
	/**
	 * returns a list of 
	 * {@link uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentLink}
	 * items that link from some other ContainmentItem to this one.
	 * 
	 * @return Set containing link items from other Items to this one.
	 */
	
	public Set getFromSet(){
		return fromList;
	}
	
	/**
	 * returns a list of 
	 * {@link uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentLink}
	 * items that link from this item to some other ContainmentItems.
	 * 
	 * @return Set containing link items from this item to other Items.
	 */
	
	public Set getToSet(){
		return toList;
	}
	
	public String printSets(){
		StringBuffer buf = new StringBuffer();
		buf.append(toString()+"\n");
		buf.append("... toSet: "+getToSet()+"\n");
		buf.append("... fromSet: "+getFromSet());
		return new String(buf);
	}

	/**
	 * this object monitors changes in its Resource, and when one
	 * happens of interest to it deals with it. The code here tracks
	 * the Resource's name, but also tracks changes in LinkableObjects that
	 * are added either with this item's Resource as the surrogate, or
	 * are displayed within its reference/annotation area, and adds
	 * or removes corresponding parent or child ContainmentItems and their links.
	 * 
	 * @param arg0 for the Resource it is linked to.
	 */
	public void propertyChange(PropertyChangeEvent arg0) {
		String propName = arg0.getPropertyName();
		if(propName == Resource.NAME_PROP){
			String oldName = (String)arg0.getOldValue();
			String newName = myResource.getName();
			if(!newName.equals(oldName))
			   this.firePropertyChange(Resource.NAME_PROP);
		} else if(propName == Resource.MYSURROGATES_PROP){
			if(arg0.getOldValue() != null)removeFromItem((LinkableObject)arg0.getOldValue());
			else if(showingAllParents)addFromItem((LinkableObject)arg0.getNewValue());
			//System.out.println(propName+" "+printSets());
		}
	    else if(propName == Resource.MYDISPLAYEDITEMS_PROP){
			//System.out.println(propName+" (before) "+printSets());

			if(arg0.getOldValue() != null)removeToItem((LinkableObject)arg0.getOldValue());
			else if(showingAllChildren)addToItem((LinkableObject)arg0.getNewValue());
			//System.out.println(propName+" (after) "+printSets());
		}
		
	}

	private boolean deleteItemIfNecessary(ContainmentItem item){
		if(item.fromList.size() != 0 || item.toList.size()!=0) return false;
		containmentSet.removeItem(item);
        return true;
	}
	
	private void removeFromItem(LinkableObject object) {
		if(!typeManager.isIncluded(object.getLoType()))return;
		Resource otherResource = object.getDisplayedIn();
		if(otherResource == null)return;
		ContainmentItem otherItem = containmentSet.getItem(otherResource);
		ContainmentLink link = containmentSet.getLink(otherItem, this);
		link.removeLinkableObject(object);
		if(link.getBothWay()){
			link.setBothWay(false);
			return;
		}
		int oldSize = fromList.size();
		fromList.remove(link);
		if(oldSize == fromList.size()){
			deleteItemIfNecessary(otherItem);
			return;
		}
		otherItem.toList.remove(link);
		if(!deleteItemIfNecessary(otherItem))
				otherItem.firePropertyChange(Resource.MYDISPLAYEDITEMS_PROP);
		if(!deleteItemIfNecessary(this))
		        firePropertyChange(Resource.MYSURROGATES_PROP);
	}
	
	private void removeToItem(LinkableObject object) {
		if(!typeManager.isIncluded(object.getLoType()))return;
		Resource otherResource = object.getSurrogateFor();
		if(otherResource == null)return;
		ContainmentItem otherItem = containmentSet.getItem(otherResource);
		ContainmentLink link = containmentSet.getLink(this, otherItem);
		link.removeLinkableObject(object);
		if(link.getBothWay()){
			link.setBothWay(false);
			return;
		}
		int oldSize = toList.size();
		toList.remove(link);
		if(oldSize == toList.size()){
			deleteItemIfNecessary(otherItem);
			return;
		}
		otherItem.fromList.remove(link);
		deleteItemIfNecessary(otherItem);
		if(!deleteItemIfNecessary(otherItem))
			otherItem.firePropertyChange(Resource.MYSURROGATES_PROP);
	    if(!deleteItemIfNecessary(this))
	        firePropertyChange(Resource.MYDISPLAYEDITEMS_PROP);
	}
	
	private void addFromItem(LinkableObject object) {
		if(object.getDisplayedIn() == null)return;
		ContainmentItem otherItem = containmentSet.getItem(object.getDisplayedIn());
		ContainmentLink link = containmentSet.getLink(otherItem, this);
		link.setLinkableObject(object);
		if(toList.contains(link))return;
		int oldSize = fromList.size();
		fromList.add(link);
		if(oldSize == fromList.size()) return;
		otherItem.toList.add(link);
		this.firePropertyChange(Resource.MYSURROGATES_PROP);
		otherItem.firePropertyChange(Resource.MYDISPLAYEDITEMS_PROP);
	}
	
	private void addToItem(LinkableObject object) {
		if(object.getSurrogateFor() == null)return;
		ContainmentItem otherItem = containmentSet.getItem(object.getSurrogateFor());
		ContainmentLink link = containmentSet.getLink(this, otherItem);
		link.setLinkableObject(object);
		if(fromList.contains(link))return;
		int oldSize = toList.size();
		if(doIt) toList.add(link);
		if(oldSize == toList.size()) return;
		otherItem.fromList.add(link);
		this.firePropertyChange(Resource.MYDISPLAYEDITEMS_PROP);
		otherItem.firePropertyChange(Resource.MYSURROGATES_PROP);
	}
	
	public String toString(){
		return "Item: ["+myResource+"]";
	}
}
