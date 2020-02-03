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

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editpolicies.ContainerEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;

import uk.ac.kcl.cch.jb.pliny.commands.AddLinkableObjectsCommand;
import uk.ac.kcl.cch.jb.pliny.model.IHoldsLinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.MapContentHolder;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.parts.LinkableObjectPart;

/**
 * GEF edit policy of type <code>EditPolicy.COMPONENT_ROLE</code> to 
 * support the re-parenting operation.
 * 
 * @author John Bradley
 *
 */

public class LinkableObjectContainerEditPolicy extends ContainerEditPolicy {

	public LinkableObjectContainerEditPolicy() {
		super();
		// TODO Auto-generated constructor stub
	}

	protected Command getCreateCommand(CreateRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getAddCommand(org.eclipse.gef.Request)
	 * this overrides XYLayoutEditPolicy's getAddCommand so that a
	 * different createAddCommand, with different parameters, will
	 * be invoked than what XYLayoutEditPolicy does
	 */
	protected Command getAddCommand(Request generic) {
		ChangeBoundsRequest request = (ChangeBoundsRequest)generic;
		List editParts = request.getEditParts();
		CompoundCommand command = new CompoundCommand();
		command.setDebugLabel("Add in ConstrainedLayoutEditPolicy");//$NON-NLS-1$
		GraphicalEditPart childPart;
		Rectangle r;
		Object constraint;

		for (int i = 0; i < editParts.size(); i++) {
			childPart = (GraphicalEditPart)editParts.get(i);
			r = childPart.getFigure().getBounds().getCopy();
			//convert r to absolute from childpart figure
			childPart.getFigure().translateToAbsolute(r);
			r = request.getTransformedRectangle(r);
			//convert this figure to relative 
			getLayoutContainer().translateToRelative(r);
			getLayoutContainer().translateFromParent(r);
			//r.translate(getLayoutOrigin().getNegated());
			constraint = new Rectangle(r);
			command.add(createAddCommand(generic, childPart,constraint));
		}
		return command.unwrap();
	}
	/**
	 * Returns the host's {@link GraphicalEditPart#getContentPane() contentPane}. The
	 * contentPane is the Figure which parents the childrens' figures. It is also the figure
	 * which has the LayoutManager that corresponds to this EditPolicy. All operations should
	 * be interpreted with respect to this figure.
	 * @return the Figure that owns the corresponding <code>LayoutManager</code>
	 */
	protected IFigure getLayoutContainer() {
		return ((GraphicalEditPart)getHost()).getContentPane();
	}
	
	protected Command createAddCommand(Request request, EditPart childEditPart, 
			Object constraint) {
		Object part = childEditPart.getModel();
		if(!(part instanceof LinkableObject))return null;
		Rectangle rect = (Rectangle)constraint;
		Object model = getHost().getModel();
		Object parent = getHost().getParent();
		LinkableObject container = null;
		if(parent instanceof LinkableObjectPart){
			container = ((LinkableObjectPart)parent).getLinkableObject();
		}
		Resource resource;
		if(model instanceof Resource)resource = (Resource)model;
		else if(model instanceof IHoldsLinkableObject)resource = ((IHoldsLinkableObject)model).getSurrogate();
		else return null;
		if((container != null) && (resource.getALID() != container.getSurrogateFor().getALID()))return null;
		AddLinkableObjectsCommand add = new AddLinkableObjectsCommand(container, resource, (LinkableObject)part, rect);
		//System.out.println("AddLinkableObjectsCommand:\n   container: "+container+
		//		"\n   resource: "+resource+"\n   part: "+part+"\n   rect: "+rect);
		return add;
	}

}
