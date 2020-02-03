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

package uk.ac.kcl.cch.jb.pliny.figures;

import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

/**
 * Manages the scaling of an image.  Used within Pliny figures that
 * display a zoomable base image upon which annotations can be attached.
 * The zoom size can be changed after the area is set up -- the code will
 * dispose of the image at the old size and produce a new one at the new size.
 * <p>
 * Size of the scaled image is given in terms of pixel width and height.  Note that
 * scaled image held by this object is disposed of when needed, and when this
 * object itself is disposed. However, the main un-scaled image is <i>not</i>
 * disposed.
 * 
 * 
 * @author John Bradley
 */
public class ScalableImageFigure {

	private Image originalImage;
	private ImageData originalImageData;
	private ImageFigure resultImage;
	private int oiWidth, oiHeight, newWidth, newHeight, displayWidth, displayHeight;
	
	/**
	 * constructor for an instance of this data where an image for zooming
	 * will be provided later.  To subsequently provide the image call
	 * {@link #setImage(Image)}.
	 *
	 */
	public ScalableImageFigure(){
		originalImageData = null;
		resultImage = null;
		newWidth = 0;
		newHeight = 0;
		displayWidth = 0;
		displayHeight = 0;
	}
	
	/**
	 * constructor for an instance of this data where the image to be scaled
	 * can be provided at construction time.
	 *
	 */
	
	public ScalableImageFigure(Image originalImage){
		this();
		setImage(originalImage);
	}
	
	
	/**
	 * constructor for an instance of this data where the image to be scaled
	 * can be provided at construction time, and the zoomed size that is needed
	 * is already known too.
	 *
	 */
	
	public ScalableImageFigure(Image originalImage, int width, int height){
		this(originalImage);
		newWidth = width;
		newHeight = height;
	}
	
	/**
	 * sets the unscaled image that is to be scaled by this item.
	 * Normally this method is only used once if the constructor without
	 * parameters was used to create this object.
	 * 
	 * @param originalImage SWT unscaled Image 
	 */
	
	public void setImage(Image originalImage){
		disposeFigure();
		this.originalImage = originalImage;
		if(originalImage == null){
			this.originalImage =originalImage;
			this.originalImageData = null;
			oiWidth = 0;
			oiHeight = 0;
			return;
		}
		this.originalImageData = originalImage.getImageData();
		
		oiWidth = this.originalImageData.width;
		oiHeight = this.originalImageData.height;
	}
	
	/**
	 * returns width in pixels of the unscaled image.
	 * 
	 * @return int the width of the unscaled image in pixels
	 */
	
	public int getOriginalWidth(){
		return oiWidth;
	}
	
	/**
	 * returns height in pixels of the unscaled image.
	 * 
	 * @return int the height of the unscaled image in pixels
	 */
	
	public int getOriginalHeight(){
		return oiHeight;
	}
	
	/**
	 * returns width in pixels of the scaled image.
	 * 
	 * @return int the width of the scaled image in pixels
	 */
	
	public int getDisplayWidth(){
		return displayWidth;
	}
	
	/**
	 * returns height in pixels of the scaled image.
	 * 
	 * @return int the height of the scaled image in pixels
	 */
	
	public int getDisplayHeight(){
		return displayHeight;
	}
	
	private int doScale(int val1, int val2, int scaleval){
		return val1 * scaleval / val2; 
	}
	
	private void disposeFigure(){
		//if(resultImage != null)resultImage.getImage().dispose();
		resultImage = null;
	}

	/**
	 * sets the width the image is to scaled to.  This method may be
	 * called any time the width of the image needs to change.
	 * 
	 * @param width int new width for the scaled image.
	 */
	
	public void setWidth(int width){
		newWidth = width;
		disposeFigure();
	}

	/**
	 * sets the height the image is to scaled to.  This method may be
	 * called any time the height of the image needs to change.
	 * 
	 * @param height int new height for the scaled image.
	 */
	
	public void setHeight(int height){
		newHeight = height;
		disposeFigure();
	}

	/**
	 * sets the width and height the image is to scaled to.  This method may be
	 * called any time the width and/or height of the image needs to change.
	 * 
	 * @param width int new width for the scaled image.
	 * @param height int new height for the scaled image.
	 */
	
	public void setDimensions(int width, int height){
		newWidth = width;
		newHeight = height;
		disposeFigure();
	}

	/**
	 * sets the width and height the image is to scaled to.  This method may be
	 * called any time the width and/or height of the image needs to change.
	 * 
	 * @param dim desired Dimension of scaled image.
	 */
	
	public void setDimensions(Dimension dim){
		if((dim.height == 0) && (dim.width == 0))return;
		setDimensions(dim.width, dim.height);
	}
	
	/**
	 * fetches the scaled image as a draw2d ImageFigure according
	 * to the scaling information currently stored here.
	 * 
	 * @return ImageFigure the scaled version of the image as a draw2d ImageFigure.
	 */
	
	public ImageFigure getFigure(){
		if(originalImage == null)return null;
		if(resultImage != null)return resultImage;
		if(((newWidth<=0?oiWidth:newWidth) == oiWidth) &&
			((newHeight<=0?oiHeight:newHeight) == oiHeight)){
			resultImage = new ImageFigure(originalImage);
			displayWidth = oiWidth;
			displayHeight = oiHeight;
			return resultImage;
		}
		if(originalImageData == null)originalImageData = originalImage.getImageData();
		displayWidth = newWidth;
		displayHeight = newHeight;
		if(displayWidth <= 0)displayWidth = doScale(oiWidth, oiHeight, newHeight);
		if(displayHeight <= 0)displayHeight = doScale(oiHeight, oiWidth, newWidth);
		Display display = Display.getCurrent();
		Image newImage = new Image(display, originalImageData.scaledTo(displayWidth, displayHeight));
		resultImage = new ImageFigure(newImage);
		originalImageData = null;
		return resultImage;
	}
	
	public void dispose(){
		//if(originalImage != null)originalImage.dispose();
		//if(resultImage != null)resultImage.getImage().dispose();
		disposeFigure();
		originalImageData = null;
	}
	
}
