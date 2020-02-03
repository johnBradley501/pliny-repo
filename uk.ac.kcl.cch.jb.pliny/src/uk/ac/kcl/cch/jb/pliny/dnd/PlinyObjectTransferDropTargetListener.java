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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.dnd.AbstractTransferDropTargetListener;
import org.eclipse.gef.editparts.AbstractEditPart;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;

import uk.ac.kcl.cch.jb.pliny.IResourceDrivenPart;
import uk.ac.kcl.cch.jb.pliny.commands.CreateLinkableObjectCommand;
import uk.ac.kcl.cch.jb.pliny.commands.SetItemsToTypeCommand;
import uk.ac.kcl.cch.jb.pliny.factories.DnDFactory;
import uk.ac.kcl.cch.jb.pliny.figures.TopPanel;
import uk.ac.kcl.cch.jb.pliny.model.Favourite;
import uk.ac.kcl.cch.jb.pliny.model.IHasLoType;
import uk.ac.kcl.cch.jb.pliny.model.IHasResource;
import uk.ac.kcl.cch.jb.pliny.model.IHoldsLinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.MapContentHolder;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.parts.LinkPart;
import uk.ac.kcl.cch.jb.pliny.parts.LinkableObjectPart;

/**
 * handles the target end of inter-Pliny DnD, when the target
 * is a reference/annotation area.  The code will translate all
 * DnD requests into a request for a Pliny
 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource Resource}.
 * 
 * @author John Bradley
 *
 */

