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

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Vector;

import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.ResourceQuery;
import uk.ac.kcl.cch.rdb2java.dynData.BaseQuery;

public class DatedDayItem extends DatedTodayItem {

	/**
	 * the item in the Resource Explorer's Date-oriented tree display that tracks resources
	 * that are created on a specific date. 
	 * 
	 *
	 * @author John Bradley
	 *
	 */

	private String label = null;
	private static SimpleDateFormat sdo = new SimpleDateFormat("EEE, d MMM yyyy");
	
	public DatedDayItem(IResourceTreeDisplayer myView, IResourceExplorerItem parent, Date today, int count){
		super(myView, parent, today);
		label = sdo.format(today);
		this.count = count;
	}
	
	public String getText() {
		return label+" ("+getNumberChildren()+")";
	}
	
	public int getNumberChildren() {
		if(count < 0)count = super.getNumberChildren();
		return count;
	}

	protected void buildItems(){
		//buildItems("Resource.creationDate ='"+today.toString()+"'");
		buildItems("creationDate", BaseQuery.FilterEQUAL, today);
	}
	

}
