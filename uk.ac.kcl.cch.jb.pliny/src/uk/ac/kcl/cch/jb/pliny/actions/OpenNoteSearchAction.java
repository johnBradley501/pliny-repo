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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;

/**
 * This action supports the opening of Pliny's
 * {@link uk.ac.kcl.cch.jb.pliny.views.NoteSearchView}.
 * 
 * 
 * @author John Bradley
 *
 */


public class OpenNoteSearchAction extends Action 
implements IWorkbenchWindowActionDelegate{
	private static final String NOTE_SEARCH_VIEW_ID =
		"uk.ac.kcl.cch.jb.pliny.searchView";
	private IWorkbenchWindow window;

	public OpenNoteSearchAction() {
		super();
		this.setText("Open Note Searching View");
		this.setImageDescriptor(
				ImageDescriptor.createFromImage(PlinyPlugin.getDefault().getImage("icons/searchIcon.gif")));
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run() {
		if(window == null)window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		if(page == null)return;
		
		// open and activite the Note Searching view.
		try {
			page.showView(NOTE_SEARCH_VIEW_ID);
		}
		catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	public void dispose() {
		// nothing to dispose here
	}

	public void run(IAction action) {
		run();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// nothing to do here
	}

}
