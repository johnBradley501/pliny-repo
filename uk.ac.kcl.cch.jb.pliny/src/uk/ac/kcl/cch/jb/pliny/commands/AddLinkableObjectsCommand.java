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
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.VirtualResource;

/**
 * This command provides the mechanism for the adding of LinkableObjects
 * that have been moved from one container to another (what GEF calls
 * 're-parenting').  Re-parenting in GEF involves two commands -- one
 * to remove the item from its old parent (called 'orphaning') and then a second
 * to reconnect the item to its new parent.  It is this second operation that
 * is defined here.
 * <p>
 * These commands are invoked from GEF policy objects method
 * <code>createAddCommand</code> in GEF's <code>XYLayoutEditPolicy</code>
 * or <code>ContainerEditPolicy</code>.  
 * 
 * @see uk.ac.kcl.cch.jb.pliny.policies.LinkableObjectContainerEditPolicy#createAddCommand
 * @see uk.ac.kcl.cch.jb.pliny.policies.ResourceObjectsXYLayoutPolicy#createAddCommand
 * 
 * @author John Bradley
 *
 */
public class AddLinkableObjectsCommand extends Command {
	
	LinkableObject newContainer;
	Resource resource;
	LinkableObject draggedObject;
	Rectangle rect, oldrect;
	int oldPageNumber = 0, newPageNumber = 0;

	/**
	 * the constructor used in the policy code to set up this command.
	 * 
	 * @param newContainer the new parent object
	 * @param resource the new parent objects 'surrogateFor' resource
	 * @param draggedObject the LinkableObject being dragged
	 * @param rect position for new linkable object
	 */
	public AddLinkableObjectsCommand(LinkableObject newContainer, Resource resource, LinkableObject draggedObject, Rectangle rect) {
		super("add objects");
		this.newContainer = newContainer;
		this.resource = resource;
		this.draggedObject = draggedObject;
		this.rect = rect;
		if(!draggedObject.getIsOpen())this.rect.height = draggedObject.getDisplayRectangle().height;
	}
	
	public void execute(){
		if(resource instanceof VirtualResource)
			((VirtualResource)resource).makeMeReal();
		//System.out.println("**newContainer: "+newContainer+"/"+newContainer.getSurrogateFor());
		//System.out.println("  resource: "+resource);
		//System.out.println("  draggedObject: "+draggedObject+"/"+draggedObject.getSurrogateFor());
		//System.out.println("  rect: "+rect);
		oldrect = draggedObject.getDisplayRectangle();
		draggedObject.setDisplayRectangle(rect);
		draggedObject.setDisplayedIn(resource);
		oldPageNumber = draggedObject.getDisplPageNo();
		newPageNumber = resource.getCurrentPage();
		draggedObject.setDisplPageNo(resource.getCurrentPage());
	}

	public void undo(){
		draggedObject.setDisplayRectangle(oldrect);
		draggedObject.setDisplayedIn(null);
		draggedObject.setDisplPageNo(oldPageNumber);
	}
	
	public void redo(){
		draggedObject.setDisplayRectangle(rect);
		draggedObject.setDisplPageNo(newPageNumber);
		draggedObject.setDisplayedIn(resource);
	}

}
