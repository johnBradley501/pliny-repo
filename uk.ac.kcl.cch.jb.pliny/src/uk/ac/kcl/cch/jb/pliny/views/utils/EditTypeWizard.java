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

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import uk.ac.kcl.cch.jb.pliny.commands.AddLOTypeCommand;
import uk.ac.kcl.cch.jb.pliny.commands.UpdateLOTypeCommand;
import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.views.TypeManagerView;

/**
 * This wizard provides support when the user either wishes to edit the
 * attributes of a 
 * {@link uk.ac.kcl.cch.jb.pliny.model.LOType LOType}, or create a new one.
 * The actual change/create is managed through a CommandStack so that it
 * is undoable.
 * 
 * @author John Bradley
 */
public class EditTypeWizard extends Wizard {
	
	private LOType myType = null;
	private EditTypePageOneWizardPage pageOne;
	private EditTypePageTwoWizardPage pageTwo;
	private TypeManagerView view;
	private Font titleFont = null;

	/**
	 * constructor for using this wizard for {
	 * {@link uk.ac.kcl.cch.jb.pliny.model.LOType LOType} creation.
	 * 
	 * @param view the view needed so that its CommandStack is available.
	 */
	public EditTypeWizard(TypeManagerView view) {
		super();
		this.view = view;
		myType = view.getSelectedType();
	}
	
	//public void dispose(){
	//	pageOne.dispose();
	//	pageTwo.dispose();
	//	super.dispose();
	//}
	
	public void dispose(){
		if(titleFont != null)titleFont.dispose();
		super.dispose();
	}

	/**
	 * constructor for using this wizard for {
	 * {@link uk.ac.kcl.cch.jb.pliny.model.LOType LOType} editing.
	 * 
	 * @param view the view needed so that its CommandStack is available.
	 * @param myType the LOType to be changed.
	 */
	public EditTypeWizard(TypeManagerView view, LOType myType){
		super();
		this.view = view;
		myType = null;
	}
	
	private Font getTheFont(){
		if(titleFont != null)return titleFont;
		FontData data = Display.getCurrent().getSystemFont().getFontData()[0];
		data.setStyle(SWT.BOLD);
		titleFont = new Font(Display.getCurrent(),data);
		return titleFont;
	}
	
	private class EditTypePageOneWizardPage extends WizardPage {
		
		private Text nameField;
		private RGB titleForeColourRGB;
		private RGB titleBackColourRGB;
		private RGB bodyForeColourRGB;
		private RGB bodyBackColourRGB;
		
		private Color titleForeColour;
		private Color titleBackColour;
		private Color bodyForeColour;
		private Color bodyBackColour;
		
		private Label titleColoursLabel;
		private Label bodyColoursLabel;
		
		public EditTypePageOneWizardPage(){
			super("EditTypePage");
			setTitle("Edit Type Attributes");
			setDescription("Provide attributes of this type");
			
			if(myType == null){
				titleForeColourRGB = new RGB(255,255,255);
				titleBackColourRGB = new RGB(0,0,0);
				bodyForeColourRGB = new RGB(0,0,0);
				bodyBackColourRGB = new RGB(240,240,240);
				setTitle("Edit Type Attributes");
				setDescription("Provide attributes for your new type");
			} else {
				titleForeColourRGB = myType.getTitleForeColourRGB();
				titleBackColourRGB = myType.getTitleBackColourRGB();
				bodyForeColourRGB = myType.getBodyForeColourRGB();
				bodyBackColourRGB = myType.getBodyBackColourRGB();
				setTitle("Edit Type Attributes");
				setDescription("Provide attributes for this type");
			}
			titleForeColour = new Color(Display.getCurrent(), titleForeColourRGB);
			titleBackColour = new Color(Display.getCurrent(), titleBackColourRGB);
			bodyForeColour = new Color(Display.getCurrent(), bodyForeColourRGB);
			bodyBackColour = new Color(Display.getCurrent(), bodyBackColourRGB);
			
		}
		
