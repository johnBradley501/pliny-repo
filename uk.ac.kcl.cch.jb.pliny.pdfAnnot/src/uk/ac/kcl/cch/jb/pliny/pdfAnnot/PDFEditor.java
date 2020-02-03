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

package uk.ac.kcl.cch.jb.pliny.pdfAnnot;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.DeleteAction;
import org.eclipse.gef.ui.actions.RedoAction;
import org.eclipse.gef.ui.actions.UndoAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
//import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.jpedal.PdfDecoder;
import org.jpedal.utils.LogWriter;
import org.eclipse.ui.part.EditorPart;

import uk.ac.kcl.cch.jb.pliny.IHandlesAnnotations;
import uk.ac.kcl.cch.jb.pliny.IResourceDrivenPart;
import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.ResourceAreaManager;
import uk.ac.kcl.cch.jb.pliny.actions.CopyPlinyAction;
import uk.ac.kcl.cch.jb.pliny.actions.CutPlinyAction;
import uk.ac.kcl.cch.jb.pliny.actions.OpenEclipseOutlineViewAction;
import uk.ac.kcl.cch.jb.pliny.actions.PastePlinyAction;
import uk.ac.kcl.cch.jb.pliny.actions.PlinySelectAllAction;
import uk.ac.kcl.cch.jb.pliny.controls.PlinyToolBar;
import uk.ac.kcl.cch.jb.pliny.editors.IPageSettableEditorPart;
import uk.ac.kcl.cch.jb.pliny.editors.ResourceEditorInput;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.pdfAnnot.model.PdfResource;
//import uk.ac.kcl.cch.jb.pliny.pdfAnnot.utils.Info;
import uk.ac.kcl.cch.jb.pliny.pdfAnnot.views.PDFOutline;

/**
 * the EditPart for the PDF annotator.
 * Much of this code is taken directly from JPedal's example of
 * how to use their PDF browser in an Eclipse environment.  The
 * annotation area is a GEF-managed object, as elsewhere.
 * <p>
 * A problem to making this code work is that JPedal's code really works
 * within the AWT/Swing model, and here we must be in the SWT model.  After
 * trying various ways to make the two live together I ended up using
 * JPedal's component for making an <i>image</i> of a page, which I then
 * converted to an SWT image!  See the utility class that does this
 * conversion in {@link SwtImageFromPdf}.  Since each PDF page is presented
 * to this editor as an image, there are many echos here between this
 * code and the code for the Pliny <code>ImageEditor</code>.
 * 
 * @author John Bradley
 *
 */

