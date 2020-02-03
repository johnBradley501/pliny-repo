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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * used by Eclipse during startup to re-open Resource objects that were open
 * in their relevant Pliny editors when it was last shutdown -- 
 * this factory takes the data stored
 * in an IMemento and first fetches the referenced 
 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource Resource} object,a
 * and then packages it in a {@link ResourceEditorInput} object.
 * 
 * @author John Bradley
 *
 */

public class ResourceEditorInputFactory implements IElementFactory {

	public IAdaptable createElement(IMemento memento) {
		Integer theKey = memento.getInteger(ResourceEditorInput.RESOURCE_KEY_TAG);
		if(theKey == null)return null;
		Integer pageNo = memento.getInteger(ResourceEditorInput.PAGE_NUMB_TAG);
		Resource theResource = Resource.getItem(theKey.intValue());
		if(theResource == null)return null;
		ResourceEditorInput rslt = new ResourceEditorInput(theResource);
		if(pageNo != null)rslt.setPageNo(pageNo.intValue());
		return rslt;
	}

}
