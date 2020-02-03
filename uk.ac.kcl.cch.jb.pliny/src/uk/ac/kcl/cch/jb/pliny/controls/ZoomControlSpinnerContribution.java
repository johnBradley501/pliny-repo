/*******************************************************************************
 * Copyright (c) 2014 John Bradley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Bradley - initial API and implementation
 *******************************************************************************/

package uk.ac.kcl.cch.jb.pliny.controls;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ZoomControlSpinnerContribution extends ControlContribution {
	
	private IZoomableImageEditor targetEditor;
	private ZoomControlSpinner myZoomControl = null;

	public ZoomControlSpinnerContribution(String id, IZoomableImageEditor targetEditor){
		super(id);
		this.targetEditor = targetEditor;
	}

	@Override
	protected Control createControl(Composite parent) {
		myZoomControl = new ZoomControlSpinner(parent, targetEditor);
		return myZoomControl.getControl();
	}
	
	public void dispose(){
		super.dispose();
		myZoomControl.dispose();
	}
	
	public void refresh(){
		if(myZoomControl != null)myZoomControl.refresh();
	}

}
