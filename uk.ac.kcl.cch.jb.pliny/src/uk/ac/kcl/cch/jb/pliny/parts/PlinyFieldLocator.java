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

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Text;

/**
 * adapts the GEF CellEditorLocator to meet the need of Pliny's
 * direct-editable items.
 * 
 * @author John Bradley
 *
 */

public class PlinyFieldLocator implements CellEditorLocator {

	private Figure theFigure;

	public PlinyFieldLocator(Figure theFigure) {
		setLabel(theFigure);
	}

	public void relocate(CellEditor celleditor) {
		Text text = (Text)celleditor.getControl();
		Rectangle rect = theFigure.getClientArea().getCopy();
		//Rectangle rect = theFigure.getAnnotationTextFlow().getClientArea().getCopy();
		//System.out.println("PlinyFieldLocator.relocate rect: x:"+rect.x+", y: "+rect.y+", height: "+rect.height+", width: "+rect.width);
		theFigure.translateToAbsolute(rect);
		//theFigure.getAnnotationTextFlow().translateToAbsolute(rect);
		org.eclipse.swt.graphics.Rectangle trim = text.computeTrim(0, 0, 0, 0);
		rect.translate(trim.x, trim.y);
		rect.width += trim.width;
		rect.height += trim.height;
		text.setBounds(rect.x, rect.y, rect.width, rect.height);
	}
	

	/**
	 * Stores the AnnotationFigure figure.
	 * 
	 * @param theFigure the figure to which this item is to attach.
	 */
	protected void setLabel(Figure theFigure) {
		this.theFigure = theFigure;
	}
}
