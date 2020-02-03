/*******************************************************************************
 * Copyright (c) 2008 John Bradley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Bradley - initial API and implementation
 *******************************************************************************/

package uk.ac.kcl.cch.jb.pliny.editors;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

public class MultiSourceSelectionProvider implements ISelectionProvider,
		ISelectionChangedListener {

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 * modelled after org.eclipse.ui.part.MultiPageSelectionProvider
	 */
    private ListenerList listeners = new ListenerList();
    //private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);
    private Set providers = new HashSet();
    private ISelectionProvider lastProvider = null;

    public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
		
	}
    
    public void dispose(){
    	Iterator it = providers.iterator();
    	while(it.hasNext()){
    		ISelectionProvider provider = (ISelectionProvider)it.next();
    		provider.removeSelectionChangedListener(this);
    	}
    }

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	public void addSelectionProvider(ISelectionProvider provider){
		provider.addSelectionChangedListener(this);
		providers.add(provider);
	}
	
	public void removeSelectionProvider(ISelectionProvider provider){
		if(!providers.contains(provider))return;
		provider.removeSelectionChangedListener(this);
		providers.remove(provider);
	}
	
    public void fireSelectionChanged(final SelectionChangedEvent event) {
        Object[] listeners = this.listeners.getListeners();
        for (int i = 0; i < listeners.length; ++i) {
            final ISelectionChangedListener l = (ISelectionChangedListener) listeners[i];
            Platform.run(new SafeRunnable() {
                public void run() {
                    l.selectionChanged(event);
                }
            });
        }
    }

	public ISelection getSelection(){
		if(lastProvider == null)return null;
		return lastProvider.getSelection();
	}

	public void setSelection(ISelection selection) {
		System.out.println("NoteEditor: unexpected set selection in MultiSourceSelectionProvider");
	}

	public void selectionChanged(SelectionChangedEvent event) {
		lastProvider = event.getSelectionProvider();
		//System.out.println("MultiSourceSelectionProvider event: "+event);
		fireSelectionChanged(event);
		
	}
	
}
