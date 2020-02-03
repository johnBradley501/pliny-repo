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

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ContainerEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.GroupRequest;

import uk.ac.kcl.cch.jb.pliny.commands.OrphanLinkableObjectsCommand;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;

/**
 * GEF edit policy of type <code>EditPolicy.CONTAINER_ROLE</code> to 
 * support the direct editing of the textual content of a reference object
 * created by a <code>NoteLucened</code>.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.parts.LinkableObjectBasePart
 * @see uk.ac.kcl.cch.jb.pliny.parts.MapContentHolderPart
 * 
 * @author John Bradley
 *
 */

public class ResourceObjectsContainerEditPolicy extends ContainerEditPolicy {

	public ResourceObjectsContainerEditPolicy() {
		super();
	}

	protected Command getCreateCommand(CreateRequest request) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Command getOrphanChildrenCommand(GroupRequest request) {
		Object model = getHost().getModel();
		List parts = request.getEditParts();
		//System.out.println("getOrphanChildrenCommand parts: "+parts);
		Vector linkableObjects = new Vector();
		Iterator it = parts.iterator();
		while(it.hasNext()){
			Object child = ((EditPart)it.next()).getModel();
			if(child instanceof LinkableObject) linkableObjects.add(child);
		}
		//System.out.println("getOrphanChildrenCommand linkableObjects: "+linkableObjects);
		if(linkableObjects.size() == 0)return null;
		return new OrphanLinkableObjectsCommand(linkableObjects);
	}	

}
