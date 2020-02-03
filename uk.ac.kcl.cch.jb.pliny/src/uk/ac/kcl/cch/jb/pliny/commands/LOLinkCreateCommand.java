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

import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.Link;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.VirtualLinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.VirtualResource;

/**
 * creates a new {@link uk.ac.kcl.cch.jb.pliny.model.Link} to connect two given 
 * {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject}s.
 * @author John Bradley
 *
 */

public class LOLinkCreateCommand extends Command {
	
	LinkableObject from = null;
	LinkableObject to = null;
	Link myLink = null;
	IExtraLinkingWorkObject fromWork = null;
	IExtraLinkingWorkObject toWork = null;

	public LOLinkCreateCommand() {
		super("Create Link");
	}
	
	/**
	 * provides the source end of the link.
	 * 
	 * @param obj source LinkableObject
	 */
	
	public void setFrom(LinkableObject obj){
		from = obj;
	}
	
	public LinkableObject getFrom(){
		return from;
	}
	
	public void setFromWork(IExtraLinkingWorkObject fromObject){
		this.fromWork = fromObject;
	}

	
	/***
	 * provides the target end of the link.
	 * 
	 * @param obj target LinkableObject
	 */
	
	public void setTo(LinkableObject obj){
		to = obj;
	}
	
	public LinkableObject getTo(){
		return to;
	}
	
	public void setToWork(IExtraLinkingWorkObject toObject){
		this.toWork = toObject;
	}
	
	/**
	 * checks to see if both a from and to (source and target)
	 * LinkableObject has been provided.
	 * 
	 * @return indicates that all needed data has been provided.
	 */
	
	public boolean isComplete(){
		return ((from!= null) && (to != null));
	}

	public void execute(){
		if(!isComplete())return;
		if(to.getALID() == 0 && (to instanceof VirtualLinkableObject))
			//checkVirtualLinkableObject((VirtualLinkableObject)to);
		    to.reIntroduceMe();
		if(from.getALID() == 0 && (from instanceof VirtualLinkableObject))
			//checkVirtualLinkableObject((VirtualLinkableObject)from);
		    from.reIntroduceMe();
		if(myLink == null)myLink = new Link();
		else myLink.reIntroduceMe();
		
		myLink.setLoType(LOType.getCurrentType());
		myLink.setToLink(to);
		myLink.setFromLink(from);
		
		if(toWork != null)toWork.run(myLink);
		if(fromWork != null)fromWork.run(myLink);
	}
	
	public void undo(){
		
		if(fromWork != null)fromWork.undo(myLink);
		if(toWork != null)toWork.undo(myLink);
		
		myLink.setToLink(null);
		myLink.setFromLink(null);
		myLink.setLoType(null);
		myLink.deleteMe();
	}

}
