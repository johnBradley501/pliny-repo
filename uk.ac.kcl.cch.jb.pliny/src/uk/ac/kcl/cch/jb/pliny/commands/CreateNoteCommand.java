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

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

import uk.ac.kcl.cch.jb.pliny.actions.CreateMinimiseStatus;
import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.ObjectType;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.VirtualResource;

public class CreateNoteCommand extends Command {

	/**
	 * Creates a new note (if not passed one) and a reference 
	 * on an reference/annotation area.
	 * It is invoked from a GEF policy
	 * {@link uk.ac.kcl.cch.jb.pliny.policies.ScalableImageXYLayoutEditPolicy#getCreateCommand}.
	 * 
	 * @author John Bradley
	 *
	 */


	private NoteLucened theNote;
	private ObjectType theObjectType;
	private Resource theResource;
	private Rectangle rectHere;
	private LinkableObject theSurrogate = null;
	private LOType currentLoType;
	private boolean makeOpen = true;
	
	//private static ObjectType noteType = null;

	/**
	 * constructor for this command.
	 * 
	 * @param note NoteLucened to be reference or <code>null</code> if it needs to be created
	 * @param theResource Resource in whose reference/annotation area it is to appear
	 * @param rectHere where it is to appear there
	 */
	public CreateNoteCommand(NoteLucened note, Resource theResource, Rectangle rectHere) {
		super("Create Note");
		this.theNote = note;
		this.theResource = theResource;
		this.rectHere = new Rectangle(rectHere);
		if(this.rectHere.height <= 100)this.rectHere.height=100;
		currentLoType = LOType.getCurrentType();
		makeOpen = !CreateMinimiseStatus.instance().makeMin();
	}
	
	public CreateNoteCommand(LOType theType, Resource theResource, Rectangle rectHere) {
		super("Create Note");
		this.theNote = null;
		this.theResource = theResource;
		this.rectHere = new Rectangle(rectHere);
		if(this.rectHere.height <= 100)this.rectHere.height=100;
		currentLoType = theType;
		makeOpen = !CreateMinimiseStatus.instance().makeMin();
	}

	public void execute(){
		if(theResource instanceof VirtualResource)
			((VirtualResource)theResource).makeMeReal();
		//if(theNote == null)theNote = new NoteLucened();
		if(theNote == null)theNote = theResource.makeNewDisplayedNote();
		else theResource.introduceNewDisplayNote(theNote);
		//theNote.setIdentifiers("note:"+theNote.getALID());
		//if((theNote.getName() == null) || theNote.getName().trim().equals(""))
		//	theNote.setName(theResource.getName()+": New Note");
		if(theNote.getContent() == null)theNote.setContent("");
		theNote.setObjectType(NoteLucened.getNoteObjectType());
		theSurrogate = new LinkableObject(true);
		theSurrogate.setDisplayRectangle(rectHere);
		theSurrogate.setShowingMap(false);
		theSurrogate.setIsOpen(makeOpen);
		theSurrogate.setDisplPageNo(theResource.getCurrentPage());
		theSurrogate.reIntroduceMe();
		theSurrogate.setSurrogateFor(theNote);
		theSurrogate.setLoType(currentLoType);
		theSurrogate.setDisplayedIn(theResource);
	}
	
	public void undo(){
		theSurrogate.setDisplayedIn(null);
		theSurrogate.setSurrogateFor(null);
		theSurrogate.setLoType(null);

		theSurrogate.deleteMe();
		theObjectType = theNote.getObjectType();
		theNote.setObjectType(null);
		theNote.deleteMe();
	}
	
	public void redo(){
		theNote.reIntroduceMe();
		//theNote.setIdentifiers("note:"+theNote.getALID());
		theNote.setObjectType(theObjectType);
		theSurrogate.reIntroduceMe();
		currentLoType.reIntroduceMe(); // needed if this was deleted before the redo.
		theSurrogate.setLoType(currentLoType);
		theSurrogate.setSurrogateFor(theNote);
		theSurrogate.setDisplayedIn(theResource);
	}
	
	public NoteLucened getTheNote(){
		return theNote;
	}
	
	public LinkableObject getTheSurrogate(){
		return theSurrogate;
	}

}
