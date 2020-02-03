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

package uk.ac.kcl.cch.jb.pliny.policies;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;

import uk.ac.kcl.cch.jb.pliny.commands.LinkableObjectNameUpdateCommand;
import uk.ac.kcl.cch.jb.pliny.figures.LinkableObjectFigure;
import uk.ac.kcl.cch.jb.pliny.parts.LinkableObjectPart;

/**
 * GEF edit policy of type <code>EditPolicy.DIRECT_EDIT_ROLE</code> to 
 * support the direct editing of the name of a reference object.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.parts.LinkableObjectPart
 * 
 * @author John Bradley
 *
 */

public class NameDirectEditPolicy extends DirectEditPolicy {

	/**
	 * @see DirectEditPolicy#getDirectEditCommand(DirectEditRequest)
	 */
	protected Command getDirectEditCommand(DirectEditRequest request) {
		String nameText = (String)request.getCellEditor().getValue();
		LinkableObjectPart part = (LinkableObjectPart)getHost();
		return new LinkableObjectNameUpdateCommand(part.getLinkableObject(), nameText);
	}

	/**
	 * @see DirectEditPolicy#getDirectEditCommand(DirectEditRequest)
	 */
	protected void showCurrentEditValue(DirectEditRequest request) {
		String value = (String)request.getCellEditor().getValue();
		((LinkableObjectFigure)getHostFigure()).setName(value);
		//hack to prevent async layout from placing the cell editor twice.
		getHostFigure().getUpdateManager().performUpdate();
	}
}
