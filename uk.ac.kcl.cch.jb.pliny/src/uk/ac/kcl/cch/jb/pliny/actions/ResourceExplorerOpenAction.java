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

/**
 * 
 */
package uk.ac.kcl.cch.jb.pliny.actions;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import uk.ac.kcl.cch.jb.pliny.model.Favourite;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.views.ResourceExplorerView;
import uk.ac.kcl.cch.jb.pliny.views.utils.IResourceExplorerItem;
import uk.ac.kcl.cch.jb.pliny.views.utils.IResourceTreeDisplayer;
import uk.ac.kcl.cch.jb.pliny.views.utils.ResourceExplorerDisplayedInItem;

/**
 * This action provides open support for the 
 * ({@link uk.ac.kcl.cch.jb.pliny.views.ResourceExplorerView})
 * and other Resource Explorer-like views, such as the
 * {@link uk.ac.kcl.cch.jb.pliny.views.NoteSearchView}.  It
 * causes selected items in the view to opened using the appropriate
 * Pliny editor.
 * 
 * @author John Bradley
 *
 */
public class ResourceExplorerOpenAction extends Action {
	private IResourceTreeDisplayer view;
	/**
	 * constructor for the Open Action. Note that it
	 * requires a
	 * ({@link uk.ac.kcl.cch.jb.pliny.views.utils.IResourceTreeDisplayer}) --
	 * one of the views that displays a list of resources in a tree
	 * context like the Resource Explorer does.
	 * 
	 * @param view the IResourceTreeView.
	 * 
	 */
	public ResourceExplorerOpenAction(IResourceTreeDisplayer view) {
		super("Open");
		this.view = view;
	}
	
	public void run(){
		//IWorkbenchWindow window = view.getViewSite().getWorkbenchWindow();
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
		
		if(window == null)return;
		Vector candidates = view.getSelectedResourceExplorerItems();
		//Vector candidates = view.getSelectedObjectsToOpen();
		Iterator it = candidates.iterator();
		while(it.hasNext()){
			IResourceExplorerItem item = (IResourceExplorerItem)it.next();
			Object obj = item.getAssociatedObject();
			if(item instanceof ResourceExplorerDisplayedInItem){
				obj = ((ResourceExplorerDisplayedInItem)item).getResource();
			}
			//Object obj = it.next();
			if(obj instanceof Resource)
				openResource(window,(Resource)obj, item.getPageNumber());
			else if(obj instanceof LinkableObject){
				Resource surrogate = ((LinkableObject)obj).getSurrogateFor();
				if(surrogate != null)openResource(window, surrogate, item.getPageNumber());
			} else if(obj instanceof Favourite){
				openResource(window,((Favourite)obj).getResource(), item.getPageNumber());
			}
		}
	}

	protected void openResource(IWorkbenchWindow window,Resource resource, int pgNumb) {
		IWorkbenchPage page = window.getActivePage();
		if(page == null){
			IWorkbench wb = PlatformUI.getWorkbench();
			page = wb.getActiveWorkbenchWindow().getActivePage();
		}
		try {
			resource.openEditor(page, pgNumb);
		} catch (PartInitException e) {
			Shell parentShell = window.getShell();
			MessageBox messageBox = new MessageBox(parentShell, 
					SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("Cannot open");
			messageBox.setMessage("Object Type object '"+resource.getName()+
					"' cannot be opened. The eclipse environment reported: "+e.getLocalizedMessage());
			messageBox.open();
		}
	}
}
