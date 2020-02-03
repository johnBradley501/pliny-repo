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

package uk.ac.kcl.cch.jb.pliny.imageRes;

import org.eclipse.gef.EditDomain;
import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.DeleteRetargetAction;
import org.eclipse.gef.ui.actions.RedoRetargetAction;
import org.eclipse.gef.ui.actions.UndoRetargetAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;

import uk.ac.kcl.cch.jb.pliny.actions.CreateMinimiseStatus;
import uk.ac.kcl.cch.jb.pliny.actions.DisplayReferrerAction;
import uk.ac.kcl.cch.jb.pliny.actions.MakeAnchorAction;
import uk.ac.kcl.cch.jb.pliny.actions.MakeAnnotationAction;
import uk.ac.kcl.cch.jb.pliny.actions.MakeConnectionAction;
import uk.ac.kcl.cch.jb.pliny.actions.MakeNoteAction;
import uk.ac.kcl.cch.jb.pliny.actions.MinimizeAllAction;

/**
 * the ActionBarContributor for the {@link ImageEditor}.
 * 
 * @author John Bradley
 */

public class ImageEditorActionBarContributor extends ActionBarContributor {

	private ImageEditor currEditor = null;
	// private MakeNoteAction makeNoteAction = null;
	// private MakeAnnotationAction makeAnnotationAction = null;
	// private MakeAnchorAction makeAnchorAction = null;
	// private MakeConnectionAction makeConnectionAction = null;
	// private MinimizeAllAction minimizeAllAction = null;
	// private DisplayReferrerAction displayReferrerAction = null;
	// private ZoomControlContributionItem zoomControl = null;
	
	protected void buildActions() {
		addRetargetAction(new DeleteRetargetAction());
		addRetargetAction(new UndoRetargetAction());
		addRetargetAction(new RedoRetargetAction());
		// makeNoteAction = new MakeNoteAction();
		// makeAnnotationAction = new MakeAnnotationAction();
		// makeAnchorAction = new MakeAnchorAction();
		// makeConnectionAction = new MakeConnectionAction();
		// minimizeAllAction = new MinimizeAllAction();
		// displayReferrerAction = new DisplayReferrerAction();
		// zoomControl = new ZoomControlContributionItem("zoom_image");
		
		addGlobalActionKey(ActionFactory.CUT.getId());
		addGlobalActionKey(ActionFactory.COPY.getId());
		addGlobalActionKey(ActionFactory.PASTE.getId());
		addGlobalActionKey(ActionFactory.PRINT.getId());
	}
	
	/* public void contributeToToolBar(IToolBarManager toolBarManager) {
		super.contributeToToolBar(toolBarManager);
		toolBarManager.add(getAction(ActionFactory.UNDO.getId()));
		toolBarManager.add(getAction(ActionFactory.REDO.getId()));
		toolBarManager.add(makeNoteAction);
		toolBarManager.add(makeConnectionAction);
		toolBarManager.add(makeAnnotationAction);
		toolBarManager.add(makeAnchorAction);
		toolBarManager.add(minimizeAllAction);
		toolBarManager.add(displayReferrerAction);
		toolBarManager.add(CreateMinimiseStatus.instance());
		// toolBarManager.add(zoomControl);
	} */

	protected void declareGlobalActionKeys() {
		// TODO Auto-generated method stub
		
	}
	
	/* private void updateEditor(ImageEditor targetEditor){
		EditDomain theDomain = null;
		if(targetEditor != null)theDomain = targetEditor.getMyEditDomain();
		// makeNoteAction.setEditDomain(theDomain);
		// makeAnchorAction.setEditDomain(theDomain);
		// makeAnnotationAction.setEditDomain(theDomain);
		// makeConnectionAction.setEditDomain(theDomain);
		
		// minimizeAllAction.updatePart(targetEditor);
		// displayReferrerAction.setActivePart(targetEditor);
		// zoomControl.setActiveEditor((IZoomableImageEditor)targetEditor);
	}
	
	public void setActiveEditor(IEditorPart targetEditor) {
		super.setActiveEditor(targetEditor);
		if(targetEditor instanceof ImageEditor){
			currEditor = (ImageEditor)targetEditor;
			updateEditor(currEditor);
		} else {
			currEditor = null;
			updateEditor(null);
		}
	} */

}
