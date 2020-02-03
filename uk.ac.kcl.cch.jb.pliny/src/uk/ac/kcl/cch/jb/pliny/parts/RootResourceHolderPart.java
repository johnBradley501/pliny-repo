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

import java.beans.PropertyChangeEvent;

import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.ResourceHolder;
/**
 * the GEF root EditPart extends {@link RootResourcePart} to support a
 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource Resource}'s reference/annotation area,
 * when the editor is using a
 * {@link uk.ac.kcl.cch.jb.pliny.model.ResourceHolder ResourceHolder}.  This means that
 * this part has to track the contents of the ResourceHolder as well as the current
 * Resource. 
 * 
 * @author John Bradley
 *
 */

public class RootResourceHolderPart extends RootResourcePart {

	public RootResourceHolderPart(ResourceHolder model) {
		setModel(model);
		// TODO Auto-generated constructor stub
	}
	
	public ResourceHolder getResourceHolder(){
		return (ResourceHolder)getModel();
	}
	
	public Resource getResource(){
		return ((ResourceHolder)getModel()).getResource();
	}
	
	public void activate(){
		if(isActive())return;
		super.activate();
		getResourceHolder().addPropertyChangeListener(this);
	}
	
	public void deactivate(){
		if(!isActive())return;
		getResourceHolder().removePropertyChangeListener(this);
		super.deactivate();
	}
	
	public void propertyChange(PropertyChangeEvent arg0) {
		String name = arg0.getPropertyName();
		if(name == ResourceHolder.NEW_RESOURCE_EVENT){
			Resource oldResource = (Resource)arg0.getOldValue();
			if(oldResource !=  null)
			   oldResource.removePropertyChangeListener(this);
			getResource().addPropertyChangeListener(this);
		}
		refreshChildren();
	}

}
