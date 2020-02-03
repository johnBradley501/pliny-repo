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
import org.eclipse.swt.graphics.RGB;

import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.Note;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * this command allows any of the attributes associated with
 * a LOType to be changed.  It is invoked from the Type Manager's
 * <code>EditTypeWizard</code>.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.views.utils.EditTypeWizard
 * 
 * @author John Bradley
 *
 */
public class UpdateLOTypeCommand extends Command {
	private LOType myType;
	private RGB titleFore, oldTitleFore = null;
	private RGB titleBack, oldTitleBack = null;
	private RGB bodyFore, oldBodyFore = null;
	private RGB bodyBack, oldBodyBack = null;
	private String name, oldName;
	
	private Resource sourceRole, oldSourceRole;
	private String sourceRoleName;
	private boolean newSource = false;
	private Resource targetRole, oldTargetRole;
	private String targetRoleName;
	private boolean newTarget = false;

	/**
	 * sets up an instance of the command to update the LOType.
	 * 
	 * @param myType LOType the type to be changed.
	 * @param name String the new name for the LOType.
	 * @param titleFore RGB the new color for the Title Foreground
	 * @param titleBack RGB the new color for the Title Backroundground
	 * @param bodyFore RGB the new color for the Body Foreground
	 * @param bodyBack RGB the new color for the Body Background
	 * @param sourceRoleKey int key to Resource to act as the Source Role
	 * @param sourceRoleName String name of Resource to be created to
	 * ast as the Source Role.
	 * @param targetRoleKey int key to Resource to act as the Target Role
	 * @param targetRoleName String name of Resource to be created to
	 * ast as the Target Role.
	 */
	
	public UpdateLOTypeCommand(LOType myType, String name,
			RGB titleFore, RGB titleBack, RGB bodyFore, RGB bodyBack,
			int sourceRoleKey, String sourceRoleName,
			int targetRoleKey, String targetRoleName) {
		super("update type");
		this.myType = myType;
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
		if(!myType.getName().equals(name)){
			oldName = myType.getName();
			myType.setName(name);
		}
		if(!sameColours(myType.getBodyBackColourRGB(),bodyBack)){
			oldBodyBack = myType.getBodyBackColourRGB();
			myType.setBodyBackColourRGB(bodyBack);
		}
		if(!sameColours(myType.getBodyForeColourRGB(),bodyFore)){
			oldBodyFore = myType.getBodyForeColourRGB();
			myType.setBodyForeColourRGB(bodyFore);
		}
		if(!sameColours(myType.getTitleForeColourRGB(),titleFore)){
			oldTitleFore = myType.getTitleForeColourRGB();
			myType.setTitleForeColourRGB(titleFore);
		}
		if(!sameColours(myType.getTitleBackColourRGB(),titleBack)){
			oldTitleBack = myType.getTitleBackColourRGB();
			myType.setTitleBackColourRGB(titleBack);
		}
		
		oldTargetRole = myType.getTargetRole();
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
		
		oldSourceRole = myType.getSourceRole();
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

	private boolean sameColours(RGB one, RGB two){
		if(one.red != two.red)return false;
		if(one.green != two.green)return false;
		if(one.blue != two.blue)return false;
		return true;
	}
	
	public void undo(){
		if(oldName != null)myType.setName(oldName);
		if(oldBodyBack != null)myType.setBodyBackColourRGB(oldBodyBack);
		if(oldBodyFore != null)myType.setBodyForeColourRGB(oldBodyFore);
		if(oldTitleBack != null)myType.setTitleBackColourRGB(oldTitleBack);
		if(oldTitleFore != null)myType.setTitleForeColourRGB(oldTitleFore);
		
		if(oldSourceRole != null && oldSourceRole.getALID() == 0){
			oldSourceRole.reIntroduceMe();
			oldSourceRole.setObjectType(Note.getNoteObjectType());
		}
		myType.setSourceRole(oldSourceRole);
		if(oldTargetRole != null && oldTargetRole.getALID() == 0){
			oldTargetRole.reIntroduceMe();
			oldTargetRole.setObjectType(Note.getNoteObjectType());
		}
		myType.setTargetRole(oldTargetRole);
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
