/*******************************************************************************
 * Copyright (c) 2009 John Bradley
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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.views.ResHolderView;

/**
 * This action supports the opening of Pliny's
 * {@link uk.ac.kcl.cch.jb.pliny.views.ResourceHolderView}.
 * 
 * @author John Bradley
 *
 */

public class OpenResHolderViewAction extends Action implements IWorkbenchWindowActionDelegate {
	
	private IWorkbenchWindow window;
	
	public OpenResHolderViewAction(){
		super("Open Resource Holder");
		window = null;
		setToolTipText("Open Resource Holder View");
		setImageDescriptor(
				ImageDescriptor.createFromImage(PlinyPlugin.getDefault().getImage("icons/resHolderView.gif")));
	}

	public void dispose() {
		// not needed.    j.b.
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
	
	public void run(){
		if(window == null){
			IWorkbench wb = PlatformUI.getWorkbench();
			window = wb.getActiveWorkbenchWindow();
		}
		IWorkbenchPage page = window.getActivePage();
		if(page == null)return;
		try {
			page.showView(ResHolderView.MY_ID);
		}
		catch (PartInitException e) {
			e.printStackTrace();
		}

	}


	public void run(IAction action) {
		run();
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

}
