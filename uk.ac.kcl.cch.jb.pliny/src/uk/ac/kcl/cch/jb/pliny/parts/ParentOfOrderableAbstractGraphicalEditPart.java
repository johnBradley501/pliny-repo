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

import java.util.Vector;

import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.gef.tools.DeselectAllTracker;
import org.eclipse.gef.tools.MarqueeDragTracker;

// import uk.ac.kcl.cch.jb.pliny.model.ReferencerManager;

/**
 * provides methods needed to allow its children GEF editparts to have their
 * Z-ordering managed so that currently selected items can be moved
 * to the top.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.parts.OrderableAbstractGraphicalEditPart
 * 
 * @author John Bradley
 *
 */

abstract public class ParentOfOrderableAbstractGraphicalEditPart 
        extends AbstractGraphicalEditPart 
		implements IMoveableChildrenEditPart {
	
	//private ReferencerManager referencerManager = new ReferencerManager(this);

	//public ReferencerManager getReferencerManager(){
	//	return referencerManager;
	//}

	public DragTracker getDragTracker(Request req){
		if (req instanceof SelectionRequest 
			&& ((SelectionRequest)req).getLastButtonPressed() == 3)
				return new DeselectAllTracker(this);
		return new MarqueeDragTracker();
	}

	protected void addReferencerList(Vector children){ // this now does nothing, and should be removed in time.  jb
	//	if(getReferencerManager().getCurrentPart() != null)
	//		children.add(getReferencerManager().getReferencerListObject());
	}
	
	public void MoveToPosition(EditPart thisOne, int pos){
		removeChildVisual(thisOne);
		if(pos >= getChildren().size())addChildVisual(thisOne, -1);
		else addChildVisual(thisOne, pos);
		//this.refreshChildren();
	}
}
