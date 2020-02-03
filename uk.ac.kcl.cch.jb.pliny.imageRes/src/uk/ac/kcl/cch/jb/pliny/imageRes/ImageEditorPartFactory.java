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

package uk.ac.kcl.cch.jb.pliny.imageRes;

import org.eclipse.gef.EditPart;
import org.eclipse.ui.IWorkbenchPart;

import uk.ac.kcl.cch.jb.pliny.factories.PlinyGEFEditFactory;
import uk.ac.kcl.cch.jb.pliny.imageRes.model.ImageResource;
import uk.ac.kcl.cch.jb.pliny.imageRes.parts.ImageResourcePart;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.parts.ScalableAnchorPart;
import uk.ac.kcl.cch.jb.pliny.parts.ScalableLinkableObjectPart;

/**
 * the GEF EditPartFactory for mapping Pliny model objects to
 * GEF Controller objects for the Image editor's annotation area.
 * <p>
 * This handles the 
 * {@link uk.ac.kcl.cch.jb.pliny.imageRes.model.ImageResource ImageResource} (root model object) so that the factory
 * creates an ImageResourcePart, and the
 * {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject LinkableObject} when
 * displayed directly within the root <code>ImageResourcePart</code> so that
 * scalable anchor- and LinkableOject- parts are created.  Other
 * kinds of data are handled by the inherited
 * {@link uk.ac.kcl.cch.jb.pliny.factories.PlinyGEFEditFactory PlinyGEFEditFactory}.
 * 
 * @author John Bradley
 *
 */

public class ImageEditorPartFactory extends PlinyGEFEditFactory {

	public ImageEditorPartFactory(IWorkbenchPart myPart) {
		super(myPart);
	}

	public EditPart createEditPart(EditPart context, Object model) {
		if(model instanceof ImageResource)
			return new ImageResourcePart((ImageResource)model);
		if((model instanceof LinkableObject) && (context instanceof ImageResourcePart)){
			LinkableObject lo = (LinkableObject)model;
			if(lo.getSurrogateFor() == null)
				return new ScalableAnchorPart(lo);
			else 
				return new ScalableLinkableObjectPart(lo);
		}
		return super.createEditPart(context, model);
	}

}
