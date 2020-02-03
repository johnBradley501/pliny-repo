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

/**
 * 
 */
package uk.ac.kcl.cch.jb.pliny.views.utils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.rdb2java.Rdb2javaPlugin;

/**
 * the content provider for the the JFace Tree viewers displayed by
 * Resource Explorer and Resource-Explorer-like views.  Like all
 * JFace content providers, this class mediates between the Resource
 * Explorer model elements (instances of
 * {@link IResourceExplorerItem} and the SWT Tree.
 * 
 * @author John Bradley
 *
 */
public class ResourceExplorerContentProvider implements
		IStructuredContentProvider,ITreeContentProvider,
	    PropertyChangeListener {

	IResourceTreeDisplayer myViewTree;
    private Object[] elements = new Object[0];
    
    static private boolean autoRefreshEnabled = true;
    static public void setAutoRefreshEnabled(boolean val){autoRefreshEnabled = val;}
	
	/**
	 * 
	 */
	public ResourceExplorerContentProvider(IResourceTreeDisplayer myViewPart) {
		this.myViewTree = myViewPart;
		Rdb2javaPlugin.getDataServer().addPropertyChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		// TODO Auto-generated method stub
		return getChildren(inputElement);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		Rdb2javaPlugin.getDataServer().removePropertyChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if(newInput != null
				   && newInput instanceof IStructuredSelection)
				{
					IStructuredSelection structured =
					   (IStructuredSelection)newInput;
					elements = structured.toArray();
					viewer.refresh();
				}
	}

	public Object[] getChildren(Object parentElement) {
		if(!(parentElement instanceof IResourceExplorerItem)) return new Object[0];
		return ((IResourceExplorerItem)parentElement).getChildren().toArray();
	}

	public Object getParent(Object element) {
		if(!(element instanceof IResourceExplorerItem)) return null;
		return ((IResourceExplorerItem)element).getParent();
	}

	public boolean hasChildren(Object element) {
		if(!(element instanceof IResourceExplorerItem)) return false;
		return ((IResourceExplorerItem)element).hasChildren();
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		//String propertyName = arg0.getPropertyName();
		if(autoRefreshEnabled)refreshMyTree();
	}
	
	public void refreshMyTree(){
	   myViewTree.getMyViewer().refresh();
	}

}
