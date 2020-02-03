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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import uk.ac.kcl.cch.jb.pliny.model.MessageForGEF;

/**
 * provides an GEF editpart to display an error message provided
 * by model element
 * {@link uk.ac.kcl.cch.jb.pliny.model.MessageForGEF MessageForGEF}.
 * @author John Bradley
 *
 */

public class MessagePart extends AbstractGraphicalEditPart {

	public MessagePart(MessageForGEF message) {
		super();
		setModel(message);
	}
	
	protected MessageForGEF getMessage(){
		return (MessageForGEF)getModel();
	}

	protected IFigure createFigure() {
		return new Label(getMessage().getText());
	}

	protected void createEditPolicies() {
	}
	
	public void refreshVisuals(){
	       ((GraphicalEditPart) getParent().getParent()).setLayoutConstraint(
	 		      this,
	 		      getFigure(),
	 		      new Rectangle(50,50,150,150));
	}

}
