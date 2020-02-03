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
import org.eclipse.jface.action.Action;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.model.Link;

/**
 * This action tells GEF to use its Creation tool to allow the user to
 * create a new {@link uk.ac.kcl.cch.jb.pliny.model.Link}.  To this end, it is also a creation factory
 * for Link objects.
 * <p>
 * The actual creation of an Anchor happens when the user draws out
 * an area on the screen, and is handled as a part of the GEF policy
 * {@link uk.ac.kcl.cch.jb.pliny.policies.LOLinkEditPolicy#getConnectionCreateCommand}.
 * 
 * @author John Bradley
 *
 */
public class MakeConnectionAction extends Action implements CreationFactory{
	
	private EditDomain editDomain;
	private CreationFactory factory;
	private Tool tool;
	
	/**
	 * constructor for this action.  It will require a GEF editDomain 
	 * to which the tool can be given before
	 * it can be used, which can be specified through <code>setEditDomain()</code>.
	 *
	 */
	public MakeConnectionAction(){
		super();
		this.editDomain = null;
		init();
	}
	
	/**
	 * stores the editDomain to which the creation tool will be given.
	 * 
	 * @param editDomain EditDomain
	 */
	public void setEditDomain(EditDomain editDomain){
		this.editDomain = editDomain;
	}
	
	/**
	 * constructor for this action.  The editDomain is the one to
	 * which the CreateTool is to be given.
	 *
	 *@param editDomain EditDomain 
	 */
	public MakeConnectionAction(EditDomain editDomain){
		super();
		this.editDomain = editDomain;
		init();
	}
	
	private void init(){
		setText("Create new Connector");
		setImageDescriptor(PlinyPlugin.getImageDescriptor("icons/connectionIcon.gif"));
		this.setToolTipText("Create new Connector");
		factory = this;
	}

	/**
	 * creates a new CreationTool with this as a factory for Links
	 * and gives the tool to the editDomain.
	 * <p>
	 * The connection tool used is
	 * {@link MyConnectionDragCreationTool} rather than the normal
	 * <code>ConnectionDragCreationTool</code>.
	 */
	public void run(){
		if(editDomain != null){
		   tool = new MyConnectionDragCreationTool(factory);
		   editDomain.setActiveTool(tool);
		}
	}

	/**
	 * creates a new {@link uk.ac.kcl.cch.jb.pliny.model.Link} object.  Part of the CreationFactory interface.
	 * <p>
	 * Note that the Link object is created in such a way that it is not
	 * immediately added to the backing DB.  This putting in the DB will
	 * be done if the user completes the connection.
	 */
	public Object getNewObject() {
		return new Link(true);
	}

	/**
	 * returns the Link class.  Part of the CreationFactory interface.
	 */
	public Object getObjectType() {
		return Link.class;
	}

}
