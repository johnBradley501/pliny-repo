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

package uk.ac.kcl.cch.jb.pliny.actions;

import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.commands.RemoveFavouriteCommand;
import uk.ac.kcl.cch.jb.pliny.model.Favourite;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.views.ResourceExplorerView;
import uk.ac.kcl.cch.rdb2java.dynData.BaseObject;

/**
 * An action for the Resource Explorer to respond to a user request
 * to remove an item from the Favourites (now called Bookmark in the UI).
 * 
 * @author John Bradley
 *
 */

public class RemoveFromFavouritesAction extends Action {
	private ResourceExplorerView view;

	public RemoveFromFavouritesAction(ResourceExplorerView view) {
		super();
		this.view = view;
		this.setText("Remove from Bookmarks");
		this.setImageDescriptor(
				ImageDescriptor.createFromImage(PlinyPlugin.getDefault().getImage("icons/favIcon.gif")));
	}
	
	public void run(){
		Vector selectedItems = view.getSelectedBaseObjects();
		if(selectedItems.size() != 1)return;
		BaseObject object = (BaseObject) selectedItems.get(0);
		if(object instanceof Favourite)
			view.getCommandStack().execute(new RemoveFavouriteCommand((Favourite)object));
		else if(object instanceof Resource){
			Favourite myFavourite = Favourite.findFromResource((Resource)object);
			if(myFavourite != null)
				view.getCommandStack().execute(new RemoveFavouriteCommand(myFavourite));
			
		}
	}
}
