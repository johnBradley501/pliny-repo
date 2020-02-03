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
import uk.ac.kcl.cch.jb.pliny.model.ObjectType;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.VirtualResource;

/**
 * Creates a reference to a given Resource on an reference/annotation area.
 * It is invoked from a GEF policy
 * {@link uk.ac.kcl.cch.jb.pliny.policies.ScalableImageXYLayoutEditPolicy#getCreateCommand}.
 * 
 * @author John Bradley
 *
 */
public class CreateResourceCommand extends Command {

	private Resource newResource;
	private ObjectType theObjectType;
	private Resource theResource;
	private Rectangle rectHere;
	private LinkableObject theSurrogate = null;
	private LOType currentLoType;
	private boolean makeOpen = true;

	/**
	 * creates the command and sets parameters with which it is to work
	 * @param newResource the Resource the reference should point it.  Cannot be <code>null</code>
	 * @param theResource Resource in whos reference/annotation area it is to appear
	 * @param rectHere where it is to appear there
	 */
	public CreateResourceCommand(Resource newResource, Resource theResource, Rectangle rectHere) {
		super("Create Reference");
		this.newResource = newResource;
		this.theResource = theResource;
		this.rectHere = new Rectangle(rectHere);
		if(rectHere.height <= 100)rectHere.height=100;
		currentLoType = LOType.getCurrentType();
		makeOpen = !CreateMinimiseStatus.instance().makeMin();
	}

	public void execute(){
		if(theResource instanceof VirtualResource)
			((VirtualResource)theResource).makeMeReal();
		if(newResource instanceof VirtualResource)
			((VirtualResource)newResource).makeMeReal();
		if(theSurrogate == null){
			theSurrogate = new LinkableObject();
			theSurrogate.setSurrogateFor(newResource);
			theSurrogate.setDisplayRectangle(rectHere);
			theSurrogate.setLoType(currentLoType);
		} else theSurrogate.reIntroduceMe();
		theSurrogate.setShowingMap(false);
		theSurrogate.setIsOpen(makeOpen);
		theSurrogate.setDisplPageNo(theResource.getCurrentPage());
		theSurrogate.setDisplayedIn(theResource);
	}
	
	public void undo(){
		theSurrogate.setDisplayedIn(null);
		theSurrogate.setSurrogateFor(null);
		theSurrogate.setLoType(null);

		theSurrogate.deleteMe();
		theObjectType = newResource.getObjectType();
		newResource.setObjectType(null);
		newResource.deleteMe();
	}
	
	public void redo(){
		newResource.reIntroduceMe();
		//theNote.setIdentifiers("resource:"+theNote.getALID());
		newResource.setObjectType(theObjectType);
		theSurrogate.reIntroduceMe();
		theSurrogate.setLoType(currentLoType);
		theSurrogate.setSurrogateFor(newResource);
		theSurrogate.setDisplayedIn(theResource);
	}
}
