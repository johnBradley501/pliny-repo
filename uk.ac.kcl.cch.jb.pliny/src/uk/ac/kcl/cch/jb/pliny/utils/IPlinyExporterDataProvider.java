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

import java.util.Iterator;
import java.util.Set;

import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

public interface IPlinyExporterDataProvider {
   public Iterator getPlugins();
   public Iterator getObjectTypes();
   public Iterator getLOTypes();
   public Iterator getResources();
   public Iterator getNotes();
   public Iterator getLinkableObjects();
   public Iterator getLinkableObjectsBelongingTo(Resource r);
   public Iterator getLinks();
   public Set getLinksBelongingTo(LinkableObject lo);
   public Iterator getFavourites();
}