		public void dispose(){
			titleForeColour.dispose();
			titleBackColour.dispose();
			bodyForeColour.dispose();
			bodyBackColour.dispose();
			super.dispose();
		}
		
		public Text getNameField(){return nameField;}
		
		public RGB getTitleForeColourRGB(){
			return titleForeColourRGB;

		}
		public RGB getTitleBackColourRGB(){
			return titleBackColourRGB;

		}
		public RGB getBodyForeColourRGB(){
			return bodyForeColourRGB;

		}
		public RGB getBodyBackColourRGB(){
			return bodyBackColourRGB;

		}

		public void createControl(Composite parent) {
		      Composite container = new Composite(parent, SWT.NULL);
		      final GridLayout gridLayout = new GridLayout();
		      gridLayout.numColumns = 4;
		      container.setLayout(gridLayout);
		      setControl(container);
		      
		      Label label = new Label(container, SWT.NONE);
		      GridData gridData = new GridData();
		      gridData.horizontalSpan = 4;
		      label.setLayoutData(gridData);
		      label.setFont(getTheFont());
		      label.setText("Provide a name that will be used for this type.");
		      
		      label = new Label(container, SWT.NONE);
		      gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		      label.setLayoutData(gridData);
		      label.setText("Name for this type:");
		      
		      if(myType == LOType.getDefaultType()){
		    	  label = new Label(container, SWT.NONE);
		    	  gridData = new GridData();
		    	  gridData.horizontalSpan = 3;
		    	  label.setText("(undefined)");
		    	  nameField = null;
		      } else {
		         nameField = new Text(container, SWT.BORDER);
		         gridData = new GridData();
		         gridData.horizontalSpan = 3;
		         gridData.grabExcessHorizontalSpace = true;
		         gridData.horizontalAlignment = GridData.FILL;

		         nameField.setLayoutData(gridData);
			     nameField.addModifyListener(new ModifyListener()  
					      {
					         public void modifyText(ModifyEvent e) {
					            updatePageComplete();
					         }
					      });
		         if(myType != null)nameField.setText(myType.getName());
		         else setPageComplete(false);
		      }
		      
		      label = new Label(container, SWT.NONE);
		      gridData = new GridData();
		      gridData.horizontalSpan = 4;
		      label.setLayoutData(gridData);
		      label.setFont(getTheFont());
		      label.setText("Specify Colours to be used for this type.");
		      
		      titleColoursLabel = new Label(container, SWT.NONE);
		      titleColoursLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		      titleColoursLabel.setForeground(titleForeColour);
		      titleColoursLabel.setBackground(titleBackColour);
		      titleColoursLabel.setText("Title Colours");
		      
		      Button button = new Button(container, SWT.PUSH);
		      button.setText("Text Colour");
		      button.addSelectionListener(new SelectionAdapter(){
		    	  public void widgetSelected(SelectionEvent event){
		    		  ColorDialog dlg = new ColorDialog(Display.getCurrent().getActiveShell());
		    		  dlg.setRGB(titleForeColourRGB);
		    		  dlg.setText("Choose a Title text colour");
		    		  RGB rslt = dlg.open();
		    		  if(rslt != null){
		    		     titleForeColourRGB = rslt;
		    		     titleForeColour.dispose();
		    		     titleForeColour = new Color(Display.getCurrent(), titleForeColourRGB);
		    		     titleColoursLabel.setForeground(titleForeColour);
		    		     titleColoursLabel.redraw();
		    		  }
		    	  }
		      });
		      
		      button = new Button(container, SWT.PUSH);
		      button.setText("Back Colour");
		      button.addSelectionListener(new SelectionAdapter(){
		    	  public void widgetSelected(SelectionEvent event){
		    		  ColorDialog dlg = new ColorDialog(Display.getCurrent().getActiveShell());
		    		  dlg.setRGB(titleBackColourRGB);
		    		  dlg.setText("Choose a Title backing colour");
		    		  RGB rslt = dlg.open();
		    		  if(rslt != null){
			    		  titleBackColourRGB = rslt;
			    		  titleBackColour.dispose();
			    		  titleBackColour = new Color(Display.getCurrent(), titleBackColourRGB);
			    		  titleColoursLabel.setBackground(titleBackColour);
			    		  titleColoursLabel.redraw();
		    		  }
		    	  }
		      });
		      
		      button = new Button(container, SWT.PUSH);
		      button.setText("Contrast Text");
		      button.addSelectionListener(new SelectionAdapter(){
		    	  public void widgetSelected(SelectionEvent event){
		    		  titleForeColourRGB = makeContrast(titleBackColourRGB);
		    		  titleForeColour.dispose();
		    		  titleForeColour = new Color(Display.getCurrent(), titleForeColourRGB);
		    		  titleColoursLabel.setForeground(titleForeColour);
		    		  titleColoursLabel.redraw();
		    	  }

		      });
		      
		      // ========================================================
		      
		      
		      bodyColoursLabel = new Label(container, SWT.NONE);
		      bodyColoursLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		      bodyColoursLabel.setForeground(bodyForeColour);
		      bodyColoursLabel.setBackground(bodyBackColour);
		      bodyColoursLabel.setText("Body Colours");
		      
		      button = new Button(container, SWT.PUSH);
		      button.setText("Text Colour");
		      button.addSelectionListener(new SelectionAdapter(){
		    	  public void widgetSelected(SelectionEvent event){
		    		  ColorDialog dlg = new ColorDialog(Display.getCurrent().getActiveShell());
		    		  dlg.setRGB(bodyForeColourRGB);
		    		  dlg.setText("Choose the Body text colour");
		    		  RGB rslt = dlg.open();
		    		  if(rslt != null){
			    		  bodyForeColourRGB = rslt;
			    		  bodyForeColour.dispose();
			    		  bodyForeColour = new Color(Display.getCurrent(), bodyForeColourRGB);
			    		  bodyColoursLabel.setForeground(bodyForeColour);
			    		  bodyColoursLabel.redraw();
		    		  }
		    	  }
		      });
		      
		      button = new Button(container, SWT.PUSH);
		      button.setText("Back Colour");
		      button.addSelectionListener(new SelectionAdapter(){
		    	  public void widgetSelected(SelectionEvent event){
		    		  ColorDialog dlg = new ColorDialog(Display.getCurrent().getActiveShell());
		    		  dlg.setRGB(bodyBackColourRGB);
		    		  dlg.setText("Choose the Body backing colour");
		    		  RGB rslt = dlg.open();
		    		  if(rslt != null){
			    		  bodyBackColourRGB = rslt;
			    		  bodyBackColour.dispose();
			    		  bodyBackColour = new Color(Display.getCurrent(), bodyBackColourRGB);
			    		  bodyColoursLabel.setBackground(bodyBackColour);
			    		  bodyColoursLabel.redraw();
		    		  }
		    	  }
		      });
		      
		      button = new Button(container, SWT.PUSH);
		      button.setText("Contrast Text");
		      button.addSelectionListener(new SelectionAdapter(){
		    	  public void widgetSelected(SelectionEvent event){
		    		  bodyForeColourRGB = makeContrast(bodyBackColourRGB);
		    		  bodyForeColour.dispose();
		    		  bodyForeColour = new Color(Display.getCurrent(), bodyForeColourRGB);
		    		  bodyColoursLabel.setForeground(bodyForeColour);
		    		  bodyColoursLabel.redraw();
		    	  }

		      });

		}
		
