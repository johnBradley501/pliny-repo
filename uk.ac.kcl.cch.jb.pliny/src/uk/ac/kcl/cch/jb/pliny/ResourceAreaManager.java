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

/**
 * 
 */
package uk.ac.kcl.cch.jb.pliny;

import java.util.EventObject;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.DeleteAction;
import org.eclipse.gef.ui.actions.RedoAction;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.gef.ui.actions.UndoAction;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.part.IPageSite;

import uk.ac.kcl.cch.jb.pliny.actions.PlinyMenuProvider;
import uk.ac.kcl.cch.jb.pliny.actions.PlinySelectAllAction;
import uk.ac.kcl.cch.jb.pliny.dnd.PlinyDragSourceListener;
import uk.ac.kcl.cch.jb.pliny.dnd.PlinyDragTextListener;
import uk.ac.kcl.cch.jb.pliny.dnd.PlinyObjectTransferDropTargetListener;
import uk.ac.kcl.cch.jb.pliny.dnd.PlinyTextTransferDropTargetListener;
import uk.ac.kcl.cch.jb.pliny.factories.PlinyGEFEditFactory;

/**
 * a manager to handle a non-scalable reference area. Can be used in the
 * context of SWT/JFace layout to provide a Pliny reference area.  The actual
 * instance of the control is created by invoking <code>createGraphicalViewer</code>
 * which returns an SWT Control that is the reference area.
 * 
 * <p>For example of its use see {@link uk.ac.kcl.cch.jb.pliny.editors.NoteEditor NoteEditor}.
 * 
 * @author John Bradley
 *
 */
public class ResourceAreaManager implements CommandStackListener, ISelectionChangedListener{
	protected GraphicalViewer graphicalViewer = null;
	//private ISelectionChangedListener listener;
	private IWorkbenchPart myPart = null;
	private IPageSite myPageSite = null;
	private EditDomain editDomain = null;
    private Object rootObject;
    private String ownerID;
    
	private UndoAction undoAction;
	private RedoAction redoAction;
	private DeleteAction deleteAction;
	private Vector selectionActions = new Vector();
	private ActionRegistry actionRegistry = null;
	private GraphicalViewerKeyHandler keyHandler = null;
	private ScrollingGraphicalViewer myScrollingGraphicalViewer = null;
	private RootEditPart myRootEditPart = null;
	
	private PlinyGEFEditFactory myFactory = null;
	private ContextMenuProvider provider= null;  
	/**
	 * establishes a manager for a non-scalable reference area.
	 * 
	 * @param rootObject the model object that provides data for the reference area.
	 * @param myPart the IWorkbenchPart that displays the reference area.
	 * @param ownerID in Eclipse ID for the editor/viewer that displays the reference area.
	 */
    public ResourceAreaManager(Object rootObject, IWorkbenchPart myPart, String ownerID){
    	this.rootObject = rootObject;
    	this.myPart = myPart;
    	this.ownerID = ownerID;
    	createActions();
    }
    
    public void setPageSite(IPageSite site){
    	myPageSite = site;
    }
    
    public void setMyFactory(PlinyGEFEditFactory factory){
    	myFactory = factory;
    	if(myFactory != null)myFactory.setWorkbenchPart(myPart);
    }
    
    public PlinyGEFEditFactory getMyFactory(){
    	if(myFactory == null){
    		myFactory = new PlinyGEFEditFactory(myPart);
    	}
    	return myFactory;
    }
    
    public void setGraphicalViewer(GraphicalViewer v){
    	if(graphicalViewer == null)graphicalViewer = v;
    }
    
    public void setRootEditPart(RootEditPart myRootEditPart){
    	this.myRootEditPart = myRootEditPart;
    }

    public void setMenuProvider(ContextMenuProvider myProvider){
    	provider = myProvider;
    }
    
    private void createActions(){
    	getActionRegistry();
		undoAction = new UndoAction(myPart);
		actionRegistry.registerAction(undoAction);
		redoAction = new RedoAction(myPart);
		actionRegistry.registerAction(redoAction);
		deleteAction = new DeleteAction(myPart);
		actionRegistry.registerAction(deleteAction);
		selectionActions.add(deleteAction);

	    Action selectAllAction = new PlinySelectAllAction(myPart);
	    actionRegistry.registerAction(selectAllAction);
		}
    
