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

package uk.ac.kcl.cch.jb.pliny.editors;

import org.eclipse.gef.EditDomain;
import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.DeleteRetargetAction;
import org.eclipse.gef.ui.actions.RedoRetargetAction;
import org.eclipse.gef.ui.actions.UndoRetargetAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;

import uk.ac.kcl.cch.jb.pliny.actions.CreateMinimiseStatus;
import uk.ac.kcl.cch.jb.pliny.actions.DisplayReferrerAction;
import uk.ac.kcl.cch.jb.pliny.actions.GenerateContentsAsTextAction;
import uk.ac.kcl.cch.jb.pliny.actions.MakeConnectionAction;
import uk.ac.kcl.cch.jb.pliny.actions.MakeNoteAction;
import uk.ac.kcl.cch.jb.pliny.actions.MinimizeAllAction;
import uk.ac.kcl.cch.jb.pliny.actions.PlinySelectAllAction;

/**
 * the ActionBarContributor for the {@link NoteEditor}.
 * 
 * @author John Bradley
 */

public class NoteEditorActionBarContributor extends ActionBarContributor {
	private NoteEditor currEditor = null;
	//private MakeNoteAction makeNoteAction = null;
	//private MakeConnectionAction makeConnectionAction = null;
	//private MinimizeAllAction minimizeAllAction = null;
	//private DisplayReferrerAction displayReferrerAction = null;
	//private GenerateContentsAsTextAction generateContentsAsTextAction = null;
    private PlinySelectAllAction selectAllAction = null;

	public NoteEditorActionBarContributor() {
		super();
		// TODO Auto-generated constructor stub
	}

	protected void buildActions() {
		addRetargetAction(new DeleteRetargetAction());
		addRetargetAction(new UndoRetargetAction());
		addRetargetAction(new RedoRetargetAction());
		//makeNoteAction = new MakeNoteAction();
		//makeConnectionAction = new MakeConnectionAction();
		//minimizeAllAction = new MinimizeAllAction();
		//displayReferrerAction = new DisplayReferrerAction();
		//generateContentsAsTextAction = new GenerateContentsAsTextAction();
		selectAllAction = new PlinySelectAllAction(null);
		
		addGlobalActionKey(ActionFactory.CUT.getId());
		addGlobalActionKey(ActionFactory.COPY.getId());
		addGlobalActionKey(ActionFactory.PASTE.getId());
		addGlobalActionKey(ActionFactory.SELECT_ALL.getId());
		addGlobalActionKey(ActionFactory.PRINT.getId());
	}
	
	/*public void contributeToToolBar(IToolBarManager toolBarManager) {
		super.contributeToToolBar(toolBarManager);
		//toolBarManager.add(getAction(ActionFactory.UNDO.getId()));
		//toolBarManager.add(getAction(ActionFactory.REDO.getId()));
		//toolBarManager.add(makeNoteAction);
		//toolBarManager.add(makeConnectionAction);
		//toolBarManager.add(minimizeAllAction);
		toolBarManager.add(displayReferrerAction);
		//toolBarManager.add(CreateMinimiseStatus.instance());
		//toolBarManager.add(generateContentsAsTextAction);
	} */

	protected void declareGlobalActionKeys() {
		// nothing here
	}
		
	private void updateEditor(NoteEditor targetEditor){
		EditDomain theDomain = null;
		if(targetEditor != null)theDomain = targetEditor.getEditDomain();
		//makeNoteAction.setEditDomain(theDomain);
		//makeConnectionAction.setEditDomain(theDomain);
		
		//minimizeAllAction.updatePart(targetEditor);
		//displayReferrerAction.setActivePart(targetEditor);
		selectAllAction.updatePart(targetEditor);
		targetEditor.updatePasteLocation();
		
		//generateContentsAsTextAction.setResource(targetEditor.getMyResource());
		//targetEditor.getSite().getKeyBindingService().registerAction(makeNoteAction);
	}
	
	public void setActiveEditor(IEditorPart targetEditor) {
		super.setActiveEditor(targetEditor);
		if(targetEditor instanceof NoteEditor){
			currEditor = (NoteEditor)targetEditor;
			updateEditor(currEditor);
		} else {
			currEditor = null;
			updateEditor(null);
		}
	}
}
