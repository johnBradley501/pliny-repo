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

package uk.ac.kcl.cch.jb.pliny.actions;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * This action supports the opening of Pliny's editors associated
 * with {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject}s stored in a Vector of items.
 * 
 * @author John Bradley
 *
 */

public class OpenSelectedEditorAction extends Action {

	private Vector items;
	
	protected OpenSelectedEditorAction(){
		super("Open selected");
		setImageDescriptor(PlinyPlugin.getImageDescriptor("icons/openToEditor.gif"));
		this.items = null;
	}
	
	/**
	 * constructor for the action.  Give the list of items to drive the
	 * opening as a Vector.  The Vector must contain a collection of
	 * LinkableObjects.  This will take the surrogate resource as the item
	 * to be opened.
	 * 
	 * @param items Vector of {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject}s
	 */
	public OpenSelectedEditorAction(Vector items) {
		super("Open selected");
		setImageDescriptor(PlinyPlugin.getImageDescriptor("icons/openToEditor.gif"));
		this.items = items;
	}
	
	protected void setItems(Vector items){
		this.items = items;
	}
	
	public void run(){
		if(items == null)return;
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(); 
		Iterator it = items.iterator();
		while(it.hasNext()){
			LinkableObject lo = (LinkableObject)it.next();
			Resource r = lo.getSurrogateFor();
			if(r != null){
				try {
					r.openEditor(page,lo.getSurrPageNo());
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
