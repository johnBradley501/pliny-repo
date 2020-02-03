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

package uk.ac.kcl.cch.jb.pliny.actions;

import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.tools.ConnectionDragCreationTool;

/**
 * This tool extends the conventional GEF ConnectionDragCreation tool
 * by changing its behaviour: when the user finishs dragging to restore
 * the default tool for the editDomain.
 * <p>
 * It is used in 
 * {@link uk.ac.kcl.cch.jb.pliny.actions.MakeConnectionAction}.
 * 
 * @author John Bradley
 *
 */
public class MyConnectionDragCreationTool extends ConnectionDragCreationTool {

	public MyConnectionDragCreationTool(CreationFactory factory) {
		super(factory);
	}
	
	protected boolean handleButtonUp(int button) {
		super.handleButtonUp(button);
		getDomain().loadDefaultTool();
		return true;
	}

}
