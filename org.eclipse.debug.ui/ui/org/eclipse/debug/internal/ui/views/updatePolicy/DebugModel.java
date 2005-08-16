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
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;

public class DebugModel {

	private static final IUpdatePolicyHandler[] EMPTY = new IUpdatePolicyHandler[0];
	
	private Set fPolicyHandlers = new HashSet();
	private String fModelIdentifier;
	private IDebugViewExtension fView;
	
	public DebugModel(String modelIdentifier, IDebugViewExtension view)
	{
		fModelIdentifier = modelIdentifier;
		fView = view;
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
		
		if (fPolicyHandlers.isEmpty())
			loadHandlers();
		
		if (debugContext != null)
		{
			if (debugContext.getModelIdentifier().equals(getModelIdentifier()))
			{
				// set input to handlers
				IUpdatePolicyHandler[] handlers = getPolicyHandlers();
				for (int i=0; i<handlers.length; i++)
				{
					// TODO:  look at for Expressions View
					// if enabled, delayed update does not work
					// if disabled, views not updated when view becomes visible
//					if (handlers[i].getDebugContext() != debugContext)
						handlers[i].setDebugContext(debugContext);
				}
				return;
			}
		}
	}
	
	public void deactivateModel()
	{
		unloadHandlers();
	}
	
	public void dispose()
	{
		unloadHandlers();
		fPolicyHandlers = null;
	}
	
	public void loadHandlers()
	{
		// when input is changed, load handlers
		IUpdatePolicySet[] policySets = DebugUITools.getUpdatePolicyManager().getPolicySets(fView.getSite().getId(), getModelIdentifier());
		
		if (policySets.length > 0)
		{
			for (int i=0; i<policySets.length; i++)
			{
				try {
					
					String [] policyIds = policySets[i].getPolicies();
					
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
}
