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

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.swt.graphics.Image;

import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;

/**
 * used by the {@link uk.ac.kcl.cch.jb.pliny.views.NoteSearchView NoteSearchView}
 * as the root of the model for its display, and contains items
 * that were selected by the query.  Since the NoteSearchView is
 * like the Resource Explorer in some ways and reuses some of this code,
 * this root is a {@link IResourceExplorerItem}.
 * @author John Bradley
 *
 */
public class NoteSearchRoot implements IResourceExplorerItem {

	public Vector noteList;
	public Vector rsltList = null;
	private IResourceTreeDisplayer myViewer;
	
	/**
	 * the constructor for this item.
	 * 
	 * @param noteList a Vector of 
	 * {@link uk.ac.kcl.cch.jb.pliny.model.NoteLucened NoteLucene}s 
	 * that were selected by
	 * the search query.  These will be this object's children.
	 * 
	 * @param myViewer the owning ViewPart.
	 */
	public NoteSearchRoot(Vector noteList, IResourceTreeDisplayer myViewer) {
		super();
		this.noteList = noteList;
		this.myViewer = myViewer;
	}
	
	public void dispose(){
		Iterator it = rsltList.iterator();
		while(it.hasNext()){
			ResourceExplorerResourceItem item = (ResourceExplorerResourceItem)it.next();
			item.dispose();
		}
	}

	public String getText() {
		return "root";
	}

	public void setText(String name) {
		// do nothing -- this is a root object
	}

	public boolean canModify() {
		return false;
	}

	public Object getAssociatedObject() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getPageNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Image getIcon() {
		return null;
	}

	public IResourceExplorerItem getParent() {
		return null;
	}

	public boolean hasChildren() {
		return getNumberChildren() != 0;
	}

	public int getNumberChildren() {
		if(noteList == null)return 0;
		return noteList.size();
	}

	public List getChildren() {
		if(rsltList == null){
		   rsltList = new Vector();
		   Iterator it = noteList.iterator();
		   while(it.hasNext()){
			   NoteLucened note = (NoteLucened)it.next();
			   rsltList.add(
					   new ResourceExplorerResourceItem(myViewer, this,note));
		   }
		}
		return rsltList;
	}

}
