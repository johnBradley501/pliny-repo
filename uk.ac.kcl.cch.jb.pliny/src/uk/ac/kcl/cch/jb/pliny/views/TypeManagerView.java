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

package uk.ac.kcl.cch.jb.pliny.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.actions.CommandStackRedoAction;
import uk.ac.kcl.cch.jb.pliny.actions.CommandStackUndoAction;
import uk.ac.kcl.cch.jb.pliny.actions.DeleteTypeAction;
import uk.ac.kcl.cch.jb.pliny.dnd.TypeManagerDragListener;
import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.views.utils.EditTypeWizard;
import uk.ac.kcl.cch.jb.pliny.views.utils.TypeManagerContentProvider;
import uk.ac.kcl.cch.jb.pliny.views.utils.TypeManagerLabelProvider;
import uk.ac.kcl.cch.rdb2java.Rdb2javaPlugin;

/**
 * the Eclipse ViewPart for the Type Manager.
 * 
 * @author John Bradley
 *
 */

public class TypeManagerView extends ViewPart implements PropertyChangeListener {
	private CommandStack commandStack;
	private TableViewer viewer;

	private CommandStackUndoAction undoAction;
	private CommandStackRedoAction redoAction;
	
	private Action setCurrentAction = null;
	private Action editTypeAction = null;
	private Action newTypeAction = null;
	private Action deleteTypeAction = null;

	public TypeManagerView() {
		super();
		commandStack = new CommandStack();
		Rdb2javaPlugin.getDataServer().addPropertyChangeListener(this);
        LOType.getCurrentTypeManager().addPropertyChangeListener(this);
	}
	
	public CommandStack getCommandStack(){
		return commandStack;
	}
	
	public void dispose(){
		Rdb2javaPlugin.getDataServer().removePropertyChangeListener(this);
        LOType.getCurrentTypeManager().removePropertyChangeListener(this);
        super.dispose();
	}

	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, 
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.setContentProvider(new TypeManagerContentProvider(viewer));
        viewer.setLabelProvider(new TypeManagerLabelProvider());
        
        Table table = viewer.getTable();
        buildColumn(table, "Curr", 20, SWT.LEFT);
        buildColumn(table, "Name", 200, SWT.LEFT);
        table.setLinesVisible(true);

        viewer.setInput(this);
        
        makeActions();
        contributeToActionBars();
        
        hookDoubleClickAction();
        hookContextMenu();
        new TypeManagerDragListener(viewer);
        
	}
	
    private void buildColumn(Table table, String name, int width, int orientation){
    	TableColumn tblc = new TableColumn(table, orientation);
		tblc.setWidth(width);
		tblc.setText(name);
    }
    
    private void makeActions(){
		undoAction = new CommandStackUndoAction(getCommandStack());
		redoAction = new CommandStackRedoAction(getCommandStack());
		
		setCurrentAction = new Action(){
			public void run(){
				LOType newType = getSelectedType();
				if(newType == null)return;
				LOType.setCurrentType(newType);
				viewer.refresh();
			}
		};
		setCurrentAction.setText("Make Current");
		setCurrentAction.setToolTipText("Make Current Type");
		setCurrentAction.setImageDescriptor(PlinyPlugin.getImageDescriptor("icons/typeIcon.gif"));
		
		editTypeAction = new Action() {
			public void run(){
				EditTypeWizard wizard = new EditTypeWizard(TypeManagerView.this);
				WizardDialog dialog = 
					new WizardDialog(TypeManagerView.this.getSite().getShell(), wizard);
				dialog.open();
			}
		};
		editTypeAction.setText("Edit Type");
		editTypeAction.setToolTipText("Edit selected Type");
		editTypeAction.setImageDescriptor(PlinyPlugin.getImageDescriptor("icons/typeIcon.gif"));
		
		newTypeAction = new Action() {
			public void run(){
				EditTypeWizard wizard = new EditTypeWizard(TypeManagerView.this, null);
				WizardDialog dialog = 
					new WizardDialog(TypeManagerView.this.getSite().getShell(), wizard);
				dialog.open();
			}
		};
		newTypeAction.setText("Create a new type");
		newTypeAction.setToolTipText("Create a new Type");
		newTypeAction.setImageDescriptor(PlinyPlugin.getImageDescriptor("icons/typeIcon.gif"));

		deleteTypeAction = new DeleteTypeAction(this);
    }

	
	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				setCurrentAction.run();
			}
		});
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				TypeManagerView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	protected void fillContextMenu(IMenuManager manager){
		LOType current = getSelectedType();
		if(current != null){
			String name = "(default)";
			if(current != LOType.getDefaultType())name = current.getName();
			setCurrentAction.setText("Make '"+name+"' Current");
			manager.add(setCurrentAction);
			editTypeAction.setText("Edit attributes of '"+name+"'");
			manager.add(editTypeAction);
			if(current.getALID() > LOType.MAX_UNDELETABLE_TYPES){
			   deleteTypeAction.setText("Delete '"+name+"'");
			   manager.add(deleteTypeAction);
			}
		}
		manager.add(newTypeAction);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		contributeToLocalToolBar(bars.getToolBarManager());
		bars.setGlobalActionHandler(ActionFactory.DELETE.getId(), deleteTypeAction);
		bars.setGlobalActionHandler(ActionFactory.UNDO.getId(),undoAction);
		bars.setGlobalActionHandler(ActionFactory.REDO.getId(),redoAction);
		//bars.setGlobalActionHandler(ActionFactory.COPY.getId(),copyAction);
	}

	private void contributeToLocalToolBar(IToolBarManager manager) {
		manager.add(newTypeAction);
		
	}

	public void setFocus() {
		if(viewer != null)viewer.getControl().setFocus();
	}
	
	public LOType getSelectedType(){
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		if(selection.size() != 1)return null;
		return (LOType)selection.getFirstElement();
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		String propertyName = arg0.getPropertyName();
		if(propertyName == LOType.NEW_CURRENT_EVENT){
			if(viewer != null)viewer.refresh();
		}
		else if(propertyName.endsWith("LOType")){
			viewer.refresh();
		}
	}

}
