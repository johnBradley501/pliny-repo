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
package uk.ac.kcl.cch.jb.pliny.views.utils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.dnd.ClipboardHandler;
import uk.ac.kcl.cch.jb.pliny.dnd.PlinyDragSourceListener;
import uk.ac.kcl.cch.jb.pliny.model.IHasResource;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.views.REBaseTab;
import uk.ac.kcl.cch.rdb2java.Rdb2javaPlugin;

/**
 * the class that provides content for the Table viewer in the
 * Resource Holder view.
 * 
 * @author John Bradley
 *
 */

public class ResHolderContentProvider implements IStructuredContentProvider,
		PropertyChangeListener {
	
	private TableViewer viewer;
	private Vector heldItems;
	
	private class ResHolderDragSourceListener implements DragSourceListener{
		private TableViewer viewer;
		private Resource resourceSource = null;
	    //private TableItem[] dragSourceItem = new TableItem[1];


		public ResHolderDragSourceListener(TableViewer viewer){
			this.viewer = viewer;
		}

		public void dragStart(DragSourceEvent event) {
			TableItem[] selection = viewer.getTable().getSelection();
			if (selection.length > 0/* && selection[0].getItemCount() == 0*/) {
				event.doit = true;
				if(selection[0].getData() instanceof Resource){
					resourceSource = (Resource)selection[0].getData();
					PlinyDragSourceListener.setCurrentObject(resourceSource);
				} else event.doit = false;
			} else {
				event.doit = false;
			}
		}

		public void dragFinished(DragSourceEvent event) {
			if (event.detail == DND.DROP_MOVE){
				remove(resourceSource);
			}
			resourceSource = null;
		}

		public void dragSetData(DragSourceEvent event) {
			event.data = resourceSource;
		}

	}
	
	private class ResHolderDropTargetHandler extends DropTargetAdapter{
	      public void drop(DropTargetEvent event) {
	          if (ClipboardHandler.TRANSFER.isSupportedType(event.currentDataType)) {
	        	if(event.data instanceof Resource)
	        		add((Resource)event.data);
	        	else if (event.data instanceof LinkableObject)
	        		add(((LinkableObject)event.data).getSurrogateFor());
	        	else if (event.data instanceof IHasResource)
	        		add(((IHasResource)event.data).getResource());
	          }
	          PlinyDragSourceListener.setCurrentObject(null);
	        }
	      
	      public void dragEnter(DropTargetEvent event) {
	    	  /*
	    	   * Drag and Drop seems such a pain!  It seems to be necessary to include
	    	   * this code so that dragging from the ResourceExplorer works properly.
	    	   * I don't understand why it is needed in this case, and not from drags
	    	   * from other parts of Pliny!           .. JB
	    	   */
	    	  if(PlinyDragSourceListener.getCurrentObject() != null){
	    		  event.data = PlinyDragSourceListener.getCurrentObject();
	    		  event.detail = DND.DROP_COPY;
	    	  }
	    	  super.dragEnter(event);
	      }

	}

	public ResHolderContentProvider(TableViewer viewer){
		this.viewer = viewer;
		//this.heldItems = new Vector();
		this.heldItems = PlinyPlugin.getDefault().getResHolderItems();
		Iterator it = heldItems.iterator();
		while(it.hasNext()){
			Resource r = (Resource)it.next();
			r.addPropertyChangeListener(this);
		}
		Rdb2javaPlugin.getDataServer().addPropertyChangeListener(this);
		
		/*
		 * setting up DnD support: initially borrowed from the SWT Snippet91 .. jb
		 */
	    Transfer[] types = new Transfer[] { ClipboardHandler.TRANSFER };
	    //Transfer[] types = new Transfer[] { TextTransfer.getInstance(), ClipboardHandler.TRANSFER };
	    //Transfer[] types = new Transfer[] { ClipboardHandler.TRANSFER, TextTransfer.getInstance()  };
	    int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;

	    final DragSource source = new DragSource(viewer.getTable(), operations);
	    source.setTransfer(types);
	    //final TableItem[] dragSourceItem = new TableItem[1];
	    source.addDragListener(new ResHolderDragSourceListener(viewer));
	    
	    final DropTarget target = new DropTarget(viewer.getTable(), operations);
	    target.setTransfer(types);
	    target.addDropListener(new ResHolderDropTargetHandler());
	    
	    hookKeyboardActions();
	}
	
	// handling the use of the Delete key to remove something from the list.
	
	private void hookKeyboardActions() {
		viewer.getControl().addKeyListener(new KeyAdapter(){
			public void keyReleased(KeyEvent event){
				handleDeleteRequested(event);
			}
		});
	}
	
	protected void handleDeleteRequested(KeyEvent event){
		TableItem[] selection = viewer.getTable().getSelection();
		if (selection.length > 0 && (selection[0].getData() instanceof Resource)){
			remove((Resource)selection[0].getData());
		}

	}
	
	
	public void add(Resource r){
		if(heldItems.contains(r))return;
		heldItems.add(0,r);
		r.addPropertyChangeListener(this);
		viewer.refresh();
	}
	
	public void remove(Resource r){
		if(!heldItems.contains(r))return;
		heldItems.remove(r);
		r.removePropertyChangeListener(this);
		viewer.refresh();
	}

	public Object[] getElements(Object inputElement) {
		// TODO Auto-generated method stub
		return heldItems.toArray();
	}

	public void dispose() {
		Rdb2javaPlugin.getDataServer().removePropertyChangeListener(this);
		if(heldItems == null)return;
		Iterator it = heldItems.iterator();
		while(it.hasNext()){
			Resource r = (Resource)it.next();
			r.removePropertyChangeListener(this);
		}

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// not needed

	}

	public void propertyChange(PropertyChangeEvent arg0) {
		String pString = arg0.getPropertyName();
		if(pString == Resource.NAME_PROP){
			viewer.refresh(arg0.getNewValue());
		}
		else if(pString.equals("Delete-Resource")){
			remove((Resource)arg0.getOldValue());
		}

	}

}
