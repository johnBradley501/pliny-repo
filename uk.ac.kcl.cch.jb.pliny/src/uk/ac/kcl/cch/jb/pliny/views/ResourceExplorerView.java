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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.actions.AddToFavouritesAction;
import uk.ac.kcl.cch.jb.pliny.actions.ClearReferentAction;
import uk.ac.kcl.cch.jb.pliny.actions.CommandStackRedoAction;
import uk.ac.kcl.cch.jb.pliny.actions.CommandStackUndoAction;
import uk.ac.kcl.cch.jb.pliny.actions.CreateMinimiseStatus;
import uk.ac.kcl.cch.jb.pliny.actions.OpenResHolderViewAction;
import uk.ac.kcl.cch.jb.pliny.actions.RemoveFromFavouritesAction;
import uk.ac.kcl.cch.jb.pliny.actions.ResourceExplorerCopyAction;
import uk.ac.kcl.cch.jb.pliny.actions.ResourceExplorerDeleteAction;
import uk.ac.kcl.cch.jb.pliny.actions.PlinyExportAction;
import uk.ac.kcl.cch.jb.pliny.actions.ResourceExplorerImportAction;
import uk.ac.kcl.cch.jb.pliny.actions.ResourceExplorerOpenAction;
import uk.ac.kcl.cch.jb.pliny.actions.SetReferentAction;
import uk.ac.kcl.cch.jb.pliny.actions.UpdateUrlAction;
import uk.ac.kcl.cch.jb.pliny.dnd.IResourceExtensionProcessor;
import uk.ac.kcl.cch.jb.pliny.dnd.IResourceExtensionProcessorSource;
import uk.ac.kcl.cch.jb.pliny.dnd.ResourceExplorerDragListener;
import uk.ac.kcl.cch.jb.pliny.model.Favourite;
import uk.ac.kcl.cch.jb.pliny.model.IHasResource;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.views.utils.IResourceTreeDisplayer;
import uk.ac.kcl.cch.jb.pliny.views.utils.NewNoteWizard;
import uk.ac.kcl.cch.jb.pliny.views.utils.ResourceExplorerDropTarget;
import uk.ac.kcl.cch.rdb2java.dynData.BaseObject;

/**
 * the Eclipse ViewPart for the Resource Explorer.
 * <p>
 * The Resource Explorer displays as a tabbed item -- each tab represents a different
 * way to search the user's collection of Resources, and the central control of
 * each tab is a JFace TreeViewer.
 * <p>The tabs are managed by a set of classes that extend the base class
 * {@link REBaseTab}.  Look in method <tt>init</tt> to see them being set up.
 * <p>
 * Note that although this code is not a GEF application, it uses
 * GEF's command stack to manage actions, and make them undoable.
 * 
 * @author John Bradley
 *
 */
