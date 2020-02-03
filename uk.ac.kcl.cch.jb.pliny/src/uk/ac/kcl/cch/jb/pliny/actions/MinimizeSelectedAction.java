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
import uk.ac.kcl.cch.jb.pliny.commands.MinMaxSelectedItemsCommand;

/**
 * This action supports the Minimize or Maximize Selected items action 
 * for Pliny editors and view. It invokes the command
 * {@link uk.ac.kcl.cch.jb.pliny.commands.MinMaxSelectedItemsCommand}
 * which means that it undoable. It is meant to be invoked in the context of
 * a contextual menu where the selected items and the command stack
 * it is to use are known at the time it is set up.
 * 
 * 
 * @author John Bradley
 */

public class MinimizeSelectedAction extends Action {

	private Vector items;
	private CommandStack commandStack;
	private boolean setToOpen;

	/**
	 * constructs an instance of this action in the context of
	 * the creation of a contextual menu.  At the time both the
	 * list of items to be minimized/maximized, the commandstack
	 * to be used and whether this is a minimize or a maximize is known.
	 * 
	 * @param items the items to minimize/maximize
	 * @param commandStack the commandstack to store the command
	 * @param setToOpen if <code>true</code> request is a maximize
	 */
	public MinimizeSelectedAction(Vector items, CommandStack commandStack, boolean setToOpen) {
		super();
		if(setToOpen){
			this.setText("Maximise selected");
			setImageDescriptor(PlinyPlugin.getImageDescriptor("icons/openNote.gif"));
		}
		else {
			this.setText("Minimize selected");
			setImageDescriptor(PlinyPlugin.getImageDescriptor("icons/closeNote.gif"));
		}
		this.setToolTipText("Minimize selected objects");
		this.items = items;
		this.commandStack = commandStack;
		this.setToOpen = setToOpen;
	}
	
	/**
	 * invokes the command 
	 * {@link uk.ac.kcl.cch.jb.pliny.commands.MinMaxSelectedItemsCommand}
	 * against the supplied CommandStack.  Whether it was a minimize or
	 * maximise depends upon the parameter given by the constructor
	 * <code>setToOpen</code>.  If <code>true</code> request is for a
	 * maximize, if <code>false</code> request is a minimize.
	 */
	public void run(){
		commandStack.execute(new MinMaxSelectedItemsCommand(items, setToOpen));
	}

}
