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

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;

/**
 * this wizard handles the getting of options for the writing of
 * materials from a reference area for a resource to an HTML file.
 * 
 * @author John Bradley
 *
 */

public class TextFileGeneratorWizard extends Wizard {

	private TextFileGenerator generator;
	
	private boolean recursiveProcessing = true;
	private int recursionDepth = 9999;
	private boolean makeXHTML = false;
	
	private static final String recursiveProcessingPrefName = "textGenerator.recursivePref";
	private static final String recursionDepthPrefName = "textGenerator.recDepth";
	private static final String makeXHTMLPrefName = "textGenerator.makeXHTML";
	
	private boolean doImport = false;
	
	public TextFileGeneratorWizard(TextFileGenerator generator) {
		super();
		this.generator = generator;
		setDefaults();
	}
	
	private void setDefaults() {
		Preferences prefs = PlinyPlugin.getDefault().getPluginPreferences();
		if(prefs.contains(recursiveProcessingPrefName))
			recursiveProcessing = prefs.getBoolean(recursiveProcessingPrefName);
		if(prefs.contains(recursionDepthPrefName))
			recursionDepth = prefs.getInt(recursionDepthPrefName);
		if(prefs.contains(makeXHTMLPrefName))
			makeXHTML = prefs.getBoolean(makeXHTMLPrefName);
		// the Preferences have problems storing a value of zero (!) -- hence, if the user wants zero
		// it is changed in performFinish() to -1 for storing in the Preferences.  
		// Here it is changed back!   ..jb
		if(recursionDepth < 0)recursionDepth = 0;
	}

	private class TextFileGeneratorOptionsPage extends WizardPage {

		public TextFileGeneratorOptionsPage(){
			super("TextFileGeneratorOptionsPage");
			setTitle("Set Text Generation Options");
			setDescription("Provide options to control the text generation operation");
		}
		
		public void createControl(Composite parent) {
		      Composite container = new Composite(parent, SWT.NULL);
		      final GridLayout gridLayout = new GridLayout();
		      gridLayout.numColumns = 2;
		      container.setLayout(gridLayout);
		      setControl(container);

		      Label label = new Label(container, SWT.WRAP);
		      GridData gridData = new GridData();
		      gridData.horizontalSpan = 2;
		      label.setLayoutData(gridData);
		      label.setText("Indicate whether or not references to resources "+
		    		  "contained inside a resource (but not currently displayed) should be included.");

		      final Label label_1 = new Label(container, SWT.NONE);
		      final GridData gridData_1 =
		         new GridData(GridData.HORIZONTAL_ALIGN_END);
		      label_1.setLayoutData(gridData_1);
		      label_1.setText("Include hidden contained items?");
		      
		      Button hiddenContainedButton = new Button(container, SWT.CHECK);
		      hiddenContainedButton.setSelection(recursiveProcessing);
		      hiddenContainedButton.addSelectionListener(new SelectionAdapter(){
		    	  public void widgetSelected(SelectionEvent e){
		    		  Button button = (Button)e.widget;
		    		  recursiveProcessing = button.getSelection();		    	  
		          }
		      });

		      label = new Label(container, SWT.WRAP);
		      gridData = new GridData();
		      gridData.horizontalSpan = 2;
		      label.setLayoutData(gridData);
		      label.setText("Specify maximum nesting depth to write out.");

		      final Label label_2 = new Label(container, SWT.NONE);
		      final GridData gridData_2 =
		         new GridData(GridData.HORIZONTAL_ALIGN_END);
		      label_2.setLayoutData(gridData_2);
		      label_2.setText("Maximum Depth:");
		      
		      Text maxDepthField = new Text(container, SWT.BORDER);
		      
			  //GC gc = new GC (maxDepthField);
			  //FontMetrics fm = gc.getFontMetrics ();
			  //int width = 5 * fm.getAverageCharWidth ();
			  //int height = fm.getHeight ();
			  //gc.dispose ();
			  //maxDepthField.setSize (maxDepthField.computeSize (60, height));
			  //maxDepthField.setLayoutData(
			  //	         new GridData(GridData.FILL_HORIZONTAL));
		      GridData gdItem = new GridData();
		      gdItem.widthHint = 60;
		      maxDepthField.setLayoutData(gdItem);

		      maxDepthField.setTextLimit(4);
		      maxDepthField.setText(Integer.toString(recursionDepth));
		      maxDepthField.addListener (SWT.Verify, new Listener () {
				public void handleEvent (Event e) {
					String string = e.text;
					char [] chars = new char [string.length ()];
					string.getChars (0, chars.length, chars, 0);
					for (int i=0; i<chars.length; i++) {
						if (!('0' <= chars [i] && chars [i] <= '9')) {
							e.doit = false;
							return;
						}
					}
				}
		      });
		      maxDepthField.addModifyListener(new ModifyListener(){
				public void modifyText(ModifyEvent e) {
		    		  Text field = (Text)e.widget;
		    		  recursionDepth = Integer.parseInt(field.getText());		    	  
				}
		      });


		      label = new Label(container, SWT.WRAP);
		      gridData = new GridData();
		      gridData.horizontalSpan = 2;
		      label.setLayoutData(gridData);
		      label.setText("Check if you wish the output to be made into XHTML.");

		      final Label label_3 = new Label(container, SWT.NONE);
		      final GridData gridData_3 =
		         new GridData(GridData.HORIZONTAL_ALIGN_END);
		      label_3.setLayoutData(gridData_3);
		      label_3.setText("XHTML?");

		      Button xhtmlButton = new Button(container, SWT.CHECK);
		      xhtmlButton.setSelection(makeXHTML);
		      xhtmlButton.addSelectionListener(new SelectionAdapter(){
		    	  public void widgetSelected(SelectionEvent e){
		    		  Button button = (Button)e.widget;
		    		  makeXHTML = button.getSelection();		    	  
		          }
		      });

		}
		
	}
	
	public void addPages() {
		setWindowTitle("Set Text Generation Options");
		
		addPage(new TextFileGeneratorOptionsPage());
	}

	public boolean performFinish() {
		generator.setMakeXHTML(makeXHTML);
		generator.setRecursionDepth(recursionDepth);
		generator.setRecursiveProcessing(recursiveProcessing);
		doImport = true;
		
		Preferences prefs = PlinyPlugin.getDefault().getPluginPreferences();
		prefs.setValue(recursiveProcessingPrefName, recursiveProcessing);
		// the Preferences have problems storing a value of zero (!) -- hence, if the user wants zero
		// it is changed to -1 for storing in the Preferences.  It is changed back when
		// subsequently read back in (see setDefaults())..jb
		int tempRecursionDepth = recursionDepth;
		if(tempRecursionDepth == 0)tempRecursionDepth = -1;
		prefs.setValue(recursionDepthPrefName, tempRecursionDepth);
		prefs.setValue(makeXHTMLPrefName, makeXHTML);
		return true;
	}

	public boolean getDoImport() {
		// TODO Auto-generated method stub
		return doImport;
	}

}
