/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.views.updatePolicy;

import java.util.ArrayList;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;

// TODO:  Dialog needs F1 help
public class PolicySetDialog extends SelectionDialog implements IUpdatePolicySetListener{

	private ListViewer fPolicySetViewer;
	private Button fNewButton;
	private Text fNameText;
	private TextViewer fDescriptionViewer;
	private CheckboxTableViewer fPoliciesViewer;
	private Text fIdText;
	private boolean fPrompt = false;
	private IUpdatePolicySet fSelectedPolicySet;
	
	private class PolicySetContentProvider implements IStructuredContentProvider
	{
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof IUpdatePolicyManager)
			{
				IUpdatePolicyManager mgr = (IUpdatePolicyManager)inputElement;
				IUpdatePolicySet[] sets = mgr.getAllPolicySets();
				ArrayList retList = new ArrayList();
				
				for (int i=0; i<sets.length; i++)
				{
					if (sets[i].canEdit() || sets[i].canRemove())
						retList.add(sets[i]);
				}
				return retList.toArray();
			}
			else if (inputElement instanceof IUpdatePolicySet)
			{
				return DebugUITools.getUpdatePolicyManager().getAllPolicies();
			}
			return new Object[0];
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	private class PolicySetLabelProvider implements ILabelProvider
	{

		public Image getImage(Object element) {
			return null;
		}

		public String getText(Object element) {
			if (element instanceof IUpdatePolicySet)
			{
				IUpdatePolicySet set = (IUpdatePolicySet)element;
				return set.getName();
			}
			else if (element instanceof IUpdatePolicy)
			{
				IUpdatePolicy policy = (IUpdatePolicy)element;
				StringBuffer buf = new StringBuffer(policy.getName());
				buf.append(" - "); //$NON-NLS-1$
				buf.append(policy.getDescription());
				return buf.toString();
			}
			return null;
		}

		public void addListener(ILabelProviderListener listener) {
			
		}

		public void dispose() {
			
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
			
		}
	}

	protected PolicySetDialog(Shell shell) {
		super(shell);
		super.setShellStyle(super.getShellStyle()|SWT.RESIZE);
		super.setTitle(DebugUIMessages.PolicySetDialog_1);
	}

	protected Control createDialogArea(Composite parent) {
		
		SashForm composite = new SashForm(parent, SWT.HORIZONTAL);
		GridData comositeSpec= new GridData(GridData.FILL_BOTH);
		comositeSpec.grabExcessVerticalSpace= true;
		comositeSpec.grabExcessHorizontalSpace= true;
		comositeSpec.verticalAlignment= GridData.FILL;
		comositeSpec.widthHint = 600;
		comositeSpec.heightHint = 500;
		composite.setLayoutData(comositeSpec);		
		
		createPolicySetPane(composite);
		createPolicyPane(composite);
		composite.setWeights(new int[] {30, 70});
		
		// set initial selection
		setInitialSelection();
		
		return composite;
	}

	private void setInitialSelection() {
		Object elm = fPolicySetViewer.getElementAt(0);
		if (elm != null)
		{
			fPolicySetViewer.setSelection(new StructuredSelection(elm));
		}
		else
		{
			fPolicySetViewer.setSelection(StructuredSelection.EMPTY);
		}
	}

