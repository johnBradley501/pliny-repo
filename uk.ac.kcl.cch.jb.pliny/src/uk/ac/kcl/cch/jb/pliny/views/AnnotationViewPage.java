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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageSite;

import uk.ac.kcl.cch.jb.pliny.ResourceAreaManager;
import uk.ac.kcl.cch.jb.pliny.actions.CopyPlinyAction;
import uk.ac.kcl.cch.jb.pliny.actions.CutPlinyAction;
import uk.ac.kcl.cch.jb.pliny.actions.DisplayReferrerAction;
import uk.ac.kcl.cch.jb.pliny.actions.MakeConnectionAction;
import uk.ac.kcl.cch.jb.pliny.actions.MakeNoteAction;
import uk.ac.kcl.cch.jb.pliny.actions.MinimizeAllAction;
import uk.ac.kcl.cch.jb.pliny.actions.PastePlinyAction;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * generates a page to be used by the AnnotationView to display
 * a reference area for the given resource.
 * 
 * @author John Bradley
 *
 */

public class AnnotationViewPage extends Page implements IAdaptable{
	
	private Resource resource = null;

	private ResourceAreaManager areaManager = null;

	private PageBook book = null;
	private AnnotationView view = null;

	private Action newNoteAction;
	private Action newConnectionAction;
	private Action minimizeAllAction;
	private PastePlinyAction pasteAction;
	private CopyPlinyAction copyAction;
	private CutPlinyAction cutAction;
	private DisplayReferrerAction displayReferrerAction = null;

	
	public AnnotationViewPage(Resource resource, PageBook book, AnnotationView view) {
		super();
		this.resource = resource;
		this.book = book;
		this.view = view;
		init(new PageSite((IViewSite)view.getSite()));
		areaManager = new ResourceAreaManager(resource, view, AnnotationView.ANNOTATIONVIEW_ID);
		areaManager.setPageSite(getSite());

	}

	private void makeActions(){
		pasteAction = new PastePlinyAction(view);
		areaManager.addSelectionAction(pasteAction);
		copyAction = new CopyPlinyAction(view);
		areaManager.addSelectionAction(copyAction);
		cutAction = new CutPlinyAction(view);
		areaManager.addSelectionAction(cutAction);

		newNoteAction = new MakeNoteAction(getEditDomain());
		newConnectionAction = new MakeConnectionAction(getEditDomain());
		minimizeAllAction = new MinimizeAllAction(view);
		displayReferrerAction = new DisplayReferrerAction(view);
	}

	public Resource getMyResource(){
		return resource;
	}
    
    public void dispose(){
    	areaManager.dispose();

    	pasteAction.dispose();
    	copyAction.dispose();
    	cutAction.dispose();
    	super.dispose();
    }
	
	public EditDomain getEditDomain(){
		return areaManager.getEditDomain();
	}
	
	public CommandStack getCommandStack(){
		return getEditDomain().getCommandStack();
	}
	
	public ActionRegistry getActionRegistry(){
		return areaManager.getActionRegistry();
	}

	public void createControl(Composite parent) {
		if(areaManager.getGraphicalViewer() == null)
		     areaManager.createGraphicalViewer(parent);
	}
	
	public GraphicalViewer getGraphicalViewer(){
		if(areaManager.getGraphicalViewer() == null)
		     areaManager.createGraphicalViewer(book);
		return areaManager.getGraphicalViewer();
	}

	public Control getControl() {
		if(areaManager.getGraphicalViewer() == null)
		     areaManager.createGraphicalViewer(book);
		return areaManager.getGraphicalViewer().getControl();
	}

	public void setFocus() {
		areaManager.getGraphicalViewer().getControl().setFocus();

	}
	
	public void setActionBars(IActionBars bars) {
		//view.getActionRegistry().registerAction(new DeleteAction((IWorkbenchPart)this));
		super.setActionBars(bars);
		
		makeActions();
		
		//ActionRegistry registry = view.getActionRegistry();
		ActionRegistry registry = getActionRegistry();

		String id = ActionFactory.UNDO.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
		id = ActionFactory.REDO.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
		id = ActionFactory.DELETE.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
		id = ActionFactory.COPY.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
		id = ActionFactory.CUT.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
		id = ActionFactory.PASTE.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
		id = ActionFactory.SELECT_ALL.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
		
		//actionsContributed = true;
		IToolBarManager manager = bars.getToolBarManager();
		manager.add(registry.getAction(ActionFactory.UNDO.getId()));
		manager.add(registry.getAction(ActionFactory.REDO.getId()));
		manager.add(newNoteAction);
		manager.add(newConnectionAction);
		manager.add(minimizeAllAction);
		manager.add(displayReferrerAction);
    }
	
	public Object getAdapter(Class adapter){
		if((adapter == GraphicalViewer.class) || adapter == EditPartViewer.class)
			return getGraphicalViewer();
		if (adapter == CommandStack.class)
			return getCommandStack();
		if (adapter == EditDomain.class)
			return getEditDomain();
		if(adapter == ActionRegistry.class)return getActionRegistry();
		return null;
	}
	
	public void updateCommandStackActions(){
		areaManager.updateCommandStackActions();
	}
	
	public void updateUndoActions(){
	    areaManager.commandStackChanged(null);
	}
}
