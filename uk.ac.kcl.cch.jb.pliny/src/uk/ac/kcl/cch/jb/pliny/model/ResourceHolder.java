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

package uk.ac.kcl.cch.jb.pliny.model;

import uk.ac.kcl.cch.rdb2java.dynData.PropertyChangeObject;

/**
 * acts as a surrogate for a {link Resource} which is acting as the root part
 * for a GEF reference/annotation area display.  This surrogate model is
 * useful when the display needs to work in situations where the Resource
 * it links to needs to change, since GEF does not allow the root
 * model element to change once established.
 * <p>
 * Instances of this class are
 * {@link uk.ac.kcl.cch.rdb2java.dynData.PropertyChangeObject PropertyChangeObject}s
 * which means that they can announce when their surrogate Resource has been
 * changed by sending an event with name <code>NEW_RESOURCE_EVENT</code>.  
 * This can be used by the root GEF edit part to trigger the update
 * of the display -- redrawing it for the new Resource that has replaced the old.
 * 
 * @author John Bradley
 *
 */

public class ResourceHolder extends PropertyChangeObject 
implements IHasResource{
	
	private Resource resource;
	
	public static final String NEW_RESOURCE_EVENT = "NewResource";

	public ResourceHolder(Resource resource) {
		super();
		this.resource = resource;
	}
	
	public Resource getResource(){
		return resource;
	}
	
	public void setResource(Resource resource){
		if(this.resource == resource)return;
		Resource oldResource = this.resource;
		this.resource = resource;
		this.firePropertyChange(NEW_RESOURCE_EVENT, oldResource, resource);
	}

}
