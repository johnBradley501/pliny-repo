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

import java.util.Iterator;

import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

/**
 * Manages the Z ordering of items displayed by GEF which need to
 * be manipulated while the user selects an item for editing.
 * The UI we use in this
 * software assumes that as soon as a user clicks on an object
 * it is (while it is selected) brought to the front of the Z
 * dimension.  This is done by using <code>MoveToPosition</code> to move
 * an object to the front when it is selected, and then back to its
 * original position when it is deselected.
 * 
 * <p>This only works when the parent object can co-operate.
 * Thus, parent classes must implement {@link IMoveableChildrenEditPart}.
 * 
 * @author John Bradley
 */

public abstract class OrderableAbstractGraphicalEditPart extends
		AbstractGraphicalEditPart {
	
	private int myPosition = 0;

	private int getMyPosition(){
		if(myPosition == 0){
		   Iterator it = getParent().getChildren().iterator();
		
		   while(it.hasNext()){
			  myPosition++;
			  if(it.next()==this)return myPosition;
		   }
		   myPosition = 1000;
		}
		return myPosition;
	}
	
	private void reOrder(int value){
		//GraphicalEditPart myPart = (GraphicalEditPart)getHost();
		if(!(getParent() instanceof IMoveableChildrenEditPart))return;
		IMoveableChildrenEditPart parent = (IMoveableChildrenEditPart)getParent();
		getMyPosition();

		if(value == 2){
			parent.MoveToPosition(this, -1);
		} else if(value == 0){
			parent.MoveToPosition(this, myPosition);
		}
	}
	
	/**
	 * overrides inherited method to tell parent to move selected item to top.
	 * GEF will provide the kind of selection in the given parameter. A
	 * kind of zero means "unselected".
	 * 
	 * @param value the value provided by GEF for the kind of selection
	 */
	
	public void setSelected(int value){
		//super.setSelected(value);
		//System.out.println("OrderableAbstractSelected: "+getModel().toString());
		//super.setSelected(value);

		if(value == 2){
		  reOrder(value);
		  super.setSelected(value);
		} else if(value == 0){
			super.setSelected(value);
			reOrder(value);
		} else super.setSelected(value);
	}
	
	public void superSetSelected(int value){
		super.setSelected(value);
	}
}
