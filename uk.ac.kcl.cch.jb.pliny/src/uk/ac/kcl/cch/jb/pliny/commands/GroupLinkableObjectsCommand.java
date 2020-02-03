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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.Link;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * groups a Vector of {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject}s by first creating a new note to hold
 * them and putting the group into the new note's reference area
 * and then creating a new reference object (LinkableObject) for the new note in the
 * place where the LinkableObjects came from.
 * 
 * @author John Bradley
 *
 */

public class GroupLinkableObjectsCommand extends Command {

	private Vector list, list2 = null;
	private Vector rects = null;
	private Vector links = null;
	private Resource parent = null;
	private int currentPage = 0;
	private NoteLucened theNote;
	private LinkableObject theSurrogate = null;
	private LOType currentLoType;
	boolean wasDone = false;
	
	/**
	 * creates an instance of this command ready to group together
	 * LinkableObjects found in the given list.  The code assumes that
	 * only LinkableObjects are in the list.
	 * 
	 * @param list a collection of LinkableObjects to be grouped.
	 */
	public GroupLinkableObjectsCommand(Vector list) {
		super("Grouping objects");
		this.list = list;
		currentLoType = LOType.getCurrentType();
	}

	public void execute(){
		Rectangle containerSize = calculateContainerSize();
		if(containerSize == null)return;
		
		wasDone = true;

		if(theNote == null)theNote = new NoteLucened();
		else theNote.reIntroduceMe();

		if((theNote.getName() == null) || theNote.getName().trim().equals(""))
			theNote.setName(parent.getName()+": New Group");
		if(theNote.getContent() == null)theNote.setContent("");
		theNote.setObjectType(NoteLucened.getNoteObjectType());

		if(theSurrogate == null){
			theSurrogate = new LinkableObject();
			theSurrogate.setSurrogateFor(theNote);
			theSurrogate.setLoType(currentLoType);
		} else theSurrogate.reIntroduceMe();
		theSurrogate.setDisplayRectangle(containerSize);
		theSurrogate.setShowingMap(true);
		theSurrogate.setDisplPageNo(parent.getCurrentPage());
		
		insertObjects(list, theSurrogate, containerSize);
		
		currentPage = parent.getCurrentPage();
		theSurrogate.setDisplPageNo(parent.getCurrentPage());
		theSurrogate.setDisplayedIn(parent);
	}
	
	private void handleLinksExe(LinkableObject obj, Vector myLinks, Set surrogateLinked, boolean from){
		Vector theLinks = null;
		if(from)theLinks = new Vector(obj.getLinkedFrom().getItems());
		else theLinks = new Vector(obj.getLinkedTo().getItems());
		Iterator it = theLinks.iterator();
		while(it.hasNext()){
			Link link = (Link)it.next();
			if(link.getFromLink() == obj){
				LinkableObject end = link.getToLink();
				if(!list2.contains(end)){
					myLinks.add(link);
					if(surrogateLinked.contains(end))deleteLink(link);
					else {
					   link.setFromLink(theSurrogate);
					   surrogateLinked.add(end);
					}
				}
			} else {
				LinkableObject end = link.getFromLink();
				if(!list2.contains(end)){
					myLinks.add(link);
					if(surrogateLinked.contains(end))deleteLink(link);
					else {
					   link.setToLink(theSurrogate);
					   surrogateLinked.add(end);
					}
				}
			}
		}
	}
	
	private void deleteLink(Link link) {
		link.backupAndClearLinks();
		link.deleteMe();
	}

	private void insertObjects(Vector list, LinkableObject theSurrogate, Rectangle containerSize) {
		Point origin = containerSize.getTopLeft().getNegated();
		rects = new Vector();
		links = new Vector();
		
		Iterator it = list2.iterator();
		Set surrogateLinked = new HashSet();
		while(it.hasNext()){
			LinkableObject obj = (LinkableObject)it.next();
			Vector myLinks = new Vector();
			links.add(myLinks);
			handleLinksExe(obj, myLinks, surrogateLinked, true);
			handleLinksExe(obj, myLinks, surrogateLinked, false);
			rects.add(obj.getDisplayRectangle());
			Rectangle newRect = obj.getDisplayRectangle().getTranslated(origin);
			obj.setDisplayRectangle(newRect);
			obj.setDisplPageNo(0);
			obj.setDisplayedIn(theNote);
		}
		
	}

	private Rectangle calculateContainerSize(){
		
		list2 = new Vector();
		Iterator it = list.iterator();
		Rectangle rslt = null;
		int count = 0;
		while(it.hasNext()){
			Object item = it.next();
			if(item instanceof LinkableObject){
				LinkableObject obj = (LinkableObject)item;
				if(parent == null)parent = obj.getDisplayedIn();
				else if(parent != obj.getDisplayedIn())return null;
				if(obj.getSurrogateFor() != null){
				   list2.add(obj);
				   Rectangle rect = new Rectangle(obj.getDisplayRectangle());
				   if(!obj.getIsOpen())rect.height = 18;
				   if(rslt == null)rslt = new Rectangle(rect);
				   else rslt = rslt.getUnion(rect);
				   ++count;
				}
			}
		}
		if(count <= 1)return null;
		//rslt.height += 18;
		rslt.height += 36;
		rslt.width += 18;
		return rslt;
	}
	
	public boolean canUndo(){
		return wasDone;
	}
	
	private void restoreLink(Link link){
		link.reIntroduceMe();
		link.restoreLinks();
	}
	
	private void handleLinksUndo(LinkableObject obj, Vector myLinks){
		Iterator it = myLinks.iterator();
		while(it.hasNext()){
			Link link = (Link)it.next();
			if(link.getALID() == 0)restoreLink(link);
			else if(link.getFromLink() == theSurrogate)link.setFromLink(obj);
			else link.setToLink(obj);
		}
	}
	
	public void undo(){
		theSurrogate.setDisplayedIn(null);
		
		Iterator it = list2.iterator();
		Iterator it2 = rects.iterator();
		Iterator it3 = links.iterator();
		while(it.hasNext()){
			LinkableObject obj = (LinkableObject)it.next();
			Rectangle oldRect = (Rectangle)it2.next();
			handleLinksUndo(obj, (Vector)it3.next());
			
			obj.setDisplPageNo(currentPage);
			obj.setDisplayRectangle(oldRect);
			obj.setDisplayedIn(parent);
			
		}
		
		theSurrogate.setSurrogateFor(null);
		theSurrogate.setLoType(null);

		theSurrogate.deleteMe();
		theNote.setObjectType(null);
		theNote.deleteMe();
	}
}
