/*******************************************************************************
 * Copyright (c) 2007, 2014 John Bradley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Bradley - initial API and implementation
 *******************************************************************************/

package uk.ac.kcl.cch.jb.pliny.actions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.eclipse.gef.editparts.AbstractEditPart;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;

import uk.ac.kcl.cch.jb.pliny.dnd.ClipboardHandler;
import uk.ac.kcl.cch.jb.pliny.model.Link;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.parts.RootResourcePart;
import uk.ac.kcl.cch.rdb2java.dynData.BaseObject;

/**
 * the standard copy action for GEF-based Pliny items.
 * Makes use of <code>ClipboardHandler</code> as the interface to the
 * Clipboard.
 * <p>As a result of the provision of a standardised PlinyToolBar, and the presence of a
 * text field in it, in 2014 code was added here to deal with situations when copying
 * was into that text field.
 * 
 * @author John Bradley
 *
 */
public class CopyPlinyAction extends SelectionAction {
	 //  public static final String ActionLabelText =
	//	   "uk.ac.kcl.cch.jb.noteMan.ui.imageEditor.CopyAction.ActionLabelText_UI_";
	
	protected StyledText styledText = null;

	/**
	 * constructor for this Action.
	 * 
	 * @param part IWorkbenchPart the owning part for this copy action.
	 * @param style int the style
	 */
	public CopyPlinyAction(IWorkbenchPart part, int style) {
		super(part, style);
	}

	/**
	 * constructor for this Action.
	 * 
	 * @param part IWorkbenchPart the owning part for this copy action.
	 */
	public CopyPlinyAction(IWorkbenchPart part) {
		super(part);
	}
	
	protected void init() {
		setId(ActionFactory.COPY.getId());
		setText("copy");
		super.init();
	}
	
	public void setStyledText(StyledText styledText){
		this.styledText = styledText;
	}
	
	public void clearStyledText(){
		this.styledText = null;
	}

	protected boolean calculateEnabled() {
		if(styledText == null){
			// selection is outside of a styledtext item: presumably in a GEF managed area.
			List selectedObjects = getSelectedObjects();
			Iterator it = selectedObjects.iterator();
		    // System.out.println("calculateEnabled: selectedObjects: "+selectedObjects);
			while(it.hasNext()){
				Object obj = it.next();
				if(!(obj instanceof RootResourcePart))return true;
			}
			return false;
		}
		// selection is in a styledtext area: selection must be a text type
		ISelection isel = getSelection();
		if(isel instanceof ITextSelection)return ((ITextSelection)isel).getLength() > 0;
		return false;
	}
	
	/**
	 * the run method for this action.  It takes the selected objects that
	 * are in its part and stores them in a Vector.  This Vector is given
	 * to the clipboard.
	 */
	public void run(){
		if(styledText != null){
			styledText.copy();
			return;
		}
		// selection is in a GEF controlled area.
 		
		/*
		 * A reminder that in the test version of this software Link objects
		 * did not seem to be selected when the MarqueeTool tool (available in 
		 * the test version) was used.  This meant that code was here to select
		 * all links that went between two selected objects, and include them
		 * in the selection.  This is not true in this version, where, instead
		 * links are checked to be sure that they link to selected objects at 
		 * both ends before being included.
		 * 
		 *  There are hints in the bug tracker for GEF that the missing of links
		 *  by the MarqueeTool is consided a bug.    ... jb
		 */
		   List items = getSelectedObjects();
		   if(items.size() == 0)return;
		   Iterator it = items.iterator();
		   Vector selectedModelItems = new Vector();
		   //Vector linkList = new Vector();
		   Set linkList = new HashSet();
		   Set itemsincluded = new HashSet();
		   while(it.hasNext()){
			   Object selItem = it.next();
			   if(selItem instanceof AbstractEditPart){
				   Object myModel = ((AbstractEditPart)selItem).getModel();
				   // System.out.println("CopyNoteManAction: "+myModel.toString());
				   if(myModel instanceof Link)linkList.add(myModel);
				   else if(myModel instanceof LinkableObject) {
					   LinkableObject lo = (LinkableObject)myModel;
					   linkList.addAll(lo.getLinkedFrom().getItems());
					   linkList.addAll(lo.getLinkedTo().getItems());
					   selectedModelItems.add(myModel);
				       itemsincluded.add(myModel);
				   //} else if (myModel instanceof BaseObject){
				   //    selectedModelItems.add(myModel);
				   //    itemsincluded.add(myModel);
				   }
			   }
		   }
		   
		   // the following code checks all included links, and includes those
		   // in the copy selection that have both ends also in.
		   
		   it = linkList.iterator();
		   while(it.hasNext()){
			   Link myLink = (Link)it.next();
			   if(itemsincluded.contains(myLink.getFromLink()) &&
				  itemsincluded.contains(myLink.getToLink()))
				   selectedModelItems.add(myLink);
		   } 
		   if(selectedModelItems.size() > 0){
			   ClipboardHandler.getDefault().setContents(selectedModelItems);
		   }
	   }
	
	// for debugging
	//   protected void setSelection(ISelection selection){
	//	   System.out.println("CopyNoteMan2Action setSelection: "+selection);
	//	   super.setSelection(selection);
	//  }
	
	public ISelection getMySelection(){
		return this.getSelection();
	}

}
