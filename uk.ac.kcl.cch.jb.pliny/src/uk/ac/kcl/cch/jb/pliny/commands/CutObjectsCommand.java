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
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractEditPart;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.dnd.ClipboardHandler;
import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.Link;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * This command supports the use of the "cut" operation within
 * a reference/annotation area.
 * 
 * @author John Bradley
 *
 */

public class CutObjectsCommand extends Command {

	List items;
	Resource mainResource = null;
	Set openIds;
	Vector cutLinkableObjects = new Vector();
	Vector cutLinks = new Vector();

	/**
	 * constructor for the Cut command.  The list it is passed
	 * must contain GEF AbstractEditParts of objects to be cut,a
	 * and the parts an be references to
	 * model items {@link uk.ac.kcl.cch.jb.pliny.model.Link}s
	 * or {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject}s.
	 * 
	 * @param items List of AbstractEditParts referring to items to be cut.
	 */
	
	public CutObjectsCommand(List items) {
		super("cut");
		this.items = items;
	}
	
	public void execute(){
		   cutLinkableObjects = new Vector();
		   cutLinks = new Vector();
		   openIds = PlinyPlugin.getDefault().getOpenNotesIds();  // see use in "isDeletable()"
		   Iterator it = items.iterator();
		   while(it.hasNext())handleModelPart(it.next());
		   Vector selectedModelItems = new Vector(cutLinkableObjects);
		   selectedModelItems.addAll(cutLinks);
		   ClipboardHandler.getDefault().setContents(selectedModelItems);

	}

   private void handleModelPart(Object selPart) {
      if(!(selPart instanceof AbstractEditPart))return;
	   Object myModel = ((AbstractEditPart)selPart).getModel();
	   if(myModel == null)return;
	   if(myModel instanceof LinkableObject)
		   handleLinkableObject((LinkableObject)myModel);
	   else if(myModel instanceof Link)
		   handleLink((Link)myModel);
   }

   private void handleLink(Link link) {
	   if(link.getALID() == 0)return; // already deleted
	   link.backupAndClearLinks();
	   link.deleteMe();
	   cutLinks.add(link);
   }
   
	private Vector buildLinkedList(LinkableObject lo, Vector thelist){
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
	
	/*
	private boolean isDeletable(Resource theResource){
		/* The resource associated with a LinkableObject we are
		 * asked to delete is also deletable if (a) it has no other
		 * surrogates that points to it, (b) there is no editor currently
		 * open the refers to it, and (c) it has no objects that it "contains"#
		 * itself. * /
		if(theResource.getMySurrogates().getItems().size() != 0) return false;
		Set openIds = PlinyPlugin.getDefault().getOpenNotesIds();
		if(openIds.contains(new Integer(theResource.getALID()))) return false;
		if(theResource.getMyDisplayedItems().getItems().size() != 0)return false;
		return true;
	}
*/


   private void handleLinkableObject(LinkableObject object) {
	   if((mainResource == null)  && (object.getDisplayedIn() != null))
		   mainResource = object.getDisplayedIn();
	   if(object.getALID() == 0)return; // already deleted
	   object.setDisplayedIn(null);
	   object.storeSurrogate();
	   Resource mySurrogate = object.getSurrogateFor();
       if(mySurrogate != null){
       	    object.setSurrogateFor(null);
       	    //if(isDeletable(mySurrogate))mySurrogate.deleteMe();
       	    if(mySurrogate.isDeletable())mySurrogate.deleteMe();
       }
       object.storeLoType();
       object.setLoType(null);
	   //object.setSurrogateFor(null);
       Vector myToLinks = buildLinkedList(object, object.getLinkedTo().getItems());
       Vector myFromLinks = buildLinkedList(object, object.getLinkedFrom().getItems());
       object.deleteMe();
       cutLinkableObjects.add(object);
       cutLinkableObjects.add(myToLinks);
       cutLinkableObjects.add(myFromLinks);
   }
   
   private void restoreLinkableObject(LinkableObject lo){
	   lo.reIntroduceMe();
	   lo.setLoType(lo.getHeldLoType());
	   Resource myResource = lo.getHeldSurrogate();
	   if(myResource != null){
	     myResource.reIntroduceMe();
	     lo.setSurrogateFor(myResource);
	   }
	   lo.setDisplayedIn(mainResource);
   }
	
	private void restoreLinks(LinkableObject lo, Vector list, boolean isFrom){
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
		//ClipboardHandler.getDefault().setContents(Collections.EMPTY_LIST);

		Iterator it = cutLinkableObjects.iterator();
		while(it.hasNext()){
			LinkableObject lo = (LinkableObject)it.next();
			Vector myToLinks = (Vector)it.next(); // ignored here
			Vector myFromLinks = (Vector)it.next(); // ignored here
			restoreLinkableObject(lo);
		}
		it = cutLinkableObjects.iterator();
		while(it.hasNext()){
			LinkableObject lo = (LinkableObject)it.next();
			Vector myToLinks = (Vector)it.next();
			Vector myFromLinks = (Vector)it.next();
			restoreLinks(lo, myFromLinks, false);
			restoreLinks(lo, myToLinks, true);
		}
		it = cutLinks.iterator();
		while(it.hasNext()){
			Link link = (Link)it.next();
			link.reIntroduceMe();
			link.restoreLinks();
		}
	}
}
