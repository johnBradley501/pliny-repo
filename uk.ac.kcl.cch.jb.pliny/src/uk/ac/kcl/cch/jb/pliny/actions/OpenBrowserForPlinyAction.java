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

import java.net.MalformedURLException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;

import uk.ac.kcl.cch.jb.pliny.browser.BrowserEditorInput;

/**
 * This action supports the opening of the 
 * {@link uk.ac.kcl.cch.jb.pliny.browser.BrowserEditor} (Pliny's
 * integrated web browser).
 * 
 * 
 * @author John Bradley
 *
 */

public class OpenBrowserForPlinyAction implements
		IWorkbenchWindowActionDelegate {

	public static String BROWSER_ID="uk.ac.kcl.cch.jb.pliny.browserEditor";
	public static final String startupURL="http://pliny.cch.kcl.ac.uk";
	private IWorkbenchWindow window;
	
	public OpenBrowserForPlinyAction() {
		super();
	}


	/**
	 * Disposes this action delegate.  The implementor should unhook any references
	 * to itself so that garbage collection can occur.
	 */
	public void dispose() {
		// do nothing
	}

	/**
	 * Initializes this action delegate with the workbench window it will work in.
	 *
	 * @param window the window that provides the context for this delegate
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	/**
	 * Performs the action of opening the Pliny web browser.
	 * <p>
	 * This method is called when the delegating action has been triggered.
	 * Implement this method to do the actual work.
	 * </p>
	 *
	 * @param action the action proxy that handles the presentation portion of the
	 *   action
	 */
	public void run(IAction action) {
		if(window == null)return;
		IWorkbenchPage page = window.getActivePage();
		if(page == null)return;
		try {
			page.openEditor(new BrowserEditorInput(startupURL),
					BROWSER_ID );
		} catch (PartInitException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Notifies this action delegate that the selection in the workbench has changed.
	 * Nothing is done with this information here.
	 *
	 * @param action the action proxy that handles presentation portion of the action
	 * @param selection the current selection in the workbench
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		//System.out.println(selection);
	}
}
