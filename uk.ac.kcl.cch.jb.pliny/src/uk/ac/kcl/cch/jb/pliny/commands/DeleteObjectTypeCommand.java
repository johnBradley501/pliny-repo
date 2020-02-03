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

import uk.ac.kcl.cch.jb.pliny.model.ObjectType;

/**
 * deletes a 
 * {@link uk.ac.kcl.cch.jb.pliny.model.ObjectType} item. It checks to be
 * sure that there are no resources linked to it before deletion, and
 * will not do a delete if there are.
 * 
 * @author John Bradley
 *
 */

public class DeleteObjectTypeCommand extends Command {
	
	private ObjectType objectType;
	private boolean didIt = false;

	public DeleteObjectTypeCommand(ObjectType objectType) {
		super("delete Object Type");
		this.objectType = objectType;
	}
	
	public void execute(){
		if(objectType.getResources().getCount() > 0)return;
		objectType.deleteMe();
		didIt = true;
	}
	
	public void undo(){
		if(didIt)objectType.reIntroduceMe();
	}
}
