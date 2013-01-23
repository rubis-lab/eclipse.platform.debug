/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.contexts;

import org.eclipse.debug.internal.ui.contexts.ClearPinnedContextAction;
import org.eclipse.debug.internal.ui.contexts.PinToContextAction;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

/**
 * A dynamic menu contribution that adds a list of factories that can be used
 * to pin a debug view to a specific debug context.  
 * <p>
 * The following example demonstrates how this contribution can be used to 
 * add a drop-down pin action to a view.
 * <pre>
 * &lt;extension 
 *    point="org.eclipse.ui.menus"&gt;
 *     &lt;menuContribution locationURI="toolbar:org.eclipse.debug.ui.VariableView?after=additions"&gt;
 *          &lt;command
 *              commandId="org.eclipse.debug.ui.pinViewToContextDropDown"
 *              icon="icons/full/elcl16/pin.gif"
 *              label="%PinViewToContextCommand.label"
 *              style="pulldown"
 *              tooltip="%PinViewToContextCommand.label"&gt;
 *          &lt;/command&gt;
 *     &lt;/menuContribution&gt;
 *     &lt;menuContribution
 *           locationURI="menu:org.eclipse.debug.ui.pinViewToContextDropDown"&gt;
 *           &lt;dynamic
 *                 class="org.eclipse.debug.ui.contexts.PinViewToContextContribution"
 *                 id="org.eclipse.debug.ui.pinToContextFactories"&gt;
 *           &lt;/dynamic&gt;
 *     &lt;/menuContribution&gt;
 * &lt;/extension&gt;  
 * 
 * &lt;extension point="org.eclipse.ui.commands"&gt;
 *   &lt;command 
 *      id="org.eclipse.debug.ui.pinViewToContextDropDown"
 *      defaultHandler="org.eclipse.debug.ui.contexts.PinViewToContextDropDownHandler" 
 *      name="%PinViewToContextCommand.label"
 *      description="%PinViewToContextCommand.description"/&gt;
 * &lt;/extension&gt;
 * </pre>
 *
 * @since 3.9
 * @noextend This class is not intended to be sub-classed by clients.
 */
public class PinViewToContextDynamicContribution extends CompoundContributionItem implements IWorkbenchContribution {
    
    private IServiceLocator fServiceLocator;

    private static IContributionItem[] NO_PIN_PROVIDERS_CONTRIBUTION_ITEMS = new IContributionItem[] { 
    	new ContributionItem() {
			public void fill(Menu menu, int index) {
				MenuItem item = new MenuItem(menu, SWT.NONE);
				item.setEnabled(false);
				item.setText("No pinned context providers available"); //$NON-NLS-1$
			}
	
			public boolean isEnabled() {
				return false;
			}
    	}
    };
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.CompoundContributionItem#getContributionItems()
     */
    protected IContributionItem[] getContributionItems() {
        IWorkbenchPart part = null;
        IPartService partService = (IPartService)fServiceLocator.getService(IPartService.class);
        if (partService != null) {
            part = partService.getActivePart();
        }

        // If no part or selection, disable all.
        if (!(part instanceof IPinnablePart)) {
            return NO_PIN_PROVIDERS_CONTRIBUTION_ITEMS;
        }
        IPinnablePart pinnable = (IPinnablePart)part;
        
        // Get breakpoint toggle target IDs.
        IDebugContextManager manager = DebugUITools.getDebugContextManager();
        IPinnedContextFactory[] factories = manager.getEnabledContextViewerFactories(pinnable, StructuredSelection.EMPTY);

        IContributionItem[] items = new IContributionItem[factories.length + 1];
        items[0] = makeActionContributionItem(new ClearPinnedContextAction(pinnable), 1);
        for (int i = 0; i < factories.length; i++) {
            Action action = new PinToContextAction(pinnable, factories[i]);
            items[i + 1] = makeActionContributionItem(action, i + 2);
        }
        
        return items;
    }

	private IContributionItem makeActionContributionItem(Action action, int accelerator) {
	    if (accelerator < 10) {
		    StringBuffer label= new StringBuffer();
			//add the numerical accelerator
			label.append('&');
			label.append(accelerator);
			label.append(' ');
			label.append(action.getText());
			action.setText(label.toString());
		}
		return new ActionContributionItem(action);
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.menus.IWorkbenchContribution#initialize(org.eclipse.ui.services.IServiceLocator)
     */
    public void initialize(IServiceLocator serviceLocator) {
        fServiceLocator = serviceLocator;
    }
}
