package uk.ac.kcl.cch.jb.pliny.actions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;

import uk.ac.kcl.cch.jb.pliny.model.Note;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.utils.TwoDOrderer;
import uk.ac.kcl.cch.jb.xmlWriter.XmlElement;
import uk.ac.kcl.cch.jb.xmlWriter.XmlFile;

public abstract class TextFileGeneratorBase implements ITextFileGenerator{
	protected Resource headResource;
	protected String fileName;
	protected IProgressMonitor monitor;
	protected boolean makeXHTML = false;
	
	public TextFileGeneratorBase(Resource headResource, String fileName) {
		this.fileName = fileName;
		this.headResource = headResource;
	}
	
	protected abstract Vector getTopItems();

	public void run(IProgressMonitor monitor) throws FileNotFoundException, IOException{
		this.monitor = monitor;
		if(fileName == null){
			monitor.done();
			return;
		}
		Vector topItems = getTopItems();
		monitor.beginTask("Creating text file of the contents", topItems.size());
		XmlFile xmlFile = new XmlFile(fileName);
		if(makeXHTML){
			xmlFile.setPublicIdentifier("-//W3C//DTD XHTML 1.0 Transitional//EN");
			xmlFile.setSystemIdentifier("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
			
		} else xmlFile.setIncludeXmlDeclaration(false);
		XmlElement htmlHead = new XmlElement("html");
		xmlFile.setDocumentElement(htmlHead);
		if(makeXHTML)htmlHead.addAttribute("xmlns","http://www.w3.org/1999/xhtml");
		
		createHead(htmlHead);
		
		createBody(htmlHead, topItems);
		
		xmlFile.close();
	}

	private void createHead(XmlElement htmlHead) throws IOException{
		XmlElement head = new XmlElement("head");
		htmlHead.addContent(head);
		XmlElement title = new XmlElement("title");
		head.addContent(title);
		title.addContent(headResource.getName());
	}

	protected abstract void createBody(XmlElement htmlHead, Vector topItems) throws IOException;

	
	/**
	 * controls whether or not the generated HTML will be identified
	 * (with a DOCTYPE and a namespace declaration as XHTML (Transitional).
	 * 
	 * @param makeXHTML
	 */

	public void setMakeXHTML(boolean makeXHTML) {
		this.makeXHTML = makeXHTML;
	}

	protected void handleNoteContents(XmlElement item, Note myNote) throws IOException {
		String noteText = myNote.getContent().trim();
		if(noteText.length() == 0)return;
		noteText = noteText.replaceAll("\\r", "");
		String[] chunks = noteText.split("\\n\\w*\\n");
		for(int i = 0; i < chunks.length; i++){
			String chunk = chunks[i];
			String[] lines = chunk.split("\\n");
			XmlElement p = new XmlElement("p");
			item.addContent(p);
			for(int j = 0; j < lines.length; j++){
				if(j > 0){
					XmlElement br = new XmlElement("br");
					p.addContent(br);
				}
				p.addContent(lines[j]);
			}
			//for(int j = 0; j < lines.length;j++){
			//	if(j > 0){
			//		XmlElement br = new XmlElement("br");
			//		p.addContent(br);
			//	}
			//	p.addContent(lines[j]);
			//}
		}
	}
	
	protected void handleResourceName(XmlElement item, Resource myResource, String header) throws IOException {
		XmlElement headP;
		if(header == null || header.length() == 0)headP = new XmlElement("p");
		else headP = new XmlElement(header);
		item.addContent(headP);
		XmlElement nameHandle = headP;

		if(header == null || header.length() == 0){
			XmlElement b = new XmlElement("b");
			headP.addContent(b);
			nameHandle  = b;
		}
		if(myResource.getIdentifier().startsWith("url:")){
			String url = myResource.getIdentifier().substring(4);
			XmlElement a = new XmlElement("a");
			nameHandle.addContent(a);
			a.addAttribute("href", url);
			nameHandle = a;
		}
		nameHandle.addContent(myResource.getName());
		if(myResource.getObjectType().getALID() != Note.getNoteObjectType().getALID()){
			headP.addContent(" ("+myResource.getObjectType().getName()+")");
		}
	}

}
