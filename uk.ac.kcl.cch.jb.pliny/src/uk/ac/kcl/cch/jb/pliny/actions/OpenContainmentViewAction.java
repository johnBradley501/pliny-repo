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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;

/**
 * This action supports the opening of Pliny's
 * {@link uk.ac.kcl.cch.jb.pliny.containmentView.ContainmentView}.
 * 
 * 
 * @author John Bradley
 *
 */

public class OpenContainmentViewAction implements
		IWorkbenchWindowActionDelegate {

	private static final String CONTAINMENTVIEW_ID =
		"uk.ac.kcl.cch.jb.pliny.containmentView";
	
	private IWorkbenchWindow window;

	public void dispose() {
		// not needed.    j.b.
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run(IAction action) {
		if(window == null)return;
		IWorkbenchPage page = window.getActivePage();
		if(page == null)return;
		
		// open and activite the Overall view.
		try {
			page.showView(CONTAINMENTVIEW_ID);
		}
		catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		//	not needed.   j.b.
	}

}
