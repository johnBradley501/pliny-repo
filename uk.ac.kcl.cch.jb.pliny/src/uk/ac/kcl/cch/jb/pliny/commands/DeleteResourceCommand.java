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

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.gef.commands.Command;

import uk.ac.kcl.cch.jb.pliny.model.Link;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.ObjectType;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.rdb2java.dynData.FKReferenceList;

/**
 * deletes a 
 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource} item. It does a "deep"
 * deletion -- also deleting any LinkableObjects linked to it either
 * through their <code>surrogateFore</code> or <code>displayedIn</code>
 * links, and then any Links that connect deleted LinkableObjects.  This
 * command is invoked through the Resource Explorer.
 * 
 * @author John Bradley
 *
 */

public class DeleteResourceCommand extends Command {
	private Resource resource;
	private ObjectType myObjectType;
	private Vector linkableObjects;
	private Vector links;

	/***
	 * constructs an instance of this command.  The resource type
	 * parameter is used so that the label associated with this
	 * instance can contain the type of resource (Note, ImageResource,
	 * etc).
	 * 
	 * @param resource the resource to be deleted
	 * @param resourceType the name of the type of resource
	 */
	
	public DeleteResourceCommand(Resource resource, String resourceType) {
		super("delete "+resourceType);
		this.resource = resource;
	}

	public void execute(){
		linkableObjects = new Vector();
		links = new Vector();
		processLOsForLinks(resource.getMyDisplayedItems());
		processLOsForLinks(resource.getMySurrogates());
		deleteLinkableObjects();
		myObjectType = resource.getObjectType();
		resource.setObjectType(null);
		resource.deleteMe();
	}
	
	private void deleteLinkableObjects() {
		Iterator it = linkableObjects.iterator();
		while(it.hasNext()){
			LinkableObject obj = (LinkableObject)it.next();
			obj.backupAndClearResLinks();
			obj.deleteMe();
		}
		
	}

	private void processLOsForLinks(FKReferenceList myItems) {
		Vector items = new Vector(myItems.getItems());
		Iterator it = items.iterator();
		while(it.hasNext()){
			LinkableObject obj = (LinkableObject)it.next();
			deleteLinks(obj.getLinkedFrom());
			deleteLinks(obj.getLinkedTo());
			linkableObjects.add(obj);
		}
	}

	private void deleteLinks(FKReferenceList linkList) {
		Vector items = new Vector(linkList.getItems());
		Iterator it = items.iterator();
		while(it.hasNext()){
			Link link = (Link)it.next();
			link.backupAndClearLinks();
			links.add(link);
			link.deleteMe();
		}
	}

	public void undo(){
		resource.reIntroduceMe();
		reinstateLinkableObjects();
		reinstateLinks();
		resource.setObjectType(myObjectType);
	}

	private void reinstateLinks() {
		Iterator it = links.iterator();
		while(it.hasNext()){
			Link obj = (Link)it.next();
			obj.reIntroduceMe();
			obj.restoreLinks();
		}
	}

	private void reinstateLinkableObjects() {
		Iterator it = linkableObjects.iterator();
		while(it.hasNext()){
			LinkableObject obj = (LinkableObject)it.next();
			obj.reIntroduceMe();
			obj.restoreResLinks();
		}
	}
}
