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
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;

public class UpdatePolicySet implements IUpdatePolicySet{

	private static final String ATTR_VIEWID = "viewId"; //$NON-NLS-1$
	private static final String ATTR_MODELID = "modelId"; //$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String ATTR_HIDDEN = "hidden"; //$NON-NLS-1$
	private static final String ELMT_POLICY_ID = "policy_id"; //$NON-NLS-1$

	
	private IConfigurationElement fConfigurationElement;
	private Set fUpdatePolicies;

	public UpdatePolicySet(IConfigurationElement elm)
	{
		fConfigurationElement = elm;
	}
	
    /**
     * Validates this contribution.
     * 
     * @exception CoreException if invalid
     */
    void validate() throws CoreException {
        verifyPresent(ATTR_VIEWID);
        verifyPresent(ATTR_MODELID);
        verifyPresent(ATTR_ID);
        
        verifyElmPresent(ELMT_POLICY_ID);
    }
    
    /**
     * Verify that the specified attribute is present
     * @param attrName - attribute name to test
     * @throws CoreException if the specified attribute is not present
     */
    private void verifyPresent(String attrName) throws CoreException {
        if (fConfigurationElement.getAttribute(attrName) == null) {
            Status status = new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.INTERNAL_ERROR,
                    "<policy_set> element missing required attribute: " + attrName, null); //$NON-NLS-1$
            throw new CoreException(status);
        }
    }
    
    private void verifyElmPresent(String elmName) throws CoreException {
    	if (fConfigurationElement.getChildren(elmName).length == 0) {
            Status status = new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.INTERNAL_ERROR,
                    "<policy_set> element missing required element: " + elmName, null); //$NON-NLS-1$
            throw new CoreException(status);
        }
    }
    
	
    /**
     * @return view id of this contribution
     */
    public String getViewId()
    {
    	return fConfigurationElement.getAttribute(ATTR_VIEWID);
    }
    
    /**
     * @return model id of this contribution
     */
    public String getModelId()
    {
    	return fConfigurationElement.getAttribute(ATTR_MODELID);
    }

	public String[] getPolicies() {
		if (fUpdatePolicies == null)
		{
			initPolicies();
		}
		return (String[])fUpdatePolicies.toArray(new String[fUpdatePolicies.size()]);
	}
	
	private void initPolicies()
	{
		IConfigurationElement[] configElmts = fConfigurationElement.getChildren(ELMT_POLICY_ID);
		fUpdatePolicies = new HashSet();
		
		for (int i=0; i<configElmts.length; i++)
		{
			fUpdatePolicies.add(configElmts[i].getAttribute(ATTR_ID));
		}
	}
	
    /**
     * @param viewId
     * @param modelId
     * @return true if the handler should be applied to the specified view and model
     */
    public boolean isApplicable(String viewId, String modelId)
    {
    	if (viewId.equals(getViewId()) && modelId.equals(getModelId()))
    		return true;
    	return false;
    }

	public String getName() {
		return fConfigurationElement.getAttribute(ATTR_NAME);
	}

	public String getDescription() {
		return fConfigurationElement.getAttribute(ATTR_DESCRIPTION);
	}

	public boolean isHidden() {
    	Boolean hidden = new Boolean("false");  //$NON-NLS-1$
        if (fConfigurationElement.getAttribute(ATTR_HIDDEN) != null)
        {
        	hidden = new Boolean(fConfigurationElement.getAttribute(ATTR_HIDDEN));
        }
        
        return hidden.booleanValue();
	}

}
