package uk.ac.kcl.cch.jb.pliny.imageRes.controls;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import uk.ac.kcl.cch.jb.pliny.imageRes.dnd.HtmlImageData;
import uk.ac.kcl.cch.jb.pliny.utils.ImageDataTools;

public class HtmlImagesSelectionDialog extends TitleAreaDialog {

	private Vector imageData;
	private Vector<Image> images;
	
	private static final Dimension thumbSize = new Dimension(120,120);
	private static final int dialogWidth = 400;

	private class IncludeSelectionAdapter extends SelectionAdapter{
		private HtmlImageData data;

		public IncludeSelectionAdapter(HtmlImageData data){
			this.data = data;
		}
		
		public void widgetSelected(SelectionEvent event){
			data.setSelected(!data.isSelected());
		}
	}
	
	public HtmlImagesSelectionDialog(Shell parentShell, Vector imageData) {
		super(parentShell);
		// setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		this.imageData = imageData;
		images = null;
	}
	
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		setMessage("This page contains more than one candidate image.  Choose the images you want to import.");
		setTitle("Choose images for import");
		return contents;
		
	}
	
	// http://www.programcreek.com/java-api-examples/org.eclipse.swt.custom.ScrolledComposite
	
	protected Control createDialogArea(Composite parent) {
		/*
		 * this code is modelled after example 4 in
		 * http://www.programcreek.com/java-api-examples/org.eclipse.swt.custom.ScrolledComposite
		 */
		images = new Vector<Image>();

		/* create and format the top-level composite of this dialog */
		Composite contentArea = (Composite) super.createDialogArea(parent);
		//contentArea.setLayout(new FillLayout());
		
		/* 
		 * Add a box containing all the images, and their selectable state. We put
		 * everything inside a ScrolledComposite, so that the scrollbar is managed for us.
		 */
		ScrolledComposite scrolledComposite =  new ScrolledComposite(contentArea,SWT.V_SCROLL | SWT.H_SCROLL);
		scrolledComposite.setLayout(new FillLayout());

		/* Add another composite (within the ScrolledComposite) that will contain the list of package names */
		Composite listComposite = new Composite(scrolledComposite,SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		listComposite.setLayout(layout);
		
		/* load thumbname images and selection button into the dialogue box's listComposite */
		Iterator it = imageData.iterator();
		while(it.hasNext()){
			HtmlImageData item = (HtmlImageData)it.next();
			ImageData thumb = ImageDataTools.getInstance().createThumbImage(item.getTheImageData(), thumbSize);
			Group g = new Group(listComposite, SWT.SHADOW_IN);
			GridLayout l = new GridLayout();
			l.numColumns = 1;
			g.setLayout(l);
			Button b = new Button(g, SWT.CHECK);
			b.setText("Include?");
			b.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
			b.addSelectionListener(new IncludeSelectionAdapter(item));
			b.setSelection(item.isSelected());
			item.setMyButton(b);
			Label x = new Label(g, SWT.NONE);
			Image i = new Image(Display.getCurrent(),thumb);
			x.setImage(i);
			images.add(i);
		}
		
		/* tell the ScrolledComposite what it's managing, and how big it should be. */
		scrolledComposite.setContent(listComposite);
		listComposite.setSize(listComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setMinWidth(0);
		listComposite.setSize(listComposite.computeSize(dialogWidth, SWT.DEFAULT));
		return contentArea;
	}
	
	protected void createButtonsForButtonBar(Composite parent){
		createButton(parent, IDialogConstants.SELECT_ALL_ID, "Select All", false);
		createButton(parent, IDialogConstants.DESELECT_ALL_ID, "Deselect All", false);
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	protected void buttonPressed(int buttonId){
		if(buttonId == IDialogConstants.SELECT_ALL_ID || buttonId == IDialogConstants.DESELECT_ALL_ID){
			doSelectAll(buttonId == IDialogConstants.SELECT_ALL_ID);
			return;
		}
		setReturnCode(buttonId);
		close();
	}
	
	private void doSelectAll(boolean selected) {
		Iterator it = imageData.iterator();
		while(it.hasNext()){
			HtmlImageData i = (HtmlImageData)it.next();
			i.setSelected(selected);
		}
	}

	public boolean close(){
		boolean rslt = super.close();
		if(images != null){
			Iterator<Image> it = images.iterator();
			while(it.hasNext()){
				Image i = it.next();
				if(!i.isDisposed())i.dispose();
			}
			images = null;
		}
		return rslt;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);;
		newShell.setText("Select Images from this HTML page to import");
	}
}
