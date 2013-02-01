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


import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Drop down action to select the debug context to pin a view to.
 * 
 * @since 3.9
 */
class NewPinnedViewDropDownAction extends NewPinnedViewAction implements IMenuCreator {

	/**
	 * Pins a workbench part using the given pinned context factory.
	 */
	class NewPinnedViewActionForFactory extends Action {
		private final IPinnedContextFactory fFactory;
		
		/**
		 * Creates the pin action for the given pinnable part.
		 * @param view
		 */
		public NewPinnedViewActionForFactory(IPinnedContextFactory factory) {
			super(null, IAction.AS_RADIO_BUTTON);
			fFactory = factory;
			IDebugContextManager manager = DebugUITools.getDebugContextManager();
			setText(manager.getPinnedContextViewerFactoryName(fFactory.getId()));
			setImageDescriptor(manager.getPinnedContextViewerFactoryImage(fFactory.getId()));
			setToolTipText(manager.getPinnedContextViewerFactoryDescription(fFactory.getId()));
			setId(DebugUIPlugin.getUniqueIdentifier() + ".PinToContextAction"); //$NON-NLS-1$
			setChecked( fFactory.getId().equals(getView().getPinnedFactoryId()) );
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.PIN_TO_CONTEXT_ACTION);
		}
		
		public void run() {
			IViewPart part = NewViewInstanceAction.createNewPart((IViewPart)getView());
			((IPinnablePart)part).pin(fFactory);
		}
	}
	
	private Menu fMenu;

	public NewPinnedViewDropDownAction(IPinnablePart view) {
		super(view, AS_DROP_DOWN_MENU);
		setMenuCreator(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
	public void dispose() {
		if (fMenu != null) {
			fMenu.dispose();
		}
		super.dispose();
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
        IPinnedContextFactory[] factories = manager.getEnabledContextViewerFactories(getView(), StructuredSelection.EMPTY);

        String pinnedFactoryId = getView().getPinnedFactoryId();
        for (int i = 0; i < factories.length; i++) {
            Action action = new NewPinnedViewActionForFactory(factories[i]);
			action.setChecked(factories[i].getId().equals(pinnedFactoryId));
			addActionToMenu(fMenu, action, i + 1);
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
		IViewPart newPart = NewViewInstanceAction.createNewPart((IViewPart)getView());
		((IPinnablePart)newPart).pin(getFactory());
	}

	private IPinnableDebugContextProvider getContextProvider() {
		  IDebugContextProvider provider = getContextService().getActiveProvider();
		  if (provider instanceof IPinnableDebugContextProvider) {
			  return (IPinnableDebugContextProvider)provider;
		  }
		  return null;
	}
		
	private IDebugContextService getContextService() {
		  IWorkbenchWindow window = getView().getSite().getWorkbenchWindow();
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
