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

package uk.ac.kcl.cch.jb.pliny.views.utils;

import java.util.List;
import java.util.Vector;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
/**
 * the item in the Resource Explorer's Date-oriented tree display that tracks years in
 * which Pliny Resources were created. 
 * 
 *
 * @author John Bradley
 *
 */

public class DatedYearItem implements IResourceExplorerItem {

	private IResourceExplorerItem parent;
	private int myYear = 0;
	private DatedMonthItem[] months = new DatedMonthItem[12];
	private int itemcount;
	private Vector myChildren = null;
	
	public DatedYearItem(IResourceTreeDisplayer myView, IResourceExplorerItem parent, int myYear) {
		this.parent = parent;
		this.myYear = myYear;
		itemcount = 0;
	}

	public DatedMonthItem addMonth(int newMonth) {
		months[newMonth] = new DatedMonthItem(this, myYear, newMonth);
		return months[newMonth];
	}

	public boolean canModify() {
		return false;
	}

	public void dispose() {
		for(int i = 0; i < 12; i++)
			if(months[i]!= null)months[i].dispose();
	}

	public Object getAssociatedObject() {
		return null;
	}
	
	private void buildMyChildren(){
		itemcount = 0;
		myChildren = new Vector();
		for(int i = 0; i < 12; i++)
			if(months[i] != null){
				myChildren.add(months[i]);
				itemcount += months[i].getNumberChildren();
			}
	}

	public List getChildren() {
		if(myChildren == null)buildMyChildren();
		return myChildren;
	}

	public Image getIcon() {
		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
	}

	public int getNumberChildren() {
		return itemcount;
	}

	public int getPageNumber() {
		return 0;
	}

	public IResourceExplorerItem getParent() {
		return parent;
	}

	public String getText() {
		if(myChildren == null)buildMyChildren();
		return myYear+" ("+itemcount+")";
	}

	public boolean hasChildren() {
		return true;
	}

	public void setText(String name) {
		// text not changeable  j.b.
	}

	public void changeCount(int change) {
		itemcount += change;
	}

}
