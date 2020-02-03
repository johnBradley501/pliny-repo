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

package uk.ac.kcl.cch.jb.pliny.containmentView.actions;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentItem;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * provides an action that allows Eclipse to open 
 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource Resource}s
 * associated with 
 * {@link uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentItem ContainmentItem}s in their own editor.
 * This Action is created for the ContainmentViews GEF-derived contextual menu
 * {@link uk.ac.kcl.cch.jb.pliny.containmentView.ContainmentViewMenuProvider}.

 * 
 * @author John Bradley
 *
 */

public class OpenSelectedItemsAction extends Action {

	private Vector items;
	
	/**
	 * creates action instance for selected items provide in list.
	 * 
	 * @param items Vector of <code>ContaineItem</code>s which refer
	 * to references to open in their own editor.
	 */
	public OpenSelectedItemsAction(Vector items) {
		super("Open selected");
		setImageDescriptor(PlinyPlugin.getImageDescriptor("icons/openToEditor.gif"));
		this.items = items;
	}
	
	public void run(){
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(); 
		Iterator it = items.iterator();
		while(it.hasNext()){
			ContainmentItem item = (ContainmentItem)it.next();
			Resource r = item.getResource();
			if(r != null){
				try {
					r.openEditor(page);
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
