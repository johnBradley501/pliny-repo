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
 * asserts that a model element has a name attribute, and provides
 * methods that can access and change that name.
 * 
 * @author John Bradley
 *
 */

public interface INamedObject {
   public void setName(String name);
   public String getName();
}
