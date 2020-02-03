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

import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory;

import uk.ac.kcl.cch.jb.pliny.dnd.ClipboardHandler;
import uk.ac.kcl.cch.jb.pliny.views.ResourceExplorerView;

/**
 * the copy action for the Resource Explorer
 * ({@link uk.ac.kcl.cch.jb.pliny.views.ResourceExplorerView})
 * 
 * @author John Bradley
 */

public class ResourceExplorerCopyAction extends Action {

	private ResourceExplorerView view;
	
	public ResourceExplorerCopyAction(ResourceExplorerView view) {
		super("copy");
		setId(ActionFactory.COPY.getId());
		this.view = view;
	}

	public void run(){
		Vector items = view.getSelectedBaseObjects();
		if(items.size() == 0) return;
		ClipboardHandler.getDefault().setContents(items);
	}
}
