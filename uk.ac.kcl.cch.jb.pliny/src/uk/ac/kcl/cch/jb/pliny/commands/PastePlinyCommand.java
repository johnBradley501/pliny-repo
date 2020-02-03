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

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

import uk.ac.kcl.cch.jb.pliny.actions.CreateMinimiseStatus;
import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.Link;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.Note;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.VirtualResource;

/**
 * handles the paste part of a copy/paste operation, for pasting into
 * the reference/annotation area.
 * @author John Bradley
 *
 */

public class PastePlinyCommand extends Command {
	
	private Resource myMainResource;
	private Collection items;
	private Vector createdObjects = new Vector();
	private Vector createdLinks = new Vector();
	private Hashtable linkedObjects;
	private boolean allowAnchors;
	private boolean makeOpen = true;

	/**
	 * constructs an instance of this command so that the collecion of
	 * objects in <code>items</code> is placed in the reference/annotation
	 * area for the resource <code>myMainResource</code>. The list
	 * of items in <code>items</code> may contain either {@link uk.ac.kcl.cch.jb.pliny.model.Resource}s
	 * or {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject}s.  Anchor objects (LinkableObjects that
	 * do not have a Surrogate) are not copied when the command is created
	 * with this constructor.
	 * 
	 * @param myMainResource destination Resource for the objects
	 * @param items list of Resources or LinkableObjects to be pasted.
	 */
	
	public PastePlinyCommand(Resource myMainResource, Collection items) {
		super("Paste");
		this.myMainResource = myMainResource;
		this.items = items;
		allowAnchors = false;
		makeOpen = !CreateMinimiseStatus.instance().makeMin();
	}

	/**
	 * constructs an instance of this command so that the collecion of
	 * objects in <code>items</code> is placed in the reference/annotation
	 * area for the resource <code>myMainResource</code>. The list
	 * of items in <code>items</code> may contain either {@link uk.ac.kcl.cch.jb.pliny.model.Resource}s
	 * or {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject}s.  Anchors
	 * will be copied if <code>allowAnchors</code> is <code>true</code>.
	 * 
	 * @param myMainResource destination Resource for the objects
	 * @param items list of Resources or LinkableObjects to be pasted.
	 * @param allowAnchors set <code>true</code> to request pasting of Anchor type
	 * LinkableObjects.
	 */

	public PastePlinyCommand(Resource myMainResource, Collection items, boolean allowAnchors) {
		super("Paste");
		this.myMainResource = myMainResource;
		this.items = items;
		this.allowAnchors = allowAnchors;
		makeOpen = !CreateMinimiseStatus.instance().makeMin();
	}
	
	public void execute(){
		if(myMainResource instanceof VirtualResource)
			((VirtualResource)myMainResource).makeMeReal();
		//System.out.println("Paste items:"+items);
		linkedObjects = new Hashtable();
		Iterator it = items.iterator();
		int offset = 0;
		while(it.hasNext()){
			Object obj = it.next();
			if(obj instanceof Resource)
				handleResource((Resource)obj, ++offset);
			else if(obj instanceof LinkableObject)
				handleLinkableObject((LinkableObject)obj, ++offset);
		}
		
		it = items.iterator();
		while(it.hasNext()){
			Object currentObj = it.next();
			if(currentObj instanceof Link){
				handleLink((Link)currentObj);
			}
		}
		linkedObjects = null;
	}

	private void handleLink(Link oldLink) {
		LinkableObject from = (LinkableObject)linkedObjects.get(oldLink.getFromLink());
		LinkableObject to = (LinkableObject)linkedObjects.get(oldLink.getToLink());
		if((from == null) || (to == null)) return;
		Link newLink = new Link();
		newLink.setAttributes(oldLink.getAttributes());
		newLink.setLoType(oldLink.getLoType());
		newLink.setFromLink(from);
		newLink.setToLink(to);
		createdLinks.add(newLink);
	}

	private void handleLinkableObject(LinkableObject object, int offset) {
		if(object.getDisplayedIn() != null && object.getDisplayedIn().getALID() == myMainResource.getALID())return;
		Resource myResource = object.getSurrogateFor();
		if(myResource == null)myResource = object.getHeldSurrogate();
		if((!allowAnchors) && (myResource == null))return;
		myResource.reIntroduceMe();
		LinkableObject rslt = new LinkableObject(true);
		rslt.setDisplayRectangle(object.getDisplayRectangle());
		boolean tobeOpen = object.getIsOpen();
		if(!makeOpen)tobeOpen = false;
		rslt.setIsOpen(tobeOpen);
		rslt.setShowingMap(object.getShowingMap());
		rslt.reIntroduceMe();
		
		LOType myType = object.getLoType();
		if(myType == null)myType = object.getHeldLoType();
		if(myType == null)myType = LOType.getCurrentType();

		rslt.setLoType(myType);
		rslt.setSurrogateFor(myResource);
		rslt.setDisplayedIn(myMainResource);
		createdObjects.add(rslt);
		linkedObjects.put(object, rslt);
	}

	private void handleResource(Resource resource, int offset) {
		LinkableObject rslt = new LinkableObject();
		rslt.setDisplayRectangle(new Rectangle(offset*15, offset*15, 150, 100));
		rslt.setIsOpen(true);
		if(resource instanceof Note)rslt.setShowingMap(false);
		rslt.setLoType(LOType.getCurrentType());
		rslt.setSurrPageNo(resource.getCurrentPage());
		rslt.setSurrogateFor(resource);
		rslt.setIsOpen(makeOpen);
		rslt.setDisplayedIn(myMainResource);
		createdObjects.add(rslt);		
	}
	
	public void undo(){
		Iterator it = createdLinks.iterator();
		while(it.hasNext()){
			Link currentObj = (Link)it.next();
			currentObj.backupAndClearLinks();
			currentObj.deleteMe();
		}
		it = createdObjects.iterator();
		while(it.hasNext()){
			LinkableObject currentObj = (LinkableObject)it.next();
			currentObj.storeSurrogate();
			currentObj.setDisplayedIn(null);
			currentObj.setSurrogateFor(null);
			currentObj.storeLoType();
			currentObj.setLoType(null);
			currentObj.deleteMe();
		}
	}
	
	public void redo(){
		Iterator it = createdObjects.iterator();
		while(it.hasNext()){
			LinkableObject currentObj = (LinkableObject)it.next();
			currentObj.reIntroduceMe();
			currentObj.setSurrogateFor(currentObj.getHeldSurrogate());
			currentObj.setLoType(currentObj.getHeldLoType());
			currentObj.setDisplayedIn(myMainResource);
		}
		it = createdLinks.iterator();
		while(it.hasNext()){
			Link currentObj = (Link)it.next();
			currentObj.reIntroduceMe();
			currentObj.restoreLinks();
		}
	}

}
