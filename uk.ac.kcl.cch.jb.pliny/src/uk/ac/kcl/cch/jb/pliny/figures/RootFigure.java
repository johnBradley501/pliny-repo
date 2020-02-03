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
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.ScrollPane;
import org.eclipse.gef.LayerConstants;

/**
 * Creates the draw2d Figure that represents the base surface
 * for Pliny's reference/annotation area.
 * 
 * @author John Bradley
 *
 */

public class RootFigure extends ScrollPane {
	private ConnectionLayer myConnectionLayer;
	private IFigure contentsLayer;

	public RootFigure() {
		super();
		FreeformLayeredPane innerLayers = new FreeformLayeredPane();
    	contentsLayer = new FreeformLayer();
    	contentsLayer.setLayoutManager(new FreeformLayout());
		contentsLayer.setBorder(new LineBorder(1));

    	myConnectionLayer = new ConnectionLayer();

    	innerLayers.add(myConnectionLayer, LayerConstants.CONNECTION_LAYER, -1);
    	innerLayers.add(contentsLayer, null, -1);

		FreeformViewport myViewPort = new FreeformViewport();
		myViewPort.setContents(innerLayers);
		setViewport(myViewPort);
	}
	
	public ConnectionLayer getMyConnectionLayer(){
		return myConnectionLayer;
	}

	public IFigure getContentsFigure(){
		return contentsLayer;
	}
}
