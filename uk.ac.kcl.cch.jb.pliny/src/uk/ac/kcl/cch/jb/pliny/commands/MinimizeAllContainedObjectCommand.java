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

import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * extends {@link MinMaxBaseCommand} to provide a command to minimize
 * all LinkableObjects displayed in the reference/annotation area for
 * the given Resource.
 * 
 * @author John Bradley
 */

public class MinimizeAllContainedObjectCommand extends MinMaxBaseCommand {

	public MinimizeAllContainedObjectCommand(Resource myResource) {
		super("minimize all");
		this.setList(myResource.getMyPagedDisplayedItems().getItems());
	}
}
