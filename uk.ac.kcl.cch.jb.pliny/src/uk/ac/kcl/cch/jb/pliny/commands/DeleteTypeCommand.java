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

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.gef.commands.Command;

import uk.ac.kcl.cch.jb.pliny.model.IHasLoType;
import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * deletes a 
 * {@link uk.ac.kcl.cch.jb.pliny.model.LOType} item and transfers any
 * LinkableObjects or Links that refer to this one being deleted to
 * a specified LOType. If the deleted type is the current type, the
 * specified replacement LOType becomes the current type instead.
 * 
 * @author John Bradley
 *
 */

public class DeleteTypeCommand extends Command {

	private LOType deleteMe;
	private LOType type2;
	private Vector reassignees;
	private Resource targetRole;
	private Resource sourceRole;
	
	/**
	 * deletes the specified type, and maps all references (Linkable
	 * Objects or Links) to the specified type <code>type2</code>.
	 * @param deleteMe the LOType to be deleted.
	 * @param type2 the LOType to replace all references to the deleted LOType.
	 */
	public DeleteTypeCommand(LOType deleteMe, LOType type2) {
		super("delete '"+deleteMe.getName()+"'");
		this.deleteMe = deleteMe;
		this.type2 = type2;
	}
	
	private void processReassignees(LOType newType){
		Iterator it = reassignees.iterator();
		while(it.hasNext()){
			IHasLoType obj = (IHasLoType)it.next();
			if(obj.getALID() != 0)
			   obj.setLoType(newType);
		}
		
	}
	
	public void execute(){
		if(type2 == null)return;
		if(LOType.getCurrentType()==deleteMe)
			LOType.setCurrentType(type2);
		reassignees = new Vector(deleteMe.getLinkableObjects().getItems());
		reassignees.addAll(deleteMe.getLinks().getItems());
		processReassignees(type2);
		
		sourceRole = deleteMe.getSourceRole();
		deleteMe.setSourceRole(null);
		targetRole = deleteMe.getTargetRole();
		deleteMe.setTargetRole(null);
		
		deleteMe.deleteMe();
	}
	
	public void undo(){
		if(type2 == null)return;
		deleteMe.reIntroduceMe();
		deleteMe.setSourceRole(sourceRole);
		deleteMe.setTargetRole(targetRole);
		processReassignees(deleteMe);
	}

}
