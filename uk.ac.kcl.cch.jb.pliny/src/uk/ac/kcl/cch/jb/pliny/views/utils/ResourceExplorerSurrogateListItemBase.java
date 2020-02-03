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

import org.eclipse.swt.graphics.Image;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * an abstract class that provides base functionality for those
 * Resource Explorer model items that manage lists of Pliny
 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource Resource}s.
 * 
 * @author John Bradley
 */
abstract public class ResourceExplorerSurrogateListItemBase extends
ResourceExplorerItemBase {
	
	private String myName;
	protected IResourceExplorerItem parent;
	private List surrogateList;

	public ResourceExplorerSurrogateListItemBase(IResourceTreeDisplayer myView,
			IResourceExplorerItem parent, Resource theObject,
			String myName, String trackingProperty,
			List surrogateList) {
		super(myView, theObject, trackingProperty);
		this.myName = myName;
		this.parent = parent;
		this.surrogateList = surrogateList;
	}
	
    public String getText(){
    	return myName;
    }
    public boolean canModify(){
    	return false;
    }

	public void setText(String name) {
		// does nothing -- name cannot be modified by the user.
	}
	
	static private Image theIcon = null;

	public Image getIcon() {
		if(theIcon ==  null){
			theIcon = PlinyPlugin.getDefault().getImage("icons/referencersOn.gif");
		}
		return theIcon;
	}

	public IResourceExplorerItem getParent() {
		return parent;
	}

	public Iterator createListIterator() {
		return surrogateList.iterator();
	}

	abstract public IResourceExplorerItem makeChild(Object item);
}
