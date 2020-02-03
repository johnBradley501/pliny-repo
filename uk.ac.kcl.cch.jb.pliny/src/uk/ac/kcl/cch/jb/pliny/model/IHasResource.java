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
 * asserted by model items that are not themselves Resources, but sometimes
 * need to stand in for them.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.dnd.PlinyObjectTransferDropTargetListener
 * @see uk.ac.kcl.cch.jb.pliny.policies.ResourceObjectsXYLayoutPolicy
 * @see uk.ac.kcl.cch.jb.pliny.policies.ScalableImageXYLayoutEditPolicy
 * 
 * @author John Bradley
 *
 */

public interface IHasResource {
	public Resource getResource();
}