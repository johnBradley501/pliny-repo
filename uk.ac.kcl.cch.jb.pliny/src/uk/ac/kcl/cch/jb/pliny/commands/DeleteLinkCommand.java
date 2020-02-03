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

import uk.ac.kcl.cch.jb.pliny.model.Link;

/**
 * deletes a 
 * {@link uk.ac.kcl.cch.jb.pliny.model.Link} item. 
 * 
 * @author John Bradley
 *
 */

public class DeleteLinkCommand extends Command {

	Link theLink;
	//LinkableObject from;
	//LinkableObject to;
	
	public DeleteLinkCommand(Link theLink) {
		super("delete link");
		this.theLink = theLink;
	}
	
	public void execute(){
		theLink.backupAndClearLinks();
		//from = theLink.getFromLink();
		//to = theLink.getToLink();
		//theLink.setFromLink(null);
		//theLink.setToLink(null);
		theLink.deleteMe();
	}
	
	public void undo(){
		theLink.reIntroduceMe();
		//theLink.setFromLink(from);
		//theLink.setToLink(to);
		theLink.restoreLinks();
	}

}
