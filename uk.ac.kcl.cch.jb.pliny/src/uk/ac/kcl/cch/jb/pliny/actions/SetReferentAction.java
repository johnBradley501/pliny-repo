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

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.commands.SetReferentCommand;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.views.ResourceExplorerView;

/**
 * One of the actions supported by the Resource Explorer
 * {@link uk.ac.kcl.cch.jb.pliny.views.ResourceExplorerView} --
 * it invokes the command
 * {@link uk.ac.kcl.cch.jb.pliny.commands.SetReferentCommand} 
 * to set the first selected item as the referent.
 * <p>
 * The referent is help by the Plugin
 * {@link uk.ac.kcl.cch.jb.pliny.PlinyPlugin}.
 * 
 * @author John Bradley
 *
 */


public class SetReferentAction extends Action {
	private ResourceExplorerView view;

	public SetReferentAction(ResourceExplorerView view) {
		super();
		this.view = view;
		this.setText("Set the Referent");
		this.setImageDescriptor(
				ImageDescriptor.createFromImage(PlinyPlugin.getDefault().getImage("icons/referent.gif")));
	}
	
	private Resource getSelectedResource(){
		Vector selectedItems = view.getSelectedBaseObjects();
		Resource rslt = null;
		Iterator it = selectedItems.iterator();
		while(it.hasNext()){
			Object obj = it.next();
			if(obj instanceof Resource){
				if(rslt == null)rslt = (Resource)obj;
				else return null;
			}
		}
		return rslt;
	}
	
	public void run(){
		Resource resource = getSelectedResource();
		if(resource == null)return;
		view.getCommandStack().execute(new SetReferentCommand(resource));
	}

}
