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

import uk.ac.kcl.cch.rdb2java.dynData.FKReferenceList;

/***
 * VirtualResource is used in places where a resource
 * item needs to be shown for a reference/annotation area -- 
 * but there is as of yet no data in the reference/annotation area
 * and hence a corresponding Resource to hold it is not yet
 * needed in the backing store DB.  The VirtualResource object is created
 * in memory, but not initally written to the database.  It is the job,
 * then of object that create "contents" for this resource to first
 * ensure that it is "real" (is in the DB, and has therefore a key)
 * before assigning new objects for it to contain.  Do this by calling
 * <code>makeMeReal</code> before creating material to be linked to it.
 * <p>
 * A part of the work of this is to store the appropriate ObjectType for this
 * resource in a holding area, since it cannot be conventionally handled
 * until the Resource itself is in the backing store DB too.
 * 
 * @author Bradley
 */


public class VirtualResource extends Resource {

	public VirtualResource() {
		super(true);
	}
	
	public VirtualResource(boolean empty){
		super(empty);
	}
	
	public String getIdentifier(){
		String id = super.getIdentifier();
		if(id != null && id.trim().length() > 0)return id;
		return "resource:"+getALID();
	}
	
	private ObjectType theType = null;
	
	public void setObjectType(ObjectType theType){
		if(resourceKey != 0)super.setObjectType(theType);
		else this.theType = theType;
	}
	
	public ObjectType getObjectType(){
		if(resourceKey != 0)return super.getObjectType();
		return theType;
	}
	
	/**
	 * puts the resource data into the DB -- thus making it 'real' and
	 * persistent.  Must be called before any data is linked to this object.
	 *
	 */
	public void makeMeReal(){
		if(resourceKey != 0)return;  // is already real.   jb.
		this.reIntroduceMe();
		super.setObjectType(theType);
	}
}
