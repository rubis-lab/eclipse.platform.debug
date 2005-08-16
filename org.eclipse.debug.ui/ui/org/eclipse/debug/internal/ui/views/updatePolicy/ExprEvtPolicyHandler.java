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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionsListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.ui.DebugUITools;

public class ExprEvtPolicyHandler  extends DebugEventPolicyHandler implements IExpressionsListener{
	
	protected void doHandleTerminateEvent(DebugEvent event) {
		super.doHandleTerminateEvent(event);
		if (event.getSource() instanceof IDebugTarget) {
			IExpression[] expressions = DebugPlugin.getDefault().getExpressionManager().getExpressions();
			IAdaptable object = DebugUITools.getDebugContext();
			IDebugElement context= null;
			if (object instanceof IDebugElement) {
				context= (IDebugElement) object;
			} else if (object instanceof ILaunch) {
				context= ((ILaunch) object).getDebugTarget();
			}
			for (int i = 0; i < expressions.length; i++) {
				IExpression expression = expressions[i];
				if (expression instanceof IWatchExpression) {
					((IWatchExpression)expression).setExpressionContext(context);
				}
			}			
		}
		getView().refresh(null, true);
	}

	public void init(IUpdatePolicy policy, IDebugViewExtension view) {
		super.init(policy, view);
		DebugPlugin plugin= DebugPlugin.getDefault();
		plugin.getExpressionManager().addExpressionListener(this);		
	}
	
	

	public void dispose() {
		DebugPlugin plugin= DebugPlugin.getDefault();
		plugin.getExpressionManager().removeExpressionListener(this);
		super.dispose();
	}

	public void expressionsAdded(IExpression[] expressions) {
		getView().refresh(null, true);
	}

	public void expressionsRemoved(IExpression[] expressions) {
		getView().refresh(null, true);
	}

	public void expressionsChanged(final IExpression[] expressions) {
	}

	protected void doHandleChangeEvent(DebugEvent event) {
	}

	protected void doHandleResumeEvent(DebugEvent event) {
	}

	protected void doHandleSuspendEvent(DebugEvent event) {
	}			
}
