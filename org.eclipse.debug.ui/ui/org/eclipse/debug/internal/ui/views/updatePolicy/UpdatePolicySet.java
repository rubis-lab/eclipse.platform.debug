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
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistable;

public class UpdatePolicySet implements IUpdatePolicySet, IPersistable{

	private static final String ATTR_VIEWID = "viewId"; //$NON-NLS-1$
	private static final String ATTR_MODELID = "modelId"; //$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String ATTR_HIDDEN = "hidden"; //$NON-NLS-1$
	private static final String ATTR_PRIMARY = "primary"; //$NON-NLS-1$
	private static final String ELMT_POLICY_ID = "policy_id"; //$NON-NLS-1$

	
	private IConfigurationElement fConfigurationElement;
	private Set fUpdatePolicies;
	private String fName;
	private String fDescription;
	private String fId;

	public UpdatePolicySet(IConfigurationElement elm)
	{
		fConfigurationElement = elm;
	}
	
	public UpdatePolicySet()
	{
	}
	
	// TODO:  should be an IElementFactory
	public static UpdatePolicySet create(IMemento memento)
	{
		String id = memento.getString(ATTR_ID);
		String name = memento.getString(ATTR_NAME);
		String description = memento.getString(ATTR_DESCRIPTION);
		
		String policyIdList = memento.getString(ELMT_POLICY_ID);
		String[] ids  = null;
		if (policyIdList != null)
		{
			ids = policyIdList.split(","); //$NON-NLS-1$
		}
		
		if (id != null && name != null && description != null && ids != null)
		{
			UpdatePolicySet newSet = new UpdatePolicySet();
			newSet.init(id, name, description, ids);
			return newSet;
		}
		DebugUIPlugin.logErrorMessage("Unable to restore policy set: " + memento.toString()); //$NON-NLS-1$
		return null;
	}
	
	public void init(String id, String name, String description, String[] policies)
	{
		fId = id;
		fName = name;
		fDescription = description;
		fUpdatePolicies = new HashSet();
		for (int i=0; i<policies.length; i++)
		{
			fUpdatePolicies.add(policies[i]);
		}
	}
	
	public void setPolicies(String[] policies)
	{
		fUpdatePolicies.clear();
		for (int i=0; i<policies.length; i++)
		{
			fUpdatePolicies.add(policies[i]);
		}
	}
	
    /**
     * Validates this contribution.
     * 
     * @exception CoreException if invalid
     */
    void validate() throws CoreException {
    	
    	// no need to validate if the policy set is user-defined
    	if (isUserDefined())
    		return;
    	
        verifyPresent(ATTR_ID);
        verifyElmPresent(ELMT_POLICY_ID);
        
        if (!isHidden())
        {
        	verifyPresent(ATTR_NAME);
        	verifyPresent(ATTR_DESCRIPTION);
        }
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
		if (fConfigurationElement == null)
			return null;
		
    	return fConfigurationElement.getAttribute(ATTR_VIEWID);
    }
    
    /**
     * @return model id of this contribution
     */
    public String getModelId()
    {
		if (fConfigurationElement == null)
			return null;
		
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
		if (fConfigurationElement == null)
			return;
		
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
    	String setViewId = getViewId();
    	String setModelId = getModelId();
    	
    	boolean applicable = true;
    	
    	if (setViewId != null)
    	{
    		if (!setViewId.equals(viewId))
    			applicable = false;
    	}
    	
    	if (setModelId != null)
    	{
    		if (!setModelId.equals(modelId))
    			applicable = false;
    	}
    	
    	return applicable;
    }

	public String getName() {
		if (fName == null && fConfigurationElement != null)
			fName = fConfigurationElement.getAttribute(ATTR_NAME);
		return fName;
	}

	public String getDescription() {
		if (fDescription == null && fConfigurationElement != null)
			fDescription = fConfigurationElement.getAttribute(ATTR_NAME);
		return fDescription;
	}

	public boolean isHidden() {
		
		if (isUserDefined())
			return false;
		
    	Boolean hidden = new Boolean("false");  //$NON-NLS-1$
        if (fConfigurationElement.getAttribute(ATTR_HIDDEN) != null)
        {
        	hidden = new Boolean(fConfigurationElement.getAttribute(ATTR_HIDDEN));
        }
        
        return hidden.booleanValue();
	}

	public boolean isPrimary() {
		
		if (isUserDefined())
			return false;
		
    	Boolean hidden = new Boolean("false");  //$NON-NLS-1$
        if (fConfigurationElement.getAttribute(ATTR_PRIMARY) != null)
        {
        	hidden = new Boolean(fConfigurationElement.getAttribute(ATTR_PRIMARY));
        }
        
        return hidden.booleanValue();
	}

	public String getId() {
		if (fId == null && fConfigurationElement != null)
			fId = fConfigurationElement.getAttribute(ATTR_ID);
		return fId;
	}

	public boolean canEdit() {
		// policy set created by extension point cannot be modified
		return isUserDefined();
	}

	public boolean canRemove() {
		// policy set created by extension point cannot be removed
		return isUserDefined();
	}

	public void setName(String name) {
		fName = name;
	}

	public void setDescription(String description) {
		fDescription = description;
	}
	
	public boolean isUserDefined()
	{
		return fConfigurationElement == null;
	}

	public void saveState(IMemento memento) {
		String id = getId();
		String name = getName();
		String description = getDescription();
		String[] policyIds = getPolicies();
		
		memento.putString(ATTR_ID,id);
		memento.putString(ATTR_NAME,name);
		memento.putString(ATTR_DESCRIPTION,description);
		
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<policyIds.length; i++)
		{
			buf.append(policyIds[i]);
			buf.append(","); //$NON-NLS-1$
		}
		memento.putString(ELMT_POLICY_ID,buf.toString());
	}

}
