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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;

import uk.ac.kcl.cch.jb.pliny.commands.AddLinkableObjectsCommand;
import uk.ac.kcl.cch.jb.pliny.commands.CreateLinkableObjectCommand;
import uk.ac.kcl.cch.jb.pliny.commands.CreateNoteCommand;
import uk.ac.kcl.cch.jb.pliny.commands.CreateResourceCommand;
import uk.ac.kcl.cch.jb.pliny.commands.FixupLinksCommand;
import uk.ac.kcl.cch.jb.pliny.commands.LinkableObjectMoveCommand;
import uk.ac.kcl.cch.jb.pliny.model.IHasResource;
import uk.ac.kcl.cch.jb.pliny.model.IHoldsLinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.MapContentHolder;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.ResourceHolder;
import uk.ac.kcl.cch.jb.pliny.parts.LinkableObjectPart;

/**
 * GEF edit policy of type <code>EditPolicy.LAYOUT_ROLE</code> to 
 * support the creation and adding (as a part of reparenting) operation
 * for non-scalable reference areas.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.parts.RootResourcePart
 * @see uk.ac.kcl.cch.jb.pliny.parts.MapContentHolderPart
 * @see uk.ac.kcl.cch.jb.pliny.policies.ScalableImageXYLayoutEditPolicy
 * 
 * @author John Bradley
 *
 */

public class ResourceObjectsXYLayoutPolicy extends XYLayoutEditPolicy {

	public ResourceObjectsXYLayoutPolicy() {
		super();
		// TODO Auto-generated constructor stub
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
		//System.out.println("XYPolicy: getAddCommand editparts: "+editParts);
		CompoundCommand command = new CompoundCommand();
		command.setDebugLabel("Add in ConstrainedLayoutEditPolicy");//$NON-NLS-1$
		GraphicalEditPart childPart;
		Rectangle r;
		Object constraint;
		
		Set linkableObjects = new HashSet();
		Iterator it = editParts.iterator();
		while(it.hasNext()){
			childPart = (GraphicalEditPart)it.next();
			Object model = childPart.getModel();
			if(model instanceof LinkableObject)linkableObjects.add(model);
		}
		
		command.add(new FixupLinksCommand(linkableObjects));

		for (int i = 0; i < editParts.size(); i++) {
			childPart = (GraphicalEditPart)editParts.get(i);
			r = childPart.getFigure().getBounds().getCopy();
			//convert r to absolute from childpart figure
			childPart.getFigure().translateToAbsolute(r);
			r = request.getTransformedRectangle(r);
			//convert this figure to relative 
			getLayoutContainer().translateToRelative(r);
			getLayoutContainer().translateFromParent(r);
			r.translate(getLayoutOrigin().getNegated());
			constraint = getConstraintFor(r);
			command.add(createAddCommand(generic, childPart,
				translateToModelConstraint(constraint)));
		}
		
		return command.unwrap();
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
		else if(model instanceof ResourceHolder) resource = ((ResourceHolder)model).getResource();
		else if(model instanceof IHoldsLinkableObject)resource = ((IHoldsLinkableObject)model).getSurrogate();
		else return null;
		if((container != null) && (resource.getALID() != container.getSurrogateFor().getALID()))return null;
		AddLinkableObjectsCommand add = new AddLinkableObjectsCommand(container, resource, (LinkableObject)part, rect);
		//System.out.println("AddLinkableObjectsCommand:\n   container: "+container+
		//		"\n   resource: "+resource+"\n   part: "+part+"\n   rect: "+rect);
		return add;
	}

	protected Command createAddCommand(EditPart child, Object constraint) {
		// TODO Auto-generated method stub
		return null;
	}

	protected Command createChangeConstraintCommand(EditPart child,
			Object constraint) {
		if (!(constraint instanceof Rectangle))
			return null;
		if (child instanceof LinkableObjectPart)
			return handleLinkableObjectMove((AbstractGraphicalEditPart)child,constraint);
		return null;
	}

	private Command handleLinkableObjectMove(AbstractGraphicalEditPart part, Object constraint) {
		Figure figure = (Figure) part.getFigure();
		Rectangle oldBounds = figure.getBounds();
		Rectangle newBounds = (Rectangle) constraint;

		LinkableObjectMoveCommand command = new LinkableObjectMoveCommand((LinkableObject)part.getModel(), oldBounds, newBounds);
		return command;
	}

	protected Command getCreateCommand(CreateRequest request) {
		Object	newObjectType = request.getNewObjectType();
		Command	createCommand = null;
		Object targetObject = getTargetEditPart(request).getModel();
		Resource theResource = null;
		if(targetObject instanceof Resource)theResource = (Resource)targetObject;
		else if(targetObject instanceof LinkableObject)
			theResource = ((LinkableObject)targetObject).getSurrogateFor();
		else if(targetObject instanceof IHoldsLinkableObject)
			theResource = ((IHoldsLinkableObject)targetObject).getSurrogate();
		else if(targetObject instanceof IHasResource)
			theResource = ((ResourceHolder)targetObject).getResource();
		if(theResource == null)return null;
		//System.out.println("getCreateCommand targetObject:"+targetObject.toString());
		/*
		if(newObjectType == NoteLucened.class){
			CreateNoteCommand create = 
				new CreateNoteCommand(
						(NoteLucened)request.getNewObject(),
						theResource,
						(Rectangle)getConstraintFor(request));
			createCommand = create;
		} else if (newObjectType == LinkableObject.class){
			CreateLinkableObjectCommand create =
				new CreateLinkableObjectCommand((LinkableObject)request.getNewObject(),
						theResource,
						(Rectangle)getConstraintFor(request));
			createCommand = create;
		}
		*/
		Object newObject = request.getNewObject();
		if(newObject instanceof NoteLucened){
			CreateNoteCommand create =
				new CreateNoteCommand(
						(NoteLucened)newObject,
						theResource,
						(Rectangle)getConstraintFor(request));
			createCommand = create;
		} else if (newObject instanceof Resource){
			Resource newResource = (Resource)newObject;
			if(newResource.getObjectType() != null){
				CreateResourceCommand create =
					new CreateResourceCommand(
							newResource,
							theResource,
							(Rectangle)getConstraintFor(request));
				createCommand = create;
			}
		} else if (newObject instanceof LinkableObject){
			CreateLinkableObjectCommand create =
				new CreateLinkableObjectCommand((LinkableObject)newObject,
						theResource,
						(Rectangle)getConstraintFor(request));
			createCommand = create;
		} else if (newObject instanceof LOType){ 
			createCommand = new CreateNoteCommand((LOType)newObject, theResource,(Rectangle)getConstraintFor(request) );
		}
		return createCommand;
	}

	protected Command getDeleteDependantCommand(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

}
