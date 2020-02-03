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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
/**
 * the item in the Resource Explorer's Date-oriented tree display that tracks months in
 * which Pliny Resources were created. 
 * 
 *
 * @author John Bradley
 *
 */

public class DatedMonthItem implements IResourceExplorerItem {

	private static SimpleDateFormat monthFormatter = new SimpleDateFormat("MMM yyyy");
	private String myLabel;
	private IResourceExplorerItem parent;
	private DatedDayItem[] days = new DatedDayItem[31];
	private int itemcount;
	private Vector myChildren = null;
	
	public DatedMonthItem(IResourceExplorerItem parent, int year, int month){
		this.parent = parent;
		Calendar cal = new GregorianCalendar();
		cal.set(year,month,1);
		myLabel = monthFormatter.format(cal.getTime());
		itemcount = 0;
	}
	
	public void addDayItem(DatedDayItem item){
		DatedResourceRoot.calendar.setTime(item.getDate());
		int day = DatedResourceRoot.calendar.get(Calendar.DATE)-1; // returned date is one-based   j.b.
		if(days[day] != null)return;
		itemcount += item.getNumberChildren();
		days[day] = item;
	}
	
	public boolean canModify() {
		return false;
	}

	public void dispose() {
		for(int i = 0; i < 31; i++)
			if(days[i]!= null)days[i].dispose();
	}

	public Object getAssociatedObject() {
		return null;
	}

	public List getChildren() {
		if(myChildren == null){
			myChildren = new Vector();
			for(int i = 0; i < 31; i++)
				if(days[i] != null)myChildren.add(days[i]);
		}
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
		return myLabel+" ("+getNumberChildren()+")";
	}

	public boolean hasChildren() {
		// TODO Auto-generated method stub
		return itemcount > 0;
	}

	public void setText(String name) {
		// unchangable   j.b.
	}

	public void changeCount(int change) {
		itemcount += change;
	}

}
