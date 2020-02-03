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

import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;

/**
 * provides a common code base for commands to minimise or maximise
 * a group of LinkableObjects.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.commands.MinimizeAllContainedObjectCommand
 * @see uk.ac.kcl.cch.jb.pliny.commands.MinMaxSelectedItemsCommand
 * 
 * @author John Bradley
 *
 */

public class MinMaxBaseCommand extends Command {
	
	private Vector list;
	private Vector changedObjects = null;
	private boolean setToOpen = false;

	public MinMaxBaseCommand() {
		super();
		// TODO Auto-generated constructor stub
	}

	public MinMaxBaseCommand(String label) {
		super(label);
		// TODO Auto-generated constructor stub
	}
	
	public MinMaxBaseCommand(boolean setToOpen){
		super();
		this.setToOpen = setToOpen;
	}
	
	/**
	 * provides the list of LinkableObjects for which the
	 * mass minimization/expansion is to be applied.
	 * 
	 * @param list items to be used by this command.
	 */
	
	protected void setList(Vector list){
		this.list = new Vector(list);
	}

    public void execute(){
    	Iterator it = list.iterator();
    	changedObjects = new Vector();
    	while(it.hasNext()){
    		Object obj = it.next();
    		if(obj instanceof LinkableObject){
	    		LinkableObject surr = (LinkableObject)obj;
	    		if((surr.getSurrogateFor() != null) && (surr.getIsOpen() != setToOpen)){
	    			changedObjects.add(surr);
	    			surr.setIsOpen(setToOpen);
	    		}
    		}
    	}
    }
    
    private void processChangedOnes(boolean setValue){
    	Iterator it = changedObjects.iterator();
    	while(it.hasNext()){
    		LinkableObject surr = (LinkableObject)it.next();
    		surr.setIsOpen(setValue);
    	}
    }
    
    public void undo(){
    	processChangedOnes(!setToOpen);
    }
    
    public void redo(){
    	processChangedOnes(setToOpen);
   }
}
