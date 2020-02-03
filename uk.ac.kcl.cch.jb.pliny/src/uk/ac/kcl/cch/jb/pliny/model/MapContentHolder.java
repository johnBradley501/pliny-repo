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

/**
 * a convenience class designed to make GEF MVC modelling for
 * Pliny's resource/annotation area easier -- to represent to GEF
 * the intermediate state between a {@link LinkableObject} and the display
 * of its reference area.
 * 
 * @see TextContentHolder
 * 
 * @author John Bradley
 *
 */

public class MapContentHolder implements IHoldsLinkableObject {

	LinkableObject myObject;
	
	//public MapContentHolder(Resource myObject) {
	//	this.myObject = myObject;
	//}
	
	public MapContentHolder(LinkableObject myObject){
		this.myObject = myObject;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.kcl.cch.jb.pliny.model.IHoldsLinkableObject#getSurrogate()
	 */
	public Resource getSurrogate(){
		return myObject.getSurrogateFor();
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.kcl.cch.jb.pliny.model.IHoldsLinkableObject#getLinkableObject()
	 */
	public LinkableObject getLinkableObject(){
		return myObject;
	}

}
