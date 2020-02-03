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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.views.utils.NewNoteWizard;

/**
 * Provides the action to create a new Pliny note.
 * As well as being an Action it is an IWorkbenchWindowActionDelegate
 * which allows it to be linked to an icon in the Eclipse-wide toolbar.
 * 
 * @author John Bradley
 *
 */
public class CreateNoteAction extends Action implements
		IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;
	/**
	 * creates the action that supports the creation of a Pliny Note.
	 */
	public CreateNoteAction() {
		super();
		setText("Create Note...");
		setImageDescriptor(PlinyPlugin.getImageDescriptor("icons/noteIcon.gif"));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		//nothing to do here
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;

	}

	/**
	 * runs the action to create the note.  This is done through the
	 * {@link uk.ac.kcl.cch.jb.pliny.views.utils.NewNoteWizard}.
	 */
	public void run() {
		NewNoteWizard wizard = new NewNoteWizard(null);
		WizardDialog dialog = 
			new WizardDialog(window.getShell(), wizard);
		dialog.open();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		run();

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// nothing to do here.
	}

}