public class PDFEditor extends EditorPart 
implements PropertyChangeListener, IResourceDrivenPart, IPageSettableEditorPart,
ISelectionChangedListener, CommandStackListener, IHandlesAnnotations{

	public static final String EDITOR_ID = "uk.ac.kcl.cch.jb.pliny.pdfAnnot.PDFEditor";

	final public static boolean debug=false;

	private static ImageRegistry imageRegistry = PdfAnnotPlugin.getDefault().getImageRegistry();
    private static final int streamCacheSize = 1024*2; //(2KB)
	
	private float scaling = 1f;

	private Composite viewer;
	private PlinyToolBar controlPanel;
	
	private ResourceAreaManager areaManager = null;
	private PdfResource myResource = null;
	private ResourceEditorInput myInput = null;

	//private Action newNoteAction;
	//private Action newConnectionAction;
	private DeleteAction deleteAction;
	private UndoAction undoAction;
	private RedoAction redoAction;
	private CutPlinyAction cutAction;
	private CopyPlinyAction copyAction;
	private PastePlinyAction pasteAction;
	
	private OpenEclipseOutlineViewAction outlineAction;
	
	private ToolItem itemFirst = null;
	private ToolItem itemBack = null;
	private ToolItem itemFBack = null;
	private ToolItem itemFForward = null;
	private ToolItem itemForward = null;
	private ToolItem itemLast = null;

	/**file name including path*/
	private String fileName="";
	
	/**actual JPedal library*/
	private PdfDecoder decodePDF;
	
	/**mechanism to create an swt image from PdfDecoder*/
	private SwtImageFromPdf imageMaker;
	private Control browser;
	
	/** Current page number (first page is 1) */
	private int currentPage = 1;

	//private Frame frame;

	private PDFOutline outlinePage;

	private Label pageCount;

	private static Image icon = null;
	
	private Image getMyIcon(){
		if(icon == null)
			icon = PdfAnnotPlugin.getImageDescriptor("icons/pdfIcon.gif").createImage();
		return icon;
	}
    	
	public PDFEditor() {
		
		super();

		if(!PdfAnnotPlugin.isBroken){
			
			if(PDFEditor.debug)
				System.out.println("PDFEditor Called1");
			
			//IPerspectiveRegistry reg = PlatformUI.getWorkbench().getPerspectiveRegistry();
	
			//IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			
			//if(activePage != null)
			//	activePage.setPerspective(reg.findPerspectiveWithId("uk.ac.kcl.cch.jb.pliny.pdfAnnot.perspective"));
			//  activePage.setPerspective(reg.findPerspectiveWithId("org.jpedal.pdf.plugins.eclipse.perspective.PDFPerspective"));
			
			if(PDFEditor.debug)
			System.out.println("Called2");
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// nothing to save .. jb
	}

	@Override
	public void doSaveAs() {
		// nothing to save .. jb
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		
		if(!(input instanceof ResourceEditorInput)){
			throw new PartInitException("specified input was not a Resource");
		}
		myInput = (ResourceEditorInput)input;
		Resource theResource = myInput.getMyResource();
		if(!(theResource instanceof PdfResource)){
			throw new PartInitException("specified input was not a PdfResource");
		}
		//super.init(site,input);
			
		if(PdfAnnotPlugin.isBroken)
			return;
		
		if(debug)
			System.out.println("Init called ");
			
		myResource = (PdfResource)theResource;
		scaling = myResource.getScale();
		currentPage = myInput.getPageNo();
		if(currentPage <= 0)currentPage = 1;
		myResource.setCurrentPage(currentPage);
		fileName = myResource.getMyCachedPdfFilename();
		myResource.addPropertyChangeListener(this);
		
		setPartName(myResource.getName());
        setSite(site);
        setInput(input);

		areaManager = new ResourceAreaManager(myResource, this, EDITOR_ID);
		areaManager.setMyFactory(new PdfEditorPartFactory(this));

		createActions();

		if(debug)
			System.out.println("fileName "+fileName);
	}
	
	@Override
	public void dispose(){
		if(decodePDF != null)
		   decodePDF.closePdfFile();
		myResource.removePropertyChangeListener(this);
		areaManager.dispose();
		controlPanel.dispose();
		if(imageMaker != null)imageMaker.dispose();
		if(myPageSelectionListener != null)myPageSelectionListener.dispose();
		super.dispose();
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	private void setButtonEnabled(){
		int pageCount = decodePDF.getPageCount();
		itemBack.setEnabled(currentPage > 1);
		itemFBack.setEnabled(currentPage > 1);
		itemFForward.setEnabled(currentPage < pageCount);
		itemForward.setEnabled(currentPage < pageCount);
	}
	
	private void changePage(int delta){
		int newPage = currentPage + delta;
		int pageCount = decodePDF.getPageCount();
		if(newPage < 1)newPage = 1;
		if(newPage > decodePDF.getPageCount()) newPage = pageCount;
		displayPage(newPage);
		((FigureCanvas)areaManager.getGraphicalViewer().getControl()).getViewport().setViewLocation(0, 0);
	}
	
	private Image getIcon(String name){
		String key = "icon-"+name;
		if(imageRegistry.getDescriptor(key) != null)
			return imageRegistry.get(key);
		ImageDescriptor idesc = PdfAnnotPlugin.getImageDescriptor(name);
		imageRegistry.put(key, idesc);
		return idesc.createImage();
	}
	
	private void setPageDisplay(){
		pageCount.setText("pg "+currentPage+" of "+decodePDF.getPageCount());
		
	}
	
	private class PageSelectionListener extends SelectionAdapter implements PropertyChangeListener {
	
		private ToolItem dropdown;
		private Menu menu = null;
		private PdfResource theResource = null;
		
		public PageSelectionListener(ToolItem dropdown) {
			this.dropdown = dropdown;
		}
		
		public void dispose(){
			if(theResource != null)theResource.removePropertyChangeListener(this);
		}
		
		private Hashtable getPagesContainingReferences(){
			Hashtable rslt = new Hashtable();
			Vector curList = myResource.getMyDisplayedItems().getItems();
            Iterator it = curList.iterator();
            while(it.hasNext()){
    			LinkableObject obj = (LinkableObject)it.next();
    			if(obj.getSurrogateFor() != null){
	    			Integer pgeNo = new Integer(obj.getDisplPageNo());
	    			if(rslt.containsKey(pgeNo)){
	    				Integer oldVal = (Integer)rslt.get(pgeNo);
	                    int newVal = oldVal.intValue()+1;
	    				rslt.put(pgeNo, new Integer(newVal));
	    			} else rslt.put(pgeNo, new Integer(1));
    			}

            }
			return rslt;
		}
		
		private void buildMenu(){
			if(theResource == null){
				theResource = myResource;
				theResource.addPropertyChangeListener(this);
			}
			menu = new Menu(dropdown.getParent().getShell());
			Hashtable rslt = getPagesContainingReferences();
			for(int i = 0; i < decodePDF.getPageCount(); i++){
				MenuItem menuItem = new MenuItem(menu, SWT.NONE);
				String displayText = "p "+(1+i);
				Integer pNo = new Integer(i+1);
				if(rslt.containsKey(pNo))
					displayText += " ["+((Integer)rslt.get(pNo)).toString()+"]";
				menuItem.setText(displayText);
				menuItem.setData(new Integer(i+1));
				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event){
						MenuItem selected = (MenuItem)event.widget;
						int selectedPage = ((Integer)selected.getData()).intValue();
						displayPage(selectedPage);
					}
				});
			}
		}
		
		@Override
		public void widgetSelected(SelectionEvent event){
			if(event.detail == SWT.ARROW){
				if(menu == null)buildMenu();
				// modelled after code in Warner The Definitive Guide to SWT and JFace pg 234  j.b.
				ToolItem item = (ToolItem) event.widget;
				Rectangle rect = item.getBounds();
				Point pt = item.getParent().toDisplay(new Point(rect.x, rect.y));
				menu.setLocation(pt.x, pt.y+rect.height);
				menu.setVisible(true);
			} else {
				MessageDialog.openInformation(dropdown.getParent().getShell(),
						"How to use this item",
						"Click on the down arrow just to the right of the label to display "+
						"a list of pages. Select one to jump to that page.");
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent arg0) {
			if(arg0.getPropertyName() == Resource.MYDISPLAYEDITEMS_PROP)menu = null;
		}
		
	}
	
	PageSelectionListener myPageSelectionListener = null;

	private void buildToolbar(Composite parent) {
		Vector contributions = new Vector();
		controlPanel = new PlinyToolBar(parent, this, areaManager, getMyIcon(), true, contributions);
		
        Composite toolbarComp = controlPanel.getToolbarComp();
        GridData ldata = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        ldata.heightHint = 24;
        //GridData ldata = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        toolbarComp.setLayoutData(ldata);
	}
	
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	@Override
	public void createPartControl(Composite container) {
		if(PdfAnnotPlugin.isBroken)
			return;
		
		if(debug)
			System.out.println("createPartControl called");
		
		container.setLayout(new GridLayout(1,false));
		GridData gdata = new GridData(GridData.FILL_HORIZONTAL);
		gdata.heightHint = 18;
		
		container.setBackground(PlinyPlugin.getBackgroundGray());

		buildToolbar(container);
		
		// Composite container=this.getContainer();
		
		/**
		 * initialise JPedal PDF view on first call
		 */
		try{
			viewer = new Composite(container, SWT.NONE);
			viewer.setLayoutData(new GridData(GridData.FILL_BOTH));

			ToolBar toolbar = new ToolBar(viewer, SWT.NONE);
			FormData data = new FormData();
			data.top = new FormAttachment(0, 0);
			data.right = new FormAttachment(100,0);
			data.left = new FormAttachment(0,0);
			
			Image icon;
			
			itemFirst = new ToolItem(toolbar, SWT.PUSH);	
			
			//String path="/org/jpedal/examples/simpleviewer/res/";
			
			icon = getIcon("icons/first.gif");
			itemFirst.setImage(icon);
			itemFirst.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					displayPage(1);
				}
			});
			
			itemBack = new ToolItem(toolbar, SWT.PUSH);	
			icon = getIcon("icons/back.gif");
			itemBack.setImage(icon);
			itemBack.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					changePage(-1);
				}
			});
			
			itemFBack = new ToolItem(toolbar, SWT.PUSH);			
			icon = getIcon("icons/fback.gif");
			itemFBack.setImage(icon);
			itemFBack.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					changePage(-10);
				}
			});
			
			itemFForward = new ToolItem(toolbar, SWT.PUSH);
			icon = getIcon("icons/fforward.gif");
			itemFForward.setImage(icon);
			itemFForward.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					changePage(10);
				}
			});
			
			itemForward = new ToolItem(toolbar, SWT.PUSH);
			icon = getIcon("icons/forward.gif");
			itemForward.setImage(icon);
			itemForward.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					changePage(1);
				}
			});
			
			itemLast = new ToolItem(toolbar, SWT.PUSH);
			icon = getIcon("icons/last.gif");
			itemLast.setImage(icon);
			itemLast.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					displayPage(decodePDF.getPageCount());
				}
			});
			
			ToolItem selectPageItem = new ToolItem(toolbar, SWT.DROP_DOWN);
			selectPageItem.setImage(getIcon("icons/page.gif"));
			myPageSelectionListener = new PageSelectionListener(selectPageItem);
			selectPageItem.addSelectionListener(myPageSelectionListener);
			
			
			new ToolItem(toolbar, SWT.SEPARATOR);
			
			ToolItem zoomIn = new ToolItem(toolbar, SWT.PUSH);
			icon = getIcon("icons/plus.gif");
			zoomIn.setImage(icon);
			zoomIn.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					zoom(1.5f);
				}
			});
			
			ToolItem zoomOut = new ToolItem(toolbar, SWT.PUSH);
			icon = getIcon("icons/minus.gif");
			zoomOut.setImage(icon);
			zoomOut.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					zoom(2f/3f);
				}
			});
			
			new ToolItem(toolbar, SWT.SEPARATOR);
			
			ToolItem openOutline = new ToolItem(toolbar, SWT.PUSH);
			icon = outlineAction.getImageDescriptor().createImage();
			openOutline.setImage(icon);
			openOutline.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					outlineAction.run();
				}
				
			});
			
			pageCount = new Label(viewer, SWT.CENTER | SWT.BORDER);
			pageCount.setBackground(ColorConstants.white);
			data = new FormData();
			data.width = 100;
			data.height = 18;
			data.top = new FormAttachment(0, 4);
			//data.top = new FormAttachment(toolbar, 0, SWT.TOP);
			//data.top = new FormAttachment(0,0);
			data.left = new FormAttachment(toolbar, 20, SWT.RIGHT);			
		
			pageCount.setLayoutData(data);
			
			//Combo pageCombo = new Combo(viewer, SWT.READ_ONLY);
			//data = new FormData();
			//data.width = 100;
			//data.height = 18;
			//data.top = new FormAttachment(0,0);
			//data.left = new FormAttachment(pageCount, 20, SWT.RIGHT);
			//pageCombo.setLayoutData(data);
			
			//DnDResourceHolder myIcon = new DnDResourceHolder(viewer, SWT.NONE);
			//myIcon.setSize(18,20);
			//myIcon.getLabel().setBackground(ColorConstants.green);
			//myIcon.getLabel().setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			//myIcon.getLabel().setImage(this.getMyIcon());
			//myIcon.setResource(myResource);
			//data = new FormData();
			//data.top = new FormAttachment(0, 5);
			//data.height = 18;
			//data.right = new FormAttachment(100, -5);
			//myIcon.getLabel().setLayoutData(data);
			
			viewer.setLayout(new FormLayout());
			
			//Control aboveBrowser = (Control)canvas;
			//Control aboveBrowser = myIcon.getLabel();
			
			
			//Composite browser = new Composite(viewer, SWT.EMBEDDED);
			//Canvas browser = new Canvas(viewer, SWT.EMBEDDED);
			//Canvas browser = (Canvas)createGraphicalViewer(viewer);
			//browser = new Label(viewer, SWT.NONE);
			
			decodePDF = new PdfDecoder();
			
			// copied from Commands.java for the simpleViewer to try cut down on memory demands   .jb
			decodePDF.useHiResScreenDisplay(true);
			//decodePDF.setStreamCacheSize(streamCacheSize);

			imageMaker = new SwtImageFromPdf(decodePDF);
			myResource.setSwtImageFromPdf(imageMaker);
			
			try {
				//PdfDecoder.setTTFontDirs(new String[]{"c:/Windows/Fonts/"});
				//PdfDecoder.enforceFontSubstitution = true;
				if(System.getProperty("os.name").startsWith("Windows"))
				        PdfDecoder.setSubstitutedFontAliases("Courier New",
						new String[]{"Courier"});
				
				decodePDF.openPdfFile(fileName);
				decodePDF.setPageParameters(scaling, currentPage);
				//PdfFileInformation fileInfo = decodePDF.getFileInformationData();
				//String titleOfPDF = fileInfo.getFieldValues()[0];
				//System.out.println(titleOfPDF);
				//decodePDF.setDefaultDisplayFont("CourierPS"); // a kludge to get monospace font printing to work j.b.
				//System.out.println("standardFonts:");
				//String[] fontList = GraphicsEnvironment.getLocalGraphicsEnvironment()
				//.getAvailableFontFamilyNames();
                //for(int i=0; i< fontList.length; i++){
                //	System.out.println("   "+fontList[i]);
                //}
				
				boolean fileCanBeOpened= false;
				if (decodePDF.isEncrypted()) {
					
					InputDialog input = new InputDialog(getSite().getShell(),"Password","Enter a password","",null);
					input.open();
					
					String password = input.getValue();
					
		            /** try and reopen with new password */
		            if (password != null) {
		            	decodePDF.setEncryptionPassword(password);
		            	// decodePDF.verifyAccess(); // from version 2.7.1 j.b.

		                if (decodePDF.isFileViewable())
		                    fileCanBeOpened = true;
		                else
		                    fileCanBeOpened = false;
		            }
		            
		            if(!fileCanBeOpened)
		            	MessageDialog.openInformation(getSite().getShell(),"Password","No valid password");

		        }else
		            fileCanBeOpened=true; 
				
				if(fileCanBeOpened){
					decodePDF.setPageParameters(scaling,1);
					decodePDF.decodePage(currentPage);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			//pageCount.setText(currentPage+"/"+decodePDF.getPageCount());
			setPageDisplay();
			setButtonEnabled();

			//add a border and center
			decodePDF.setPDFBorder(BorderFactory.createLineBorder(Color.black, 1));
			decodePDF.setInset(5,5);
			
			//browser.setImage(imageMaker.getSwtImageForPage(currentPage));
			//browser.redraw();
			//browser.update();
			browser = areaManager.createGraphicalViewer(viewer);

			/** setup GUI and wrapper around SWING component - 
			 * does not work on MAC until AWT bridge bug fixed */
			//frame = SWT_AWT.new_Frame(browser);
			
	        //JScrollPane scrollPane = new JScrollPane(decodePDF);
			//frame.add(scrollPane);
			//frame.pack();
			
			/** adding code to layer GEF viewer overtop (I hope!)
			 *  of the PDF image.
			
			
			
			Canvas myComposite = new Canvas(viewer,SWT.NO_BACKGROUND);
			//myComposite.setBounds(viewer.getBounds());
			myComposite.setBounds(0,100,500,500);
			Button testButton = new Button(myComposite, SWT.PUSH);
			testButton.setText("hi there");
			testButton.setBounds(100,100,100,100);
			
			/* end of test code */
			
			data = new FormData();
			data.left = new FormAttachment(0, 0);
			// data.top = new FormAttachment(aboveBrowser, 5, SWT.DEFAULT);
			data.top = new FormAttachment(toolbar, 5, SWT.DEFAULT);
			data.right = new FormAttachment(100, 0);
			data.bottom = new FormAttachment(100, 0);

			browser.setLayoutData(data);
			
			viewer.layout(true);
			
			//this.addPage(viewer);

			
		}catch(Exception e){
			LogWriter.writeLog("Exception "+e+" in createPartControl >> PDFViewer");
			e.printStackTrace();
		}catch(Error e){
			LogWriter.writeLog("Error "+e+" in createPartControl >> PDFViewer");
			e.printStackTrace();
		}
	}
	
	public EditDomain getEditDomain(){
		return areaManager.getEditDomain();
	}
	
	public CommandStack getCommandStack(){
		return areaManager.getEditDomain().getCommandStack();
	}
	
	protected ActionRegistry getActionRegistry(){
		return areaManager.getActionRegistry();			
	}
	
	protected void createActions(){
		ActionRegistry registry = getActionRegistry();
		// undoAction = new UndoAction(this);
        // registry.registerAction(undoAction);
		// redoAction = new RedoAction(this);
        // registry.registerAction(redoAction);
		// deleteAction = new DeleteAction((IWorkbenchPart)this); // j.b. deleted because having this here prevented Delete key and menu connecting to
        // registry.registerAction(deleteAction);                 // an active delete action (who knows how it is set up !!!)   .jb

        // cutAction = new CutPlinyAction(this);
        // registry.registerAction(cutAction);
        // copyAction = new CopyPlinyAction(this);
        // registry.registerAction(copyAction);
        // pasteAction = new PastePlinyAction(this);
        // registry.registerAction(pasteAction);
        cutAction = new CutPlinyAction(this);   // these lines copied from the NoteEditor which seems to work properly... jb
        areaManager.addSelectionAction(cutAction);
        copyAction = new CopyPlinyAction(this);
        areaManager.addSelectionAction(copyAction);
        pasteAction = new PastePlinyAction(this);
        areaManager.addSelectionAction(pasteAction);

        Action selectAllAction = new PlinySelectAllAction(this);
        registry.registerAction(selectAllAction);

		//newNoteAction = new MakeNoteAction(getEditDomain());
		//newConnectionAction = new MakeConnectionAction(getEditDomain());
        
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
		
		outlineAction = new OpenEclipseOutlineViewAction();
	}
