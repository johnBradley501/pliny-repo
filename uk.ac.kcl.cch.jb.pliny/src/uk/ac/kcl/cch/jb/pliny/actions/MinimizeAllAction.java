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

import org.eclipse.gef.ui.actions.WorkbenchPartAction;

import uk.ac.kcl.cch.jb.pliny.IResourceDrivenPart;
import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.commands.MinimizeAllContainedObjectCommand;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * This action supports the Minimize All action for Pliny
 * editors and view.
 * <P>
 * Note that this action derives from GEF's WorkbenchPartAction,
 * so it operates with editors and views that contain a CommandStack,
 * and the minimize is done by means of a command so that it is
 * undoable.
 * 
 * @author John Bradley
 */

public class MinimizeAllAction extends WorkbenchPartAction {
	private IResourceDrivenPart myPart;
    //AnnotationView viewer = null;
    //Resource myResource = null;
    
    /**
     * constructor for this action.  If this constructor is used
     * <code>updatePart(part)</code> must be called (to give the code a part
     * against which to work) before <code>run()</code> is invoked.
     *
     */
    public MinimizeAllAction(){
    	super(null);
    	myPart = null;
    }
	
    /*
     * constructor for this action to be used by
     * {@link uk.ac.kcl.cch.jb.pliny.views.AnnotationView}.
     *
     * @param viewer the AnnotationView
     */
    
	//public MinimizeAllAction(AnnotationView viewer) {
	//	super(viewer);
	//	this.viewer = viewer;
	//}

	/**
     * constructor for this action to be used by any pliny
     * view or editor that implements
     * {@link uk.ac.kcl.cch.jb.pliny.IResourceDrivenPart}.
     *
     * @param part the Resource Driver Part
     */
	
	public MinimizeAllAction(IResourceDrivenPart part){
		super(part);
		myPart = part;
		//this.myResource = part.getMyResource();
	}
	
	protected void init(){
		setText("Minimize all");
		setImageDescriptor(PlinyPlugin.getImageDescriptor("icons/allClose.gif"));
		this.setToolTipText("Minimize all objects");
	}
	
	/**
	 * provides a pliny part with which this action is to work.
	 * The part must implement {@link uk.ac.kcl.cch.jb.pliny.IResourceDrivenPart}.
	 * 
	 * @param part the IResourceDrivenPart
	 */
	
	public void updatePart(IResourceDrivenPart part){
		this.setWorkbenchPart(part);
		myPart = part;
		//this.myResource = part.getMyResource();
	}
	
	//private Resource getTheResource(){
	//	if(myResource != null)return myResource;
	//	if(viewer != null){
	//		return ((AnnotationViewPage)viewer.getCurrentPage()).getMyResource();
	//	}
	//	return null;
	//}
	
	/**
	 * executes the command
	 * {@link uk.ac.kcl.cch.jb.pliny.commands.MinimizeAllContainedObjectCommand}
	 * against the part's CommandStack.  This makes the minimization
	 * undoable.
	 */
	
	public void run(){
		Resource theResource = myPart.getMyResource();
		if(theResource == null)return;
		execute(new MinimizeAllContainedObjectCommand(theResource));
	}

	protected boolean calculateEnabled() {
		return true;
	}

}
