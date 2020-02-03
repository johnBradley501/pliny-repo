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

package uk.ac.kcl.cch.jb.pliny.editors;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
//import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.PrintAction;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorPart;

import uk.ac.kcl.cch.jb.pliny.IHandlesAnnotations;
import uk.ac.kcl.cch.jb.pliny.IResourceDrivenPart;
import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.ResourceAreaManager;
import uk.ac.kcl.cch.jb.pliny.actions.CopyPlinyAction;
import uk.ac.kcl.cch.jb.pliny.actions.CutPlinyAction;
import uk.ac.kcl.cch.jb.pliny.actions.GenerateContentsAsTextAction;
import uk.ac.kcl.cch.jb.pliny.actions.MakeConnectionAction;
import uk.ac.kcl.cch.jb.pliny.actions.MakeNoteAction;
import uk.ac.kcl.cch.jb.pliny.actions.PastePlinyAction;
import uk.ac.kcl.cch.jb.pliny.commands.NoteTextUpdateCommand;
import uk.ac.kcl.cch.jb.pliny.commands.UpdateNameCommand;
import uk.ac.kcl.cch.jb.pliny.controls.PlinyToolBar;
import uk.ac.kcl.cch.jb.pliny.dnd.DnDResourceHolder;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * an EditorPart that implements the Note Editor.
 * <p>
 * Note that the input the Note Editor is to work on is specified by
 * a {@link NoteEditorInput} instance.
 * 
 * @author John Bradley
 *
 */

