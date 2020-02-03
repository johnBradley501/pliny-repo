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

public class DateFilterDayControl {
	SortedMap myDays;
	int[]theDays = null;
	int bottomDay = 0;
	Combo myCombo = null;
	
	public DateFilterDayControl(Composite composite, SortedMap days){
		myDays = days;
		myCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		doSetup();
	}

	private void doSetup() {
		myCombo.add("");
		if(myDays != null){
			Iterator it = myDays.keySet().iterator();
			int size = myDays.size();
			theDays = new int[size+1];
			theDays[0] = 0;
			int i = 1;
			while(it.hasNext()){
				Integer thisDay = (Integer)it.next();
				theDays[i++] = thisDay.intValue();
				myCombo.add(thisDay.toString());
			}
			myCombo.select(0);
		}
		myCombo.select(0);
	}
	
	public void dispose(){
		myCombo.dispose();
	}
	
	public void setDays(SortedMap days){
		this.myDays = days;
		bottomDay = 0;
		myCombo.removeAll();
		doSetup();
		myCombo.notifyListeners(SWT.Selection, null);
	}


	public int getDay(){
		if(theDays == null)return 0;
		return theDays[myCombo.getSelectionIndex()+bottomDay];
	}
	
	public void addSelectionListener(SelectionListener listener){
		myCombo.addSelectionListener(listener);
	}
	
	
	public void setBottomDay(int day){
		int i = 0;
		if(theDays == null){
			bottomDay = 0;
			return;
		}
		while(theDays[i] < day)i++;
		if(i >= theDays.length) return;
		bottomDay = i;
		myCombo.removeAll();
		for(i = bottomDay; i < theDays.length;i++){
			String dayDisplay = "";
			if(theDays[i] > 0)dayDisplay = theDays[i]+"";
			myCombo.add(dayDisplay);
		}
		myCombo.select(0);
		myCombo.notifyListeners(SWT.Selection, null);
	}
}