	public Control createGraphicalViewer(Composite parent){
		if(graphicalViewer == null)graphicalViewer = new ScrollingGraphicalViewer();
		graphicalViewer.createControl(parent);
		
		graphicalViewer.getControl().setBackground(ColorConstants.white);
		if(myRootEditPart == null)myRootEditPart = new ScalableRootEditPart();
		graphicalViewer.setRootEditPart(myRootEditPart);
		//graphicalViewer.setRootEditPart(new ScalableFreeformRootEditPart());
		graphicalViewer.addSelectionChangedListener(this);
		
		getEditDomain().addViewer(graphicalViewer);
		getEditDomain().getCommandStack().addCommandStackListener(this);
		
		handleSelectionProvider();
		
		//graphicalViewer.setEditPartFactory(new PlinyGEFEditFactory(myPart)); //IWorkbenchPart myPart
		graphicalViewer.setEditPartFactory(getMyFactory()); //IWorkbenchPart myPart
		if(rootObject != null)graphicalViewer.setContents(rootObject);
		
		keyHandler = new GraphicalViewerKeyHandler(graphicalViewer);
		graphicalViewer.setKeyHandler(keyHandler);
		
		if(provider == null)
		    provider = new PlinyMenuProvider(myPart,graphicalViewer, getEditDomain().getCommandStack());
		graphicalViewer.setContextMenu(provider);
		if(myPageSite != null)
			myPageSite.registerContextMenu(ownerID+".contextmenu", provider, graphicalViewer);
		else if(myPart != null)
		   myPart.getSite().registerContextMenu(ownerID+".contextmenu", provider, graphicalViewer);
		
		TransferDropTargetListener dropListener = getMyDropTargetListener(graphicalViewer);
		if(dropListener != null)graphicalViewer.addDropTargetListener(dropListener);
		dropListener = getMyTextDropTargetListener(graphicalViewer);
		if(dropListener != null)graphicalViewer.addDropTargetListener(dropListener);
		
		graphicalViewer.addDragSourceListener(getMyDragSourceListener());
		graphicalViewer.addDragSourceListener(getMyDragTextListener());

		return graphicalViewer.getControl();
	}
	
	protected TransferDropTargetListener getMyDropTargetListener(EditPartViewer viewer){
		return new PlinyObjectTransferDropTargetListener(viewer);
	}
	
	protected TransferDropTargetListener getMyTextDropTargetListener(EditPartViewer viewer){
		return new PlinyTextTransferDropTargetListener(graphicalViewer);
	}
	
	protected PlinyDragSourceListener getMyDragSourceListener(){
		return new PlinyDragSourceListener(graphicalViewer);
	}
	
	protected PlinyDragTextListener getMyDragTextListener(){
		return new PlinyDragTextListener(graphicalViewer);
	}

	protected void handleSelectionProvider(){
		if(myPageSite != null)myPageSite.setSelectionProvider(graphicalViewer);
		else if(myPart != null)
		   myPart.getSite().setSelectionProvider(graphicalViewer);
	}
	
	public void dispose(){
    	undoAction.dispose();
    	redoAction.dispose();
    	deleteAction.dispose();
    	
    	Iterator it = selectionActions.iterator();
    	while(it.hasNext()){
    		SelectionAction action = (SelectionAction)it.next();
    		action.dispose();
    	}
    	
    	if(graphicalViewer != null)graphicalViewer.removeSelectionChangedListener(this);
	}
	
	public void setRootObject(Object rootObject){
		this.rootObject = rootObject;
		if(graphicalViewer != null)graphicalViewer.setContents(rootObject);
	}
	
	public EditDomain getEditDomain(){
		if(editDomain == null)editDomain = new DefaultEditDomain(null);
		return editDomain;
	}

	public GraphicalViewer getGraphicalViewer(){
		return graphicalViewer;
	}

	public void commandStackChanged(EventObject event) {
		undoAction.update();
		redoAction.update();
	}
	
	public void addSelectionAction(SelectionAction action){
		getActionRegistry();
		selectionActions.add(action);
		actionRegistry.registerAction(action);
	}

	public void selectionChanged(SelectionChangedEvent event) {
		updateCommandStackActions();
	}

	public void updateCommandStackActions(){
		Iterator it = selectionActions.iterator();
		while(it.hasNext()){
			SelectionAction action = (SelectionAction)it.next();
			action.update();
		}
	}
	
	public ActionRegistry getActionRegistry(){
		if(actionRegistry == null)actionRegistry = new ActionRegistry();
        return actionRegistry;			
	}
	
	public void addKeyStrokeAction(KeyStroke keystroke, IAction action){
		if(keyHandler != null)
			keyHandler.put(keystroke, action);
	}
	
	public void removeKeyStrokeAction(KeyStroke keystroke){
		if(keyHandler != null)
			keyHandler.remove(keystroke);
	}

}
