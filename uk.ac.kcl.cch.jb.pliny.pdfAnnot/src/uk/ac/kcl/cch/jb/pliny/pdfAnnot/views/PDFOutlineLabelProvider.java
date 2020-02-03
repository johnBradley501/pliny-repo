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

package uk.ac.kcl.cch.jb.pliny.pdfAnnot.views;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * the label provider for the the JFace Tree viewer displayed by
 * the PDFOutline view.  Like all
 * JFace content providers, this class interprets the PDFOutline
 * View's model elements (instances of
 * {@link PDFOutlineItem} for display in the SWT Tree.
 * 
 * @author John Bradley
 *
 */
public class PDFOutlineLabelProvider implements ILabelProvider {

	public PDFOutlineLabelProvider() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Image getImage(Object element) {
		return null;
	}

	public String getText(Object element) {
		if(element instanceof PDFOutlineItem)
			return ((PDFOutlineItem)element).getTitle();
		return null;
	}

	public void addListener(ILabelProviderListener listener) {
//		 do nothing here at present!
	}

	public void dispose() {
//		 do nothing here at present!
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
//		 do nothing here at present!
	}
}
