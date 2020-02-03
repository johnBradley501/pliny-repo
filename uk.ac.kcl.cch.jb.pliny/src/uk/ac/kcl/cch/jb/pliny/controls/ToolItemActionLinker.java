package uk.ac.kcl.cch.jb.pliny.controls;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class ToolItemActionLinker extends SelectionAdapter implements
		IPropertyChangeListener {

	private IAction action;
	private ToolItem item;
	
	public ToolItemActionLinker(ToolItem item, IAction action){
		this.item = item;
		this.action = action;
		doSetup();
	}
	
	public ToolItemActionLinker(ToolBar bar, IAction action){
		this.item = new ToolItem(bar, SWT.PUSH);
		this.action = action;
		doSetup();
	}

	private void doSetup() {
		item.setImage(action.getImageDescriptor().createImage());
		item.setEnabled(action.isEnabled());
		item.setData(action);
		item.setToolTipText(action.getToolTipText());
		item.addSelectionListener(this);
		action.addPropertyChangeListener(this);
	}

	public void dispose(){
		item.dispose();
		action.removePropertyChangeListener(this);
	}
	
	public void widgetSelected(SelectionEvent e){
		if(!(e.widget.getData() instanceof Action))return;
		((Action)e.widget.getData()).run();
	}

	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String propName = event.getProperty();
		if(propName == Action.ENABLED){
			boolean enabled = ((Boolean)event.getNewValue()).booleanValue();
			item.setEnabled(enabled);
		}
	}

}
