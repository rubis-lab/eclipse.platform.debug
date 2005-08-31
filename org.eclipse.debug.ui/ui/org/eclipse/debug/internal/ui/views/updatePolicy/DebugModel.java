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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

public class DebugModel extends PlatformObject implements IPersistableElement, IUpdatePolicySetListener{

	private static final IUpdatePolicyHandler[] EMPTY = new IUpdatePolicyHandler[0];
	public static final String ATTR_MODEL_ID = "modelId"; //$NON-NLS-1$
	public static final String ATTR_ACTIVE_POLICY_SET = "activePolicySet"; //$NON-NLS-1$
	
	private Set fPolicyHandlers = new HashSet();
	private String fModelIdentifier;
	private IDebugViewExtension fView;
	private IUpdatePolicySet fActivePolicySet;
	private boolean fIsActive = false;
	private IPropertyChangeListener fListener;
	private IDebugElement fDebugContext;
	
	/**
	 * Constructs using a memento
	 * @param memento
	 */
	public DebugModel(IMemento memento, IDebugViewExtension view)
	{
		fView = view;
		
		String modelId = memento.getString(DebugModel.ATTR_MODEL_ID);
		fModelIdentifier = modelId;
				
		String activePolicySet = memento.getString(DebugModel.ATTR_ACTIVE_POLICY_SET);
		if (activePolicySet != null)
		{
			IUpdatePolicySet set = DebugUITools.getUpdatePolicyManager().getPolicySet(activePolicySet);
			fActivePolicySet = set;
			// do not activate handlers unless the model is activated
		}
		addListeners();
	}
	
	public DebugModel(String modelIdentifier, IDebugViewExtension view)
	{
		fModelIdentifier = modelIdentifier;
		fView = view;
		addListeners();
	}
	
