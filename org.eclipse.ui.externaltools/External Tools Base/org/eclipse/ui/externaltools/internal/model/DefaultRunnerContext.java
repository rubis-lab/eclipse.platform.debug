package org.eclipse.ui.externaltools.internal.model;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.io.File;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.externaltools.model.*;
import org.eclipse.ui.externaltools.variable.ExpandVariableContext;

/**
 * Context to run the external tool in.
 */
public final class DefaultRunnerContext implements IRunnerContext {
	private ExternalTool tool;
	private ExpandVariableContext expandVarCtx;
	private String expandedLocation;
	private String[] expandedArguments;
	private String expandedDirectory;
	private String buildType = IExternalToolConstants.BUILD_TYPE_NONE;
	
	/**
	 * Create a new context
	 * 
	 * @param tool the external tool for which the context applies to
	 * @param currentProject the project to run the external tool on, or <code>null</code>
	 */
	public DefaultRunnerContext(ExternalTool tool, IProject project) {
		super();
		this.tool = tool;
		this.expandVarCtx = new ExpandVariableContext(project);
	}

	/**
	 * Create a new context. The project will be determine from the
	 * specified selected resource.
	 * 
	 * @param tool the external tool for which the context applies to
	 * @param selectedResource the selected resource to run the external
	 * 		tool on, or <code>null</code>
	 */
	public DefaultRunnerContext(ExternalTool tool, IResource selectedResource) {
		super();
		this.tool = tool;
		this.expandVarCtx = new ExpandVariableContext(selectedResource);
	}

	/* (non-Javadoc)
	 * Method declared on IRunnerContext.
	 */
	public String getExpandedLocation() {
		return expandedLocation;
	}

	/* (non-Javadoc)
	 * Method declared on IRunnerContext.
	 */
	public String[] getExpandedArguments() {
		return expandedArguments;
	}

	/* (non-Javadoc)
	 * Method declared on IRunnerContext.
	 */
	public String getExpandedWorkingDirectory() {
		return expandedDirectory;
	}
	
	/* (non-Javadoc)
	 * Method declared on IRunnerContext.
	 */
	public String getExtraAttribute(String key) {
		return tool.getExtraAttribute(key);
	}

	/* (non-Javadoc)
	 * Method declared on IRunnerContext.
	 */
	public boolean getLogMessages() {
		return tool.getLogMessages();	
	}

	/* (non-Javadoc)
	 * Method declared on IRunnerContext.
	 */
	public String getName() {
		return tool.getName();
	}
	
	/* (non-Javadoc)
	 * Method declared on IRunnerContext.
	 */
	public boolean getRunInBackground() {
		return tool.getRunInBackground();
	}

	/**
	 * Expands the variables found in the text.
	 */
	private String expandVariables(String text, boolean addQuotes) {
		StringBuffer buffer = new StringBuffer();
		
		int start = 0;
		while (true) {
			ToolUtil.VariableDefinition varDef = ToolUtil.extractVariableTag(text, start);
			
			if (varDef.start == -1) {
				if (start == 0)
					buffer.append(text);
				else
					buffer.append(text.substring(start));
				break;
			} else if (varDef.start > start) {
				buffer.append(text.substring(start, varDef.start));
			}

			if (varDef.end == -1) {
				buffer.append(text.substring(varDef.start));
				break;
			} else {
				start = varDef.end;
			}

			if (varDef.name != null)			
				expandVariable(varDef, buffer, addQuotes);
		}
		
		return buffer.toString();
	}

