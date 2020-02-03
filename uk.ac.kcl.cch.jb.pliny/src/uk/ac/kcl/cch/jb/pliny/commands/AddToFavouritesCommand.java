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

package uk.ac.kcl.cch.jb.pliny.commands;

import org.eclipse.gef.commands.Command;

import uk.ac.kcl.cch.jb.pliny.model.Favourite;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.VirtualResource;

/**
 * This command creates a new Favourites item (now called Bookmark
 * in the UI) that points to a given Resource.  This command is
 * invoked through the Resource Explorer's
 * {@link uk.ac.kcl.cch.jb.pliny.actions.AddToFavouritesAction}
 * 
 * @author John Bradley
 *
 */

public class AddToFavouritesCommand extends Command {
	
	private Resource resource;
	private Favourite myFav;

	/**
	 * creates a command to add the given resource as a 
	 * favourite (UI now calls them "Bookmark").
	 * 
	 * @param resource Resource to be added
	 */
	public AddToFavouritesCommand(Resource resource) {
		super("add to Bookmarks");
		this.resource = resource;
	}
	
	public void execute(){
		if((resource instanceof VirtualResource) && resource.getALID() == 0)((VirtualResource)resource).makeMeReal();
		myFav = new Favourite();
		myFav.setResource(resource);
	}
	
	public void undo(){
		myFav.setResource(null);
		myFav.deleteMe();
	}

}
