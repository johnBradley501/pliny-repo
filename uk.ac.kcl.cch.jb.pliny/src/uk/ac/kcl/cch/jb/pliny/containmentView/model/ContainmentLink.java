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
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.rdb2java.dynData.PropertyChangeObject;

/**
 * this model class for the ContainmentView represents a link between two
 * ContainmentItems.  <i>Note</i> that a single link is created when there is one or
 * more Pliny model 
 * {@link uk.ac.kcl.cch.jb.pliny.model.Link Link}s between the same two resources.  If at least
 * link goes in each direction, then there is still only one
 * ContainmentLink created, but it has recorded that there are Links between 
 * the Resources it connects in both directions.
 * <p>
 * This class implements {@link uk.ac.kcl.cch.rdb2java.dynData.PropertyChangeObject PropertyChangeObject},
 * and so can so can be listened to.  It raises the following events:
 * <ul>
 * <li><code>BOTHWAY_EVENT</code>: when the link is first recognised
 * as actually working in both directions.
 * <li><code>COLOUR_EVENT</code>: when the end of the link needs to
 * change colour.
 * <li><code>ENDCHANGE_EVENT</code>: when either end of the object
 * needs to change.
 * </ul>
 * 
 * @see uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentSet
 * @see uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentItem
 * @see uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentLinkEnd
 * 
 * @author John Bradley
 */

public class ContainmentLink extends PropertyChangeObject 
implements PropertyChangeListener {
	
	public static final String BOTHWAY_EVENT="ContainmentLink.BothWay";
	public static final String COLOUR_EVENT="ContainmentLink.Colours";
	public static final String ENDCHANGE_EVENT="ContainmentLink.EndChange";
	
	private ContainmentItem from;
	private ContainmentItem to;
	private ContainmentLinkEnd sourceEnd = null;
	private ContainmentLinkEnd targetEnd = null;
	private boolean bothWay;
    private ContainmentSet containmentSet;

    /**
     * constructs this object for a given pair of 
     * {@link uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentItem ContainmentItem}s that
     * belong in the given 
     * {@link uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentSet ContainmentSet}.
     * 
     * @param from ContainmentItem source end of the link
     * @param to ContainmentItem target end of the link
     * @param containmentSet ContainmentSet set owner.
     */
    
	public ContainmentLink(ContainmentItem from, ContainmentItem to, ContainmentSet containmentSet) {
		this.from = from;
		this.to = to;
		this.containmentSet = containmentSet;
		bothWay = false;
	}
	
	public Color getSourceColour(){
		if(sourceEnd == null)return ColorConstants.white;
		return sourceEnd.getMyColour();
	}
	
	public ContainmentLinkEnd getSourceEnd(){
		return sourceEnd;
	}
	
	public ContainmentLinkEnd getTargetEnd(){
		return targetEnd;
	}
	
	public Color getTargetColour(){
		if(targetEnd == null)return ColorConstants.white;
		return targetEnd.getMyColour();
	}
	
	public void setLinkableObject(LinkableObject obj){
		Resource surr = obj.getSurrogateFor();
		if(surr == null)return;
		if(surr.equals(to.getResource())){
			if(targetEnd != null){
				if(surr.equals(targetEnd.getMyLinkableObject().getSurrogateFor())) return;
				throw new RuntimeException("Second targetEnd set in ContainmentLink");
			}
			targetEnd = new ContainmentLinkEnd(obj);
			targetEnd.addPropertyChangeListener(this);
		}
		else{
			if(sourceEnd != null){
				if(surr.equals(sourceEnd.getMyLinkableObject().getSurrogateFor())) return;
				throw new RuntimeException("Second sourceEnd set in ContainmentLink");
			}
			sourceEnd = new ContainmentLinkEnd(obj);
			sourceEnd.addPropertyChangeListener(this);
		}
        this.firePropertyChange(ENDCHANGE_EVENT);
	}
	
	public void removeLinkableObject(LinkableObject obj){
		Resource surr = obj.getSurrogateFor();
		if(surr == null)return;
		if(surr.equals(to.getResource())){
			if(targetEnd == null)return; //throw new RuntimeException("targetEnd already gone");
			targetEnd.removePropertyChangeListener(this);
			targetEnd.dispose();
			targetEnd = null;
		} else {
			if(sourceEnd == null)return; //throw new RuntimeException("sourceEnd already gone");
			sourceEnd.removePropertyChangeListener(this);
			sourceEnd.dispose();
			sourceEnd = null;
		}
        this.firePropertyChange(ENDCHANGE_EVENT);
	}
	
	public void dispose(){
		if(sourceEnd != null){
			sourceEnd.removePropertyChangeListener(this);
			sourceEnd.dispose();
		}
		if(targetEnd != null){
			targetEnd.removePropertyChangeListener(this);
			targetEnd.dispose();
		}
	}
	
	public void setBothWay(boolean val){
		if(bothWay == val)return;
		
		bothWay = val;
        this.firePropertyChange(BOTHWAY_EVENT);
	}
	
	public boolean getBothWay(){
		return bothWay;
	}
	
	public ContainmentItem getFrom(){
		return from;
	}
	
	public ContainmentItem getTo(){
		return to;
	}

	/**
	 * this class tracks changes in the ContainmentLinkEnd items, and
	 * responds to changes in them through this method.
	 */
	
	public void propertyChange(PropertyChangeEvent arg0) {
		String propName = arg0.getPropertyName();
		if(propName == ContainmentLinkEnd.NEW_TYPE_EVENT){
			LOType oldType = (LOType)arg0.getOldValue();
			LOType newType = (LOType)arg0.getNewValue();
			if(oldType != newType && containmentSet.isIncluded(oldType) != containmentSet.isIncluded(newType))
				containmentSet.refresh();
			else
				this.firePropertyChange(COLOUR_EVENT);
		} else if(propName == ContainmentLinkEnd.COLOUR_EVENT)
			this.firePropertyChange(COLOUR_EVENT);
		
	}
	
	public String toString(){
		return "Link: [from:"+from.getResource().getALID()+
		", to: "+to.getResource().getALID()+"]";
	}

}
