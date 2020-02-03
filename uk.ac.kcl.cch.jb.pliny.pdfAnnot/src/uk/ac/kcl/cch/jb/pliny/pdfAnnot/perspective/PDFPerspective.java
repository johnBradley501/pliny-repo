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

package uk.ac.kcl.cch.jb.pliny.pdfAnnot.perspective;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import uk.ac.kcl.cch.jb.pliny.pdfAnnot.PDFEditor;

/**
 * The PDF Perspective as provided with the JPedal Eclipse plugin examples.
 *
 */
public class PDFPerspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		
		if(PDFEditor.debug)
			System.out.println("createInitialLayout called");
		
		try{
			String editorArea=layout.getEditorArea();
			
			//Outline on left
			layout.addView(IPageLayout.ID_OUTLINE,IPageLayout.LEFT,0.25f,editorArea);
		
		}catch(Exception e){
			e.printStackTrace();
		}catch(Error e){
			e.printStackTrace();
		}
	}
}
