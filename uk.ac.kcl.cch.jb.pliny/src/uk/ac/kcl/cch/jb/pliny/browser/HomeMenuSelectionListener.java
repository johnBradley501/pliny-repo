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

package uk.ac.kcl.cch.jb.pliny.browser;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;

/**
 * a SelectionAdapter to manage the selection of commands associated with the
 * Browser 'Home' button.
 * 
 * @author John Bradley
 *
 */

public class HomeMenuSelectionListener extends SelectionAdapter {
   private ToolItem dropdown;
   private Menu menu;
   private BrowserViewer bv;
   
   public HomeMenuSelectionListener(ToolItem dropdown, BrowserViewer bv){
	   this.dropdown = dropdown;
	   this.bv = bv;
	   menu = new Menu(dropdown.getParent().getShell(), SWT.POP_UP);
	   addMenuItems();
   }
   
   public void widgetSelected(SelectionEvent event){
	   if(event.detail == SWT.ARROW) {
		   ToolItem item = (ToolItem) event.widget;
		   Rectangle rect = item.getBounds();
		   Point pt = item.getParent().toDisplay(new Point(rect.x, rect.y));
		   menu.setLocation(pt.x, pt.y+rect.height);
		   menu.setVisible(true);
	   } else {
		   bv.home();
	   }
   }
   
   private void addMenuItems(){
	   MenuItem item1 = new MenuItem(menu, SWT.NONE);
	   item1.setText("Go Home");
	   item1.addSelectionListener(new SelectionAdapter() {
		   public void widgetSelected(SelectionEvent event) {
			   bv.home();
		   }
	   });
	   
	   MenuItem item2 = new MenuItem(menu, SWT.NONE);
	   item2.setText("Make current Home");
	   item2.addSelectionListener(new SelectionAdapter() {
		   public void widgetSelected(SelectionEvent event) {
			   bv.setHome(bv.getURL());
		   }
	   });
   }
}
