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
import uk.ac.kcl.cch.jb.pliny.model.Annotation;

/**
 * This action tells GEF to use its Creation tool to allow the user to
 * create a new {@link uk.ac.kcl.cch.jb.pliny.model.Annotation}.  To this end, it is also a creation factory
 * for Annotation objects.
 * <p>
 * The actual creation of an Annotation (which is really
 * an anchor, a note reference and a link between them)
 * happens when the user draws out
 * an area on the screen, and is handled as a part of the GEF policy
 * {@link uk.ac.kcl.cch.jb.pliny.policies.ScalableImageXYLayoutEditPolicy#getCreateCommand}.
 * 
 * @author John Bradley
 *
 */
public class MakeAnnotationAction extends Action implements CreationFactory {
	
	private EditDomain editDomain;
	private CreationFactory factory;
	private Tool tool;

	/**
	 * constructor for this action.  It will require a GEF editDomain 
	 * to which the tool can be given before
	 * it can be used, which can be specified through <code>setEditDomain()</code>.
	 *
	 */
	public MakeAnnotationAction() {
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
	public MakeAnnotationAction(EditDomain editDomain) {
		super();
		this.editDomain = editDomain;
		init();
	}
	
	private void init(){
		setText("Create new Annotation");
		setImageDescriptor(PlinyPlugin.getImageDescriptor("icons/annotationIcon.gif"));
				// ImageDescriptor.createFromImage(ModelPlugin.getDefault().getImage("icons/noteIcon.gif")));
		this.setToolTipText("Create new Annotation");
		factory = this;
	}
	
	/**
	 * creates a new CreationTool with this as a factory for Annotations
	 * and gives the tool to the editDomain.
	 */
	public void run(){
		if(editDomain != null){
		  tool = new CreationTool(factory);
		  editDomain.setActiveTool(tool);
		}
	}

	/**
	 * creates a new Annotation object.  Part of the CreationFactory interface.
	 */
	public Object getNewObject() {
		return new Annotation();
	}

	/**
	 * returns the Annotation class.  Part of the CreationFactory interface.
	 */
	public Object getObjectType() {
		// TODO Auto-generated method stub
		return Annotation.class;
	}
}
