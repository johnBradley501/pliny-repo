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

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.commands.GroupLinkableObjectsCommand;

/**
 * the action to group references into an enclosing group
 * This action supports the contextual menu action within Pliny reference
 * or annotation areas.  It makes use of a CommandStack so that it can be
 * undoable, and runs the command {@link uk.ac.kcl.cch.jb.pliny.commands.GroupLinkableObjectsCommand}.
 * 
 * @author John Bradley
 *
 */
public class GroupLinkableObjectsAction extends Action {
	private Vector items;
	private CommandStack commandStack;

	/**
	 * constructor for the group action.  The items to be grouped
	 * are provided since it is used in a contextual menu which is
	 * always created at the moment the menu is invoked.
	 * 
	 * @param items Vector of items to be included in the group
	 * @param commandStack CommandStack to use to handle grouping command.
	 */
	public GroupLinkableObjectsAction(Vector items, CommandStack commandStack) {
		super("Group objects");
		setImageDescriptor(PlinyPlugin.getImageDescriptor("icons/groupIcon.gif"));
		this.items = items;
		this.commandStack = commandStack;
	}

	public void run(){
		commandStack.execute(new GroupLinkableObjectsCommand(items));
	}
}
