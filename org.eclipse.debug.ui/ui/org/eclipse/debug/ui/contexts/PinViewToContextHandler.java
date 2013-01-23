/*******************************************************************************
 * Copyright (c) 2013 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.ui.contexts;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command handler that pins a view to a debug context.
 * <p> 
 * The handler selects the pin-able context factory based on the active 
 * debug context provider and on the available factories.  If a view is
 * already pinned to a context, this handler will un-pin the view.
 * </p>
 * <p>
 * This handler can be used in conjunction with a dynamic contribution for 
 * a drop-down pin menu.  See {@link PinViewToContextDynamicContribution} for 
 * details.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 * @since 3.9
 */
public class PinViewToContextHandler extends AbstractHandler {
	
	  public Object execute(ExecutionEvent event) throws ExecutionException {
		  IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
		  if (part instanceof IPinnablePart) {
			  IPinnablePart pinnable = (IPinnablePart)part;
			  if (!pinnable.isPinned()) {
				  pinnable.pin(getFactory(event));
			  } else {
				  pinnable.clearPin();
			  }
		  }
		  return null;
	  }
	  
	  private IPinnableDebugContextProvider getContextProvider(ExecutionEvent event) {
		  IDebugContextProvider provider = getContextService(event).getActiveProvider();
		  if (provider instanceof IPinnableDebugContextProvider) {
			  return (IPinnableDebugContextProvider)provider;
		  }
		  return null;
	  }
		
	  private IDebugContextService getContextService(ExecutionEvent event) {
		  IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		  if (window != null) {
			  return DebugUITools.getDebugContextManager().getContextService(window);
		  }
		  return null;
	  }
		
	  private IPinnedContextFactory getFactory(ExecutionEvent event) {
		  IPinnableDebugContextProvider provider = getContextProvider(event);
		  if (provider != null) {
			  return DebugUITools.getDebugContextManager().getPinnedContextViewerFactory( provider.getFactoryId() );
		  }
		  return null;
	  }
}