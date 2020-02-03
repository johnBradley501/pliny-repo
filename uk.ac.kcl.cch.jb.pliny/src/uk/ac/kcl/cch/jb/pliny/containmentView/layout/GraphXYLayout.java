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

package uk.ac.kcl.cch.jb.pliny.containmentView.layout;

import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;

import uk.ac.kcl.cch.jb.pliny.containmentView.parts.ContainmentSetPart;

/**
 * Subclass of XYLayout which can use the child figures actual bounds as a constraint
 * when doing manual layout (XYLayout).  This code was borrowed with only minor
 * changes from 
 * com.realpersist.gef.schemaeditor with thanks to the original authors.
 * 
 * @author Phil Zoio
 */
public class GraphXYLayout extends FreeformLayout {
	
	private ContainmentSetPart diagram;

	public GraphXYLayout(ContainmentSetPart diagram) {
		this.diagram = diagram;
	}

	public Object getConstraint(IFigure child)
	{
		Object constraint = constraints.get(child);
		if (constraint != null || constraint instanceof Rectangle)
		{
			return (Rectangle)constraint;
		}
		else
		{
			Rectangle currentBounds = child.getBounds();
			return new Rectangle(currentBounds.x, currentBounds.y, -1,-1);
		}
	}

}