	private void addListeners() {
		fListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (fIsActive && fActivePolicySet != null) {
					if (event.getProperty().equals(IDebugViewExtension.PROPERTY_UPDATE_POLICY)) {
						Object obj = event.getNewValue();
						if (obj instanceof String) {
							String newId = (String) obj;
							if (!newId.equals(fActivePolicySet.getId())) {
								IUpdatePolicySet set = DebugUITools.getUpdatePolicyManager().getPolicySet(newId);
								if (set != null) {
									unloadHandlers();
									loadHandlers(set);
								} else {
									DebugUIPlugin.logErrorMessage("Unable to load policy set: " + newId); //$NON-NLS-1$
								}
							}
						}
					}
				}
			}
		};
			
		fView.addListener(fListener);
	}
	
	private void removeListeners()
	{
		fView.removeListener(fListener);
	}
	
	public String getModelIdentifier()
	{
		return fModelIdentifier;
	}
	
	public void setModelIdentifier(String modelId)
	{
		fModelIdentifier = modelId;
	}
	
	public void addPolicyHandlers(IUpdatePolicyHandler[] handlers) {
		for (int i=0; i<handlers.length; i++)
			fPolicyHandlers.add(handlers[i]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.handlers.IDebugViewExtension#gethandlers(org.eclipse.debug.core.model.IDebugElement)
	 */
	public IUpdatePolicyHandler[] getPolicyHandlers() {
		if ( fPolicyHandlers.isEmpty())
			return EMPTY;
		
		return (IUpdatePolicyHandler[])fPolicyHandlers.toArray(new IUpdatePolicyHandler[fPolicyHandlers.size()]);
	}

	public void removePolicyHandlers(IUpdatePolicyHandler[] handlers) {
		for (int i=0; i<handlers.length; i++)
			fPolicyHandlers.remove(handlers[i]);
	}

	public void activateModel(IDebugElement debugContext) {
		
		fIsActive = true;
		fDebugContext = debugContext;
		if (fPolicyHandlers.isEmpty())
		{
			IUpdatePolicySet set = null;
			if (fActivePolicySet == null)
				set = DebugUITools.getUpdatePolicyManager().getPrimaryPolicySet(fView.getSite().getId(), fModelIdentifier);
			else
				set = fActivePolicySet;
			loadHandlers(set);
		}
		DebugUITools.getUpdatePolicyManager().addPolicySetListener(this);
		if (debugContext != null)
		{
			if (debugContext.getModelIdentifier().equals(getModelIdentifier()))
			{
				// set input to handlers
				IUpdatePolicyHandler[] handlers = getPolicyHandlers();
				for (int i=0; i<handlers.length; i++)
				{
					if (handlers[i].getDebugContext() != debugContext)
						handlers[i].setDebugContext(debugContext);
				}
				return;
			}
		}
	}
	
	public void deactivateModel()
	{
		fIsActive = false;
		unloadHandlers();
		DebugUITools.getUpdatePolicyManager().removePolicySetListener(this);
	}
	
	public void dispose()
	{
		removeListeners();
		DebugUITools.getUpdatePolicyManager().removePolicySetListener(this);
		unloadHandlers();
		fPolicyHandlers = null;
	}
	
	public void loadHandlers(IUpdatePolicySet policySet)
	{
		// when input is changed, load handler
		if (policySet != null)
		{
			try {
				fActivePolicySet = policySet;
				String [] policyIds = policySet.getPolicies();
				
				for (int j=0; j<policyIds.length; j++)
				{
					IUpdatePolicy policy = DebugUITools.getUpdatePolicyManager().getPolicy(policyIds[j]);
					if (policy != null)
					{
						IUpdatePolicyHandler handler = policy.createHandler();
						
						if (handler != null)
						{
							handler.init(policy, fView);
						}		
						fPolicyHandlers.add(handler);
					}
				}
			} catch (CoreException e) {
				// log error
				DebugUIPlugin.log(e);
			}
		}
		else
		{	
			try {
				IUpdatePolicy[] policies = fView.getDefaultUpdatePolicies();
				for (int i=0; i<policies.length; i++)
				{
					IUpdatePolicyHandler handler;
					handler = policies[i].createHandler();
					handler.init(policies[i], fView);
					fPolicyHandlers.add(handler);
				}
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		}		
	}
	
	public void unloadHandlers()
	{
		// dispose all handlers
		IUpdatePolicyHandler[] handlers = getPolicyHandlers();
		for (int i=0; i<handlers.length; i++)
		{
			handlers[i].dispose();
			fPolicyHandlers.remove(handlers[i]);
		}
	}
	
	public IUpdatePolicySet[] getPolicySets()
	{
		return DebugUITools.getUpdatePolicyManager().getPolicySets(fView.getSite().getId(), fModelIdentifier);
	}
	
	public IUpdatePolicySet getActivePolicySet() 
	{
		return fActivePolicySet;
	}

	public void saveState(IMemento memento) {
		memento.putString(ATTR_MODEL_ID, fModelIdentifier);
		
		if (fActivePolicySet != null)
			memento.putString(ATTR_ACTIVE_POLICY_SET, fActivePolicySet.getId());
	}

	public String getFactoryId() {
		return null;
	}
	
	public IDebugElement getDebugContext()
	{
		return fDebugContext;
	}

	public void policySetAdded(IUpdatePolicySet set) {
		// do not handle policy set added
		// not activating the policy set unless user has set it
		
	}

	public void policySetRemoved(IUpdatePolicySet set) {
		// if the current policy set is removed, pick primary policy set
		if (fActivePolicySet != null && fActivePolicySet == set)
		{
			unloadHandlers();
			IUpdatePolicySet newSet = DebugUITools.getUpdatePolicyManager().getPrimaryPolicySet(fView.getSite().getId(), getModelIdentifier());
			
			if (newSet == null)
			{
				DebugUIPlugin.logErrorMessage("Cannot find primary policy set: " + fView.getSite().getId() + " " + getModelIdentifier());  //$NON-NLS-1$//$NON-NLS-2$
				return;
			}
			loadHandlers(newSet);
		}
	}

	public void policySetChanged(IUpdatePolicySet set) {
		// if the current policy set is changed, reload policy set
		if (fActivePolicySet != null && fActivePolicySet == set)
		{
			unloadHandlers();
			loadHandlers(set);
		}
		
	}
}
