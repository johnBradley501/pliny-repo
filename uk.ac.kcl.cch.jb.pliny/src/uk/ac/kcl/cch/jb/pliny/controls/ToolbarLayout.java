/*******************************************************************************
 * Copyright (c) 2003, 2005, 2007 IBM Corporation, John Bradley and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *     John Bradley - modifications to fit Pliny model
 *******************************************************************************/

package uk.ac.kcl.cch.jb.pliny.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;

/**
 * Copied code from the internal code section of the embedded
 * Eclipse browser <code>org.eclipse.ui.internal.browser</code>.
 * Copied because original version is not available for reuse
 * in my code.
 *
 */

public class ToolbarLayout extends Layout {
	private static final int SPACING = 5;
	private static final int EXTRA_BUSY_SPACING = 2;
	private static final int MARGIN = 2;

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite, int, int, boolean)
	 */
	protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
		if (hHint != SWT.DEFAULT)
			return new Point(wHint, hHint);
		
		Control[] children = composite.getChildren();
		int h = 0;
		int size = children.length;
		for (int i = 0; i < size; i++) {
			h = Math.max(h, children[i].computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		}
		
		return new Point(wHint, h + MARGIN * 2);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite, boolean)
	 */
	protected void layout(Composite composite, boolean flushCache) {
		Control[] children = composite.getChildren();
		Rectangle r = composite.getClientArea();
		
		int size = children.length;
		Point[] sizes = new Point[size];
		for (int i = 0; i < size; i++) {
			sizes[i] = children[i].computeSize(SWT.DEFAULT, SWT.DEFAULT);
		}

		int h = r.height - MARGIN * 2;
		
		// put busy icon at right with a little extra spacing
		int busy = size - 1;
		children[busy].setBounds(r.x + r.width - MARGIN - sizes[busy].x,
				r.y + MARGIN + (h-sizes[busy].y) / 2, 
				sizes[busy].x, sizes[busy].y);
		
		// find combo
		int combo = -1;
		int tw = r.width - MARGIN * 2 - (size - 1) * SPACING
				- sizes[size-1].x - EXTRA_BUSY_SPACING;
		for (int i = 0; i < size - 1; i++) {
			if ((children[i] instanceof Combo) || (children[i] instanceof StyledText)) // jb added StyledText to Combo
				combo = i;
			else
				tw -= sizes[i].x;
		}
		if (combo >= 0)
			sizes[combo].x = tw;
		
		// space out other children with their standard size, give combo all
		// remaining space (if it exists)
		int x = MARGIN;
		for (int i = 0; i < size - 1; i++) {
			children[i].setBounds(r.x + x, r.y + MARGIN + (h-sizes[i].y) / 2,
					sizes[i].x, sizes[i].y);
			x += SPACING + sizes[i].x;
		}
	}
}
