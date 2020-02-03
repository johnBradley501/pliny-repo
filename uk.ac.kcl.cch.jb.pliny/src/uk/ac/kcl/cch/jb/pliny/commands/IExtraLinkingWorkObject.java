package uk.ac.kcl.cch.jb.pliny.commands;

import uk.ac.kcl.cch.jb.pliny.model.Link;

public interface IExtraLinkingWorkObject {

	public void run(Link link);
	
	public void undo(Link link);
}
