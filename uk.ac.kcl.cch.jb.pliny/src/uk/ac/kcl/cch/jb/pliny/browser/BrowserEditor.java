/*******************************************************************************
 * Copyright (c) 2003, 2005, 2007, 2014 IBM Corporation, John Bradley and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *     John Bradley - modifications to fit Pliny model
 *******************************************************************************/

package uk.ac.kcl.cch.jb.pliny.browser;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorPart;

import uk.ac.kcl.cch.jb.pliny.IHandlesAnnotations;
import uk.ac.kcl.cch.jb.pliny.IResourceDrivenPart;
import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.ResourceAreaManager;
import uk.ac.kcl.cch.jb.pliny.actions.MakeConnectionAction;
import uk.ac.kcl.cch.jb.pliny.actions.MakeNoteAction;
import uk.ac.kcl.cch.jb.pliny.controls.PlinyToolBar;
import uk.ac.kcl.cch.jb.pliny.editors.IResourceChangeablePart;
import uk.ac.kcl.cch.jb.pliny.editors.ResourceChangingAnnouncer;
import uk.ac.kcl.cch.jb.pliny.editors.ResourceEditorInput;
//import uk.ac.kcl.cch.jb.pliny.editors.NoteEditor.SashSelectionAdapter;
import uk.ac.kcl.cch.jb.pliny.model.ObjectType;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.ResourceHolder;
import uk.ac.kcl.cch.jb.pliny.model.VirtualResource;

/**
 * A web browser integrated with Pliny so that it runs a browser and
 * a Pliny reference area separated by a SWT sash.  The browser's viewer part of the code is largely taken from WebBrowserEditor
 * in org.eclipse.ui.internal.browser.  We would have done an inherited
 * class for this except that this package supports no access outside of
 * itself.
 */

