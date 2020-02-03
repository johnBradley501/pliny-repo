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

package uk.ac.kcl.cch.jb.pliny.containmentView.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Vector;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentItem;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * this is the GEF MVC Controller for a ContainmentView models'
 * {@link uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentItem ContainmentItem}.
 * This item is a GEF <code>NodeEditPart</code>, since it has connections (links) coming
 * in and out of it.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.containmentView.parts.ContainerItemSelectionEditPolicy
 * 
 * @author John Bradley
 *
 */

public class ContainmentItemPart extends AbstractGraphicalEditPart 
implements NodeEditPart,  PropertyChangeListener{
	private ConnectionAnchor anchor;

	public ContainmentItemPart(ContainmentItem item) {
		setModel(item);
	}
	
	public ContainmentItem getContainmentItem(){
		return (ContainmentItem)getModel();
	}

	protected IFigure createFigure() {
		IFigure figure = new ContainmentItemFigure(getContainmentItem());
		//figure.setBounds(new Rectangle(-1,-1,100,18));
		return figure;
	}
	
	protected ContainmentItemFigure getContainmentItemFigure(){
		return (ContainmentItemFigure)getFigure();
	}

	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new ContainerItemSelectionEditPolicy());
	}
	
	protected List getModelSourceConnections(){
		Vector rslt = new Vector(getContainmentItem().getToSet());
		//System.out.println("SourceConnections item: "+getContainmentItem());
		//System.out.println("---- ToSet: "+rslt);
		return rslt;
	}
	
	protected List getModelTargetConnections(){
		Vector rslt = new Vector(getContainmentItem().getFromSet());
		//System.out.println("TargetConnections item: "+getContainmentItem());
		//System.out.println("---- FromSet: "+rslt);
		return rslt;
	}
	
	private ConnectionAnchor getConnectionAnchor() {
		if(anchor == null) anchor = new ChopboxAnchor(getFigure());
		return anchor;
	}

	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		return getConnectionAnchor();
	}

	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		return getConnectionAnchor();
	}

	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return getConnectionAnchor();
	}

	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return getConnectionAnchor();
	}

	public void openFullEditor() {
		//IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getSite().getPage(); 
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(); 
		//IWorkbenchPage page = ((GenericPartBasedEditDomain)this.getViewer().getEditDomain()).getPart().
		//                       getSite().getPage();
		try {
			getContainmentItem().getResource().openEditor(page, 0);
		} catch (PartInitException e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), 
					"Opening Failure",
			"This resource could not be opened for editing.");

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void activate() {
		if(isActive())return;
		super.activate();
		getContainmentItem().addPropertyChangeListener(this);
	}
	
	public void deactivate() {
		if(!isActive())return;
		super.deactivate();
		getContainmentItem().removePropertyChangeListener(this);
	}
	
	// needed for direct textual editing:
	private boolean directEditHitTest(Point requestLoc, IFigure figure)
	{
		//IFigure figure = getFigure();
		//IFigure figure = getMyFigure().getTitleLabel();
		figure.translateToRelative(requestLoc);
		if (figure.containsPoint(requestLoc))
			return true;
		return false;
	}
	
	public void performRequest(Request request){
		if(request.getType() == RequestConstants.REQ_OPEN){
			if(request instanceof SelectionRequest /* DirectEditRequest */){
				SelectionRequest myRequest = (SelectionRequest)request;
			   if(directEditHitTest(myRequest.getLocation().getCopy(),
		          getContainmentItemFigure().getTheIdentifierIconLabel()))
				   openFullEditor();
			   else super.performRequest(request);
			   }
			else super.performRequest(request);
		}
		else super.performRequest(request);
	}
	
	public void propertyChange(PropertyChangeEvent arg0) {
		String propName = arg0.getPropertyName();
		if(propName == Resource.NAME_PROP){
			getContainmentItemFigure().setName(getContainmentItem().getResource().getName());
		} else if(propName == Resource.MYSURROGATES_PROP || propName == Resource.MYDISPLAYEDITEMS_PROP){
	           refreshTargetConnections();
	           refreshSourceConnections();
			   //((ContainmentSetPart)getParent()).refresh();
		}
	}
}
