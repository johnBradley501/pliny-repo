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

import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * the Resource Explorer's model element for the 5th level items
 * displayed under the 4th level "Displayed In" item.
 * 
 * @author John Bradley
 */

public class ResourceExplorerDisplayedInItem extends ResourceExplorerResourceItem {
	
	LinkableObject myLinkableObject;

	public ResourceExplorerDisplayedInItem(IResourceTreeDisplayer myView,
			IResourceExplorerItem parent, LinkableObject linkableObject) {
		super(myView, parent, linkableObject.getDisplayedIn());
		myLinkableObject = linkableObject;
	}
	
	public Resource getResource(){
		return myLinkableObject.getDisplayedIn();
	}
	
	public Object getAssociatedObject(){
		return myLinkableObject;
	}

	public String getText() {
		if(myLinkableObject == null)return "";
		if(myLinkableObject.getDisplayedIn() == null){
			myLinkableObject.addPropertyChangeListener(this);
			return "";
		}
		String rslt = myLinkableObject.getDisplayedIn().getName();
		if(myLinkableObject.getDisplPageNo() != 0)
			rslt = rslt + " ("+myLinkableObject.getDisplPageNo()+")";
		return rslt;
		//return myLinkableObject.getDisplayedIn().getName();
	}

	public void setText(String text) {
		myLinkableObject.getDisplayedIn().setName(text);

	}
	
    public int getPageNumber(){
    	return myLinkableObject.getDisplPageNo();
    }
    
	public void propertyChange(PropertyChangeEvent arg0) {
		String propName = arg0.getPropertyName();
		if(propName==LinkableObject.DISPLAYEDINKEY_PROP){
			myLinkableObject.removePropertyChangeListener(this);
			setBaseObject(myLinkableObject.getDisplayedIn());
			myView.getMyViewer().refresh(this);
		}
		super.propertyChange(arg0);
	}


}
