package uk.ac.kcl.cch.jb.pliny.browser;

import uk.ac.kcl.cch.jb.pliny.actions.CopyPlinyAction;

public class BrowserEditorCopyAction extends CopyPlinyAction {

	public boolean inBrowser = true;
	public BrowserAction browserAction;

	public BrowserEditorCopyAction(BrowserEditor myBrowser){
		super(myBrowser);
		BrowserViewer browser = myBrowser.getWebBrowser();
		browserAction = new BrowserAction(browser, BrowserAction.COPY);
	}

	public void setInBrowser(boolean inBrowser) {
		this.inBrowser = inBrowser;
	}
	
	public void run(){
		if(inBrowser)browserAction.run();
		else super.run();
	}
	
	protected boolean calculateEnabled(){
		if(inBrowser)return browserAction.isEnabled();
		return super.calculateEnabled();
	}

}
