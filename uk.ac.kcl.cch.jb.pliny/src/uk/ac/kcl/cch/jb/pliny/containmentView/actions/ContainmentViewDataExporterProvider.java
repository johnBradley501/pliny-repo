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

package uk.ac.kcl.cch.jb.pliny.containmentView.actions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;

import uk.ac.kcl.cch.jb.pliny.containmentView.model.ContainmentItem;
import uk.ac.kcl.cch.jb.pliny.model.Favourite;
import uk.ac.kcl.cch.jb.pliny.model.Link;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.Note;
import uk.ac.kcl.cch.jb.pliny.model.Plugin;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.utils.IPlinyExporter;
import uk.ac.kcl.cch.jb.pliny.utils.IPlinyExporterDataProvider;
import uk.ac.kcl.cch.jb.pliny.utils.PlinyExportException;

public class ContainmentViewDataExporterProvider implements IPlinyExporterDataProvider {

	private Set loTypes = null;
	private Set linkableObjects = null;
	private Set links = null;
	private Set notes = null;
	private Set objectTypes = null;
	private Set plugins = null;
	private Set resources = null;
	private Favourite theFav = null;
	
	//private int savedResourceNo = 0;
	//private Set savedSubset = null;
	
	public ContainmentViewDataExporterProvider(Vector items, Resource startingResource){
		loTypes = new HashSet();
		linkableObjects = new HashSet();
		links = new HashSet();
		links = new HashSet();
		plugins = new HashSet();
		objectTypes = new HashSet();
		notes = new HashSet();
		resources = new HashSet();
		init(items, startingResource);
	}
	
	private void init(Vector items, Resource startingResource){
		Resource theFavResource = null;
		if(items.size() > 0) theFavResource = ((ContainmentItem)items.get(0)).getResource();
		Iterator it = items.iterator();
		int noteObjectType = Note.getNoteObjectType().getALID();
		while(it.hasNext()){
			ContainmentItem item = (ContainmentItem)it.next();
			Resource myResource = item.getResource();
			if(myResource != null){
				resources.add(myResource);
				objectTypes.add(myResource.getObjectType());
				if(myResource.getObjectType().getALID() == noteObjectType)
					notes.add(myResource);
				Plugin myPlugin = myResource.getObjectType().getPlugin();
				plugins.add(myPlugin);
				if(startingResource.getALID() == myResource.getALID())theFavResource = startingResource;
			}
		}
		processLinkableObjects(items);
		
		theFav = new Favourite(true);
		theFav.setResource(theFavResource);
	}
	
	private void processLinkableObjects(Vector items) {
		Iterator it = items.iterator();
		while(it.hasNext()){
			ContainmentItem item = (ContainmentItem)it.next();
			Resource myResource = item.getResource();
			Vector los = myResource.getMyDisplayedItems().getItems();
			Iterator it2 = los.iterator();
			while(it2.hasNext()){
				LinkableObject obj = (LinkableObject)it2.next();
				if(obj.getSurrogateFor() == null || resources.contains(obj.getSurrogateFor())){
					linkableObjects.add(obj);
					loTypes.add(obj.getLoType());
				}
				Vector cons = obj.getLinkedTo().getItems();
				Iterator it3 = cons.iterator();
				while(it3.hasNext()){
					Link lnk = (Link)it3.next();
					Resource lnkRs = lnk.getFromLink().getSurrogateFor();
					if(lnkRs != null && resources.contains(lnkRs))links.add(lnk);
				}
					
			}
		}		
	}

	public Iterator getFavourites() {
		Vector rslt = new Vector();
		if(theFav.getResource() == null)return rslt.iterator();
		rslt.add(theFav);
		return rslt.iterator();
	}

	public Iterator getLOTypes() {
		return loTypes.iterator();
	}

	public Iterator getLinkableObjects() {
		return linkableObjects.iterator();
	}

	public Iterator getLinks() {
		return links.iterator();
	}

	public Iterator getNotes() {
		return notes.iterator();
	}

	public Iterator getObjectTypes() {
		return objectTypes.iterator();
	}

	public Iterator getPlugins() {
		return plugins.iterator();
	}

	public Iterator getResources() {
		return resources.iterator();
	}

	public Iterator getLinkableObjectsBelongingTo(Resource r) {
		//if(r.getALID() == savedResourceNo)return savedSubset.iterator();
		Iterator builder = linkableObjects.iterator();
		Set subset = new HashSet();
		while(builder.hasNext()){
			LinkableObject obj = (LinkableObject)builder.next();
			if(obj.getDisplayedInKey() == r.getALID())subset.add(obj);
		}
		//savedSubset = subset;
		//savedResourceNo = r.getALID();
		return subset.iterator();
	}

	public Set getLinksBelongingTo(LinkableObject lo) {
		int loKey = lo.getALID();
		Iterator builder = links.iterator();
		Set subset = new HashSet();
		while(builder.hasNext()){
			Link obj = (Link)builder.next();
			if(obj.getFromLinkKey()==loKey || obj.getToLinkKey()==loKey)subset.add(obj);
		}
		return subset;
	}

//	public Iterator getLinksBelongingTo(Resource r) {
//		Set linkset = new HashSet();
//		Set loSet = null;
//		if(r.getALID() != savedResourceNo)getLinkableObjectsBelongingTo(r);
//		Iterator builder = savedSubset.iterator();
//		while(builder.hasNext()){
//			LinkableObject obj = (LinkableObject)builder.next();
//			linkset.addAll(obj.getLinkedFrom().getItems());
//			linkset.addAll(obj.getLinkedTo().getItems());
//		}
//		return linkset.iterator();
//	}

}
