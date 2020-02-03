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

import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayeredPane;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.FreeformViewport;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ScrollPane;
import org.eclipse.gef.LayerConstants;

/**
 * Creates the draw2d Figure that represents the MVC View for 
 * {@link uk.ac.kcl.cch.jb.pliny.model.MapContentHolder}s
 * in a GEF generated reference/annotation area -- the content
 * part for what the Pliny help pages
 * calls the <i>Reference Object</i> that displays the surrogate's
 * reference/annotation area.
 * 
 * @author John Bradley
 */

public class MapContentFigure extends ScrollPane {

	private IFigure contentsLayer;
	private ConnectionLayer myConnectionLayer;

	public MapContentFigure() {
		super();
		//setBorder(new LineBorder(ColorConstants.red,1));
		FreeformViewport viewPort = new FreeformViewport();
		//viewPort.setSize(this.getSize());
		
    	setViewport(viewPort);
    	FreeformLayeredPane flPane = new FreeformLayeredPane();
    	
    	contentsLayer = new FreeformLayer();
    	//contentsLayer.setBounds(new Rectangle(new Point(0,0),this.getSize()));
    	contentsLayer.setLayoutManager(new FreeformLayout());
    	//setBackgroundColor(ColorConstants.white);
    	
    	myConnectionLayer = new ConnectionLayer();

    	flPane.add(myConnectionLayer, LayerConstants.CONNECTION_LAYER);
    	flPane.add(contentsLayer, null, -1); // add layer over top

    	setContents(flPane);
	}

	public IFigure getContentsFigure(){
		return contentsLayer;
	}
	
	public ConnectionLayer getMyConnectionLayer(){
		return myConnectionLayer;
	}

	
}
