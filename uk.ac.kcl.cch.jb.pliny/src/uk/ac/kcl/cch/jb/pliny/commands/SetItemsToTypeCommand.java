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

/**
 * command to set all the provided items to a specified 
 * {@link uk.ac.kcl.cch.jb.pliny.model.LOType}.
 * 
 * @author John Bradley
 */

public class SetItemsToTypeCommand extends Command {

	private Vector items;
	private LOType newType;
	Vector restoreData;
	
	/**
	 * creates an instance of this command and provides the list of
	 * items (all of which must be {@link uk.ac.kcl.cch.jb.pliny.model.IHasLoType}s
	 *  -- LinkableObjects or Links -- to be changed to the provide <code>LOType</code>.
	 * 
	 * @param items Vector of items to be assigned the new type.
	 * @param newType LOType new type to assign them to.
	 */
	public SetItemsToTypeCommand(Vector items, LOType newType) {
		super();
		String name = newType.getName();
		if(name == null || name.length() == 0)name = "(default)";
		this.setLabel("set type to '"+name+"'");
		this.items = items;
		this.newType = newType;
		restoreData = new Vector();
	}

	public void execute(){
		Iterator it = items.iterator();
		while(it.hasNext()){
			Object obj = it.next();
			if(obj instanceof IHasLoType){
				IHasLoType obj2 = (IHasLoType)obj;
				restoreData.add(obj2);
				restoreData.add(obj2.getLoType());
				obj2.setLoType(newType);
			}
		}
	}
	
	public void undo(){
		Iterator it = restoreData.iterator();
		while(it.hasNext()){
			IHasLoType obj = (IHasLoType)it.next();
			LOType oldType = (LOType)it.next();
			obj.setLoType(oldType);
		}
	}
	
	public void redo(){
		Iterator it = restoreData.iterator();
		while(it.hasNext()){
			IHasLoType obj = (IHasLoType)it.next();
			it.next();
			obj.setLoType(newType);
		}
	}
}
