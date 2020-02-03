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

package uk.ac.kcl.cch.jb.pliny.imageRes.actions;

import java.net.URL;

import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchPage;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.browser.BrowserViewer;
import uk.ac.kcl.cch.jb.pliny.browser.IBrowserToolbarContribution;
import uk.ac.kcl.cch.jb.pliny.imageRes.dnd.ImageEditorResourceExtensionProcessor;

/**
 * an Action-like class to be displayed in the Pliny browser that,
 * when clicked, harvests suitable images from the current HTML page and sets
 * up Image Resource that point at them.
 * <p>
 * This is done by invoking methods in
 * {@link uk.ac.kcl.cch.jb.pliny.imageRes.dnd.ImageEditorResourceExtensionProcessor ImageEditorResourceExtensionProcessor}
 * 
 * @author John Bradley
 *
 */
public class BrowserButtonImageImport implements IBrowserToolbarContribution {
	
	private URL theUrl;
	private String title = "";

	public BrowserButtonImageImport() {
		super();
	}

	public void setupToolItem(ToolItem item) {
		item.setImage(PlinyPlugin.getImageDescriptor("icons/imageIcon.gif").createImage());
		item.setToolTipText("Create image item(s) from this page");
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setUrl(URL url) {
		theUrl = url;
	}

	@Override
	public void run(IWorkbenchPage page, BrowserViewer browserViewer) {
		ImageEditorResourceExtensionProcessor proc = new ImageEditorResourceExtensionProcessor(page);
		if(!proc.processBrowserPage(browserViewer)) proc.processUrl(theUrl, title);
        proc.openResources();
	}
}

