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

package uk.ac.kcl.cch.jb.pliny.parts;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.swt.widgets.Text;

/**
 * identifies a GEF EditPart that contains direct-editable text.
 * 
 * @see PlinyDirectEditManager
 * 
 * @author John Bradley
 *
 */

public interface IDirectEditablePart extends GraphicalEditPart {
	
	/**
	 * requests the edit part to provide the text that is to be
	 * direct edited.
	 * 
	 * @return text to be edited.
	 */
	public String getTextToEdit();
	
	/**
	 * allows the edit part to specify display attributes to be used
	 * while displaying the text for editing.  This usually involves
	 * specifying the colours to be used, and indicating whether the
	 * entire text should be initially selected or not.
	 * 
	 * @param text org.eclipse.swt.widgets.Text to be used to manage
	 * the text editing.
	 */
	public void setupText(Text text);
}
