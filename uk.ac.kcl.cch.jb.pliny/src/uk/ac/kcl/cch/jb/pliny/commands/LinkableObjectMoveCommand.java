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

import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;

/**
 * 
 * moves the display position and resizes a {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject}
 * in its reference area.  This command is not used
 * when the drag results in LinkableObject having to change owning resource.
 * 
 * @author John Bradley
 *
 */

public class LinkableObjectMoveCommand extends Command {

	private LinkableObject surrogate;
	private Rectangle oldBounds;
	private Rectangle newBounds;
	
	/**
	 * creates command to move the given object to the location and size
	 * given by the newBounds.
	 * 
	 * @param object object to be moved/resized
	 * @param oldBounds old position/size (saved for undo)
	 * @param newBounds new position/size
	 */

	public LinkableObjectMoveCommand(LinkableObject object, Rectangle oldBounds, Rectangle newBounds) {
		super("Move/Resize");
		this.surrogate = object;
		this.oldBounds = new Rectangle(oldBounds);
		this.newBounds = new Rectangle(newBounds);
		if(!surrogate.getIsOpen())
			this.newBounds.height = surrogate.getDisplayRectangle().height;
		//System.out.println("Move: getIsOpen(): "+surrogate.getIsOpen()+", displayRectangle: "+surrogate.getDisplayRectangle()+
		//		", newBounds: "+this.newBounds);
	}
	
	private void setCoord(Rectangle r){
		Rectangle r1 = new Rectangle(r);
		//if((surrogate.getSurrogateFor() != null) && (!surrogate.getIsOpen()))
		//	r1.height = 18;
		surrogate.setDisplayRectangle(r1);
	}
	
	public void execute(){
		setCoord(newBounds);
	}
	
	public void undo(){
		setCoord(oldBounds);
	}
	
}
