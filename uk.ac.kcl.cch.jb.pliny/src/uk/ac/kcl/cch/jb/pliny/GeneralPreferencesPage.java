package uk.ac.kcl.cch.jb.pliny;

import java.io.IOException;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class GeneralPreferencesPage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public static final String AUTO_LOGIN = "prefs_auto_login";

	private ScopedPreferenceStore preferences;

	public GeneralPreferencesPage() {
		super(GRID);
		this.preferences = new ScopedPreferenceStore(new ConfigurationScope(),
				PlinyPlugin.PLUGIN_ID);
		setPreferenceStore(preferences);
	}

	public void init(IWorkbench workbench) {
	}

	protected void createFieldEditors() {
		BooleanFieldEditor boolEditor = new BooleanFieldEditor(AUTO_LOGIN,
				"Login automatically at startup", getFieldEditorParent());
		addField(boolEditor);
	}

	public boolean performOk() {
		try {
			preferences.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return super.performOk();
	}

}
