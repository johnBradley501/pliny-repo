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

package uk.ac.kcl.cch.jb.pliny.parts;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;

/**
 * announces that this GEF edit part can hold scaleable data (such as
 * zoomable images) where the children GEF editparts must be repositioned
 * whenever the scale of this part is changed.  The methods here support the
 * mapping of coordinates and sizes between an absolute size and a current
 * scaled size -- where the provided coordinates might be (for an image) the
 * position relative to the actual width/height of the image, and the scaled
 * co-ordinates need to be adjusted for the current presented size of the image.
 * 
 * @author John Bradley
 *
 */

public interface IScalableImagePart extends GraphicalEditPart{

	public Point shiftNoScaling(int x, int y);
	
	public Point alignForImage(int x, int y);
	public Point alignForImage(Point pt);
	public Dimension scaleForImage(Dimension in);
	public Rectangle scaleForImage(Rectangle in);
	public Rectangle positionForImage(Rectangle in);
	
	public Point alignForDisplay(int x, int y);
	public Point alignForDisplay(Point pt);
	public Dimension scaleForDisplay(Dimension in);
	public Rectangle positionForDisplay(Rectangle in);
	public Rectangle scaleForDisplay(Rectangle in);
}
