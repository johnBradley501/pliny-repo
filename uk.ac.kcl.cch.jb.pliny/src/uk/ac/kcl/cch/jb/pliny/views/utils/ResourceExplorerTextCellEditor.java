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

package uk.ac.kcl.cch.jb.pliny.views.utils;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * a JFace TextCellEditor that manages the direct editing of the names
 * of item within the Resource Explorer, and ResourceExplorer-like viewParts.
 * 
 * @author John Bradley
 *
 */

public class ResourceExplorerTextCellEditor extends TextCellEditor {
	
	private boolean focusSet = false;
	private Action deleteAction = null;

	public ResourceExplorerTextCellEditor(Composite parent, Action deleteAction) {
		super(parent);
		this.deleteAction = deleteAction;
	}
	
	protected Control createControl(Composite parent) {
		Control text = super.createControl(parent);
		text.addMouseListener(new MouseAdapter(){
	           public void mouseUp(MouseEvent e) {
	        	   focusSet = false;
	        	   //System.out.println("mouseUp, focusSet=false");
	           }
		});
		return text;
	}
		   
	
	public void setFocus(){
		focusSet = true;
		//System.out.println("setFocus called");
		super.setFocus();
	}
	
	protected void keyReleaseOccured(KeyEvent keyEvent){
		//System.out.println("keyReleaseOccured, char"+keyEvent.keyCode+", focusSet="+focusSet);
		if(keyEvent.character == SWT.DEL){
			if(focusSet){
				fireCancelEditor();
				deleteAction.run();
				return;
			}
		}
		focusSet = false;
		super.keyReleaseOccured(keyEvent);
	}
}
