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

import org.eclipse.ui.IWorkbenchPart;

import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * This interface should be implemented by Eclipse Workbench Parts
 * that work with input as a Pliny Resource.
 * 
 * @author John Bradley
 *
 */

public interface IResourceDrivenPart extends IWorkbenchPart {
	
	/**
	 * returns the Resource that this part is currently using as its input.
	 * 
	 * @return Resource
	 */
   public Resource getMyResource();
}
