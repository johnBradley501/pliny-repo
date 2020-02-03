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
 * deletes a 
 * {@link uk.ac.kcl.cch.jb.pliny.model.Favourite} (now called "bookmark") item.
 * 
 * @author John Bradley
 *
 */
public class DeleteFavouriteCommand extends Command {
	
	private Favourite favourite;
	private Resource resource;

	public DeleteFavouriteCommand(Favourite favourite) {
		super("delete bookmark item");
		this.favourite = favourite;
	}
	
	public void execute(){
		resource = favourite.getResource();
		favourite.deleteMe();
	}
	
	public void undo(){
		favourite.reIntroduceMe();
		favourite.setResource(resource);
	}

}
