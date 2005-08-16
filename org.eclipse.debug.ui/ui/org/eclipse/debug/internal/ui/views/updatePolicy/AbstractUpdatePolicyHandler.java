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

import org.eclipse.debug.core.model.IDebugElement;

/**
 * Default implementation of an handler that handles selection event
 *
 */
public abstract class AbstractUpdatePolicyHandler implements IUpdatePolicyHandler {

	private IDebugViewExtension fView;
	protected IDebugElement fActiveElement;
	private boolean fIsDisposed = false;
	private IUpdatePolicy fUpdatePolicy;
	
	public void init(IUpdatePolicy policy, IDebugViewExtension view) {
		fUpdatePolicy = policy;
		fView = view;
	}
	
	public void setDebugContext(IDebugElement debugContext)
	{
		fActiveElement = debugContext;
	}

	public void becomesVisible(IDebugViewExtension view) {

	}

	public void becomesHidden(IDebugViewExtension view) {

	}

	public void becomesVisible(Object[] obj) {

	}

	public void becomesHidden(Object[] obj) {

	}

	public boolean isVisible() {
		return fView.isVisible();
	}

	public boolean isVisible(Object obj) {
		return fView.isVisible();
	}

	public void dispose() {
		fIsDisposed = true;
		fView = null;
		fActiveElement = null;
	}

	public IDebugElement getDebugContext() {
		return fActiveElement;
	}

	public IDebugViewExtension getView() {
		return fView;
	}

	public String getViewId() {
		return fView.getSite().getId();
	}

	public void updateView(IDebugElement elm, boolean getContent) {
		fView.refresh(elm, getContent);
	}
	
	public boolean isDisposed()
	{
		return fIsDisposed;
	}

	public IUpdatePolicy getPolicy()
	{
		return fUpdatePolicy;
	}
}
