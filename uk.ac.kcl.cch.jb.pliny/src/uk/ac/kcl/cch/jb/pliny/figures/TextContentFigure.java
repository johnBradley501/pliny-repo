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
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.ScrollPane;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.draw2d.Viewport;

/**
 * Creates the draw2d Figure that provides a scolling object (draw2d ScrollPane)
 * that will display the textual content of a
 * {@link uk.ac.kcl.cch.jb.pliny.model.NoteLucened}.  The textual content itself
 * is displayed via the Figure {@link NoteTextFigure}.
 * 
 * @author John Bradley
 *
 */

public class TextContentFigure extends ScrollPane {

	private IFigure contentsLayer;
	//private RectangleFigure backgroundFigure = null;
	//private Color backgroundColour;

	public TextContentFigure() {
		super();
		//Color backgroundColour = PlinyPlugin.getColour(
		//		"textContentFigure.background",new RGB(240,255,240));
		//this.setBackgroundColor(backgroundColour);
		
		Viewport vp = new Viewport();
		vp.setContentsTracksWidth(true);
		//vp.setBackgroundColor(backgroundColour);
		//vp.setContentsTracksHeight(true);
		setViewport(vp);
		
		contentsLayer = new Panel();
		contentsLayer.setBorder(new MarginBorder(2));
		//contentsLayer.setLayoutManager(new ToolbarLayout());
		contentsLayer.setLayoutManager(new StackLayout());
		//contentsLayer.setBackgroundColor(ColorConstants.lightGreen);
		//contentsLayer.setMinimumSize(this.getBounds().getSize());
		contentsLayer.setOpaque(true);

		//backgroundFigure = new RectangleFigure();
		//backgroundFigure.setBackgroundColor(ColorConstants.lightGreen);
		//add(backgroundFigure);

		
		setContents(contentsLayer);
	}

	public IFigure getContentsFigure(){
		return contentsLayer;
	}
	
	public ConnectionLayer getMyConnectionLayer(){
		return null;
	}
}
