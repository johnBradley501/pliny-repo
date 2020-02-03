/*******************************************************************************
 * Copyright (c) 2008 John Bradley
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

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

import uk.ac.kcl.cch.jb.pliny.figures.LinkableObjectFigure;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.utils.TwoDOrderer;

/**
 * this aligns LinkableObjects that are passed to it.  The objects that
 * are aligned are those whose positions are defined by a rectangle
 * area, and the algorithm to do this is found in
 * {@link uk.ac.kcl.cch.jb.pliny.utils.TwoDOrderer}
 * 
 * Since version 1.1
 * 
 * @author John Bradley
 *
 */
public class AlignItemsCommand extends Command {

   private Vector list;
   private boolean vertical;
   private boolean minimize;
   private int newWidth = 0, newHeight = 0;
   
   private class DataItem {
	   LinkableObject myLO;
	   Rectangle oldBounds, newBounds;
	   boolean wasOpen;
   }
   
   private DataItem items[];
   
   public AlignItemsCommand(boolean vertical, boolean minimize){
	   super("Align items");
	   this.vertical = vertical;
	   this.minimize = minimize;
   }

   public AlignItemsCommand(String label, boolean vertical, boolean minimize){
	   super(label);
   }
	
	/**
	 * provides the list of LinkableObjects for which the
	 * mass alignment is to be applied.
	 * 
	 * @param list items to be used by this command.
	 */
	
	public void setList(Vector list){
		this.list = new Vector(list);
	}
	
	public void execute(){
		if(list == null)return;
		Vector orderedItems = TwoDOrderer.selectAndOrderContents(list, vertical);
		if(orderedItems == null || orderedItems.size() <= 1)return;
		items = new DataItem[list.size()];
		Iterator it = orderedItems.iterator();
		int i = 0;
		while(it.hasNext()){
    		LinkableObject surr = (LinkableObject)it.next();
    		Rectangle bound = surr.getDisplayRectangle();
			if(bound != null){
				items[i] = new DataItem();
				items[i].myLO = surr;
				items[i].oldBounds = bound;
				if(bound.width > newWidth)newWidth = bound.width;
				if(bound.height > newHeight)newHeight = bound.height;
				items[i].wasOpen = surr.getIsOpen();
				i++;
			}
		}
		Rectangle prevBound = null;
		for(i = 0; i < items.length; i++){
			if(vertical)prevBound = defineBoundsVert(prevBound, items[i]);
			else prevBound = defineBoundsHoriz(prevBound, items[i]);
		}
	}

	private Rectangle defineBoundsVert(Rectangle prevBound, DataItem item) {
		if(item == null)return prevBound;
		Rectangle newBound = new Rectangle();
		Rectangle currentBound = item.myLO.getDisplayRectangle();
		
		if(prevBound != null)newBound.y = prevBound.y + prevBound.height;
		else newBound.y = currentBound.y;
		if(prevBound != null)newBound.x = prevBound.x;
		else newBound.x = currentBound.x;
		newBound.height = currentBound.height;
		newBound.width = newWidth;
		
		item.newBounds = newBound;
		if(minimize && item.myLO.getIsOpen())item.myLO.setIsOpen(false);
		item.myLO.setDisplayRectangle(newBound);
		
		Rectangle newVisualBound = new Rectangle(newBound);
		if(minimize || !item.myLO.getIsOpen())newVisualBound.height = LinkableObjectFigure.MINIMIZED_HEIGHT;
		return newVisualBound;
	}
	
	private Rectangle defineBoundsHoriz(Rectangle prevBound, DataItem item) {
		if(item == null)return prevBound;
		Rectangle newBound = new Rectangle();
		Rectangle currentBound = item.myLO.getDisplayRectangle();
		
		if(prevBound != null)newBound.x = prevBound.x + prevBound.width;
		else newBound.x = currentBound.x;
		if(prevBound != null)newBound.y = prevBound.y;
		else newBound.y = currentBound.y;
		newBound.width = currentBound.width;
		newBound.height = newHeight;
		
		item.newBounds = newBound;
		if(minimize && item.myLO.getIsOpen())item.myLO.setIsOpen(false);
		item.myLO.setDisplayRectangle(newBound);
		
		return newBound;
	}

	public void undo(){
		if(items == null)return;
		for(int i = 0; i < items.length; i++){
			if(items[i] != null){
			   if(minimize && !items[i].myLO.getIsOpen())items[i].myLO.setIsOpen(true);
			   items[i].myLO.setDisplayRectangle(items[i].oldBounds);
			}
		}
	}
	
    public void redo(){
		if(items == null)return;
		for(int i = 0; i < items.length; i++){
			if(items[i] != null){
				   if(minimize && items[i].myLO.getIsOpen())items[i].myLO.setIsOpen(false);
				   items[i].myLO.setDisplayRectangle(items[i].newBounds);
				}
		}
   }
}
