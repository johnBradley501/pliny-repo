package uk.ac.kcl.cch.jb.pliny.browser;

import org.eclipse.ui.IWorkbenchPart;

import uk.ac.kcl.cch.jb.pliny.actions.CutPlinyAction;

public class BrowserEditorCutAction extends CutPlinyAction {

	public boolean inBrowser = true;
	public BrowserAction browserAction;

	public BrowserEditorCutAction(BrowserEditor myBrowser){
		super(myBrowser);
		BrowserViewer browser = myBrowser.getWebBrowser();
		browserAction = new BrowserAction(browser, BrowserAction.CUT);
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
