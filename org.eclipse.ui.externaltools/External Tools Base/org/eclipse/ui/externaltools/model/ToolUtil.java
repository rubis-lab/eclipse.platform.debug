package org.eclipse.ui.externaltools.model;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.externaltools.internal.core.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.core.IPreferenceConstants;
import org.eclipse.ui.externaltools.internal.core.ToolMessages;
import org.eclipse.ui.externaltools.internal.registry.PathLocationVariable;
import org.eclipse.ui.externaltools.internal.registry.PathLocationVariableRegistry;
import org.eclipse.ui.externaltools.variable.ExpandVariableContext;

/**
 * General utility class dealing with external tools
 */
public final class ToolUtil {
	/**
	 * Variable tag indentifiers
	 */
	private static final String VAR_TAG_START = "${"; //$NON-NLS-1$
	private static final String VAR_TAG_END = "}"; //$NON-NLS-1$
	private static final String VAR_TAG_SEP = ":"; //$NON-NLS-1$

	/**
	 * No instances allowed
	 */
	private ToolUtil() {
		super();
	}

	/**
	 * Builds a variable tag that will be auto-expanded before
	 * the tool is run.
	 * 
	 * @param varName the name of a known variable (one of the VAR_* constants for instance)
	 * @param varArgument an optional argument for the variable, <code>null</code> if none
	 */
	public static String buildVariableTag(String varName, String varArgument) {
		StringBuffer buf = new StringBuffer();
		buildVariableTag(varName,varArgument, buf);
		return buf.toString();
	}
	
	/**
	 * Builds a variable tag that will be auto-expanded before
	 * the tool is run.
	 * 
	 * @param varName the name of a known variable (one of the VAR_* constants for instance)
	 * @param varArgument an optional argument for the variable, <code>null</code> if none
	 * @param buffer the buffer to write the constructed variable tag
	 */
	public static void buildVariableTag(String varName, String varArgument, StringBuffer buffer) {
		buffer.append(VAR_TAG_START);
		buffer.append(varName);
		if (varArgument != null && varArgument.length() > 0) {
			buffer.append(VAR_TAG_SEP);
			buffer.append(varArgument);
		}
		buffer.append(VAR_TAG_END);
	}
	
	/**
	 * Returns the expanded directory location if represented by a
	 * directory variable. Otherwise, the directory location given is
	 * return unless an unknown variable was detected.
	 * 
	 * @param dirLocation a directory location either as a path or a variable
	 * 		with leading and trailing spaces already removed.
	 * @param context the context used to expand the variable
	 * @return the directory location as a string
	 */
	public static String expandDirectoryLocation(String dirLocation, ExpandVariableContext context) throws CoreException {
		if (dirLocation == null || dirLocation.length() == 0)
			return ""; //$NON-NLS-1$

		VariableDefinition varDef = extractVariableTag(dirLocation, 0);
		// Return if no variable found
		if (varDef.start < 0)
			return dirLocation;
		
		// Disallow text before/after variable
		if (varDef.start != 0 || (varDef.end < dirLocation.length() && varDef.end != -1)) {
			String text = ToolMessages.getString("ToolUtil.dirLocVarBetweenText"); //$NON-NLS-1$
			throw ExternalToolsPlugin.getDefault().newError(text, null);
		}
		
		// Invalid variable format
		if (varDef.name == null || varDef.name.length() == 0 || varDef.end == -1) {
			String text = ToolMessages.getString("ToolUtil.dirLocVarFormatWrong"); //$NON-NLS-1$
			throw ExternalToolsPlugin.getDefault().newError(text, null);
		}
		
		// Lookup the variable if it exist
		PathLocationVariableRegistry registry;
		registry = ExternalToolsPlugin.getDefault().getDirectoryLocationVariableRegistry();
		PathLocationVariable variable = registry.getPathLocationVariable(varDef.name);
		if (variable == null) {
			String text = ToolMessages.format("ToolUtil.dirLocVarMissing", new Object[] {varDef.name}); //$NON-NLS-1$
			throw ExternalToolsPlugin.getDefault().newError(text, null);
		}
		
		// Expand the variable into a IPath if possible
		IPath path = variable.getLocation().getPath(varDef.name, varDef.argument, context);
		if (path == null) {
			String text = ToolMessages.format("ToolUtil.dirLocVarExpandFailed", new Object[] {varDef.name}); //$NON-NLS-1$
			throw ExternalToolsPlugin.getDefault().newError(text, null);
		}
		
		return path.toOSString();
	}
	
