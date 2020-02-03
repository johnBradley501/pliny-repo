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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.kcl.cch.rdb2java.Rdb2javaPlugin;
import uk.ac.kcl.cch.rdb2java.dynData.CountItem;

public class DateFilterManager {
	
   static Calendar myCalendar = new GregorianCalendar();
	
   SortedMap years = new TreeMap();
   DateFilterYearControl startYearControl;
   DateFilterYearControl endYearControl;
   DateFilterMonthControl startMonthControl;
   DateFilterMonthControl endMonthControl;
   DateFilterDayControl startDayControl;
   DateFilterDayControl endDayControl;
	
   public DateFilterManager(){
	   buildDateData();
   }
   
   public void dispose(){
	   startYearControl.dispose();
	   endYearControl.dispose();
	   startMonthControl.dispose();
	   endMonthControl.dispose();
	   startDayControl.dispose();
	   endDayControl.dispose();
   }

private void buildDateData() {
	Iterator<CountItem> it = Rdb2javaPlugin.getDataServer().queryCount("creationDate", "Resource", "resourceKey", "desc").iterator();
    while(it.hasNext()){
    	CountItem data = it.next();
    	processDate(data);
    }
	//Connection con = DBServices.getConnection();
	//try {
	//	String sql = "select creationDate, count(resourceKey) from Resource "+
    //                 "group by creationDate order by creationDate desc";
	//	//PreparedStatement stmt = con.prepareStatement(sql);
	//	Statement stmt = con.createStatement();
	//	ResultSet rs = stmt.executeQuery(sql);
	//	while(rs.next()){
	//		Date date = rs.getDate(1);
	//		int count = rs.getInt(2);
	//		processDate(date, count);
	//	}
	//} catch(Exception e){
	//    e.printStackTrace(System.out);
	//} finally {
	//    //PlinyPlugin.getDBServicesInstance().returnConnection(con);
	//	DBServices.returnConnection(con);
	//}
}

private void processDate(CountItem data) {
	Date date = (Date)data.getIndex();
	int count = data.getCount();
	int year = date.getYear()+1900;
	int mon = date.getMonth()+1;
	int day = date.getDate();
	
	Integer yearKey = new Integer(year);
	SortedMap monthForYear = null; 
	if(!years.containsKey(yearKey)){
		monthForYear = new TreeMap();
		years.put(yearKey, monthForYear);
	} else monthForYear = (SortedMap)years.get(yearKey);
	
	Integer monthKey = new Integer(mon);
	SortedMap dayForMonth = null;
	if(monthForYear.containsKey(monthKey)){
		dayForMonth = (SortedMap)monthForYear.get(monthKey);
	} else {
		dayForMonth = new TreeMap();
		monthForYear.put(monthKey, dayForMonth);
	}
	
	Integer dayKey = new Integer(day);
	dayForMonth.put(dayKey, new Integer(count));
}

public Composite getDateFilterDisplay(Composite parent, int style){
	Composite composite = new Composite (parent, style);
	GridLayout layout = new GridLayout ();
	layout.numColumns = 2;
	composite.setLayout(layout);
	Label label = new Label(composite, SWT.CENTER);
	label.setText("Start of");
	label = new Label(composite, SWT.CENTER);
	label.setText("End of");
	
	startYearControl = new DateFilterYearControl(composite, years);
	endYearControl = new DateFilterYearControl(composite, null);
	startYearControl.addSelectionListener(new SelectionListener(){
		
		private void doSelection(){
			int setYear = startYearControl.getYear();
			endYearControl.setYears(years);
			endYearControl.setBottomYear(setYear);
			SortedMap theMonths = null;
			if(setYear != 0)theMonths = (SortedMap)years.get(new Integer(setYear));
			startMonthControl.setMonths(theMonths);
			endMonthControl.setMonths(theMonths); // is this needed? j.b.
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			doSelection();
		}

		public void widgetSelected(SelectionEvent e) {
			doSelection();
		}
		
	});
	
	endYearControl.addSelectionListener(new SelectionListener(){
		private void doSelection(){
			int setYear = endYearControl.getYear();
			SortedMap theMonths = null;
			if(setYear != 0)theMonths = (SortedMap)years.get(new Integer(setYear));
			endMonthControl.setMonths(theMonths);
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			doSelection();
		}

		public void widgetSelected(SelectionEvent e) {
			doSelection();
		}
		
	});
	startMonthControl = new DateFilterMonthControl(composite, null);
	
	startMonthControl.addSelectionListener(new SelectionListener(){
		
		private void doSelection(){
			if(startYearControl.getYear() == endYearControl.getYear())
				endMonthControl.setBottomMonth(startMonthControl.getMonth());
			int setMonth = startMonthControl.getMonth();
			SortedMap theDays = null;
			if(setMonth != 0)
				theDays = (SortedMap)startMonthControl.getMonths().get(new Integer(setMonth));
			startDayControl.setDays(theDays);
			endDayControl.setDays(theDays);
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			doSelection();
		}

		public void widgetSelected(SelectionEvent e) {
			doSelection();
		}
		
	});
	
	endMonthControl = new DateFilterMonthControl(composite, null);
	
	endMonthControl.addSelectionListener(new SelectionListener(){
		private void doSelection(){
			int setMonth = endMonthControl.getMonth();
			SortedMap theDays = null;
			if(setMonth != 0)
				theDays = (SortedMap)endMonthControl.getMonths().get(new Integer(setMonth));
			endDayControl.setDays(theDays);
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			doSelection();
		}

		public void widgetSelected(SelectionEvent e) {
			doSelection();
		}
		
	});
	
	startDayControl = new DateFilterDayControl(composite,null);
	
	startDayControl.addSelectionListener(new SelectionListener(){
		private void doSelection(){
			if(startYearControl.getYear() == endYearControl.getYear() &&
					startMonthControl.getMonth() == endMonthControl.getMonth())
				endDayControl.setBottomDay(startDayControl.getDay());
			
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			doSelection();
		}

		public void widgetSelected(SelectionEvent e) {
			doSelection();
		}
		
	});
	
	endDayControl = new DateFilterDayControl(composite,null);
	return composite;
}

public Date getStartDate(){
	if(startYearControl == null)return null;
	int year = startYearControl.getYear();
	if(year == 0)return null;
	int mon = startMonthControl.getMonth();
	if(mon > 0)mon--;
	int day = startDayControl.getDay();
	if(day == 0)day = 1;
	myCalendar.clear();
	myCalendar.set(year, mon,day);
	Date rslt = new Date(myCalendar.getTimeInMillis());
	// SimpleDateFormat formatter = new SimpleDateFormat();
	// System.out.println("getStartDate: "+formatter.format(rslt));
	return rslt;
}

public Date getEndDate(){
	if(endYearControl == null)return null;
	int year = endYearControl.getYear();
	if(year == 0)return null;
	int mon = endMonthControl.getMonth(); // mon is one based, Calendar is zero based  j.b.
	int day = 1;
	if(mon == 0){
		year++;
		mon = 0;
	} else {
	   day = startDayControl.getDay();
	   if(day == 0){
		   day = 1;
		   if(mon == 12){
			   // if month is december, next month is Jan of next year j.b.
			   mon = 0;
			   year++;
		   }
	   } else {
		   mon--;
		   day++;
	   }
	}
	myCalendar.clear();
	myCalendar.set(year, mon, day);
	Date rslt = new Date(myCalendar.getTimeInMillis());
	// SimpleDateFormat formatter = new SimpleDateFormat();
	// System.out.println("getEndDate: "+formatter.format(rslt));
	return rslt;
}
}
