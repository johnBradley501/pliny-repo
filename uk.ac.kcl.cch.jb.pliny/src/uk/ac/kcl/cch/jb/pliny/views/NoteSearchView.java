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

package uk.ac.kcl.cch.jb.pliny.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Date;
import java.util.Iterator;
import java.util.Vector;

import org.apache.lucene.queryParser.ParseException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.actions.CommandStackRedoAction;
import uk.ac.kcl.cch.jb.pliny.actions.CommandStackUndoAction;
import uk.ac.kcl.cch.jb.pliny.actions.ResourceExplorerOpenAction;
import uk.ac.kcl.cch.jb.pliny.dnd.ResourceExplorerDragListener;
import uk.ac.kcl.cch.jb.pliny.lucene.NoteTextIndex;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.Note;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.views.utils.DateFilterManager;
import uk.ac.kcl.cch.jb.pliny.views.utils.IResourceExplorerItem;
import uk.ac.kcl.cch.jb.pliny.views.utils.IResourceTreeDisplayer;
import uk.ac.kcl.cch.jb.pliny.views.utils.NewNoteWizard;
import uk.ac.kcl.cch.jb.pliny.views.utils.NoteSearchRoot;
import uk.ac.kcl.cch.jb.pliny.views.utils.ResourceExplorerCellModifier;
import uk.ac.kcl.cch.jb.pliny.views.utils.ResourceExplorerContentProvider;
import uk.ac.kcl.cch.jb.pliny.views.utils.ResourceExplorerDisplayedInItem;
import uk.ac.kcl.cch.jb.pliny.views.utils.ResourceExplorerLabelProvider;
import uk.ac.kcl.cch.rdb2java.Rdb2javaPlugin;

/**
 * the Eclipse ViewPart for Pliny's Note Search View.
 * 
 * @author John Bradley
 *
 */

