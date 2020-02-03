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

package uk.ac.kcl.cch.jb.pliny.actions;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LOTypeQuery;

/**
 * This action supports the opening of Pliny's
 * {@link uk.ac.kcl.cch.jb.pliny.views.TypeManagerView}.
 * 
 * 
 * @author John Bradley
 *
 */

public class OpenTypeManagerAction extends Action implements
//		IWorkbenchWindowActionDelegate {
	IWorkbenchWindowPulldownDelegate {

	private static final String TYPE_MANAGER_VIEW_ID =
		"uk.ac.kcl.cch.jb.pliny.typeManager";
	private IWorkbenchWindow window;
	
	private class MySelectionListener extends SelectionAdapter{
		public void widgetSelected(SelectionEvent e){
			LOType myType = (LOType)e.widget.getData();
			LOType.setCurrentType(myType);
		}
	}
	
	private MySelectionListener mySelectionListener = new MySelectionListener();

	public OpenTypeManagerAction() {
		super();
		this.setText("Open Note Searching View");
		this.setImageDescriptor(
				ImageDescriptor.createFromImage(PlinyPlugin.getDefault().getImage("icons/typeIcon.gif")));
	}

	public void dispose() {
		// nothing to dispose here
	}

	public void run() {
		if(window == null)window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		if(page == null)return;
		
		// open and activite the Note Searching view.
		try {
			page.showView(TYPE_MANAGER_VIEW_ID);
		}
		catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run(IAction action) {
		run();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// nothing to do here
	}

	public Menu getMenu(Control parent) {
		Menu menu = new Menu(parent);
		LOTypeQuery q = new LOTypeQuery();
		q.addOrder("name");
		// q.setOrderString("name");
		Vector list = q.executeQuery();
		Iterator it = list.iterator();
		while(it.hasNext()){
			LOType type = (LOType)it.next();
			handleType(menu, type);
		}
		return menu;
	}

	private void handleType(Menu menu, LOType type) {
		MenuItem item = new MenuItem(menu, SWT.RADIO);
		String name = type.getName();
		if(name == null || name.length()==0)name = "(default)";
		item.setText(name);
		item.setImage(type.getColourIcon());
		item.setSelection(type==LOType.getCurrentType());
		item.setData(type);
		item.addSelectionListener(mySelectionListener);
	}

}
