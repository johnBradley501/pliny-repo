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

import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;

/**
 * the Resource Explorer's model element for data that appears in
 * a Resource's reference/annotation area.
 * 
 * @author John Bradley
 */
public class ResourceExplorerSurrogateItem extends
ResourceExplorerResourceItem {
	
	LinkableObject myLinkableObject;

	public ResourceExplorerSurrogateItem(IResourceTreeDisplayer myView,
			IResourceExplorerItem parent, 
            LinkableObject linkableObject) {
		super(myView, parent, linkableObject.getSurrogateFor());
		myLinkableObject = linkableObject;
	}
	
	public Object getAssociatedObject(){
		return myLinkableObject;
	}
	
    public int getPageNumber(){
    	return myLinkableObject.getSurrPageNo();
    }
}
