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

package uk.ac.kcl.cch.jb.pliny.containmentView.parts;

import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentItem;
import uk.ac.kcl.cch.jb.pliny.figures.TopPanel;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.parts.IButtonHolderPart;

/**
 * This draw2d figure draws the box and contents for each 
 * {@link uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentItem ContainmentItem}
 * in a ContainmentView.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.containmentView.parts.ContainmentItemPart
 * 
 * @author John Bradley
 *
 */

public class ContainmentItemFigure extends TopPanel {
	private Resource myResource;
	private boolean selected;
	private boolean hasFocus;
	
	/**
	 * builds a figure based on the information contained in the
	 * associated ContainmentItem.
	 * 
	 * @param item ContainmentItem associated item.
	 */

	public ContainmentItemFigure(ContainmentItem item) {
		super();
		setBorder(new LineBorder(ColorConstants.black,1));
		maxNameLength = 40;

		myResource = item.getResource();
		canMinimize = false;
		myName = myResource.getName();
		
		this.foreground = ColorConstants.black;
		this.background = ColorConstants.white;
		
		objectTypeIcon = myResource.getObjectType().getIconImage();
		doBuild();
	}
	
	protected void doEditorOpen(){
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(); 
		try {
			myResource.openEditor(page);
		} catch (PartInitException e1) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), 
					"Opening Failure",
			"This resource could not be opened for editing.");
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}


	protected Label buildIdentifierIcon(){
		Label openEditorLabel = new Label(objectTypeIcon);
        openEditorLabel.addMouseMotionListener(new OpenButtonMouseMotionListener(myResource));
        openEditorLabel.addMouseListener(new MouseListener(){

			public void mouseDoubleClicked(MouseEvent me) {
				doEditorOpen();
			}

			public void mousePressed(MouseEvent me) {
				// do nothing
			}

			public void mouseReleased(MouseEvent me) {
				// do nothing
				
			}
			
		});
        return openEditorLabel;
	}

	public ContainmentItemFigure(String myName, boolean canOpen,
			boolean isOpen, Color background, Color foreground,
			IButtonHolderPart thePart, Image topLeftIcon,
			Clickable[] otherButtons) {
		super(myName, 0, canOpen, isOpen, background, foreground, thePart,
				topLeftIcon, otherButtons);
	}

	/**
	 * Sets the selection state of this ContainmentItemFigure
	 * @param b true will cause the figure to appear selected.
	 */
	public void setSelected(boolean b) {
		if (selected != b) {
			selected = b;
			setBorder(new LineBorder(ColorConstants.black,selected?2:1));

			repaint();
		}
	}

	/**
	 * Sets the focus state of this ContainmentItemFigure
	 * @param b true will cause a focus rectangle to be drawn around the figure.
	 */
	public void setFocus(boolean b) {
		if (hasFocus != b) {
			hasFocus = b;
			setBorder(new LineBorder(ColorConstants.black,selected?2:1));
			repaint();
		}
	}

}
