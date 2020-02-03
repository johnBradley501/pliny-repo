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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.eclipse.swt.graphics.Image;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.model.Favourite;
import uk.ac.kcl.cch.jb.pliny.model.FavouriteQuery;
import uk.ac.kcl.cch.rdb2java.Rdb2javaPlugin;
import uk.ac.kcl.cch.rdb2java.dynData.BaseQuery;

/**
 * provides the model object for the Favourites (now called 'Bookmarks')
 * list.  Tracks the creation and removal of 
 * {@link uk.ac.kcl.cch.jb.pliny.model.Favourite Favourite} items so that its list is
 * kept up-to-date.
 * 
 * @author John Bradley
 *
 */
public class ResourceExplorerFavGroupItem extends ResourceExplorerItemBase{
	private IResourceExplorerItem parent;
	private String name;
	private Set waitingForResource;
	private Set favourites;
	
	public ResourceExplorerFavGroupItem(IResourceTreeDisplayer myView, ResourceExplorerRoot parent, String name) {
		super(myView, null, null);
		this.parent = parent;
		this.name = name;
		waitingForResource = new HashSet();
		FavouriteQuery fq = new FavouriteQuery();
		favourites = new HashSet();
		favourites.addAll(fq.executeQuery());
		Rdb2javaPlugin.getDataServer().addPropertyChangeListener(this);
	}

	public void dispose() {
		Rdb2javaPlugin.getDataServer().removePropertyChangeListener(this);
	}

	public String getText() {
		return name;
	}

	public void setText(String name) {
		// not needed, cannot modify
	}

	public boolean canModify() {
		return false;
	}

	public Object getAssociatedObject() {
		return name;
	}

	public Image getIcon() {
		return PlinyPlugin.getImageDescriptor("icons/favIcon.gif").createImage();
	}

	public IResourceExplorerItem getParent() {
		return parent;
	}

	public boolean hasChildren() {
		return getNumberChildren() != 0;
	}

	public int getNumberChildren() {
		return favourites.size();
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		String name = arg0.getPropertyName();
		if(name.equals("Create-Favourite")){
			Favourite fav = (Favourite)arg0.getNewValue();
			favourites.add(fav);
			if(fav.getResource() == null){
				fav.addPropertyChangeListener(this);
				this.waitingForResource.add(fav);
			} else {
			   updateMyChildren();
			   myView.getMyViewer().refresh(this);
			}
		}
		else if(name.equals("Delete-Favourite")){
			Favourite fav = (Favourite)arg0.getOldValue();
			favourites.remove(fav);
			updateMyChildren();
			myView.getMyViewer().refresh(this);
		} else if(name == Favourite.FAVOURITERESOURCE_PROP){
			Favourite fav = (Favourite)arg0.getNewValue();
			fav.removePropertyChangeListener(this);
			this.waitingForResource.remove(fav);
			updateMyChildren();
			myView.getMyViewer().refresh(this);
		}
		
	}

	public Iterator createListIterator() {
		SortedMap sortedFavs = new TreeMap(ResourceNameManager.getMyCollator());
		Iterator it = favourites.iterator();
		while(it.hasNext()){
			Favourite fav = (Favourite)it.next();
			if(fav.getResource() != null)
			   sortedFavs.put(fav.getResource().getFullName(),fav);
		}
		return sortedFavs.values().iterator();
	}

	public IResourceExplorerItem makeChild(Object item) {
		return new ResourceExplorerFavouriteItem(myView, this, (Favourite)item);
	}

}
