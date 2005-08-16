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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.internal.ui.DebugUIPlugin;

public class UpdatePolicyMgr implements IUpdatePolicyManager {

	private static final IUpdatePolicySet[] EMPTY = new IUpdatePolicySet[0];
	private static final String ELEMENT_POLICY_SET = "policySet"; //$NON-NLS-1$
	private static final String ELEMENT_POLICY = "policy"; //$NON-NLS-1$
	
	private static IUpdatePolicyManager fgDefault;
	
	private List fPolicySets = new ArrayList();
	private Hashtable fPolicies = new Hashtable();
	
	public static IUpdatePolicyManager getDefault()
	{
		if (fgDefault == null)
			fgDefault = new UpdatePolicyMgr();
		
		return fgDefault;
	}
	
	public UpdatePolicyMgr()
	{
		parseExtensionPoint();
	}
	
	public IUpdatePolicySet[] getPolicySets(String viewId, String modelId) {
		List returnList = new ArrayList();
		
		Iterator iter = fPolicySets.iterator();
		while(iter.hasNext())
		{
			UpdatePolicySet policySet = (UpdatePolicySet)iter.next();
			if (policySet.isApplicable(viewId, modelId))
				returnList.add(policySet);
		}
		
		if (returnList.isEmpty())
			return EMPTY;
		
		return (IUpdatePolicySet[])returnList.toArray(new IUpdatePolicySet[returnList.size()]);
	}
	
	private void parseExtensionPoint()
	{
        IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), IUpdatePolicyConstants.EXTENSION_POINT_UPDATE_POLICY);
        IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
        for (int i = 0; i < configurationElements.length; i++) {
            IConfigurationElement element= configurationElements[i];
            if (element.getName().equals(ELEMENT_POLICY_SET))
            {
            	try {
					UpdatePolicySet policySet = new UpdatePolicySet(element);
					policySet.validate();
					fPolicySets.add(policySet);
				} catch (CoreException e) {
					DebugUIPlugin.log(e);
				}
            }
            else if (element.getName().equals(ELEMENT_POLICY))
            {
            	try {
					UpdatePolicy policy = new UpdatePolicy(element);
					policy.validate();
					fPolicies.put(policy.getId(), policy);
				} catch (CoreException e) {
					DebugUIPlugin.log(e);
				}
            }
        }
	}

	public IUpdatePolicy getPolicy(String policyId) {
		return (IUpdatePolicy) fPolicies.get(policyId);
	}

}
