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

/**
 * 
 */
package uk.ac.kcl.cch.jb.pliny.actions;

import org.eclipse.gef.EditDomain;
import org.eclipse.gef.Tool;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.tools.CreationTool;
import org.eclipse.jface.action.Action;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.policies.NoteCreationTool;
//import uk.ac.kcl.cch.jb.pliny.policies.NoteCreationTool;

/**
 * This action tells GEF to use its Creation tool to allow the user to
 * create a new {@link uk.ac.kcl.cch.jb.pliny.model.NoteLucened}.  To this end, it is also a creation factory
 * for NoteLucened objects.
 * <p>
 * The code is modelled on code in IBM's redbook "Eclipse
 * Development using GEF and EMF", pg 155.
 * 
 * @author John Bradley
 */
public class MakeNoteAction extends Action 
implements CreationFactory{
	
	private EditDomain editDomain;
	private CreationFactory factory;
	private Tool tool;
	
	/**
	 * constructor for this action.  It will require a GEF editDomain 
	 * to which the tool can be given before
	 * it can be used, which can be specified through <code>setEditDomain()</code>.
	 *
	 */
	public MakeNoteAction(){
		super();
		this.editDomain = null;
		init();
	}
	
	/**
	 * stores the editDomain to which the creation tool will be given.
	 * 
	 * @param editDomain EditDomain
	 */
	public void setEditDomain(EditDomain editDomain){
		this.editDomain = editDomain;
	}
	
	/**
	 * constructor for this action.  The editDomain is the one to
	 * which the CreateTool is to be given.
	 *
	 *@param editDomain EditDomain 
	 */
	public MakeNoteAction(EditDomain editDomain){
		super();
		this.editDomain = editDomain;
		init();
	}
	
	private void init(){
		setText("Create new Note");
		setImageDescriptor(PlinyPlugin.getImageDescriptor("icons/noteIcon.gif"));
				// ImageDescriptor.createFromImage(ModelPlugin.getDefault().getImage("icons/noteIcon.gif")));
		this.setToolTipText("Create new Note");
		factory = this;
		//this.setActionDefinitionId("uk.ac.kcl.cch.jb.pliny.command.newNoteMode");
	}
	
	/**
	 * creates a new CreationTool with this as a factory for NoteLucenes
	 * and gives the tool to the editDomain.
	 * 
	 */
	public void run(){
		if(editDomain != null){
		  //tool = new CreationTool(factory);
		  tool = new NoteCreationTool(factory);
		  editDomain.setActiveTool(tool);
		}
	}

	/**
	 * creates a new {@link uk.ac.kcl.cch.jb.pliny.model.NoteLucened} object.  Part of the CreationFactory interface.
	 * <p>
	 * Note that the NoteLucened object is created in such a way that it is not
	 * immediately added to the backing DB.  This putting in the DB will
	 * be done if the user completes the note creation.
	 */
	public Object getNewObject(){
		return new NoteLucened(true);
	}

	/**
	 * Returns the new object's type.
	 * @return the type
	 */
	public Object getObjectType(){
		return NoteLucened.class;
	}

}
