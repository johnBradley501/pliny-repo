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
import java.util.Iterator;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import uk.ac.kcl.cch.jb.pliny.model.ObjectType;

/**
 * provides Resource Explorer model elements for Pliny
 * {@link uk.ac.kcl.cch.jb.pliny.model.ObjectType ObjectType}s --
 * the top level of the Resource Explorer's display.
 * <p>
 * Items below each of these items is a list of names grouped by
 * the initial letter.  This is managed by a 
 * {@link ResourceNameManager}.
 * 
 * @author John Bradley
 */

public class ResourceExplorerObjectTypeItem extends
		ResourceExplorerNamedModelItem {
	
	private ObjectType theObject;
	private ResourceNameManager myManager = null;

	public ResourceExplorerObjectTypeItem(IResourceTreeDisplayer myView,
			IResourceExplorerItem parent, ObjectType theObject) {
		super(myView, parent, theObject, "", ObjectType.NAME_PROP);
		this.theObject = theObject;
		myManager = new ResourceNameManager(theObject);
		myManager.addPropertyChangeListener(this);
	}
	
	public void dispose(){
		myManager.removePropertyChangeListener(this);
		myManager.dispose();
		super.dispose();
	}
	
	public ResourceNameManager getMyManager(){
		return myManager;
	}
	
	static public Image theIcon = null;
	
	static public Image getMyIcon(){
		if(theIcon ==  null){
			theIcon = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		}
		return theIcon;
	}

	public Image getIcon() {
		return getMyIcon();
	}

	public Iterator createListIterator() {
		// TODO Auto-generated method stub
		return myManager.getInitialLetterIterator();
	}

	public IResourceExplorerItem makeChild(Object item) {
		return new ResourceExplorerInitialLetterItem(myView, this, (ResourceNameInitialLetter)item);
	}
	
    public int getNumberChildren(){
    	return myManager.getNumberInitialLetters();
    }
    
    public void removeChild(ResourceExplorerInitialLetterItem item){
    	if(myManager == null)return;
    	myManager.removeInitialLetter(item.getInitialLetter());
    	updateMyChildren();
    	item.dispose();
    }

	public void propertyChange(PropertyChangeEvent arg0) {
		String name = arg0.getPropertyName();
		if(name == ResourceNameManager.NEW_INITLETTER_EVENT){
			this.updateMyChildren();
		    myView.getMyViewer().refresh(this);
		} else super.propertyChange(arg0);
	}
	
}
