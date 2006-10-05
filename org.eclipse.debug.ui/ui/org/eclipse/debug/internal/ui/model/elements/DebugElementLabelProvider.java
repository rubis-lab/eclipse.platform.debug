/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.model.elements;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.LazyModelPresentation;
import org.eclipse.debug.internal.ui.model.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.views.launch.DebugElementHelper;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @since 3.3
 */
public class DebugElementLabelProvider extends ElementLabelProvider {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#retrieveLabel(org.eclipse.debug.internal.ui.model.ILabelUpdate)
	 */
	protected void retrieveLabel(ILabelUpdate update) throws CoreException {
    	DelegatingModelPresentation presentation = DebugElementHelper.getPresentation();
    	// Honor view specific settings in a debug view by copying model presentation settings
    	// into the debug element helper's presentation before we get the label. This allows
    	// for qualified name and type name settings to remain in tact.
    	Object element = update.getElement();
    	IPresentationContext context = update.getPresentationContext();
    	if (element instanceof IDebugElement && context.getPart() instanceof IDebugView) {
    		IWorkbenchPart part = context.getPart();
    		if (part instanceof IDebugView) {
    			IDebugModelPresentation pres = ((IDebugView)part).getPresentation(((IDebugElement)element).getModelIdentifier());
    			Map settings = null;
	    		synchronized (presentation) {
	    			if (pres instanceof DelegatingModelPresentation) {
	    				settings = ((DelegatingModelPresentation)pres).getAttributes();
	    			} else if (pres instanceof LazyModelPresentation) {
	    				settings = ((LazyModelPresentation)pres).getAttributes();
	    			}
	    			if (settings != null) {
			    		Iterator iterator = settings.entrySet().iterator();
			    		while (iterator.hasNext()) {
			    			Map.Entry entry = (Entry) iterator.next();
			    			presentation.setAttribute((String) entry.getKey(), entry.getValue());
			    		}
			        	super.retrieveLabel(update);
			        	return;
	    			}
	    		}
	    	}
		}
		super.retrieveLabel(update);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#getLabel(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, java.lang.String)
	 */
	protected String getLabel(Object element, IPresentationContext presentationContext, String columnId) throws CoreException {
		return DebugElementHelper.getLabel(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#getBackground(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, java.lang.String)
	 */
	protected RGB getBackground(Object element, IPresentationContext presentationContext, String columnId) throws CoreException {
		return DebugElementHelper.getBackground(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#getFontDatas(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, java.lang.String)
	 */
	protected FontData getFontData(Object element, IPresentationContext presentationContext, String columnId) throws CoreException {
		return DebugElementHelper.getFont(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#getForeground(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, java.lang.String)
	 */
	protected RGB getForeground(Object element, IPresentationContext presentationContext, String columnId) throws CoreException {
		return DebugElementHelper.getForeground(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#getImageDescriptor(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, java.lang.String)
	 */
	protected ImageDescriptor getImageDescriptor(Object element, IPresentationContext presentationContext, String columnId) throws CoreException {
		return DebugElementHelper.getImageDescriptor(element);
	}
	


}
