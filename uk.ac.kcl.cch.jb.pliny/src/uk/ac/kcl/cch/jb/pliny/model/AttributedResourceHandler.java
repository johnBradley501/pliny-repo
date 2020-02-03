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
 * extends AttributedObjectHandler to handle Resource types items
 * 
 * @author John Bradley
 *
 */

public class AttributedResourceHandler extends AttributedObjectHandler{
   
   /**
    * creates an instance of this item that will manage data for the given
    * {@link Resource} record.
    * @param myResource
    */
   
   public AttributedResourceHandler(Resource myResource){
	   super(myResource,"resource");
   }
}
