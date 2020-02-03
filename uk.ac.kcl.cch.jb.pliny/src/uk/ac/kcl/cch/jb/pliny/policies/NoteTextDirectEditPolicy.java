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

package uk.ac.kcl.cch.jb.pliny.policies;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;

import uk.ac.kcl.cch.jb.pliny.commands.NoteTextUpdateCommand;
import uk.ac.kcl.cch.jb.pliny.figures.NoteTextFigure;
import uk.ac.kcl.cch.jb.pliny.parts.NoteTextPart;

/**
 * GEF edit policy of type <code>EditPolicy.DIRECT_EDIT_ROLE</code> to 
 * support the direct editing of the textual content of a reference object
 * created by a <code>NoteLucened</code>.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.parts.NoteTextPart
 * 
 * @author John Bradley
 *
 */

public class NoteTextDirectEditPolicy extends DirectEditPolicy {
	protected Command getDirectEditCommand(DirectEditRequest request) {
		String text = (String)request.getCellEditor().getValue();
		NoteTextPart notePart = (NoteTextPart)getHost();
		return new NoteTextUpdateCommand(notePart.getNote(), text);
	}

	protected void showCurrentEditValue(DirectEditRequest request) {
		String value = (String)request.getCellEditor().getValue();
		((NoteTextFigure)getHostFigure()).setText(value);
		//hack to prevent async layout from placing the cell editor twice.
		getHostFigure().getUpdateManager().performUpdate();
	}
}
