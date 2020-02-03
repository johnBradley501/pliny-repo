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

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.actions.ResourceExplorerCopyAction;
import uk.ac.kcl.cch.jb.pliny.actions.ResourceExplorerDeleteAction;
import uk.ac.kcl.cch.jb.pliny.actions.ResourceExplorerOpenAction;
import uk.ac.kcl.cch.jb.pliny.model.Favourite;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.views.utils.DatedResourceRoot;
import uk.ac.kcl.cch.jb.pliny.views.utils.IResourceExplorerItem;
import uk.ac.kcl.cch.jb.pliny.views.utils.IResourceTreeDisplayer;
import uk.ac.kcl.cch.jb.pliny.views.utils.ResourceExplorerCellModifier;
import uk.ac.kcl.cch.jb.pliny.views.utils.ResourceExplorerContentProvider;
import uk.ac.kcl.cch.jb.pliny.views.utils.ResourceExplorerDisplayedInItem;
import uk.ac.kcl.cch.jb.pliny.views.utils.ResourceExplorerLabelProvider;
import uk.ac.kcl.cch.jb.pliny.views.utils.ResourceExplorerRoot;
import uk.ac.kcl.cch.jb.pliny.views.utils.ResourceExplorerTextCellEditor;
import uk.ac.kcl.cch.rdb2java.dynData.BaseObject;

/**
 * the abstract class that supports the different tabbed viewers of Resources in the
 * {@link ResourceExplorerView}.
 * <p>see the classes that extend on this for the three tabs in {@link RETypeTab}, {@link REDateTab}
 * and {@link RENameSearchTab}.
 * 
 * @author John Bradley
 *
 */
public abstract class REBaseTab implements IResourceTreeDisplayer {

	private ResourceExplorerView view;
	protected TreeViewer viewer;
	private IStructuredContentProvider myContentProvider;
	private ILabelProvider myLabelProvider;
	private Action deleteAction;
	private IResourceExplorerItem displayRoot;
	
	public REBaseTab(ResourceExplorerView view){
		this.view = view;
	}
	
	public abstract String getTabName();
	public abstract String getToolTipText();
	protected abstract IResourceExplorerItem makeDisplayRoot();
	
	public CommandStack getCommandStack() {
		return view.getCommandStack();
	}

	public TreeViewer getMyViewer() {
		return viewer;
	}
	
	protected void buildContent(Composite composite){
		Layout testL = composite.getLayout();
		viewer = new TreeViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		myContentProvider = new ResourceExplorerContentProvider(this);
		viewer.setContentProvider(myContentProvider);
		myLabelProvider = new ResourceExplorerLabelProvider();
		viewer.setLabelProvider(myLabelProvider);
		//viewer.setSorter(new NameSorter());
		viewer.setInput(getDisplayRoot());
		//viewer.refresh();
		viewer.setColumnProperties(new String[]{ResourceExplorerView.NAME_ID});
	}

	public void createTabItem(TabFolder tabFolder) {
		TabItem item = new TabItem(tabFolder, SWT.NONE);
		item.setText(getTabName());
		item.setToolTipText(getToolTipText());
		Composite composite = new Composite(tabFolder, SWT.NONE);
		//composite.setLayout(new FillLayout());
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		item.setControl(composite);
		
		buildContent(composite);
		
		hookDragAndDrop();
	}
	
	public void setTextCellEditor(Action deleteAction){
		this.deleteAction = deleteAction;
		Tree tree = viewer.getTree();
		final TextCellEditor nameEditor =
			new ResourceExplorerTextCellEditor(tree, deleteAction);
		
		viewer.setCellEditors(new CellEditor[]{nameEditor});
		viewer.setCellModifier(new ResourceExplorerCellModifier(viewer, this));
		
		hookKeyboardActions();
		hookContextMenu();
	}
	
	public void hookDoubleClickAction(final Action openAction) {
		
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				openAction.run();
			}
		});
	}
	
	public void dispose(){
		if(displayRoot != null)displayRoot.dispose();
	}

	public IResourceExplorerItem getDisplayRoot() {
		if(displayRoot == null)displayRoot = makeDisplayRoot();
		return displayRoot;
	}
	
	public Vector getSelectedBaseObjects(){
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		Vector rslts = new Vector();
		Iterator iter = selection.iterator();
		while(iter.hasNext()){
			Object item = iter.next();
			if(item instanceof IResourceExplorerItem)
				item = ((IResourceExplorerItem)item).getAssociatedObject();
			if((item != null) && (item instanceof BaseObject))rslts.add(item);
		}
		return rslts;
	}

	public Vector getSelectedObjectsToOpen() {
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		Vector rslts = new Vector();
		Iterator iter = selection.iterator();
		while(iter.hasNext()){
			Object item = iter.next();
			if(item instanceof IResourceExplorerItem){
				IResourceExplorerItem reItem = (IResourceExplorerItem)item;
				if(reItem instanceof ResourceExplorerDisplayedInItem){
					LinkableObject lo = (LinkableObject)reItem.getAssociatedObject();
					rslts.add(lo.getDisplayedIn());
				} else rslts.add(reItem.getAssociatedObject());
			}
		}
		return rslts;
	}
	
	public Vector getSelectedResourceExplorerItems() {
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		Vector rslts = new Vector();
		Iterator iter = selection.iterator();
		while(iter.hasNext()){
			Object item = iter.next();
			if(item instanceof IResourceExplorerItem)
				rslts.add(item);
		}
		return rslts;
	}

	public IWorkbenchPartSite getSite() {
		return view.getSite();
	}

	public IViewSite getViewSite() {
		return view.getViewSite();
	}
	
	public Control getFocusControl(){
		return getMyViewer().getControl();
	}
	
	private void hookKeyboardActions() {
		viewer.getControl().addKeyListener(new KeyAdapter(){
			public void keyReleased(KeyEvent event){
				handleKeyReleased(event);
			}
		});
	}
	
	protected void handleKeyReleased(KeyEvent event){
		if(event.character == SWT.DEL && event.stateMask == 0){
			deleteAction.run();
		}
	}
	
	// code to handle the contextual menu.

	
	protected void fillContextMenu(IMenuManager manager){
		view.fillContextMenu(manager);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				REBaseTab.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}
	
	protected abstract void hookDragAndDrop();
	
	public void setState(IMemento memento){
		// do nothing by default.   j.b.
	}
	
	public void saveState(IMemento memento){
		// do nothing by default.   j.b.
	}

}
