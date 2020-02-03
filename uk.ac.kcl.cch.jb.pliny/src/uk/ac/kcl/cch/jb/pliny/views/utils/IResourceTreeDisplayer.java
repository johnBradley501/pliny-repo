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

package uk.ac.kcl.cch.jb.pliny.views.utils;

import java.util.Vector;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * all ViewParts that wish to implement a Resource Explorer like GUI should
 * implement this interface.  Note that it extends Eclipse's 
 * <code>org.eclipse.ui.IViewPart</code>.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.views.ResourceExplorerView
 * @see uk.ac.kcl.cch.jb.pliny.views.NoteSearchView
 * 
 * @author John Bradley
 *
 */

public interface IResourceTreeDisplayer{
	
	/**
	 * return the main tree viewer that displays items in this View.
	 * 
	 * @return the main TreeViewer.
	 */
	public TreeViewer getMyViewer();
	
	/**
	 * returns a Vector containing instances of 
	 * {@link IResourceExplorerItem}s that are currently selected by
	 * the user.
	 * 
	 * @return a Vector of IResourceExplorerItems.
	 */
	public Vector getSelectedResourceExplorerItems();
	
	/**
	 * returns a Vector containing Pliny Model elements that are selected
	 * and ready to open in their own editors.  This code needs to contain
	 * special handling code to take care of the special needs of items
	 * under the 'DisplayedIn' category.
	 * 
	 * @return Vector of Pliny Model elements
	 */
	public Vector getSelectedObjectsToOpen();

	/**
	 * returns a Vector containing Pliny Model BaseObject elements that are selected.
	 * 
	 * @return Vector of Pliny Model BaseObject elements
	 */
	public Vector getSelectedBaseObjects();

	
	/**
	 * returns the view's CommandStack.
	 * 
	 * @return CommandStack for the view.
	 */
	public CommandStack getCommandStack();

	/**
	 * returns the IViewSite for the current view.
	 * 
	 * @see org.eclipse.ui.IViewPart#getViewSite
	 */
	
    //public IViewSite getViewSite();

	/**
	 * returns the IWorkbenchPartSite for the current view.
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#getSite
	 */
    //public IWorkbenchPartSite getSite();
    
	public void dispose();

}
