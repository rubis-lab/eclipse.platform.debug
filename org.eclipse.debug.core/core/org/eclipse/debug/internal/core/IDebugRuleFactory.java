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
import org.eclipse.debug.core.model.IDebugElement;

/**
 * Creates scheduling rules for debug elements.
 * 
 * @since 3.1
 */
public interface IDebugRuleFactory {

    /**
     * Returns a scheduling rule used to schedule a job that accesses a debug element.
     * 
     * @param debugElement debug element to be accessed
     * @return rule used to schedule a job that accesses a debug element.
     */
    public ISchedulingRule accessRule(IDebugElement debugElement);
    
    /**
     * Returns a scheduling rule used to schedule a job that modifies the state of
     * a debug element
     * @param debugElement debug element to be modified
     * @return rule used to schedule a job that modifies a debug element
     */
    public ISchedulingRule modificationRule(IDebugElement debugElement);
}
