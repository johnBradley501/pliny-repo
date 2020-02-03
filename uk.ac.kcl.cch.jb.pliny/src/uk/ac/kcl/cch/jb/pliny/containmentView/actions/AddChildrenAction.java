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

package uk.ac.kcl.cch.jb.pliny.containmentView.actions;

import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentSet;

/**
 * Provides the action to extend the current ContainmentView
 * to include display of children for the selected
 * {@link uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentItem ContainmentItem}s.
 * This Action is created for the ContainmentViews GEF-derived contextual menu
 * {@link uk.ac.kcl.cch.jb.pliny.containmentView.ContainmentViewMenuProvider}.
 * <p>
 * The method {@link uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentSet#extendItems}
 * does the real work here.
 * 
 * @author John Bradley
 */

public class AddChildrenAction extends Action {

	private Vector items;
	private ContainmentSet set;
	
	/**
	 * constructs the action so that it will add children
	 * of selected <code>ContainmentItem</code>s contained in
	 * <code>items</code> that appear within the given
	 * <code>ContainmentSet</code>
	 * 
	 * @see uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentItem
	 * @see uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentSet
	 * 
	 * @param set ContainmentSet that contains the items to be expanded.
	 * @param items Vector of ContainmentItems that should be expanded.
	 */

	public AddChildrenAction(ContainmentSet set, Vector items) {
		super();
		this.items = items;
		this.set = set;
		this.setText("Add Containment Children");
		this.setImageDescriptor(
				ImageDescriptor.createFromImage(PlinyPlugin.getDefault().getImage("icons/addChildrenIcon.gif")));
	}

	public void run(){
		set.extendItems(items, false, true);
	}
	
}
