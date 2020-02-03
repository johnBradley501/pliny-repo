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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import uk.ac.kcl.cch.jb.pliny.model.Resource;
/**
 * Resets a URL attached to a particular Web-Page Resource to
 * the given new URL.  It has the side effect of closing the open
 * web viewer (if there currently is one) for the updated resource.
 * <p>
 * This command is invoked by the Resource Explorer's
 * {@link uk.ac.kcl.cch.jb.pliny.actions.UpdateUrlAction}
 * 
 * @author John Bradley
 *
 */

public class ChangeUrlCommand extends Command {

	private Resource theResource;
	private String newURL, oldId;

	/**
	 * constructs the command to change the URL associated with
	 * the identifier for the given resource.
	 * 
	 * @param theResource Resource the resource to a web page
	 * @param newURL the new URL to attach to it.
	 */
	public ChangeUrlCommand(Resource theResource, String newURL) {
		super("change resource's URL");
		this.theResource = theResource;
		this.newURL = newURL;
	}

	public void execute(){
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(); 
		theResource.closeMyEditor(page, false);
		oldId = theResource.getIdentifier();
		theResource.setIdentifiers("url:"+newURL);
	}
	
	public void undo(){
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(); 
		theResource.closeMyEditor(page, false);
		theResource.setIdentifiers(oldId);
	}
}
