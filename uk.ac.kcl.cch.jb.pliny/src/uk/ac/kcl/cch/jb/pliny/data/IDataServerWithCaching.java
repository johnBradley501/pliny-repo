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
package uk.ac.kcl.cch.jb.pliny.data;

import org.eclipse.core.runtime.Plugin;

import uk.ac.kcl.cch.rdb2java.dynData.IDataServer;

public interface IDataServerWithCaching extends IDataServer {
   public IFileCacheManager createCacheManager(Plugin owner, String cacheName, String myType);
}
