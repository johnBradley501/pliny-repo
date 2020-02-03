/*******************************************************************************
 * Copyright (c) 2007, 2014 John Bradley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Bradley - initial API and implementation
 *******************************************************************************/

package uk.ac.kcl.cch.jb.pliny.actions;

import java.util.Collection;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;

import uk.ac.kcl.cch.jb.pliny.IResourceDrivenPart;
import uk.ac.kcl.cch.jb.pliny.commands.PastePlinyCommand;
import uk.ac.kcl.cch.jb.pliny.dnd.ClipboardHandler;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * the standard paste action for GEF-based Pliny items.
 * Makes use of <code>ClipboardHandler</code> as the interface to the
 * Clipboard.  When pasting in a GEF context it runs a command 
 * ({@link uk.ac.kcl.cch.jb.pliny.commands.PastePlinyCommand}) through the 
 * provided CommandStack so that it is undoable.
 * <p>As a result of the provision of a standardised PlinyToolBar, and the presence of a
 * text field in it, in 2014 code was added here to deal with situations when copying
 * was into that text field.
 * 
 * @author John Bradley
 *
 */

public class PastePlinyAction extends SelectionAction {
	private boolean allowAnchors;
	
	protected StyledText styledText = null;

	public PastePlinyAction(IWorkbenchPart part, boolean allowAnchors) {
		super(part);
		setId(ActionFactory.PASTE.getId());
		setText("Paste");
		this.allowAnchors = allowAnchors;
	}
	
	public PastePlinyAction(IWorkbenchPart part){
		super(part);
		setId(ActionFactory.PASTE.getId());
		setText("Paste");
		this.allowAnchors = false;
	}
	
	public void setStyledText(StyledText styledText){
		this.styledText = styledText;
	}
	
	public void clearStyledText(){
		this.styledText = null;
	}
	
	private Command createPasteCommand(){
		IWorkbenchPart part = this.getWorkbenchPart();
		if(part ==  null)return null;
		if(!(part instanceof IResourceDrivenPart))return null;
		Resource myMainResource = ((IResourceDrivenPart)part).getMyResource();
		
		Collection rslt = ClipboardHandler.getDefault().getContents();
		if((rslt == null) || (rslt.size() == 0))return null;
		return new PastePlinyCommand(myMainResource, rslt, allowAnchors);
	}

	protected boolean calculateEnabled() {
		return true;
	}
	
	public void run(){
		if(styledText != null){
			styledText.paste();
			return;
		}
		execute(createPasteCommand());
	}

}
