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

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.ResourceQuery;
import uk.ac.kcl.cch.rdb2java.dynData.BaseQuery;

/**
 * provides a SWT Composite containing the set of UI elements that allow
 * the user to specify the Target and Source Role for
 * {@link uk.ac.kcl.cch.jb.pliny.model.LOType LOType} items 
 * on the 2nd page of the {@link EditTypeWizard}.
 * 
 * @author John Bradley
 *
 */
public class TypeRoleUIManager {
	
	private Composite container;
	private int roleKey;
	private Resource selectedResource = null;
	private String typeName;
	private Text roleNameField;
	private Button makeNew;
	private TableViewer resourceListTable;
	private ResourceListProvider listProvider;
	private boolean selectedAction = false;
	
	public TypeRoleUIManager(Composite parent, int roleKey, String typeName){
		container = new Composite(parent, SWT.NULL);
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 3;
        container.setLayoutData(gridData);
		this.roleKey = roleKey;
		this.typeName = typeName;
        
        String name = null;
        if(roleKey != 0){
        	selectedResource = Resource.getItem(roleKey);
        	if(selectedResource == null)roleKey = 0;
        	else name = selectedResource.getName();
        }
        listProvider = new ResourceListProvider(name);
		
	    final GridLayout gridLayout = new GridLayout();
	    gridLayout.numColumns = 3;
	    container.setLayout(gridLayout);
		
	    Button useDefault = new Button(container, SWT.RADIO);
	    useDefault.setLayoutData(gridData);
	    useDefault.setText(
	    		"Invent Role Name of '~"+typeName+"'");
	    useDefault.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				roleNameField.setEnabled(false);
				makeNew.setEnabled(false);
				Table theTable = resourceListTable.getTable();
				if(theTable != null)theTable.setEnabled(false);
				TypeRoleUIManager.this.roleKey = 0;
			}
	    	
	    });
	    Button chooseName = new Button(container, SWT.RADIO);
	    chooseName.setLayoutData(gridData);
	    chooseName.setText(
	            "Use chosen resource for the "+typeName+" role:");
	    chooseName.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				roleNameField.setEnabled(true);
				makeNew.setEnabled(true);
				roleNameField.setFocus();
				Table theTable = resourceListTable.getTable();
				if(theTable != null)theTable.setEnabled(true);
				if(selectedResource != null)TypeRoleUIManager.this.roleKey = selectedResource.getALID();
				else TypeRoleUIManager.this.roleKey = 0;
			}
	    	
	    });
	    
	    if(roleKey == 0)useDefault.setSelection(true);
	    else chooseName.setSelection(true);
	    
	    roleNameField = new Text(container, SWT.BORDER);
        if(name != null)roleNameField.setText(name);
        else roleNameField.setEnabled(false);

        roleNameField.addModifyListener(new ModifyListener (){

			public void modifyText(ModifyEvent e) {
				Text text = (Text) e.widget;
				String string = text.getText();
				if(string.length() == 0)string = null;
				listProvider.setString(string);
				resourceListTable.refresh();
				TypeRoleUIManager.this.makeNew.setSelection(
						!TypeRoleUIManager.this.selectedAction);
				TypeRoleUIManager.this.selectedAction = false;
			}
	    	
	    });
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        roleNameField.setLayoutData(gridData);
        
        Label makeNewLabel = new Label(container, SWT.NONE);
        makeNewLabel.setText(" New? ");
        
        makeNew = new Button(container, SWT.CHECK);
        makeNew.setSelection(roleKey == 0);
        if(roleKey == 0)makeNew.setEnabled(false);

	    
        //ScrolledComposite sc = new ScrolledComposite(container, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        gridData = new GridData();
        gridData.heightHint = 100;
        gridData.horizontalSpan = 3;
        gridData.grabExcessHorizontalSpace = true;
        //sc.setLayoutData(gridData);
        //sc.setAlwaysShowScrollBars(false);
        //sc.setExpandHorizontal(true);
        //sc.setExpandVertical(true);
        //sc.setMinHeight(150);
        //Composite tableComposite = new Composite(sc, SWT.NONE);
        //tableComposite.setLayout(new FillLayout());
        //tableComposite.setLayoutData(gridData);
        
        resourceListTable = new TableViewer(container, SWT.SINGLE | SWT.BORDER);
        Table table = resourceListTable.getTable();
        table.setLayoutData(gridData);
        table.setEnabled(roleKey != 0);
        //table.setSize(250,100);
        new TableColumn(table, SWT.LEFT).setWidth(350);
        //sc.setContent(tableComposite);
        //sc.setContent(table);
        resourceListTable.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection)event.getSelection();
				if(selection == null)return;
				//System.out.println("Selection: "+selection);
				Resource selectedResource = (Resource)selection.getFirstElement();
				if(selectedResource == null)return;
				roleNameField.setText(selectedResource.getName());
				TypeRoleUIManager.this.roleKey = selectedResource.getALID();
				TypeRoleUIManager.this.selectedResource = selectedResource;
				TypeRoleUIManager.this.selectedAction = true;
				TypeRoleUIManager.this.makeNew.setSelection(false);
			}
        	
        });
        
        resourceListTable.setContentProvider(new ResourceListContentProvider());
        resourceListTable.setLabelProvider(new ResourceListLabelProvider());
        resourceListTable.setInput(listProvider);
	}
	
	private class ResourceListProvider {
		private String string;
		private List theList = null;
		
		public ResourceListProvider(String string){
			setString(string);
		}
		
		public void setString(String string){
			if(string == null)this.string = null;
			else this.string = string.trim();
			theList = null;
		}
		
		public List getList(){
			if(theList != null)return theList;
			if(string == null || string.length() == 0)return Collections.EMPTY_LIST;
			ResourceQuery q = new ResourceQuery();
			//q.setWhereString("LOWER(Resource.fullName) like '"+string.toLowerCase()+"%'");
			q.addConstraint("fullName", BaseQuery.FilterSTARTS, string);
			q.addOrder("fullName");
			//q.setOrderString("fullName");
			theList = q.executeQuery();
			return theList;
		}
	}
	
	private class ResourceListContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			if(!(inputElement instanceof ResourceListProvider))return new Object[0];
			return ((ResourceListProvider)inputElement).getList().toArray();
		}

		public void dispose() {
			// nothing to do
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// nothing to do
		}
	}
	
	private class ResourceListLabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			if(columnIndex == 0 && element instanceof Resource){
				if(((Resource)element).getObjectType() == null)return null; // shouldn't happen.  JB
				return ((Resource)element).getObjectType().getIconImage();
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if(columnIndex == 0 && element instanceof Resource){
				return ((Resource)element).getName();
			}
			return null;
		}

		public void addListener(ILabelProviderListener listener) {
			// nothing to do
			
		}

		public void dispose() {
			// nothing to do
			
		}

		public boolean isLabelProperty(Object element, String property) {
			// nothing to do
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
			// nothing to do
		}
	}
	
	public int getRoleKey(){
		if(makeNew.getSelection())return 0;
		return roleKey;
	}
	
	public String getName(){
		String rslt = null;
		if(makeNew.getEnabled() && makeNew.getSelection())
			rslt = roleNameField.getText();
		return rslt;
	}
	
	public void setRoleKey(int key){
		roleKey = key;
	}

}
