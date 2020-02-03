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

package uk.ac.kcl.cch.jb.pliny.views.utils;

import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * the Resource Explorer's model element for the 4th level "Contains"
 * item.
 * 
 * @author John Bradley
 */

public class ResourceExplorerContainsList extends
		ResourceExplorerSurrogateListItemBase {

	public ResourceExplorerContainsList(IResourceTreeDisplayer myView,
			IResourceExplorerItem parent, Resource theObject) {
		super(myView, parent, theObject, 
				"Contains", Resource.MYDISPLAYEDITEMS_PROP,
				theObject.getMyDisplayedItems().getItems());
	}

	public IResourceExplorerItem makeChild(Object item) {
		LinkableObject object = (LinkableObject)item;
		if(object.getSurrogateFor() == null)return null;
		return new ResourceExplorerSurrogateItem(getMyView(), parent, object);
	}

}
