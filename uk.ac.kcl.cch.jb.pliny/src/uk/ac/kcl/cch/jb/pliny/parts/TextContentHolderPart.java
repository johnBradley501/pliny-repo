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

import java.util.List;
import java.util.Vector;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import uk.ac.kcl.cch.jb.pliny.figures.TextContentFigure;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.TextContentHolder;

/**
 * this GEF editPart manages its associated reference object's content area
 * when it is displaying the text area of a NoteLucene.
 * Model data for this area comes from the
 * {@link uk.ac.kcl.cch.jb.pliny.model.TextContentHolder TextContentHolder}
 * which acts as a holder for the 
 * {@link uk.ac.kcl.cch.jb.pliny.model.NoteLucened NoteLucened} which owns the
 * appropriate reference object.
 * 
 * @author John Bradley
 *
 */

public class TextContentHolderPart extends AbstractGraphicalEditPart {

	public TextContentHolderPart(TextContentHolder model) {
		super();
		setModel(model);
		// TODO Auto-generated constructor stub
	}

	protected IFigure createFigure() {
		return new TextContentFigure();
	}

	public IFigure getContentPane(){
		return ((TextContentFigure)getFigure()).getContentsFigure();
	}

	protected void createEditPolicies() {
		// none needed -- this part provides a figure link only
	}
	
	public List getModelChildren(){
		Vector children = new Vector();
		NoteLucened myObject = ((TextContentHolder)getModel()).getObject();
		children.add(myObject);
		return children;
	}

}
