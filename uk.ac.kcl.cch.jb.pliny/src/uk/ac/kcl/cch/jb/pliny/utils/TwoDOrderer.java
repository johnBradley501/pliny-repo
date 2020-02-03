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
package uk.ac.kcl.cch.jb.pliny.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.eclipse.draw2d.geometry.Rectangle;

import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;

/**
 * this utility object takes a list of LinkableObjects that are assumed 
 * to contained rectangular display area items and orders them by their
 * upper left hand corner.
 * 
 * This is used in the actions
 * {@link uk.ac.kcl.cch.jb.pliny.actions.TextFileGenerator}
 * and
 * {@link uk.ac.kcl.cch.jb.pliny.actions.AlignItemsCommand}
 * 
 * It has been designed so that in any case I can currently imagine it
 * need not be instantiated at all, but used only through its <code>static</code>
 * method <code>selectAndOrderContents</code>.
 * 
 * Since version 1.1
 * 
 * @author John Bradley
 *
 */

public class TwoDOrderer implements Comparable {
	
	private LinkableObject obj;
	int x,y,count;
	boolean yPriority;
	
	public TwoDOrderer(LinkableObject obj, int count, boolean yPriority){
		this.obj = obj;
		Rectangle pos = obj.getDisplayRectangle();
		y = pos.y;
		x = pos.x;
		this.count = count;
		this.yPriority = yPriority;
	}
	
	public LinkableObject getLinkableObject(){
		return obj;
	}

	public int compareTo(Object arg0) {
		if(!(arg0 instanceof TwoDOrderer)) return -1;
		TwoDOrderer tester = (TwoDOrderer) arg0;
		if(yPriority){
		   if(y < tester.y)return -1;
		   if(y > tester.y)return 1;
		}
		if(x < tester.x)return -1;
		if(x > tester.x)return 1;
		if(!yPriority){
		   if(y < tester.y)return -1;
		   if(y > tester.y)return 1;
		}
		if(count > tester.count)return -1;
		if(count < tester.count)return 1;
		return 0;
	}
	
	public static Vector selectAndOrderContents(Vector contents, boolean yPriority){
		if(contents.size() == 0)return contents;
		Map orderer = new TreeMap();
		
		Iterator it = contents.iterator();
		int cnt = 0;
		while(it.hasNext()){
			LinkableObject lo = (LinkableObject)it.next();
			if(lo.getLoType().getALID() != LOType.getBibRefType().getALID() && lo.getDisplayRectangle() != null){
			   cnt++;
			   TwoDOrderer thisOrder = new TwoDOrderer(lo, cnt, yPriority);
			   orderer.put(thisOrder, lo);
			}
		}
		return new Vector(orderer.values());
	}
}
