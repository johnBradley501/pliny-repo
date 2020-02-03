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

package uk.ac.kcl.cch.jb.pliny.pdfAnnot.views;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.jpedal.PdfDecoder;
import org.w3c.dom.Document;

import uk.ac.kcl.cch.jb.pliny.pdfAnnot.PDFEditor;

/**
 * an Eclipse <code>IContentOutlinePage</code> to support the display of
 * a PDF file's table of contents as an Eclipse outline.
 * 
 * @author John Bradley
 *
 */

public class PDFOutline implements IContentOutlinePage {

	private PdfDecoder pdfDecoder;
	private PDFEditor pdfEditor;
	
	private static MessagePage noOutlinePage = null;
	private TreeViewer viewer = null;
	private IStructuredContentProvider myContentProvider;
	private ILabelProvider myLabelProvider;
	private PDFOutlineItem dataRoot = null;

	public static final String NAME_ID = "Name";
	
	private void makeNoOutlinePage(){
		noOutlinePage = new MessagePage();
		noOutlinePage.setMessage("There is no outline available.");
	}

	public PDFOutline(PDFEditor myEditor) {
		super();
		this.pdfEditor = myEditor;
		this.pdfDecoder=myEditor.getPdfDecoder();
		if(noOutlinePage == null)makeNoOutlinePage();
	}

	public void createControl(Composite parent) {
		noOutlinePage.createControl(parent);
		Document XMLOutline = null;
		
		try {
			XMLOutline = pdfDecoder.getOutlineAsXML();
		} catch (Exception ee) {
			
		}
		if(XMLOutline == null){
			noOutlinePage.createControl(parent);
			return;
		}
		viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		myContentProvider = new PDFOutlineContentProvider();
		viewer.setContentProvider(myContentProvider);
		myLabelProvider = new PDFOutlineLabelProvider();
		viewer.setLabelProvider(myLabelProvider);
		//viewer.setSorter(new NameSorter());
		dataRoot = new PDFOutlineItem();
		dataRoot.setAsRoot(XMLOutline);
		viewer.setInput(dataRoot);
		viewer.setColumnProperties(new String[]{NAME_ID});
		
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				setToPage();
			}
		});
		viewer.expandAll();

	}
	
	public void setToPage(){
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		if(selection.size() != 1)return;
		Object item = selection.getFirstElement();
		if(!(item instanceof PDFOutlineItem))return;
		int pageNo = ((PDFOutlineItem)item).getPage();
		if(pageNo < 1)return;
		pdfEditor.turnToPage(pageNo);
	}

	public void dispose() {
		// seems to be nothing to do here
	}

	public Control getControl() {
		if(viewer != null)return viewer.getControl();
		return noOutlinePage.getControl();
	}

	public void setActionBars(IActionBars actionBars) {
		// TODO Auto-generated method stub

	}

	public void setFocus() {
		getControl().setFocus();

	}

	public ISelection getSelection() {
		if(viewer == null)return null;
		return viewer.getSelection();
	}

	public void setSelection(ISelection selection) {
		if(viewer == null)return;
		viewer.setSelection(selection);
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		// not doing this -- the PDFEditor does not involve selection.
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		// not doing this -- the PDFEditor does not involve selection.
	}
}
