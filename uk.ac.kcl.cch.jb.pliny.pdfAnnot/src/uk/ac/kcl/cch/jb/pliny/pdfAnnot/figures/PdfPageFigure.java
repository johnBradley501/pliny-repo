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

package uk.ac.kcl.cch.jb.pliny.pdfAnnot.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayeredPane;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.FreeformViewport;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.ScrollPane;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.LayerConstants;

import uk.ac.kcl.cch.jb.pliny.pdfAnnot.model.PdfResource;

/**
 * the image for the root GEF editPart for the PDF Editor.  Takes the image 
 * for the current page specified
 * by the given 
 * {@link uk.ac.kcl.cch.jb.pliny.pdfAnnot.model.PdfResource PdfResource} 
 * and prepares it for the use of the root editPart for display in the GEF-managed
 * annotation area.
 * 
 * @author John Bradley
 *
 */public class PdfPageFigure extends ScrollPane {

	private PdfResource resource = null;
	private int imageShiftX, imageShiftY;

	private IFigure figurePane;
	private IFigure pane;
	private ConnectionLayer myConnectionLayer;

	public PdfPageFigure(PdfResource resource, int imageShiftX, int imageShiftY) {
		super();
		this.resource = resource;
		this.imageShiftX = imageShiftX;
		this.imageShiftY = imageShiftY;
		doSetup();
	}

	private void doSetup() {
		FreeformLayeredPane innerLayers = new FreeformLayeredPane();
		if(resource != null){
			figurePane = new FreeformLayer();
			figurePane.setLayoutManager(new FreeformLayout());
			figurePane.setOpaque(true);
			figurePane.setBackgroundColor(ColorConstants.lightGray);
			ImageFigure pageImage = new ImageFigure(resource.getPageImage());
			
			figurePane.add(pageImage, new Rectangle(imageShiftX,imageShiftY,-1,-1));
	    	innerLayers.add(figurePane,null, -1); // is constraint null right?

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

	public IFigure getImageContentsFigure(){
		return pane;
	}
	
	public void updateImage(){
		((FreeformLayer)figurePane).removeAll();
		ImageFigure pageImage = new ImageFigure(resource.getPageImage());
		
		figurePane.add(pageImage, new Rectangle(imageShiftX,imageShiftY,-1,-1));
	}
}
