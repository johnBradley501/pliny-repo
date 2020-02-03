/*******************************************************************************
 * Copyright (c) 2012 John Bradley
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
 * Pliny Resources that wish to use the file caching mechanism provided by Pliny 
 * (see {@link uk.ac.kcl.cch.jb.pliny.data.IFileCacheManager IFileCacheManager}) 
 * need to implement this interface.
 * 
 * @author John Bradley
 *
 */

public interface ICachingResource extends IAuthorityListItem {
	public int getCacheNumber();
	public void setCacheNumber(int numb);
	
	public String getExtension();
}
