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

import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.help.IContext;
import org.eclipse.help.IHelpResource;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PlatformUI;

public class SetUpdatePolicyAction extends Action {
	
	private IUpdatePolicySet fPolicySet;
	private AbstractDebugViewExtension fView;
	private IContext fContextHelp;
	
	public SetUpdatePolicyAction(AbstractDebugViewExtension view, IUpdatePolicySet policySet)
	{
		super(policySet.getName());
		fPolicySet = policySet;
		fView = view;
		
		fContextHelp = new IContext() {

			public IHelpResource[] getRelatedTopics() {
				return new IHelpResource[0];
			}

			public String getText() {
				return getPolicySetDescriptions();
			}
		};
		
		setHelpListener(new HelpListener() {
			public void helpRequested(HelpEvent e) {
				Point point = e.widget.getDisplay().getCursorLocation();
				PlatformUI.getWorkbench().getHelpSystem().displayContext(fContextHelp, point.x, point.y);
			}
		});
				
	}
	public void run() {
		fView.setActivePolicySet(fPolicySet.getId());
	}
	
	private String getPolicySetDescriptions()
	{
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(DebugUIMessages.SetUpdatePolicyAction_0);
		strBuf.append(DebugUIMessages.SetUpdatePolicyAction_1);
		strBuf.append(fPolicySet.getName());
		strBuf.append("<br><br>"); //$NON-NLS-1$
		strBuf.append(DebugUIMessages.SetUpdatePolicyAction_3);
		strBuf.append(":<br>"); //$NON-NLS-1$
		strBuf.append(fPolicySet.getDescription());
		strBuf.append("<br><br>"); //$NON-NLS-1$
		strBuf.append(DebugUIMessages.SetUpdatePolicyAction_6);
		strBuf.append(":<br>"); //$NON-NLS-1$
		
		String[] policyIds = fPolicySet.getPolicies();
		for (int i=0; i<policyIds.length; i++)
		{
			IUpdatePolicy policy = DebugUITools.getUpdatePolicyManager().getPolicy(policyIds[i]);
			strBuf.append("["); //$NON-NLS-1$
			strBuf.append(policy.getName());
			strBuf.append("] : "); //$NON-NLS-1$
			strBuf.append(policy.getDescription());
			strBuf.append("<br>"); //$NON-NLS-1$
		}
		return strBuf.toString();
	}

}
