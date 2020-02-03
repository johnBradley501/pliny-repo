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

import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;

/**
 * changes the name of a {@link uk.ac.kcl.cch.jb.pliny.model.Resource} linked as the
 * surrogate to the given {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject}.
 * 
 * @author John Bradley
 *
 */
public class LinkableObjectNameUpdateCommand extends Command {

	private String oldName, newName;
	private LinkableObject obj = null;
	
	/**
	 * creates instance of this command that will change the name of
	 * the surrogate resource to <code>obj</code>'s to <code>newName</code>.
	 * 
	 * @param obj LinkableObject who's surrogate is to be renamed.
	 * @param newName replacement name.
	 */
	
	public LinkableObjectNameUpdateCommand(LinkableObject obj, String newName) {
		super();
		this.newName = newName;
		this.obj = obj;
	}

	public void execute(){
		oldName = obj.getSurrogateFor().getName();
		obj.getSurrogateFor().setName(newName);
	}
	
	public void undo(){
		obj.getSurrogateFor().setName(oldName);
	}

}
