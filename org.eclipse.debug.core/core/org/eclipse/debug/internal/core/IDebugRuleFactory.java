/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Creates scheduling rules for debug elements.
 * 
 * @since 3.1
 */
public interface IDebugRuleFactory {

    /**
     * Returns a scheduling rule used to schedule a job that accesses a debug artifact,
     * or <code>null</code> if none.
     * 
     * @param artifact debug artifact to be accessed
     * @return rule used to schedule a job that accesses an artifact, or <code>null</code>
     */
    public ISchedulingRule accessRule(Object artifact);
    
    /**
     * Returns a scheduling rule used to schedule a job that modifies the state of
     * a debug artifact, or <code>null</code> if none.
     * 
     * @param artifact debug artifact to be modified
     * @return rule used to schedule a job that modifies an artifact, or <code>null</code>
     */
    public ISchedulingRule modificationRule(Object artifact);
}
