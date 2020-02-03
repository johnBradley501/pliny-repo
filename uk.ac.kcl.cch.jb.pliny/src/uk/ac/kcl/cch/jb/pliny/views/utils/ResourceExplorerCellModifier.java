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

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeItem;

import uk.ac.kcl.cch.jb.pliny.commands.UpdateNameCommand;
import uk.ac.kcl.cch.jb.pliny.model.INamedObject;
import uk.ac.kcl.cch.jb.pliny.views.ResourceExplorerView;

/**
 * a JFace ICellModifier that manages the direct editing of the names
 * of item within the Resource Explorer, and ResourceExplorer-like viewParts.
 * 
 * @author John Bradley
 *
 */

public class ResourceExplorerCellModifier implements ICellModifier {
	private TreeViewer viewer;
	private IResourceTreeDisplayer myPart;
	
	public ResourceExplorerCellModifier(TreeViewer viewer, IResourceTreeDisplayer myPart) {
		this.myPart = myPart;
		this.viewer = viewer;
	}

	public boolean canModify(Object element, String property) {
		if(property != ResourceExplorerView.NAME_ID) return false;
		if(!(element instanceof IResourceExplorerItem))return false;
		return ((IResourceExplorerItem)element).canModify();
	}

	public Object getValue(Object element, String property) {
		if(property != ResourceExplorerView.NAME_ID) return null;
		if(!(element instanceof IResourceExplorerItem))return null;
		return ((IResourceExplorerItem)element).getText();
	}

	public void modify(Object item, String property, Object value) {
		if(value == null)return;
		
		Object element = item;
		if(item instanceof TreeItem)
			element = ((TreeItem)item).getData();
		String text = ((String)value).trim();
		if(property != ResourceExplorerView.NAME_ID)return;
		
		if(!(element instanceof IResourceExplorerItem)) return;
		IResourceExplorerItem reItem = (IResourceExplorerItem)element;
		if(!reItem.canModify())return;
		
		Object model = reItem.getAssociatedObject();
		if(!(model instanceof INamedObject))return;
		
		if(((INamedObject)model).getName().equals(text))return;

		myPart.getCommandStack().execute(
			new UpdateNameCommand(viewer, (INamedObject)model, text));
	}

}
