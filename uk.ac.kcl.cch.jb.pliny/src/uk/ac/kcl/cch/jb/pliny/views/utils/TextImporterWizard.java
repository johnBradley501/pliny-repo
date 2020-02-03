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
import java.util.TreeMap;
import java.util.Vector;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import uk.ac.kcl.cch.jb.pliny.model.Favourite;
import uk.ac.kcl.cch.jb.pliny.model.FavouriteQuery;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.utils.PlinyTxtImporter;

/**
 * the wizard the handles the getting of parameters for a textual
 * import into Pliny.
 * 
 * @see PlinyTxtImporter
 * 
 * @author John Bradley
 *
 */
public class TextImporterWizard extends Wizard {
	
	private PlinyTxtImporter importer;
	private Combo favouriteDropdown;
	private Resource favouriteResource = null;
	private static String noteMarker = "=";
	private Text noteMarkerField;
	private static String referentMarker = "==";
	private Text referentMarkerField;
	
	private boolean doImport = false;

	public TextImporterWizard(PlinyTxtImporter importer) {
		super();
		this.importer = importer;
	}
	
	private class SpecifyTextImporterOptionsPage extends WizardPage {
		
		public SpecifyTextImporterOptionsPage(){
			super("SpecifyTextImporterOptions");
			setTitle("Set Text Importer options");
			setDescription("Provide options for the Text importing operation");
		}
			
			private Vector resources = new Vector();
			
			private void buildList(){
				FavouriteQuery q = new FavouriteQuery();
				Vector favourites = q.executeQuery();
				favouriteDropdown.add("[no starting item specified]");
				TreeMap favResources = new TreeMap(ResourceNameManager.getMyCollator());
				Iterator it = favourites.iterator();
				while(it.hasNext()){
					Favourite fav = (Favourite)it.next();
					Resource myResource = fav.getResource();
					favResources.put(myResource.getName()+"/"+myResource.getALID(),myResource);
				}
				it = favResources.keySet().iterator();
				while(it.hasNext()){
					String key = (String)it.next();
					String displayKey = key.replaceAll("/[0-9]+","");
					favouriteDropdown.add(displayKey);
					resources.add(favResources.get(key));
				}
				favouriteDropdown.select(0);
			}
			
			public Resource getSelectedResource(){
				int selectedItem = favouriteDropdown.getSelectionIndex();
				if(selectedItem <= 0)return null;
				return (Resource)resources.get(selectedItem-1);
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
		      label.setText("Select a starting point resource into which references to "+
		    		  "new Notes will be put as they are imported.");

		      final Label label_1 = new Label(container, SWT.NONE);
		      final GridData gridData_1 =
		         new GridData(GridData.HORIZONTAL_ALIGN_END);
		      label_1.setLayoutData(gridData_1);
		      label_1.setText("Starting point:");

		      favouriteDropdown = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			  buildList();
			  favouriteDropdown.setTextLimit(50);

		      favouriteDropdown.addSelectionListener(new SelectionListener()  
		      {
				public void widgetSelected(SelectionEvent e) {
		        	 favouriteResource = getSelectedResource();
				}

				public void widgetDefaultSelected(SelectionEvent e) {
		        	 favouriteResource = getSelectedResource();
				}
		      });


		      final Label label_3 = new Label(container, SWT.NONE);
		      final GridData gridData_3 = new GridData();
		      gridData_3.horizontalSpan = 2;
		      label_3.setLayoutData(gridData_3);
		      label_3.setText("Provide text the importer will look for to demarcate "+
		    		  "notes and referree notes");

		      final Label label_4 = new Label(container, SWT.NONE);
		      final GridData gridData_4 =
		         new GridData(GridData.HORIZONTAL_ALIGN_END);
		      label_4.setLayoutData(gridData_4);
		      label_4.setText("Marker for Note");
		      
		      noteMarkerField = new Text(container, SWT.BORDER);
		      noteMarkerField.setText(noteMarker);
		      noteMarkerField.addModifyListener(new ModifyListener(){
			      public void modifyText(ModifyEvent e) {
			         updatePageComplete();
			      }
			  });

		      final Label label_5 = new Label(container, SWT.NONE);
		      final GridData gridData_5 =
		         new GridData(GridData.HORIZONTAL_ALIGN_END);
		      label_5.setLayoutData(gridData_5);
		      label_5.setText("Marker for Referent Note");
		      
		      referentMarkerField = new Text(container, SWT.BORDER);
		      referentMarkerField.setText(referentMarker);
		      referentMarkerField.addModifyListener(new ModifyListener(){
			      public void modifyText(ModifyEvent e) {
			         updatePageComplete();
			      }
			  });

		}

		protected void updatePageComplete() {
			setMessage(null);
			setErrorMessage(null);
			
			String noteMarkerText = noteMarkerField.getText();
			if(noteMarkerText == null || noteMarkerText.trim().length() == 0){
				setErrorMessage("Please provide a set of characters to identify a start of each note.");
				setPageComplete(false);
				return;
			}
			
			noteMarker = noteMarkerText.trim();
			
			String refereeMarkerText = referentMarkerField.getText();
			if(refereeMarkerText == null || refereeMarkerText.trim().length() == 0){
				setErrorMessage("Please provide a set of characters to identify a start of each referee note.");
				setPageComplete(false);
				return;
			}
			
			referentMarker = refereeMarkerText.trim();

			setPageComplete(true);
		}
		
	}

	public void addPages() {
		setWindowTitle("Specify Text Importing Information");

		addPage(new SpecifyTextImporterOptionsPage());
	}

	public boolean performFinish() {
		importer.setReceivingFavourite(favouriteResource);
		importer.setNoteStartID(noteMarker);
		importer.setReferentStartID(referentMarker);
		doImport = true;
		return true;
	}
	
	public boolean getDoImport(){
		return doImport;
	}

}
