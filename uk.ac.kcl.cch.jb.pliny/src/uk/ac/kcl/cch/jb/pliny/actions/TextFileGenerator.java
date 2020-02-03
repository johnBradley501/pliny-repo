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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Shell;

import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.Note;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.utils.TwoDOrderer;
import uk.ac.kcl.cch.jb.xmlWriter.XmlElement;
import uk.ac.kcl.cch.jb.xmlWriter.XmlFile;

/**
 * this text generator takes material in a reference area attached to
 * a given resource and writes it (with optional recursion into the
 * resources contained by reference therein) to a file (as HTML).
 * 
 * It is invoked by the action
 * {@link uk.ac.kcl.cch.jb.pliny.actions.GenerateContentsAsTextAction GenerateContentsAsTextAction},
 * and gets optional parameters from the wizard
 * {@link uk.ac.kcl.cch.jb.pliny.actions.TextFileGeneratorWizard TextFileGeneratorWizard}.
 * 
 * @author John Bradley
 *
 */

public class TextFileGenerator extends TextFileGeneratorBase{
	
	private Set displayedResources;

	private boolean recursiveProcessing = true;
	private int recursionDepth = 9999;

	/**
	 * creates an instance of TextFileGenerator.
	 * 
	 * @param headResource the resource to have its reference area written to a file.
	 * @param fileName the name of the file to be contain this generated materials.
	 */
	public TextFileGenerator(Resource headResource, String fileName) {
		super(headResource, fileName);
		displayedResources = new HashSet();
		displayedResources.add(headResource);
	}
	
	/**
	 * runs the associated wizard to get options from the user.  returns
	 * <code>false</code> if the user has cancelled the export by pushing
	 * the 'cancel' button.
	 * 
	 * @param parentShell
	 * @return returns <code>false</code> if the user has cancelled the export
	 */

	public boolean getOptions(Shell parentShell) {
		TextFileGeneratorWizard wizard = new TextFileGeneratorWizard(this);
		WizardDialog dialog =
			new WizardDialog(parentShell, wizard);
		dialog.open();
		return wizard.getDoImport();
	}

	protected Vector getTopItems() {
		return TwoDOrderer.selectAndOrderContents(headResource.getMyDisplayedItems().getItems(), true);
	}

	protected void createBody(XmlElement htmlHead, Vector topItems) throws IOException {
		XmlElement body = new XmlElement("body");
		htmlHead.addContent(body);
		XmlElement header = new XmlElement("h2");
		body.addContent(header);
		header.addContent(headResource.getName());
		if(headResource instanceof Note)
		  handleNoteContents(body, (Note)headResource);
		handleContent(body, topItems, 0);
	}

	private void handleContent(XmlElement container, Vector topItems, int currentDepth) throws IOException {
		if(topItems.size() == 0)return;
		XmlElement list = new XmlElement("ul");
		container.addContent(list);
		Iterator it = topItems.iterator();
		while(it.hasNext()){
			LinkableObject lo = (LinkableObject)it.next();
			handleLinkableObject(list, lo, currentDepth);
			if(currentDepth == 0)monitor.worked(1);
		}
	}

	private void handleLinkableObject(XmlElement list, LinkableObject lo, int currentDepth) throws IOException {
		if(lo.getLoType().getALID() == LOType.getBibRefType().getALID())return;
		Resource myResource = lo.getSurrogateFor();
		if(myResource == null)return;
		XmlElement item = new XmlElement("li");
		list.addContent(item);
		handleResourceName(item, myResource, null);
		if(displayedResources.contains(myResource))return;
		displayedResources.add(myResource);
		if(myResource instanceof Note){
			Note myNote = (Note)myResource;
			handleNoteContents(item, myNote);
		}
		currentDepth++;
		boolean recurse = currentDepth <= recursionDepth && 
		   (recursiveProcessing || 
				   (lo.getIsOpen() && lo.getShowingMap() &&
						   myResource.getObjectType().getALID() == Note.getNoteObjectType().getALID()));
		if(!recurse) return;
		Vector contents = TwoDOrderer.selectAndOrderContents(myResource.getMyDisplayedItems().getItems(), true);
		if(contents.size() != 0)handleContent(item, contents, currentDepth);
	}

	/*
	private void handleResourceName(XmlElement item, Resource myResource) throws IOException {
		XmlElement headP = new XmlElement("p");
		item.addContent(headP);
/		XmlElement b = new XmlElement("b");
		headP.addContent(b);
		XmlElement nameHandle  = b;
		if(myResource.getIdentifier().startsWith("url:")){
			String url = myResource.getIdentifier().substring(4);
			nameHandle = new XmlElement("a");
			b.addContent(nameHandle);
			nameHandle.addAttribute("href", url);
		}
		nameHandle.addContent(myResource.getName());
		if(myResource.getObjectType().getALID() != Note.getNoteObjectType().getALID()){
			headP.addContent(" ("+myResource.getObjectType().getName()+")");
		}
	}
	*/

	/**
	 * controls how many recursions into the reference areas of contained
	 * resources will be performed (0 means top level only).  Convention
	 * within the program provides a default of 9999 -- which in any
	 * normal context would mean full recursion.
	 * 
	 * @param recursionDepth
	 */
	public void setRecursionDepth(int recursionDepth) {
		this.recursionDepth = recursionDepth;
	}

	/**
	 * controls whether recursion is allowed only when a viewer of the
	 * reference area would actually see the contained items, or not.
	 * 
	 * @param recursiveProcessing
	 */
	
	public void setRecursiveProcessing(boolean recursiveProcessing) {
		this.recursiveProcessing = recursiveProcessing;
	}

}
