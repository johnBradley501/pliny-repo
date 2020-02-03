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

import java.util.Vector;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.commands.ResourceExplorerNewNoteCommand;

/**
 * the wizard for the Resource Explorer's view (and Resource Explorer
 * like views) for handling the creation of a new Note.
 * 
 * @author John Bradley
 *
 */

public class NewNoteWizard extends Wizard implements INewWizard{
//	private IResourceTreeDisplayer myview;
	private CommandStack commandStack;
	private SpecifyNoteNameWizardPage myPage;
	private boolean addToFavourites = false;
	private boolean makeReferent = false;
	private String noteNameSuggestion = "";
	private Vector linktoList = null;
	
	public NewNoteWizard(){
		super();
		commandStack = null;
	}
	
	/**
	 * constructor for basic wizard, when only the owning View is known.
	 * 
	 * @param myview the owning ViewPart.
	 */
	public NewNoteWizard(CommandStack myCommandStack) {
		super();
		commandStack = myCommandStack;
	}
	
	/**
	 * constructor to use when a new-note name is provided and a number
	 * of items 
	 * {@link uk.ac.kcl.cch.jb.pliny.model.NoteLucened NoteLucened})
	 * to include the new note's reference area are to be provided.
	 * 
	 * @param myview owning ViewPart.
	 * @param noteNameSuggestion note name to use.
	 * @param linktoList list of Resources to have reference object.
	 * included.
	 */
	public NewNoteWizard(CommandStack myCommandStack, 
			String noteNameSuggestion,Vector linktoList){
		super();
		this.commandStack = myCommandStack;
		this.noteNameSuggestion = noteNameSuggestion;
		this.linktoList = linktoList;
		
	}
	
	private class SpecifyNoteNameWizardPage extends WizardPage {
		
		private Text nameField;
		private Button favouritesButton;
		private Button referentButton;
		
		public Text getNameField(){return nameField;}
		
		public SpecifyNoteNameWizardPage(){
			super("SpecifyNoteName");
			setTitle("Specify Note Name");
			setDescription ("Provide a name for the new Note.");
		}

		public void createControl(Composite parent) {
		      Composite container = new Composite(parent, SWT.NULL);
		      final GridLayout gridLayout = new GridLayout();
		      gridLayout.numColumns = 2;
		      container.setLayout(gridLayout);
		      setControl(container);

		      final Label label = new Label(container, SWT.NONE);
		      final GridData gridData = new GridData();
		      gridData.horizontalSpan = 2;
		      label.setLayoutData(gridData);
		      label.setText("Provide a name that will be used as a "+
		      		"name for this note.");

		      final Label label_1 = new Label(container, SWT.NONE);
		      final GridData gridData_1 =
		         new GridData(GridData.HORIZONTAL_ALIGN_END);
		      label_1.setLayoutData(gridData_1);
		      label_1.setText("Name for Note:");

		      nameField = new Text(container, SWT.BORDER);
		      nameField.setText(noteNameSuggestion);
		      nameField.addModifyListener(new ModifyListener()  
		      {
		         public void modifyText(ModifyEvent e) {
		            updatePageComplete();
		         }
		      });
		      
		      if(PlinyPlugin.getReferent() != null){
		    	  nameField.setText(PlinyPlugin.getReferent().getName()+": Note "+PlinyPlugin.incReferenceNumber());
		      }

		      nameField.setLayoutData(
		         new GridData(GridData.FILL_HORIZONTAL));
		      
		      /* for Favourites option */

		      final Label label_2 = new Label(container, SWT.NONE);
		      final GridData gridData_2 = new GridData();
		      gridData_2.horizontalSpan = 2;
		      label_2.setLayoutData(gridData_2);
		      label_2.setText("Check the option below to have this new note "+
		    		  "automatically added to your bookmark list.");

		      final Label label_3 = new Label(container, SWT.NONE);
		      final GridData gridData_3 =
		         new GridData(GridData.HORIZONTAL_ALIGN_END);
		      label_3.setLayoutData(gridData_3);
		      label_3.setText("Add to Bookmark List?");

		      favouritesButton = new Button(container, SWT.CHECK);
		      favouritesButton.addSelectionListener(new SelectionAdapter(){
		    	  public void widgetSelected(SelectionEvent e){
		    		  Button button = (Button)e.widget;
		    		  addToFavourites = button.getSelection();		    	  
		          }
		      });
		      favouritesButton.setLayoutData(
		         new GridData(GridData.FILL_HORIZONTAL));

		      
		      /* for Referent option */

		      final Label label_4 = new Label(container, SWT.NONE);
		      final GridData gridData_4 = new GridData();
		      gridData_4.horizontalSpan = 2;
		      label_4.setLayoutData(gridData_4);
		      label_4.setText("Check the option below to have this new note "+
		    		  "automatically be set as your new referent.");

		      final Label label_5 = new Label(container, SWT.NONE);
		      final GridData gridData_5 =
		         new GridData(GridData.HORIZONTAL_ALIGN_END);
		      label_5.setLayoutData(gridData_5);
		      label_5.setText("Make referent?");

		      referentButton = new Button(container, SWT.CHECK);
		      referentButton.addSelectionListener(new SelectionAdapter(){
		    	  public void widgetSelected(SelectionEvent e){
		    		  Button button = (Button)e.widget;
		    		  makeReferent = button.getSelection();		    	  
		          }
		      });
		      referentButton.setLayoutData(
		         new GridData(GridData.FILL_HORIZONTAL));

		}
		
		/**
		 * Update the current page complete state
		 * based on the field content.
		 */
		private void updatePageComplete() {
		   setPageComplete(false);
		   
		   String nameText = nameField.getText().trim();
		   if((nameText == null) || (nameText.equals(""))){
			      setMessage(null);
			      setErrorMessage("Please provide a name for the new note.");
			      return;
		   }

		   setPageComplete(true);

		   setMessage(null);
		   setErrorMessage(null);
		}
		
	}

	public void addPages() {
		setWindowTitle("Specify New Note Information");

		myPage = new SpecifyNoteNameWizardPage();
		addPage(myPage);
	}

	public boolean performFinish() {
		String theName = myPage.getNameField().getText().trim();
		ResourceExplorerNewNoteCommand cmd = new ResourceExplorerNewNoteCommand(theName, addToFavourites, makeReferent, linktoList);
		if(commandStack != null)
		   commandStack.execute(cmd);
		else {
			cmd.execute();
		}
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// nothing needed here.
		
	}

}
