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

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

import uk.ac.kcl.cch.jb.pliny.actions.CreateMinimiseStatus;
import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.VirtualResource;

/**
 * places a new {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject}
 * in the reference/annotation area of a given
 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource}.   The LinkableObject has been already
 * created by the GEF CreateTool.  It is the job of this
 * command to place it in the appropriate place.
 * <p>
 * This used in the GEF policy
 * {@link uk.ac.kcl.cch.jb.pliny.policies.ResourceObjectsXYLayoutPolicy#getCreateCommand} and
 * {@link uk.ac.kcl.cch.jb.pliny.policies.ScalableImageXYLayoutEditPolicy#getCreateCommand}
 * @author John Bradley
 *
 */
public class CreateLinkableObjectCommand extends Command {
	
	private LinkableObject object;
	private Resource theContainer;
	private Resource theSurrogate;
	private Rectangle rectHere;
	private LOType currentLoType;
	/**
	 * a standard dimension for a new LinkableObject reference when open
	 */
	public static Dimension DEFAULT_NOTE_DIMENSION = new Dimension(200,100);
	/**
	 * a standard dimension for a new LinkableObject reference when closed
	 */
	public static Dimension DEFAULT_DIMENSION = new Dimension(200,18);

	/**
	 * constructor to place given {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject} within the
	 * resource/annotation area of a given Resource.
	 * 
	 * @param object LinkableObject created by the CreationTool
	 * @param theContainer Resource in which it should appear
	 * @param rectangle place in the area where it should appear
	 */
	public CreateLinkableObjectCommand(LinkableObject object, Resource theContainer, Rectangle rectangle) {
		super("Create Surrogate");
		this.object = object;
		object.setIsOpen(!CreateMinimiseStatus.instance().makeMin());
		this.theContainer = theContainer;
		this.theSurrogate = object.getSurrogateFor();
		rectHere = new Rectangle(rectangle);
		Point putHere = rectangle.getTopLeft();
		String position = object.getPosition();
		if((position == null) || (position.trim().equals(""))){
			   object.setDisplayRectangle(rectHere);
		} else {
			Rectangle givenRect = object.getDisplayRectangle();
			givenRect.setLocation(putHere);
			object.setDisplayRectangle(givenRect);
		}
		currentLoType = LOType.getCurrentType();
	}

	public void execute(){
		if(theContainer instanceof VirtualResource){
			((VirtualResource)theContainer).makeMeReal();
		}
		if(theSurrogate == null){
			theSurrogate = theContainer.makeNewDisplayedNote();
			//theSurrogate = new NoteLucened();
			//theSurrogate.setName("New Note");
		}
		object.reIntroduceMe();
		object.setLoType(currentLoType);
		object.setSurrogateFor(theSurrogate);
		object.setDisplPageNo(theContainer.getCurrentPage());
		object.setDisplayedIn(theContainer); // was before setDisplPageNo  j.b.
	}
	
	public void undo(){
		object.setLoType(null);
		object.setDisplayedIn(null);
		object.setSurrogateFor(null);
		object.deleteMe();
	}
	
	public void redo(){
		object.reIntroduceMe();
		object.setLoType(currentLoType);
		object.setSurrogateFor(theSurrogate);
		object.setDisplayedIn(theContainer);
	}
}
