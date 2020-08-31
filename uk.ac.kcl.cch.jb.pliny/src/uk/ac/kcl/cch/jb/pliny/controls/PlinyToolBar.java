/*******************************************************************************
 * Copyright (c) 2014 John Bradley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Bradley - initial API and implementation
 *******************************************************************************/

package uk.ac.kcl.cch.jb.pliny.controls;
/**
 * provides the standard toolbar area for Pliny annotators/editors.  The toolbar area provides, in turn,
 * the standard tools available in Pliny annotators/editors.
 * 
 * @since 1.3
 * @author John Bradley
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import uk.ac.kcl.cch.jb.pliny.IResourceDrivenPart;
import uk.ac.kcl.cch.jb.pliny.ResourceAreaManager;
import uk.ac.kcl.cch.jb.pliny.actions.CopyPlinyAction;
import uk.ac.kcl.cch.jb.pliny.actions.CutPlinyAction;
import uk.ac.kcl.cch.jb.pliny.actions.DisplayReferrerAction;
import uk.ac.kcl.cch.jb.pliny.actions.IStyledTextHandler;
import uk.ac.kcl.cch.jb.pliny.actions.MakeAnchorAction;
import uk.ac.kcl.cch.jb.pliny.actions.MakeAnnotationAction;
import uk.ac.kcl.cch.jb.pliny.actions.MakeConnectionAction;
import uk.ac.kcl.cch.jb.pliny.actions.MakeNoteAction;
import uk.ac.kcl.cch.jb.pliny.actions.MinimizeAllAction;
import uk.ac.kcl.cch.jb.pliny.actions.PastePlinyAction;
import uk.ac.kcl.cch.jb.pliny.browser.VirtualBrowserResource;
import uk.ac.kcl.cch.jb.pliny.commands.ResourceNameUpdateCommand;
import uk.ac.kcl.cch.jb.pliny.dnd.DnDResourceHolder;
import uk.ac.kcl.cch.jb.pliny.dnd.NewNoteButtonDragListener;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

public class PlinyToolBar implements PropertyChangeListener{
	private IResourceDrivenPart resourceHolder;
	private Resource currentResource = null;
	private boolean includeAnchorTools = false; 
	
	private Composite toolbarComp = null;
	private StyledText nameField;
	private DnDResourceHolder rh = null;
	private ToolBarManager manager = null;
	
	private CommandStack commandStack;
	//private ActionRegistry actionRegistry;
	
	private IStyledTextHandler cutAction = null;
	private IStyledTextHandler copyAction = null;
	private IStyledTextHandler pasteAction = null;


	private Vector toolItemActionLinkers= new Vector();

	public PlinyToolBar(Composite parent, IResourceDrivenPart part, ResourceAreaManager areaManager, 
	Image myImage, Vector otherContributions){
		this(parent, part, areaManager, myImage, false, otherContributions);
	}

	public PlinyToolBar(Composite parent, IResourceDrivenPart part, ResourceAreaManager areaManager, 
	Image myImage, boolean includeAnchorTools, Vector otherContributions){
		this.resourceHolder = part;
		currentResource = part.getMyResource();
		currentResource.addPropertyChangeListener(this);
        if(otherContributions == null)otherContributions = new Vector();
        this.includeAnchorTools = includeAnchorTools;
        
        commandStack = areaManager.getEditDomain().getCommandStack();
        setupActions(areaManager.getActionRegistry());
        
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

        toolbarComp = new Composite(parent, SWT.BORDER);
        toolbarComp.setLayout(new ToolbarLayout());

        rh = new DnDResourceHolder(toolbarComp, SWT.NONE);
        rh.getLabel().setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        rh.getLabel().setImage(myImage);
        if(resourceHolder.getMyResource() != null)rh.setResource(resourceHolder.getMyResource());
        
        Label sep1 = new Label(toolbarComp, SWT.SEPARATOR);
        GridData data = new GridData();
        data.heightHint = 32;
        sep1.setLayoutData(data);
        
        buildLeftEndTools(toolbarComp);
        
        nameField = new StyledText(toolbarComp, SWT.BORDER);
        nameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        nameField.setBackground(ColorConstants.white);
        nameField.setText(resourceHolder.getMyResource().getName());
        nameField.addFocusListener(new FocusAdapter(){

    		public void focusGained(FocusEvent e) {
    			giveActionsStyledText(nameField);
    			//nameField.setFocus();
    			
    		}
       	@Override
			public void focusLost(FocusEvent e){
        		updateResourceName();
    			giveActionsStyledText(null);
        	}
        });
        nameField.addKeyListener(new KeyAdapter(){
        //nameWidget.addKeyListener(new KeyAdapter(){
        	@Override
			public void keyReleased(KeyEvent e){
        		if(e.character==SWT.CR){
        			updateResourceName();
        			giveActionsStyledText(null);
       		}
        	}
        });
        
       buildToolbarPart(part, areaManager, otherContributions);

	}
	
	private void setupActions(ActionRegistry actionRegistry) {
		cutAction = (IStyledTextHandler) actionRegistry.getAction(ActionFactory.CUT.getId());
		copyAction = (IStyledTextHandler) actionRegistry.getAction(ActionFactory.COPY.getId());
		pasteAction = (IStyledTextHandler) actionRegistry.getAction(ActionFactory.PASTE.getId());
	}
	
	private void giveActionsStyledText(StyledText myText){
		cutAction.setStyledText(myText);
		copyAction.setStyledText(myText);
		pasteAction.setStyledText(myText);
	}
	
	protected void buildLeftEndTools(Composite toolbarComp) {
		//override to make it do something useful  JB
	}

	private void buildToolbarPart(IResourceDrivenPart part, ResourceAreaManager areaManager, Vector otherContributions) {
		ToolBar toolbar = new ToolBar(toolbarComp, SWT.FLAT);
		manager = new ToolBarManager(toolbar);
		
		DisplayReferrerAction displayReferrer = new DisplayReferrerAction();
		displayReferrer.setActivePart(part);
		manager.add(displayReferrer);
		
		manager.add(new Separator());
		
        IAction undoAction = ActionFactory.UNDO.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
        // toolItemActionLinkers.add(new ToolItemActionLinker(toolbar, undoAction));
        manager.add(undoAction);

        IAction redoAction = ActionFactory.REDO.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
        //toolItemActionLinkers.add(new ToolItemActionLinker(toolbar, redoAction));
        manager.add(redoAction);
		
		final MakeNoteAction mnAction = new MakeNoteAction();
		mnAction.setEditDomain(areaManager.getEditDomain());
		//toolItemActionLinkers.add(new ToolItemActionLinker(toolbar, mnAction));
		//manager.add(mnAction);
		manager.add(new ControlContribution("New Note") {

			@Override
			protected Control createControl(Composite parent) {
				//parent.setLayout(new FillLayout());
				Label b1 = new Label(parent, SWT.NONE);
				b1.setImage(mnAction.getImageDescriptor().createImage());
				b1.addMouseListener(new MouseAdapter(){
					public void mouseUp(MouseEvent e){
						mnAction.run();
					}
				});
				new NewNoteButtonDragListener(b1);
				return b1;
			}
			
		});
		areaManager.addKeyStrokeAction(KeyStroke.getReleased(SWT.F5, 0), mnAction);
		
		MakeConnectionAction mcAction = new MakeConnectionAction();
		mcAction.setEditDomain(areaManager.getEditDomain());
		manager.add(mcAction);
		//toolItemActionLinkers.add(new ToolItemActionLinker(toolbar, mcAction));
		areaManager.addKeyStrokeAction(KeyStroke.getReleased(SWT.F6, 0), mcAction);
		
        if(includeAnchorTools){
        	MakeAnnotationAction makeAnnotationAction = new MakeAnnotationAction();
        	makeAnnotationAction.setEditDomain(areaManager.getEditDomain());
        	manager.add(makeAnnotationAction);
        	areaManager.addKeyStrokeAction(KeyStroke.getReleased(SWT.F7, 0), makeAnnotationAction);
        	
        	MakeAnchorAction makeAnchorAction = new MakeAnchorAction();
        	makeAnchorAction.setEditDomain(areaManager.getEditDomain());
        	manager.add(makeAnchorAction);
        	areaManager.addKeyStrokeAction(KeyStroke.getReleased(SWT.F8, 0), makeAnchorAction);
       }
        
		MinimizeAllAction minimizeAllAction = new MinimizeAllAction();
		minimizeAllAction.updatePart(part);
		manager.add(minimizeAllAction);

		if(otherContributions != null && otherContributions.size() != 0){
			Iterator it = otherContributions.iterator();
			while(it.hasNext()){
				Object thing = it.next();
				if(thing instanceof IAction){
					IAction newAction = (IAction)thing;
					manager.add(newAction);
				} else if(thing instanceof IContributionItem){
					IContributionItem newContribution = (IContributionItem)thing;
					manager.add(newContribution);
				}
			}
		}
		manager.update(true);
	}

	public void dispose(){
	if(manager != null)manager.dispose();
		if(currentResource != null)currentResource.removePropertyChangeListener(this);
	}
	
	public Resource getResource() {
		return resourceHolder.getMyResource();
	}

	
	/**
	 * updates the name in the Pliny resource object from the the control panel's nameField Text object.
	 */
	public void updateResourceName(){
		Resource myResource = resourceHolder.getMyResource();
		String oldText = myResource.getName();
		String newText = nameField.getText();
		//String newText = nameField.getTextWidget().getText();
		if(oldText.equals(newText))return;
		if(myResource.getALID() == 0){
			((VirtualBrowserResource)myResource).makeMeReal();
		}
		commandStack.execute(new ResourceNameUpdateCommand(myResource, newText));
		//myResource.setName(newText);
	}
	
	public void changeResource(){
		currentResource.removePropertyChangeListener(this);
		currentResource = resourceHolder.getMyResource();
		currentResource.addPropertyChangeListener(this);
		nameField.setText(currentResource.getName());
		//nameField.getTextWidget().setText(currentResource.getName());
		rh.setResource(currentResource);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName()==Resource.NAME_PROP){
			String newName = currentResource.getName();
			if((!nameField.isDisposed()) && (!newName.equals(nameField.getText())))
				nameField.setText(newName);
			//StyledText theText = nameField.getTextWidget();
			//if((!nameWidget.isDisposed()) && (!newName.equals(nameWidget.getText())))
			//	nameWidget.setText(newName);
			return;
		}
	}

	public Composite getToolbarComp() {
		return toolbarComp;
	}

}
