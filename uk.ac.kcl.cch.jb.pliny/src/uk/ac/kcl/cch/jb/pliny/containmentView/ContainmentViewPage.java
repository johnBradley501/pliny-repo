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

package uk.ac.kcl.cch.jb.pliny.containmentView;

import java.util.EventObject;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageSite;

import uk.ac.kcl.cch.jb.pliny.actions.PlinySelectAllAction;
import uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentItem;
import uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentLink;
import uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentSet;
import uk.ac.kcl.cch.jb.pliny.containmentView.model.IncludedTypeManager;
import uk.ac.kcl.cch.jb.pliny.containmentView.parts.ContainmentItemPart;
import uk.ac.kcl.cch.jb.pliny.containmentView.parts.ContainmentLinkPart;
import uk.ac.kcl.cch.jb.pliny.containmentView.parts.ContainmentSetPart;
import uk.ac.kcl.cch.jb.pliny.dnd.PlinyDragSourceListener;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * generates a page to be used by the ContainmentView to display
 * containment information for the given resource.
 * <p>
 * Note that the ContainmentView page display is managed by GEF, so
 * has a GEF-oriented design. In particular, this class also implements
 * code for GEF's EditPartFactory: the function GEF needs to map
 * parts of the model it is given to work with with GEF EditParts.
 * <p>
 * All the model information that this object managers is accessed
 * through its 
 * {@link uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentSet ContainmentSet}
 * object, which it creates for the given Resource.  This page object
 * also has to handle situations where the Editor it is synchronised with
 * can change its resource.
 * 
 * @author John Bradley
 *
 */
