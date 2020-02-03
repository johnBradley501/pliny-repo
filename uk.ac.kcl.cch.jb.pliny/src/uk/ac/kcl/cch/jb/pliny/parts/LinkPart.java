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

package uk.ac.kcl.cch.jb.pliny.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import uk.ac.kcl.cch.jb.pliny.commands.DeleteLinkCommand;
import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.Link;

/**
 * proves the GEF editPart that manages the display of the connection
 * lines between reference objects in the reference/annotation display.
 * Model data for these is the
 * {@link uk.ac.kcl.cch.jb.pliny.model.Link Link} object.
 * <p>
 * As well as tracking any change in the connections in the model, this
 * code also tracks its Link's
 * {@link uk.ac.kcl.cch.jb.pliny.model.LOType LOType} for changes in
 * associated Type, and changes in the current type's colour parameters.
 * 
 * 
 * @author John Bradley
 *
 */
public class LinkPart extends AbstractConnectionEditPart implements
		PropertyChangeListener {
	
	IHasConnectionLayer myRootPart;

	public LinkPart(IHasConnectionLayer myRootPart, Link link) {
		super();
		setModel(link);
		this.myRootPart = myRootPart;
	}
	
	public Link getLink(){
		return (Link)getModel();
	}
	
	protected IFigure createFigure() {
		PolylineConnection myFigure = new PolylineConnection();
		myFigure.setTargetDecoration(new PolygonDecoration()); // arrow at target endpoint
		myFigure.setForegroundColor(getLink().getLoType().getTitleBackColour());
		myFigure.setLineWidth(2);
		return myFigure;
	}

	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE,
				new ConnectionEndpointEditPolicy());
		installEditPolicy(EditPolicy.CONNECTION_ROLE, new ConnectionEditPolicy() {
			protected Command getDeleteCommand(GroupRequest request) {
				return new DeleteLinkCommand((Link)getHost().getModel());
			}
		});
	}
	
	/*
	 * the following block of code makes use of the connection layer
	 * provided by my code rather than GEF's  . jb
	 */
	
	protected ConnectionLayer getTheConnectionLayer(){
		return myRootPart.getMyConnectionLayer();
	}
	
	/**
	 * Activates the Figure representing this connection
	 * object.  This overrides the normal process
	 * to achieve the layering of the connections
	 * in the contained object, rather than at the very
	 * top.
	 * 
	 * @see #deactivate()
	 */
	protected void activateFigure() {
		if(myRootPart == null)super.activateFigure();
		else getTheConnectionLayer().add(getFigure());
	}

	/**
	 * Deactivates the Figure representing this, by removing
	 * it from the connection layer, and resetting the 
	 * source and target connections to <code>null</code>.
	 */
	protected void deactivateFigure() {
		if(myRootPart == null)super.deactivateFigure();
		else {
		   getTheConnectionLayer().remove(getFigure());
		   getConnectionFigure().setSourceAnchor(null);
		   getConnectionFigure().setTargetAnchor(null);
		}
	}
	
	private LOType myType = null;

	/**
	 * Upon activation, attach to the model element as a property change listener.
	 */
	public void activate() {
		if (!isActive()) {
			super.activate();
			getLink().addPropertyChangeListener(this);
			myType = getLink().getLoType();
			if(myType != null)
				myType.addPropertyChangeListener(this);
		}
	}

	/**
	 * Upon deactivation, detach from the model element as a property change listener.
	 */
	public void deactivate() {
		if (isActive()) {
			super.deactivate();
			getLink().removePropertyChangeListener(this);
			if(myType != null)
				myType.removePropertyChangeListener(this);
		}
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		String pName = arg0.getPropertyName();
		if(pName == Link.TYPEKEY_PROP){
			if(myType != null)
				myType.removePropertyChangeListener(this);
			myType = getLink().getLoType();
			if(myType != null)
			    myType.addPropertyChangeListener(this);
			getFigure().setForegroundColor(myType.getTitleBackColour());
			return;
		} else if(pName == LOType.TITLEBACKCOLOURINT_PROP){
			getFigure().setForegroundColor(myType.getTitleBackColour());
		} else if(pName == Link.FROMLINKKEY_PROP || pName == Link.TOLINKKEY_PROP){
		    refreshSourceAnchor();
		    refreshTargetAnchor();
		}
	}
}
