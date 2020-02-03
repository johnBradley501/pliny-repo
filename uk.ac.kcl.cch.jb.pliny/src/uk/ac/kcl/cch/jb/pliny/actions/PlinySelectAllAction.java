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

package uk.ac.kcl.cch.jb.pliny.actions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.actions.SelectAllAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * the standard select-all action for Pliny displays
 * created from GEF GraphicViewers.
 * Marks all LinkableObjects and Links as selected.
 * <p>
 * Selects all edit parts and links in the active workbench part. This is, in
 * my opinion, a fix on the "SelectAllAction" provided by GEF, which 
 * (at least in GEF 3.1) ignores connection items.
 *
 * @author John Bradley
 *
 */

public class PlinySelectAllAction extends SelectAllAction {
	private IWorkbenchPart part;

	/**
	 * constructor for the SelectAllAction.  Items on display
	 * are taken from information available from the part which
	 * is assumed to be a GEF GraphicalViewer, and the list of
	 * items is passed back to the Graphical Viewer for it to handle.
	 * @param part
	 */
	public PlinySelectAllAction(IWorkbenchPart part) {
		super(part);
		this.part = part;
	}
	
	public void updatePart(IWorkbenchPart part){
		this.part = part;
	}

	public void run() {
		GraphicalViewer viewer = (GraphicalViewer)part.getAdapter(GraphicalViewer.class);
		if (viewer != null){
			Set items = new HashSet();
			Iterator it = viewer.getContents().getChildren().iterator();
			while(it.hasNext()){
				Object item = it.next();
				items.add(item);
				if(item instanceof GraphicalEditPart){
					items.addAll(((GraphicalEditPart)item).getSourceConnections());
					items.addAll(((GraphicalEditPart)item).getTargetConnections());
				}
			}
			Vector allItems = new Vector(items);
			viewer.setSelection(new StructuredSelection(allItems));
		}
	}

}
