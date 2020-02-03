package uk.ac.kcl.cch.jb.pliny.utils;

import java.net.MalformedURLException;
import java.net.URL;

import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

public class StringToNoteHandler {
	private static StringToNoteHandler instance = null;
	
	private StringToNoteHandler(){
		
	}
	
	public static StringToNoteHandler getInstance(){
		if(instance == null)
			instance = new StringToNoteHandler();
		return instance;
	}
	
	public Resource handleStringAsNote(String input){
		input = input.trim();
		int newLine = input.indexOf('\n');
		String title = "New Note";
		// code added Feb 2010 to make short texts into a Pliny note
		// with the dropped text as title, and no content.  j.b.
		if(input.length() <= 60 && !input.contains("\n")){
			title = input;
			input = "";
		}
		else if((newLine >5) && (newLine < 60)){
			title = input.substring(0, newLine).trim();
			//input = input.substring(newLine+1,input.length()-1).trim();
			input = input.substring(newLine+1).trim();
		}
		NoteLucened theNote = null;
		theNote = new NoteLucened(true);
		theNote.setName(title);
		theNote.setContent(input);
		theNote.reIntroduceMe();
		theNote.setObjectType(NoteLucened.getNoteObjectType());
		return theNote;
	}

}

