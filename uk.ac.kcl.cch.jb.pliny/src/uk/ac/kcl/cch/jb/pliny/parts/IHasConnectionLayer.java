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

import org.eclipse.draw2d.ConnectionLayer;

/**
 * announces that the figure associated with this GEF editpart has a connection
 * layer (for drawing the Pliny connection lines between reference objects).
 * <p>
 * This is needed because the reference/annotation areas for Pliny want the
 * ordering of the connection layer to be different than the one normally used
 * by GEF, and the connection layer to use must be explicitly provided to those
 * GEF connection objects that draw them.
 * 
 * @author John Bradley
 *
 */

public interface IHasConnectionLayer {
	
	/**
	 * returns the connection layer belonging to this editpart that a 
	 * connection child should use.
	 * 
	 * @return ConnectionLayer to use.
	 */
	public ConnectionLayer getMyConnectionLayer();
}
