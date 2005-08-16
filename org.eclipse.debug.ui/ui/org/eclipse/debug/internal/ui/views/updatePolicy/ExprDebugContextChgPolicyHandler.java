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

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IWatchExpression;

public class ExprDebugContextChgPolicyHandler extends
		DebugContextChangedPolicyHandler {

	public void setDebugContext(IDebugElement debugContext) {
		
		fActiveElement = debugContext;
		
		if (!isVisible()) {
			return;
		}

		// update watch expressions with new context
		IExpression[] expressions = DebugPlugin.getDefault().getExpressionManager().getExpressions();
		for (int i = 0; i < expressions.length; i++) {
			IExpression expression = expressions[i];
			if (expression instanceof IWatchExpression) {
				((IWatchExpression)expression).setExpressionContext(debugContext);
			}
		}
	}
}
