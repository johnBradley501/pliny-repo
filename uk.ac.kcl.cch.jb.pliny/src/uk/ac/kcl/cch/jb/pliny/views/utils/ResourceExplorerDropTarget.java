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

package uk.ac.kcl.cch.jb.pliny.views.utils;

import java.util.Collection;
import java.util.Vector;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Widget;

import uk.ac.kcl.cch.jb.pliny.commands.AddToFavouritesCommand;
import uk.ac.kcl.cch.jb.pliny.commands.PastePlinyCommand;
import uk.ac.kcl.cch.jb.pliny.dnd.ClipboardHandler;
import uk.ac.kcl.cch.jb.pliny.dnd.IResourceExtensionProcessorSource;
import uk.ac.kcl.cch.jb.pliny.model.Favourite;
import uk.ac.kcl.cch.jb.pliny.model.IHasResource;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.views.ResourceExplorerView;

/**
 * provides code that is invoked whenever an object is DnD dropped
 * on an item in the Resource Explorer.
 * <p>
 * It can handle drops of Pliny objects (via the
 * {@link uk.ac.kcl.cch.jb.pliny.dnd.ClipboardHandler ClipboardHandler}) and
 * text (converted to a new Pliny NoteLucened through
 * {@link uk.ac.kcl.cch.jb.pliny.dnd.ClipboardHandler#processTextObject}.
 * <p>
 * If the drop was onto a ObjectType item in the Resource Explorer, then
 * the appropriate
 * {@link uk.ac.kcl.cch.jb.pliny.dnd.IResourceExtensionProcessor IResourceExtensionProcessor}
 * is invoked to perform an import.
 * 
 * @author John Bradley
 *
 */

public class ResourceExplorerDropTarget extends DropTargetAdapter {

	private IResourceTreeDisplayer view;
	//private TreeViewer viewer;
	
	public ResourceExplorerDropTarget(IResourceTreeDisplayer view) {
		super();
		this.view = view;
		TreeViewer viewer = view.getMyViewer();
		DropTarget target = new DropTarget(
				viewer.getControl(), 
				DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
		target.setTransfer(
				new Transfer[] {
					ClipboardHandler.TRANSFER,
					TextTransfer.getInstance(),
					FileTransfer.getInstance()
				});
		target.addDropListener(this);
	}
	
	private boolean testIsSupported(Object myObject,DropTargetEvent event){
		//System.out.println("  myObject: "+myObject);
		
		IResourceExtensionProcessorSource ot = null;
		
		boolean isResourceItem = myObject instanceof ResourceExplorerResourceItem;
		if(!isResourceItem){
			if(myObject instanceof ResourceExplorerFavGroupItem){
				return ClipboardHandler.TRANSFER.isSupportedType(event.currentDataType);
			} else if(!(myObject instanceof ResourceExplorerObjectTypeItem)){
				return false;
			}
			ResourceExplorerObjectTypeItem item = (ResourceExplorerObjectTypeItem)myObject;
			ot = (IResourceExtensionProcessorSource)item.getAssociatedObject();
			if(ot.getDropTargetProcessor() == null)return false;
		}

		boolean canHandle = false;


		if(ClipboardHandler.TRANSFER.isSupportedType(event.currentDataType))
			if(event.data == null || ot == null) canHandle = true;
			else canHandle = ot.getDropTargetProcessor().canHandleObject(event.data);
		
		if(TextTransfer.getInstance().isSupportedType(event.currentDataType)){
			canHandle = true;
		}

		if(!isResourceItem && FileTransfer.getInstance().isSupportedType(event.currentDataType)){
			canHandle = true;
		}
		//System.out.println("  canHandle: "+canHandle);
		return canHandle;
	}
	
	public void dragOver(DropTargetEvent event) {
	    Widget target = event.item;
	    if(target == null)return;
		//System.out.println("dragOver: event.item:"+event.item+", event.widget:"+event.widget);
		Object myObject = target.getData();
		if(myObject == null)return;
		//System.out.println("  myObject: "+myObject);
		/*
		if(!(myObject instanceof ResourceExplorerObjectTypeItem)){
			//event.feedback = DND.FEEDBACK_NONE;
			event.detail = DND.DROP_NONE;
			return;
		}
		
		ResourceExplorerObjectTypeItem item = (ResourceExplorerObjectTypeItem)myObject; */
		if(testIsSupported(myObject,event))event.detail = DND.DROP_COPY;
		else event.detail = DND.DROP_NONE;

		/*
		IResourceExtensionProcessorSource ot = (IResourceExtensionProcessorSource)item.getAssociatedObject();
		if(ot.getDropTargetProcessor() == null){
			//event.feedback = DND.FEEDBACK_NONE;
			event.detail = DND.DROP_NONE;
			//System.out.println("  event.detail: "+event.detail);
			return;
		} else if(event.data != null && !ot.getDropTargetProcessor().canHandleObject(event.data))
			event.detail = DND.DROP_NONE;
		else event.detail = DND.DROP_COPY;
		*/
		
	}

	public void dragEnter(DropTargetEvent event){
		if (event.detail == DND.DROP_MOVE || event.detail == DND.DROP_DEFAULT){
			if((event.operations & DND.DROP_COPY) != 0)
				event.detail = DND.DROP_COPY;
			else event.detail = DND.DROP_NONE;
		} else if(event.detail == DND.DROP_NONE)event.detail=DND.DROP_COPY;
	}
	
	public void drop(DropTargetEvent event){
		Widget target = event.item;
		Object myObject = target.getData();
		if(myObject == null)return;
		if(!testIsSupported(myObject,event))return;
		
		if(myObject instanceof ResourceExplorerResourceItem){
			processResourceDrop(((ResourceExplorerResourceItem)myObject).getResource(),event);
			return;
		}
		
		if(myObject instanceof ResourceExplorerFavGroupItem){
			processFavouriteDrop(event);
		}
		
		ResourceExplorerObjectTypeItem item = (ResourceExplorerObjectTypeItem)myObject;
		IResourceExtensionProcessorSource ot = (IResourceExtensionProcessorSource)item.getAssociatedObject();
		
		ot.getDropTargetProcessor().processDrop(event);
	}

	private void processFavouriteDrop(DropTargetEvent event) {
		if(event.data == null)return;
		if(ClipboardHandler.TRANSFER.isSupportedType(event.currentDataType)){
			Resource r = null;
			if(event.data instanceof Resource) r = (Resource)event.data;
			else if(event.data instanceof LinkableObject)r = ((LinkableObject)event.data).getSurrogateFor();
			else if(event.data instanceof IHasResource)r = ((IHasResource)event.data).getResource();
			if(r == null)return;
			if(Favourite.findFromResource(r) != null)return;
			view.getCommandStack().execute(new AddToFavouritesCommand(r));
		}
	}

	private void processResourceDrop(Resource target, DropTargetEvent event) {
		if(ClipboardHandler.TRANSFER.isSupportedType(event.currentDataType)){
			handlePlinyData(target, event.data);
		}
		else if(TextTransfer.getInstance().isSupportedType(event.currentDataType)){
			handleText(target, event.data);
		}
	}
	
	private void handleText(Resource target, Object data) {
		Vector rslt = ClipboardHandler.getDefault().processTextObject(data);
		handlePlinyData(target, rslt);
	}

	private void handlePlinyData(Resource target, Object data){
		Collection dataIn = null;
		if(data instanceof Collection)dataIn = (Collection)data;
		else {
		   dataIn = new Vector();
		   dataIn.add(data);
		}
		view.getCommandStack().execute(new PastePlinyCommand(target, dataIn));
	}

}
