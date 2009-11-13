/*******************************************************************************
 * Copyright (c) 2009 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     2009 Freescale - initial API and implementation (Bug 238956)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.model.elements;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IMemento;

public class BreakpointMementoProvider extends ElementMementoProvider {

	private static final String MAP_ELEMENT_NAME = "MAP_ENTRIES"; 	//$NON-NLS-1$
	private static final String MODEL_ID_NAME 	 = "MODEL_ID"; 		//$NON-NLS-1$

	/**
	 * Encodes the map as it may be contained in a property map of a marker into a memento.
	 * 
	 * The map must only contain String keys and values must be of String type, Integer type, Boolean type or null. 
	 * 
	 * @param values
	 * @param memento
	 */
	private static boolean writeMarkerMap(IMemento parent, IMarker marker) {
		Map values;
		try {
			values = marker.getAttributes();
		} catch (CoreException e) {
			// Failed to retrieve markers, just fail to write the memento.
			return false;
		}
		IMemento memento = parent.createChild(MAP_ELEMENT_NAME);
		if (values == null) {
			// Null map is legal for a marker, but we cannot compare such breakpoints.
			return false;
		}
		Iterator it = values.keySet().iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			Assert.isLegal(obj instanceof String);
			String key = (String)obj;
			Object value = values.get(obj);

			if (value instanceof String) {
				memento.putString(key, (String)value);
			} else if (value instanceof Integer) {
				memento.putInteger(key, ((Integer)value).intValue());
			} else if (value instanceof Boolean) {
				memento.putBoolean(key, ((Boolean)value).booleanValue());
			} else {
				// For now using float type as null replacement
				Assert.isLegal(obj == null);
				memento.putFloat(key, 0);
			}
		}
		return true;
	}
	
	/**
	 * Compares a memento generated with WriteMap to a map retrieved from
	 * 
	 * Returns false if the comparison is not possible.
	 * 
	 * Returns true if all marker attributes match.
	 * 
	 * @param parent a memento in which the state of another marker was stored with writeMarkerMap
	 * @param values
	 * @return
	 */
	private static boolean compareMarkerMap(IMemento parent, IMarker marker) {
		Map values;
		try {
			values = marker.getAttributes();
		} catch (CoreException e) {
			return false;
		}
		if (values == null) {
			return false;
		}
		IMemento memento = parent.getChild(MAP_ELEMENT_NAME);
		if (memento == null || values.size() != memento.getAttributeKeys().length) {
			return false; // Different number of elements
		}
		
		Iterator it = values.keySet().iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			Assert.isLegal(obj instanceof String);
			String key = (String)obj;
			Object value = values.get(obj);
			boolean same;
			if (value instanceof String) {
				String val = memento.getString(key);
				same = value.equals(val);
			} else if (value instanceof Integer) {
				Integer val = memento.getInteger(key);
				same = value.equals(val);
			} else if (value instanceof Boolean) {
				Boolean val = memento.getBoolean(key);
				same = value.equals(val);
			} else {
				Float val = memento.getFloat(key);
				// For now using float type as null replacement
				same = val != null && val.floatValue() == 0;
			}
			if (!same) {
				return false;
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.DebugElementMementoProvider#supportsContextId(java.lang.String)
	 */
	protected boolean supportsContextId(String id) {
    	return IDebugUIConstants.ID_BREAKPOINT_VIEW.equals(id);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementMementoProvider#encodeElement(java.lang.Object, org.eclipse.ui.IMemento, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
	 */
	protected boolean encodeElement(Object element, IMemento memento, IPresentationContext context) throws CoreException {
		if (element instanceof IBreakpoint) {
			String modelId = ((IBreakpoint)element).getModelIdentifier();
			memento.putString(MODEL_ID_NAME, modelId);
			
			IMarker marker = ((IBreakpoint)element).getMarker();
			if (marker != null) {
				return writeMarkerMap(memento, marker);
			}
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementMementoProvider#isEqual(java.lang.Object, org.eclipse.ui.IMemento, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
	 */
	protected boolean isEqual(Object element, IMemento memento, IPresentationContext context) throws CoreException {
		if (element instanceof IBreakpoint) {
			String modelId = ((IBreakpoint)element).getModelIdentifier();
			String compareModelId = memento.getString(MODEL_ID_NAME);
			if (!modelId.equals(compareModelId)) {
				return false;
			}

			IMarker marker = ((IBreakpoint)element).getMarker();
			if (marker != null) {
				return compareMarkerMap(memento, marker);
			}
		}
		return false;
	}

}
