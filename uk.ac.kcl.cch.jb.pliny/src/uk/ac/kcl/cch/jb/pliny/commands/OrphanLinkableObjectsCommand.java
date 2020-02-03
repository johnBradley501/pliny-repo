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
package uk.ac.kcl.cch.jb.pliny.commands;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.gef.commands.Command;

import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * This command provides the mechanism for the orphaning of LinkableObjects
 * that are being moved from one container to another (what GEF calls
 * 're-parenting').  Re-parenting in GEF involves two commands -- one
 * to remove the item from its old parent (called 'orphaning') and then a second
 * to reconnect the item to its new parent.  It is this first operation that
 * is defined here.
 * <p>
 * These commands are invoked from GEF policy objects method
 * <code>getOrphanChildrenCommand</code> in GEF's <code>ContainerEditPolicy</code>.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.policies.ResourceObjectsContainerEditPolicy#getOrphanChildrenCommand
 * 
 * @author John Bradley
 */
public class OrphanLinkableObjectsCommand extends Command {
	
	Vector linkableObjects = null;
	Resource displayedIn = null;
	Hashtable theCoords;

	/**
	 * the constructor used in the policy code to set up this command.
	 * 
	 * @param linkableObjects collection of LinkableOjects that need to
	 * be orphaned in preparation for the subsequent reparenting.
	 */
	public OrphanLinkableObjectsCommand(Vector linkableObjects) {
		super("drag into container");
		this.linkableObjects = linkableObjects;
		theCoords = new Hashtable();
	}
	
	public void execute(){
		Iterator it = linkableObjects.iterator();
		while(it.hasNext()){
			LinkableObject obj = (LinkableObject)it.next();
			theCoords.put(obj, obj.getPosition());
			if(displayedIn == null){
				displayedIn = obj.getDisplayedIn();
			}
			obj.setDisplayedIn(null);
		}
	}

	public void undo(){
		Iterator it = linkableObjects.iterator();
		while(it.hasNext()){
			LinkableObject obj = (LinkableObject)it.next();
			String theCoord = (String)theCoords.get(obj);
			if(theCoord != null)obj.setPosition(theCoord);
			obj.setDisplayedIn(displayedIn);
		}
	}
}
