package uk.ac.kcl.cch.jb.pliny.controls;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class ButtonActionLinker extends SelectionAdapter  implements IPropertyChangeListener{
	
	private Button button;
	private IAction action;

	public ButtonActionLinker(Button button, IAction action){
		this.button = button;
		this.action = action;
		doSetup();
	}
	
	public ButtonActionLinker(Composite parent, IAction action){
		button = new Button(parent, SWT.FLAT);
		this.action = action;
		doSetup();
	}
	
	private void doSetup(){
		button.setImage(action.getImageDescriptor().createImage());
		button.setEnabled(action.isEnabled());
		button.setData(action);
		button.setToolTipText(action.getToolTipText());
		button.addSelectionListener(this);
		action.addPropertyChangeListener(this);
	}
	
	public void dispose(){
		button.dispose();
		action.removePropertyChangeListener(this);
	}
	
	public void widgetSelected(SelectionEvent e){
		if(!(e.widget.getData() instanceof Action))return;
		((Action)e.widget.getData()).run();
	}

	@Override
	public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
		String propName = event.getProperty();
		if(propName == Action.ENABLED){
			boolean enabled = ((Boolean)event.getNewValue()).booleanValue();
			button.setEnabled(enabled);
		}
		
	}

}
