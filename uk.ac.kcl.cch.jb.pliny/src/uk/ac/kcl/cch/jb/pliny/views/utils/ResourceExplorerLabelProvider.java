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

package uk.ac.kcl.cch.jb.pliny.views.utils;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * the label provider for the the JFace Tree viewer displayed by
 * Resource Explorer and Resource-Explorer-like views.  Like all
 * JFace label providers, this class interprets Resource
 * Explorer model elements (instances of
 * {@link IResourceExplorerItem} for display in the SWT Tree.
 * 
 * @author John Bradley
 *
 */

public class ResourceExplorerLabelProvider implements ILabelProvider {

	public Image getImage(Object element) {
		if(element instanceof IResourceExplorerItem)
			return ((IResourceExplorerItem)element).getIcon();
		return null;
	}

	public String getText(Object element) {
		if(element instanceof IResourceExplorerItem)
			return ((IResourceExplorerItem)element).getText();
		return null;
	}

	public void addListener(ILabelProviderListener listener) {
		// do nothing here at present!
	}

	public void dispose() {
		// nothing to dispose at present  j.b.
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// no listeners to remove at present.
	}

}
