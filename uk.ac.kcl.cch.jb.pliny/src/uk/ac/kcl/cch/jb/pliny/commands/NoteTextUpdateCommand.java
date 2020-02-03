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

package uk.ac.kcl.cch.jb.pliny.commands;

import org.eclipse.gef.commands.Command;

import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;

/**
 * replaces the content of a Note (
 * {@link uk.ac.kcl.cch.jb.pliny.model.NoteLucened}) with new text.
 * @author John Bradley
 *
 */
public class NoteTextUpdateCommand extends Command {
	private String oldText, newText;
	private NoteLucened note;

	/**
	 * creates an instance of this command and shows that it applies
	 * to the given note.  The new content text is provided.
	 * 
	 * @param note note which needs to have its context text updated.
	 * @param text String of text which is the new content.
	 */
	
	public NoteTextUpdateCommand(NoteLucened note, String text) {
		this.note = note;
		if(text != null)
			newText = text;
	}

	public void execute() {
		if(newText != null){
		   oldText = note.getContent();
		   note.setContent(newText);
		}
	}

	public void undo() {
		if(newText != null)note.setContent(oldText);
	}

}
