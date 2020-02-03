/*******************************************************************************
 * Copyright (c) 2011 John Bradley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Bradley - initial API and implementation
 *******************************************************************************/

/**
 * 
 */
package uk.ac.kcl.cch.jb.pliny.policies;

import java.util.Map;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.gef.tools.CreationTool;

import uk.ac.kcl.cch.jb.pliny.commands.CreateNoteCommand;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;

/**
 * Overrides GEF's standard CreationTool to extend it's performCreation() method.  This
 * extension means that the Note's title is immediately opened for direct edit upon
 * creation of the new note.
 * 
 * The code is derived from a hint provided kindly by Tony Modica in answer to my request
 * on the eclipse.tools.get newsgroup for a way to immediately set up direct edit after creation
 * (see commented out executeCurrentCommand() method), but it didn't do the job for me, since it 
 * apparently tried to invoke the direct edit request before the newly created LinkableObject
 * was quite ready.  Doing it after super.performCreation() was done appears to have fixed this.
 * @see org.eclipse.gef.tools.AbstractTool#performCreation().  You can see an example that works
 * this way in section 12 of http://www.vainolo.com/2011/07/07/creating-a-gef-editor-%E2%80%93-part-7-moving-elements-and-direct-editing/
 * but it uses a Runnable invoked through Display.getCurrent().asyncExec(), perhaps to get it scheduled to happen at the right time...
 * 
 * @author John Bradley
 *
 */

public class NoteCreationTool extends CreationTool {
	
	public NoteCreationTool(CreationFactory factory) {
		super(factory);
	}
	/*

	protected void executeCurrentCommand() {
	     // Store current command
	     final Command command = getCurrentCommand();

	     super.executeCurrentCommand();

	     if (command instanceof CreateNoteCommand) {
	       final LinkableObject node = ((CreateNoteCommand) command).getTheSurrogate();
	       final Map reg = getCurrentViewer().getEditPartRegistry();
	       final EditPart editPart = (EditPart) reg.get(node);
	       final SelectionRequest request = new SelectionRequest();
	       request.setType(RequestConstants.REQ_DIRECT_EDIT);
	       request.setLocation(new Point(-1,-1));
	       editPart.performRequest(request);
	       //command.setLabel(command.getLabel() + node.getName());
	     }
		}
		*/
	
	protected void performCreation(int button) {
		final Command command = getCurrentCommand();
		super.performCreation(button);
		if (command instanceof CreateNoteCommand) {
			final LinkableObject node = ((CreateNoteCommand) command).getTheSurrogate();
			final Map reg = getCurrentViewer().getEditPartRegistry();
			final EditPart editPart = (EditPart) reg.get(node);
			final SelectionRequest request = new SelectionRequest();
			request.setType(RequestConstants.REQ_DIRECT_EDIT);
			request.setLocation(new Point(-1,-1));
			editPart.performRequest(request);
			//command.setLabel(command.getLabel() + node.getName());
		}
	}

	
	//private void selectAddedObject(EditPartViewer viewer) {
	//	super.selectAddedObject(viewer);
	//}
}
