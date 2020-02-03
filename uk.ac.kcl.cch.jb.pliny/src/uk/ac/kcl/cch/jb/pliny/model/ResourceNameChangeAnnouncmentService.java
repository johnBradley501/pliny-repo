/*******************************************************************************
 * Copyright (c) 2009 John Bradley
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
 * a singlet class that provides a service that announces when <b>any</b> resource changes its
 * name.  Listeners subscribe to this service, and are then notified whenever such a name
 * change happens for any resource.
 * <p>This object gets its information from Resource.setName, so no setup is needed for
 * listeners to be told about resource name changes.
 * 
 * @author John Bradley
 *
 */

public class ResourceNameChangeAnnouncmentService extends PropertyChangeObject {
	
	private static ResourceNameChangeAnnouncmentService service = null;
	
	public static ResourceNameChangeAnnouncmentService getService(){
		if (service == null)service = new ResourceNameChangeAnnouncmentService();
		return service;
	}
	
	private ResourceNameChangeAnnouncmentService(){
		
	}
	
	public void announceNameChange(Resource r, String oldName){
		String fullName = r.getFullName();
		if(fullName.equals(oldName))return;
		firePropertyChange(Resource.NAME_PROP, oldName, r);
	}

}
