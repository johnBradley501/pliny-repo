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
 * of its surrogate's content.
 * 
 * @see MapContentHolder
 * 
 * @author John Bradley
 *
 */

public class TextContentHolder {

	NoteLucened myObject;
	
	public TextContentHolder(NoteLucened myObject) {
		this.myObject = myObject;
	}
	
	public NoteLucened getObject(){
		return myObject;
	}

}