public class ResourceExplorerView extends ViewPart
implements IResourceTreeDisplayer/*, ISelectionProvider*/{
	
	private TabFolder tabFolder;
	private int selectedTabIndex = 0;
	private RETypeTab reTypeTab = null;
	private REDateTab reDateTab = null;
	private RENameSearchTab reSearchTab = null;
	//private Composite myComposite;
	
	
	
	//private TreeViewer viewer;
	//private Tree tree;

	//private IStructuredContentProvider myContentProvider;
	//private ILabelProvider myLabelProvider;
	//private ResourceExplorerRoot displayRoot;
	private CommandStack commandStack;
	public static final String NAME_ID = "Name";
	private static final String MEMENTO_OPEN_TAB = "OpenTab";

	private CommandStackUndoAction undoAction;
	private CommandStackRedoAction redoAction;
	private Action deleteAction;
	private Action copyAction;
	private Action openAction;
	private Action makeNoteAction;
	private Action exportAction;
	private Action importAction;
	private AddToFavouritesAction addToFavouritesAction;
	private RemoveFromFavouritesAction removeFromFavouritesAction;
	private Action setReferentAction;
	private Action clearReferentAction;
	private UpdateUrlAction updateUrlAction;
	//private Action openNoteSearchAction;
	
	private REBaseTab[] tabs;

	private class TabSelectionListener extends SelectionAdapter{
		public void widgetSelected(SelectionEvent e){
			selectedTabIndex = tabFolder.getSelectionIndex();
		}
	}
	
	private TabSelectionListener tabListener = new TabSelectionListener();

	public ResourceExplorerView() {
		super();
		commandStack = new CommandStack();
	}
	
	public CommandStack getCommandStack(){
		return commandStack;
	}
	
	public void dispose(){
		super.dispose();
		for(int i = 0; i < tabs.length; i++)tabs[i].dispose();
	}
	
	public void init(IViewSite site, IMemento memento) throws PartInitException	{
		super.init(site, memento);
		
		reTypeTab = new RETypeTab(this);
		reDateTab = new REDateTab(this);
		reSearchTab = new RENameSearchTab(this);
		REBaseTab[] valtemp = new REBaseTab[]{reTypeTab, reDateTab, reSearchTab};
		tabs = valtemp;
		
		if(memento == null)return;
		Integer val = memento.getInteger(MEMENTO_OPEN_TAB);
		if(val != null)selectedTabIndex = val.intValue();
		for(int i = 0; i < tabs.length; i++)tabs[i].setState(memento);
	}
	
	public void saveState(IMemento memento){
		super.saveState(memento);
		if(memento == null)return;
		memento.putInteger(MEMENTO_OPEN_TAB, selectedTabIndex);
		for(int i = 0; i < tabs.length; i++)tabs[i].saveState(memento);
	}
	
	private class SashSelectionAdapter extends SelectionAdapter {
		private Sash sash;
		public SashSelectionAdapter(Sash sash){
			this.sash = sash;
		}
		public void widgetSelected(SelectionEvent event){
			   ((FormData) sash.getLayoutData()).top = new FormAttachment(0, event.y);
			   sash.getParent().layout();
		}
	}
	
/*	private void buildTabs(){
		for(int i = 0; i < tabs.length; i++)tabs[i].createTabItem(tabFolder);
		
		tabFolder.setSelection(selectedTabIndex);
		tabFolder.addSelectionListener(tabListener);

		makeActions();
		contributeToActionBars();
		
		for(int i = 0; i < tabs.length; i++)tabs[i].setTextCellEditor(deleteAction);
		
		setupDnDTargetProcessors();
		
		for(int i = 0; i < tabs.length; i++)tabs[i].hookDoubleClickAction(openAction);
	}
*/	
	public void createPartControl(Composite parent) {
		//myComposite = new Composite(parent, SWT.NONE);
		//myComposite.setLayout(new FillLayout());
		tabFolder = new TabFolder(parent, SWT.BOTTOM);
		for(int i = 0; i < tabs.length; i++)tabs[i].createTabItem(tabFolder);
		
		tabFolder.setSelection(selectedTabIndex);
		tabFolder.addSelectionListener(tabListener);

		makeActions();
		contributeToActionBars();
		
		for(int i = 0; i < tabs.length; i++)tabs[i].setTextCellEditor(deleteAction);
		
		setupDnDTargetProcessors();
		
		for(int i = 0; i < tabs.length; i++)tabs[i].hookDoubleClickAction(openAction);
	}

	private void makeActions() {
		
		final ResourceExplorerView myview = this;
		
		//IActionBars bars = ((IEditorSite)getSite()).getActionBars();

		undoAction = new CommandStackUndoAction(getCommandStack());
		//bars.setGlobalActionHandler(ActionFactory.UNDO.getId(), undoAction);
		redoAction = new CommandStackRedoAction(getCommandStack());
		//bars.setGlobalActionHandler(ActionFactory.REDO.getId(), redoAction);
		
		copyAction = new ResourceExplorerCopyAction(this);
		deleteAction = new ResourceExplorerDeleteAction(this);
		openAction = new ResourceExplorerOpenAction(this);
		makeNoteAction = new Action() {
			
			public void run() {
				Vector selected = getSelectedBaseObjects();
				Vector selectedResources = new Vector();
				Iterator it = selected.iterator();
				while(it.hasNext()){
					Object item = it.next();
					if(item instanceof Resource)selectedResources.add(item);
					else if(item instanceof IHasResource)
						selectedResources.add(((IHasResource)item).getResource());
					else if(item instanceof LinkableObject)
						selectedResources.add(((LinkableObject)item).getSurrogateFor());
				}
				NewNoteWizard wizard = null;
				if(selectedResources != null && selectedResources.size() > 1)
					wizard = new NewNoteWizard(getCommandStack(), "Selected items", selectedResources);
				else 
					wizard = new NewNoteWizard(getCommandStack());
				WizardDialog dialog = 
					new WizardDialog(myview.getSite().getShell(), wizard);
				dialog.open();
			}
		};
		makeNoteAction.setText("Create Note...");
		makeNoteAction.setToolTipText("Create new Note");
		makeNoteAction.setImageDescriptor(PlinyPlugin.getImageDescriptor("icons/noteIcon.gif"));
		
		//exportAction = new PlinyExportAction(this);
		exportAction = new PlinyExportAction();
		importAction = new ResourceExplorerImportAction(this);
		
		addToFavouritesAction = new AddToFavouritesAction(this);
		removeFromFavouritesAction = new RemoveFromFavouritesAction(this);
		
		setReferentAction = new SetReferentAction(this);
		clearReferentAction = new ClearReferentAction(this);
		
		//openNoteSearchAction = new OpenNoteSearchAction();
		updateUrlAction = new UpdateUrlAction(this);
	}
	
	//code to populate the contextual menu
	
	private void handleFavourites(IMenuManager manager, Vector selectedObjects){
		if(selectedObjects.size() != 1)return;
		BaseObject selectedObject = (BaseObject)selectedObjects.get(0);
		if(selectedObject instanceof Resource){
			if(Favourite.findFromResource((Resource)selectedObject) != null)
                 manager.add(removeFromFavouritesAction);
			else manager.add(addToFavouritesAction);
			return;
		}
		if(!(selectedObject instanceof Favourite))return;
		manager.add(removeFromFavouritesAction);
	}
	
	private void handleReferent(IMenuManager manager, Vector selectedObjects){
		if(PlinyPlugin.getReferent() != null)
		    manager.add(clearReferentAction);

		if(selectedObjects.size() != 1)return;
		BaseObject selectedObject = (BaseObject)selectedObjects.get(0);
		if(selectedObject instanceof Resource){
			manager.add(setReferentAction);
		}
	}
	
	private void handleWebReference(IMenuManager manager, Vector selectedObjects){
		if(selectedObjects.size() != 1)return;
		BaseObject selectedObject = (BaseObject)selectedObjects.get(0);
		if(selectedObject instanceof Favourite)
			selectedObject = ((Favourite)selectedObject).getResource();
		if(!(selectedObject instanceof Resource))return;
		Resource resource = (Resource)selectedObject;
		if(resource.getObjectType().getALID() != 2)return;
		updateUrlAction.setResource(resource);
		manager.add(updateUrlAction);
	}
	
	public void fillContextMenu(IMenuManager manager){
		Vector selectedObjects = getSelectedBaseObjects();
		manager.add(makeNoteAction);
		handleFavourites(manager, selectedObjects);
		handleReferent(manager, selectedObjects);
		handleWebReference(manager, selectedObjects);
		
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		contributeToLocalToolBar(bars.getToolBarManager());
		bars.setGlobalActionHandler(ActionFactory.DELETE.getId(),deleteAction);
		bars.setGlobalActionHandler(ActionFactory.UNDO.getId(),undoAction);
		bars.setGlobalActionHandler(ActionFactory.REDO.getId(),redoAction);
		bars.setGlobalActionHandler(ActionFactory.COPY.getId(),copyAction);
	}
	
	private void contributeToLocalToolBar(IToolBarManager manager) {
		manager.add(makeNoteAction);
		manager.add(exportAction);
		manager.add(importAction);
		manager.add(CreateMinimiseStatus.instance());
		manager.add(new OpenResHolderViewAction());
		//manager.add(openNoteSearchAction);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private REBaseTab getCurrentBaseTab(){
		if(tabs == null)return reTypeTab;
		return tabs[selectedTabIndex];
	}

	public TreeViewer getMyViewer(){
		return getCurrentBaseTab().getMyViewer();
	}

	public void setFocus() {
		REBaseTab curtab = getCurrentBaseTab();
		if(curtab != null)
			curtab.getFocusControl().setFocus();
	}
	
	public Vector getSelectedBaseObjects(){
		return getCurrentBaseTab().getSelectedBaseObjects();
	}
	
	public Vector getSelectedResourceExplorerItems(){
		return getCurrentBaseTab().getSelectedResourceExplorerItems();
	}
	
	public Vector getSelectedObjectsToOpen(){
		return getCurrentBaseTab().getSelectedObjectsToOpen();
	}
	
	public void refreshTreeViewers(){
		for(int i = 0; i < tabs.length; i++)tabs[i].getMyViewer().refresh();
		//if(reTypeTab != null)reTypeTab.getMyViewer().refresh();
		//if(reDateTab != null)reDateTab.getMyViewer().refresh();
		//if(reSearchTab != null)reSearchTab.getMyViewer().refresh();
	}
	
	private static final String targetProcessorId =
		"uk.ac.kcl.cch.jb.pliny.resourceExtensionProcessor";
	
	private void setupDnDTargetProcessors(){
		//System.out.println("setupDnDTargetProcessors starts");
		IExtensionRegistry registry= Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint= registry.getExtensionPoint(targetProcessorId);
		IExtension[] extensions= extensionPoint.getExtensions();
		for (int i= 0; i < extensions.length; i++) {
			IConfigurationElement[] elements= extensions[i].getConfigurationElements();
			for (int j= 0; j < elements.length; j++) {
				try {
					Object item= elements[j].createExecutableExtension("class");
					if (item instanceof IResourceExtensionProcessor){
						IResourceExtensionProcessor proc = (IResourceExtensionProcessor)item;
						proc.setViewPart(this);
						IResourceExtensionProcessorSource source = proc.getSource();
						source.setDropTargetProcessor(proc);
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
		IResourceExtensionProcessor browserProc = PlinyPlugin.getTheBrowserResourceExtensionProcessor();
		browserProc.setViewPart(this);
		IResourceExtensionProcessorSource source = browserProc.getSource();
		source.setDropTargetProcessor(browserProc);
		//System.out.println("setupDnDTargetProcessors ends");
	}
}
