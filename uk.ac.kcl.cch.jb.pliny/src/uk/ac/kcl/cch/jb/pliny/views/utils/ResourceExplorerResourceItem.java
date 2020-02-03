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

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.swt.graphics.Image;

import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * provides Resource Explorer model elements for Pliny
 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource Resource}s displayed
 * in the Resource Explorer's display.
 * <p>
 * Items below each of these items is a list of names grouped by
 * the initial letter.  This is managed by a 
 * {@link ResourceNameManager}.
 * 
 * @author John Bradley
 */
public class ResourceExplorerResourceItem extends
		ResourceExplorerNamedModelItem {

	//protected Resource theResource;
	
	public ResourceExplorerResourceItem(IResourceTreeDisplayer myView, IResourceExplorerItem parent,
			Resource theObject) {
		super(myView,parent, theObject,"",Resource.NAME_PROP);
		//theResource = theObject;
	}
	
	public Resource getResource(){
		return (Resource)getBaseObject();
	}

	public Image getIcon() {
		if(getResource() == null)return null;
		if(getResource().getObjectType() == null)return null;
		return getResource().getObjectType().getIconImage();
	}

	public boolean hasChildren() {
		return true;
	}

	public List getChildren() {
		if(myChildren == null){
			myChildren = new Vector();
			myChildren.add(new ResourceExplorerDisplayedInList(
				getMyView(), this, getResource()));
			myChildren.add(new ResourceExplorerContainsList(
				getMyView(), this, getResource()));
		}
		return myChildren;
	}

	public Iterator createListIterator() {
		// not used here
		return null;
	}

	public IResourceExplorerItem makeChild(Object item) {
		// not used here
		return null;
	}

}
