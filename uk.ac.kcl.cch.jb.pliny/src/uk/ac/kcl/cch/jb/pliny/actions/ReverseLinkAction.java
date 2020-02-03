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

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.commands.ReverseLinkCommand;
import uk.ac.kcl.cch.jb.pliny.model.Link;

public class ReverseLinkAction extends Action {
	
	private Vector items;
	private CommandStack commandStack;
	
    public ReverseLinkAction(Vector items, CommandStack commandStack) {
	   super("reverse link direction");
	   this.items = items;
	   this.commandStack = commandStack;
		this.setImageDescriptor(
				ImageDescriptor.createFromImage(PlinyPlugin.getDefault().getImage("icons/reverseLinkIcon.gif")));
    }
    
    public void run(){
    	CompoundCommand cmds = new CompoundCommand("reverse direction");
    	Iterator it = items.iterator();
    	while(it.hasNext()){
    		Object obj = it.next();
    		if(obj instanceof Link)cmds.add(new ReverseLinkCommand((Link)obj));
    	}
    	if(cmds.size() > 0)
    		commandStack.execute(cmds);
    }
}
