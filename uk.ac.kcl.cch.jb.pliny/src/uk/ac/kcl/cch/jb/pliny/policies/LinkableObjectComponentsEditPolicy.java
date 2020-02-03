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
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import uk.ac.kcl.cch.jb.pliny.commands.DeleteLinkableObjectCommand;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.parts.LinkableObjectBasePart;

/**
 * GEF edit policy of type <code>EditPolicy.COMPONENT_ROLE</code> to 
 * support the deletion of reference objects.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.parts.LinkableObjectBasePart
 * 
 * @author John Bradley
 *
 */

public class LinkableObjectComponentsEditPolicy extends ComponentEditPolicy {

	protected Command createDeleteCommand(GroupRequest request) {
		Object toDeletePart = getHost();
		//Command myCommand = null;
		if(!(toDeletePart instanceof LinkableObjectBasePart)) return null;
		
		LinkableObject lo = ((LinkableObjectBasePart)toDeletePart).getLinkableObject();
		
		return new DeleteLinkableObjectCommand(lo);
	}

}
