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

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.kcl.cch.jb.pliny.dnd.ClipboardHandler;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.VirtualResource;

/**
 * this class wraps the function of an image Icon and a draggable object
 * together for 
 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource resource}s.  
 * It creates an image for an icon and is given a Resource which it holds.
 * The image is setup as draggable, and if dropped elsewhere in a Pliny
 * application will have a Pliny Resource to deliver as its content.
 * <p>
 * <i>Note</i>: This
 * object supports DnD between Pliny components within a single Eclipse
 * instance only.
 * 
 * @author John Bradley
 *
 */

public class DnDResourceHolder implements DragSourceListener{
	
	private Resource heldResource = null;
	private Label theLabel = null;

	/**
	 * create the DnDResource Holder, and create the Icon that will represent it.
	 * 
	 * @param parent Composite to act as parent to the icon
	 * @param style int SWT style values for the icon
	 */
	public DnDResourceHolder(Composite parent, int style) {
		theLabel = new Label(parent, style);
		DragSource source = new DragSource(theLabel, DND.DROP_MOVE | DND.DROP_COPY);
		Transfer[] types = new Transfer[] {ClipboardHandler.TRANSFER};
		source.setTransfer(types);
		source.addDragListener(this);
	}
	
	public Label getLabel(){
		return theLabel;
	}
	
	public Resource getResource(){
		return heldResource;
	}
	
	public void setResource(Resource resource){
		heldResource = resource;
	}

	public void dragStart(DragSourceEvent event) {
		//System.out.println("DnDResourceHolder.dragStart: "+heldResource);
		if(getResource() == null)event.doit = false;
		PlinyDragSourceListener.setCurrentObject(getResource());
	}

	public void dragSetData(DragSourceEvent event) {
		//System.out.println("DnDResourceHolder.dragSetData: "+heldResource);
		if(ClipboardHandler.TRANSFER.isSupportedType(event.dataType)) {
			event.data = getResource();
		}
	}

	public void dragFinished(DragSourceEvent event) {
		if(event.doit && (getResource() instanceof VirtualResource))
			((VirtualResource)getResource()).makeMeReal();
	}

}
