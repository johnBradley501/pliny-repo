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

package uk.ac.kcl.cch.jb.pliny.views;

import java.util.Vector;

import uk.ac.kcl.cch.jb.pliny.dnd.ResourceExplorerDragListener;
import uk.ac.kcl.cch.jb.pliny.views.utils.DatedResourceRoot;
import uk.ac.kcl.cch.jb.pliny.views.utils.IResourceExplorerItem;
import uk.ac.kcl.cch.jb.pliny.views.utils.ResourceExplorerDropTarget;
import uk.ac.kcl.cch.jb.pliny.views.utils.ResourceExplorerRoot;

/**
 * the class that supports the date-ordered view of Resources in the
 * {@link ResourceExplorerView}.  The way this tab displays resources is almost entirely
 * managed by the root object of the TreeView:  
 * {@link uk.ac.kcl.cch.jb.pliny.views.utils.DatedResourceRoot DatedResourceRoot}.
 * 
 * @author John Bradley
 *
 */
public class REDateTab extends REBaseTab {
	public REDateTab(ResourceExplorerView view) {
		super(view);
	}
	
	public IResourceExplorerItem makeDisplayRoot() {
		return new DatedResourceRoot(this);
	}

	public String getTabName() {
		return "By Date";
	}

	public String getToolTipText() {
		return "View Resources by creation date";
	}
	
	protected void hookDragAndDrop(){
		new ResourceExplorerDragListener(this);
	}

}
