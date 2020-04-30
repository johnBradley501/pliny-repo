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
import java.util.List;
import java.util.Vector;

import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;

import uk.ac.kcl.cch.jb.pliny.figures.RootFigure;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.policies.ResourceObjectsContainerEditPolicy;
import uk.ac.kcl.cch.jb.pliny.policies.ResourceObjectsXYLayoutPolicy;
/**
 * the GEF root EditPart for a
 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource Resource}'s reference/annotation area.
 * 
 * @author John Bradley
 *
 */
public class RootResourcePart extends
		ParentOfOrderableAbstractGraphicalEditPart 
		implements PropertyChangeListener, IHasConnectionLayer, IHasIsContainmentLoop{
	
	//private RootFigure myFigure;
	
	protected RootResourcePart(){
		
	}

	public RootResourcePart(Resource model) {
		setModel(model);
	}
	
	public Resource getResource(){
		return (Resource)getModel();
	}

	protected IFigure createFigure() {
		return new RootFigure();
	}

	public RootFigure getMyFigure(){
		return (RootFigure)getFigure();
	}
	
	/**
	 * @return the Content pane for adding or removing child figures
	 */
	public IFigure getContentPane(){
		return getMyFigure().getContentsFigure();
	}
	
	public List getModelChildren(){
		if(getResource() == null)return new Vector();
		Vector children = new Vector(getResource().getMyDisplayedItems().getItems());
		// addReferencerList(children);
		return children;
	}

	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new ResourceObjectsXYLayoutPolicy());
		installEditPolicy(EditPolicy.CONTAINER_ROLE, new ResourceObjectsContainerEditPolicy());
	}
	

	/**
	 * Upon activation, attach to the model element as a property change listener.
	 */
	public void activate() {
		if (!isActive()) {
			super.activate();
			getResource().addPropertyChangeListener(this);
		}
	}

	/**
	 * Upon deactivation, detach from the model element as a property change listener.
	 */
	public void deactivate() {
		if (isActive()) {
			super.deactivate();
			getResource().removePropertyChangeListener(this);
			//myFigure.dispose();
		}
	}
	
	/**
	 * Can check if a containment loop is happening   ..jb
	 */
	public boolean isContainmentLoop(Resource o){
		if(o == null){
			return false;
		}
		return getResource().getALID()==o.getALID();
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		refreshChildren();
	}

	public ConnectionLayer getMyConnectionLayer() {
		return getMyFigure().getMyConnectionLayer();
	}

}
