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

import org.eclipse.draw2d.geometry.Rectangle;

import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
/**
 * extends {@link LinkableObjectPart} to handle the display
 * of scalable reference objects derived from
 * {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject LinkableObject} and
 * displayed in scalable parent objects.
 * <p>
 * Note that ScalableAnchors appear in scalable contexts, where the actual
 * size on the screen depends not only on the size parameter in the
 * LinkableObject's data, but also on the current scale ratio which is
 * available from this part's parent editPart.
 * 
 * @author John Bradley
 *
 */

public class ScalableLinkableObjectPart extends LinkableObjectPart {

	public ScalableLinkableObjectPart(LinkableObject model) {
		super(model);
	}

	protected Rectangle mapLocation(Rectangle r){
		Rectangle rslt = new Rectangle(r);
		rslt.setLocation(((IScalableImagePart)getParent()).alignForDisplay(r.getLocation()));
		return rslt;
		
	}
}
