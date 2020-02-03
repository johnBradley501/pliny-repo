package uk.ac.kcl.cch.jb.pliny.browser;

import uk.ac.kcl.cch.jb.pliny.actions.PastePlinyAction;

public class BrowserEditorPasteAction extends PastePlinyAction {

	public boolean inBrowser = true;
	public BrowserAction browserAction;

	public BrowserEditorPasteAction(BrowserEditor myBrowser){
		super(myBrowser);
		BrowserViewer browser = myBrowser.getWebBrowser();
		browserAction = new BrowserAction(browser, BrowserAction.PASTE);
	}

	public void setInBrowser(boolean inBrowser) {
		this.inBrowser = inBrowser;
	}
	
	public void run(){
		if(inBrowser)browserAction.run();
		else super.run();
	}

}
