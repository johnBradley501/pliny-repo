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

import uk.ac.kcl.cch.rdb2java.dynData.IPropertyChangeObject;

/**
 * Editor or view parts should implement this interface when they
 * operate in such a way that the Resource they work with can change
 * during operation.
 * <p>
 * The editor supporting this must become a PropertyChangeSource, and
 * whenever the resource change fire an event with name provided by
 * <code>CHANGE_EVENT</code>, and containing both the previouus and new Resource.
 * 
 * @author John Bradley
 *
 */

public interface IResourceChangeablePart extends IPropertyChangeObject {

	public static final String CHANGE_EVENT="ResourceChanged";
}
