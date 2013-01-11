/*******************************************************************************
 * Copyright (c) 2009, 2013 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.launch;

import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.ui.contexts.IDebugContextProvider;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.widgets.Menu;

/**
 * Breadcrumb that is shown in debug view when view is in compact mode.
 * 
 * @since 3.5
 */
public class LaunchViewBreadcrumb extends AbstractLaunchViewBreadcrumb {

    public LaunchViewBreadcrumb(LaunchView view, TreeModelViewer treeViewer, IDebugContextProvider contextProvider) {
    	super(view, treeViewer, contextProvider);
    }

    private LaunchView getLaunchView() {
    	return (LaunchView)getPart();
    }
    
    protected boolean isWindowContextProvider() {
    	return true;
    }

    protected boolean isBreadcrumbDropDownAutoexpand() {
    	return getLaunchView().getBreadcrumbDropDownAutoExpand();
    }

    protected boolean isBreadcrumbVisible() {
    	return getLaunchView().isBreadcrumbVisible();
    }
    
    protected void createMenuManager() {
        MenuManager menuMgr = new MenuManager("#PopUp"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager mgr) {
                getLaunchView().fillContextMenu(mgr);
            }
        });
        final Menu menu= menuMgr.createContextMenu(getBreadcrumbViewer().getControl());

        // register the context menu such that other plug-ins may contribute to it
        if (getLaunchView().getSite() != null) {
        	getLaunchView().getSite().registerContextMenu(menuMgr, getBreadcrumbViewer());
        }
        getLaunchView().addContextMenuManager(menuMgr);

        getBreadcrumbViewer().addMenuDetectListener(new MenuDetectListener() {
            public void menuDetected(MenuDetectEvent event) {
                menu.setLocation(event.x + 10, event.y + 10);
                menu.setVisible(true);
                while (!menu.isDisposed() && menu.isVisible()) {
                    if (!menu.getDisplay().readAndDispatch())
                        menu.getDisplay().sleep();
                }
            }
        });
    }
}
