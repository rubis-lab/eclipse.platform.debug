package org.eclipse.ui.externaltools.internal.ant.preferences;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.externaltools.internal.ant.editor.text.PlantyColorConstants;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IPreferenceConstants;

public class AntPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private StringFieldEditor fBuildFileNames;
	
	private List fAppearanceColorList;
	private ColorEditor fAppearanceColorEditor;
	
	private final String[][] fAppearanceColorListModel= new String[][] {
		{AntPreferencesMessages.getString("AntPreferencePage.&Error__2"), IPreferenceConstants.CONSOLE_ERROR_RGB}, //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntPreferencePage.&Warning__3"), IPreferenceConstants.CONSOLE_WARNING_RGB}, //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntPreferencePage.I&nformation__4"), IPreferenceConstants.CONSOLE_INFO_RGB}, //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntPreferencePage.Ve&rbose__5"), IPreferenceConstants.CONSOLE_VERBOSE_RGB}, //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntPreferencePage.Deb&ug__6"), IPreferenceConstants.CONSOLE_DEBUG_RGB}, //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntPreferencePage.Ant_editor_text_1"), PlantyColorConstants.P_DEFAULT}, //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntPreferencePage.Ant_editor_processing_instuctions_2"),  PlantyColorConstants.P_PROC_INSTR}, //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntPreferencePage.Ant_editor_constant_strings_3"),  PlantyColorConstants.P_STRING},  //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntPreferencePage.Ant_editor_tags_4"),    PlantyColorConstants.P_TAG},  //$NON-NLS-1$
		{AntPreferencesMessages.getString("AntPreferencePage.Ant_editor_comments_5"), PlantyColorConstants.P_XML_COMMENT} //$NON-NLS-1$
	};
	
	/**
 	 * Create the Ant page.
     */
		public AntPreferencePage() {
			super(GRID);
			setDescription(AntPreferencesMessages.getString("AntPreferencePage.General")); //$NON-NLS-1$
			setPreferenceStore(ExternalToolsPlugin.getDefault().getPreferenceStore());
		}
	
	/**
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {

		Label label= new Label(getFieldEditorParent(), SWT.NONE);
		label.setText(AntPreferencesMessages.getString("AntPreferencePage.Enter")); //$NON-NLS-1$
		GridData gd= new GridData();
		gd.horizontalSpan= 2;
		label.setLayoutData(gd);
		label.setFont(getFieldEditorParent().getFont());
		
		fBuildFileNames = new StringFieldEditor(IPreferenceConstants.ANT_FIND_BUILD_FILE_NAMES, AntPreferencesMessages.getString("AntPreferencePage.&Names__3"), getFieldEditorParent()); //$NON-NLS-1$
		addField(fBuildFileNames);
		fBuildFileNames.getTextControl(getFieldEditorParent()).addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {

			}
		});
		
		new Label(getFieldEditorParent(), SWT.NONE);
		createColorComposite();
	}
	
	private void createColorComposite() {
		
		Label l= new Label(getFieldEditorParent(), SWT.LEFT);
		l.setText(AntPreferencesMessages.getString("AntPreferencePage.Ant_Color_Options__6"));  //$NON-NLS-1$
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		l.setLayoutData(gd);
				
		Composite editorComposite= new Composite(getFieldEditorParent(), SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		editorComposite.setLayout(layout);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		gd.horizontalSpan= 2;
		editorComposite.setLayoutData(gd);		

		fAppearanceColorList= new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		gd= new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		gd.heightHint= convertHeightInCharsToPixels(11);
		fAppearanceColorList.setLayoutData(gd);
				
		Composite stylesComposite= new Composite(editorComposite, SWT.NONE);
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		l= new Label(stylesComposite, SWT.LEFT);
		l.setText(AntPreferencesMessages.getString("AntPreferencePage.Color__7"));  //$NON-NLS-1$
		gd= new GridData();
		gd.horizontalAlignment= GridData.BEGINNING;
		l.setLayoutData(gd);

		fAppearanceColorEditor= new ColorEditor(stylesComposite);
		Button foregroundColorButton= fAppearanceColorEditor.getButton();
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);

		fAppearanceColorList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleAppearanceColorListSelection();
			}
		});
		foregroundColorButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int i= fAppearanceColorList.getSelectionIndex();
				String key= fAppearanceColorListModel[i][1];
		
				PreferenceConverter.setValue(getPreferenceStore(), key, fAppearanceColorEditor.getColorValue());
			}
		});
	}
	
	private void handleAppearanceColorListSelection() {	
		int i= fAppearanceColorList.getSelectionIndex();
		String key= fAppearanceColorListModel[i][1];
		RGB rgb= PreferenceConverter.getColor(getPreferenceStore(), key);
		fAppearanceColorEditor.setColorValue(rgb);		
	}
	
	/**
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		//TODO: help context id
		//WorkbenchHelp.setHelp(parent, IAntHelpContextIds.ANT_PREFERENCE_PAGE);
	}

	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	/**
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#initialize()
	 */
	protected void initialize() {
		super.initialize();
		for (int i= 0; i < fAppearanceColorListModel.length; i++) {
			fAppearanceColorList.add(fAppearanceColorListModel[i][0]);
		}
		fAppearanceColorList.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (fAppearanceColorList != null && !fAppearanceColorList.isDisposed()) {
					fAppearanceColorList.select(0);
					handleAppearanceColorListSelection();
				}
			}
		});
	}
}