		private RGB makeContrast(RGB input) {
			RGB rslt = new RGB(255-input.red, 255-input.green, 255-input.blue);
			if(contrastBad(input.red,rslt.red) && 
					contrastBad(input.green, rslt.green) &&
					contrastBad(input.blue, rslt.blue)){
				return new RGB(0,0,0);
			}
			return rslt;
		}
		
		private boolean contrastBad(int c1, int c2){
			if(c1 > c2)return c1 - c2 <= 40;
			return c2 - c1 <= 40;
		}
		
		private void updatePageComplete() {
			   setPageComplete(false);
			   
			   String nameText = nameField.getText().trim();
			   if((nameText == null) || (nameText.equals(""))){
				      setMessage(null);
				      setErrorMessage("Please provide a name for the new type.");
				      return;
			   }

			   setPageComplete(true);

			   setMessage(null);
			   setErrorMessage(null);
			}
		
	}
	
	private class EditTypePageTwoWizardPage extends WizardPage {
		
		private int sourceRoleKey;
		private int targetRoleKey;
		private TypeRoleUIManager sourceManager;
		private TypeRoleUIManager targetManager;
		//private Font titleFont = null;
		
		public EditTypePageTwoWizardPage(){
			super("EditTypePage");
			setTitle("Edit Type Roles");
			setDescription("Provide role specifications for this type");
			
			if(myType == null){
				sourceRoleKey = 0;
				targetRoleKey = 0;
			} else {
				Resource res = myType.getSourceRole();
				if(res == null)sourceRoleKey = 0;
				else sourceRoleKey = res.getALID();

                res = myType.getTargetRole();
                if(res == null)targetRoleKey = 0;
                else targetRoleKey = res.getALID();
			}
		}

