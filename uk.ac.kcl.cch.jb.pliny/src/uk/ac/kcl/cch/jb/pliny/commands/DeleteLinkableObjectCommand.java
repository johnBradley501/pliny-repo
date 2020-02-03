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

import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.Link;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.ObjectType;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * deletes a 
 * {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject} item. It also
 * checks the surrogate note attached to it, and if this object is the
 * note's only reference, deletes the note as well.  Also, links between
 * this object and other objects are deleted.
 * 
 * @author John Bradley
 *
 */

public class DeleteLinkableObjectCommand extends Command {

	private LinkableObject lo;
	private Resource shownIn;
	private Resource myResource;
	private LOType myLoType;
	private ObjectType myObjectType = null;
	protected Vector myToLinks, myFromLinks;
	
	public DeleteLinkableObjectCommand(LinkableObject lo) {
		//super("Delete Surrogate");
		super();
		if(lo.getSurrogateFor() == null)setLabel("delete anchor");
		else setLabel("delete reference object");
		this.lo = lo;
		shownIn = lo.getDisplayedIn();
		myToLinks = new Vector();
		myFromLinks = new Vector();
	}
	
	private Vector buildLinkedList(Vector thelist){
		Vector linklist = new Vector(thelist);
		Vector rslt = new Vector();
        Iterator it =linklist.iterator();
        int targetID = lo.getALID();
        while(it.hasNext()){
        	Link theLink = (Link)it.next();
        	if (theLink.getFromLink().getALID() == targetID)
        		rslt.add(theLink.getToLink());
        	else rslt.add(theLink.getFromLink());
        	theLink.setFromLink(null);
        	theLink.setToLink(null);
        	rslt.add(theLink.getLoType());
        	rslt.add(theLink.getAttributes());
        	theLink.deleteMe();
        }
        return rslt;
	}
	
	public void execute(){
		myResource = lo.getSurrogateFor();
        lo.setDisplayedIn(null);
        if(myResource != null){
        	myObjectType = myResource.getObjectType();
        	lo.setSurrogateFor(null);
        	if(myResource.isDeletable()){
        		myResource.setObjectType(null);
        		myResource.deleteMe();
        	}
        }
        myLoType = lo.getLoType();
        lo.setLoType(null);
        myToLinks = buildLinkedList(lo.getLinkedTo().getItems());
        myFromLinks = buildLinkedList(lo.getLinkedFrom().getItems());
        lo.deleteMe();
	}
	
	private void restoreLinks(Vector list, boolean isFrom){
		Iterator it = list.iterator();
		while(it.hasNext()){
			LinkableObject theLinkedOne = (LinkableObject)it.next();
			LOType theType = (LOType)it.next();
			Link theLink = new Link();
			theLink.setLoType(theType);
			if(isFrom){
				theLink.setFromLink(theLinkedOne);
				theLink.setToLink(lo);
			} else {
				theLink.setFromLink(lo);
				theLink.setToLink(theLinkedOne);
			}
			theLink.setAttributes((String)it.next());
		}
	}
	
	public void undo(){
		if(lo == null)return;
		lo.reIntroduceMe();
		restoreLinks(myFromLinks, true);
		restoreLinks(myToLinks, false);
		if(myResource != null){
			myResource.reIntroduceMe();
			myResource.setObjectType(myObjectType);
			lo.setSurrogateFor(myResource);
		}
		myLoType.reIntroduceMe(); // needed if myLoType was deleted in the Type Manager
		lo.setLoType(myLoType);
		lo.setDisplayedIn(shownIn);
	}
}
