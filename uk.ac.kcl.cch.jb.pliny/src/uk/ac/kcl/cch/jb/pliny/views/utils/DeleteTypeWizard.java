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

package uk.ac.kcl.cch.jb.pliny.views.utils;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.kcl.cch.jb.pliny.commands.DeleteTypeCommand;
import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LOTypeQuery;
import uk.ac.kcl.cch.jb.pliny.views.TypeManagerView;
import uk.ac.kcl.cch.rdb2java.dynData.BaseQuery;

/**
 * provides the wizard that allows the user to delete an
 * {@link uk.ac.kcl.cch.jb.pliny.model.LOType LOType} in situations
 * when there are LinkableObject or Link items that refer to the
 * this item by asking the user to select a replacement LOType.
 * 
 * @author John Bradley
 */
public class DeleteTypeWizard extends Wizard {

	private TypeManagerView myView;
	private LOType theType;
	private int count;
	private Combo typeDropDown;
	private Vector typeList;
	
	/**
	 * constructor for this wizard.
	 * 
	 * @param myView the view attached to the wizard.  This is needed so
	 * that its <code>CommandStack</code> can be available.
	 * @param theType the <code>LOType</code> to be deleted.
	 * @param count the number of items that use this LOType.
	 */
	public DeleteTypeWizard(TypeManagerView myView, LOType theType, int count) {
		this.myView = myView;
		this.theType = theType;
		this.count = count;
	}
	
	public class SelectReplacementTypePage extends WizardPage {
		
		public SelectReplacementTypePage(){
			super("SelectReplacementTypePage");
			setTitle("Select replacement type");
			setDescription("The type you want to delete ('"+theType.getName()+
					"') has "+count+" references linked to it.  Choose below "+
					"the type you want those references to be set to instead, "+
					"or choose 'Cancel' to cancel the deletion.");
		}

		public void createControl(Composite parent) {
		      Composite container = new Composite(parent, SWT.NULL);
		      final GridLayout gridLayout = new GridLayout();
		      gridLayout.numColumns = 2;
		      container.setLayout(gridLayout);
		      setControl(container);

		      Label label = new Label(container, SWT.NONE);
		      GridData gridData = new GridData();
		      gridData.horizontalSpan = 2;
		      label.setLayoutData(gridData);
		      label.setText("Choose the type you want me to assign to those references "+
		    		  "current assigned to the type you are currently deleting.");
		      
		      label = new Label(container, SWT.NONE);
		      gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		      label.setLayoutData(gridData);
		      label.setText("New Type: ");
		      
		      typeDropDown = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		      buildList();
		      typeDropDown.setTextLimit(50);
        }

		private void buildList() {
			LOTypeQuery q = new LOTypeQuery();
			//q.setWhereString("loTypeKey <> "+theType.getALID());
			q.addConstraint("loTypeKey", BaseQuery.FilterNOT_EQUAL, theType.getALID());
			q.addOrder("name");
			//q.setOrderString("name");
			typeList = q.executeQuery();
			Iterator it = typeList.iterator();
			while(it.hasNext()){
				LOType type = (LOType)it.next();
				String name = type.getName();
				if(type == LOType.getDefaultType())name = "(default)";
				typeDropDown.add(name);
			}
			typeDropDown.select(0);
		}
		
	}
	
	private LOType getSelectedType(){
		int selectedItem = typeDropDown.getSelectionIndex();
		if(selectedItem < 0)return null;
		return (LOType)typeList.get(selectedItem);
	}

	public void addPages() {
		setWindowTitle("Specify The replacement type for the deleted one");

		addPage(new SelectReplacementTypePage());
	}

	public boolean performFinish() {
		myView.getCommandStack().execute(new DeleteTypeCommand(theType, getSelectedType()));
		return true;
	}

}
