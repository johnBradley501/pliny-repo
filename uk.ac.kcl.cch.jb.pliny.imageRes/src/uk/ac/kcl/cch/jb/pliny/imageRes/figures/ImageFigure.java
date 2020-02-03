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

package uk.ac.kcl.cch.jb.pliny.imageRes.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayeredPane;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.FreeformViewport;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.ScrollPane;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.LayerConstants;
import org.eclipse.jface.resource.ImageRegistry;

import uk.ac.kcl.cch.jb.pliny.figures.ScalableImageFigure;
import uk.ac.kcl.cch.jb.pliny.imageRes.ImageResPlugin;
import uk.ac.kcl.cch.jb.pliny.imageRes.model.ImageResource;

/**
 * the image for the root GEF editPart for the Image Editor.  Takes the image specified
 * by the given 
 * {@link uk.ac.kcl.cch.jb.pliny.imageRes.model.ImageResource ImageResource} 
 * and prepares it for the use of the root editPart for display in the GEF-managed
 * annotation area. 
 * 
 * @author John Bradley
 *
 */

public class ImageFigure extends ScrollPane   {
	
	private static ImageRegistry imageRegistry = ImageResPlugin.getDefault().getImageRegistry();

	private String myUrl = null;
	private ScalableImageFigure imageFigure = null;
	private org.eclipse.swt.graphics.Image myImage;
	private ImageResource model;
	private IFigure pane;
	private IFigure figurePane;
	private ConnectionLayer myConnectionLayer;
	int imageShiftX = 100;
	int imageShiftY = 100;
	int zoomSize = 0;
	
	public ConnectionLayer getMyConnectionLayer(){
		return myConnectionLayer;
	}
	
	public void dispose(){
		//if(myImage != null)myImage.dispose();
		if(imageFigure != null)imageFigure.dispose();
		imageFigure = null;
	}
	
	private void setupImage(){
		if(imageFigure != null){
			
		    //pane.remove(imageFigure.getFigure());
			((FreeformLayer)figurePane).removeAll();
		    imageFigure.dispose();
	    }
	    imageFigure = new ScalableImageFigure(myImage);
	    if(model.getZoomSize() == 0){
			if(imageFigure.getOriginalWidth() > 500)imageFigure.setWidth(500);
	    } else
	    	imageFigure.setWidth(model.getZoomSize());
	    	//imageFigure.setWidth(model.getZoomSize()*imageFigure.getOriginalWidth()/100);
	    figurePane.add(imageFigure.getFigure(),new Rectangle(imageShiftX,imageShiftY,-1,-1));
	}
	
	private void doSetup(){
		FreeformLayeredPane innerLayers = new FreeformLayeredPane();
		if(myUrl != null){
	    	// first add image to the backing pane.
	    	
			figurePane = new FreeformLayer();
			figurePane.setLayoutManager(new FreeformLayout());
			figurePane.setOpaque(true);
			figurePane.setBackgroundColor(ColorConstants.white);
			//figurePane.setBackgroundColor(ColorConstants.lightGray);
			try {
			   myImage = imageRegistry.get(myUrl);
			} catch (Exception e){
				e.printStackTrace();
			}
			setupImage();
	    	innerLayers.add(figurePane,null, -1); // is constraint null right?
	    	
	    	// now, provide layer into which annotation material will go
	    	
	    	pane = new FreeformLayer();
	    	pane.setLayoutManager(new FreeformLayout());
			pane.setBorder(new LineBorder(1));
	    	innerLayers.add(pane, null, -1); // add layer over top
	    	
	    	myConnectionLayer = new ConnectionLayer();
	    	innerLayers.add(myConnectionLayer, LayerConstants.CONNECTION_LAYER, -1);
	    }
		FreeformViewport myViewPort = new FreeformViewport();
		myViewPort.setContents(innerLayers);
		setViewport(myViewPort);
	}
	
	/**
	 * creates an empty place-holder instance of this class.
	 *
	 */
	public ImageFigure(){
		super();
		doSetup();
	}
	/*
	public ImageFigure(String url, int shiftX, int shiftY){
		super();
		imageShiftX = shiftX;
		imageShiftY = shiftY;
		setUrl(url);
	}
	*/
	
	/**
	 * creates an instance of this class for the given
	 * {@link uk.ac.kcl.cch.jb.pliny.imageRes.model.ImageResource ImageResource}.  The image is placed so that it
	 * is shifted by the specified amount.
	 * 
	 * @param model the ImageResource to which this figure belongs.
	 * @param shiftX the number of pixels to shift the image in the X direction
	 * @param shiftY the number of pixels to shift the image in the Y direction
	 */
	public ImageFigure(ImageResource model, int shiftX, int shiftY){
		super();
		imageShiftX = shiftX;
		imageShiftY = shiftY;
		this.model = model;
		zoomSize = this.model.getZoomSize();
		setUrlFromModel();
	}
	
	public ScalableImageFigure getScalableImageFigure(){
		return imageFigure;
	}
	
	public IFigure getImageContentsFigure(){
		return pane;
		//return imageFigure;
	}
	
	/**
	 * instructs the figure to create generate itself to the given
	 * zoomsize.
	 * 
	 * @param zoomSize the Zoomsize (in pixel width) for the image to
	 * be displayed.
	 */
	public void setZoomSize(int zoomSize){
		if(zoomSize == this.zoomSize)return;
		this.zoomSize = zoomSize;
		setupImage();
		//this.repaint();
	}
	
	/**
	 * sets up the image based on the URL found in the provided ImageResource.
	 *
	 */
	public void setUrlFromModel(){
		if(model == null)return;
		myUrl = model.getUrl();
		if(imageRegistry.getDescriptor(model.getUrl()) == null)
		   imageRegistry.put(model.getUrl(), model.getMyImageDescriptor());
		doSetup();
	}
}
