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
import java.util.List;
import java.util.Vector;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;

import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.parts.LinkableObjectPart;
import uk.ac.kcl.cch.jb.pliny.parts.NoteTextPart;
import uk.ac.kcl.cch.jb.pliny.parts.TextContentHolderPart;

/**
 * @author John Bradley
 *
 */
public class OptimiseSizeCommand extends Command {

	private Vector selectedEditParts;
	private Vector changedItems;
	
	public OptimiseSizeCommand(Vector selectedEditParts) {
		super("optimise size");
		this.selectedEditParts = selectedEditParts;
		changedItems = new Vector();
	}
	
	private class ChangedItem{
		
		private LinkableObject lo;
		private Dimension oldSize;
		private Dimension newSize;
		public ChangedItem(LinkableObject lo, Dimension oldSize, Dimension newSize){
			this.lo = lo;
			this.oldSize = oldSize;
			this.newSize = newSize;
		}
		
		public LinkableObject getLo(){return lo;}
		public Dimension getOldSize(){return oldSize;}
		public Dimension getNewSize(){return newSize;}
		
		public void updateLo(Dimension d){
			Rectangle r = new Rectangle(lo.getDisplayRectangle());
			r.setSize(d);
			lo.setDisplayRectangle(r);
		}
	}
	
	private Dimension getTextDimension(LinkableObjectPart itemPart){
		LinkableObject itemLo = itemPart.getLinkableObject();
		if((!(itemLo.getSurrogateFor() instanceof NoteLucened))
		|| (!itemLo.getIsOpen()) || (itemLo.getShowingMap())) return null;
		
        List lopChildren = itemPart.getChildren();
		if(lopChildren.size()!=1)return null;
		EditPart ep = (EditPart)lopChildren.get(0);
		if(!(ep instanceof TextContentHolderPart))return null;
		
		List tcChildren = ep.getChildren();
		if(tcChildren.size() != 1)return null;
		
		ep =(EditPart)tcChildren.get(0);
		if(!(ep instanceof NoteTextPart))return null;
		NoteTextPart ntp = (NoteTextPart)ep;
		return ntp.getTextSize();
	}
	
	public void execute(){
		Iterator it = selectedEditParts.iterator();
		while(it.hasNext()){
			Object item = it.next();
			if(item instanceof LinkableObjectPart){
				LinkableObjectPart itemPart = (LinkableObjectPart)item;
				Dimension textDim = getTextDimension(itemPart);
				if(textDim != null){
					LinkableObject thisLo = itemPart.getLinkableObject();
					Dimension oldDim = thisLo.getDisplayRectangle().getSize();
					//Dimension newDim = new Dimension(textDim.width+2,textDim.height+18);
					Dimension newDim = new Dimension(oldDim.width,textDim.height+35);
					ChangedItem ci = new ChangedItem(thisLo, oldDim, newDim);
					changedItems.add(ci);
					ci.updateLo(newDim);
				}
			}
		}
	}
	
	public void undo(){
		Iterator it = changedItems.iterator();
		while(it.hasNext()){
			ChangedItem ci = (ChangedItem)it.next();
			ci.updateLo(ci.getOldSize());
		}
	}
	
	public void redo(){
		Iterator it = changedItems.iterator();
		while(it.hasNext()){
			ChangedItem ci = (ChangedItem)it.next();
			ci.updateLo(ci.getNewSize());
		}
	}

}
