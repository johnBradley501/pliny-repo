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

import java.util.List;

import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;

import uk.ac.kcl.cch.jb.pliny.commands.CutObjectsCommand;

/**
 * the standard cut action for GEF-based Pliny items.
 * Makes use of <code>ClipboardHandler</code> as the interface to the
 * Clipboard.
 * <p>As a result of the provision of a standardised PlinyToolBar, and the presence of a
 * text field in it, in 2014 code was added here to deal with situations when cutting
 * was into that text field.
 * 
 * @author John Bradley
 *
 */
public class CutPlinyAction extends CopyPlinyAction {

	/**
	 * constructor for this Action.
	 * 
	 * @param part IWorkbenchPart the owning part for this copy action.
	 * @param style int the style
	 */
	public CutPlinyAction(IWorkbenchPart part, int style) {
		super(part, style);
		setId(ActionFactory.CUT.getId());
		setText("Cut");
	}

	/**
	 * constructor for this Action.
	 * 
	 * @param part IWorkbenchPart the owning part for this copy action.
	 */
	public CutPlinyAction(IWorkbenchPart part) {
		super(part);
		// TODO Auto-generated constructor stub
	}
	
   /**
    * @see org.eclipse.gef.ui.actions.EditorPartAction#init()
    */
   protected void init() {
   	  setId(ActionFactory.CUT.getId());
   	  setText("cut");
   }
   
	/**
	 * the run method for this action.  If the cutting is in a styledText item, it allows the styledText item to handle the curreing. 
	 * If, however, it is in a GEF-managed area, it takes the selected objects that
	 * are in its part and stores them in a List.  Cutting is done through
	 * the command {@link uk.ac.kcl.cch.jb.pliny.commands.CutObjectsCommand} so that it can be undoable.
	 */
   public void run(){
	   if(styledText != null){
		  styledText.cut();
		  return;
	   }
 	   //System.out.println("CutPlinyAction run for: "+this.getWorkbenchPart());
	   List items = getSelectedObjects();
	   if(items.size() == 0)return;
	   //super.run();
	   
	   this.execute(new CutObjectsCommand(items));
   }
}
