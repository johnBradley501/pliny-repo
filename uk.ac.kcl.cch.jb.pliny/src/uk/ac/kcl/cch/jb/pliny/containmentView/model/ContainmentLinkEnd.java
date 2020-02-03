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

package uk.ac.kcl.cch.jb.pliny.containmentView.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.rdb2java.dynData.PropertyChangeObject;

/**
 * part of the data model for the ContainmentView that represents the ends
 * (arrowheads) of the links between ContainmentItems.
 * <p>
 * As representation of the ends of the links, it is natural that it track
 * changes in the corresponding 
 * {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject LinkableObject} that
 * indicates when a new colour for the arrow is neede.  There are two
 * situations that are tracked by generating events:
 * <ul>
 * <li><code>COLOUR_EVENT</code>: when a colour change is needed because
 * a colour associated with a type has changed.
 * <li><code>NEW_TYPE_EVENT</code>: when a colour change is needed because
 * the type associated with the LinkableObject has changed.  This event might
 * also be important is the new type represents a move from a link that
 * should not be displayed (because the LOType has been excluded from display)
 * or vice-versa.
 * </ul>
 * <p> To track these two events, this object listens to both its LinkableObject
 * and to the 
 * {@link uk.ac.kcl.cch.jb.pliny.model.LOType LOType} associated with it.
 * 
 * @author John Bradley
 */
public class ContainmentLinkEnd extends PropertyChangeObject 
implements PropertyChangeListener {
	public static final String COLOUR_EVENT="ContainmentLinkEnd.Colours";
	public static final String NEW_TYPE_EVENT="ContainmentLinkEnd.newType";

	LinkableObject myObject = null;
	private LOType myType = null;
	
	/**
	 * builds a instance of this model object for the given 
	 * {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject LinkableObject}.
	 * 
	 * @param myObject LinkableObject object this instance should be associated with.
	 */
	public ContainmentLinkEnd(LinkableObject myObject) {
		super();
		this.myObject = myObject;
		myObject.addPropertyChangeListener(this);
		myType = myObject.getLoType();
		if(myType != null)
			myType.addPropertyChangeListener(this);
		
	}
	
	public void dispose(){
		if(myType != null)
			myType.removePropertyChangeListener(this);
		myObject.removePropertyChangeListener(this);
	}
	
	public Color getMyColour(){
		if(myType == null)return ColorConstants.black;
		return myType.getTitleBackColour();
	}
	
	public LinkableObject getMyLinkableObject(){
		return myObject;
	}
	
	/**
	 * this method is called whenever a property associated
	 * with either the LinkableObject or its LOType is changed.
	 */

	public void propertyChange(PropertyChangeEvent arg0) {
		String propName = arg0.getPropertyName();
		if(propName == LinkableObject.TYPEKEY_PROP){
			if(myType == myObject.getLoType())return;
			myType.removePropertyChangeListener(this);
			LOType myOldType = myType;
			myType = myObject.getLoType();
			myType.addPropertyChangeListener(this);
			this.firePropertyChange(NEW_TYPE_EVENT, myOldType, myType);
			return;
		}
		if(propName == LOType.TITLEFORECOLOURINT_PROP){
			this.firePropertyChange(COLOUR_EVENT);
		}
	}
	
	public String toString(){
		return "LinkEnd ["+myObject+"]";
	}

}
