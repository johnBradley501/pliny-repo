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

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.text.FlowPage;
import org.eclipse.draw2d.text.ParagraphTextLayout;
import org.eclipse.draw2d.text.TextFlow;
import org.eclipse.swt.graphics.Color;

/**
 * Creates the draw2d Figure that represents the MVC View for displaying
 * the textual content of {@link uk.ac.kcl.cch.jb.pliny.model.NoteLucened}s
 * in a GEF generated reference/annotation area -- what the Pliny help pages
 * calls the content area of <i>Reference Object</i>.
 * <p>
 * This makes use of draw2d's FlowPage and related objects to layout the text.
 * 
 * @author John Bradley
 *
 */
public class NoteTextFigure extends Figure {

	private String myText;
	private TextFlow textFlow;
	//private RectangleFigure backgroundFigure = null;
	//private Color backgroundColour;
	
	public NoteTextFigure(String text, Color background, Color foreground){
		if(text == null)text = "";
		//setLayoutManager(new BorderLayout());
		setLayoutManager(new StackLayout());
		setBackgroundColor(background);
		//backgroundColour = background;
		setForegroundColor(foreground);
		FlowPage flowPage = new FlowPage();
		//setMinimumSize(this.getBounds().getSize());

		//BorderLayout layout = new BorderLayout();
	    //setLayoutManager(layout);
		myText = text;
		textFlow = new TextFlow();

		textFlow.setLayoutManager(new ParagraphTextLayout(textFlow,
						ParagraphTextLayout.WORD_WRAP_SOFT));

		textFlow.setText(text);
		textFlow.setOpaque(true);
		flowPage.add(textFlow);
        //add(flowPage, BorderLayout.CENTER);
        add(flowPage);
        setOpaque(true);
        //System.out.println("NoteTextFigure init size:"+this.getSize());
	}
	
	public void setText(String text){
		if(text == null)text = "";
		myText = text;
		textFlow.setText(text);
		//getParent().getParent().repaint();
		repaint();
	    //System.out.println("NoteTextFigure repaint textflow size:"+textFlow.getSize());
	}
	
	public Dimension getTextSize(){
		return textFlow.getSize();
	}
	
	/*
	public void setBounds(Rectangle rect){
		if(backgroundFigure == null){
		   backgroundFigure = new RectangleFigure();
		   backgroundFigure.setBackgroundColor(backgroundColour);
		   add(backgroundFigure);
		}
		backgroundFigure.setBounds(this.getParent().getBounds());
		super.setBounds(rect);
	}*/
	
	public String getText(){
		return myText;
	}
	
	public void setColours(Color background, Color foreground){
		setBackgroundColor(background);
		//backgroundColour = background;
		//backgroundFigure.setBackgroundColor(backgroundColour);
		setForegroundColor(foreground);
		repaint();
	}
}
