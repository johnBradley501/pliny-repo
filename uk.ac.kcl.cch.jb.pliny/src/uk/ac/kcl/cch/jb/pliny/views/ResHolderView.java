/*******************************************************************************
 * Copyright (c) 2009 John Bradley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Bradley - initial API and implementation
 *******************************************************************************/
package uk.ac.kcl.cch.jb.pliny.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.actions.ResourceExplorerOpenAction;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.views.utils.ResHolderContentProvider;
import uk.ac.kcl.cch.jb.pliny.views.utils.ResHolderLabelProvider;
/**
 * the Eclipse ViewPart for the Resource Holder.
 * 
 * @author John Bradley
 *
 */

public class ResHolderView extends ViewPart implements PropertyChangeListener {
	
	public static final String MY_ID="uk.ac.kcl.cch.jb.pliny.resHolderView";
	
	private TableViewer viewer;
	private ResHolderContentProvider dataHolder;
	private Action openResourceAction;
	
	private class OpenResourceAction extends ResourceExplorerOpenAction{
		public OpenResourceAction(){
			super(null);
		}
		
		public void run(){
			TableItem[] selection = viewer.getTable().getSelection();
			if(selection.length == 0)return;
			Resource r = (Resource)selection[0].getData();
			IWorkbench wb = PlatformUI.getWorkbench();
			IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
			openResource(window, r, 0 );

		}
	}

	public ResHolderView() {
		super();
	}
	
	public void dispose(){
		if(dataHolder != null)dataHolder.dispose();
		super.dispose();
	}
	
	@Override
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, 
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		dataHolder = new ResHolderContentProvider(viewer);
		viewer.setContentProvider(dataHolder);
		viewer.setLabelProvider(new ResHolderLabelProvider());
        Table table = viewer.getTable();
        //TableColumn tblc = new TableColumn(table, SWT.LEFT);
        //tblc.setWidth(50);
        //tblc.setText("Resource");
        table.setLinesVisible(true);
        
        viewer.setInput(this);
        makeActions();
        hookDoubleClickAction();
	}

	private void makeActions() {
		openResourceAction = new OpenResourceAction();		
	}
	
	private void hookDoubleClickAction(){
		viewer.addDoubleClickListener(new IDoubleClickListener(){
			public void doubleClick(DoubleClickEvent event) {
				openResourceAction.run();
			}
		});
	}

	@Override
	public void setFocus() {
		if(viewer != null)viewer.getControl().setFocus();
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		String pString = arg0.getPropertyName();
		if(pString == Resource.FULLNAME_PROP){
			viewer.refresh(arg0.getNewValue());
		}
		else if(pString.equals("Delete-Resource")){
			dataHolder.remove((Resource)arg0.getOldValue());
		}
	}

}
