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

package uk.ac.kcl.cch.jb.pliny;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.part.WorkbenchPart;
import org.osgi.framework.Bundle;

import uk.ac.kcl.cch.jb.pliny.model.ObjectType;

/**
 * Other plugins who wish to work with Pliny components should use
 * this class as a connection to Pliny.
 * <p>
 * Note: I'm not sure this object is "finished".
 */

public class PlinyHandle {
	
	private Plugin plugin;
	private WorkbenchPart part;

	public PlinyHandle(Plugin plugin, WorkbenchPart part) {
		this.plugin = plugin;
		this.part = part;
	}
	
	public Plugin getPlugin(){return plugin;}
	public WorkbenchPart getPart(){return part;}
	
	private ObjectType setupObjectType(){
		Bundle theBundle = plugin.getBundle();
		String bundleId = theBundle.getSymbolicName();
		IWorkbenchPartSite site = null;
		String iconId = null;
		if(part instanceof IEditorPart){
			site = ((IEditorPart)part).getSite();
			iconId = "editor:";
		}
		else if(part instanceof IViewPart){
			site = ((IViewPart)part).getSite();
			iconId = "view:";
		}
		String partId = site.getId();
		iconId += partId;
		ObjectType ot = ObjectType.findFromIds(bundleId, partId);
		if(ot == null){
			ot = new ObjectType();
			ot.setPlugin(uk.ac.kcl.cch.jb.pliny.model.Plugin.
					findFromId(bundleId));
			ot.setEditorId(partId);
			ot.setName(site.getRegisteredName());
			//ImageDescriptor id = ImageDescriptor.createFromImage(part.getTitleImage());
			ot.setIconId(iconId);
		}
		//String partId = part.
		return ot;
	}

}
