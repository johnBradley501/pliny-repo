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

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * The <code>EditorInput</code> object for the Pliny 
 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource Resource}-driven editors.  The
 * object holds the 
 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource Resource} that it refers to.
 * <p>
 * By making the object <code>IPersistableElement</code> and <code>IAdaptable</code> it is
 * made possible for open editors to persist between Eclipse/Pliny
 * sessions.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.model.Resource#idString2EditorInput
 * 
 * @author John Bradley
 *
 */

public class ResourceEditorInput implements IStorageEditorInput,
		IPersistableElement, IInputCanContainResource, IAdaptable {

	public static String RESOURCE_EDIT_INPUT_FACTORY_ID=
		"uk.ac.kcl.cch.jb.pliny.resourceEditorInputFactory";
	public static String RESOURCE_KEY_TAG = "resourceKey";
	public static String PAGE_NUMB_TAG = "pageNumber";

	private Resource myResource;
	private int pageNo = 0;
	
	public Resource getMyResource(){return myResource;}
	
	public ResourceEditorInput(Resource myResource) {
		this.myResource = myResource;
	}
	
	public int getPageNo(){
		return pageNo;
	}
	
	public void setPageNo(int numb){
		pageNo = numb;
	}

	public IStorage getStorage() throws CoreException {
		//return null;
		return EmptyIStorage.getInstance(); // seems to be needed during generation of contextual menu in 3.5

	}

	public boolean exists() {
		return myResource != null;
	}

	public ImageDescriptor getImageDescriptor() {
		if(myResource == null)return null;
		return ImageDescriptor.createFromImage(
				myResource.getObjectType().getIconImage());
	}

	public String getName() {
		if(myResource == null)return null;
		return myResource.getName();
	}

	public IPersistableElement getPersistable() {
		return this;
	}

	public String getToolTipText() {
		// TODO Auto-generated method stub
		return getName();
	}

	public Object getAdapter(Class adapter) {
		if(adapter.equals(Resource.class))return myResource;
		return null;
	}

	public String getFactoryId() {
		return RESOURCE_EDIT_INPUT_FACTORY_ID;
	}

	public void saveState(IMemento memento) {
		if((memento == null) || (myResource == null))return;
		memento.putInteger(RESOURCE_KEY_TAG, myResource.getALID());
		memento.putInteger(PAGE_NUMB_TAG, pageNo);
	}
	
	public boolean equals(Object obj){
		if(!(obj instanceof ResourceEditorInput))return false;
		ResourceEditorInput candidate = (ResourceEditorInput)obj;
		return candidate.myResource.getALID() == myResource.getALID();
	}

	public void setMyResource(Resource theResource) {
		myResource = theResource;
	}

}
