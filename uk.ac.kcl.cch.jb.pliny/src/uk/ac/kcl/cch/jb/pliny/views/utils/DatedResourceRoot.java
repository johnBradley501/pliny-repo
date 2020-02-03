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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.swt.graphics.Image;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.rdb2java.Rdb2javaPlugin;
import uk.ac.kcl.cch.rdb2java.dynData.CountItem;


/**
 * the root object for the Resource Explorer's Date-oriented tree display. This item has the complex
 * job of creating the top level date tree, which will always contain the "today" item, but may
 * also contain Year, Month and Day-of-month items too.
 * 
 * @author John Bradley
 *
 */
public class DatedResourceRoot implements IResourceExplorerItem, PropertyChangeListener {

	private Vector myChildren = null;
	private DatedTodayItem todayItem = null;
	private IResourceTreeDisplayer myView = null;
	private Date today = null;
	private int yearToday = 0;
	private int monthToday = 0;
	private Map dateInfo = new HashMap();
	private DatedMonthItem currentMonthItem = null;
	private int currentMonth = 0;
	private DatedYearItem currentYearItem = null;
	private int currentYear = 0;
	
	public static Calendar calendar = new GregorianCalendar();
	
	public DatedResourceRoot(IResourceTreeDisplayer myView){
		Rdb2javaPlugin.getDataServer().addPropertyChangeListener(this);
		this.myView = myView;
		GregorianCalendar now = new GregorianCalendar();
		now.setTimeInMillis(System.currentTimeMillis());
		calendar = new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
		//today = new Date(System.currentTimeMillis()); //1 day = 24 × 60 × 60 = 86400 j.b. 
		today = new Date(calendar.getTimeInMillis()); //1 day = 24 × 60 × 60 = 86400 j.b. 
		//calendar.setTime(today);
		yearToday = calendar.get(Calendar.YEAR);
		monthToday = calendar.get(Calendar.MONTH);
		currentMonth = monthToday;
		currentYear = yearToday;
	}

	public void dispose() {
		Rdb2javaPlugin.getDataServer().removePropertyChangeListener(this);
		Iterator it = myChildren.iterator();
		while(it.hasNext()){
			IResourceExplorerItem item = (IResourceExplorerItem)it.next();
			item.dispose();
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		String pString = evt.getPropertyName();
		if(pString.equals("Delete-Resource"))deleteResource((Resource)evt.getOldValue());
		else if(pString.equals("Create-Resource"))addResource((Resource)evt.getNewValue());
	}
	
	private void updateTreeLabels(DatedTodayItem item, int change){
		IResourceExplorerItem updateThis = item;
        IResourceExplorerItem par = item.getParent();
        while(par != null){
        	if(par instanceof DatedMonthItem){
        		updateThis = par;
        		((DatedMonthItem)par).changeCount(change);
        	} else if(par instanceof DatedYearItem){
        		updateThis = par;
        		((DatedYearItem)par).changeCount(change);
        	}
        	par = par.getParent();
        }
        myView.getMyViewer().refresh(updateThis, true);
	}

	private void addResource(Resource resource) {
		Date rDate = resource.getCreationDate();
		DatedTodayItem item;
		if(rDate.getTime() >= today.getTime())item = todayItem; 
		else item = (DatedDayItem)dateInfo.get(rDate);
		if(item == null)item = todayItem; // sometimes it seems that dates are wrong -- this is a kludge   j.b.
        item.addResource(resource);
        updateTreeLabels(item, 1);
	}

	private void deleteResource(Resource resource) {
		Date rDate = resource.getCreationDate();
		DatedTodayItem item;
		if(rDate.getTime() >= today.getTime())item = todayItem; 
		else item = (DatedDayItem)dateInfo.get(rDate);
        item.removeResource(resource); 
        updateTreeLabels(item, -1);
	}

	public boolean canModify() {
		return false;
	}

	public Object getAssociatedObject() {
		return null;
	}

	public List getChildren() {
		if(myChildren == null)buildChildren();
		return myChildren;
	}

	public Image getIcon() {
		return null;
	}

	public int getNumberChildren() {
		return 1;
	}

	public int getPageNumber() {
		return 0;
	}

	public IResourceExplorerItem getParent() {
		return null;
	}

	public String getText() {
		return "root";
	}

	public boolean hasChildren() {
		return true;
	}

	public void setText(String name) {
		// do nothing -- this is a root object
	}
	
	private void buildChildren(){
		myChildren = new Vector();
		todayItem = new DatedTodayItem(myView, this, today);
		myChildren.add(todayItem);
		
		Iterator<CountItem> it = Rdb2javaPlugin.getDataServer().queryCount("creationDate", "Resource", "resourceKey", "desc").iterator();
		
		while(it.hasNext()){
			CountItem data = it.next();
			if(!data.getIndex().equals(today))buildDayItem(data);
		}
		//Connection con = PlinyPlugin.getDBServicesInstance().getConnection();
		//Connection con = DBServices.getConnection();
		//try {
		//	String sql = "select creationDate, count(resourceKey) from Resource "+
		//	             "where creationDate < '"+today.toString()+"' "+
        //                "group by creationDate order by creationDate desc";
			//PreparedStatement stmt = con.prepareStatement(sql);
		//	Statement stmt = con.createStatement();
		//	ResultSet rs = stmt.executeQuery(sql);
		//	while(rs.next()){
		//		Date date = rs.getDate(1);
		//		int count = rs.getInt(2);
		//		if(!date.equals(today))
		//		    buildDayItem(date, count);
		//	}
		//} catch(Exception e){
		//    e.printStackTrace(System.out);
		//} finally {
		//    //PlinyPlugin.getDBServicesInstance().returnConnection(con);
		//	DBServices.returnConnection(con);
		//}
	}
	
	private void buildDayItem(CountItem data) {
		Date date = (Date)data.getIndex();
		int count = data.getCount();
		calendar.setTime(date);
		DatedDayItem item = null;
		//DatedDayItem item = new DatedDayItem(myView, this, date, count);
		if(calendar.get(Calendar.YEAR)< yearToday){
			int newYear = calendar.get(Calendar.YEAR);
			if(currentYear > newYear){
				currentYearItem = new DatedYearItem(myView, this, newYear);
				myChildren.add(currentYearItem);
				currentMonth = 13;
				currentYear = newYear;
			}
			int newMonth = calendar.get(Calendar.MONTH);
			if(currentMonth > newMonth){
                currentMonthItem = currentYearItem.addMonth(newMonth);
                currentMonth = newMonth;
			}
			item = new DatedDayItem(myView, currentMonthItem, date, count);
			currentMonthItem.addDayItem(item);
		} else if(calendar.get(Calendar.MONTH) < monthToday){
			int newMonth = calendar.get(Calendar.MONTH);
			if(currentMonth > newMonth){
                //currentMonthItem = currentYearItem.addMonth(newMonth);
				currentMonthItem = new DatedMonthItem(this, currentYear, newMonth);
                currentMonth = newMonth;
                myChildren.add(currentMonthItem);
			}
			item = new DatedDayItem(myView, currentMonthItem, date, count);
			currentMonthItem.addDayItem(item);
		} else {
			item = new DatedDayItem(myView, this, date, count);
			myChildren.add(item);
		}
		dateInfo.put(date, item);
	}
}
