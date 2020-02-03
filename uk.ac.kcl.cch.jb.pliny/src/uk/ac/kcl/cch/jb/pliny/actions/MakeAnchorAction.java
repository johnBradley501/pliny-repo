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

package uk.ac.kcl.cch.jb.pliny.actions;

import org.eclipse.gef.EditDomain;
import org.eclipse.gef.Tool;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.tools.CreationTool;
import org.eclipse.jface.action.Action;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.model.Anchor;

/**
 * This action tells GEF to use its Creation tool to allow the user to
 * create a new {@link uk.ac.kcl.cch.jb.pliny.model.Anchor}.  To this end, it is also a creation factory
 * for Anchor objects.
 * <p>
 * The actual creation of an Anchor happens when the user draws out
 * an area on the screen, and is handled as a part of the GEF policy
 * {@link uk.ac.kcl.cch.jb.pliny.policies.ScalableImageXYLayoutEditPolicy#getCreateCommand}.
 * 
 * @author John Bradley
 *
 */
public class MakeAnchorAction extends Action implements CreationFactory {
	
	private EditDomain editDomain;
	private CreationFactory factory;
	private Tool tool;

	/**
	 * constructor for this action.  It will require a GEF editDomain 
	 * to which the tool can be given before
	 * it can be used, which can be specified through <code>setEditDomain()</code>.
	 *
	 */
	public MakeAnchorAction() {
		super();
		this.editDomain = null;
		init();
	}

	/**
	 * constructor for this action.  The editDomain is the one to
	 * which the CreateTool is to be given.
	 *
	 *@param editDomain EditDomain 
	 */
	public MakeAnchorAction(EditDomain editDomain) {
		super();
		this.editDomain = editDomain;
		init();
	}
	
	private void init(){
		setText("Create new Anchor");
		setImageDescriptor(PlinyPlugin.getImageDescriptor("icons/anchorIcon.gif"));
				// ImageDescriptor.createFromImage(ModelPlugin.getDefault().getImage("icons/noteIcon.gif")));
		this.setToolTipText("Create new Anchor");
		factory = this;
	}
	
	/**
	 * creates a new CreationTool with this as a factory for Anchors
	 * and gives the tool to the editDomain.
	 */
	public void run(){
		if(editDomain != null){
		  tool = new CreationTool(factory);
		  editDomain.setActiveTool(tool);
		}
	}

	/**
	 * creates a new Anchor object.  Part of the CreationFactory interface.
	 */
	public Object getNewObject() {
		// TODO Auto-generated method stub
		return new Anchor();
	}

	/**
	 * returns the Anchor class.  Part of the CreationFactory interface.
	 */
	public Object getObjectType() {
		// TODO Auto-generated method stub
		return Anchor.class;
	}

	/**
	 * stores the editDomain to which the creation tool will be given.
	 * 
	 * @param theDomain EditDomain
	 */
	public void setEditDomain(EditDomain theDomain) {
		this.editDomain = theDomain;
	}
}
