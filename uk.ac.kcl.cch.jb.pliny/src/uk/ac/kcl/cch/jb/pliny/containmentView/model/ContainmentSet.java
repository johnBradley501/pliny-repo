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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.rdb2java.dynData.PropertyChangeObject;

/**
 * this model class for the ContainmentView represents the top level
 * object for the a view page.  Each view page is centered on a
 * single {@link uk.ac.kcl.cch.jb.pliny.model.Resource Resource}, 
 * so this object contains that starting Resource
 * and manages the <code>ContainmentItem</code>s and <code>ContainmentLink</code>s 
 * that it needs to generate the display.
 * <p>
 * This class implements {@link uk.ac.kcl.cch.rdb2java.dynData.PropertyChangeObject PropertyChangeObject},
 * and so can so can be listened to.  It raises an event <code>ITEMSCHANGED_EVENT</code>
 * each time an ContainmentItem is added or removed from the set.
 * The UI part of the the ContainmentView subscribes to this service
 * to that it can update itself at the time the user makes a change
 * to the underlying data.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentItem
 * @see uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentLink
 * 
 * @author John Bradley
 */

public class ContainmentSet extends PropertyChangeObject
implements PropertyChangeListener{
	
	public static final String ITEMSCHANGED_EVENT="Containment.ItemsChanged";
	
	private Set centralResources = new HashSet();
	private Set allResources = new HashSet();
	private Map resource2Item = new HashMap();
	private Map linkCache = new HashMap();
	private IncludedTypeManager typeManager = null;
	private Resource startingResource = null;

	private Map expandedResources = new HashMap();
	private Set undisplayedLinkableObjects = new HashSet();

	private class ExpandParameters {
		public boolean showingParent = false;
		public boolean showingChild = false;
	}
	
	/**
	 * this constructor creates an instance of this object that
	 * is meant to be centered on the <code>startingResource</code>.
	 * 
	 * @param startingResource Resource that this ContainmentSet
	 * refers to -- corresponds to the Resource the current ContainmentView page
	 * is for.
	 * @param typeManager
	 */
	
	public ContainmentSet(Resource startingResource, IncludedTypeManager typeManager) {
		this.startingResource = startingResource;
		this.typeManager = typeManager;
		typeManager.addPropertyChangeListener(this);
		if(startingResource == null)return;
		centralResources.add(startingResource);
		allResources.add(startingResource);
		//startingResource.addPropertyChangeListener(this);
		processResource(startingResource);
	}
	
	/**
	 * returns the ContainmentItems that this set is currently holding.
	 * 
	 * @return Vector containing ContainmentItems.
	 */
	public Vector getMyItems(){
		return new Vector(resource2Item.values());
		//return new Vector(resource2Item.elements());
	}
	
	public Map getExpandedResources(){
		return expandedResources;
	}
	
	public Resource getStartingResource(){
		return startingResource;
	}
	
	/**
	 * returns a {@link uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentItem} that corresponds to the given Resource.
	 * If the ContainmentItem does not exist at the time, it is created
	 * and stored so that the same one can be returned the next time the
	 * one for the same resource is requested.
	 * <p> This method is used internally within the ContainmentView's
	 * classes, and it is not expected to need to be called by others.
	 * 
	 * @param r Resource the ContainmentItem is for.
	 * @return the corresponding ContainmentItem.
	 */
	
	public ContainmentItem getItem(Resource r){
		if(resource2Item.containsKey(r))return (ContainmentItem)resource2Item.get(r);
		ContainmentItem rslt = new ContainmentItem(r, this, typeManager);
		resource2Item.put(r,rslt);
		firePropertyChange(ITEMSCHANGED_EVENT, null, rslt);
		return rslt;
	}
	
	public ContainmentLink getLink(ContainmentItem from, ContainmentItem to){
		ContainmentLink rslt;
		String key = from.getResource().getSavedID()+"/"+to.getResource().getSavedID();
		if(linkCache.containsKey(key))return (ContainmentLink)linkCache.get(key);
		String key2 = to.getResource().getSavedID()+"/"+from.getResource().getSavedID();
		if(linkCache.containsKey(key2)){
			rslt = (ContainmentLink)linkCache.get(key2);
			rslt.setBothWay(true);
			return rslt;
		}
		rslt = new ContainmentLink(from,to, this);
		linkCache.put(key, rslt);
		return rslt;
	}
	
	public boolean hasLink(Resource from, Resource to){
		String key = from.getSavedID()+"/"+to.getSavedID();
		return linkCache.containsKey(key);
	}
	
	public void removeLink(ContainmentLink theLink){
		String key = theLink.getFrom().getResource().getSavedID()+"/"+theLink.getTo().getResource().getSavedID();
		linkCache.remove(key);
	}
	
	public void removeItem(ContainmentItem theItem){
		Resource myResource = theItem.getResource();
		theItem.dispose();
		centralResources.remove(myResource);
		allResources.remove(myResource);
		resource2Item.remove(myResource);
		firePropertyChange(ITEMSCHANGED_EVENT, theItem, this);

	}
	
	/**
	 * extends the ContainmentItems in <code>items</code> by adding
	 * either their parents or children or both to the set.
	 * 
	 * @param items Vector containing ContainmentItems
	 * @param showParents boolean if <code>true</code>, add parent items
	 * @param showChildren boolean if <code>true</code>, add children items
	 */
	public void extendItems(Vector items, boolean showParents, boolean showChildren){
		Iterator it = items.iterator();
		while(it.hasNext()){
			ContainmentItem item = (ContainmentItem)it.next();
		    if(showParents && !item.getShowingAllParents())item.buildParentList();
		    if(showChildren && !item.getShowingAllChildren())item.buildChildList();
		}
		firePropertyChange(ITEMSCHANGED_EVENT, null, this);
	}
	
	private void processResource(Resource resource) {
		ContainmentItem mainItem = getItem(resource);
		mainItem.buildLinks();
	}

	public void dispose(){
		//Iterator it = centralResources.iterator();
		//while(it.hasNext()){
		//	Resource resource = (Resource)it.next();
		//	resource.removePropertyChangeListener(this);
		//}
		cleanupContainmentItems();
		cleanupUndisplayedLinkableObjects();
		typeManager.removePropertyChangeListener(this);

	}
	
	private void cleanupContainmentItems(){
		Iterator it = resource2Item.values().iterator();
		while(it.hasNext()){
			ContainmentItem item = (ContainmentItem)it.next();
			item.dispose();
		}
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		String propName = arg0.getPropertyName();
		if(propName==IncludedTypeManager.CHANGE_TYPE_PROPERTY)
			refresh();
		else if(propName == LinkableObject.TYPEKEY_PROP){
			LinkableObject lo = (LinkableObject)arg0.getNewValue();
			if(typeManager.isIncluded(lo.getLoType())){
				lo.removePropertyChangeListener(this);
				undisplayedLinkableObjects.remove(lo);
				refresh();
			}
		}
		
	}

	/**
	 * refocuses this ContainmentSet so that it is centered on a new
	 * Resource. This is needed when the View is driven by an Editor
	 * in which the resource being worked on can change.  The code
	 * here cleans up the old data for the old Resource, rebuilds the
	 * model for the new resource, and fires a property change to
	 * announce that the items have changed.
	 * 
	 * @param newResource Resource to replace old Resource.
	 */
	
	public void updateResource(Resource newResource) {
		cleanupContainmentItems();
		cleanupUndisplayedLinkableObjects();
		startingResource = newResource;
		centralResources = new HashSet();
		allResources = new HashSet();
		resource2Item = new HashMap();
		linkCache = new HashMap();
		expandedResources = new HashMap();

		centralResources.add(newResource);
		allResources.add(newResource);
		processResource(newResource);
		firePropertyChange(ITEMSCHANGED_EVENT, null, this);
	}

	/**
	 * completely regenerates the model for the existing
	 * Resource. The code
	 * here cleans up the old data built previously, rebuilds the
	 * model for the resource completely, and fires a property change to
	 * announce that the items have changed.
	 * <p>Even though most of the data in the ContainmentSet is thrown
	 * away the set saves data that indicates whether or not a
	 * ContainmentSet for each Resource was expanded by showing its
	 * parents/children or not, so that after the refresh the display
	 * shows the same amount of child/parent data as it did before.
	 */
	
	public void refresh(){
		cleanupContainmentItems();
		cleanupUndisplayedLinkableObjects();
		centralResources = new HashSet();
		allResources = new HashSet();
		resource2Item = new HashMap();
		linkCache = new HashMap();
		// note that expandedResources is not cleared!
		processResource(startingResource);
		firePropertyChange(ITEMSCHANGED_EVENT, null, this);
	}
	
	private void cleanupUndisplayedLinkableObjects() {
		Iterator it = undisplayedLinkableObjects.iterator();
		while (it.hasNext()) {
			LinkableObject lo = (LinkableObject) it.next();
			lo.removePropertyChangeListener(this);
		}
		
	}
	
	/**
	 * convenience method to ask the 
	 * {@link IncludedTypeManager} to include links of
	 * the given type.
	 * 
	 * @param theType LOType type to include
	 */

	public void includeType(LOType theType){
		typeManager.includeType(theType);
		//updateResource(startingResource);
	}
	
	/**
	 * convenience method to ask the 
	 * {@link IncludedTypeManager} to exclude links of
	 * the given type.
	 * 
	 * @param theType LOType type to exclude
	 */

	public void excludeType(LOType theType){
		typeManager.excludeType(theType);
		//updateResource(startingResource);
	}
	
	/**
	 * convenience method to ask the 
	 * {@link IncludedTypeManager} to ask if the given type
	 * is currently to be included or not.
	 * 
	 * @param theType LOType type to exclude
	 * @return boolean is <code>true</code> is the manager says
	 * data of this type should be included.
	 */
	
	public boolean isIncluded(LOType theType){
		return typeManager.isIncluded(theType);
	}
	
	// there should be code here to remove Resources if they are deleted
	// this is not an "end of the world problem", since it automatically
	// cleans itself up when Pliny is terminated.    .. jb
	
	public void recordParentExpand(Resource r){
		ExpandParameters par = (ExpandParameters)expandedResources.get(r);
		if(par == null){
			par = new ExpandParameters();
		    expandedResources.put(r, par);
		}
	    par.showingParent = true;
	}
	
	public void recordChildExpand(Resource r){
		ExpandParameters par = (ExpandParameters)expandedResources.get(r);
		if(par == null){
			par = new ExpandParameters();
		    expandedResources.put(r, par);
		}
	    par.showingChild = true;
	}
	
	public boolean isParentExpanded(Resource r){
		ExpandParameters par = (ExpandParameters)expandedResources.get(r);
		if(par == null)return false;
		return par.showingParent;
	}
	
	public boolean isChildExpanded(Resource r){
		ExpandParameters par = (ExpandParameters)expandedResources.get(r);
		if(par == null)return false;
		return par.showingChild;
	}
	
	public void addUndisplayedLinkableObject(LinkableObject lo){
		if(undisplayedLinkableObjects.contains(lo))return;
		undisplayedLinkableObjects.add(lo);
		lo.addPropertyChangeListener(this);
	}

}
