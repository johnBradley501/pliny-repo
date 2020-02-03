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

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.commands.AddToFavouritesCommand;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.views.ResourceExplorerView;

/**
 * This is an action used within the Resource Explorer to add the selected
 * resource(s) in the Explorer to the favourites list (now called Bookmarks).
 * It makes use of the Resource Explorer's command stack, and executes the
 * action as a Command so that it is readily undoable.
 * 
 * @author John Bradley
 *
 */

public class AddToFavouritesAction extends Action {
    
	private ResourceExplorerView view;
	
	/**
	 * creates an instance of this class -- requires the ResourceExplorerView
	 * so that it can use its CommandStack.
	 * 
	 * @param view owning ResourceExplorerView object.
	 */
	public AddToFavouritesAction(ResourceExplorerView view) {
		super();
		this.view = view;
		this.setText("Add to Bookmarks");
		this.setImageDescriptor(
				ImageDescriptor.createFromImage(PlinyPlugin.getDefault().getImage("icons/favIcon.gif")));
	}
	
	private Resource getSelectedResource(){
		Vector selectedItems = view.getSelectedBaseObjects();
		Resource rslt = null;
		Iterator it = selectedItems.iterator();
		while(it.hasNext()){
			Object obj = it.next();
			if(obj instanceof Resource){
				if(rslt == null)rslt = (Resource)obj;
				else return null;
			}
		}
		return rslt;
	}

	/**
	 * runs the command.
	 */
	public void run(){
		Resource resource = getSelectedResource();
		if(resource == null)return;
		view.getCommandStack().execute(new AddToFavouritesCommand(resource));
	}
}
