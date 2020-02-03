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

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.model.LOType;

/**
 * the JFace Label Provider for the Type Manager.  Note that the data
 * model behind this is Pliny's {@link uk.ac.kcl.cch.jb.pliny.model.LOType LOType}.
 * 
 * @author John Bradley
 *
 */

public class TypeManagerLabelProvider implements ITableLabelProvider {
	
	static private Image typeIcon = null;

	public TypeManagerLabelProvider() {
		if(typeIcon == null)
			typeIcon = PlinyPlugin.getImageDescriptor("icons/typeIcon.gif").createImage();
	}

	public Image getColumnImage(Object element, int columnIndex) {
		if(!(element instanceof LOType))return null;
		LOType myType = (LOType)element;
		if(columnIndex == 0)return getTypeImageIfCurrent(myType);
		if(columnIndex == 1)return myType.getColourIcon();
		return null;
	}

	private Image getTypeImageIfCurrent(LOType myType) {
		if(myType == LOType.getCurrentType())
			return typeIcon;
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if(columnIndex != 1)return null;
		if(!(element instanceof LOType))return null;
		LOType myType = (LOType)element;
		String name = myType.getName();
		if(name == null || name.trim().length() == 0 )name = "(default)";
		else name = name.trim();
		return name;
	}

	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	public void dispose() {
		if(typeIcon != null)typeIcon.dispose();
        typeIcon = null;
	}

	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

}
