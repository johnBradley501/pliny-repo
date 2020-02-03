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
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;

import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.Link;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.policies.LOLinkEditPolicy;
import uk.ac.kcl.cch.jb.pliny.policies.LinkableObjectComponentsEditPolicy;
import uk.ac.kcl.cch.rdb2java.dynData.FKReferenceList;

/**
 * provides base support for GEF editparts that derive from 
 * {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject LinkableObject}s, 
 * including (i) reference objects (both scaleable and unscaleable), and
 * anchor objects.
 * <p>
 * This abstract base class provides:
 * <ul>
 * <li>definition of the objects as GEF Nodes that allow for Connections,
 * <li>common edit policies that provide functions needed for the three
 * kind of objects derived from it (specifically the policies
 * {@link uk.ac.kcl.cch.jb.pliny.policies.LOLinkEditPolicy LOLinkEditPolicy} which
 * provides support for connections, and 
 * {@link uk.ac.kcl.cch.jb.pliny.policies.LinkableObjectComponentsEditPolicy LinkableObjectComponentsEditPolicy}
 * which supports deletion of the object) and
 * <li>activition support to provide update to relevant model changes for the
 * linked surrogate 
 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource Resource}
 * (if any -- an anchor doesn't have one), for
 * the editPart model's LinkableObject itself and for the associated
 * {@link uk.ac.kcl.cch.jb.pliny.model.LOType LOType}
 * (which provides information about colours for display among other things),
 * and tracking support for model changing information of interest.
 * </ul>
 * 
 * @author John Bradley
 *
 */

public abstract class LinkableObjectBasePart extends OrderableAbstractGraphicalEditPart
		implements NodeEditPart, IHasIsContainmentLoop, PropertyChangeListener {

	private ConnectionAnchor anchor;
	private Resource myResource = null;
	private LOType myType;

	public LinkableObjectBasePart(LinkableObject model) {
		super();
		setModel(model);
		myType = model.getLoType();
	}
	
	public LinkableObject getLinkableObject(){
		return (LinkableObject)getModel();
	}
	
	public LOType getMyType(){
		return myType;
	}

	protected abstract IFigure createFigure();

	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new LOLinkEditPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new LinkableObjectComponentsEditPolicy());
	}
	
	
	/**
	 * Upon activation, attach to the model element as a property change listener.
	 * <p>This part has to listen to both its LinkableObject, but also the connected
	 * resource, since the resource provides the editable title that this part
	 * displays.
	 */
	public void activate() {
		if (!isActive()) {
			super.activate();
			getLinkableObject().addPropertyChangeListener(this);
			myResource = getLinkableObject().getSurrogateFor();
			if(myResource != null)
			   myResource.addPropertyChangeListener(this);
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
			getLinkableObject().removePropertyChangeListener(this);
			if(myResource != null)
				myResource.removePropertyChangeListener(this);
			if(myType != null)
			    myType.removePropertyChangeListener(this);
		}
	}
	/**
	 * Can check if a containment loop is happening   ..jb
	 */
	public boolean isContainmentLoop(Resource o){
		//if(getLinkableObject().getSurrogateFor().getALID()==o.getALID())return true;
		Object par = getParent();
		if(par instanceof IHasIsContainmentLoop)
			return ((IHasIsContainmentLoop)par).isContainmentLoop(o);
		return false;
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		String pName = arg0.getPropertyName();
		if((pName == LinkableObject.ISOPEN_PROP) ||
		  (pName == LinkableObject.SHOWINGMAP_PROP) ||
		  (pName == LinkableObject.POSITION_PROP) ||
		  (pName == Resource.NAME_PROP)){
		  refreshVisuals();
		  if(pName == LinkableObject.SHOWINGMAP_PROP) refreshChildren();
		}
		else if((pName == LinkableObject.LINKEDFROM_PROP) ||
		  (pName == LinkableObject.LINKEDTO_PROP)){
		   //filteredListFrom = null;
		   //filteredListTo = null;
           refreshTargetConnections();
           refreshSourceConnections();
		} else if(pName == LinkableObject.TYPEKEY_PROP){
			if(myType != null)
				myType.removePropertyChangeListener(this);
			myType = getLinkableObject().getLoType();
			if(myType != null)
			    myType.addPropertyChangeListener(this);
			setColourFromType(myType);
		} else if(pName == LOType.BODYBACKCOLOURINT_PROP ||
				pName == LOType.BODYFORECOLOURINT_PROP ||
				pName == LOType.TITLEBACKCOLOURINT_PROP ||
				pName == LOType.TITLEFORECOLOURINT_PROP){
			setColourFromType(myType);
		}
	}
	
	//private Vector filteredListFrom = null;
	//private Vector filteredListTo = null;
	
/*
	private Vector filterForRects(FKReferenceList list){
		Vector vList = list.getItems();
		Vector rslt = new Vector(vList.size());
		Iterator it = vList.iterator();
		while(it.hasNext()){
			Link lnk = (Link)it.next();
			LinkableObject from = lnk.getFromLink();
			LinkableObject to = lnk.getToLink();
			//if(from != null && to != null && from.getPosition().startsWith("rect:") && to.getPosition().startsWith("rect:")){
			if((from == null || from.getPosition().startsWith("rect:")) && (to == null || to.getPosition().startsWith("rect:"))){
				rslt.add(lnk);
			}
		}
		return rslt;
	}
	*/
	
	protected List getModelSourceConnections(){
		//if(filteredListFrom == null){
		//	filteredListFrom = filterForRects(getLinkableObject().getLinkedFrom());
		//}
		//return filteredListFrom;
		//return filterForRects(getLinkableObject().getLinkedFrom());
		return new Vector(getLinkableObject().getLinkedFrom().getItems());
	}
	
	protected List getModelTargetConnections(){
		//if(filteredListTo == null){
		//	filteredListTo = filterForRects(getLinkableObject().getLinkedTo());
		//}
		//return filteredListTo;
		//return filterForRects(getLinkableObject().getLinkedTo());
		return new Vector(getLinkableObject().getLinkedTo().getItems());
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
	
	public abstract void refreshVisuals();
	
	public abstract void setColourFromType(LOType type);
}
