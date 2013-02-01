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


import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.contexts.OverlayIcon;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Drop down action to select the debug context to pin a view to.
 * 
 * @since 3.9
 */
class NewPinnedViewAction extends Action {
	private IPinnablePart fView;
	private Menu fMenu;

	public NewPinnedViewAction(IPinnablePart view) {
		this(view, AS_PUSH_BUTTON);
	}

	public NewPinnedViewAction(IPinnablePart view, int style) {
		super(null, style);
		fView= view;
		setText(ActionMessages.PinViewToContextAction_label);
		setToolTipText(ActionMessages.PinViewToContextAction_tooltip); 
		Image viewImage = fView.getSite().getPart().getTitleImage();
		ImageDescriptor plusOverlayImage = new OverlayIcon(
				ImageDescriptor.createFromImage(viewImage), 
				DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_OVR_NEW)); 
		setImageDescriptor(plusOverlayImage);
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

	protected IPinnablePart getView() {
		return fView;
	}
	
	public void run() {
		IViewPart newPart = NewViewInstanceAction.createNewPart((IViewPart)fView);
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
