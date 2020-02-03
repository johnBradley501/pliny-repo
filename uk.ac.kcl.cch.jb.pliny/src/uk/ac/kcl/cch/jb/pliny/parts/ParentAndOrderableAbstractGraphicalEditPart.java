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

import org.eclipse.gef.EditPart;

//import uk.ac.kcl.cch.jb.pliny.model.ReferencerManager;


/**
 * extends the Z-ordering functions provided in {@link OrderableAbstractGraphicalEditPart}
 * to provide a common base for managing the referencer manager
 * ({@link uk.ac.kcl.cch.jb.pliny.model.ReferencerManager ReferencerManager}).
 * 
 * @author John Bradley
 *
 */
abstract public class ParentAndOrderableAbstractGraphicalEditPart extends
		OrderableAbstractGraphicalEditPart 
		implements IMoveableChildrenEditPart {
	
	//private ReferencerManager referencerManager = new ReferencerManager(this);
	
	//public ReferencerManager getReferencerManager(){
	//	return referencerManager;
	//}
	
	//protected void addReferencerManager(Vector children){
	//	if(getReferencerManager().getCurrentPart() != null)
	//		children.add(getReferencerManager());
	//}
	
	public void MoveToPosition(EditPart thisOne, int pos){
		int realPos = pos;
		//if(referencerManager.getReferencerListObject() != null)++realPos;
		removeChildVisual(thisOne);
		if(realPos >= getChildren().size())addChildVisual(thisOne, -1);
		else addChildVisual(thisOne, realPos);
		//this.refreshChildren();
	}
}
