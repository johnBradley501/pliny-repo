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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * the content provider for the the JFace Tree viewer displayed by
 * the PDFOutline view.  Like all
 * JFace content providers, this class mediates between the PDFOutline
 * View's model elements (instances of
 * {@link PDFOutlineItem} and the SWT Tree.
 * 
 * @author John Bradley
 *
 */
public class PDFOutlineContentProvider 
implements IStructuredContentProvider,ITreeContentProvider {
	
	public PDFOutlineContentProvider() {
		super();
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// it won't change!
	}

	public Object[] getChildren(Object parentElement) {
		if(!(parentElement instanceof PDFOutlineItem )) return new Object[0];
		return ((PDFOutlineItem)parentElement).getChildren().toArray();
	}

	public Object getParent(Object element) {
		if(!(element instanceof PDFOutlineItem )) return null;
		return ((PDFOutlineItem)element).getParent();
	}

	public boolean hasChildren(Object element) {
		if(!(element instanceof PDFOutlineItem )) return false;
		return ((PDFOutlineItem)element).hasChildren();
	}

}
