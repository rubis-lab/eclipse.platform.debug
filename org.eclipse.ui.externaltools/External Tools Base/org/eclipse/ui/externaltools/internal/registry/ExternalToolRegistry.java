package org.eclipse.ui.externaltools.internal.registry;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.externaltools.internal.core.*;
import org.eclipse.ui.externaltools.model.*;
import org.eclipse.ui.texteditor.AddTaskAction;

/**
 * The registry of available external tools.
 */
public class ExternalToolRegistry {
	// Format for a tool looks like:
	// <externalTool
	//		type={string}
	//		name={string}
	//		location={string:path}
	//		workDirectory={string:path}
	//		logMessages={string:true/false}
	//		runInBackground={string:true/false}
	//		promptForArguments={string: true/false}
	//		openPerspective={string:id}
	//		refreshScope={string}>
	//		<description>{string}</description}
	//		<arguments>{string}</arguments}
	//		<extraAttribute
	//			key={String}>
	//			{String}
	//		</extraAttribute>
	// </externalTool>
	//
	// Element and attribute tags for storing a tool in an XML file.
	private static final String TAG_EXTERNAL_TOOL = "externalTool"; //$NON-NLS-1$
	private static final String TAG_TYPE = "type"; //$NON-NLS-1$
	private static final String TAG_NAME = "name"; //$NON-NLS-1$
	private static final String TAG_LOCATION = "location"; //$NON-NLS-1$
	private static final String TAG_WORK_DIR = "workDirectory"; //$NON-NLS-1$
	private static final String TAG_LOG_MSG = "logMessages"; //$NON-NLS-1$
	private static final String TAG_RUN_BKGRND = "runInBackground"; //$NON-NLS-1$
	private static final String TAG_OPEN_PERSP = "openPerspective"; //$NON-NLS-1$
	private static final String TAG_PROMPT_ARGS = "promptForArguments"; //$NON-NLS-1$
	private static final String TAG_DESC = "description"; //$NON-NLS-1$
	private static final String TAG_ARGS = "arguments"; //$NON-NLS-1$
	private static final String TAG_REFRESH = "refreshScope"; //$NON-NLS-1$
	private static final String TAG_EXTRA_ATTR = "extraAttribute"; //$NON-NLS-1$
	private static final String TAG_KEY = "key"; //$NON-NLS-1$

	// Possible values for boolean type of attributes	
	private static final String TRUE = "true"; //$NON-NLS-1$
	private static final String FALSE = "false"; //$NON-NLS-1$

	private static final ExternalTool[] EMPTY_TOOLS = new ExternalTool[0];
	
	/**
	 * Path to where the user defined external tools
	 * are stored within the workspace.
	 */
	private static final IPath TOOLS_PATH =
		ExternalToolsPlugin.getDefault().getStateLocation().append(".xtools"); //$NON-NLS-1$

	/**
	 * Extension for external tool files stored within
	 * the workspace
	 */
	private static final String TOOLS_EXTENSION = ".xtool"; //$NON-NLS-1$
	
	/**
	 * Lookup table of external tools where the key is the
	 * type of tool, and the value is a array list of tools of
	 * that type.
	 */
	private HashMap tools = new HashMap();
	
	/**
	 * Lookup table of file names where the key if the external
	 * tool name, and the value if the full path to the file
	 * for that tool
	 */
	private HashMap filenames = new HashMap();
	
	/**
	 * Creates the registry and loads the external tools
	 * from storage.
	 * 
	 * @param shell the shell to use for displaying any errors
	 * 		when loading external tool definitions from storage
	 * 		or <code>null</code> to not report these problems.
	 */
	public ExternalToolRegistry(final Shell shell) {
		super();
		final IStatus results = loadTools();
		if (!results.isOK() && shell != null && !shell.isDisposed()) {
			shell.getDisplay().syncExec(new Runnable() {
				public void run() {
					String title = ToolMessages.getString("ExternalToolRegistry.loadErrorTitle"); //$NON-NLS-1$
					String msg = ToolMessages.getString("ExternalToolRegistry.loadErrorMessage"); //$NON-NLS-1$
					ErrorDialog.openError(shell, title, msg, results);
				}
			});
		}
	}

	/**
	 * Adds an external tool to the in-memory registry.
	 * Note that no check for an existing tool with the
	 * same name is done.
	 */
	private void addTool(ExternalTool tool, IPath filePath) {
		ArrayList list = (ArrayList) tools.get(tool.getType());
		if (list == null) {
			list = new ArrayList(10);
			tools.put(tool.getType(), list);
		}
		list.add(tool);
		
		filenames.put(tool.getName(), filePath);
	}

	/**
	 * Deletes the external tool from storage and
	 * registry.
	 */
	public IStatus deleteTool(ExternalTool tool) {
		IPath filename = (IPath) filenames.get(tool.getName());
		if (filename == null) {
			String msg = ToolMessages.getString("ExternalToolRegistry.noToolFilename"); //$NON-NLS-1$
			return ExternalToolsPlugin.getDefault().newErrorStatus(msg, null);
		}
		
		if (!filename.toFile().delete()) {
			String msg = ToolMessages.format("ExternalToolRegistry.deleteToolFileFailed", new Object[] {filename.toOSString()}); //$NON-NLS-1$
			return ExternalToolsPlugin.getDefault().newErrorStatus(msg, null);
		}
		
		filenames.remove(tool.getName());
		
		ArrayList list = (ArrayList) tools.get(tool.getType());
		if (list != null)
			list.remove(tool);
			
		return ExternalToolsPlugin.OK_STATUS;
	}

