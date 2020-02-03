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

import org.eclipse.ui.IEditorPart;

/**
 * Pliny aware editors which display their materials as Pages should
 * implement this interface.  It allows the editor to be opened to
 * a particular page when a Resource has a page number in it.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.pdfAnnot.PDFEditor
 * 
 * @author John Bradley
 *
 */

public interface IPageSettableEditorPart extends IEditorPart {
	
	/**
	 * causes the implementor to turn to the given page in its display.
	 * 
	 * @param pageNo int the requested page number.
	 */
    public void turnToPage(int pageNo);
}