	private void createPolicyPane(Composite composite) {
		Group group = new Group(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		group.setLayout(layout);
		group.setText(DebugUIMessages.PolicySetDialog_2);
		GridData groupData = new GridData(GridData.FILL_BOTH);
		groupData.horizontalSpan = 2;
		group.setLayoutData(groupData);
		
		Label idLabel = new Label(group, SWT.NONE);
		idLabel.setText(DebugUIMessages.PolicySetDialog_3);
		GridData idLabelData = new GridData();
		idLabelData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
		idLabelData.horizontalSpan = 1;
		idLabel.setLayoutData(idLabelData);
		
		fIdText = new Text(group, SWT.BORDER);
		fIdText.setEditable(false);
		GridData idTextData = new GridData(GridData.FILL_HORIZONTAL);
		idTextData.horizontalSpan = 1;
		idTextData.grabExcessHorizontalSpace = true;
		fIdText.setLayoutData(idTextData);
		
		Label nameLabel = new Label(group, SWT.NONE);
		nameLabel.setText(DebugUIMessages.PolicySetDialog_4);
		GridData nameLabelData = new GridData();
		nameLabelData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
		nameLabelData.horizontalSpan = 1;
		nameLabel.setLayoutData(nameLabelData);
		
		fNameText = new Text(group, SWT.BORDER);
		fNameText.setText(""); //$NON-NLS-1$
		GridData nameTextData = new GridData(GridData.FILL_HORIZONTAL);
		nameTextData.horizontalSpan = 1;
		nameTextData.grabExcessHorizontalSpace = true;
		fNameText.setLayoutData(nameTextData);
		
		fNameText.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				fPrompt = true;
				super.keyPressed(e);
			}
		});
		
		Label descriptionLabel = new Label(group, SWT.NONE);
		descriptionLabel.setText(DebugUIMessages.PolicySetDialog_6);
		GridData descriptionLabelData = new GridData(GridData.FILL_HORIZONTAL);
		descriptionLabelData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
		descriptionLabelData.horizontalSpan = 2;
		descriptionLabel.setLayoutData(descriptionLabelData);
		
		fDescriptionViewer = new TextViewer(group, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		fDescriptionViewer.setDocument(new Document());
		GridData descriptionTextData = new GridData(GridData.FILL_HORIZONTAL);
		descriptionTextData.horizontalSpan = 2;
		descriptionTextData.grabExcessHorizontalSpace = true;
		descriptionTextData.heightHint = 50;
		fDescriptionViewer.getControl().setLayoutData(descriptionTextData);
		fDescriptionViewer.getTextWidget().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				fPrompt = true;
				super.keyPressed(e);
			}});
		
		Label policyLabel = new Label(group, SWT.NONE);
		policyLabel.setText(DebugUIMessages.PolicySetDialog_7);
		GridData policyLabelData = new GridData();
		policyLabelData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
		policyLabelData.horizontalSpan = 2;
		policyLabel.setLayoutData(policyLabelData);
		
		fPoliciesViewer =  CheckboxTableViewer.newCheckList(group, SWT.FILL | SWT.BORDER);
		GridData policiesData = new GridData(GridData.FILL_BOTH);
		policiesData.heightHint = 80;
		policiesData.horizontalSpan = 2;
		fPoliciesViewer.getControl().setLayoutData(policiesData);
		fPoliciesViewer.addCheckStateListener(new ICheckStateListener() {

			public void checkStateChanged(CheckStateChangedEvent event) {
				fPrompt = true;
			}});
		
		// TODO:  reuse content provider and label provider
		fPoliciesViewer.setContentProvider(new PolicySetContentProvider());
		fPoliciesViewer.setLabelProvider(new PolicySetLabelProvider());
		
		Label filler = new Label(group, SWT.NONE);
		filler.setText(""); //$NON-NLS-1$
		GridData fillerData = new GridData();
		fillerData.horizontalSpan = 1;
		filler.setLayoutData(fillerData);
		
		Button fApplyButton = new Button(group, SWT.NONE);
		fApplyButton.setText(DebugUIMessages.PolicySetDialog_9);
		GridData applyButtonData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		applyButtonData.horizontalSpan = 1;
		fApplyButton.setLayoutData(applyButtonData);
		fApplyButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				performApply();
				super.widgetSelected(e);
			}
		});
		
		setEditable(true);
	}
	
	private void createPolicySetPane(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout compositeLayout = new GridLayout();
		compositeLayout.numColumns = 2;
		compositeLayout.makeColumnsEqualWidth = false;
		composite.setLayout(compositeLayout);
		
		GridData comositeSpec= new GridData(GridData.FILL_BOTH);
		comositeSpec.grabExcessVerticalSpace= true;
		comositeSpec.grabExcessHorizontalSpace= true;
		comositeSpec.verticalAlignment= GridData.FILL;
		comositeSpec.widthHint = 400;
		composite.setLayoutData(comositeSpec);		
		
		Label label = new Label(composite, SWT.NONE);
		GridData labelData = new GridData();
		labelData.horizontalSpan = 2;
		label.setText(DebugUIMessages.PolicySetDialog_10);
		label.setLayoutData(labelData);
		
		fPolicySetViewer = new ListViewer(composite, SWT.FILL | SWT.BORDER);
		GridData listLayout = new GridData(GridData.FILL_BOTH);
		listLayout.horizontalSpan = 2;
		listLayout.heightHint =140;
		listLayout.widthHint =140;
		fPolicySetViewer.getControl().setLayoutData(listLayout);
		fPolicySetViewer.setContentProvider(new PolicySetContentProvider());
		fPolicySetViewer.setLabelProvider(new PolicySetLabelProvider());
		fPolicySetViewer.setInput(DebugUITools.getUpdatePolicyManager());
		fPolicySetViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = fPolicySetViewer.getSelection();
				if (selection != null && selection instanceof IStructuredSelection)
				{
					IStructuredSelection strucSel = (IStructuredSelection)selection;
					Object elm = strucSel.getFirstElement();
					if (elm == fSelectedPolicySet)
						return;
					
					if (fPrompt)
					{
						promtAndSave();
					}
					
					if (elm instanceof IUpdatePolicySet)
					{
						fSelectedPolicySet = (IUpdatePolicySet)elm;
						populateDetailedFields((IUpdatePolicySet)elm);
					}
					else
					{
						fSelectedPolicySet = null;
						populateDetailedFields(null);
					}
				}
				
			}});

		fNewButton = new Button(composite, SWT.NONE);
		fNewButton.setText(DebugUIMessages.PolicySetDialog_11);
		GridData newButtonData = new GridData(GridData.FILL_HORIZONTAL);
		newButtonData.horizontalSpan = 1;
		fNewButton.setLayoutData(newButtonData);
		fNewButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				createNewPolicySet();
				super.widgetSelected(e);
			}});
		
		Button removeButton = new Button(composite, SWT.NONE);
		removeButton.setText(DebugUIMessages.PolicySetDialog_12);
		GridData removeButtonData = new GridData(GridData.FILL_HORIZONTAL);
		removeButtonData.horizontalSpan = 1;
		removeButton.setLayoutData(removeButtonData);
		removeButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				ISelection selection = fPolicySetViewer.getSelection();
				if (selection != null && selection instanceof IStructuredSelection)
				{
					Object obj = ((IStructuredSelection)selection).getFirstElement();
					if (obj != null && obj instanceof IUpdatePolicySet)
					{
						removePolicySet((IUpdatePolicySet)obj);
					}
				}
				super.widgetSelected(e);
			}});
	}
	
	
	private void performApply() {
		// TODO:  selection in policy set viewer incorrect after perform apply is called.
		Object input = fSelectedPolicySet;
		if (input != null && input instanceof UpdatePolicySet)
		{
			// TODO:  Look at storing references to IUpdatePolicy in a policy set
			// instead of storing the ids of the policies
			// It will simplify this code.
			UpdatePolicySet set = (UpdatePolicySet)input;
			String id = fIdText.getText();
			String name = fNameText.getText();
			String description = fDescriptionViewer.getTextWidget().getText();
			Object[] objs = fPoliciesViewer.getCheckedElements();
			IUpdatePolicy[] policies = new IUpdatePolicy[objs.length];
			System.arraycopy(objs, 0, policies, 0, objs.length);
			
			String[] policyIds = new String[policies.length];
			for (int i=0; i<policyIds.length; i++)
				policyIds[i] = policies[i].getId();
			set.init(id, name, description,policyIds);

			IUpdatePolicySet existing = DebugUITools.getUpdatePolicyManager().getPolicySet(id);
			if (existing != null)
			{
				existing.setName(name);
				existing.setDescription(description);
				existing.setPolicies(policyIds);
				DebugUITools.getUpdatePolicyManager().policySetChanged(existing);
			}
		}
	}
	
	private void setEditable(boolean editable)
	{
		fNameText.setEditable(editable);
		fDescriptionViewer.setEditable(editable);
		fPoliciesViewer.getControl().setEnabled(editable);
	}
	
	private void createNewPolicySet()
	{

		// find policy set id
		String[] identifiers = getUniqueIdAndName();
		String id = identifiers[0];
		
		// find policy set name
		String name = identifiers[1];
		
		// create temp update policy set
		UpdatePolicySet set = new UpdatePolicySet();
		set.init(id, name, "", new String[0]); //$NON-NLS-1$
		fPoliciesViewer.setInput(set);
		fPoliciesViewer.setAllChecked(false);
		
		try {
			DebugUITools.getUpdatePolicyManager().addPolicySet(set);
		} catch (DebugException e) {
			DebugUIPlugin.errorDialog(getParentShell(), DebugUIMessages.PolicySetDialog_14, DebugUIMessages.PolicySetDialog_15, e);
		}
	}
	
	private void removePolicySet(IUpdatePolicySet set)
	{
		if (set != null)
		{
			try {
				DebugUITools.getUpdatePolicyManager().removePolicySet(set);
			} catch (DebugException e) {
				DebugUIPlugin.errorDialog(getParentShell(), DebugUIMessages.PolicySetDialog_16, DebugUIMessages.PolicySetDialog_17, e);
			}
		}
	}
	
	private void populateDetailedFields(IUpdatePolicySet set)
	{
		if (set == null)
		{
			// clear fields
			fIdText.setText(""); //$NON-NLS-1$
			fNameText.setText(""); //$NON-NLS-1$
			fDescriptionViewer.getTextWidget().setText(""); //$NON-NLS-1$
			fPoliciesViewer.setInput(set);
		}
		else
		{
			fIdText.setText(set.getId());
			fNameText.setText(set.getName());
			fDescriptionViewer.getTextWidget().setText(set.getDescription());
			fPoliciesViewer.setInput(set);
			
			String[] ids = set.getPolicies();
			ArrayList policies = new ArrayList();
			for (int i=0; i<ids.length; i++)
			{
				IUpdatePolicy policy = DebugUITools.getUpdatePolicyManager().getPolicy(ids[i]);
				policies.add(policy);
			}
			
			fPoliciesViewer.setCheckedElements(policies.toArray());
		}
	}

	private String[] getUniqueIdAndName() {
		IUpdatePolicySet[] policySets = DebugUITools.getUpdatePolicyManager().getAllPolicySets();
		int cnt = 1;
		String prefix = "org.eclipse.debug.ui.user.policySet."; //$NON-NLS-1$
		String id = prefix + cnt;
		
		String namePrefix = DebugUIMessages.PolicySetDialog_0;
		String name = namePrefix + " (" + cnt + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		
		boolean foundId = false;
		while (!foundId)
		{
			boolean unique = true;
			for (int i=0; i<policySets.length; i++)
			{
				if (policySets[i].getId().equals(id))
				{
					unique = false;
					break;
				}
			}
			
			if (!unique)
			{
				foundId = false;
				cnt ++;
				id = prefix + cnt;
				name = namePrefix + " (" + cnt; //$NON-NLS-1$
				name += ")"; //$NON-NLS-1$
			}
			else
				foundId = true;
		}
		return new String[]{id, name};
	}
	
	public boolean close() {
		DebugUITools.getUpdatePolicyManager().removePolicySetListener(this);
		return super.close();
	}

	public int open() {
		DebugUITools.getUpdatePolicyManager().addPolicySetListener(this);
		return super.open();
	}

	public void policySetAdded(IUpdatePolicySet set) {
		fPolicySetViewer.refresh();
		fPolicySetViewer.setSelection(new StructuredSelection(set));
	}

	public void policySetRemoved(IUpdatePolicySet set) {
		fPolicySetViewer.refresh();
		setInitialSelection();
	}

	public void policySetChanged(IUpdatePolicySet set) {
		fPolicySetViewer.refresh();
	}

	private void promtAndSave() {
		boolean save = MessageDialog.openQuestion(DebugUIPlugin.getShell(), DebugUIMessages.PolicySetDialog_25, DebugUIMessages.PolicySetDialog_26);
		
		if (save)
		{
			performApply();
		}
		fPrompt = false;
	}

	protected void cancelPressed() {

		if (fPrompt)
		{
			promtAndSave();
		}
		super.cancelPressed();
	}

	protected void okPressed() {
		// save info
		if (fPrompt)
			performApply();
		super.okPressed();
	}

}
