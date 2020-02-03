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

package uk.ac.kcl.cch.jb.pliny.dnd;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.dnd.AbstractTransferDropTargetListener;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;

import uk.ac.kcl.cch.jb.pliny.browser.BrowserResourceExtensionProcessor;
import uk.ac.kcl.cch.jb.pliny.commands.CreateLinkableObjectCommand;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.ObjectType;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.utils.StringToNoteHandler;

/**
 * handles the target end of a Pliny DnD, when the source
 * is text from outside of Pliny, and the target is to a
 * reference/annotation area.  
 * <p>What this code does depends upon the form of the text provided.  If the
 * text starts with <code>http:</code> or <code>https:</code> it is assumed to
 * be a URL, and the codee will translate the DnD request into a request for a
 * new Pliny {@link uk.ac.kcl.cch.jb.pliny.model.Resource Resource} of the
 * web-browser type and a {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject LinkableObject} to display it
 * in the target.  Otherwise, the code will translate the
 * request into a request for a new Pliny
 * {@link uk.ac.kcl.cch.jb.pliny.model.NoteLucened NoteLucened}
 * to contain the text, and a 
 * {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject LinkableObject} to display it
 * in the target.
 * <p>
 * An attempt is made to see of the provided text can be intrepreted as
 * providing a title.  If the text is a URL to an HTML page, the process
 * will fetch the page's <code>title</code> element to provide
 * a title.  If the provided text is not a URL, the text itself is
 * examined to see if it contains a suitable
 * first line title, which, if it does, is used as a name for the
 * created Note.
 * <p>
 * This class also implements <code>org.eclipse.gef.requests.CreationFactory</code>
 * which allows it to act as its own factory for creating the new Reference or NoteLucened
 * object.
 * 
 * @author John Bradley
 *
 */

public class PlinyTextTransferDropTargetListener extends
		AbstractTransferDropTargetListener implements CreationFactory {

	private Resource theResource = null;

	public PlinyTextTransferDropTargetListener(EditPartViewer viewer) {
		super(viewer, TextTransfer.getInstance());
	}

	protected void handleDragOver() {
		   getCurrentEvent().detail = DND.DROP_COPY;
		   super.handleDragOver();
		}
	
	protected Request createTargetRequest() {
		   CreateRequest request = new CreateRequest();
		   request.setFactory(this);
		   return request;
		}

	protected void updateTargetRequest() {
		//((CreateRequest)getTargetRequest()).setLocation(getDropLocation());
		Request theRequest = getTargetRequest();
		CreateRequest myRequest = (CreateRequest)theRequest; // getTargetRequest();
		myRequest.setLocation(getDropLocation());
		myRequest.setSize(CreateLinkableObjectCommand.DEFAULT_NOTE_DIMENSION);
	}
	
	protected void handleDrop() {
		DropTargetEvent event = getCurrentEvent();
		boolean rslt = false;
		if(TextTransfer.getInstance().isSupportedType(event.currentDataType)){
			rslt = processData((String)event.data);
		}
		
		if(!rslt)event.detail = DND.DROP_NONE;
		else event.detail = DND.DROP_COPY;
		super.handleDrop();
	}
	
	private boolean processData(String rawInput){
		String input = rawInput.trim();
		if(input.startsWith("http:") || input.startsWith("https:"))
			theResource = BrowserResourceExtensionProcessor.processAsURL(input);
		else theResource = StringToNoteHandler.getInstance().handleStringAsNote(input);
		return theResource != null;
	}

	// ===== CreationFactory objects
	
	public Object getNewObject() {
		return theResource;
	}

	public Object getObjectType() {
		if(theResource == null)return NoteLucened.class;
		return theResource.getClass();
	}

}
