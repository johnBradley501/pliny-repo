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

package uk.ac.kcl.cch.jb.pliny.views.utils;

import java.util.Iterator;
import java.util.SortedMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

public class DateFilterMonthControl {

	private static String[] monthNames =
	{"", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", 
		"Oct", "Nov", "Dec"};
	
	SortedMap months;
	int[]theMonths = null;
	int bottomMonth = 0;
	Combo myCombo = null;

	public DateFilterMonthControl(Composite composite, SortedMap months){
		this.months = months;
		myCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		doSetup();
	}
	
	private void doSetup(){
		myCombo.add("");
		if(months != null){
			Iterator it = months.keySet().iterator();
			int size = months.size();
			theMonths = new int[size+1];
			theMonths[0] = 0;
			int i = 1;
			while(it.hasNext()){
				Integer thisMonth = (Integer)it.next();
				theMonths[i++] = thisMonth.intValue();
				myCombo.add(monthNames[thisMonth.intValue()]);
			}
		} else {
			theMonths = new int[1];
			theMonths[0] = 0;
		}
		myCombo.select(0);
	}
	
	public void dispose(){
		myCombo.dispose();
	}
	
	public void setMonths(SortedMap months){
		this.months = months;
		bottomMonth = 0;
		myCombo.removeAll();
		doSetup();
		myCombo.notifyListeners(SWT.Selection, null);
	}
	
	public SortedMap getMonths(){
		return months;
	}

	public int getMonth(){
		return theMonths[myCombo.getSelectionIndex()+bottomMonth];
	}
	
	public void addSelectionListener(SelectionListener listener){
		myCombo.addSelectionListener(listener);
	}

	public void setBottomMonth(int month){
		int i = 0;
		while(theMonths[i] < month)i++;
		if(i >= theMonths.length) return;
		bottomMonth = i;
		myCombo.removeAll();
		for(i = bottomMonth; i < theMonths.length;i++){
			String monthDisplay = "";
			if(theMonths[i] > 0)monthDisplay = monthNames[theMonths[i]];
			myCombo.add(monthDisplay);
		}
		myCombo.select(0);
		myCombo.notifyListeners(SWT.Selection, null);
	}

}
