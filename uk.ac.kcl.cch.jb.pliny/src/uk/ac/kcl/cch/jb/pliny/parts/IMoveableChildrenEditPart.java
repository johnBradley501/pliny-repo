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

import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;

// import uk.ac.kcl.cch.jb.pliny.model.ReferencerManager;

/**
 * announces that this GEF editpart is moveable, and that it has a
 * referencer manager.
 * 
 * @author John Bradley
 *
 */
public interface IMoveableChildrenEditPart extends GraphicalEditPart {
	   public void MoveToPosition(EditPart thisOne, int pos);
	   // public ReferencerManager getReferencerManager();

}
