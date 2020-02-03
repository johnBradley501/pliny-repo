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

public class ReverseLinkCommand extends Command {
	
	private Link theLink;
	
   public ReverseLinkCommand(Link theLink){
	   super("reverse direction");
	   this.theLink = theLink;
   }
   
   public void execute(){
	   LinkableObject a = theLink.getToLink();
	   LinkableObject b = theLink.getFromLink();
	   theLink.setToLink(null);
	   
	   String oldFromText = theLink.getFromAttr();
	   theLink.setFromAttr(theLink.getToAttr());
	   theLink.setToAttr(oldFromText);
	   
	   theLink.setFromLink(a);
	   theLink.setToLink(b);
   }
   
   public void undo(){
	   execute();
   }
}
