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
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.graphics.RGB;

import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObjectQuery;
import uk.ac.kcl.cch.jb.pliny.model.Note;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.rdb2java.dynData.BaseQuery;

/**
 * Creates a new {@link uk.ac.kcl.cch.jb.pliny.model.LOType}.
 * This is used by the 
 * {@link uk.ac.kcl.cch.jb.pliny.views.utils.EditTypeWizard}
 * which first interacts with the user to get data upon which
 * the LOType should be created.
 * 
 * @author John Bradley
 *
 */

public class AddLOTypeCommand extends Command {
	private LOType myType = null;
	private RGB titleFore;
	private RGB titleBack;
	private RGB bodyFore;
	private RGB bodyBack;
	private String name;
	
	private Resource sourceRole;
	private String sourceRoleName;
	private boolean newSource = false;
	private Resource targetRole;
	private String targetRoleName;
	private boolean newTarget = false;

	/**
	 * constructs a new {@link uk.ac.kcl.cch.jb.pliny.model.LOType}.
	 * <p>
	 * In all cases colours and a name must be provided.  The Role
	 * information however is slightly more complex:
	 * <ul>
	 * <li>If the user has pointed to an existing Resource as the
	 * role, give its key as the value for sourceRoleKey or
	 * targetRoleKey.
	 * <li>If the user has given a name and not a Resource
	 * the system should create a new Resource with the given name.
	 * In this case give the name and give the Key value as 0.
	 * </ul>
	 * 
	 * @param name String name of new LOType.
	 * @param titleFore RGB of color for title Foreground.
	 * @param titleBack RGB of color for title Background.
	 * @param bodyFore RGB of color for body Foreground.
	 * @param bodyBack RGB of color for body Background.
	 * @param sourceRoleKey int key to Resource to act as source Role
	 * @param sourceRoleName String name of Resource to be created to act as source Role
	 * @param targetRoleKey int key to Resource to act as target Role
	 * @param targetRoleName String name of Resource to be created to act as target Role
	 */
	public AddLOTypeCommand(String name,
			RGB titleFore, RGB titleBack, RGB bodyFore, RGB bodyBack,
			int sourceRoleKey, String sourceRoleName,
			int targetRoleKey, String targetRoleName) {
		super("add type");
		this.bodyBack = bodyBack;
		this.bodyFore = bodyFore;
		this.titleBack = titleBack;
		this.titleFore = titleFore;
		this.name = name.trim();
		
		if(sourceRoleKey == 0)sourceRole = null;
		else sourceRole = Resource.getItem(sourceRoleKey);
		this.sourceRoleName = sourceRoleName;
		if(targetRoleKey == 0)targetRole = null;
		else targetRole = Resource.getItem(targetRoleKey);
		this.targetRoleName = targetRoleName;

	}

	public void execute(){
		myType = new LOType(true);
		myType.setName(name);
		myType.setBodyBackColourRGB(bodyBack);
		myType.setBodyForeColourRGB(bodyFore);
		myType.setTitleBackColourRGB(titleBack);
		myType.setTitleForeColourRGB(titleFore);
		myType.reIntroduceMe();
		
		if(targetRole == null && targetRoleName != null){
			NoteLucened newNote = new NoteLucened(true);
			newNote.setName(targetRoleName);
			newNote.setContent("target role for '"+myType.getName()+"'");
			newNote.reIntroduceMe();
			newNote.setObjectType(Note.getNoteObjectType());
			targetRole = newNote;
			newTarget = true;
		}
		myType.setTargetRole(targetRole);
		
		if(sourceRole == null && sourceRoleName != null){
			NoteLucened newNote = new NoteLucened(true);
			newNote.setName(sourceRoleName);
			newNote.setContent("source role for '"+myType.getName()+"'");
			newNote.reIntroduceMe();
			newNote.setObjectType(Note.getNoteObjectType());
			sourceRole = newNote;
			newSource = true;
		}
		myType.setSourceRole(sourceRole);
	}
	
	public void undo(){
		if(myType == null || myType.getALID() <= 1)return;
		LinkableObjectQuery q = new LinkableObjectQuery();
		//q.setWhereString("typeKey="+myType.getALID());
		q.addConstraint("typeKey", BaseQuery.FilterEQUAL, myType.getALID());
		int usage = q.executeCount();
		if(usage > 0){
			ErrorDialog.openError(null,"Type in Use",
					"The type '"+myType.getName()+"' is being used and cannot be removed by undoing its creation.",null);
			return;
		}
		myType.setTargetRole(null);
		myType.setSourceRole(null);
		myType.deleteMe();
		if(newSource && sourceRole.isDeletable()){
			sourceRole.setObjectType(null);
			sourceRole.deleteMe();
		}
		if(newTarget && targetRole.isDeletable()){
			targetRole.setObjectType(null);
			targetRole.deleteMe();
		}
	}
}
