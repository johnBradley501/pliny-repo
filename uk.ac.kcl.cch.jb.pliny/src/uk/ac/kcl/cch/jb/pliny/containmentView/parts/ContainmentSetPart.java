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

import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import uk.ac.kcl.cch.jb.pliny.containmentView.layout.GraphLayoutManager;
import uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentSet;

/**
 * specifies the top level GEF part for the ContainmentView -- the area
 * on which ContainmentItems and links between them are displayed. This corresponds
 * with the model's
 * {@link uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentSet ContainmentSet}.
 * <p>Note that the automatic layout of ContainmentItems is managed by
 * specifying the specialpurpose 
 * {@link uk.ac.kcl.cch.jb.pliny.containmentView.layout.GraphLayoutManager GraphLayoutManager} as the LayoutManager
 * for this item's figure.
 * 
 * @author John Bradley
 */

public class ContainmentSetPart extends AbstractGraphicalEditPart implements
		PropertyChangeListener {

	public ContainmentSetPart(ContainmentSet set) {
		super();
		setModel(set);
	}
	
	public ContainmentSet getContainmentSet(){
		return (ContainmentSet)getModel();
	}

	protected IFigure createFigure() {
		FreeformLayer rslt = new FreeformLayer();
		rslt.setOpaque(true);
		rslt.setLayoutManager(new GraphLayoutManager(this));
		return rslt;
	}
	
	protected List getModelChildren(){
		return getContainmentSet().getMyItems();
	}
	
	public boolean isSelectable(){
		return false;
	}

	protected void createEditPolicies() {
	}
	
	public void activate() {
		if(isActive())return;
		super.activate();
		getContainmentSet().addPropertyChangeListener(this);
	}
	
	public void deactivate() {
		if(!isActive())return;
		super.deactivate();
		getContainmentSet().removePropertyChangeListener(this);
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		refreshChildren();
	}

}
