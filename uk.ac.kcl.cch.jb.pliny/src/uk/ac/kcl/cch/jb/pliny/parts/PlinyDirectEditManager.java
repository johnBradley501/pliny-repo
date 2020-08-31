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

package uk.ac.kcl.cch.jb.pliny.parts;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.CellEditorActionHandler;
import org.eclipse.ui.part.ViewPart;

/**
 * manages the direct textual editing for those GEF Pliny editParts
 * that require it.
 * 
 * @author John Bradley
 *
 */

public class PlinyDirectEditManager extends DirectEditManager {

	protected IDirectEditablePart source;
	protected IActionBars actionBars;
	protected CellEditorActionHandler actionHandler;
	protected IAction copy, cut, paste, undo, redo, find, selectAll, delete;
	protected Font scaledFont;
	protected int cellEditorOptions;
	private int traverseDetail = SWT.TRAVERSE_NONE; // see SWT.TRAVERSE_... values
	private Vector textEditCommitListeners = new Vector();
	
	public PlinyDirectEditManager(IDirectEditablePart source,
			CellEditorLocator locator, int cellEditorOptions) {
		super(source, null, locator);
		this.source = source;
		this.cellEditorOptions = cellEditorOptions;
	}
	
	public void dispose(){
		if(this.isDirty())this.commit();
	}
	
	public int getTraverseDetail(){
		return traverseDetail;
	}


	/**
	 * @see org.eclipse.gef.tools.DirectEditManager#bringDown()
	 */
	protected void bringDown() {
		if (actionHandler != null) {
			actionHandler.dispose();
			actionHandler = null;
		}
		if (actionBars != null) {
			restoreSavedActions(actionBars);
			actionBars.updateActionBars();
			actionBars = null;
		}

		Font disposeFont = scaledFont;
		scaledFont = null;
		super.bringDown();
		if (disposeFont != null)
			disposeFont.dispose();
	}

	protected CellEditor createCellEditorOn(Composite composite) {
		//return new TextCellEditor(composite, cellEditorOptions);
		return new TitleTextCellEditor(composite, cellEditorOptions, this);
	}

	protected void initCellEditor() {
		Text text = (Text)getCellEditor().getControl();
		traverseDetail = SWT.TRAVERSE_NONE;
		text.addTraverseListener(new TraverseListener(){
			public void keyTraversed(TraverseEvent e) {
				// System.out.println("PlinyDirectEditorManager.TraverseListener: TraverseEvent: "+e);
				if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
					e.doit = true;
					// System.out.println("PlinyDirectEditorManager.TraverseListener: Traverse_TAB_NEXT");
				}
				traverseDetail = e.detail;

			}
		});
		
		IFigure theFigure = getEditPart().getFigure();
		getCellEditor().setValue(source.getTextToEdit());
		scaledFont = theFigure.getFont();
		FontData data = scaledFont.getFontData()[0];
		Dimension fontSize = new Dimension(0, data.getHeight());
		theFigure.translateToAbsolute(fontSize);
		data.setHeight(fontSize.height);
		scaledFont = new Font(null, data);
		text.setFont(scaledFont);
		source.setupText(text);

		// Hook the cell editor's copy/paste actions to the actionBars so that they can
		// be invoked via keyboard shortcuts.
		IWorkbenchPart activePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
		.getActivePart();
		actionBars = null;
		if(activePart instanceof ViewPart){
			actionBars = ((ViewPart)activePart).getViewSite().getActionBars();
		} else 
		actionBars = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActiveEditor().getEditorSite().getActionBars();
		saveCurrentActions(actionBars);
		actionHandler = new CellEditorActionHandler(actionBars);
		actionHandler.addCellEditor(getCellEditor());
		actionBars.updateActionBars();
	}
	
	// trying to catch tab key in editing as a signal to move to note body.
	// handling this via commit() seems "too early", so that focus cannot be shifted to the note body
	// perhaps try extending TextCellEditor, then overriding "protected void focusLost()", with
	// call to super.focusLost(), then invoking code to to "fire" the commit in this object?
	
	//protected void commit() {
	public void announceFocusLost(){
		//super.commit();
		TextEditCommitEvent e = new TextEditCommitEvent(source, traverseDetail);
		fireCommit(e);
	}

	protected void restoreSavedActions(IActionBars actionBars){
		actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), copy);
		actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(), paste);
		actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), delete);
		actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), selectAll);
		actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(), cut);
		actionBars.setGlobalActionHandler(ActionFactory.FIND.getId(), find);
		actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(), undo);
		actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), redo);
	}

	protected void saveCurrentActions(IActionBars actionBars) {
		copy = actionBars.getGlobalActionHandler(ActionFactory.COPY.getId());
		paste = actionBars.getGlobalActionHandler(ActionFactory.PASTE.getId());
		delete = actionBars.getGlobalActionHandler(ActionFactory.DELETE.getId());
		selectAll = actionBars.getGlobalActionHandler(ActionFactory.SELECT_ALL.getId());
		cut = actionBars.getGlobalActionHandler(ActionFactory.CUT.getId());
		find = actionBars.getGlobalActionHandler(ActionFactory.FIND.getId());
		undo = actionBars.getGlobalActionHandler(ActionFactory.UNDO.getId());
		redo = actionBars.getGlobalActionHandler(ActionFactory.REDO.getId());
	}
	
	//protected void unhookListeners(){
	//	Text text = (Text)getCellEditor().getControl();
	//}
	
	public void addTextEditCommitListener(ITextEditCommittedListener l){
		if(textEditCommitListeners.contains(l))return;
		textEditCommitListeners.add(l);
	}
	
	public void removeTextEditCommitListener(ITextEditCommittedListener l){
		if(textEditCommitListeners.contains(l))textEditCommitListeners.remove(l);
	}
	
	private void fireCommit(TextEditCommitEvent e){
		Iterator it = textEditCommitListeners.iterator();
		while(it.hasNext()){
			ITextEditCommittedListener l = (ITextEditCommittedListener)it.next();
			l.handleCommit(e);
		}
	}
}