	/**
	 * Returns the number of external tools of the specified
	 * type.
	 */
	public int getToolCountOfType(String toolTypeId) {
		ArrayList list = (ArrayList) tools.get(toolTypeId);
		if (list == null)
			return 0;
		else
			return list.size();
	}

	/**
	 * Returns the external tools of the specified
	 * type.
	 */
	public ExternalTool[] getToolsOfType(String toolTypeId) {
		ArrayList list = (ArrayList) tools.get(toolTypeId);
		if (list == null)
			return EMPTY_TOOLS;
		ExternalTool[] results = new ExternalTool[list.size()];
		list.toArray(results);
		return results;
	}

	/**
	 * Returns the external tool with the specified name.
	 * 
	 * @return the external tool with the specified name or
	 * 		<code>null</code> if none exist with that name.
	 */
	public ExternalTool getToolNamed(String name) {
		Iterator typeEnum = tools.values().iterator();
		while (typeEnum.hasNext()) {
			ArrayList list = (ArrayList) typeEnum.next();
			if (list != null && !list.isEmpty()) {
				Iterator toolEnum = list.iterator();
				while (toolEnum.hasNext()) {
					ExternalTool tool = (ExternalTool)toolEnum.next();
					if (tool.getName().equalsIgnoreCase(name))
						return tool;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Loads the external tools from storage and
	 * adds them to the registry.
	 * 
	 * @return a status containing any problems encountered.
	 */
	private IStatus loadTools() {
		String msg = ToolMessages.getString("ExternalToolRegistry.loadToolFailure"); //$NON-NLS-1$
		MultiStatus results = new MultiStatus(IExternalToolConstants.PLUGIN_ID, 0, msg, null);

		final File toolsPath = TOOLS_PATH.toFile();
		if (toolsPath.isDirectory()) {
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return dir.equals(toolsPath) && name.endsWith(TOOLS_EXTENSION);
				}
			};
			
			String[] files = toolsPath.list(filter);
			for (int i = 0; i < files.length; i++) {
				IStatus status = loadTool(TOOLS_PATH.append(files[i]));
				if (status != null)
					results.add(status);
			}
		}
		
		return results;
	}
	
	/**
	 * Loads an external tool from storage.
	 */
	private IStatus loadTool(IPath filePath) {
		IStatus result = null;
		InputStreamReader reader = null;
		
		try {
			FileInputStream input = new FileInputStream(filePath.toFile());
			reader = new InputStreamReader(input, "utf-8"); //$NON-NLS-1$
			IPath basePath = filePath.removeLastSegments(1).addTrailingSeparator();
			XMLMemento memento = XMLMemento.createReadRoot(reader, basePath.toOSString());

			String type = memento.getString(TAG_TYPE);
			String name = memento.getString(TAG_NAME);
			ExternalTool tool = new ExternalTool(type, name);
			
			tool.setLocation(memento.getString(TAG_LOCATION));
			tool.setWorkingDirectory(memento.getString(TAG_WORK_DIR));
			tool.setLogMessages(TRUE.equals(memento.getString(TAG_LOG_MSG)));
			tool.setRunInBackground(TRUE.equals(memento.getString(TAG_RUN_BKGRND)));
			tool.setPromptForArguments(TRUE.equals(memento.getString(TAG_PROMPT_ARGS)));
			tool.setOpenPerspective(memento.getString(TAG_OPEN_PERSP));
			tool.setRefreshScope(memento.getString(TAG_REFRESH));
			
			IMemento child = memento.getChild(TAG_DESC);
			if (child != null)
				tool.setDescription(child.getTextData());

			child = memento.getChild(TAG_ARGS);
			if (child != null)
				tool.setArguments(child.getTextData());
			
			IMemento[] attributes = memento.getChildren(TAG_EXTRA_ATTR);
			for (int i = 0; i < attributes.length; i++) {
				String key = attributes[i].getString(TAG_KEY);
				String value = attributes[i].getTextData();
				tool.setExtraAttribute(key, value);
			}
			
			addTool(tool, filePath);
		} catch (FileNotFoundException e) {
			String msg = e.getMessage();
			if (msg == null)
				msg = ToolMessages.getString("ExternalToolRegistry.fileNotFoundError"); //$NON-NLS-1$
			result = ExternalToolsPlugin.newErrorStatus(msg, e);
		} catch (IOException e) {
			String msg = e.getMessage();
			if (msg == null)
				msg = ToolMessages.getString("ExternalToolRegistry.ioLoadError"); //$NON-NLS-1$
			result = ExternalToolsPlugin.newErrorStatus(msg, e);
		} catch (WorkbenchException e) {
			result = e.getStatus();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch(IOException e) {
					ExternalToolsPlugin.getDefault().log("Unable to close external tool storage reader.", e); //$NON-NLS-1$
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Cause the registry to reload all the tools from storage.
	 * 
	 * @return a status containing any problems encountered.
	 */
	public IStatus reloadTools() {
		tools = new HashMap();
		return loadTools();
	}
	
	/**
	 * Save an external tool to storage. Does not
	 * modify the in-memory registry.
	 */
	public IStatus saveTool(ExternalTool tool) {
		// TODO:
		return null;
	}
}
