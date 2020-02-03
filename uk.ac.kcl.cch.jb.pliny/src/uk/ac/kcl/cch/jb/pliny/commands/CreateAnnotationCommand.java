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
import uk.ac.kcl.cch.jb.pliny.model.Link;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.ObjectType;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.VirtualResource;

/**
 * Creates a new annotation in a Pliny reference or annotation area.
 * It is invoked from a GEF policy
 * {@link uk.ac.kcl.cch.jb.pliny.policies.ScalableImageXYLayoutEditPolicy#getCreateCommand}.
 * An annotation is an anchor, a note with a note reference
 * and a connection line between them.
 * @author John Bradley
 *
 */


public class CreateAnnotationCommand extends Command {
	
	private Resource resource;
	private Rectangle bounds;
	private LinkableObject anchor, noteSurrogate;
	private NoteLucened note;
	private Link link;
	private boolean makeOpen = true;
	private LOType currentLoType;
	
	/**
	 * constructor for command to create a new annotation.
	 * 
	 * @param resource Resource who will own the annotation
	 * @param bounds Rectangle where the anchor is to be placed.
	 */

	public CreateAnnotationCommand(Resource resource, Rectangle bounds) {
		super("create Annotation");
		this.resource = resource;
		this.bounds = bounds;
		noteSurrogate = null;
		note = null;
		link = null;
		currentLoType = LOType.getCurrentType();
		makeOpen = !CreateMinimiseStatus.instance().makeMin();
	}
	
	public void execute(){
		if(resource instanceof VirtualResource)
			((VirtualResource)resource).makeMeReal();
		anchor = new LinkableObject();
		createNotePieces();
		anchor.setLoType(currentLoType);
		anchor.setDisplayRectangle(bounds);
		anchor.setDisplayedIn(resource);
		anchor.setDisplPageNo(resource.getCurrentPage());
		noteSurrogate.setDisplPageNo(resource.getCurrentPage());
		noteSurrogate.setDisplayedIn(resource);
	}
	
	private void createNotePieces() {
		//if(note == null)note = new NoteLucened();
		if(note == null)note = resource.makeNewDisplayedNote();
		else note.reIntroduceMe();
		//note.setName("New Note");
		note.setContent("");
		note.setObjectType(ObjectType.getItem(1));
		
		if(noteSurrogate == null)noteSurrogate = new LinkableObject();
		else noteSurrogate.reIntroduceMe();
		Rectangle surrogateBounds = bounds.getTranslated(150,-(CreateLinkableObjectCommand.DEFAULT_NOTE_DIMENSION.height+50));
		if(surrogateBounds.y < 0)surrogateBounds.y = 0;
		//surrogateBounds.setSize(150,100);
		surrogateBounds.setSize(CreateLinkableObjectCommand.DEFAULT_NOTE_DIMENSION);
		noteSurrogate.setLoType(currentLoType);
		noteSurrogate.setDisplayRectangle(surrogateBounds);
		noteSurrogate.setSurrogateFor(note);
		noteSurrogate.setShowingMap(false);
		noteSurrogate.setIsOpen(makeOpen);
		
		if(link == null)link = new Link();
		else link.reIntroduceMe();
		link.setLoType(LOType.getCurrentType());
		link.setFromLink(anchor);
		link.setToLink(noteSurrogate);
	}

	public void undo(){
		anchor.setDisplayedIn(null);
		anchor.setLoType(null);
		noteSurrogate.setDisplayedIn(null);
		noteSurrogate.setSurrogateFor(null);
		noteSurrogate.setLoType(null);
		link.setFromLink(null);
		link.setToLink(null);
		link.setLoType(null);
		link.deleteMe();
		note.setObjectType(null);
		note.deleteMe();
		noteSurrogate.deleteMe();
		anchor.deleteMe();
	}
}
