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

package uk.ac.kcl.cch.jb.pliny.containmentView.actions;

import java.util.Vector;

import uk.ac.kcl.cch.jb.pliny.actions.PlinyExportAction;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.utils.IPlinyExporterDataProvider;
import uk.ac.kcl.cch.jb.pliny.utils.PlinyExporterFullDataProvider;

public class CreateArchiveAction extends PlinyExportAction {

	private Vector items;
	private Resource startingResource;
	
	public CreateArchiveAction(Vector items, Resource startingResource){
		super();
		this.startingResource = startingResource;
		this.items = items;
	}
	
	protected IPlinyExporterDataProvider getTheDataProvider(){
		return new ContainmentViewDataExporterProvider(items, startingResource);
	}

}
