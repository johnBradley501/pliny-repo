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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LOTypeQuery;

/**
 * the JFace Content Provider for the Type Manager.  Note that the data
 * model behind this is Pliny's {@link uk.ac.kcl.cch.jb.pliny.model.LOType LOType}.
 * @author John Bradley
 *
 */
public class TypeManagerContentProvider implements IStructuredContentProvider, PropertyChangeListener {

	private TableViewer viewer;
	private Vector oldList;
	
	public TypeManagerContentProvider(TableViewer viewer) {
		this.viewer = viewer;
	}

	public Object[] getElements(Object inputElement) {
		LOTypeQuery q = new LOTypeQuery();
		//q.setOrderString("name");
		q.addOrder("name");
		Vector newList = q.executeQuery();
		if(oldList != null){
			Iterator it = oldList.iterator();
			while(it.hasNext()){
				LOType type = (LOType)it.next();
				type.removePropertyChangeListener(this);
			}
		}
		oldList = newList;
		Iterator it = newList.iterator();
		while(it.hasNext()){
			LOType type = (LOType)it.next();
			type.addPropertyChangeListener(this);
		}
		return newList.toArray();
	}

	public void dispose() {
		if(oldList != null){
			Iterator it = oldList.iterator();
			while(it.hasNext()){
				LOType type = (LOType)it.next();
				type.removePropertyChangeListener(this);
			}
		}
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// not needed
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		String pString = arg0.getPropertyName();
		if(pString == LOType.NAME_PROP || pString == LOType.TITLEBACKCOLOURINT_PROP ||
				pString == LOType.TITLEFORECOLOURINT_PROP){
			viewer.refresh(arg0.getNewValue());
		}
		
	}

}
