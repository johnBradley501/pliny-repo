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

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.model.Favourite;
import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.Note;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.views.utils.IResourceTreeDisplayer;

/**
 * invoked by the ResourceExplorer's {@link uk.ac.kcl.cch.jb.pliny.views.utils.NewNoteWizard NewNoteWizard} 
 * to create a new Note.  The various NewNoteWizard UI options
 * are provided as parameters to guide the creation.
 * 
 * @author John Bradley
 *
 */

public class ResourceExplorerNewNoteCommand extends Command {

	private String newName;
	private NoteLucened newNote = null;
	private Favourite fav = null;
	private boolean openSucceeded = true;
	private boolean addToFavourites;
	private boolean makeReferent;
	private Resource oldReferent = null;
	private LinkableObject forNewNote = null;
	private LinkableObject forReferent = null;
	private Vector linktoList = null;
	
	/**
	 * creates an instance of this command with information to
	 * guide the note creation as parameters.
	 * 
	 * @param newName String name of new note to create
	 * @param addToFavourites boolean indicates whether new note should be also
	 * added to the Favourites (now called Bookmarks) or not.
	 * @param makeReferent indicates whether the new note should be set up as the
	 * current Referent or not.
	 * @param linktoList Vector list of Resources for which a LinkableObject
	 * reference object should be automatically created to link them to this new note.
	 */
	public ResourceExplorerNewNoteCommand(String newName, boolean addToFavourites, boolean makeReferent, Vector linktoList) {
		super("create note");
		this.newName = newName;
		this.addToFavourites = addToFavourites;
		this.makeReferent = makeReferent;
		this.linktoList = linktoList;
	}
	//private static ObjectType noteType = null;
	
	//private ObjectType getNoteType(){
	//	if(noteType == null)
	//		noteType = ObjectType.getItem(1);
	//	return noteType;
	//}
	
	private void doFavouriteAdd(){
		fav = new Favourite();
		fav.setResource(newNote);
	}
	
	private void buildLinkableObjects(){
		Rectangle myPosition = new Rectangle(new Point(0,0),CreateLinkableObjectCommand.DEFAULT_NOTE_DIMENSION);
		Rectangle refLinkPosition = new Rectangle(myPosition);

		forReferent = new LinkableObject();
		//Rectangle refRefPos = new Rectangle(myPosition);
		myPosition.y = (PlinyPlugin.getReferenceNumber()-1)*CreateLinkableObjectCommand.DEFAULT_NOTE_DIMENSION.height;
		forReferent.setDisplayRectangle(myPosition);
		forReferent.setIsOpen(true);
		forReferent.setShowingMap(false);
		forReferent.setSurrogateFor(newNote);
		forReferent.setLoType(LOType.getCurrentType());
		forReferent.setDisplayedIn(PlinyPlugin.getReferent());
		
		forNewNote = new LinkableObject();
		forNewNote.setDisplayRectangle(refLinkPosition);
		forNewNote.setIsOpen(false);
		forNewNote.setShowingMap(false);
		forNewNote.setSurrogateFor(PlinyPlugin.getReferent());
		forNewNote.setLoType(LOType.getBibRefType());
		forNewNote.setDisplayedIn(newNote);
	}
	
	public void addLinktoList(){
		if(linktoList == null)return;
		
		int ypos = 0;
		if(forNewNote != null)ypos = 36;
		
		Rectangle myPosition = new Rectangle(new Point(0,ypos),CreateLinkableObjectCommand.DEFAULT_NOTE_DIMENSION);
		
		Iterator it = linktoList.iterator();
		
		while(it.hasNext()){
			Resource r = (Resource)it.next();
			LinkableObject lo = new LinkableObject();
			//lo.setIsOpen(true);
			lo.setIsOpen(false);
			lo.setShowingMap(false);
			myPosition.y = ypos;
			ypos += 18;
			lo.setDisplayRectangle(new Rectangle(myPosition));
			lo.setLoType(LOType.getCurrentType());
			lo.setSurrogateFor(r);
			lo.setDisplayedIn(newNote);
		}
	}
	
	public void execute(){
		if (newNote == null){
			if(newName != null && newName.trim().length() > 0){
				newNote = new NoteLucened();
				newNote.setName(newName.trim());
			}
			else if(PlinyPlugin.getReferent() != null)
				newNote = PlinyPlugin.getReferent().makeNewDisplayedNote();
			else {
				newNote = new NoteLucened();
				newNote.setName("New Note");
			}
		}
		else newNote.reIntroduceMe();
		newNote.setObjectType(Note.getNoteObjectType());
		newNote.setContent("");
		if(addToFavourites)doFavouriteAdd();
		if(makeReferent){
			oldReferent = PlinyPlugin.getReferent();
			PlinyPlugin.setReferent(newNote);
		} else if(PlinyPlugin.getReferent() != null)
			buildLinkableObjects();
		addLinktoList();
		
		IWorkbenchPage thePage = null;
		//if(view != null)thePage = view.getSite().getPage();
		//else {
			IWorkbench wb = PlatformUI.getWorkbench();
			thePage = wb.getActiveWorkbenchWindow().getActivePage();
		//}
		
		try {
			newNote.openEditor(thePage);
		} catch (PartInitException e) {
			openSucceeded = false;
		}
	}
	
	private void removeLinkableObject(LinkableObject thisOne){
		if(thisOne == null)return;
		thisOne.setDisplayedIn(null);
		thisOne.setSurrogateFor(null);
		thisOne.deleteMe();
	}
	
	private void removeLinkableObjects(Vector los){
		Vector newlos = new Vector(los);
		Iterator it = newlos.iterator();
		while(it.hasNext()){
			LinkableObject lo = (LinkableObject)it.next();
			removeLinkableObject(lo);
		}
	}
	
	public void undo(){
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchPage thePage = wb.getActiveWorkbenchWindow().getActivePage();

		//if(openSucceeded)newNote.closeMyEditor(view.getSite().getPage(), false);
		if(openSucceeded)newNote.closeMyEditor(thePage, false);
		if(fav != null){
			fav.setResource(null);
			fav.deleteMe();
		}
		if(makeReferent)PlinyPlugin.setReferent(oldReferent);
		else {
			removeLinkableObject(forReferent);
			removeLinkableObject(forNewNote);
		}
		
		//if(linktoList != null)
		removeLinkableObjects(newNote.getMyDisplayedItems().getItems());
		removeLinkableObjects(newNote.getMySurrogates().getItems());
		
		newNote.setObjectType(null);
		newNote.deleteMe();
	}

}
