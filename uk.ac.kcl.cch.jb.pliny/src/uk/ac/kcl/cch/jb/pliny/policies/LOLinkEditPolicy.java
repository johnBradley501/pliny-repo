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
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;

import uk.ac.kcl.cch.jb.pliny.commands.LOLinkCreateCommand;
import uk.ac.kcl.cch.jb.pliny.commands.MoveLinkEndCommand;
import uk.ac.kcl.cch.jb.pliny.model.IHoldsLinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.Link;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.MapContentHolder;

/**
 * GEF edit policy of type <code>EditPolicy.GRAPHICAL_NODE_ROLE</code> to 
 * support the creation and manipulation of connections between reference objects and/or
 * anchors.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.parts.LinkableObjectBasePart
 * @see uk.ac.kcl.cch.jb.pliny.parts.MapContentHolderPart
 * 
 * @author John Bradley
 *
 */

public class LOLinkEditPolicy extends GraphicalNodeEditPolicy {
	
	private LinkableObject getLinkableObject(){
		Object linkEnd = getHost().getModel();
		if(linkEnd instanceof LinkableObject)return (LinkableObject)linkEnd;
		if(linkEnd instanceof IHoldsLinkableObject)
			return ((IHoldsLinkableObject)linkEnd).getLinkableObject();
		return null;
	}

	protected Command getConnectionCompleteCommand(
	    CreateConnectionRequest request) {
		//System.out.println("LOLinkEditPolicy: getConnectionCompleteCommand ======");
		LinkableObject linkEnd = getLinkableObject();
		//System.out.println("   linkEnd: "+linkEnd);
		if(linkEnd == null)return null;
		
		LOLinkCreateCommand myCommand = (LOLinkCreateCommand) request.getStartCommand();
		if(myCommand == null)return null;
		myCommand.setTo(linkEnd);
		myCommand.setToWork(null);
		if(myCommand.getFrom().getDisplayedIn().getALID() !=
			((LinkableObject)linkEnd).getDisplayedIn().getALID())
			return null;
		if(myCommand.isComplete())return myCommand;
	    return null;
	}

	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		//System.out.println("LOLinkEditPolicy: getConnectionCreateCommand ======");
		LinkableObject linkEnd = getLinkableObject();
		//System.out.println("   linkEnd: "+linkEnd);
		if(linkEnd == null)return null;
		
		LOLinkCreateCommand myCommand = new LOLinkCreateCommand();
        myCommand.setFrom(linkEnd);
        myCommand.setFromWork(null);
		request.setStartCommand(myCommand);
		return myCommand;
	}

	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		Link link = (Link) request.getConnectionEditPart().getModel();
		LinkableObject linkEnd = getLinkableObject();
		if(linkEnd == null)return null;
		
		MoveLinkEndCommand myCommand = new MoveLinkEndCommand(link);
		myCommand.setToEnd(linkEnd);
		if(myCommand.getFromEnd().getDisplayedIn().getALID() !=
			linkEnd.getDisplayedIn().getALID())
			return null;
		return myCommand;
	}

	protected Command getReconnectSourceCommand(ReconnectRequest request) {
		Link link = (Link) request.getConnectionEditPart().getModel();
		LinkableObject linkEnd = getLinkableObject();
		if(linkEnd == null)return null;

		MoveLinkEndCommand myCommand = new MoveLinkEndCommand(link);
		myCommand.setFromEnd(linkEnd);
		return myCommand;
	}

}
