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

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Tool;
import org.eclipse.gef.dnd.AbstractTransferDragSourceListener;
import org.eclipse.gef.tools.SelectionTool;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;

import uk.ac.kcl.cch.jb.pliny.figures.TopPanel;

/**
 * Provides the source support for inter-Pliny annotation/reference area DnD.
 * 
 * @author John Bradley
 */

public class PlinyDragSourceListener extends AbstractTransferDragSourceListener {
	//public class PlinyDragSourceListener implements TransferDragSourceListener {

	private static Object currentObject = null;
	private static EditPartViewer viewer;
	
	public PlinyDragSourceListener(EditPartViewer viewer) {
		super(viewer, ClipboardHandler.TRANSFER);
		this.viewer = viewer;
	}

	public void dragStart(DragSourceEvent event) {
		Tool currentTool = viewer.getEditDomain().getActiveTool();
		//System.out.println("dragStart: currentTool: "+currentTool);
		if(!(currentTool instanceof SelectionTool)){
			event.doit = false;
			event.detail = DND.DROP_NONE;
			//System.out.println("dragStart: doit set false");
			currentObject = null;
			return;
		}
        event.doit = TopPanel.getCurrentObject() != null;
        currentObject = TopPanel.getCurrentObject();
		//System.out.println("dragStart: currentObject: "+TopPanel.getCurrentObject()+", doit: "+event.doit);
	}

	public void dragSetData(DragSourceEvent event) {
		//System.out.println("dragSetData: currentObject: "+TopPanel.getCurrentObject());
		//Tool currentTool = viewer.getEditDomain().getActiveTool();
		//System.out.println("dragStart: currentTool: "+currentTool);
		//if(!(currentTool instanceof SelectionTool)){
		//	event.doit = false;
		//	System.out.println("dragStart: doit set false");
		//	currentObject = null;
		//	return;
		//}
		if(ClipboardHandler.TRANSFER.isSupportedType(event.dataType))
		    event.data = TopPanel.getCurrentObject();
		//System.out.println("dragSetData: "+event.data);
	}

	public void dragFinished(DragSourceEvent event) {
		super.dragFinished(event);
		currentObject = null;
		ClipboardHandler.TRANSFER.setObject(null);
		TopPanel.clearCurrentObject();
		//System.out.println("PlinyDragSourceListener: dragFinished");
	}

	public Transfer getTransfer() {
		//System.out.println("getTransfer()");
		return ClipboardHandler.TRANSFER;
	}
	
	/**
	 * gets the object that has been dragged.
	 * 
	 * @return Object that was dragged
	 */
	
	public static Object getCurrentObject(){
		return currentObject;
	}
	
	/**
	 * stores the object that is to be dragged.
	 * 
	 * @param theObject object to be dragged
	 */
	
	public static void setCurrentObject(Object theObject){
		currentObject = theObject;
	}

}
