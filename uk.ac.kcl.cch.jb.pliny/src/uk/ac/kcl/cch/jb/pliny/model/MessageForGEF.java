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

public class MessageForGEF {
	
	/**
	 * a model element for GEF to cause GEF to display a message box
	 * in its area rather than the usual reference/annotation area
	 * materials.  Note that the use of this element is expected to
	 * mean that an error has occurred that prevents the usual display, and
	 * message text should report on this error to the user.
	 * 
	 * @author John Bradley
	 */
	
	private String text;

	public MessageForGEF(String text) {
		this.text = text;
	}
	
	public String getText(){
		return text;
	}

}