		public void createControl(Composite parent) {
		      Composite container = new Composite(parent, SWT.NULL);
		      final GridLayout gridLayout = new GridLayout();
		      gridLayout.numColumns = 1;
		      container.setLayout(gridLayout);
		      setControl(container);
		      
		      Label sourceLabel = new Label(container,SWT.NULL);
		      GridData gridData = new GridData();
		      gridData.verticalIndent = 5;
		      sourceLabel.setLayoutData(gridData);
		      sourceLabel.setText("Set the Role for the Source here:");
		      sourceLabel.setFont(getTheFont());
		      
		      sourceManager = new TypeRoleUIManager(
		    		  container, sourceRoleKey, "Source");
		      
		      Label targetLabel = new Label(container,SWT.NULL);
		      targetLabel.setLayoutData(gridData);
		      targetLabel.setText("Set the Role for the Target here:");
		      targetLabel.setFont(getTheFont());
		      
		      targetManager = new TypeRoleUIManager(
		    		  container, targetRoleKey, "Target");
		}
		
		public int getSourceResourceKey(){
			return sourceManager.getRoleKey();
		}
		
		public String getSourceResourceName(){
			return sourceManager.getName();
		}
		
		public int getTargetResourceKey(){
			return targetManager.getRoleKey();
		}
		
		public String getTargetResourceName(){
			return targetManager.getName();
		}
	}
	
	public void addPages(){
		setWindowTitle("Set Attributes for a Type");
		pageOne = new EditTypePageOneWizardPage();
		addPage(pageOne);
		pageTwo = new EditTypePageTwoWizardPage();
		addPage(pageTwo);
		
		
	}

	public boolean performFinish() {
		String name = "";
		if(pageOne.getNameField() != null)
		   name = pageOne.getNameField().getText().trim();
		if(myType != null)
		   view.getCommandStack().execute(
				new UpdateLOTypeCommand(myType, name,
		            pageOne.getTitleForeColourRGB(),
                    pageOne.getTitleBackColourRGB(),
                    pageOne.getBodyForeColourRGB(),
                    pageOne.getBodyBackColourRGB(),
                    pageTwo.getSourceResourceKey(),
                    pageTwo.getSourceResourceName(),
                    pageTwo.getTargetResourceKey(),
                    pageTwo.getTargetResourceName()));
		else
			view.getCommandStack().execute(
				new AddLOTypeCommand(name,
			            pageOne.getTitleForeColourRGB(),
	                    pageOne.getTitleBackColourRGB(),
	                    pageOne.getBodyForeColourRGB(),
	                    pageOne.getBodyBackColourRGB(),
	                    pageTwo.getSourceResourceKey(),
	                    pageTwo.getSourceResourceName(),
	                    pageTwo.getTargetResourceKey(),
	                    pageTwo.getTargetResourceName()));
		return true;
	}

}
