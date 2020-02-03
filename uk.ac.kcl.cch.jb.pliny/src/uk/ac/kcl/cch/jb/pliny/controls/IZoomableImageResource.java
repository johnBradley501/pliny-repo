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

import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.geometry.Rectangle;

/**
 * implement this interface in a Pliny resource which has an image-oriented Resource
 * which has its size controllable by a {@link ZoomControlSpinner} or {@link ZoomControlSpinnerContribution}.
 * 
 * @author John Bradley
 */

public interface IZoomableImageResource {
	
	/**
	 * gets the current Zoom size stored in the resource.
	 * 
	 * @return int the current zoom size.
	 */
	
	public int getZoomSize();
	
	/**
	 * sets the current Zoom size stored in the resource to the given value.
	 * 
	 * @param zoom int the new zoom size to save.
	 */
	
	public void setZoomSize(int zoom);
	
	
	/**
	 * gets the current ImagePosition rectangle -- the position where the
	 * image sits in the user's frame.
	 * 
	 * @return Rectangle the resource's current ImagePosition setting.
	 */
	

	public Rectangle getImagePosition();

	/**
	 * allows the caller to subscribe to the resource to be notified about
	 * changes in zoom size as they occur.  The caller who makes use of
	 * this interface is only interested in 
	 * <code>Resource.ATTRIBUTES_PROP</code> events.
	 * 
	 * @param listener
	 */
	
	public void addPropertyChangeListener(PropertyChangeListener listener);

	/**
	 * allows the caller to unsubscribe to the resource.
	 * 
	 * @param listener
	 */
	
	public void removePropertyChangeListener(PropertyChangeListener listener);
}