	/**
	 * Returns the expanded file location if represented by a
	 * file variable. Otherwise, the file location given is
	 * return unless an unknown variable was detected.
	 * 
	 * @param fileLocation a file location either as a path or a variable
	 * 		with leading and trailing spaces already removed.
	 * @param context the context used to expand the variable
	 * @return the file location as a string
	 */
	public static String expandFileLocation(String fileLocation, ExpandVariableContext context) throws CoreException {
		if (fileLocation == null || fileLocation.length() == 0)
			return ""; //$NON-NLS-1$

		VariableDefinition varDef = extractVariableTag(fileLocation, 0);
		// Return if no variable found
		if (varDef.start < 0)
			return fileLocation;
		
		// Disallow text before/after variable
		if (varDef.start != 0 || (varDef.end < fileLocation.length() && varDef.end != -1)) {
			String text = ToolMessages.getString("ToolUtil.fileLocVarBetweenText"); //$NON-NLS-1$
			throw ExternalToolsPlugin.getDefault().newError(text, null);
		}
		
		// Invalid variable format
		if (varDef.name == null || varDef.name.length() == 0 || varDef.end == -1) {
			String text = ToolMessages.getString("ToolUtil.fileLocVarFormatWrong"); //$NON-NLS-1$
			throw ExternalToolsPlugin.getDefault().newError(text, null);
		}
		
		// Lookup the variable if it exist
		PathLocationVariableRegistry registry;
		registry = ExternalToolsPlugin.getDefault().getFileLocationVariableRegistry();
		PathLocationVariable variable = registry.getPathLocationVariable(varDef.name);
		if (variable == null) {
			String text = ToolMessages.format("ToolUtil.fileLocVarMissing", new Object[] {varDef.name}); //$NON-NLS-1$
			throw ExternalToolsPlugin.getDefault().newError(text, null);
		}
		
		// Expand the variable into a IPath if possible
		IPath path = variable.getLocation().getPath(varDef.name, varDef.argument, context);
		if (path == null) {
			String text = ToolMessages.format("ToolUtil.fileLocVarExpandFailed", new Object[] {varDef.name}); //$NON-NLS-1$
			throw ExternalToolsPlugin.getDefault().newError(text, null);
		}
		
		return path.toOSString();
	}
	
	/**
	 * Extracts from the source text the variable tag's name
	 * and argument.
	 * 
	 * @param text the source text to parse for a variable tag
	 * @param start the index in the string to start the search
	 * @return the variable definition
	 */
	public static VariableDefinition extractVariableTag(String text, int start) {
		VariableDefinition varDef = new VariableDefinition();
		
		varDef.start = text.indexOf(VAR_TAG_START, start);
		if (varDef.start < 0)
			return varDef;
		start = varDef.start + VAR_TAG_START.length();
		
		int end = text.indexOf(VAR_TAG_END, start);
		if (end < 0)
			return varDef;
		varDef.end = end + VAR_TAG_END.length();
		if (end == start)
			return varDef;
	
		int mid = text.indexOf(VAR_TAG_SEP, start);
		if (mid < 0 || mid > end) {
			varDef.name = text.substring(start, end);
		} else {
			if (mid > start)
				varDef.name = text.substring(start, mid);
			mid = mid + VAR_TAG_SEP.length();
			if (mid < end)
				varDef.argument = text.substring(mid, end);
		}
		
		return varDef;
	}
	
	/**
	 * Saves any dirty editors if user preference
	 */
	public static void saveDirtyEditors(IWorkbenchWindow window) {
		IPreferenceStore store = ExternalToolsPlugin.getDefault().getPreferenceStore();
		boolean autoSave = store.getBoolean(IPreferenceConstants.AUTO_SAVE);
		if (autoSave) {
			IWorkbenchWindow[] windows = window.getWorkbench().getWorkbenchWindows();
			for (int i=0; i < windows.length; i++) {
				IWorkbenchPage[] pages = windows[i].getPages();
				for (int j = 0; j < pages.length; j++) {
					pages[j].saveAllEditors(false);
				}
			}
		}
	}
	
	/**
	 * Structure to represent a variable definition within a
	 * source string.
	 */
	public static final class VariableDefinition {
		/**
		 * Index in the source text where the variable started
		 * or <code>-1</code> if no valid variable start tag 
		 * identifier found.
		 */
		public int start = -1;
		
		/**
		 * Index in the source text of the character following
		 * the end of the variable or <code>-1</code> if no 
		 * valid variable end tag found.
		 */
		public int end = -1;
		
		/**
		 * The variable's name found in the source text, or
		 * <code>null</code> if no valid variable found.
		 */
		public String name = null;
		
		/**
		 * The variable's argument found in the source text, or
		 * <code>null</code> if no valid variable found or if
		 * the variable did not specify an argument
		 */
		public String argument = null;
		
		/**
		 * Create an initialized variable definition.
		 */
		private VariableDefinition() {
			super();
		}
		
		/**
		 * Create an initialized variable definition.
		 */
		private VariableDefinition(int start, int end) {
			super();
			this.start = start;
			this.end = end;
		}
	}
}
