/*******************************************************************************
 * Copyright (c) 2007, 2014 John Bradley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Bradley - initial API and implementation
 *******************************************************************************/

package uk.ac.kcl.cch.jb.pliny.imageRes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.PrintAction;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorPart;

import uk.ac.kcl.cch.jb.pliny.IHandlesAnnotations;
import uk.ac.kcl.cch.jb.pliny.IResourceDrivenPart;
import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.ResourceAreaManager;
import uk.ac.kcl.cch.jb.pliny.actions.CopyPlinyAction;
import uk.ac.kcl.cch.jb.pliny.actions.CutPlinyAction;
import uk.ac.kcl.cch.jb.pliny.actions.PastePlinyAction;
import uk.ac.kcl.cch.jb.pliny.actions.PlinySelectAllAction;
import uk.ac.kcl.cch.jb.pliny.controls.IZoomableImageEditor;
import uk.ac.kcl.cch.jb.pliny.controls.IZoomableImageResource;
import uk.ac.kcl.cch.jb.pliny.controls.PlinyToolBar;
import uk.ac.kcl.cch.jb.pliny.controls.ZoomControlSpinnerContribution;
import uk.ac.kcl.cch.jb.pliny.editors.ResourceEditorInput;
import uk.ac.kcl.cch.jb.pliny.imageRes.ImageResPlugin;
import uk.ac.kcl.cch.jb.pliny.imageRes.model.ImageResource;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * an EditorPart that implements the Pliny Image Resource Editor.
 * <p>
 * Note that the input the Image Editor is to work on is specified by
 * a {@link uk.ac.kcl.cch.jb.pliny.editors.ResourceEditorInput ResourceEditorInput} instance.
 * 
 * @author John Bradley
 *
 */

public class ImageEditor extends EditorPart 
implements PropertyChangeListener, IResourceDrivenPart, IHandlesAnnotations, IZoomableImageEditor{

	public static final String EDITOR_ID = "uk.ac.kcl.cch.jb.pliny.imageRes.editor";

	
	private ImageResource myModel;
	private static Image icon = null;
	
	private ResourceAreaManager areaManager = null;

	// private Text nameField = null;
	private ZoomControlSpinnerContribution zoomSpinner;
	private PlinyToolBar controlPanel;


	private Control annotationArea;
	
	public ImageEditor() {
		super();
	}
	
	public EditDomain getMyEditDomain(){
		return areaManager.getEditDomain();
	}
	
	@Override
	public IZoomableImageResource getMyImageResource(){
		return myModel;
	}
	
	@Override
	public Resource getMyResource(){
		return myModel;
	}
	
	private Image getIcon(){
		if(icon == null)
			icon = ImageResPlugin.getImageDescriptor("icons/imageIcon.gif").createImage();
		return icon;
	}
	
	@Override
	public void init(IEditorSite site, IEditorInput input)
	throws PartInitException {
		if(!(input instanceof ResourceEditorInput)){
			throw new PartInitException("specified input was not a Resource");
		}
		setSite(site);
		setInput(input);

		areaManager = new ResourceAreaManager(myModel, this, EDITOR_ID);
		areaManager.setMyFactory(new ImageEditorPartFactory(this));
		
		createActions();

	}
	
	@Override
	protected void setInput(IEditorInput input){
		super.setInput(input);
		if(!(input instanceof ResourceEditorInput))return;
		myModel = (ImageResource)(((ResourceEditorInput)input).getMyResource());
		setPartName(myModel.getName());
		myModel.addPropertyChangeListener(this);
	}
	
	@Override
	public void dispose(){
		if(icon != null)icon.dispose();
		icon = null;
		controlPanel.dispose();
		areaManager.dispose();
		if(zoomSpinner != null)zoomSpinner.dispose();
		if(myModel != null)myModel.removePropertyChangeListener(this);
		super.dispose();
	}
	
	@Override
	public void createPartControl(Composite parent){
		parent.setLayout(new GridLayout(1,false));
		GridData gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.heightHint = 18;
		
		parent.setBackground(PlinyPlugin.getBackgroundGray());

		buildToolbar(parent);
		
		defineAnnotationArea(parent);

	}
	
	private void buildToolbar(Composite parent) {
		Vector contributions = new Vector();
		contributions.add(new ZoomControlSpinnerContribution("#zoom_control", this));
		controlPanel = new PlinyToolBar(parent, this, areaManager, getIcon(), true, contributions);
		
        Composite toolbarComp = controlPanel.getToolbarComp();
        GridData ldata = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        ldata.heightHint = 24;
        toolbarComp.setLayoutData(ldata);
	}
	
	private void defineAnnotationArea(Composite parent){
		annotationArea = areaManager.createGraphicalViewer(parent);
		annotationArea.setLayoutData(new GridData(GridData.FILL_BOTH));
		
	}

	protected void createActions(){
		ActionRegistry registry = areaManager.getActionRegistry();
		IAction action;
        SelectionAction saction = new CutPlinyAction(this);
        //registry.registerAction(saction);
        areaManager.addSelectionAction(saction);
        saction = new CopyPlinyAction(this);
        //registry.registerAction(action);
        //getSelectionActions().add(action.getId());
        areaManager.addSelectionAction(saction);
        saction = new PastePlinyAction(this);
        //registry.registerAction(action);
        //getSelectionActions().add(action.getId());
        areaManager.addSelectionAction(saction);
        
        action = new PrintAction(this);
        registry.registerAction(action);

        Action selectAllAction = new PlinySelectAllAction(this);
        registry.registerAction(selectAllAction);

		IActionBars bars = ((IEditorSite)getSite()).getActionBars();
		String id = ActionFactory.UNDO.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
		id = ActionFactory.REDO.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
		id = ActionFactory.DELETE.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
		id = ActionFactory.COPY.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
		id = ActionFactory.CUT.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
		id = ActionFactory.PASTE.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));
		bars.updateActionBars();
		id = ActionFactory.SELECT_ALL.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));

		bars.updateActionBars();
	}
	
	@Override
	public CommandStack getCommandStack(){
		return areaManager.getEditDomain().getCommandStack();
	}
	
    public GraphicalViewer getMyGraphicViewer(){
    	return areaManager.getGraphicalViewer();
    }

	@Override
	public void doSave(IProgressMonitor monitor) {
		// not needed here -- model does save to database    .. jb
	}
	
    @Override
	public boolean isSaveOnCloseNeeded() {
        return false;
    }

	@Override
	public void doSaveAs() {
		// not needed here    .. jb
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void setFocus() {
		annotationArea.setFocus();
		
	}

	@Override
	public Object getAdapter(Class adapter) {
		if(adapter == CommandStack.class)
			return getCommandStack(); // needed in MinimizeAllAction
		                              // which is a WorkbenchPartAction
		if(adapter == ActionRegistry.class)
			return areaManager.getActionRegistry();
		if(adapter == GraphicalViewer.class)
			return areaManager.getGraphicalViewer();
		return super.getAdapter(adapter);
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}
	

	public void propertyChange(PropertyChangeEvent arg0) {
	       if(arg0.getPropertyName()==NoteLucened.NAME_PROP){
	           setPartName(myModel.getName());
	       }
	}

	
}