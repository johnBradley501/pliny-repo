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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.commands.SetReferentCommand;
import uk.ac.kcl.cch.jb.pliny.views.ResourceExplorerView;

/**
 * This is an action used within the Resource Explorer to clear the current
 * referent.
 * It makes use of the Resource Explorer's command stack, and executes the
 * action as a Command so that it is readily undoable.
 * 
 * @author John Bradley
 *
 */

public class ClearReferentAction extends Action {
	private ResourceExplorerView view;

	/**
	 * creates an instance of this class -- requires the ResourceExplorerView
	 * so that it can use its CommandStack.
	 * 
	 * @param view owning ResourceExplorerView object.
	 */
	public ClearReferentAction(ResourceExplorerView view) {
		super();
		this.view = view;
		this.setText("Clear the Referent");
		this.setImageDescriptor(
				ImageDescriptor.createFromImage(PlinyPlugin.getDefault().getImage("icons/referent.gif")));
	}

	/**
	 * runs the command.
	 */
	public void run(){
		view.getCommandStack().execute(new SetReferentCommand(null));
	}


}
