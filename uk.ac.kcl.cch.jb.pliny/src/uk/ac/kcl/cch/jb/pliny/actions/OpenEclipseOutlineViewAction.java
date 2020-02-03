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
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * This action supports the opening of Eclipse's OutlineView.
 * This is used by Pliny's PDF viewer 
 * {@link uk.ac.kcl.cch.jb.pliny.pdfAnnot.PDFEditor} to
 * display the outline of a PDF document.
 * 
 * 
 * @author John Bradley
 *
 */

public class OpenEclipseOutlineViewAction extends Action {
	private IWorkbenchWindow window;

	public OpenEclipseOutlineViewAction() {
		super();
		this.setImageDescriptor(PlatformUI.getWorkbench().
				getViewRegistry().find(IPageLayout.ID_OUTLINE).
				getImageDescriptor());
		this.setText("Open the Outline View");
	}

	public void dispose() {
		// not needed.    j.b.
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run() {
		if(window == null)window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		if(page == null)return;
		
		// open and activite the Overall view.
		try {
			page.showView(IPageLayout.ID_OUTLINE);
		}
		catch (PartInitException e) {
			e.printStackTrace();
		}
	}
}