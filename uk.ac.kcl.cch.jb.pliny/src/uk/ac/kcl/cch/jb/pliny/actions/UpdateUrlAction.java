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

package uk.ac.kcl.cch.jb.pliny.actions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.commands.ChangeUrlCommand;
import uk.ac.kcl.cch.jb.pliny.model.Favourite;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.views.ResourceExplorerView;
import uk.ac.kcl.cch.rdb2java.dynData.BaseObject;

/**
 * One of the actions supported by the Resource Explorer
 * {@link uk.ac.kcl.cch.jb.pliny.views.ResourceExplorerView} 
 * to allow the user to change the URL associated with a web page
 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource}.  It first locates
 * the Resource from the current selection, invokes an JFace InputDialog
 * to ask the user to supply the new URL, and then invokes the command
 * {@link uk.ac.kcl.cch.jb.pliny.commands.ChangeUrlCommand} 
 * to change it.  By invoking a command the operation becomes undoable.
 * 
 * @author John Bradley
 *
 */

public class UpdateUrlAction extends Action
implements IInputValidator{

	private ResourceExplorerView view;
	private Resource theResource = null;
	
	public UpdateUrlAction(ResourceExplorerView view) {
		super("Update resource's URL");
		this.view = view;
		this.setImageDescriptor(
				ImageDescriptor.createFromImage(PlinyPlugin.getDefault().getImage("icons/browserIcon.gif")));
	}
	
	public void setResource(Resource theResource){
		this.theResource = theResource;
	}

	public void run(){
		if(theResource == null){
			Vector selectedObjects = view.getSelectedBaseObjects();
			if(selectedObjects.size() != 1)return;
			BaseObject selectedObject = (BaseObject)selectedObjects.get(0);
			if(selectedObject instanceof Favourite)
				selectedObject = ((Favourite)selectedObject).getResource();
			if(!(selectedObject instanceof Resource))return;
			theResource = (Resource)selectedObject;
		}
		String theIdentifier = theResource.getIdentifier();
		if(!theIdentifier.startsWith("url:"))return;
		String theURL = theIdentifier.substring(4);
		
		InputDialog dlg = new InputDialog(
				Display.getCurrent().getActiveShell(),
				"Specify the new URL", 
				"Provide a new URL for '"+theResource.getName()+"'",
				theURL,
				this);
		if(dlg.open() != Window.OK)return;
		String newURL = dlg.getValue();
		if(theURL.equals(newURL))return;
		
		view.getCommandStack().execute(new ChangeUrlCommand(theResource, newURL));
		
		theResource = null;
	}

	public String isValid(String newText) {
		try {
			URL testURL = new URL(newText);
		} catch (MalformedURLException e) {
			return "This is not a valid URL.";
		}
		return null;
	}

}
