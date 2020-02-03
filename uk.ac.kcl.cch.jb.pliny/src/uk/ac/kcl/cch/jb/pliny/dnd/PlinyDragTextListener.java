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
package uk.ac.kcl.cch.jb.pliny.dnd;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Tool;
import org.eclipse.gef.dnd.AbstractTransferDragSourceListener;
import org.eclipse.gef.tools.SelectionTool;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;

import uk.ac.kcl.cch.jb.pliny.figures.TopPanel;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * @author John Bradley
 *
 */
public class PlinyDragTextListener extends AbstractTransferDragSourceListener {

	private static Object currentObject = null;
	private EditPartViewer viewer;

	public PlinyDragTextListener(EditPartViewer viewer){
		super(viewer, TextTransfer.getInstance());
		this.viewer = viewer;
	}

	public void dragStart(DragSourceEvent event) {
		Tool currentTool = viewer.getEditDomain().getActiveTool();
		//System.out.println("dragStart (Text): currentTool: "+currentTool);
		if(!(currentTool instanceof SelectionTool)){
			event.doit = false;
			event.detail = DND.DROP_NONE;
			//System.out.println("dragStart: doit set false");
			currentObject = null;
			return;
		}
        event.doit = TopPanel.getCurrentObject() != null;
        currentObject = TopPanel.getCurrentObject();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	public void dragSetData(DragSourceEvent event) {
        if(currentObject instanceof LinkableObject){
        	LinkableObject lo = (LinkableObject) currentObject;
        	Resource r = lo.getSurrogateFor();
        	if(r == null){
        		event.doit = false;
        		return;
        	}
        	StringBuffer buf = new StringBuffer();
        	
        	if(r.getIdentifier().startsWith("url:")){
        		String url = r.getIdentifier().substring(4);
        		//buf.append("[InternetShortcut]\nURL=");
        		buf.append(url);
        	} else {
        	   buf.append(r.getName());
        	   if(r instanceof NoteLucened){
        		  NoteLucened n = (NoteLucened)r;
        		  buf.append("\n\n"+n.getContent().trim());
        	   }
        	}
        	event.data = buf.toString();
        }
        else event.doit = false;
	}

	public void dragFinished(DragSourceEvent event) {
		currentObject = null;
	}

}
