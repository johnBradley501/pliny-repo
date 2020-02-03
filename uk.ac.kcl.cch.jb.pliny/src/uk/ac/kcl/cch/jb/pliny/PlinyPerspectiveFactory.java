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

package uk.ac.kcl.cch.jb.pliny;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;

import uk.ac.kcl.cch.jb.pliny.views.ResHolderView;

/**
 * The class for the Pliny perspective.
 */
public class PlinyPerspectiveFactory implements IPerspectiveFactory {
	
	private static final String leftFolderID="uk.ac.kcl.cch.jb.pliny.leftFolder";

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();

		//IFolderLayout bottom = layout.createFolder(
		IPlaceholderFolderLayout bottom = layout.createPlaceholderFolder(
				"uk.ac.kcl.cch.jb.pliny.bottomFolder",
				IPageLayout.BOTTOM,
				0.80f,
				editorArea
				);
		bottom.addPlaceholder("uk.ac.kcl.cch.jb.pliny.containmentView");

		IFolderLayout left = layout.createFolder(
				leftFolderID,
				IPageLayout.LEFT,
				0.20f,
				editorArea
				);
		
		left.addView("uk.ac.kcl.cch.jb.pliny.resourceExplorer");
		left.addPlaceholder("uk.ac.kcl.cch.jb.pliny.searchView");


		IPlaceholderFolderLayout leftBottom = layout.createPlaceholderFolder("uk.ac.kcl.cch.jb.pliny.leftBottomFolder", 
				IPageLayout.BOTTOM, 0.70f, leftFolderID);
		leftBottom.addPlaceholder(ResHolderView.MY_ID);
		
		//IFolderLayout right = layout.createFolder(
		IPlaceholderFolderLayout right = layout.createPlaceholderFolder(
				"uk.ac.kcl.cch.jb.pliny.rightFolder",
				IPageLayout.RIGHT,
				0.75f,
				editorArea
				);
		right.addPlaceholder(IPageLayout.ID_OUTLINE);
		right.addPlaceholder("uk.ac.kcl.cch.jb.pliny.typeManager");
		//layout.addFastView(IPageLayout.ID_OUTLINE);
		
		layout.addNewWizardShortcut("uk.ac.kcl.cch.jb.pliny.createNoteAction");
	}

}
