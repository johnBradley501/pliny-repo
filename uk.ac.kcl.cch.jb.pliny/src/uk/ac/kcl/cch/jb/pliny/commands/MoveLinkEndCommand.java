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
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;

/**
 * changes either end (source or target) of a {@link uk.ac.kcl.cch.jb.pliny.model.Link}
 *  from one {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject}
 * to another.  At least one of <code>setToEnd</code> or <code>setFromEnd</code>
 * must be used before the command is execuated to specify an end to change.
 *  
 * @author John Bradley
 *
 */

public class MoveLinkEndCommand extends Command {
	
	Link link = null;
	LinkableObject toEnd, oldToEnd;
	LinkableObject fromEnd, oldFromEnd;
	String oldToAttr, oldFromAttr;

	/**
	 * creates an instance of this command, and provides the Link to
	 * which it is to apply.
	 * 
	 * @param link the link for which an end is to change.
	 */
	public MoveLinkEndCommand(Link link) {
		super("Move Link End");
		this.link = link;
		oldToEnd = link.getToLink();
		toEnd = oldToEnd;
		oldFromEnd = link.getFromLink();
		fromEnd = oldFromEnd;
		oldToAttr = link.getToAttr();
		oldFromAttr = link.getFromAttr();
	}

	/**
	 * specify the new target {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject} for the link.
	 * 
	 * @param object target end
	 */
	
	public void setToEnd(LinkableObject object) {
		toEnd = object;
	}
	
	public LinkableObject getToEnd(){
		return toEnd;
	}
	/**
	 * specify the new source {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject} for the link.
	 * 
	 * @param object target end
	 */

	public void setFromEnd(LinkableObject object) {
		fromEnd = object;
	}
	
	public LinkableObject getFromEnd(){
		return fromEnd;
	}
	
	public void execute(){
		if(oldToEnd != toEnd){
			link.setToAttr(null);
			link.setToLink(toEnd);
		}
		if(oldFromEnd != fromEnd){
			link.setFromAttr(null);
			link.setFromLink(fromEnd);
		}
	}

	public void undo(){
		if(oldToEnd != toEnd){
			link.setToLink(oldToEnd);
			link.setToAttr(oldToAttr);
		}
		if(oldFromEnd != fromEnd){
			link.setFromLink(oldFromEnd);
			link.setFromAttr(oldFromAttr);
		}
	}
}
