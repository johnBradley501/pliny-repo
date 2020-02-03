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
import java.util.Set;
import java.util.Vector;

import org.eclipse.gef.commands.Command;

import uk.ac.kcl.cch.jb.pliny.model.Link;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;

/**
 * used as a part of a compound command created by
 * {@link uk.ac.kcl.cch.jb.pliny.policies.ResourceObjectsXYLayoutPolicy#getAddCommand}
 * to handle all {@link uk.ac.kcl.cch.jb.pliny.model.Link}s between a set of 
 * {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject}s that are involved
 * in a move from one display resource to another.
 * 
 * @author John Bradley
 *
 */

public class FixupLinksCommand extends Command {
	
	Set linkableObjects = null;
	Vector removedLinks = new Vector();

	/**
	 * sets of this command to process the LinkableObjects contained
	 * within the given Set.  The set is assumed to contain Linkable
	 * Objects only.
	 * 
	 * @param linkableObjects set of LinkableObjects to work with.
	 */
	public FixupLinksCommand(Set linkableObjects) {
		super("fixup links");
		this.linkableObjects = linkableObjects;
	}
	
	public void execute(){
		Iterator it = linkableObjects.iterator();
		while(it.hasNext()){
			LinkableObject obj = (LinkableObject)it.next();
			handleLinkableObject(obj);
		}
	}
	
	private void handleLinkableObject(LinkableObject obj) {
		processLinkSet(obj.getLinkedFrom().getItems());
		processLinkSet(obj.getLinkedTo().getItems());
	}

	private void processLinkSet(Vector items) {
		Vector work = new Vector(items);
		Iterator it = work.iterator();
		while(it.hasNext()){
			Link l = (Link)it.next();
			if((!linkableObjects.contains(l.getFromLink())) ||
			   (!linkableObjects.contains(l.getToLink()))) removeLink(l);
		}
	}

	private void removeLink(Link l) {
		l.backupAndClearLinks();
		l.deleteMe();
		removedLinks.add(l);
	}

	public void undo(){
		Iterator it = removedLinks.iterator();
		while(it.hasNext()){
			Link l = (Link)it.next();
			l.reIntroduceMe();
			l.restoreLinks();
		}
	}
	
	public void redo(){
		Iterator it = removedLinks.iterator();
		while(it.hasNext()){
			Link l = (Link)it.next();
			l.backupAndClearLinks();
			l.deleteMe();
		}
	}

}
