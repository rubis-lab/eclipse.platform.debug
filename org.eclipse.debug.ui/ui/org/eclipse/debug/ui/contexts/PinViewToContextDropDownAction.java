/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.contexts;


import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.contexts.ClearPinnedContextAction;
import org.eclipse.debug.internal.ui.contexts.PinToContextAction;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Drop down action to select the debug context to pin a view to.
 * 
 * @since 3.9
 */
class PinViewToContextDropDownAction extends Action implements IMenuCreator {

	private IPinnablePart fView;
	private Menu fMenu;
	
	public PinViewToContextDropDownAction(IPinnablePart view) {
		super(null, AS_DROP_DOWN_MENU);
		fView= view;
		setText(ActionMessages.PinViewToContextAction_label);
		setToolTipText(ActionMessages.PinViewToContextAction_tooltip); 
		setImageDescriptor(DebugUITools.getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_PIN));
		setMenuCreator(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
	public void dispose() {
		if (fMenu != null) {
			fMenu.dispose();
		}
		
		fView = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
	 */
	public Menu getMenu(Menu parent) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
	 */
	public Menu getMenu(Control parent) {
		if (fMenu != null) {
			fMenu.dispose();
		}
		
		fMenu= new Menu(parent);
        IDebugContextManager manager = DebugUITools.getDebugContextManager();
        IPinnedContextFactory[] factories = manager.getEnabledContextViewerFactories(fView, StructuredSelection.EMPTY);

        String pinnedFactoryId = fView.getPinnedFactoryId();
		addActionToMenu(fMenu, new ClearPinnedContextAction(fView), 1);
        for (int i = 0; i < factories.length; i++) {
            Action action = new PinToContextAction(fView, factories[i]);
			action.setChecked(factories[i].getId().equals(pinnedFactoryId));
			addActionToMenu(fMenu, action, i + 2);
        }
		return fMenu;
	}
	
	private void addActionToMenu(Menu parent, Action action, int accelerator) {
	    if (accelerator < 10) {
		    StringBuffer label= new StringBuffer();
			//add the numerical accelerator
			label.append('&');
			label.append(accelerator);
			label.append(' ');
			label.append(action.getText());
			action.setText(label.toString());
		}
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		if (!fView.isPinned()) {
			fView.pin(getFactory());
		} else {
			fView.clearPin();
		}
	}
	
	private IPinnableDebugContextProvider getContextProvider() {
		  IDebugContextProvider provider = getContextService().getActiveProvider();
		  if (provider instanceof IPinnableDebugContextProvider) {
			  return (IPinnableDebugContextProvider)provider;
		  }
		  return null;
	}
		
	private IDebugContextService getContextService() {
		  IWorkbenchWindow window = fView.getSite().getWorkbenchWindow();
		  return DebugUITools.getDebugContextManager().getContextService(window);
	}
		
	private IPinnedContextFactory getFactory() {
		  IPinnableDebugContextProvider provider = getContextProvider();
		  if (provider != null) {
			  return DebugUITools.getDebugContextManager().getPinnedContextViewerFactory( provider.getFactoryId() );
		  }
		  return null;
	}
}
