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

import java.util.List;

import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

import uk.ac.kcl.cch.jb.pliny.containmentView.parts.ContainmentSetPart;
/**
 * Uses the DirectedGraphLayoutVisitor to automatically lay out figures on Containment
 * View's screen.  This code was borrowed with only minor changes from 
 * <code>com.realpersist.gef.schemaeditor</code> with thanks to the 
 * original authors.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.containmentView.parts.ContainmentSetPart
 * 
 * @author Phil Zoio, minor adaptations John Bradley
 */

public class GraphLayoutManager extends AbstractLayout {
	
	private ContainmentSetPart diagram;

	public GraphLayoutManager(ContainmentSetPart diagram) {
		this.diagram = diagram;
	}

	
	protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint)
	{		
		container.validate();
		List children = container.getChildren();
		Rectangle result = new Rectangle().setLocation(container.getClientArea().getLocation());
		for (int i = 0; i < children.size(); i++)
			result.union(((IFigure) children.get(i)).getBounds());
		result.resize(container.getInsets().getWidth(), container.getInsets().getHeight());
		return result.getSize();		
	}

	
	public void layout(IFigure container)
	{

		GraphAnimation.recordInitialState(container);
		if (GraphAnimation.playbackState(container))
			return;
	
		new DirectedGraphLayoutVisitor().layoutDiagram(diagram);
		// diagram.setTableModelBounds();

	}
}