public class NoteSearchView extends ViewPart 
implements IResourceTreeDisplayer, PropertyChangeListener{

	private StyledText queryText;
	private TreeViewer viewer;
	private IStructuredContentProvider myContentProvider;
	private ILabelProvider myLabelProvider;
	private NoteSearchRoot displayRoot;
	private Vector searchRslt = null;
	private CommandStack commandStack;
	
	private Action undoAction;
	private Action redoAction;
	private Action openAction;
	private Action makeNoteAction;

	private DateFilterManager dateFilterManager = null;
	private boolean filterShowing = false;

	public NoteSearchView() {
		super();
		Rdb2javaPlugin.getDataServer().addPropertyChangeListener(this);
		commandStack = new CommandStack();
	}
	
	public CommandStack getCommandStack(){
		return commandStack;
	}
	
	public void dispose(){
		Rdb2javaPlugin.getDataServer().removePropertyChangeListener(this);
		if(displayRoot != null)displayRoot.dispose();
		if(dateFilterManager != null)dateFilterManager.dispose();
        super.dispose();
	}

	public void createPartControl(Composite parent) {
		parent.setLayout(new FormLayout());
		
		// search query area
		
		Composite searchQueryArea = defineSearchQueryArea(parent);
		FormData sqData = new FormData();
		sqData.top = new FormAttachment(0,0);
		sqData.right = new FormAttachment(100,0);
		sqData.left = new FormAttachment(0,0);
		//sqData.height = 30;
		searchQueryArea.setLayoutData(sqData);

		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		FormData treeData = new FormData();
		treeData.top = new FormAttachment(searchQueryArea,0);
		treeData.bottom = new FormAttachment(100,0);
		treeData.right = new FormAttachment(100,0);
		treeData.left = new FormAttachment(0,0);
		viewer.getControl().setLayoutData(treeData);
		viewer.getControl().setBackground(ColorConstants.white);
		myContentProvider = new ResourceExplorerContentProvider(this);
		viewer.setColumnProperties(new String[]{ResourceExplorerView.NAME_ID});

		viewer.setContentProvider(myContentProvider);
		myLabelProvider = new ResourceExplorerLabelProvider();
		viewer.setLabelProvider(myLabelProvider);

		Tree tree = viewer.getTree();
		final TextCellEditor nameEditor =
			new TextCellEditor(tree);
		viewer.setCellEditors(new CellEditor[]{nameEditor});
		viewer.setCellModifier(new ResourceExplorerCellModifier(viewer, this));

		makeActions();
		
		hookDoubleClickAction();
		hookDragAndDrop();
		
		contributeToActionBars();

	}

	private void hookDragAndDrop() {
		new ResourceExplorerDragListener(this);
	}

	private void makeActions() {
		final IResourceTreeDisplayer myView = this;
		
		//IActionBars bars = ((IEditorSite)getSite()).getActionBars();

		openAction = new ResourceExplorerOpenAction(this);
		undoAction = new CommandStackUndoAction(getCommandStack());
		//bars.setGlobalActionHandler(ActionFactory.UNDO.getId(), undoAction);
		redoAction = new CommandStackRedoAction(getCommandStack());
		//bars.setGlobalActionHandler(ActionFactory.REDO.getId(), redoAction);
		makeNoteAction = new Action() {
			
			public void run() {
				if(searchRslt == null){
					final Shell parentShell = Display.getDefault().getActiveShell();
					MessageBox messageBox = new MessageBox(parentShell, SWT.ICON_INFORMATION | SWT.OK);
					messageBox.setText("Run a search first");
					messageBox.setMessage("Before using this button you must first run a search");
					messageBox.open();
		            return;
				}
				NewNoteWizard wizard = new NewNoteWizard(getCommandStack(),
						"Query Selection: "+queryText.getText().trim(),
						searchRslt);
				WizardDialog dialog = 
					new WizardDialog(getSite().getShell(), wizard);
				dialog.open();
			}
		};
		makeNoteAction.setText("Create Note...");
		makeNoteAction.setToolTipText("Create a Note from query results");
		makeNoteAction.setImageDescriptor(PlinyPlugin.getImageDescriptor("icons/noteIcon.gif"));
	}
	
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		contributeToLocalToolBar(bars.getToolBarManager());
		bars.setGlobalActionHandler(ActionFactory.UNDO.getId(),undoAction);
		bars.setGlobalActionHandler(ActionFactory.REDO.getId(),redoAction);
		//bars.setGlobalActionHandler(ActionFactory.COPY.getId(),copyAction);
	}
	
	private void contributeToLocalToolBar(IToolBarManager manager) {
		manager.add(makeNoteAction);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}


	private Composite defineSearchQueryArea(Composite parent) {
		Composite rslt = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3,false);
		layout.horizontalSpacing = 5;
		layout.marginWidth = 5;
		layout.marginTop = 5;
		rslt.setLayout(layout);
		rslt.setBackground(ColorConstants.menuBackground);
		
		Label label = new Label(rslt, SWT.NONE);
		label.setText("Search: ");
		
		TextViewer queryViewer = new TextViewer(rslt, SWT.SINGLE | SWT.BORDER);
		queryViewer.setDocument(new Document(""));
		queryText = queryViewer.getTextWidget();
		queryText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		queryText.addKeyListener(new KeyListener(){

			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				if(e.character == '\r')
					doSearch();
			}
			
		});
		
		Button goButton = new Button(rslt, SWT.FLAT);
		goButton.setText("Go");
		goButton.setToolTipText("Click to find notes containing specified word.");
		goButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event){
				doSearch();
			}
		});
		
		ExpandBar bar = new ExpandBar (rslt, SWT.V_SCROLL);
		GridData expandGD = new GridData();
		expandGD.horizontalAlignment = GridData.FILL;
		expandGD.grabExcessHorizontalSpace = true;
		expandGD.horizontalSpan = 3;
		bar.setLayoutData(expandGD);
		
		bar.addExpandListener(new ExpandListener(){
			public void itemCollapsed(ExpandEvent e) {
				filterShowing = false;
			}

			public void itemExpanded(ExpandEvent e) {
				filterShowing = true;
			}
			
		});
		
		dateFilterManager = new DateFilterManager();
		Composite composite = dateFilterManager.getDateFilterDisplay(bar, SWT.NONE);
		
		ExpandItem item0 = new ExpandItem (bar, SWT.NONE, 0);
		item0.setText("Filter by Date:");
		item0.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item0.setControl(composite);
		
		final Composite keptParent = parent;
		
		PaintListener pl = new PaintListener(){
			public void paintControl(PaintEvent e) {
				keptParent.layout();
			}
		};
		bar.addPaintListener(pl);
		return rslt;
	}

	public void setFocus() {
		queryText.setFocus();

	}
	
	
	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				openAction.run();
			}
		});
	}

	
	public void doSearch(){
		final Shell parentShell = Display.getDefault().getActiveShell();
		String queryString = queryText.getText().trim();
		//System.out.println("search requested: "+queryString);
		if(queryString == null || queryString.length() == 0){
			MessageBox messageBox = new MessageBox(parentShell, SWT.ICON_INFORMATION | SWT.OK);
			messageBox.setText("Provide search specification");
			messageBox.setMessage("To do a search, type type a search specification in the field provided first.");
			messageBox.open();
            return;
		}
		final NoteTextIndex indexer = NoteTextIndex.getInstance();
		if(!indexer.isIndexOn()) buildIndex(parentShell, indexer);
		//System.out.println("search requested: "+queryString);
		Vector rslt;
		try {
			rslt = indexer.search(queryString);
		} catch (ParseException e) {
			MessageBox messageBox = new MessageBox(parentShell, SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("Query error");
			messageBox.setMessage("Your query is incorrectly specified:\n"+e.getLocalizedMessage());
			messageBox.open();
            return;
		}
		//System.out.println("   rslt: "+rslt);
		
		if(filterShowing)rslt = doFiltering(rslt);
		
		if(displayRoot != null)displayRoot.dispose();
		displayRoot = new NoteSearchRoot(rslt, this);
		searchRslt = rslt;
        viewer.setInput(displayRoot);
        viewer.refresh();
	}
	
	private Vector doFiltering(Vector input) {
		Date startingDate = dateFilterManager.getStartDate();
		if(startingDate == null)return input;
		Date endingDate = dateFilterManager.getEndDate();
		Vector rslt = new Vector();
		Iterator it = input.iterator();
		while(it.hasNext()){
			NoteLucened note = (NoteLucened)it.next();
			Date creationDate = note.getCreationDate();
			if((creationDate.compareTo(startingDate) >= 0) &&
					(creationDate.compareTo(endingDate) <= 0))
				rslt.add(note);
		}
		return rslt;
	}

	private void buildIndex(Shell parentShell, final NoteTextIndex indexer){
		ProgressMonitorDialog monitor = new ProgressMonitorDialog(parentShell);
		try {
			monitor.run(true, false, new IRunnableWithProgress(){
			//monitor.run(false, false, new IRunnableWithProgress(){

				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						indexer.buildIndex(monitor);
					} catch (IOException e) {
						throw new InvocationTargetException(e);
					}
					
				}
				
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		try {
			indexer.buildIndex(null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}

	public TreeViewer getMyViewer() {
		return viewer;
	}

	public Vector getSelectedResourceExplorerItems() {
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		Vector rslts = new Vector();
		Iterator iter = selection.iterator();
		while(iter.hasNext()){
			Object item = iter.next();
			if(item instanceof IResourceExplorerItem)
				rslts.add(item);
		}
		return rslts;
	}
	
	public Vector getSelectedObjectsToOpen(){
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		Vector rslts = new Vector();
		Iterator iter = selection.iterator();
		while(iter.hasNext()){
			Object item = iter.next();
			if(item instanceof IResourceExplorerItem){
				IResourceExplorerItem reItem = (IResourceExplorerItem)item;
				if(reItem instanceof ResourceExplorerDisplayedInItem){
					LinkableObject lo = (LinkableObject)reItem.getAssociatedObject();
					rslts.add(lo.getDisplayedIn());
				} else rslts.add(reItem.getAssociatedObject());
			}
		}
		return rslts;
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		if(searchRslt == null)return;
		String eventName = arg0.getPropertyName();
		if(eventName.equals("Delete-Resource")){
			Object resource = arg0.getOldValue();
			if(!(resource instanceof Note))return;
			if(searchRslt.contains(resource)){
				searchRslt.remove(resource);
				displayRoot = new NoteSearchRoot(searchRslt, this);
		        viewer.setInput(displayRoot);
		        viewer.refresh();
			}
		}
		
	}

	public Vector getSelectedBaseObjects() {
		// TODO Auto-generated method stub
		return null;
	}

}
