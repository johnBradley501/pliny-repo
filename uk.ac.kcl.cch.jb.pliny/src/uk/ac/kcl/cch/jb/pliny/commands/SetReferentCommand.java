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

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * a command to take the given 
 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource} and set it up as
 * the current Referent.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.PlinyPlugin
 * 
 * @author John Bradley
 *
 */

public class SetReferentCommand extends Command {

	private Resource myResource;
	private Resource oldResource;
	
	/**
	 * create instance of this command that will set the given
	 * resource up as the referent.
	 * 
	 * @param resource Resource to be used as Referent.
	 */
	
	public SetReferentCommand(Resource resource) {
		super();
		myResource = resource;
	}
	
	public void execute(){
		oldResource = PlinyPlugin.getReferent();
		PlinyPlugin.setReferent(myResource);
	}
	
	public void undo(){
		PlinyPlugin.setReferent(oldResource);
	}
}