/*
	public void openPDF(String fileName) {
		
		if(debug)
			System.out.println("OpenPDF called with "+fileName);
		currentPage=1;
		try {
			setPartName(new File(fileName).getName());
			
			decodePDF.openPdfFile(fileName);
			decodePDF.setPageParameters(scaling, currentPage); 
			decodePDF.decodePage(currentPage);
						
			pageCount.setText(currentPage+"/"+decodePDF.getPageCount());
			
			outlinePage.setPDFDecoder(decodePDF);
			
			outlinePage.setTree();
			
			//repaint();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
*/

	public void zoom(float scale) {
		
		scaling=scaling*scale;
		decodePDF.setPageParameters(scaling, currentPage); 
		decodePDF.updateUI();
		//repaint();
		myResource.setScale(scaling);
	}
	
    protected void displayPage(int newPage) {
	//public void changePage(int pageChange) {
		
		//int newPage=currentPage+pageChange;
		
		if((newPage>0)&&(newPage<=decodePDF.getPageCount())){
			currentPage=newPage;
			try {
				decodePDF.setPageParameters(scaling, currentPage); 
				decodePDF.decodePage(currentPage);
				
				//pageCount.setText(currentPage+"/"+decodePDF.getPageCount());
				setPageDisplay();
				//System.out.println("currentFonts: "+decodePDF.getFontsInFile());

				
				//repaint();
			} catch (Exception e) {
				e.printStackTrace();
			}
			myResource.setCurrentPage(currentPage);
			myInput.setPageNo(currentPage);
			setButtonEnabled();
		}
	}
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		viewer.setFocus();
		decodePDF.repaint();
	}
	
	@Override
	public Object getAdapter(Class key){
		
		
		if(debug)
			System.out.println("Get adapter called with "+key);
		
		if(key.equals(IContentOutlinePage.class)&&(!PdfAnnotPlugin.isBroken)){
			
			if(outlinePage == null)
				outlinePage = new PDFOutline(this);
			
			return outlinePage;
		}
		if(key == CommandStack.class)
			return getCommandStack(); // needed in MinimizeAllAction
		                              // which is a WorkbenchPartAction
		if(key == ActionRegistry.class)
			return getActionRegistry();
		if(key == GraphicalViewer.class)
			return areaManager.getGraphicalViewer();
		return super.getAdapter(key);
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		if(arg0.getPropertyName()==Resource.NAME_PROP)
			setPartName(myResource.getName());
	}
	
	
	@Override
	public Resource getMyResource(){
		return myResource;
	}

	@Override
	public void commandStackChanged(EventObject event) {
		undoAction.update();
		redoAction.update();
	}
	
	public void updateCommandStackActions(){
		deleteAction.update();
		cutAction.update();
		copyAction.update();
		pasteAction.update();
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		updateCommandStackActions();
	}
	
	@Override
	public void turnToPage(int pageNo){
		displayPage(pageNo);
	}
	
	public PdfDecoder getPdfDecoder(){
		return decodePDF;
	}
}
