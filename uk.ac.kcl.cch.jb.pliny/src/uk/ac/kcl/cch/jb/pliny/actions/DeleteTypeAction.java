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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.commands.DeleteTypeCommand;
import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LinkQuery;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObjectQuery;
import uk.ac.kcl.cch.jb.pliny.views.TypeManagerView;
import uk.ac.kcl.cch.jb.pliny.views.utils.DeleteTypeWizard;
import uk.ac.kcl.cch.rdb2java.dynData.BaseQuery;

/**
 * This class provides an action for the {@link uk.ac.kcl.cch.jb.pliny.views.TypeManagerView} to delete
 * a Pliny {@link uk.ac.kcl.cch.jb.pliny.model.LOType}.  It operates through the view's CommandStack.
 * @author John Bradley
 *
 */
public class DeleteTypeAction extends Action {

	private TypeManagerView myView;
	
	/***
	 * constructor for this class.  Requires the TypeManagerView so that
	 * it can get access to its CommandStack.
	 * 
	 * @param view TypeManagerView the owning view.
	 */
	public DeleteTypeAction(TypeManagerView view) {
		super("Delete Type");
		this.setImageDescriptor(
				ImageDescriptor.createFromImage(PlinyPlugin.getDefault().getImage("icons/typeIcon.gif")));
		myView = view;
	}
	
	/**
	 * runs the delete {@link uk.ac.kcl.cch.jb.pliny.model.LOType} action using a command given to the View's
	 * <code>CommandStack</code>.
	 */
	public void run(){
		LOType theType = myView.getSelectedType();
		if(theType.getALID() <= LOType.MAX_UNDELETABLE_TYPES)return;
		
		LinkableObjectQuery q = new LinkableObjectQuery();
		//q.setWhereString("typeKey="+theType.getALID());
		q.addConstraint("typeKey", BaseQuery.FilterEQUAL, theType.getALID());
		int count = q.executeCount();
		LinkQuery q2 = new LinkQuery();
		//q2.setWhereString("typeKey="+theType.getALID());
		q2.addConstraint("typeKey", BaseQuery.FilterEQUAL, theType.getALID());
		count += q2.executeCount();
		if(count == 0){
			boolean rslt = MessageDialog.openConfirm(myView.getSite().getShell(),
					"Confirm Deletion", "Confirm deletion of type '"+theType.getName()+"'");
			if(rslt)
				myView.getCommandStack().execute(new DeleteTypeCommand(theType, LOType.getDefaultType()));
		} else {
			DeleteTypeWizard wizard = new DeleteTypeWizard(myView, theType, count);
			WizardDialog dialog = 
				new WizardDialog(myView.getSite().getShell(), wizard);
			dialog.open();
		}
	}

}
