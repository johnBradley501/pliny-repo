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

package uk.ac.kcl.cch.jb.pliny.browser;

import java.net.URL;

import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchPage;
/**
 * Plugins who wish to contribute an action button to the browser
 * for Pliny must implement this interface.  See the extension point
 * defined by the Pliny plugin 'browserToolbarContribution'.
 * <p>
 * The browser displays a button for each object of this kind that
 * it finds (via its extension point), and will provide to the code
 * behind this interface the URL and title of the current webpage.
 * It is the job of the supplying plugin to process this through
 * (<code>run()</code>) when the user
 * clicks on the button.
 * <p>
 * For the contract to work the browser requires the following:
 * <ul>
 * <li>There must be a constructor with no parameters.
 * <li>method <code>setupToolItem</code> must provide an image
 * and tooltip text for the Toolitem the browser will create.
 * <li>the browser will provide a title (through <code>setTitle</code>
 * and a URL (<code>setUrl</code>) that will be used as parameters
 * for the method
 * </ul>
 * <p>
 * There is an implementation in
 * {@link uk.ac.kcl.cch.jb.pliny.imageRes.actions.BrowserButtonForSetup}
 * 
 * @author Bradley
 *
 */

public interface IBrowserToolbarContribution {
	/**
	 * The browser calls this to get a button to display.  The
	 * implementor must define the provided ToolItem -- with an image
	 * or text label and other options for display of the button
	 * such as a tooltip.
	 * 
	 * @param item ToolItem the browser will display
	 */
     public void setupToolItem(ToolItem item);
     
     /**
      * Each time the browser user changes the current web page
      * the browser will call this method to keep the implementing
      * process informed about the current URL the user is seeing.
      * 
      * @param url URL the URL of the current displayed page
      */
     public void setUrl(URL url);
     
     /**
      * Each time the browser processes the web page and collects the
      * page title it will call this method to provide the current
      * web page's title to the implementor.
      * 
      * @param title
      */
     public void setTitle(String title);
     
     /**
      * When the browser use clicks on the contributed button the
      * browser will call this code, passing as a parameter its
      * workbench Page.  The implementor should interpret this as a
      * request to process the previously passed URL and title.
      * 
      * @param page IWorkbenchPage the browser's page
     * @param browserViewer 
      */
     public void run(IWorkbenchPage page, BrowserViewer browserViewer);
}
