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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.WorkbenchException;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;

/**
 * This action supports the opening of Pliny's perspective: 
 * {@link uk.ac.kcl.cch.jb.pliny.PlinyPerspectiveFactory}.
 * 
 * 
 * @author John Bradley
 *
 */

public class OpenPlinyPerspectiveAction extends Action implements
		IWorkbenchWindowActionDelegate {

	public OpenPlinyPerspectiveAction() {
		super("Open Pliny Perspective");
		// Do nothing more
	}

	public void dispose() {
		// Do not do anything

	}

	public void init(IWorkbenchWindow window) {
		//  Do not do anything

	}
	
	public void run(){
		IWorkbench workbench = PlinyPlugin.getDefault().getWorkbench();
		IWorkbenchWindow window= workbench.getActiveWorkbenchWindow();
		try {
			workbench.showPerspective("uk.ac.kcl.cch.jb.pliny.perspective", window);  //$NON-NLS-1$
		} catch (WorkbenchException e) {
			//TODO add logging PlinyPlugin.logError(e);
			e.printStackTrace();
		}
	}

	public void run(IAction action) {
		run();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		//  Do not do anything

	}

}
