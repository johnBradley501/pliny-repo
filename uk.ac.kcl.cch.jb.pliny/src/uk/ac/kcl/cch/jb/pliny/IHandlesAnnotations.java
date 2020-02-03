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

package uk.ac.kcl.cch.jb.pliny;

/**
 * This otherwise empty interface is used by the AnnotationView to decide
 * whether it should display a reference area.  Objects that provide their
 * own reference or annotation areas should implement this interface and
 * thereby tell the AnnotationView that it shouldn't try to display a reference
 * area as well.
 * 
 * @author John Bradley
 *
 */
public interface IHandlesAnnotations {

}