	/**
	 * Expands the variable
	 */
	private void expandVariable(ToolUtil.VariableDefinition varDef, StringBuffer buf, boolean addQuotes) {
		if (tool.VAR_BUILD_TYPE.equals(varDef.name)) {
			appendVariable(buildType, buf, addQuotes);	
		}

		if (tool.VAR_ANT_TARGET.equals(varDef.name)) {
			if (varDef.argument != null && varDef.argument.length() > 0)
				antTargets.add(varDef.argument);
			return;
		}
		
		if (tool.VAR_WORKSPACE_LOC.equals(varDef.name)) {
			String location = null;
			if (varDef.argument != null && varDef.argument.length() > 0)
				location = ToolUtil.getLocationFromFullPath(varDef.argument);
			else
				location = Platform.getLocation().toOSString();
			appendVariable(location, buf, addQuotes);
			return;
		}
		
		if (tool.VAR_PROJECT_LOC.equals(varDef.name)) {
			String location = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varDef.argument);
				if (member != null)
					location = member.getProject().getLocation().toOSString();
			} else {
				if (currentProject != null)
					location = currentProject.getLocation().toOSString();
			}
			appendVariable(location, buf, addQuotes);
			return;
		}
		
		if (tool.VAR_RESOURCE_LOC.equals(varDef.name)) {
			String location = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				location = ToolUtil.getLocationFromFullPath(varDef.argument);
			} else {
				if (selectedResource != null)
					location = selectedResource.getLocation().toOSString();
			}
			appendVariable(location, buf, addQuotes);
			return;			
		}
		
		if (tool.VAR_CONTAINER_LOC.equals(varDef.name)) {
			String location = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varDef.argument);
				if (member != null)
					location = member.getParent().getLocation().toOSString();
			} else {
				if (selectedResource != null)
					location = selectedResource.getParent().getLocation().toOSString();
			}
			appendVariable(location, buf, addQuotes);
			return;			
		}
		
		if (tool.VAR_PROJECT_PATH.equals(varDef.name)) {
			String fullPath = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varDef.argument);
				if (member != null)
					fullPath = member.getProject().getFullPath().toString();
			} else {
				if (currentProject != null)
					fullPath = currentProject.getFullPath().toString();
			}
			appendVariable(fullPath, buf, addQuotes);
			return;
		}
		
		if (tool.VAR_RESOURCE_PATH.equals(varDef.name)) {
			String fullPath = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varDef.argument);
				if (member != null)
					fullPath = member.getFullPath().toString();
			} else {
				if (selectedResource != null)
					fullPath = selectedResource.getFullPath().toString();
			}
			appendVariable(fullPath, buf, addQuotes);
			return;			
		}
		
		if (tool.VAR_CONTAINER_PATH.equals(varDef.name)) {
			String fullPath = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varDef.argument);
				if (member != null)
					fullPath = member.getParent().getFullPath().toString();
			} else {
				if (selectedResource != null)
					fullPath = selectedResource.getParent().getFullPath().toString();
			}
			appendVariable(fullPath, buf, addQuotes);
			return;			
		}
		
		if (tool.VAR_PROJECT_NAME.equals(varDef.name)) {
			String name = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varDef.argument);
				if (member != null)
					name = member.getProject().getName();
			} else {
				if (currentProject != null)
					name = currentProject.getName();
			}
			appendVariable(name, buf, addQuotes);
			return;
		}
		
		if (tool.VAR_RESOURCE_NAME.equals(varDef.name)) {
			String name = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varDef.argument);
				if (member != null)
					name = member.getName();
			} else {
				if (selectedResource != null)
					name = selectedResource.getName();
			}
			appendVariable(name, buf, addQuotes);
			return;			
		}
		
		if (tool.VAR_CONTAINER_NAME.equals(varDef.name)) {
			String name = null;
			if (varDef.argument != null && varDef.argument.length() > 0) {
				IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varDef.argument);
				if (member != null)
					name = member.getParent().getName();
			} else {
				if (selectedResource != null)
					name = selectedResource.getParent().getName();
			}
			appendVariable(name, buf, addQuotes);
			return;			
		}
	}

	/**
	 * Helper method to add the given variable string to the given
	 * string buffer if the string is not null. Adds enclosing quotation
	 * marks if addQuotes is true.
	 * 
	 * @param var the variable string to be added
	 * @param buf the string buffer to which the string will be added
	 * @parman addQuotes whether or not to add enclosing quotation marks
	 */
	private void appendVariable(String var, StringBuffer buf, boolean addQuotes) {
		if (var != null)
			buf.append(var);
	}
	
	/**
	 * Executes the runner to launch the external tool. A resource refresh
	 * is done if specified.
	 * 
	 * @param monitor the monitor to report progress to, or <code>null</code>.
	 */
	private void executeRunner(IProgressMonitor monitor) throws CoreException, InterruptedException {
		if (monitor == null)
			monitor = new NullProgressMonitor();
		try {
			ToolUtil.VariableDefinition scope = ToolUtil.extractVariableTag(tool.getRefreshScope(), 0);
			ExternalToolsRunner runner = ToolUtil.getRunner(tool.getType());
			if (runner != null) {
				if (scope.name == null || tool.REFRESH_SCOPE_NONE.equals(scope.name)) {
					runner.execute(monitor, this);
				} else {
					monitor.beginTask(ToolMessages.getString("DefaultRunnerContext.runningExternalTool"), 100); //$NON-NLS-1$
					runner.execute(new SubProgressMonitor(monitor, 70), this);
					refreshResources(new SubProgressMonitor(monitor, 30), scope.name, scope.argument);
				}
			}
		} finally {
			monitor.done();
		}
	}
	
	/**
	 * Runs the external tool is the context is valid. Once the tool
	 * has run, resources are refreshed based on the scope. Any problems
	 * while validating or running the tool will cause an exception
	 * to be thrown.
	 * 
	 * @param monitor the monitor to report progress to, or <code>null</code>.
	 */
	public void run(IProgressMonitor monitor) throws CoreException, InterruptedException {
		validateContext();
		executeRunner(monitor);
	}

	/**
	 * Causes the specified resources to be refreshed.
	 */
	private void refreshResources(IProgressMonitor monitor, String scope, String argument) throws CoreException {
		if (tool.REFRESH_SCOPE_WORKSPACE.equals(scope)) {
			ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
			return;
		}
		
		if (tool.REFRESH_SCOPE_PROJECT.equals(scope)) {
			IProject container = null;
			if (argument == null) {
				container = currentProject;
			} else {
				container = ResourcesPlugin.getWorkspace().getRoot().getProject(argument);
			}
			if (container != null && container.isAccessible())
				container.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			return;
		}
		
		if (tool.REFRESH_SCOPE_WORKING_SET.equals(scope)) {
			if (argument == null)
				return;
			IWorkingSet set = workingSetManager.getWorkingSet(argument);
			if (set == null)
				return;
			try {
				IAdaptable[] elements = set.getElements();
				monitor.beginTask(
					ToolMessages.getString("DefaultRunnerContext.refreshWorkingSet"), //$NON-NLS-1$
					elements.length);
				for (int i = 0; i < elements.length; i++) {
					IAdaptable adaptable = elements[i];
					IResource resource;
					
					if (adaptable instanceof IResource)
						resource = (IResource) adaptable;
					else
						resource = (IResource) adaptable.getAdapter(IResource.class);
					if (resource != null)
						resource.refreshLocal(IResource.DEPTH_INFINITE, null);

					monitor.worked(1);
				}
			}
			finally {
				monitor.done();
			}
			
			return;
		}
	}
	
	/**
	 * Set the build type for this context based on the kind of build
	 * being performed by the builder. This is set when the external
	 * tool is being run as a builder.
	 * 
	 * @param buildKind the kind of build being performed (see <code>IncrementalProjectBuilder</code>).
	 */
	public void setBuildType(int buildKind) {
		if (buildKind == IncrementalProjectBuilder.INCREMENTAL_BUILD)
			buildType = IExternalToolConstants.BUILD_TYPE_INCREMENTAL;
		else if (buildKind == IncrementalProjectBuilder.FULL_BUILD)
			buildType = IExternalToolConstants.BUILD_TYPE_FULL;
		else if (buildKind == IncrementalProjectBuilder.AUTO_BUILD)
			buildType = IExternalToolConstants.BUILD_TYPE_AUTO;
		else 
			buildType = IExternalToolConstants.BUILD_TYPE_NONE;
	}
	
	/**
	 * Validates the context in which to run the external tool.
	 * This will cause the location, arguments, and working
	 * directory to be expanded and verified.
	 */
	private void validateContext() throws CoreException {
		expandedLocation = ToolUtil.expandFileLocation(tool.getLocation(), expandVarCtx);
		if (expandedLocation == null || expandedLocation.length() == 0) {
			String msg = ToolMessages.format("DefaultRunnerContext.invalidLocation", new Object[] {tool.getName()}); //$NON-NLS-1$
			ExternalToolsPlugin.getDefault().newError(msg, null);
		}
		File file = new File(expandedLocation);
		if (!file.isFile()) {
			String msg = ToolMessages.format("DefaultRunnerContext.invalidLocation", new Object[] {tool.getName()}); //$NON-NLS-1$
			ExternalToolsPlugin.getDefault().newError(msg, null);
		}
		
		expandedDirectory = ToolUtil.expandDirectoryLocation(tool.getWorkingDirectory(), expandVarCtx);
		if (expandedDirectory != null && expandedDirectory.length() > 0) {
			File path = new File(expandedDirectory);
			if (!path.isDirectory()) {
				String msg = ToolMessages.format("DefaultRunnerContext.invalidDirectory", new Object[] {tool.getName()}); //$NON-NLS-1$
				ExternalToolsPlugin.getDefault().newError(msg, null);
			}
		}
		
		expandedArguments = ToolUtil.expandArguments(tool.getArguments(), expandVarCtx);
	}
}