public class PlinyObjectTransferDropTargetListener extends
		AbstractTransferDropTargetListener {
	
	private DnDFactory theFactory = new DnDFactory();
	//private IResourceDrivenPart rPart = null;
	//private Set nonReceivers = null;
	private Resource oldSource =  null;
	
	private static int enabledCount = 0;
	//private static int count=0;

	public PlinyObjectTransferDropTargetListener(EditPartViewer viewer/*, IResourceDrivenPart rPart*/) {
		super(viewer, ClipboardHandler.TRANSFER);
		//this.rPart = rPart;
	}

	//public PlinyObjectTransferDropTargetListener(EditPartViewer viewer,
	//		Transfer xfer) {
	//	super(viewer, xfer);
	//}
	
	// possible help with DnD then immediate Direct Edit:
	// http://www.eclipse.org/forums/index.php/m/249686/?srch=GEF+DnD+direct+edit#msg_249686
	
	private Resource getTargetResource(){
		//Resource rslt = null;
		if(getTargetEditPart() == null)updateTargetEditPart();
		Object targetModel = this.getTargetEditPart().getModel();
		if(targetModel instanceof Resource)return (Resource)targetModel;
		if(targetModel instanceof LinkableObject)return ((LinkableObject)targetModel).getSurrogateFor();
		if(targetModel instanceof Favourite)return ((Favourite)targetModel).getResource();
		if(targetModel instanceof IHoldsLinkableObject) return ((IHoldsLinkableObject)targetModel).getSurrogate();
		if(targetModel instanceof IHasResource) return ((IHasResource)targetModel).getResource();
		return null;
	}
	
	protected boolean testCanDrop(){
		Resource targetResource = getTargetResource();
		if(targetResource == null)return false;
		//Object source = ClipboardHandler.TRANSFER.getObject();
		//if(source == null)source = PlinyDragSourceListener.getCurrentObject();
		Object source = PlinyDragSourceListener.getCurrentObject();
		if(source == null)source = ClipboardHandler.TRANSFER.getObject();
		//System.out.println("PlinyObjectTransferDropTargetListener:testCanDrop ("+(enabledCount)+"): source:"+source+", targetResource="+targetResource);
		//if(source == null)return true; // allows DnDResourceHolder to work j.b.
		if(source == null)return false;
		Resource sourceHolder = targetResource;
		if(source instanceof LinkableObject){
			LinkableObject lo = (LinkableObject)source;
			sourceHolder = lo.getDisplayedIn();
			Resource sourceResource = lo.getSurrogateFor();
			if(targetResource.equals(sourceResource))return false;
		}else if(source instanceof Resource){
			sourceHolder = (Resource)source;
		//}else if(source instanceof Favourite){ // Favourite is an IHasResource
		//	sourceHolder = ((Favourite)source).getResource();
		}else if(source instanceof IHasResource){
			sourceHolder = ((IHasResource)source).getResource();
		}else if(source instanceof LOType){
			return true;
		}
		//System.out.println("textCanDrop2 sourceResource: "+sourceHolder);
		if(sourceHolder == null)return false;
		if(targetResource.equals(sourceHolder))return false;
		//if(!sourceResource.equals(oldSource))nonReceivers = null;
		oldSource = sourceHolder;
		
		return true;
/*      the following code was supposed to not allow an object to be dragged
 *      to a holder that already held it.  I've decided to allow this.   jb
 *      
		if(nonReceivers == null){
			nonReceivers = new HashSet();
			Iterator it = sourceResource.getMySurrogates().getItems().iterator();
			while(it.hasNext()){
				LinkableObject lo = (LinkableObject)it.next();
				if(lo.getDisplayedIn() != null)nonReceivers.add(lo.getDisplayedIn());
				//System.out.println("   displayedIn: "+lo.getDisplayedIn()+", size: "+nonReceivers.size());
			}
		}
		
		boolean rslt = !nonReceivers.contains(targetResource);

		return rslt;
*/
	}
	
	public void dragEnter(DropTargetEvent event){
		//System.out.println("dragEnter");
		//nonReceivers = null;
		super.dragEnter(event);
	}

	public boolean isEnabled(DropTargetEvent event){
		//System.out.println("isEnabled entered: "+(++enabledCount));
		//System.out.flush();
		if(!super.isEnabled(event))return false;
		boolean rslt = testCanDrop();
		return rslt;
	}

	protected void handleDragOver() {
		   getCurrentEvent().detail = DND.DROP_COPY;
		   //System.out.println("handleDragOver, invoked");
		   super.handleDragOver();
		}
	
	//private void setSize(CreateRequest myRequest, Object myObject){
		/* this doesn't seem to work -- tracking size according to 
		 * previous item dragged in rather than current.
		 *
		if((myObject != null) && (myObject instanceof LinkableObject)){
			LinkableObject surr = (LinkableObject)myObject;
			Dimension size = new Dimension(surr.getDisplayRectangle().getSize());
			if(!surr.getIsOpen())size.height = 18;
			//System.out.println("PlinyObjectTransferDropTargetListener: surr:"+surr+", isOpen: "+surr.getIsOpen()+
			//		", size: "+size);
		    myRequest.setSize(size);
		} else {
			Dimension size = CreateLinkableObjectCommand.DEFAULT_DIMENSION;
			if((myObject != null) && (myObject instanceof Note))
				size = CreateLinkableObjectCommand.DEFAULT_NOTE_DIMENSION;
			myRequest.setSize(size);
		} */
	//	myRequest.setSize(CreateLinkableObjectCommand.DEFAULT_DIMENSION);
	//}
	
	protected Request createTargetRequest() {
		   CreateRequest request = new CreateRequest();
		   request.setFactory(theFactory);
		   //System.out.println("createTargetRequest event.data: "+getCurrentEvent().data);
		   //if((getCurrentEvent() != null) && (getCurrentEvent().data!= null)){
		   //   theFactory.setupObject(getCurrentEvent().data);
		   //   setSize(request, getCurrentEvent().data);
		   //}
		   request.setSize(CreateLinkableObjectCommand.DEFAULT_DIMENSION);
		   return request;
		}

	protected void updateTargetRequest() {
		Request theRequest = getTargetRequest();
		CreateRequest myRequest = (CreateRequest)theRequest;
		myRequest.setLocation(getDropLocation());
		//Object myObject = myRequest.getNewObject();
		//setSize(myRequest, myObject);
		myRequest.setSize(CreateLinkableObjectCommand.DEFAULT_DIMENSION);
	}
	
	public void drop(DropTargetEvent event) {
		Object source=getCurrentEvent().data;
		EditPart targetEditPart=getViewer().findObjectAt(getDropLocation());
		if((source instanceof LOType) && 
				((targetEditPart.getModel() instanceof IHasLoType))
				/*((targetEditPart instanceof LinkableObjectPart) || targetEditPart instanceof LinkPart)*/){
			handleTypeDropOnLo(event, (LOType)source, (AbstractEditPart)targetEditPart);
			return;
		}
		if(ClipboardHandler.TRANSFER.isSupportedType(event.currentDataType)){
			if(testCanDrop())
			  theFactory.setupObject(event.data);
		}
		//System.out.println("PlinyObjectTransfer#drop: getTargetEditPart:"+getTargetEditPart());
		//nonReceivers = null;
		//System.out.println("PlinyObjectTransfer#drop: TopPanel.getCurrentObject: "+TopPanel.getCurrentObject());
		//System.out.println("PlinyObjectTransfer#drop: findObjectAt:"+getViewer().findObjectAt(getDropLocation()));
		super.drop(event);
		ClipboardHandler.TRANSFER.setObject(null);
		TopPanel.clearCurrentObject();
	}
	
	private void handleTypeDropOnLo(DropTargetEvent event, LOType loSource,
			AbstractEditPart lop) {
		// code surrounding central code is copied from super.drop(event)
		setCurrentEvent(event);
		eraseTargetFeedback();
		
		Vector items = new Vector();
		items.add(lop.getModel());
		getViewer().getEditDomain().getCommandStack().execute(
			new SetItemsToTypeCommand(items, loSource)
        );
		
		unload(); // copied from super.drop(event)
		
	}

	/*
	protected void handleDrop() {
		DropTargetEvent event = getCurrentEvent();
		if(ClipboardHandler.TRANSFER.isSupportedType(event.currentDataType)){
			if(testCanDrop())
			  theFactory.setupObject(event.data);
		}
		nonReceivers = null;
		super.handleDrop();
		ClipboardHandler.TRANSFER.setObject(null);
	}
	*/

	// removed because seemed to cause problems with dragging between open editor frames.
	//protected void unload(){
	//	super.unload();
	//	//nonReceivers = null;
	//	PlinyDragSourceListener.setCurrentObject(null);
	//}

}
