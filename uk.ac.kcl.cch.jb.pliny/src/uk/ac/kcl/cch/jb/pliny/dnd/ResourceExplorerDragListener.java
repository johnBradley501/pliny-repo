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

package uk.ac.kcl.cch.jb.pliny.dnd;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;

import uk.ac.kcl.cch.jb.pliny.model.Favourite;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.IHasResource;
import uk.ac.kcl.cch.jb.pliny.views.utils.IResourceTreeDisplayer;

/**
 * Used in the ResourceExplorer and ResourceExplorer-like views (such as
 * the NoteSearchView) to handle DnD requests out of ResourceExplorer to other
 * parts of Pliny.
 * 
 * @author John Bradley
 *
 */

public class ResourceExplorerDragListener implements DragSourceListener {

	private IResourceTreeDisplayer view;
	private Object heldItem = null;
	
	public ResourceExplorerDragListener(IResourceTreeDisplayer view) {
		//super();
		this.view = view;
		TreeViewer viewer = view.getMyViewer();
		DragSource source = new DragSource(viewer.getControl(), DND.DROP_COPY);
		source.setTransfer(new Transfer[] {ClipboardHandler.TRANSFER, FileTransfer.getInstance()});
		source.addDragListener(this);
	}

	public void dragStart(DragSourceEvent event) {
		event.doit = true;
		Vector selected = view.getSelectedObjectsToOpen();
		if (selected.size() != 1){
			event.doit = false;
			return;
		}
		Object item = selected.get(0);
		heldItem = item;
		event.data = item;
		PlinyDragSourceListener.setCurrentObject(item);
		//ClipboardHandler.TRANSFER.setObject(item); // this has been commented out jb
		//System.out.println("dragStart, event.data: "+item);
		if(item instanceof Resource)return;
		if(item instanceof LinkableObject)return;
		if(item instanceof Favourite)return;
		event.doit = false;
		PlinyDragSourceListener.setCurrentObject(null);
		//ClipboardHandler.TRANSFER.setObject(null);
	}

	public void dragSetData(DragSourceEvent event) {
		//System.out.println("ResourceExplorerDragListener dragSetData starts");
		/* 
		 * Apparently on the Macintosh the selection is not available by the time you get here.
		 * Hence the change in code to save the selected item in dragStart() in heldItem
		 * and then use it here when it is time to set the event.data     ... JB (5 Oct 2009)
		 */
		if (ClipboardHandler.TRANSFER.isSupportedType(event.dataType)){
		    if(heldItem != null)event.data = heldItem;
		    else {
			   Vector selected = view.getSelectedObjectsToOpen();
			   //System.out.println("ResourceExplorerDragListener dragSetData selected[1]: "+selected.size());

			   event.data = selected.get(0);
			   heldItem = event.data;
		    }
			PlinyDragSourceListener.setCurrentObject(heldItem);
			//System.out.println("ResourceExplorerDragListener dragSetData: "+event.data);
		}
		if (FileTransfer.getInstance().isSupportedType(event.dataType)){
			handleFileDrag(event);
		}
	}

	private void handleFileDrag(DragSourceEvent event) {
		event.doit = false;
		Resource theResource = null;
		if(heldItem instanceof Resource)theResource = (Resource)heldItem;
		else if(heldItem instanceof LinkableObject)theResource = ((LinkableObject)heldItem).getSurrogateFor();
		else if(heldItem instanceof IHasResource)theResource = ((IHasResource)heldItem).getResource();
		if(theResource == null)return;
		File theFile = theResource.getResourceFile();
		if(theFile == null)return;
		String theString = null;
		try {
			theString = theFile.getCanonicalPath();
		} catch (IOException e) {
			return;
		}
		String[] stringList = new String[1];
		stringList[0] = theString;
		event.data = stringList;
		event.doit = true;
	}

	public void dragFinished(DragSourceEvent event) {
		PlinyDragSourceListener.setCurrentObject(null);
		//ClipboardHandler.TRANSFER.setObject(null);
	}

}
