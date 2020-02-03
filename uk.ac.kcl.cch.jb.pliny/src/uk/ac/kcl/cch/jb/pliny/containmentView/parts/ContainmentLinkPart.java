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

import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;

import uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentLink;

/**
 * this is the GEF MVC Controller for a ContainmentView models'
 * {@link uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentLink ContainmentLink}.
 * This item is a GEF <code>AbstractConnectionEditPart</code>, since it appears as a
 * GEF connection object between 
 * {@link uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentItem ContainmentItem} s.
 * 
 * @author John Bradley
 *
 */

public class ContainmentLinkPart extends AbstractConnectionEditPart 
implements PropertyChangeListener{
	
	PolygonDecoration target = null;
	PolygonDecoration source = null;

	public ContainmentLinkPart(ContainmentLink link) {
		setModel(link);
	}
	
	public ContainmentLink getContainmentLink(){
		return (ContainmentLink)getModel();
	}
	
	/*
	private class MyPolygonDecoration extends PolygonDecoration {
		private Color myFillColour;
		
		public MyPolygonDecoration(Color myFillColour){
			super();
			this.myFillColour = myFillColour;
		}
		
		public Color getLocalBackgroundColor(){
			return myFillColour;
		}
		
		public Color getBackgroundColor(){
			return myFillColour;
		}
		
	}
	*/
	
	/*
	private class MyConnection extends PolygonConnection {
		public String toString(){
			return "decoration: Source:"+this.get
		}
	}
	*/
	
	private void buildEnds(PolylineConnection conn, ContainmentLink link){
		if(link.getSourceEnd() != null){
			if(source == null){
				source = new PolygonDecoration();
				conn.setSourceDecoration(source);
			}
			source.setBackgroundColor(link.getSourceColour());

		} else if(source != null){
			source = null;
			conn.setSourceDecoration(null);
		}
		if(link.getTargetEnd() != null){
			if(target == null){
				target = new PolygonDecoration();
				conn.setTargetDecoration(target);
			}
			target.setBackgroundColor(link.getTargetColour());
		} else if(target != null){
			target = null;
			conn.setTargetDecoration(null);
		}
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractConnectionEditPart#createFigure()
	 */
	protected IFigure createFigure()
	{
		PolylineConnection conn = (PolylineConnection) super.createFigure();
		conn.setConnectionRouter(new BendpointConnectionRouter());
		ContainmentLink link = getContainmentLink();
		buildEnds(conn, link);
		//System.out.println("createFigure: From: "+link.getFrom()+
		//		", To: "+link.getTo()+
		//		"; SourceEnd: "+link.getSourceEnd()+
		//		", TargetEnd: "+link.getTargetEnd());
		
		return conn;
	}

	protected void createEditPolicies() {
		// TODO Auto-generated method stub

	}
	
	public void activate() {
		if(isActive())return;
		super.activate();
		getContainmentLink().addPropertyChangeListener(this);
	}
	
	public void deactivate() {
		if(!isActive())return;
		super.deactivate();
		getContainmentLink().removePropertyChangeListener(this);
	}
	
	public void refreshVisuals(){
		PolylineConnection conn = (PolylineConnection)getFigure();
		ContainmentLink link = getContainmentLink();
		buildEnds(conn, link);
		conn.repaint();
		//System.out.println("refreshVisuals: From: "+link.getFrom()+
		//		", To: "+link.getTo()+
		//		"; SourceEnd: "+link.getSourceEnd()+
		//		", TargetEnd: "+link.getTargetEnd());
		//System.out.println("--- conn: "+conn);
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		String eventName = arg0.getPropertyName();
		/* if(eventName == ContainmentLink.BOTHWAY_EVENT){
			if(getContainmentLink().getBothWay()){
				source = new PolygonDecoration();
				source.setBackgroundColor(getContainmentLink().getSourceColour());
			} else source = null;
			((PolylineConnection)getFigure()).setSourceDecoration(source);
			getFigure().repaint();
		}
		else */
		if(eventName == ContainmentLink.COLOUR_EVENT){
			if(source != null)
				source.setBackgroundColor(getContainmentLink().getSourceColour());
			if(target != null)
				target.setBackgroundColor(getContainmentLink().getTargetColour());
		}
		else if(eventName == ContainmentLink.ENDCHANGE_EVENT)
			refreshVisuals();
		
	}

}
