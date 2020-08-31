package uk.ac.kcl.cch.jb.pliny.editors;

import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.DeleteRetargetAction;
import org.eclipse.gef.ui.actions.RedoRetargetAction;
import org.eclipse.gef.ui.actions.UndoRetargetAction;
import org.eclipse.ui.actions.ActionFactory;

/**
 * a generic, rudamentory ActionBarContributor for the Pliny-associated editors (built upon GEF}.
 * Sets up cut-copy-paste.
 * 
 * @author John Bradley
 */

public class PlinyGenericActionBarContributor extends ActionBarContributor {

	@Override
	protected void buildActions() {
		addRetargetAction(new DeleteRetargetAction());
		addRetargetAction(new UndoRetargetAction());
		addRetargetAction(new RedoRetargetAction());

		addGlobalActionKey(ActionFactory.CUT.getId());
		addGlobalActionKey(ActionFactory.COPY.getId());
		addGlobalActionKey(ActionFactory.PASTE.getId());
	}

	@Override
	protected void declareGlobalActionKeys() {
		// nothing needed here

	}

}
