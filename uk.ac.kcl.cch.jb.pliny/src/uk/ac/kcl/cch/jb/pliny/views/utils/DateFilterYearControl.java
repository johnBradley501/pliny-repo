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

public class DateFilterYearControl {
	
	SortedMap myYears;
	int[]theYears = null;
	int bottomYear = 0;
	Combo myCombo = null;
	
	public DateFilterYearControl(Composite composite, SortedMap years){
		myYears = years;
		myCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		doSetup();
	}
	
	private void doSetup(){
		myCombo.add("");
		if(myYears != null){
			Iterator it = myYears.keySet().iterator();
			int size = myYears.size();
			theYears = new int[size+1];
			theYears[0] = 0;
			int i = 1;
			while(it.hasNext()){
				Integer thisYear = (Integer)it.next();
				theYears[i++] = thisYear.intValue();
				myCombo.add(thisYear.toString());
			}
		}
		myCombo.select(0);
	}
	
	public void dispose(){
		myCombo.dispose();
	}
	
	public int getYear(){
		if(theYears == null)return 0;
		return theYears[myCombo.getSelectionIndex()+bottomYear];
	}
	
	public void setYears(SortedMap years){
		myYears = years;
		bottomYear = 0;
		myCombo.removeAll();
		doSetup();
	}
	
	public void addSelectionListener(SelectionListener listener){
		myCombo.addSelectionListener(listener);
	}
	
	public void setBottomYear(int year){
		int i = 0;
		while(theYears[i] < year)i++;
		if(i >= theYears.length) return;
		bottomYear = i;
		myCombo.removeAll();
		for(i = bottomYear; i < theYears.length;i++){
			String yearDisplay = "";
			if(theYears[i] > 0)yearDisplay = theYears[i]+"";
			myCombo.add(yearDisplay);
		}
		myCombo.select(0);
		myCombo.notifyListeners(SWT.Selection, null);
	}

}
