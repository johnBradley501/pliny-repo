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

import uk.ac.kcl.cch.jb.pliny.figures.MapContentFigure;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.MapContentHolder;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.parts.IHasIsContainmentLoop;
import uk.ac.kcl.cch.jb.pliny.policies.LOLinkEditPolicy;
import uk.ac.kcl.cch.jb.pliny.policies.ResourceObjectsContainerEditPolicy;
import uk.ac.kcl.cch.jb.pliny.policies.ResourceObjectsXYLayoutPolicy;

/**
 * this GEF editPart manages its associated reference object's content area
 * when it is displaying the associated reference area.
 * Model data for this area comes from the
 * {@link uk.ac.kcl.cch.jb.pliny.model.MapContentHolder MapContentHolder}
 * which acts as a holder for the 
 * {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject LinkableObject} which owns the
 * appropriate reference object.
 * 
 * @author John Bradley
 *
 */

public class MapContentHolderPart extends ParentOfOrderableAbstractGraphicalEditPart 
implements IHasConnectionLayer, IHasIsContainmentLoop, PropertyChangeListener{

	public MapContentHolderPart( MapContentHolder model) {
		super();
		setModel(model);
		// TODO Auto-generated constructor stub
	}
	
	public MapContentHolder getMyHolder(){
		return (MapContentHolder)getModel();
	}
	
	public Resource getMyResource(){
		return getMyHolder().getSurrogate();
	}

	protected IFigure createFigure() {
		return new MapContentFigure();
	}
	
	protected MapContentFigure getMyFigure(){
		return (MapContentFigure)getFigure();
	}

	public IFigure getContentPane(){
		return getMyFigure().getContentsFigure();
	}

	public List getModelChildren(){
		Vector children = new Vector(getMyResource().getMyDisplayedItems().getItems());
		//addReferencerList(children);
		return children;
	}

	protected void createEditPolicies() {
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new ResourceObjectsXYLayoutPolicy());
		installEditPolicy(EditPolicy.CONTAINER_ROLE, new ResourceObjectsContainerEditPolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new LOLinkEditPolicy());
	}

	public ConnectionLayer getMyConnectionLayer() {
		return getMyFigure().getMyConnectionLayer();
	}

	/**
	 * Upon activation, attach to the model element as a property change listener.
	 */
	public void activate() {
		if (!isActive()) {
			super.activate();
			getMyResource().addPropertyChangeListener(this);
		}
	}

	/**
	 * Upon deactivation, detach from the model element as a property change listener.
	 */
	public void deactivate() {
		if (isActive()) {
			super.deactivate();
			getMyResource().removePropertyChangeListener(this);
			//myFigure.dispose();
		}
	}
	
	public void propertyChange(PropertyChangeEvent arg0) {
		String pName = arg0.getPropertyName();
		if(pName == Resource.MYDISPLAYEDITEMS_PROP)
			refreshChildren();
	}
	
	/**
	 * Can check if a containment loop is happening   ..jb
	 */
	public boolean isContainmentLoop(Resource o){
		if(o == null)return false;
		if(getMyHolder().getSurrogate().getALID()==o.getALID())return true;
		Object par = getParent();
		if(par instanceof IHasIsContainmentLoop)
			return ((IHasIsContainmentLoop)par).isContainmentLoop(o);
		return false;
	}

}
