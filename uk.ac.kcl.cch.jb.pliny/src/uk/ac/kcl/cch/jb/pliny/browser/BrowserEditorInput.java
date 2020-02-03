/*******************************************************************************
 * Copyright (c) 2003, 2005, 2007 IBM Corporation, John Bradley and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *     John Bradley - modifications to fit Pliny model
 *******************************************************************************/

package uk.ac.kcl.cch.jb.pliny.browser;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;

/**
 * The IEditorInput for the Pliny browser.  It is similar to the input
 * class defined for org.eclipse.ui.internal.browser.  It also acts
 * as the IElementFactory for itself.
 * 
 * @author John Bradley
 *
 */

public class BrowserEditorInput implements IEditorInput, IPersistableElement,
		IElementFactory {

	private static final String ELEMENT_FACTORY_ID = "uk.ac.kcl.cch.jb.pliny.browser.elementFactory"; //$NON-NLS-1$
	
	URL myUrl;
	String myTitle;

	public BrowserEditorInput() {
		myUrl = null;
		myTitle = null;
	}
	
	public BrowserEditorInput(URL myUrl){
		this.myUrl = myUrl;
	}
	
	public BrowserEditorInput(String theUrl) throws MalformedURLException{
		this.myUrl = new URL(theUrl);
	}
	
	public URL getUrl(){
		return myUrl;
	}
	
	public void setUrl(URL newUrl){
		myUrl = newUrl;
	}
	
	public String getTitle(){
		return myTitle;
	}
	
	public void setTitle(String title){
		myTitle = title;
	}

	public boolean exists() {
		return true;
	}

	public ImageDescriptor getImageDescriptor() {
		return PlinyPlugin.getImageDescriptor("icons/browserIcon.gif");
		//		"icons/browser/obj16/internal_browser.gif");
	}

	public String getName() {
		if(myTitle != null) return myTitle;
		if(myUrl != null)return myUrl.toExternalForm();
		return "Web Browser";
	}

	public IPersistableElement getPersistable() {
		return this;
	}

	public String getToolTipText() {
		return getName();
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

	public String getFactoryId() {
		return ELEMENT_FACTORY_ID;
	}

	public void saveState(IMemento memento) {
		if(memento == null)return;
		if(myUrl != null)
		    memento.putString("url",myUrl.toExternalForm());
		if(myTitle != null)
			memento.putString("title",myTitle);

	}

	public IAdaptable createElement(IMemento memento) {
		URL theUrl = null;
		String theTitle = null;
		try {
			theUrl = new URL(memento.getString("url"));
			theTitle = memento.getString("title");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		BrowserEditorInput rslt = new BrowserEditorInput(theUrl);
		rslt.setTitle(theTitle);
		return rslt;
	}
	
	public boolean equals(Object obj){
		if(obj == this)return true;
		if(!(obj instanceof BrowserEditorInput))return false;
		BrowserEditorInput other = (BrowserEditorInput)obj;
		return (myUrl == null) || myUrl.equals(other.myUrl);
	}

}
