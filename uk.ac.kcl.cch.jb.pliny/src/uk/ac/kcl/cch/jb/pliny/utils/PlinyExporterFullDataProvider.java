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
package uk.ac.kcl.cch.jb.pliny.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import uk.ac.kcl.cch.jb.pliny.model.FavouriteQuery;
import uk.ac.kcl.cch.jb.pliny.model.LOTypeQuery;
import uk.ac.kcl.cch.jb.pliny.model.LinkQuery;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObjectQuery;
import uk.ac.kcl.cch.jb.pliny.model.ModelDataIterator;
import uk.ac.kcl.cch.jb.pliny.model.NoteQuery;
import uk.ac.kcl.cch.jb.pliny.model.ObjectTypeQuery;
import uk.ac.kcl.cch.jb.pliny.model.PluginQuery;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.ResourceQuery;
import uk.ac.kcl.cch.rdb2java.dynData.BaseQuery;

public class PlinyExporterFullDataProvider implements IPlinyExporterDataProvider{

	public Iterator getFavourites() {
		//return new FavouriteQuery().executeQuery().iterator();
		return new ModelDataIterator(new FavouriteQuery());
	}

	public Iterator getLOTypes() {
		return new LOTypeQuery().executeQuery().iterator();
	}

	public Iterator getLinkableObjects() {
		//return new LinkableObjectQuery().executeQuery().iterator();
		return new ModelDataIterator(new LinkableObjectQuery());
	}

	public Iterator getLinks() {
		//return new LinkQuery().executeQuery().iterator();
		return new ModelDataIterator(new LinkQuery());
	}

	public Iterator getNotes() {
		//return new NoteQuery().executeQuery().iterator();
		return new ModelDataIterator(new NoteQuery());
	}

	public Iterator getObjectTypes() {
		return new ObjectTypeQuery().executeQuery().iterator();
	}

	public Iterator getPlugins() {
		return new PluginQuery().executeQuery().iterator();
	}

	public Iterator getResources() {
		//return new ResourceQuery().executeQuery().iterator();
		return new ModelDataIterator(new ResourceQuery());
	}

	public Iterator getLinkableObjectsBelongingTo(Resource r) {
		LinkableObjectQuery q = new LinkableObjectQuery();
		q.addConstraint("displayedInKey", BaseQuery.FilterEQUAL, r.getALID());
		return new ModelDataIterator(q);
	}

	public Set getLinksBelongingTo(LinkableObject lo) {
		Set rsltSet = new HashSet();
		rsltSet.addAll(lo.getLinkedFrom().getItems());
		rsltSet.addAll(lo.getLinkedTo().getItems());
		return rsltSet;
	}

}
