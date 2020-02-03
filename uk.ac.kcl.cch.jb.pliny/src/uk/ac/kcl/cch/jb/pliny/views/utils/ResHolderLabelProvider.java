/*******************************************************************************
 * Copyright (c) 2009 John Bradley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Bradley - initial API and implementation
 *******************************************************************************/
package uk.ac.kcl.cch.jb.pliny.views.utils;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * the class that provides display information for the Table viewer in
 * the Resource Holder view.
 * 
 * @author John Bradley
 *
 */

public class ResHolderLabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		if(!(element instanceof Resource))return null;
		Resource r = (Resource) element;
		return r.getObjectType().getIconImage();
	}

	public String getColumnText(Object element, int columnIndex) {
		if(!(element instanceof Resource))return "";
		Resource r = (Resource) element;
		return r.getFullName();
	}

	public void addListener(ILabelProviderListener listener) {
		// not needed
	}

	public void dispose() {
		// not needed
	}

	public boolean isLabelProperty(Object element, String property) {
		// not needed here
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// not needed here
	}

}