public class NoteEditor extends EditorPart 
implements PropertyChangeListener, /*ModifyListener, */ IHandlesAnnotations, 
IResourceDrivenPart/* , ISelectionListener*/{
	
	private static Image icon = null;
	
	private static final Color titleAreaColour = ColorConstants.lightGray;
	private static final String NOTEEDITOR_ID="uk.ac.kcl.cch.jb.pliny.noteEditor";
	
	private Image getIcon(){
		if(icon == null)
			icon = PlinyPlugin.getImageDescriptor("icons/noteIcon.gif").createImage();
		return icon;
	}
	
	private class MyResourceAreaManager extends ResourceAreaManager {

		public MyResourceAreaManager(Object rootObject, IWorkbenchPart myPart, String ownerID) {
			super(rootObject, myPart, ownerID);
		}
		
	    protected void handleSelectionProvider(){
			mySelectionProvider.addSelectionProvider(graphicalViewer);
		}
		
	}
	
	private NoteLucened myNote;
	//private Text contentsWidget;
	private StyledText contentsText;
	//private StyledText titleText;
	//private Text titleWidget;
	private Control annotationArea;
	private MultiSourceSelectionProvider mySelectionProvider = null;
	
	private PlinyToolBar controlPanel;
	
	// private TitleFocusListener titleFocusListener = null;
	private ContentFocusListener contentFocusListener = null;
	
	private ResourceAreaManager areaManager = null;
	private PastePlinyAction pasteAction;
	private CopyPlinyAction copyAction;
	private CutPlinyAction cutAction;

	private boolean isDirty = false;
	
	public NoteEditor() {
		super();
		// TODO Auto-generated constructor stub
	} 
	
	public void init(IEditorSite site, IEditorInput input)
	throws PartInitException {
		if(!(input instanceof NoteEditorInput)){
			throw new PartInitException("specified input is not a Note");
		}
		setSite(site);
		setInput(input);
		
		createActions();
	}
	
	public void dispose(){
		super.dispose();
		//if((titleFocusListener != null) && (!titleText.isDisposed()))titleText.removeFocusListener(titleFocusListener);
		//if(contentFocusListener != null)contentsWidget.removeFocusListener(contentFocusListener);
		if((contentFocusListener != null) && (!contentsText.isDisposed()))contentsText.removeFocusListener(contentFocusListener);
		//?? areaManager.getGraphicalViewer().removeSelectionChangedListener(this);
    	// pasteAction.dispose();  // these actions, as selection actions known to the areaManager
    	// copyAction.dispose();   // are now deleted by the areaManager instead.    .. jb
    	// cutAction.dispose();
    	areaManager.dispose();
    	//if(icon != null && (!icon.isDisposed()))icon.dispose();
    	
    	if(myNote != null)myNote.removePropertyChangeListener(this);

    	controlPanel.dispose();
    }
	
	public void setDirty(boolean dirty){
		if(isDirty != dirty){
			isDirty = dirty;
			this.firePropertyChange(IEditorPart.PROP_DIRTY);
		}
	}

	public void doSave(IProgressMonitor monitor) {
		//if(!myNote.getName().equals(titleText.getText()))
		//	myNote.setName(titleText.getText());
		//if(!myNote.getContent().equals(contentsWidget.getText()))
		//	myNote.setContent(contentsWidget.getText());
		if(!myNote.getContent().equals(contentsText.getText()))
			myNote.setContent(contentsText.getText());
		setDirty(false);
	}

	public void doSaveAs() {
		// do nothing
	}

	public boolean isDirty() {
		return isDirty;
	}
	
    public boolean isSaveOnCloseNeeded() {
        return false;
    }

	public boolean isSaveAsAllowed() {
		return false;
	}
	
	private class MultiSourceSelectionProvider 
	implements ISelectionProvider, ISelectionChangedListener {

		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
		 * modelled after org.eclipse.ui.part.MultiPageSelectionProvider
		 */
	    private ListenerList listeners = new ListenerList();
	    //private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);
	    private Set providers = new HashSet();
	    private ISelectionProvider lastProvider = null;

	    public void addSelectionChangedListener(ISelectionChangedListener listener) {
			listeners.add(listener);
			
		}
	    
	    public void dispose(){
	    	Iterator it = providers.iterator();
	    	while(it.hasNext()){
	    		ISelectionProvider provider = (ISelectionProvider)it.next();
	    		provider.removeSelectionChangedListener(this);
	    	}
	    }

		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			listeners.remove(listener);
		}

		public void addSelectionProvider(ISelectionProvider provider){
			provider.addSelectionChangedListener(this);
			providers.add(provider);
		}
		
		public void removeSelectionProvider(ISelectionProvider provider){
			if(!providers.contains(provider))return;
			provider.removeSelectionChangedListener(this);
			providers.remove(provider);
		}
		
	    public void fireSelectionChanged(final SelectionChangedEvent event) {
	        Object[] listeners = this.listeners.getListeners();
	        for (int i = 0; i < listeners.length; ++i) {
	            final ISelectionChangedListener l = (ISelectionChangedListener) listeners[i];
	            Platform.run(new SafeRunnable() {
	                public void run() {
	                    l.selectionChanged(event);
	                }
	            });
	        }
	    }

		public ISelection getSelection(){
			if(lastProvider == null)return null;
			return lastProvider.getSelection();
		}

		public void setSelection(ISelection selection) {
			System.out.println("NoteEditor: unexpected set selection in MultiSourceSelectionProvider");
		}

		public void selectionChanged(SelectionChangedEvent event) {
			lastProvider = event.getSelectionProvider();
			//System.out.println("MultiSourceSelectionProvider event: "+event);
			fireSelectionChanged(event);
			
		}
		
	}
	
	private void giveActionsStyledText(StyledText styledText){
		cutAction.setStyledText(styledText);
		copyAction.setStyledText(styledText);
		pasteAction.setStyledText(styledText);
	}
	
	private class ContentFocusListener implements FocusListener{
		
		protected StyledText myText;
		protected NoteLucened myNote;
		
		public ContentFocusListener(StyledText myText, NoteLucened myNote){
			this.myText = myText;
			this.myNote = myNote;
		}

		public void focusGained(FocusEvent e) {
			giveActionsStyledText(myText);
			
		}

		public void focusLost(FocusEvent e) {
			if(!myNote.getContent().equals(myText.getText()))
				getCommandStack().execute(
						new NoteTextUpdateCommand(myNote, myText.getText()));
			giveActionsStyledText(null);
		}
		
	}
	
	/*private class TitleFocusListener extends ContentFocusListener {
		public TitleFocusListener(StyledText myText, NoteLucened myNote){
			super(myText, myNote);
		}
		public void focusLost(FocusEvent e) {
			if(!myNote.getName().equals(myText.getText()))
				getCommandStack().execute(
						new UpdateNameCommand(null, myNote, myText.getText()));
			giveActionsStyledText(null);
		}

	} */
	
	private Composite defineNoteArea(Composite parent){
/*		Composite noteArea = new Composite(parent, SWT.BORDER);
		GridLayout layout = new GridLayout(1,true);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		noteArea.setLayout(layout);
		GridData gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.heightHint = 18;
		
		Composite titleArea = new Composite(noteArea, SWT.NONE);
		titleArea.setLayout(new GridLayout(2, false));
		//titleArea.setBackground(ColorConstants.green);
		titleArea.setBackground(titleAreaColour);
		titleArea.setLayoutData(gdata);
		
		Composite titleHolder = new Composite(titleArea, SWT.NONE);
		titleHolder.setLayout(new FillLayout());
		titleHolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		TextViewer viewer = new TextViewer(titleHolder, SWT.SINGLE);
		titleText = viewer.getTextWidget();
		titleText.setBackground(titleAreaColour);
		viewer.setDocument(new Document(myNote.getName()));
		titleFocusListener = new TitleFocusListener(titleText, myNote);
		titleText.addFocusListener(titleFocusListener);
		mySelectionProvider.addSelectionProvider(viewer);

		
		DnDResourceHolder myIcon = new DnDResourceHolder(titleArea, SWT.NONE);
		myIcon.getLabel().setBackground(titleAreaColour);
		myIcon.getLabel().setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
		myIcon.getLabel().setImage(getIcon());
		myIcon.setResource(myNote);
		
		gdata = new GridData(GridData.FILL_BOTH);
		Composite noteHolder = new Composite(noteArea, SWT.NONE); */
		Composite noteHolder = new Composite(parent, SWT.NONE);
		noteHolder.setLayout(new FillLayout(SWT.VERTICAL));
		//noteHolder.setLayoutData(gdata);
		TextViewer viewer = new TextViewer(noteHolder, SWT.V_SCROLL | SWT.BORDER);
		contentsText = viewer.getTextWidget();
		contentsText.setWordWrap(true);
		viewer.setDocument(new Document(myNote.getContent()));
		//contentText.addModifyListener(this);
		contentFocusListener = new ContentFocusListener(contentsText, myNote);
		contentsText.addFocusListener(contentFocusListener);
		mySelectionProvider.addSelectionProvider(viewer);
		
		//viewer.setDocument(new Document(myNote.getContent()));
		
		//contentsWidget = new Text(noteArea, SWT.V_SCROLL | SWT.WRAP);
		//contentsWidget.setLayoutData(gdata);
		//contentsWidget.setText(myNote.getContent());
		//contentFocusListener = new ContentFocusListener(contentsWidget, myNote);
		////contentsWidget.addModifyListener(this);
		//contentsWidget.addFocusListener(contentFocusListener);
		
		//return noteArea;
		return noteHolder;
	}
	
	public CommandStack getCommandStack(){
		return areaManager.getEditDomain().getCommandStack();
	}
	
	public EditDomain getEditDomain(){
		return areaManager.getEditDomain();
	}

	public void createPartControl(Composite parent) {
		mySelectionProvider = new MultiSourceSelectionProvider();
		getSite().setSelectionProvider(mySelectionProvider);
		mySelectionProvider.addSelectionChangedListener(areaManager);
		
		GridLayout layout = new GridLayout(1,false);
		layout.verticalSpacing = 2;
		layout.marginWidth = 0;
		
		parent.setLayout(layout);
		parent.setBackground(PlinyPlugin.getBackgroundGray());

		Vector otherActions = new Vector();
		otherActions.add(new GenerateContentsAsTextAction(this.getMyResource()));
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
		data.left = new FormAttachment(myNote.getSashPosition(), 0); // Attach halfway across
		sash.setLayoutData(data);
		
		// create "note area"
		
		Composite noteArea = defineNoteArea(restOfArea);
		data = new FormData();
		data.top = new FormAttachment(0,0);
		data.bottom = new FormAttachment(100,0);
		data.left = new FormAttachment(0,0);
		data.right = new FormAttachment(sash, 0);
		noteArea.setLayoutData(data);
		
		// create "annotation area"
		
		annotationArea = areaManager.createGraphicalViewer(restOfArea);
		data = new FormData();
		data.top = new FormAttachment(0,0);
		data.bottom = new FormAttachment(100,0);
		data.left = new FormAttachment(sash,0);
		data.right = new FormAttachment(100, 0);
		annotationArea.setLayoutData(data);
		
		// make sash boundary movable
		
		SashSelectionAdapter mySashAdapter = new SashSelectionAdapter(sash);
		sash.addSelectionListener(mySashAdapter);
		sash.addMouseListener(mySashAdapter);
		
		//MakeNoteAction mnAction = new MakeNoteAction();
		//mnAction.setEditDomain(areaManager.getEditDomain());
		//areaManager.addKeyStrokeAction(KeyStroke.getReleased(SWT.F5, 0), mnAction);
		//MakeConnectionAction mcAction = new MakeConnectionAction();
		//mcAction.setEditDomain(areaManager.getEditDomain());
		//areaManager.addKeyStrokeAction(KeyStroke.getReleased(SWT.F6, 0), mcAction);
	}
	
	private class SashSelectionAdapter extends SelectionAdapter implements MouseListener{
		private Sash sash;
		int positionValue = -1;
		public SashSelectionAdapter(Sash sash){
			this.sash = sash;
		}
		
		public void widgetSelected(SelectionEvent event){
			   ((FormData) sash.getLayoutData()).left =
				   new FormAttachment(0, event.x);
			   Rectangle bounds = sash.getParent().getBounds();
			   positionValue = event.x * 100 / bounds.width;
			   sash.getParent().layout();
			}

		@Override
		public void mouseDoubleClick(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseDown(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseUp(MouseEvent e) {
			//System.out.println("Sash mouse released");
			if(positionValue < 0)return;
			myNote.setSashPosition(positionValue);
			positionValue = -1;
		}
	}

	protected void createActions(){
		
		// see http://www.eclipsezone.com/eclipse/forums/t72707.html
		ActionRegistry registry = areaManager.getActionRegistry();

		cutAction = new CutPlinyAction(this);
        areaManager.addSelectionAction(cutAction);
        //registry.registerAction(cutAction);
        copyAction = new CopyPlinyAction(this);
        areaManager.addSelectionAction(copyAction);
        registry.registerAction(copyAction);
        pasteAction = new PastePlinyAction(this);
        areaManager.addSelectionAction(pasteAction);
        //registry.registerAction(pasteAction);
        
        PrintAction pAction = new PrintAction(this);
		
		registry.registerAction(pAction);
       
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
	}

	public void setFocus() {
		annotationArea.setFocus();
		//contentsWidget.setFocus();
	}
	
	public Object getAdapter(Class adapter){
		if(adapter == CommandStack.class)
			return getCommandStack(); // needed in MinimizeAllAction
		                              // which is a WorkbenchPartAction
		if(adapter == ActionRegistry.class)
			return areaManager.getActionRegistry();
		if(adapter == GraphicalViewer.class)
			return areaManager.getGraphicalViewer();
		return super.getAdapter(adapter);
	}
	
	protected void setInput(IEditorInput input){
		super.setInput(input);
		if(!(input instanceof NoteEditorInput))return;
		myNote = ((NoteEditorInput)input).getMyNote();
		setPartName(myNote.getName());
		myNote.addPropertyChangeListener(this);
		areaManager = new MyResourceAreaManager(myNote, this, NOTEEDITOR_ID);
	}

	public void propertyChange(PropertyChangeEvent arg0) {
	       if(arg0.getPropertyName()==NoteLucened.NAME_PROP){
	           setPartName(myNote.getName());
	           // titleText.setText(myNote.getName());
	       } else if(arg0.getPropertyName()==NoteLucened.CONTENT_PROP){
	    	   if(!myNote.getContent().equals(contentsText.getText()))
	    		   contentsText.setText(myNote.getContent());
	       }
	}

	//public void modifyText(ModifyEvent e) {
	//	//myNote.setContent(contentText.getText());
	//	setDirty(true);
	//}
	
	public void updatePasteLocation(){
		pasteAction.update();
	}

	public Resource getMyResource() {
		return myNote;
	}
	
	/*
	public Vector getSelectedBaseObjects(){
		IStructuredSelection selection = (IStructuredSelection)graphicalViewer.getSelection();
		Vector rslts = new Vector();
		Iterator iter = selection.iterator();
		while(iter.hasNext()){
			Object item = iter.next();
			if(item instanceof BaseObject)rslts.add(item);
		}
		return rslts;
	}
	*/
	
	public String toString(){
		return "NoteEditor myNote: "+myNote;
	}
}
