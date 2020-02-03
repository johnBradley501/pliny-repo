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

package uk.ac.kcl.cch.jb.pliny.parts;

import org.eclipse.gef.EditDomain;
import org.eclipse.ui.IWorkbenchPart;

public class GenericPartBasedEditDomain extends EditDomain {
	
	IWorkbenchPart workbenchPart;

	public GenericPartBasedEditDomain(IWorkbenchPart workbenchPart) {
		super();
		setPart(workbenchPart);
	}

	public void setPart(IWorkbenchPart workbenchPart) {
		this.workbenchPart =  workbenchPart;
	}
	
	public IWorkbenchPart getPart(){
		return workbenchPart;
	}

}
