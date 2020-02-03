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

import java.util.Vector;

/**
 * extends {@link MinMaxBaseCommand} to provide a command to take
 * the set of {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject}s provided and either minimize them
 * or expand them all.
 * 
 * @author John Bradley
 *
 */
public class MinMaxSelectedItemsCommand extends MinMaxBaseCommand {

	/**
	 * creates an instance of this command, and gives it the
	 * list of LinkableObjects it is to work with, and an indication
	 * of whether it needs to expand or minimize them.  This command
	 * assumes the list contains only LinkableObjects.
	 * 
	 * @param items a collection of LinkableObjects to work with.
	 * @param setToOpen if <code>true</code> expand them, if
	 * <code>false</code> minimise them.
	 */
	
	public MinMaxSelectedItemsCommand(Vector items, boolean setToOpen) {
		super(setToOpen);
		if(setToOpen)setLabel("maximize selected");
		else setLabel("minimize selected");
		this.setList(items);
	}
}
