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

import uk.ac.kcl.cch.rdb2java.dynData.IAuthorityListItem;

/**
 * announces that the model data has a {@link LOType} value, and defines
 * the method names used to access them. This is needed because
 * two rather different model records both have LOTypes, and some
 * Pliny code needs to deal with a mix of both kinds.
 * 
 * @author John Bradley
 *
 */

public interface IHasLoType extends IAuthorityListItem{
	public LOType getLoType();
	public void setLoType(LOType item);
}
