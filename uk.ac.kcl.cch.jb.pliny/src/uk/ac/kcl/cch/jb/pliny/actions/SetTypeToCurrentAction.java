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

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import uk.ac.kcl.cch.jb.pliny.commands.SetItemsToTypeCommand;
import uk.ac.kcl.cch.jb.pliny.model.LOType;

/**
 * This action supports the setting of the "current" 
 * {@link uk.ac.kcl.cch.jb.pliny.model.LOType} associated with selected
 * with {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject}s stored in a Vector of items.
 * It is set up as part of the GEF-driven contextual menu
 * {@link uk.ac.kcl.cch.jb.pliny.actions.PlinyMenuProvider}, and
 * invokes the command 
 * {@link uk.ac.kcl.cch.jb.pliny.commands.SetItemsToTypeCommand}
 * via the given CommandStack, so that it is undoable.
 * 
 * @author John Bradley
 *
 */


public class SetTypeToCurrentAction extends Action {
	private Vector items;
	private CommandStack commandStack;
	
	public SetTypeToCurrentAction(Vector items, CommandStack commandStack) {
		super("change type to current");
		this.setImageDescriptor(
				ImageDescriptor.createFromImage(LOType.getCurrentType().getColourIcon()));
				//ImageDescriptor.createFromImage(PlinyPlugin.getDefault().getImage("icons/typeIcon.gif")));
		this.items = items;
		this.commandStack = commandStack;
	}

	public void run(){
		commandStack.execute(new SetItemsToTypeCommand(items, LOType.getCurrentType()));
	}
}
