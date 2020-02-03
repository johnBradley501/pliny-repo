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

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;

import uk.ac.kcl.cch.jb.pliny.commands.CreateAnchorCommand;
import uk.ac.kcl.cch.jb.pliny.commands.CreateAnnotationCommand;
import uk.ac.kcl.cch.jb.pliny.commands.CreateLinkableObjectCommand;
import uk.ac.kcl.cch.jb.pliny.commands.CreateNoteCommand;
import uk.ac.kcl.cch.jb.pliny.commands.CreateResourceCommand;
import uk.ac.kcl.cch.jb.pliny.commands.LinkableObjectMoveCommand;
import uk.ac.kcl.cch.jb.pliny.model.Anchor;
import uk.ac.kcl.cch.jb.pliny.model.Annotation;
import uk.ac.kcl.cch.jb.pliny.model.IHasResource;
import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.parts.IScalableImagePart;
import uk.ac.kcl.cch.jb.pliny.parts.LinkableObjectBasePart;
import uk.ac.kcl.cch.jb.pliny.parts.LinkableObjectPart;

/**
 * GEF edit policy of type <code>EditPolicy.LAYOUT_ROLE</code> to 
 * support the creation and adding (as a part of reparenting) operation
 * for scalable reference areas.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.parts.RootResourcePart
 * @see uk.ac.kcl.cch.jb.pliny.parts.MapContentHolderPart
 * @see uk.ac.kcl.cch.jb.pliny.policies.ResourceObjectsXYLayoutPolicy
 * 
 * @author John Bradley
 *
 */

public class ScalableImageXYLayoutEditPolicy extends XYLayoutEditPolicy {

	public ScalableImageXYLayoutEditPolicy() {
		super();
		// TODO Auto-generated constructor stub
	}

	protected Command createAddCommand(EditPart child, Object constraint) {
		// TODO Auto-generated method stub
		return null;
	}

	protected Command createChangeConstraintCommand(EditPart child,
			Object constraint) {
		if (!(constraint instanceof Rectangle))
			return null;
		if(child instanceof LinkableObjectBasePart)
			return handleLinkableObjectMove((LinkableObjectBasePart)child, constraint);
		return null;
	}
	private Command handleLinkableObjectMove(LinkableObjectBasePart child, Object constraint) {
		LinkableObject surr = child.getLinkableObject();
		Figure figure = (Figure) child.getFigure();
		IScalableImagePart myParentPart = (IScalableImagePart)child.getParent();
		Rectangle oldBounds, newBounds;
		if(child instanceof LinkableObjectPart){
		    oldBounds = myParentPart.positionForImage(figure.getBounds());
		    newBounds = myParentPart.positionForImage((Rectangle)constraint);
		} else {
			oldBounds = myParentPart.scaleForImage(figure.getBounds());
			newBounds = myParentPart.scaleForImage((Rectangle)constraint);
			//System.out.println("move: image:"+newBounds+", display:"+constraint+", surr:"+surr.getALID());
		}
		return new LinkableObjectMoveCommand(surr, oldBounds, newBounds);
	}

	protected Command getCreateCommand(CreateRequest request) {
		Object	newObjectType = request.getNewObjectType();
		//System.out.println("getCreateCommand: "+request.getExtendedData());
		Command	createCommand = null;
		IScalableImagePart scalableImagePart = (IScalableImagePart)getHost();
		Object targetObject = scalableImagePart.getModel();
		Resource theResource = null;
		if(targetObject instanceof Resource)theResource = (Resource)targetObject;
		else if(targetObject instanceof IHasResource)
			theResource = ((IHasResource)targetObject).getResource();
		else return null;

		Rectangle bounds = (Rectangle)getConstraintFor(request);
		/*
		if(newObjectType == NoteLucened.class){
			createCommand = new CreateNoteCommand(
					(NoteLucened)request.getNewObject(),
					theResource,
					scalableImagePart.positionForImage(bounds));
		} else if (newObjectType == Annotation.class){
			createCommand = new CreateAnnotationCommand(
					theResource,
					scalableImagePart.scaleForImage(bounds));
		} else if (newObjectType == Anchor.class){
			createCommand = new CreateAnchorCommand(
					theResource,
					scalableImagePart.scaleForImage(bounds));
		} else if (newObjectType == LinkableObject.class){
			createCommand = new CreateLinkableObjectCommand(
					(LinkableObject)request.getNewObject(),
					theResource,
					scalableImagePart.positionForImage(bounds));
		}
		*/
		Object newObject = request.getNewObject();
		if(newObject instanceof NoteLucened){
			createCommand = new CreateNoteCommand(
					(NoteLucened)newObject,
					theResource,
					scalableImagePart.positionForImage(bounds));
		}else if(newObject instanceof Resource){
			Resource newResource = (Resource)newObject;
		    if(newResource.getObjectType() != null)
		    	createCommand = new CreateResourceCommand(
						newResource,
						theResource,
						scalableImagePart.positionForImage(bounds));
		} else if (newObject instanceof Annotation){
			createCommand = new CreateAnnotationCommand(
					theResource,
					scalableImagePart.scaleForImage(bounds));
		} else if (newObject instanceof Anchor){
			createCommand = new CreateAnchorCommand(
					theResource,
					scalableImagePart.scaleForImage(bounds));
		} else if (newObject instanceof LinkableObject){
			createCommand = new CreateLinkableObjectCommand(
					(LinkableObject)request.getNewObject(),
					theResource,
					scalableImagePart.positionForImage(bounds));
		} else if (newObject instanceof LOType){ 
			createCommand = new CreateNoteCommand((LOType)newObject, theResource, scalableImagePart.positionForImage(bounds) );
		}
		return createCommand;
	}

	protected Command getDeleteDependantCommand(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

}
