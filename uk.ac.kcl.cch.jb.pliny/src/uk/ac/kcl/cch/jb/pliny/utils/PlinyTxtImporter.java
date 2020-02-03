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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.views.utils.TextImporterWizard;

/**
 * the importer to take a plain text file delineated into notes
 * and create a set of Pliny 
 * {@link uk.ac.kcl.cch.jb.pliny.model.NoteLucened NoteLucened} objects from them.
 * 
 * @author John Bradley
 *
 */
public class PlinyTxtImporter implements IPlinyImporter {
	
	private String fileName;
	private String noteStartID = "=";
	private String referentStartID="==";
	private int status;
	private int noteNumber = 0;
	private int referedNoteNumber = 0;
	private int favouriteCount = 0;
	
	private static final int START_UP = 0;
	private static final int IN_NOTE = 1;
	private static final int IN_REFERENT = 2;
	
	private String title = "";
	private String body = "";
	private NoteLucened referent = null;
	private Resource receivingFavourite = null;

	public PlinyTxtImporter(String fileName) {
		this.fileName = fileName;
	}
	
	public String getFileName(){
		return fileName;
	}
	
	public void setReceivingFavourite(Resource resource){
		receivingFavourite = resource;
	}
	
	public void setNoteStartID(String string){
		noteStartID = string;
	}
	
	public void setReferentStartID(String string){
		referentStartID = string;
	}

	public void run(IProgressMonitor monitor) throws PlinyImportException {
		if(fileName == null){
			monitor.done();
			return;
		}
		monitor.beginTask("Importing data", IProgressMonitor.UNKNOWN);
		LineNumberReader reader = null;
		try {
			FileReader file = new FileReader(fileName);
			reader = new LineNumberReader(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new PlinyImportException("File could not be found: "+fileName);
		}
		String line;
		status = START_UP;
		noteNumber = 0;
		try {
			line = reader.readLine();
			while(line != null){
				switch(status){
				case START_UP: doStartUp(line); break;
				case IN_NOTE: doInNote(line); break;
				case IN_REFERENT: doInReferent(line); break;
				}
				line = reader.readLine();
			}
			if(status == IN_NOTE)finishNote();
			else if(status == IN_REFERENT)finishReferent();
		} catch (IOException e) {
			e.printStackTrace();
			throw new PlinyImportException("There was a problem reading your input file: "+
					e.getLocalizedMessage());
		}
        monitor.done();
	}

	private void doInReferent(String line) {
		String trimmedLine = line.trim();
		if(body.length() == 0 && trimmedLine.length() == 0)return;
		if(trimmedLine.startsWith(referentStartID)){
			finishReferent();
			status = IN_REFERENT;
			startReferent(trimmedLine.substring(referentStartID.length()));
		} else if(trimmedLine.startsWith(noteStartID)){
			finishReferent();
			status = IN_NOTE;
			startNote(trimmedLine.substring(noteStartID.length()));
		} else if(body.length() > 0)
		   body += "\n"+line;
		else body = line;
	}
	
	private void finishReferent() {
		body = body.trim();
		if((body.length() == 0) && (title.length() == 0)){
			referent = null;
			return;
		}
		NoteLucened newNote = new NoteLucened(true);
		newNote.setName(title);
		newNote.setContent(body);
		referent = newNote;
		newNote.reIntroduceMe();
		newNote.setObjectType(NoteLucened.getNoteObjectType());
		if(receivingFavourite != null){
			//buildLinkableObject(receivingFavourite, newNote, 1);
			buildLinkableObject(newNote, receivingFavourite, ++favouriteCount);
		}
		body = "";
		referedNoteNumber = 0;
	}

	private void buildLinkableObject(Resource from, Resource to, int count){
		int offset = (count-1)*18;
		LinkableObject lo = new LinkableObject(true);
		//lo.setDisplayRectangle(new Rectangle(offset,offset,200,150));
		lo.setDisplayRectangle(new Rectangle(0,offset,200,150));
		lo.setShowingMap(false);
		lo.setIsOpen(true);
		//lo.setResourceKeys(from.getALID(), to.getALID());
		lo.reIntroduceMe();
		lo.setLoType(LOType.getCurrentType());
		lo.setSurrogateFor(from);
		lo.setDisplayedIn(to);
	}

	private void finishNote() {
		body = body.trim();
		if((body.length() == 0) && (title.length() == 0))return;
		NoteLucened newNote = new NoteLucened(true);
		newNote.setName(title);
		newNote.setContent(body);
		newNote.reIntroduceMe();
		newNote.setObjectType(NoteLucened.getNoteObjectType());
		if(referent != null){
			buildLinkableObject(newNote, referent, referedNoteNumber);
			//buildLinkableObject(referee, newNote,1);
		} else if(receivingFavourite != null){
			//buildLinkableObject(receivingFavourite, newNote, 1);
			buildLinkableObject(newNote, receivingFavourite, ++favouriteCount);
		}
		body = "";
	}

	private void doInNote(String line) {
		String trimmedLine = line.trim();
		if(body.length() == 0 && trimmedLine.length() == 0)return;
		if(trimmedLine.startsWith(referentStartID)){
			finishNote();
			status = IN_REFERENT;
			startReferent(trimmedLine.substring(referentStartID.length()));
		} else if(trimmedLine.startsWith(noteStartID)){
			finishNote();
			status = IN_NOTE;
			startNote(trimmedLine.substring(noteStartID.length()));
		} else if(body.length() > 0)
		   body += "\n"+line;
		else body = line;
	}

	private void doStartUp(String line) throws PlinyImportException {
		String trimmedLine = line.trim();
		if(trimmedLine.length() == 0)return;
		if(trimmedLine.startsWith(referentStartID)){
			status = IN_REFERENT;
			startReferent(trimmedLine.substring(referentStartID.length()));
		} else if(trimmedLine.startsWith(noteStartID)){
			status = IN_NOTE;
			startNote(trimmedLine.substring(noteStartID.length()));
		}
		else throw new PlinyImportException("Your input file did not start with a note ('"+
				noteStartID+"')or referee ('"+referentStartID+
				"')indicator but with '"+trimmedLine+"' instead.");
	}

	private void startNote(String string) {
		noteNumber += 1;
		String trimmedTitle = string.trim();
		if(trimmedTitle.length() == 0){
			if(referent == null)title = "New Note: "+noteNumber;
			else title = referent.getName()+": Note "+(++referedNoteNumber);
		} else title = trimmedTitle;
		body = "";
	}

	private void startReferent(String string) {
		noteNumber += 1;
		String trimmedTitle = string.trim();
		if(trimmedTitle.length() == 0)title = "New Note: "+noteNumber;
		else title = trimmedTitle;
		body = "";
		referedNoteNumber = 0;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;

	}
	
	public boolean getOptions(Shell parentShell){
		TextImporterWizard wizard = new TextImporterWizard(this);
		WizardDialog dialog =
			new WizardDialog(parentShell, wizard);
		dialog.open();
		return wizard.getDoImport();
	}

}
