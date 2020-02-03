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

package uk.ac.kcl.cch.jb.pliny.controls;

import org.eclipse.gef.commands.CommandStack;

/**
 * Editors who wish to make use of the {@link ZoomControlSpinner} or {@link ZoomControlSpinnerContribution] to provide
 * a user control to set the zoomable size of their base images should implement
 * this interface.
 * 
 * @author John Bradley
 *
 */

public interface IZoomableImageEditor {
	/**
	 * return the Pliny Resource that this editor is currently working with.
	 * The resource must implement the IZoomableImageResource interace 
	 * 
	 * @return IZoomableImageResource the editor's current resource.
	 */
	public IZoomableImageResource getMyImageResource();
	
	/**
	 * changes in the zoom size are meant to be undoable, so they are managed in
	 * the GEF manner by a CommandStack owned by the editor, and must be made
	 * accessible to the control.
	 * 
	 * @return CommandStack owned by the editor.
	 */
	public CommandStack getCommandStack();
}
