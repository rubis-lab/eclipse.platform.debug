package org.eclipse.debug.internal.ui.views.variables;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.treeviewer.AsyncTreeViewer;
import org.eclipse.debug.internal.ui.treeviewer.IPresentationUpdate;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.progress.UIJob;

public class VariablesViewer extends AsyncTreeViewer{

	private VariablesView fView;

	private UIJob fRestoreJob = new UIJob("restore viewer state") { //$NON-NLS-1$
		public IStatus runInUIThread(IProgressMonitor monitor) {
			fView.restoreState();
			return Status.OK_STATUS;
		}
	};
	
	public VariablesViewer(Composite parent, int style, VariablesView view) {
		super(parent, style);
		fView = view;
		fRestoreJob.setSystem(true);
	}

	protected void updateComplete(IPresentationUpdate update) {
		super.updateComplete(update);
		if (fView != null) {
			fRestoreJob.schedule(100);
		}
	}
	
}
