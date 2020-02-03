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

public interface IHasAttributeProperties {

	/**
	 * announces to the Pliny exporter that the Resource has its
	 * attributes field handled as a set of <code>java.util.Properties</code>.
	 * This causes the exporter to take each attributes thus encoded
	 * and write it to the data XML file as a separate element.
	 */
	
}
