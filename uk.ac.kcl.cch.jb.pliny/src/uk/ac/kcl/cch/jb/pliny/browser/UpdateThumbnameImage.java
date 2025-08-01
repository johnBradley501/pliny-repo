package uk.ac.kcl.cch.jb.pliny.browser;

import java.net.URL;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchPage;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

public class UpdateThumbnameImage implements IBrowserToolbarContribution {

	
	public UpdateThumbnameImage() {
		super();
	}
	
	@Override
	public void setupToolItem(ToolItem item) {
		item.setImage(PlinyPlugin.getImageDescriptor("icons/thumb.png").createImage());
		item.setToolTipText("Refresh thumbnail image");
	}

	@Override
	public void setUrl(URL url) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run(IWorkbenchPage page, BrowserViewer browserViewer) {
		Resource r = browserViewer.getBrowserEditor().getMyResource();
		ImageDescriptor id = null;
		boolean updated = true;
		if(r instanceof VirtualBrowserResource) {
			VirtualBrowserResource vbr = (VirtualBrowserResource)r;
			if(vbr.isNotPersisting()) {
				vbr.makeMeReal();
				id = vbr.getMyThumbnailDescriptor();
				updated = false;
			} else {
				id = vbr.makeThumbnail(browserViewer.getBrowser());
			}
			
		}
		Shell shell = Display.getCurrent().getActiveShell();
		if((id != null) && updated)
			MessageDialog.openInformation(shell, "Updating Thumbnail",  "The thumbnail has been updated.");
		else if(id != null)
			MessageDialog.openInformation(shell, "Updating Thumbnail",  "The thumbnail has been created.");
		else MessageDialog.openInformation(shell,  "Updating Thumbnail", "The update could not be carried out.");

	}

}
