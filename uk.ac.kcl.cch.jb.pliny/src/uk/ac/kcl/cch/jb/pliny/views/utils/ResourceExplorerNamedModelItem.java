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

import org.eclipse.swt.graphics.Image;

import uk.ac.kcl.cch.jb.pliny.model.INamedObject;
import uk.ac.kcl.cch.rdb2java.dynData.BaseObject;

/**
 * this abstract class provides base implementation for all Resource
 * Explorer display items which display names that come from the
 * Pliny data model, and hence can be edited.
 * <p>
 * The class also tracks name changes that happen elsewhere in Pliny
 * so that the name displaying in the Resource Explorer is updated.
 * 
 * @author John Bradley
 *
 */

public abstract class ResourceExplorerNamedModelItem extends ResourceExplorerItemBase {

	//private INamedObject theObject;
	private IResourceExplorerItem parent;
	private String nameProperty;
	
	public ResourceExplorerNamedModelItem(IResourceTreeDisplayer myView, 
			IResourceExplorerItem parent, 
			INamedObject theObject, 
			String trackingProperty,
			String nameProperty) {
		super(myView, (BaseObject)theObject, trackingProperty);
		//this.theObject = theObject;
		this.parent = parent;
		this.nameProperty = nameProperty;
	}
	
	protected INamedObject getNamedObject(){
		return (INamedObject)this.getBaseObject();
	}

	public String getText() {
		INamedObject theObject = getNamedObject();
		if(theObject == null)return "";
		return getNamedObject().getName();
	}

	public void setText(String text) {
		getNamedObject().setName(text);

	}

	public boolean canModify() {
		return true;
	}

	public IResourceExplorerItem getParent() {
		return parent;
	}

	abstract public Image getIcon();

	public void propertyChange(PropertyChangeEvent arg0) {
		String pName = arg0.getPropertyName();
		if(pName.equals(nameProperty)){
			myView.getMyViewer().update(this, null);
		} else super.propertyChange(arg0);
	}

}