public class ContainmentViewPage extends Page 
implements IAdaptable, CommandStackListener, ISelectionChangedListener, EditPartFactory{
	//private Resource resource = null;
	private ContainmentSet containmentSet = null;
	private PageBook book = null;
	private ContainmentView view;
	private GraphicalViewer graphicalViewer = null;
	private EditDomain editDomain = null;

	public ContainmentViewPage(Resource resource, PageBook book, ContainmentView view) {
		super();
		//this.resource = resource;
		this.book = book;
		this.view = view;
		init(new PageSite((IViewSite)view.getSite()));
		containmentSet = new ContainmentSet(resource, view.getTypeManager());
	}
	
	/**
	 * returns the IncludedTypeManager applicable to this page.  Since
	 * all pages share a single IncludedTypeManager, this method
	 * actually gets it from the ContainmentView, which holds the
	 * single instance for all Pages to use.
	 * 
	 * @return IncludedTypeManager the current Manager.
	 */
	public IncludedTypeManager getTypeManager(){
		return view.getTypeManager();
	}
	
	public void dispose(){
		containmentSet.dispose();
		getCommandStack().removeCommandStackListener(this);
		super.dispose();
	}

	public void createControl(Composite parent) {
		if(graphicalViewer == null)
		     createGraphicalViewer(parent);
	}
	
	private void createGraphicalViewer(Composite parent){
		graphicalViewer = new ScrollingGraphicalViewer();
		graphicalViewer.createControl(parent);
		
		graphicalViewer.getControl().setBackground(ColorConstants.lightGray);
		graphicalViewer.setRootEditPart(new ScalableRootEditPart());
		graphicalViewer.addSelectionChangedListener(this);
		
		getEditDomain().addViewer(graphicalViewer);
		getEditDomain().getCommandStack().addCommandStackListener(this);
		//view.changeActionCommandStack(getEditDomain().getCommandStack());
		
		getSite().setSelectionProvider(graphicalViewer);
		
		graphicalViewer.setEditPartFactory(this);
		graphicalViewer.setContents(containmentSet);
		
		ContextMenuProvider provider = new ContainmentViewMenuProvider(this);
		graphicalViewer.setContextMenu(provider);
		getSite().registerContextMenu("uk.ac.kcl.cch.jb.pliny.annotationView.contextmenu", provider, graphicalViewer);
		
		//graphicalViewer.addDropTargetListener(
		//		new PlinyObjectTransferDropTargetListener(graphicalViewer));
		graphicalViewer.addDragSourceListener(new PlinyDragSourceListener(graphicalViewer));
	}
	/**
	 * Returns the <code>GraphicalViewer</code> of this view Page.
	 * 
	 * @return the <code>GraphicalViewer</code>
	 */
	public GraphicalViewer getGraphicalViewer()
	{
		return graphicalViewer;
	}
	
	/**
	 * Returns the <code>EditDomain</code> of this view Page.
	 * 
	 * @return the <code>EditDomain</code>
	 */
	public EditDomain getEditDomain(){
		if(editDomain == null)editDomain = new DefaultEditDomain(null);
		return editDomain;
	}
	
	
	/**
	 * Returns the <code>CommandStack</code> of this view Page.
	 * 
	 * @return the <code>CommandStack</code>
	 */
	public CommandStack getCommandStack(){
		return getEditDomain().getCommandStack();
	}

	public Control getControl() {
		if(graphicalViewer == null)
			createGraphicalViewer(book);
		return graphicalViewer.getControl();
	}

	public void setFocus() {
		graphicalViewer.getControl().setFocus();
	}
	
	public void setActionBars(IActionBars bars) {
		//view.getActionRegistry().registerAction(new DeleteAction((IWorkbenchPart)this));
		super.setActionBars(bars);
		
		PlinySelectAllAction saAction = new PlinySelectAllAction(view);
		bars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), saAction);
	}
	
	/**
	 * gets the ContainmentSet this page is using as the basis
	 * for its display.
	 * 
	 * @return ContainmentSet the page's ContainmentSet.
	 */
	public ContainmentSet getContainmentSet(){
		return containmentSet;
	}

	public void updateCommandStackActions() {
		// TODO Auto-generated method stub
		
	}

	public void updateUndoActions() {
		// TODO Auto-generated method stub
		
	}

	public Object getAdapter(Class adapter) {
		if(adapter == CommandStack.class)
			return getCommandStack(); 
		if(adapter == EditDomain.class)
			return getEditDomain();
		if(adapter == GraphicalViewer.class)
			return graphicalViewer;
		return null;
	}

	public void commandStackChanged(EventObject event) {
		// TODO Auto-generated method stub
		
	}

	public void selectionChanged(SelectionChangedEvent event) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * implements code required by GEF's EditPartFactory to map
	 * model elements to GEF's editParts.
	 * 
	 * @see org.eclipse.gef.EditPartFactory
	 */
	public EditPart createEditPart(EditPart context, Object model) {
		if(model instanceof ContainmentSet)
			return new ContainmentSetPart((ContainmentSet)model);
		if(model instanceof ContainmentItem)
			return new ContainmentItemPart((ContainmentItem)model);
		if(model instanceof ContainmentLink)
			return new ContainmentLinkPart((ContainmentLink)model);
		return null;
	}
	
	public Vector getSelectedContainmentItems(){
		IStructuredSelection selection = (IStructuredSelection)graphicalViewer.getSelection();
		Vector rslts = new Vector();
		Iterator iter = selection.iterator();
		while(iter.hasNext()){
			Object item = iter.next();
			if(item instanceof ContainmentItemPart){
				item = ((ContainmentItemPart)item).getContainmentItem();
			    if(item != null) rslts.add(item);
			}
		}
		return rslts;
	}

	/**
	 * invoked by ContainmentView if the Editor it is currently
	 * connected to is a <code>IResourceChangeablePart</code>, and 
	 * that editor has just changed its resource.
	 * 
	 * @see uk.ac.kcl.cch.jb.pliny.editors.IResourceChangeablePart
	 * 
	 * @param newResource the New resource to display.
	 */
	public void updateResource(Resource newResource) {
		containmentSet.updateResource(newResource);
	}

}
