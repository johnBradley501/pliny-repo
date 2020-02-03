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

package uk.ac.kcl.cch.jb.pliny.commands;

import org.eclipse.gef.commands.Command;

import uk.ac.kcl.cch.jb.pliny.controls.IZoomableImageResource;

/**
 * Changes the Zoom value (which controls the visual display size of
 * an image) for resources of type
 * {@link uk.ac.kcl.cch.jb.pliny.controls.IZoomableImageResource IZoomableImageResource}.
 * Generally, changing the Zoom value for a zoomable resource can only be
 * done while the image is on the screen, so the visual result of this
 * is therefore to also change the size of it on the screen.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.controls.ZoomControlSpinner ZoomControlSpinner
 * 
 * @author John Bradley
 *
 */
public class ChangeImageZoomValueCommand extends Command {
	private IZoomableImageResource myResource;
	private int newZoomValue, oldZoomValue;

	public ChangeImageZoomValueCommand(IZoomableImageResource myResource, int newZoomValue) {
		super("change image scale");
		this.myResource = myResource;
		this.newZoomValue = newZoomValue;
	}
	
	public void execute(){
		oldZoomValue = myResource.getZoomSize();
		myResource.setZoomSize(newZoomValue);
	}
	
	public void undo(){
		myResource.setZoomSize(oldZoomValue);
	}
}
