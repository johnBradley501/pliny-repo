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
import org.eclipse.jface.viewers.TreeViewer;

import uk.ac.kcl.cch.jb.pliny.model.INamedObject;

/**
 * command to change the name of any object that has a name.  This
 * command is used in the Resource Explorer and NoteSearchView.
 * 
 * @author John Bradley
 *
 */
public class UpdateNameCommand extends Command {
	
	INamedObject object;
	String newText, oldText;
	TreeViewer theViewer;

	/**
	 * creates an instance of this command that will change the name
	 * of <code>object</code> to the given new name, and refresh
	 * the TreeViewer so that it displays the new name.
	 * 
	 * @see uk.ac.kcl.cch.jb.pliny.model.INamedObject
	 * 
	 * @param theViewer TreeViewer to be refreshed after new name is set.
	 * @param object INamedObject object to be named.
	 * @param newText String new name to use.
	 */
	
	public UpdateNameCommand(TreeViewer theViewer, INamedObject object, String newText) {
		super("update name");
		this.object = object;
		this.newText = newText;
		this.theViewer = theViewer;
		this.oldText = null;
	}
	
	public void execute(){
		if(oldText == null)oldText = object.getName();
		if(oldText.equals(newText))return;
		object.setName(newText);
		if(theViewer != null)theViewer.refresh();
	}
	
	public void undo(){
		if(oldText.equals(newText))return;
		object.setName(oldText);
		if(theViewer != null)theViewer.refresh();
	}
}
