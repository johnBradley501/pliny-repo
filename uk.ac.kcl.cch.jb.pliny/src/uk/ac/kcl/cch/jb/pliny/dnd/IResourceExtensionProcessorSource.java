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

/**
 * objects which wish to be used as identifiers for which
 * IResourceExtensionProcessor to execute when needed should
 * implement this interface.  The link is primarily used by the
 * Resource Explorer.
 * <p>
 * This interface is implemented in 
 * {@link uk.ac.kcl.cch.jb.pliny.model.ObjectType ObjectType},
 * and allows ObjectTypes to be used as identifiers within Resources
 * of which IResourceExtensionProcessor is linked to the particular Resource.
 * 
 * @author John Bradley
 */

public interface IResourceExtensionProcessorSource {
	   
	   /**
	    * Pliny will call this method when it needs access to the appropriate
	    * IResourceExtensionProcessor
	    * 
	    * @return IResourceExtensionProcessor for this item.
	    */
	   
	   public IResourceExtensionProcessor getDropTargetProcessor();
	   
	   
	   /**
	    * The Resource Explorer will use this method to store the
	    * assocation between this object and the IResourceExtensionProcessor.
	    * This happens when it is reading the IExtensionRegistry to find
	    * suitable plugins.
	    * 
	    * @param module IResourceExtensionProcessor for this item.
	    */
	   

	   public void setDropTargetProcessor(IResourceExtensionProcessor module);
}
