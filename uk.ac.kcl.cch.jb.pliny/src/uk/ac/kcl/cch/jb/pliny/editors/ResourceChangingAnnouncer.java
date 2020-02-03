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

package uk.ac.kcl.cch.jb.pliny.editors;

import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.rdb2java.dynData.PropertyChangeObject;

/**
 * A convenience class to implement code that provides a service of
 * announcing a property change whenever a 
 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource Resource} changes.
 * Editors that wish to be a 
 * {@link IResourceChangeablePart} could make use of this
 * class.
 * 
 * @author John Bradley
 *
 */

public class ResourceChangingAnnouncer extends PropertyChangeObject {
	
	/**
	 * announces to listeners that a {@link uk.ac.kcl.cch.jb.pliny.model.Resource Resource} 
	 * change has just happened.
	 * 
	 * @param oldResource Resource in place before the change
	 * @param newResource Resource in place after the change.
	 */
    public void announceResource(Resource oldResource, Resource newResource){
    	firePropertyChange(IResourceChangeablePart.CHANGE_EVENT,oldResource,newResource);
    }
}
