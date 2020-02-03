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

package uk.ac.kcl.cch.jb.pliny.dnd;

import java.io.InputStream;

/**
 * This interface, implemented by Pliny archive readers, provides an
 * InputStream from the archive for independant plugins handling 
 * the cacheing of data from an archive.
 * 
 * @see IResourceExtensionProcessor
 * 
 * @author John Bradley
 */

public interface IGetsArchiveEntries {
	
	/**
	 * asks the archive reader to deliver an InputStream from the archive
	 * for the given component name.  If null is returned, the archive had
	 * no data component of the given name.
	 * 
	 * @param name String requested archive data component.
	 * @return InputStream for the requested component, or null if there was none.
	 */
	
	public InputStream getArchiveEntry(String name);
	
	/**
	 * asks the archive reader to get from the archive the key the current
	 * resource had when it was being exported.  This is needed because
	 * some cache objects use the key as a part of the cache'd filename.
	 * 
	 * @return int the key of the resource currently being processed from the archive.
	 */
	public int getOldKey();

}
