/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model.provisional;

import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.jface.viewers.Viewer;

/**
 * A model proxy represents a model for a specific presentation context and
 * fires deltas to notify listeners of changes in the model. A model proxy
 * is created by a model proxy factory.
 * <p>
 * When an element is added to an asynchronous viewer, its associated model proxy
 * factory is queried to create a model proxy for that element. The model proxy
 * is then installed into the viewer and the viewer listens to model deltas
 * in order to update that element. Generally, a model proxy factory creates
 * model proxies for root elements in a model, and then represents all elements
 * within that model for a specific presentation context. 
 * </p>
 * <p>
 * Clients may implement this interface. Implementations of this interface
 * must subclass {@link AbstractModelProxy}.
 * </p>
 * <p>
 * Note: provider methods are called in the Display thread of the viewer.
 * To avoid blocking the UI, long running operations should be performed 
 * asynchronously.
 * </p>
 * @see IModelDelta
 * @see IModelProxyFactory
 * @see IModelChangedListener
 * @see ICheckboxModelProxy
 * @since 3.2
 */
public interface IModelProxy {

    /**
     * Initialize model proxy with given tree model viewer.  This method is 
     * called on the viewer's Display thread and is guaranteed to be called 
     * before the dispose() method is called on the same proxy.  The default 
     * implementation of this method calls {@link #init(IPresentationContext)} 
     * and {@link #installed(Viewer)} asynchornously and not in the Display 
     * thread.
     * <br>Client may override this method directly, in order to initialize the 
     * model proxy in the viewer's display thread.  Alternatively clients may 
     * override the {@link #installed(Viewer)} method to initialize the proxy
     * in an asynchronous job thread. 
     * </p>
     * <p>
     * This method is called by the asynchronous viewer framework and should 
     * not be called by clients.
     * </p>
     * @param viewer Viewer that is installing this model proxy.
     * 
     * @since 3.8
     */
    public void initialize(ITreeModelViewer viewer);
    
	/**
	 * Notification this model proxy has been created and is about to be installed
	 * in the following context. This is the first method called after a model proxy
	 * is created.  This method is called by the {@link AbstractModelProxy} base 
	 * class using a job and NOT in viewers display thread. It allows the client to
	 * initialize the proxy without blocking the UI.  The default implementaiton is 
	 * a no-op. 
	 * <p>
	 * This method is called by the asynchronous viewer framework and should not
	 * be called by clients.
	 * </p>
	 * @param context presentation context in which the proxy will be installed
	 */
	public void init(IPresentationContext context);
	
	/** 
	 * Notification this model proxy has been installed in the specified 
	 * viewer. This indicates that the model proxy has been created and registered
	 * model change listeners are ready to process deltas.  This method is called 
	 * by the {@link AbstractModelProxy} base class using a job and NOT in viewers 
	 * display thread. It allows the client to initialize the proxy without 
	 * blocking the UI. The default implementaiton is a no-op.
	 * <p>
	 * This method is called by the asynchronous viewer framework and should not
	 * be called by clients.
	 * </p>
     * @param viewer viewer
	 * @since 3.3
	 */
	public void installed(Viewer viewer);
	
	/**
	 * Disposes this model proxy.
	 * <p>
	 * This method is called by the asynchronous viewer framework and should not
	 * be called by clients.
	 * </p>
	 */
	public void dispose();
	
	/**
	 * Registers the given listener for model delta notification.
	 * 
	 * @param listener model delta listener
	 */
	public void addModelChangedListener(IModelChangedListener listener);
	
	/**
	 * Unregisters the given listener from model delta notification.
	 * 
	 * @param listener model delta listener
	 */
	public void removeModelChangedListener(IModelChangedListener listener);
	
	/**
	 * Returns whether this proxy has been disposed.
	 * 
	 * @return whether this proxy has been disposed
	 * @since 3.3
	 */
	public boolean isDisposed();
	
}
