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

import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * represents the Resource Explorer's 2nd level (initial letter of name)
 * level item in its data model.  This is a "list" item, and it
 * tracks its ResourceNameManager to watch to see when changes in the
 * count associated with an initial letter, or when an initial letter 
 * needs to be created or deleted.
 * 
 * @author John Bradley
 */
public class ResourceExplorerInitialLetterItem extends ResourceExplorerItemBase {

	private ResourceNameInitialLetter ilItem = null;
	private ResourceExplorerObjectTypeItem parent;
	
	public ResourceExplorerInitialLetterItem(IResourceTreeDisplayer myView, 
			ResourceExplorerObjectTypeItem parent, 
			ResourceNameInitialLetter ilItem) {
		super(myView,null, null);
		this.ilItem = ilItem;
		this.parent = parent;
		//parent.getMyManager().addPropertyChangeListener(this);
		ilItem.addPropertyChangeListener(this);
	}

	public void dispose() {
		//parent.getMyManager().removePropertyChangeListener(this);
		ilItem.removePropertyChangeListener(this);
		super.dispose();
	}

	public String getText() {
		return ilItem.getText();
	}

	public void setText(String name) {
		// does not need to do anything
	}
	
	public String getInitialLetter(){
		return ilItem.getLetter();
	}

	public boolean canModify() {
		return false;
	}

	public Object getAssociatedObject() {
		return ilItem;
	}

	public Image getIcon() {
		return ResourceExplorerObjectTypeItem.getMyIcon();
	}

	public IResourceExplorerItem getParent() {
		return parent;
	}

	public int getNumberChildren() {
		// TODO Auto-generated method stub
		return ilItem.getCount();
	}

	public Iterator createListIterator() {
		return ilItem.getChildrenIterator();
	}

	public IResourceExplorerItem makeChild(Object item) {
		return new ResourceExplorerResourceItem(myView, this, (Resource)item);
	}
	
	public void propertyChange(PropertyChangeEvent arg0) {
		String name = arg0.getPropertyName();
		if(name == ResourceNameManager.INITLETTER_EVENT){
			if(ilItem.getCount() == 0){
				parent.removeChild(this);
				myView.getMyViewer().refresh(parent);
				return;
			}
			if(ilItem.resourcesLoaded())this.updateMyChildren();
			myView.getMyViewer().refresh(this);
		}
	}

}
