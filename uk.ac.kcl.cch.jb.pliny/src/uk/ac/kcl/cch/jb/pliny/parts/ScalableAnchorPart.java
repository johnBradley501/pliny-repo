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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;

import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
/**
 * extends {@link LinkableObjectBasePart} to handle the display
 * of anchors.  Anchors have no surrogate Resource in their
 * {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject LinkableObject}.
 * <p>
 * Note that ScalableAnchors appear in scalable contexts, where the actual
 * size on the screen depends not only on the size parameter in the
 * LinkableObject's data, but also on the current scale ratio which is
 * available from this part's parent editPart.
 * 
 * @author John Bradley
 *
 */

public class ScalableAnchorPart extends LinkableObjectBasePart {

	public ScalableAnchorPart(LinkableObject model) {
		super(model);
		// TODO Auto-generated constructor stub
	}

	protected IFigure createFigure() {
		Label myFigure = new Label();
		myFigure.setBorder(new LineBorder(getMyType().getTitleBackColour(),2));
		return myFigure;
	}
	
	protected Rectangle mapLocation(Rectangle r){
		Rectangle rslt = new Rectangle(r);
		rslt.setLocation(((IScalableImagePart)getParent()).alignForDisplay(r.getLocation()));
		return rslt;
	}

	public void refreshVisuals(){
		LinkableObject model = getLinkableObject();
	    Rectangle r = model.getDisplayRectangle();
	    IScalableImagePart ip = (IScalableImagePart)getParent();
	    //Rectangle r2 = ip.scaleForDisplay(r);
	    //System.out.println("refreshVisuals: image: "+r+", display:"+r2);
	    ((GraphicalEditPart) getParent().getParent()).setLayoutConstraint(
		    this,
		    getFigure(),
		    ip.scaleForDisplay(r));
	}

	public void setColourFromType(LOType type) {
		this.getFigure().setBorder(new LineBorder(getMyType().getTitleBackColour(),2));
		
	}
}
