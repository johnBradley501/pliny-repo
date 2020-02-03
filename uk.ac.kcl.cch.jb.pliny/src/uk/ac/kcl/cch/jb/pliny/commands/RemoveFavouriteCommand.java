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

/**
 * This command removes an existing Favourites item (now called Bookmark
 * in the UI) from the list of favourites.  This command is
 * invoked through the Resource Explorer's
 * {@link uk.ac.kcl.cch.jb.pliny.actions.RemoveFromFavouritesAction}
 * 
 * @author John Bradley
 *
 */

public class RemoveFavouriteCommand extends Command {

	private Favourite favourite;
	private Resource resource;
	
	/**
	 * sets up command to delete specified 
     * {@link uk.ac.kcl.cch.jb.pliny.model.Favourite}.
	 * 
	 * @param favourite
	 */
	
	public RemoveFavouriteCommand(Favourite favourite) {
		super("remove Bookmarked item");
		this.favourite = favourite;
	}
	
	public void execute(){
		resource = favourite.getResource();
		favourite.setResource(null);
		favourite.deleteMe();
	}
	
	public void undo(){
		favourite.reIntroduceMe();
		favourite.setResource(resource);
	}

}
