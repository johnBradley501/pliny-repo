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

import org.eclipse.gef.GraphicalEditPart;

import uk.ac.kcl.cch.rdb2java.dynData.BaseObject;

/**
 * identifes that this GEF edit part displays buttons that manage
 * the switching between open(expanded) and minimized display, and
 * between displaying the map/reference area and the content area.
 * 
 * @author John Bradley
 *
 */

public interface IButtonHolderPart extends GraphicalEditPart {
	
	/**
	 * toggles open/mininized status of the edit part.
	 *
	 */
	public void switchOpenStatus();
	
	
	/**
	 * requests the opening of the full editor for the surrogate of
	 * the LinkableOject associated with this GEF editpart.
	 *
	 */

    public void openFullEditor();

    /**
	 * toggles reference/content display status of the edit part.
	 *
	 */

    public void switchMapStatus();

    /**
	 * returns the current reference/content display status of the edit part.
	 * 
	 * @return <code>true</code> if reference (map) display is currently set.
	 *
	 */

    public boolean getMapStatus();
    
    /**
     * returns the 
     * {@link uk.ac.kcl.cch.rdb2java.dynData.BaseObject BaseObject} 
     * object that is the model for this editPart.
     * 
     * @return BaseObject
     */
    public BaseObject getHeldObject();
}
