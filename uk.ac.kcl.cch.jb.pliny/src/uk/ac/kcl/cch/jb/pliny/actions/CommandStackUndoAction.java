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

import java.util.EventObject;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.jface.action.Action;

/**
 * This class provides some basic actions that can be used by Pliny Workbench
 * parts that use the CommandStack to handle Undo operations.
 * 
 * @author John Bradley
 *
 */

public class CommandStackUndoAction extends Action implements
		CommandStackListener {
	
	CommandStack commandStack;

	/**
	 * takes the given CommandStack as the one against which the Undo is to
	 * be done.
     * This object must be disposed when you are finished with it since it
     * is a CommandStackListener.
	 * 
	 * @param commandStack the CommandStack to use.
	 */
	public CommandStackUndoAction(CommandStack commandStack) {
		super("Undo");
		setEnabled(false);
		this.commandStack = commandStack;
		this.commandStack.addCommandStackListener(this);
	}
	
	/**
	 * disposes this item.  Releases the commandStackListener connection.
	 *
	 */
	public void dispose(){
		commandStack.removeCommandStackListener(this);
	}
	
	/**
	 * performs the CommandStack undo.
	 */
	public void run() {
		commandStack.undo();
	}

	/**
	 * called when commandstack changes. This is the listener 
	 * interface method.  Here it enables the action as an undo is
	 * there are any commands in the stack that can be undone.
	 * 
	 * @param event the commandStack changing event
	 */
	public void commandStackChanged(EventObject event) {
		setEnabled(commandStack.canUndo());
		if(commandStack.canUndo()){
			setText("Undo "+commandStack.getUndoCommand().getLabel());
		}
	}

}
