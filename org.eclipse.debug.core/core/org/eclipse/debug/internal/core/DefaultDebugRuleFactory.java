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
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * Default rule factory returns rules that serialzie access and modification of
 * debug elements.
 * 
 * @since 3.1
 */
public class DefaultDebugRuleFactory implements IDebugRuleFactory {
    
    private static IDebugRuleFactory fgDefault;
    
    class PessimisticRule implements ISchedulingRule {
        
        private IDebugElement fElement;
        
        PessimisticRule(IDebugElement element) {
            fElement = element;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
         */
        public boolean contains(ISchedulingRule rule) {
            if (rule instanceof PessimisticRule) {
                return isSameTarget((PessimisticRule) rule, this);
            }
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
         */
        public boolean isConflicting(ISchedulingRule rule) {
            if (rule instanceof PessimisticRule) {
                return isSameTarget((PessimisticRule) rule, this);
            }
            return false;
        }
        
        private boolean isSameTarget(PessimisticRule a, PessimisticRule b) {
            IDebugTarget t1 = a.fElement.getDebugTarget();
            IDebugTarget t2 = b.fElement.getDebugTarget();
            return t1 != null && t1.equals(t2);
        }
        
    }
    
    public static IDebugRuleFactory getDefault() {
        if (fgDefault == null) {
            fgDefault = new DefaultDebugRuleFactory();
        }
        return fgDefault;
    }

    /**
     * Constucts a rule factory.
     */
    DefaultDebugRuleFactory() {
        super();
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.core.IDebugRuleFactory#accessRule(java.lang.Object)
     */
    public ISchedulingRule accessRule(Object artifact) {
    	if (artifact instanceof IDebugElement) {
        	return new PessimisticRule((IDebugElement)artifact);
    	}
    	return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.core.IDebugRuleFactory#modificationRule(java.lang.Object)
     */
    public ISchedulingRule modificationRule(Object artifact) {
    	if (artifact instanceof IDebugElement) {
        	return new PessimisticRule((IDebugElement)artifact);
    	}
    	return null;
    }

}
