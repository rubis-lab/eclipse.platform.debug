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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * Implementation to IUpdatePolicy.
 *
 */
public class UpdatePolicy  implements IUpdatePolicy{
	
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	
	private IConfigurationElement fConfigurationElement;

	public UpdatePolicy(IConfigurationElement elm)
	{
		fConfigurationElement = elm;
	}
	
    /**
     * Validates this contribution.
     * 
     * @exception CoreException if invalid
     */
    void validate() throws CoreException {
        verifyPresent(ATTR_CLASS);
        verifyPresent(ATTR_ID);
    }
    
    /**
     * Verify that the specified attribute is present
     * @param attrName - attribute name to test
     * @throws CoreException if the specified attribute is not present
     */
    private void verifyPresent(String attrName) throws CoreException {
        if (fConfigurationElement.getAttribute(attrName) == null) {
            Status status = new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.INTERNAL_ERROR,
                    "<policy> element missing required attribute: " + attrName, null); //$NON-NLS-1$
            throw new CoreException(status);
        }
    }

    
    /**
     * @return name of this handler
     */
    public String getName()
    {
    	return fConfigurationElement.getAttribute(ATTR_NAME);
    }
    
    /**
     * @return description of this handler
     */
    public String getDescription()
    {
    	return fConfigurationElement.getAttribute(ATTR_DESCRIPTION);
    }
    
    public String getId()
    {
    	return fConfigurationElement.getAttribute(ATTR_ID);
    }
    
    /**
     * @return the debug view handler
     * @throws CoreException
     */
    public IUpdatePolicyHandler createHandler() throws CoreException
    {
    	return (IUpdatePolicyHandler)fConfigurationElement.createExecutableExtension(ATTR_CLASS);
    }
}
