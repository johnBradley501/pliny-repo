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
import java.util.Vector;

import org.eclipse.swt.graphics.Image;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.model.ObjectType;
import uk.ac.kcl.cch.jb.pliny.model.ObjectTypeQuery;
import uk.ac.kcl.cch.rdb2java.Rdb2javaPlugin;

/**
 * the root object for the Resource Explorer's Type-oriented tree display. Since the
 * first level of the display is 
 * {@link uk.ac.kcl.cch.jb.pliny.model.ObjectType ObjectType} items, it is the job of
 * this item to track the creation and deletion of ObjectTypes, and
 * request an update of its children list, and of the viewer, as necessary.
 * @author John Bradley
 *
 */
public class ResourceExplorerRoot extends ResourceExplorerItemBase {
	
	public ResourceExplorerRoot(IResourceTreeDisplayer myView){
		super(myView, null, null);
		Rdb2javaPlugin.getDataServer().addPropertyChangeListener(this);
	}

	public void dispose() {
		super.dispose();
		Rdb2javaPlugin.getDataServer().removePropertyChangeListener(this);
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		String pName = arg0.getPropertyName();
		if(pName.equals("Create-ObjectType") || pName.equals("Delete-ObjectType")){
			updateMyChildren();
			myView.getMyViewer().refresh(this);
		}
	}

	public String getText() {
		return "root";
	}

	public void setText(String name) {
	}

	public boolean canModify() {
		return false;
	}

	public Image getIcon() {
		return null;
	}

	public IResourceExplorerItem getParent() {
		return null;
	}

	public Iterator createListIterator() {
		ObjectTypeQuery q = new ObjectTypeQuery();
		Vector rslts = q.executeQuery();
		//rslts.add("My Starting Points");
		rslts.add("My Bookmarks");
		return rslts.iterator();
	}

	public IResourceExplorerItem makeChild(Object item) {
		if(item instanceof String)
			return new ResourceExplorerFavGroupItem(myView, this, (String)item);
		return new ResourceExplorerObjectTypeItem(myView, this, (ObjectType)item);
	}

}
