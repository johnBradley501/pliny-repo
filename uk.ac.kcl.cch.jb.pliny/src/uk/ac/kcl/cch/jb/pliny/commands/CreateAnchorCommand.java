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

import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.VirtualResource;

/**
 * Creates a new anchor in a Pliny reference or annotation area.
 * It is invoked from a GEF policy
 * {@link uk.ac.kcl.cch.jb.pliny.policies.ScalableImageXYLayoutEditPolicy#getCreateCommand}
 * @author John Bradley
 *
 */

public class CreateAnchorCommand extends Command {
	private Resource resource;
	private Rectangle bounds;
	private LinkableObject anchor;
	private LOType currentLoType;

	/**
	 * creates a new anchor (a kind of {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject})
	 * in the reference/annotation area associated with a 
	 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource}.  This is done
	 * by setting the display area to the given bounds.  Anchors are
	 * LinkableObjects that don't have a SurrogateFor set.
	 * 
	 * @param resource Resource to be owner of the new anchor
	 * @param bounds area where the bounds are to appear.
	 */
	public CreateAnchorCommand(Resource resource, Rectangle bounds) {
		super("create Anchor");
		this.resource = resource;
		this.bounds = bounds;
		currentLoType = LOType.getCurrentType();
	}
	
	public void execute(){
		if(resource instanceof VirtualResource)
			((VirtualResource)resource).makeMeReal();
		anchor = new LinkableObject();
		anchor.setDisplayRectangle(bounds);
		anchor.setLoType(currentLoType);
		anchor.setDisplPageNo(resource.getCurrentPage());
		anchor.setDisplayedIn(resource);
	}
	
	public void undo(){
		anchor.setDisplayedIn(null);
		anchor.setLoType(null);
		anchor.deleteMe();
	}
}
