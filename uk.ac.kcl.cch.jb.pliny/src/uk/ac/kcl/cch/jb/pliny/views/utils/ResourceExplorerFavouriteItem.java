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

import uk.ac.kcl.cch.jb.pliny.model.Favourite;

/**
 * provides the model object for each 
 * {@link uk.ac.kcl.cch.jb.pliny.model.Favourite Favourite}s (now called 'Bookmarks')
 * list.
 * 
 * @author John Bradley
 *
 */

public class ResourceExplorerFavouriteItem extends ResourceExplorerResourceItem {

	private Favourite favourite;
	
	public ResourceExplorerFavouriteItem(IResourceTreeDisplayer myView,
			IResourceExplorerItem parent, Favourite favourite) {
		super(myView, parent, favourite.getResource());
		this.favourite = favourite;
	}
	
    public Object getAssociatedObject(){
    	return favourite;
    }

}
