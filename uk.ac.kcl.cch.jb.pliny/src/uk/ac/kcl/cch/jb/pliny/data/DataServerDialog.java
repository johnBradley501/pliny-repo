package uk.ac.kcl.cch.jb.pliny.data;

import java.util.HashMap;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;

import uk.ac.kcl.cch.jb.pliny.GeneralPreferencesPage;
import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;

public class DataServerDialog extends Dialog {
   
   private Text userIdText;
   private Text passwordText;
   private Text serverURL;

   private String user = null;
   private String server = null;
   private String password = null;
   private String storageType = null;
   private boolean autologin = false;

   private HashMap savedDetails = new HashMap();
   private Image icon = null;
	
   public DataServerDialog(Shell parentShell){
	   super(parentShell);
	   loadData();
   }

   private void loadData(){
	   Preferences preferences = ConfigurationScope.INSTANCE.getNode(PlinyPlugin.PLUGIN_ID);
	   user = preferences.get(DataServerSetupManager.USER, "");
	   server = preferences.get(DataServerSetupManager.SERVER,"");
	   password = preferences.get(DataServerSetupManager.PASSWORD,"");
	   storageType = preferences.get(DataServerSetupManager.STORAGE_TYPE, DataServerSetupManager.LOCAL_STORAGE);
	   
	   IPreferencesService service = Platform.getPreferencesService();
	   autologin = service.getBoolean(PlinyPlugin.PLUGIN_ID,
				GeneralPreferencesPage.AUTO_LOGIN, true, null);

   }
   
   protected void configureShell(Shell newShell){
	   super.configureShell(newShell);
	   newShell.setText("Pliny Data Connection");
	   if(icon == null){
		   icon = PlinyPlugin.getImageDescriptor("icons/pliny-icon16.gif").createImage();
	   }
	   newShell.setImage(icon);
   }
   
   public boolean close(){
	   if(icon != null) icon.dispose();
	   icon = null;
	   return super.close();
   }
   
   
   
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);
		
		// data storage type

		Label accountLabel = new Label(composite, SWT.NONE);
		accountLabel.setText("Data storage:");
		accountLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER,
				false, false));
		
		Composite dataStorageComposite = new Composite(composite, SWT.NONE);
		dataStorageComposite.setLayout(new FillLayout());
		dataStorageComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
		
		Button localStorageButton = new Button(dataStorageComposite, SWT.RADIO);
		localStorageButton.setSelection(storageType.equals(DataServerSetupManager.LOCAL_STORAGE));
		localStorageButton.setText("Local");
		localStorageButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				storageType = DataServerSetupManager.LOCAL_STORAGE;
				userIdText.setEnabled(false);
				passwordText.setEnabled(false);
				serverURL.setEnabled(false);
			}
		});

		Button cloudStorageButton = new Button(dataStorageComposite, SWT.RADIO);
		cloudStorageButton.setSelection(storageType.equals(DataServerSetupManager.CLOUD_STORAGE));
		cloudStorageButton.setText("Cloud");
		cloudStorageButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				storageType = DataServerSetupManager.CLOUD_STORAGE;
				userIdText.setEnabled(true);
				passwordText.setEnabled(true);
				serverURL.setEnabled(true);
			}
		});

		// userID
		
		Label userIdLabel = new Label(composite, SWT.NONE);
		userIdLabel.setText("&User ID:");
		userIdLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER,
				false, false));

		userIdText = new Text(composite, SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true,
				false);
		//gridData.widthHint = convertHeightInCharsToPixels(20);
		userIdText.setLayoutData(gridData);
		userIdText.setText(user);

		// server
		
		Label serverLabel = new Label(composite, SWT.NONE);
		serverLabel.setText("&Server:");
		serverLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER,
				false, false));

		serverURL = new Text(composite, SWT.BORDER);
		serverURL.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
				true, false));
		serverURL.setText(server);
		
		// password

		Label passwordLabel = new Label(composite, SWT.NONE);
		passwordLabel.setText("&Password:");
		passwordLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER,
				false, false));

		passwordText = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		passwordText.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
				true, false));
		passwordText.setText(password);

		final Button autoLoginButton = new Button(composite, SWT.CHECK);
		autoLoginButton.setText("Connect &automatically at startup");
		autoLoginButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true,
				true, 2, 1));
		autoLoginButton.setSelection(autologin);
		autoLoginButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//IEclipsePreferences prefs = new ConfigurationScope()
				//		.getNode(PlinyPlugin.PLUGIN_ID);
				//prefs.putBoolean(GeneralPreferencesPage.AUTO_LOGIN, autoLogin
				//		.getSelection());
				autologin = autoLoginButton.getSelection();
			}
		});
		//IPreferencesService service = Platform.getPreferencesService();
		//boolean auto_login = service.getBoolean(PlinyPlugin.PLUGIN_ID,
		//		GeneralPreferencesPage.AUTO_LOGIN, true, null);

		//autoLoginButton.setSelection(auto_login);
		
		if(storageType.equals(DataServerSetupManager.LOCAL_STORAGE)){
			userIdText.setEnabled(false);
			passwordText.setEnabled(false);
			serverURL.setEnabled(false);
		}
		
		return composite;
	}
	
	public void create(){
		super.create();
		getButton(IDialogConstants.CANCEL_ID).setEnabled(false); // user cannot cancel this.    .jb
	}
	
	protected void buttonPressed(int buttonId){
		Preferences preferences = new ConfigurationScope().getNode(PlinyPlugin.PLUGIN_ID);
		if(storageType.equals(DataServerSetupManager.CLOUD_STORAGE)){
			user = userIdText.getText().trim();
			server = serverURL.getText().trim();
			password = passwordText.getText().trim();
			if(user.length()==0 || server.length()==0 || password.length()==0){
				MessageBox message = new MessageBox(this.getShell(), SWT.OK | SWT.ICON_ERROR);
				message.setMessage("To use cloud storage a User ID, server and password must be provided.");
				message.open();
				return;
			}
			preferences.put(DataServerSetupManager.USER, user);
			preferences.put(DataServerSetupManager.SERVER, server);
			preferences.put(DataServerSetupManager.PASSWORD, password);
		}
		preferences.put(DataServerSetupManager.STORAGE_TYPE, storageType);
		IEclipsePreferences prefs = new ConfigurationScope()
				.getNode(PlinyPlugin.PLUGIN_ID);
		prefs.putBoolean(GeneralPreferencesPage.AUTO_LOGIN, autologin);
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}

		super.buttonPressed(buttonId);
	}

}
