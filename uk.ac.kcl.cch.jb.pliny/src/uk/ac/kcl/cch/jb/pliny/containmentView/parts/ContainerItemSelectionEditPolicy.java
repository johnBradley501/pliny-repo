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

package uk.ac.kcl.cch.jb.pliny.containmentView.parts;

import org.eclipse.gef.editpolicies.NonResizableEditPolicy;

/**
 * this class is a GEF policy class of type <code>NonResizableEditPolicy</code>
 * and is used to manage selection handling by GEF in a way suitable
 * for the ContainmentView.
 * 
 * @author John Bradley
 *
 */

public class ContainerItemSelectionEditPolicy extends NonResizableEditPolicy {

	public ContainerItemSelectionEditPolicy() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public ContainmentItemFigure getMyFigure(){
		return (ContainmentItemFigure)getHostFigure();
	}
	/**
	 * @see org.eclipse.gef.editpolicies.NonResizableEditPolicy#hideFocus()
	 */
	protected void hideFocus() {
		//System.out.println("hideFocus: "+getHostFigure());
		getMyFigure().setFocus(false);
	}

	/**
	 * @see org.eclipse.gef.editpolicies.SelectionHandlesEditPolicy#hideSelection()
	 */
	protected void hideSelection() {
		//System.out.println("hideSelection: "+getHostFigure());
		getMyFigure().setSelected(false);
		getMyFigure().setFocus(false);
		
	}

	/**
	 * @see org.eclipse.gef.editpolicies.NonResizableEditPolicy#showFocus()
	 */
	protected void showFocus() {
		//System.out.println("showFocus: "+getHostFigure());
		getMyFigure().setFocus(true);
	}

	/**
	 * @see org.eclipse.gef.editpolicies.SelectionHandlesEditPolicy#showSelection()
	 */
	protected void showPrimarySelection() {
		//System.out.println("showPrimarySelection: "+getHostFigure());
		getMyFigure().setSelected(true);
		getMyFigure().setFocus(true);
	}

	/**
	 * @see org.eclipse.gef.editpolicies.SelectionHandlesEditPolicy#showSelection()
	 */
	protected void showSelection() {
		//System.out.println("showPrimarySelection: "+getHostFigure());
		getMyFigure().setSelected(true);
		getMyFigure().setFocus(false);
	}

}