public class BrowserEditor extends EditorPart 
implements PropertyChangeListener, IHandlesAnnotations, IResourceDrivenPart, IResourceChangeablePart{
	
	protected BrowserViewer webBrowser;
	protected String initialURL;
	protected Image image;

	protected BrowserEditorCutAction cutAction;
	protected BrowserEditorCopyAction copyAction;
	protected BrowserEditorPasteAction pasteAction;
	
	private boolean disposed;
	
	private BrowserEditorInput input = null;
	
	public static final String BROWSER_ID = "uk.ac.kcl.cch.jb.pliny.browserEditor";
	
	private static final String browserContributionsId =
		"uk.ac.kcl.cch.jb.pliny.browserToolbarContribution";
	
	private static final int defaultSashPostion = 65;
	//private static final String resourceSashAttribute="sash=";
	private static final String sashPreferenceName="browserEditor.sash";
	
	private IBrowserToolbarContribution[] toolbarContributions = null;

	private PlinyToolBar controlPanel;
	private Control annotationArea;
	private ResourceHolder myResourceHolder = null;

	private ResourceAreaManager areaManager = null;

	//private GraphicalViewer graphicalViewer = null;
	//private EditDomain editDomain = null;
	//private ActionRegistry actionRegistry = null;

	private ResourceChangingAnnouncer resourceChangingAnnouncer = 
       new ResourceChangingAnnouncer();
	
	public BrowserEditor() {
		super();
	}
	
	private Image getIcon(){
		if(image == null)
			image = PlinyPlugin.getImageDescriptor("icons/browserIcon.gif").createImage();
		return image;
	}
	
	public Resource getMyResource(){
		if(myResourceHolder == null)return null;
		return myResourceHolder.getResource();
	}
	
	public BrowserViewer getWebBrowser(){
		return webBrowser;
	}
	
	private class BrowserFocusListener implements FocusListener{
		
		private void setActionFocus(boolean val){
			cutAction.setInBrowser(val);
			copyAction.setInBrowser(val);
			pasteAction.setInBrowser(val);
		}

		public void focusGained(FocusEvent e) {
			setActionFocus(true);
		}

		public void focusLost(FocusEvent e) {
			setActionFocus(false);
		}
		
	}
	
	public void defineBrowserArea(Composite parent){
		BrowserEditorInput input = getBrowserEditorInput();
		
		getToolbarContributions();

		webBrowser = new BrowserViewer(parent, this, toolbarContributions);

		webBrowser.getBrowser().addFocusListener(new BrowserFocusListener());
		webBrowser.combo.addFocusListener(new BrowserFocusListener());
		
		if((input != null) && (input.getUrl() != null))
		   webBrowser.setURL(input.getUrl().toExternalForm());
		
		PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (BrowserViewer.PROPERTY_TITLE.equals(event.getPropertyName())) {
					//setPartName((String) event.getNewValue());
					Resource myResource = myResourceHolder.getResource();
					if((myResource != null) && (myResource instanceof VirtualResource) &&
							myResource.getALID() == 0){
						myResource.setName((String)event.getNewValue());
						setPartName((String) event.getNewValue());
					}else if(myResource != null){
						setPartName(myResource.getName());
					}
				}
			}
		};
		webBrowser.addPropertyChangeListener(propertyChangeListener);
	}
	
	/*
	 * Creates the SWT controls for this workbench part.
	 */
	
	private class SashSelectionAdapter extends SelectionAdapter implements MouseListener {
		private Sash sash;
		int positionValue = -1;
		public SashSelectionAdapter(Sash sash){
			this.sash = sash;
		}
		
		public void widgetSelected(SelectionEvent event){
			   ((FormData) sash.getLayoutData()).left =
				   new FormAttachment(0, event.x);
			   Rectangle bounds = sash.getParent().getBounds();
			   //myNote.setSashPosition(event.x * 100 / bounds.width);
			   positionValue = event.x * 100 / bounds.width;
			   sash.getParent().layout();
			}

		@Override
		public void mouseDoubleClick(MouseEvent e) {
		}

		@Override
		public void mouseDown(MouseEvent e) {
		}

		@Override
		public void mouseUp(MouseEvent e) {
			if(positionValue < 0)return;
			VirtualBrowserResource myResource = (VirtualBrowserResource)getMyResource();
			if(myResource != null){
				//myResource.setAttributes(resourceSashAttribute+positionValue);
				myResource.setSashValue(positionValue);
			}
			PlinyPlugin.getDefault().getPluginPreferences().setValue(sashPreferenceName,positionValue);
			positionValue = -1;
		}
	}
	
	static private ObjectType browserObjectType = null;
	
	public Resource getCurrentResource(){
		Resource rslt = null;
		//String proposedIdentifier = null;
		String proposedIdentifier = "url:"+BrowserViewer.getHomeFromPreferences();
		if((input != null) && (input.getUrl() != null)){
		   String urlAsString = input.getUrl().toExternalForm();
		   urlAsString = urlAsString.replaceAll("\\#.*", ""); // crude code to remove URL's position value #xyz
		   //String proposedIdentifier = "url:"+input.getUrl();
		   proposedIdentifier = "url:"+urlAsString;
		   if(getMyResource() != null && proposedIdentifier.equals(getMyResource().getIdentifier()))
			   return getMyResource();
			
		   if(browserObjectType == null)browserObjectType=ObjectType.getItem(2);
		   //rslt = Resource.find(browserObjectType, input);
		   rslt = Resource.find(browserObjectType, proposedIdentifier);
		}
		//System.out.println("proposedIdentifier:"+proposedIdentifier+";getCurrentResource: rslt:"+rslt);
		//if(rslt == null && proposedIdentifier != null){
		if(rslt == null){
			rslt = new VirtualBrowserResource(webBrowser);
			//rslt.setObjectType(browserObjectType);
			//if(input != null)rslt.setIdentifiers(input);
			rslt.setIdentifiers(proposedIdentifier);
			//String partName = this.getPartName();
			String partName = input.getName();
			rslt.setName(partName);
			//rslt.setName(input.getUrl().toExternalForm());
		}
		return rslt;
	}
	
	public EditDomain getEditDomain(){
	    return areaManager.getEditDomain();
	}
	
	public CommandStack getCommandStack(){
		return getEditDomain().getCommandStack();
	}
	
	private int getSashPosition(VirtualBrowserResource resource){
		int rslt = PlinyPlugin.getDefault().getPluginPreferences().getInt(sashPreferenceName);
        if(rslt == 0){
        	rslt = defaultSashPostion;
			PlinyPlugin.getDefault().getPluginPreferences().setValue(sashPreferenceName,rslt);
        }
        
		if(resource != null){
			//String attr = resource.getAttributes();
			//if(attr.startsWith(resourceSashAttribute)){
			//	rslt = Integer.parseInt(attr.substring(resourceSashAttribute.length()));
			//} else resource.setAttributes("sash="+rslt);
			int trslt = resource.getSashValue();
			if(trslt == 0)resource.setSashValue(rslt);
			else rslt = trslt;
		}
		return rslt;
	}

	public void createPartControl(Composite parent) {
		VirtualBrowserResource currentResource = (VirtualBrowserResource)getCurrentResource();
		myResourceHolder = new ResourceHolder(currentResource);
		currentResource.addPropertyChangeListener(this);
		areaManager = new ResourceAreaManager(myResourceHolder, this, BROWSER_ID);

		GridLayout layout = new GridLayout(1,false);
		layout.verticalSpacing = 2;
		layout.marginWidth = 0;
		
		parent.setLayout(layout);
		parent.setBackground(PlinyPlugin.getBackgroundGray());

		Vector otherActions = new Vector();
        controlPanel = new PlinyToolBar(parent, this, areaManager, getIcon(), otherActions);
        Composite toolbarComp = controlPanel.getToolbarComp();
        GridData ldata = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        ldata.heightHint = 24;
        //GridData ldata = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        toolbarComp.setLayoutData(ldata);

        Composite restOfArea = new Composite(parent, SWT.None);
        GridData ldatar = new GridData(GridData.FILL_BOTH);
        restOfArea.setLayoutData(ldatar);
		
        restOfArea.setLayout(new FormLayout());
		final Sash sash = new Sash(restOfArea, SWT.VERTICAL);
		FormData data = new FormData();
		data.top = new FormAttachment(0,0); // Attach to top
		data.bottom = new FormAttachment(100,0); // Attach to bottom
		data.left = new FormAttachment(getSashPosition(currentResource), 0); 
		sash.setLayoutData(data);
		
		// create "browser area"
		
		defineBrowserArea(restOfArea);
		data = new FormData();
		data.top = new FormAttachment(0,0);
		data.bottom = new FormAttachment(100,0);
		data.left = new FormAttachment(0,0);
		data.right = new FormAttachment(sash, 0);
		webBrowser.setLayoutData(data);
		
		// create "annotation area"
		
		// webBrowser.giveBusyIndicatorResource(currentResource);
		
		annotationArea = areaManager.createGraphicalViewer(restOfArea);
		data = new FormData();
		data.top = new FormAttachment(0,0);
		data.bottom = new FormAttachment(100,0);
		data.left = new FormAttachment(sash,0);
		data.right = new FormAttachment(100, 0);
		annotationArea.setLayoutData(data);
		
		// make sash boundary movable
		
		//sash.addSelectionListener(new SashSelectionAdapter(sash));
		SashSelectionAdapter mySashAdapter = new SashSelectionAdapter(sash);
		sash.addSelectionListener(mySashAdapter);
		sash.addMouseListener(mySashAdapter);
		

		
		createActions();
	}
	
	protected ActionRegistry getActionRegistry(){
		return areaManager.getActionRegistry();			
	}
	
	protected void createActions(){
		ActionRegistry registry = areaManager.getActionRegistry();

		cutAction = new BrowserEditorCutAction(this);
		areaManager.addSelectionAction(cutAction);
        registry.registerAction(cutAction);
		copyAction = new BrowserEditorCopyAction(this);
		areaManager.addSelectionAction(copyAction);
        registry.registerAction(copyAction);
		pasteAction = new BrowserEditorPasteAction(this);
		areaManager.addSelectionAction(pasteAction);
        registry.registerAction(pasteAction);

        //minimizeAllAction = new MinimizeAllAction(this);

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

		id = ActionFactory.SELECT_ALL.getId();
		bars.setGlobalActionHandler(id, registry.getAction(id));

		//IToolBarManager manager = bars.getToolBarManager();
		//manager.add(minimizeAllAction);

		bars.updateActionBars();

		MakeNoteAction mnAction = new MakeNoteAction();
		mnAction.setEditDomain(areaManager.getEditDomain());
		areaManager.addKeyStrokeAction(KeyStroke.getReleased(SWT.F5, 0), mnAction);
		MakeConnectionAction mcAction = new MakeConnectionAction();
		mcAction.setEditDomain(areaManager.getEditDomain());
		areaManager.addKeyStrokeAction(KeyStroke.getReleased(SWT.F6, 0), mcAction);
}
	
	private void getToolbarContributions() {
		IExtensionRegistry registry= Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint= registry.getExtensionPoint(browserContributionsId);
		IExtension[] extensions= extensionPoint.getExtensions();
		Vector contrs = new Vector();
		for (int i= 0; i < extensions.length; i++) {
			IConfigurationElement[] elements= extensions[i].getConfigurationElements();
			for (int j= 0; j < elements.length; j++) {
				try {
					Object item= elements[j].createExecutableExtension("class");
					if (item instanceof IBrowserToolbarContribution)
						contrs.add(item);

				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
		if(contrs.size() > 0)
			toolbarContributions = (IBrowserToolbarContribution[])
				contrs.toArray(new IBrowserToolbarContribution[0]);
	}

	public void dispose() {
		if (image != null && !image.isDisposed())
			image.dispose();
		image = null;

		if(areaManager != null)areaManager.dispose();
		if(cutAction != null)cutAction.dispose();
		if(copyAction != null)copyAction.dispose();
		if(pasteAction != null)pasteAction.dispose();


		super.dispose();
		disposed = true;
	}
	
	public boolean isDisposed() {
		return disposed;
	}
	
	public void doSave(IProgressMonitor monitor) {
		// do nothing
	}

	public void doSaveAs() {
		// do nothing
	}
	
	/**
	 * Returns the copy action.
	 *
	 * @return org.eclipse.jface.action.IAction
	 */
	public IAction getCopyAction() {
		return copyAction;
	}
	
	/**
	 * Returns the cut action.
	 *
	 * @return org.eclipse.jface.action.IAction
	 */
	public IAction getCutAction() {
		return cutAction;
	}

	/**
	 * Returns the paste action.
	 *
	 * @return org.eclipse.jface.action.IAction
	 */
	public IAction getPasteAction() {
		return pasteAction;
	}
	
	/**
	 * Returns the web editor input, if available. If the input was of
	 * another type, <code>null</code> is returned.
	 *
	 * @return org.eclipse.ui.internal.browser.IWebBrowserEditorInput
	 */
	protected BrowserEditorInput getBrowserEditorInput() {
		IEditorInput input = getEditorInput();
		if (input instanceof BrowserEditorInput)
			return (BrowserEditorInput) input;
		return null;
	}

	/* (non-Javadoc)
	 * Initializes the editor part with a site and input.
	 */
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		//Trace.trace(Trace.FINEST, "Opening browser: " + input); //$NON-NLS-1$
		if (input instanceof ResourceEditorInput){
			Resource theResource = ((ResourceEditorInput)input).getMyResource();
			String id = theResource.getIdentifier();
			if(!id.startsWith("url:"))throw new PartInitException("Wrong Resource type given as input: "+input);
			String urlString = id.substring(4);
			try {
				input = new BrowserEditorInput(urlString);
			} catch (MalformedURLException e) {
				throw new PartInitException("bad URL provided: "+ urlString);
			}
			if(myResourceHolder == null)
				myResourceHolder = new ResourceHolder(theResource);
		}
		if (input instanceof IPathEditorInput) {
			IPathEditorInput pei = (IPathEditorInput) input;
			IPath path = pei.getPath();
			URL url = null;
			try {
				if (path != null && path.toFile().exists())
					url = path.toFile().toURL();
			} catch (Exception e) {
				throw new PartInitException("Error getting URL from file: "+e.getLocalizedMessage());
				//Trace.trace(Trace.SEVERE, "Error getting URL to file"); //$NON-NLS-1$
			}
			initialURL = url.toExternalForm();
			if (webBrowser != null) {
				webBrowser.setURL(initialURL);
				site.getWorkbenchWindow().getActivePage().activate(this);
			}
			
			setPartName(path.lastSegment());
			setTitleToolTip(url.getFile());

			Image oldImage = image;
			//ImageDescriptor id = PlinyPlugin.getImageDescriptor("icons/browser/internal_browser.gif");
			ImageDescriptor id = PlinyPlugin.getImageDescriptor("icons/browserIcon.gif");
			image = id.createImage();

			setTitleImage(image);
			if (oldImage != null && !oldImage.isDisposed())
				oldImage.dispose();
			try {
				this.input = new BrowserEditorInput(initialURL);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				throw new PartInitException("Error 2 getting URL from file: "+e.getLocalizedMessage());
			}
			//addResourceListener(file);
		} else if (input instanceof BrowserEditorInput) {
			this.input = (BrowserEditorInput)input;
			BrowserEditorInput wbei = (BrowserEditorInput) input;
			initialURL = null;
			if (wbei.getUrl() != null)
				initialURL = wbei.getUrl().toExternalForm();
			if (webBrowser != null) {
				webBrowser.setURL(initialURL);
				site.getWorkbenchWindow().getActivePage().activate(this);
			}
	
			setPartName(wbei.getName());
			setTitleToolTip(wbei.getToolTipText());

			Image oldImage = image;
			ImageDescriptor id = wbei.getImageDescriptor();
			image = id.createImage();

			setTitleImage(image);
			if (oldImage != null && !oldImage.isDisposed())
				oldImage.dispose();
		} else
			throw new PartInitException("invalid input for the Browser:"+input.getName());
		
		setSite(site);
		setInput(input);
	}
	
	// this code was here to facilitate debugging.   ..jb
	
	//protected void setPartName(String partName){
	//	System.out.println("setPartName: "+partName);
	//	super.setPartName(partName);
	//}
	
	/* (non-Javadoc)
	 * Returns whether the contents of this editor have changed since the last save
	 * operation.
	 */
	public boolean isDirty() {
		return false;
	}
	
	/* (non-Javadoc)
	 * Returns whether the "save as" operation is supported by this editor.
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	/*
	 * Asks this part to take focus within the workbench.
	 */
	public void setFocus() {
		if (webBrowser != null)
			webBrowser.setFocus();
	}

	/**
	 * Close the editor correctly.
	 */
	public boolean close() {
        final boolean [] result = new boolean[1];
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				result[0] = getEditorSite().getPage().closeEditor(BrowserEditor.this, false);
			}
		});
        return result[0];
	}
	
    public IActionBars getActionBars() {
        return getEditorSite().getActionBars();
    }
	
	public Object getAdapter(Class adapter){
		if(adapter == CommandStack.class)
			return getCommandStack(); 
		if(adapter == ActionRegistry.class)
			return getActionRegistry();
		if(adapter == GraphicalViewer.class)
			return areaManager.getGraphicalViewer();
		return super.getAdapter(adapter);
	}
    
    public void updateLocation(String newURL){
    	//System.out.println("updateLocation:"+newURL);
    	URL theURL;
    	try {
			theURL = new URL(newURL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		}
		input.setUrl(theURL);
		//AnnotationView av = getAnnotationView();
		if(toolbarContributions != null){
			for(int i = 0; i < toolbarContributions.length; i++){
				toolbarContributions[i].setUrl(theURL);
			}
		}
		//if(av != null)av.refreshPart(this);
		Resource currentResource = getCurrentResource();
		Resource oldResource = myResourceHolder.getResource();
		oldResource.removePropertyChangeListener(this);
		myResourceHolder.setResource(currentResource);
		controlPanel.changeResource();
		currentResource.addPropertyChangeListener(this);
		// webBrowser.giveBusyIndicatorResource(currentResource);
		if(oldResource != currentResource)
		   resourceChangingAnnouncer.announceResource(oldResource, currentResource);
		setPartName(currentResource.getFullName());
    }
    
    public void updateTitle(String title){
    	//System.out.println("updateTitle:"+title);
    	input.setTitle(title);
    	//setPartName(title);
		Resource currentResource = getCurrentResource();
		if(currentResource.getALID() == 0){
			currentResource.setName(title);
			setPartName(title);
		} else setPartName(currentResource.getName());
		if(toolbarContributions != null){
			for(int i = 0; i < toolbarContributions.length; i++){
				toolbarContributions[i].setTitle(title);
			}
		}
    }

	public void addPropertyChangeListener(PropertyChangeListener l) {
		if(resourceChangingAnnouncer != null)
			resourceChangingAnnouncer.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		if(resourceChangingAnnouncer != null)
			resourceChangingAnnouncer.removePropertyChangeListener(l);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName()==Resource.NAME_PROP){
			Resource currentResource = getMyResource();
			String newName = currentResource.getName();
			setPartName(newName);
		}
	}
}
