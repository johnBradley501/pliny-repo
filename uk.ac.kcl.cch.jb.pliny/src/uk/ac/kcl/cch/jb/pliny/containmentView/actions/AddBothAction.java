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
 * to include display of both parents and children for the selected
 * {@link uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentItem ContainmentItem}s.
 * This Action is created for the ContainmentViews GEF-derived contextual menu
 * {@link uk.ac.kcl.cch.jb.pliny.containmentView.ContainmentViewMenuProvider}.
 * <p>
 * The method {@link uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentSet#extendItems}
 * does the real work here.
 * 
 * @author John Bradley
 */

public class AddBothAction extends Action {

	private Vector items;
	private ContainmentSet set;

	/**
	 * constructs the action so that it will add parents and children
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
	public AddBothAction(ContainmentSet set, Vector items) {
		super();
		this.items = items;
		this.set = set;
		this.setText("Add Parents and Children");
		this.setImageDescriptor(
				ImageDescriptor.createFromImage(PlinyPlugin.getDefault().getImage("icons/addBothIcon.gif")));
	}

	public void run(){
		set.extendItems(items, true, true);
	}
}
