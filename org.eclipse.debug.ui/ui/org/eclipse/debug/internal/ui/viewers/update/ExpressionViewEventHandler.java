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

package org.eclipse.debug.internal.ui.viewers.update;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.internal.ui.viewers.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.IModelProxy;

public class ExpressionViewEventHandler extends DebugEventHandler {

    public ExpressionViewEventHandler(IModelProxy proxy) {
        super(proxy);
    }

    protected boolean handlesEvent(DebugEvent event) {
        return event.getSource() instanceof IExpression;
    }

    protected void handleChange(DebugEvent event) {
        // TODO Auto-generated method stub
        super.handleChange(event);
    }

    protected void handleCreate(DebugEvent event) {
        // TODO Auto-generated method stub
        super.handleCreate(event);
    }

    protected void refreshRoot(DebugEvent event) {
        ModelDelta delta = new ModelDelta();
        delta.addNode(DebugPlugin.getDefault().getExpressionManager(), IModelDelta.CHANGED | IModelDelta.CONTENT);
        getModelProxy().fireModelChanged(delta);
    }
    
    
    

}
