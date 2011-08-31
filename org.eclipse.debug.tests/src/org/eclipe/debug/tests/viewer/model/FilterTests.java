/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Copyright (c) 2009, 2011 Wind River Systems and others.
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipe.debug.tests.viewer.model;

import java.util.regex.Pattern;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipe.debug.tests.viewer.model.TestModel.TestElement;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.internal.ui.viewers.model.IInternalTreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Tests that verify that the viewer property retrieves all the content 
 * from the model.
 * 
 * @since 3.8
 */
abstract public class FilterTests extends TestCase implements ITestModelUpdatesListenerConstants {
    
    Display fDisplay;
    Shell fShell;
    ITreeModelViewer fViewer;
    TestModelUpdatesListener fListener;
    
    public FilterTests(String name) {
        super(name);
    }

    /**
     * @throws java.lang.Exception
     */
    protected void setUp() throws Exception {
        fDisplay = PlatformUI.getWorkbench().getDisplay();
        fShell = new Shell(fDisplay);
        fShell.setMaximized(true);
        fShell.setLayout(new FillLayout());

        fViewer = createViewer(fDisplay, fShell);
        
        fListener = new TestModelUpdatesListener(fViewer, true, true);

        fShell.open ();
    }

    abstract protected IInternalTreeModelViewer createViewer(Display display, Shell shell);
    
    /**
     * @throws java.lang.Exception
     */
    protected void tearDown() throws Exception {
        fListener.dispose();
        fViewer.getPresentationContext().dispose();
        
        // Close the shell and exit.
        fShell.close();
        while (!fShell.isDisposed()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
    }

    protected void runTest() throws Throwable {
        try {
            super.runTest();
        } catch (Throwable t) {
            throw new ExecutionException("Test failed: " + t.getMessage() + "\n fListener = " + fListener.toString(), t);
        }
    }
    
    class TestViewerFilter extends ViewerFilter {
    	
    	Pattern fPattern;
    	TestViewerFilter(String pattern) {
    		fPattern = Pattern.compile(pattern);
    	}
    	
    	
    	 public boolean select(Viewer viewer, Object parentElement, Object element) {
    		 if (element instanceof TestElement) {
    			 TestElement te = (TestElement)element;
    			 return !fPattern.matcher(te.getLabel()).find();
    		 }
    		
    		return true;
    	}
    }
    
    public void testSimpleSingleLevel() throws InterruptedException {
        // Create the model with test data
        TestModel model = TestModel.simpleSingleLevel();

        // Make sure that all elements are expanded
        fViewer.setAutoExpandLevel(-1);
        
        // Create filter for element 2
        ViewerFilter[] filters = new ViewerFilter[] { new TestViewerFilter("2") };
        fViewer.setFilters(filters);
        
        // Create the agent which forces the tree to populate
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        
        // Create the listener which determines when the view is finished updating.
        // fListener.reset(TreePath.EMPTY, model.getRootElement(), filters, -1, false, false);
        fListener.reset(TreePath.EMPTY, model.getRootElement(), filters, -1, true, true);
        
        // Set the viewer input (and trigger updates).
        fViewer.setInput(model.getRootElement());
        
        // Wait for the updates to complete.
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE)) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        
        model.validateData(fViewer, TreePath.EMPTY, false, filters);
    }

    public void testSimpleMultiLevel() throws InterruptedException {
        //TreeModelViewerAutopopulateAgent autopopulateAgent = new TreeModelViewerAutopopulateAgent(fViewer);
        
        TestModel model = TestModel.simpleMultiLevel();
        fViewer.setAutoExpandLevel(-1);
        
        // Create filter for elements ending with numbers 0-4.
        ViewerFilter[] filters = new ViewerFilter[] { new TestViewerFilter(".2") };
        fViewer.setFilters(filters);
        
        //fListener.reset(TreePath.EMPTY, model.getRootElement(), filters, -1, false, false);
        fListener.reset(TreePath.EMPTY, model.getRootElement(), filters, -1, true, true);
        fViewer.setInput(model.getRootElement());
        while (!fListener.isFinished()) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        model.validateData(fViewer, TreePath.EMPTY, false, filters);
    }

    public void testLargeSingleLevel() throws InterruptedException {
        TestModel model = new TestModel();
        model.setRoot( new TestElement(model, "root", new TestElement[0] ) ); 
        model.setElementChildren(TreePath.EMPTY, TestModel.makeSingleLevelModelElements(model, 300, "model."));

        // Create filter for element 2
        ViewerFilter[] filters = new ViewerFilter[] { new TestViewerFilter("2") };
        fViewer.setFilters(filters);

        fListener.setFailOnRedundantUpdates(false);
        //fListener.setFailOnMultipleLabelUpdateSequences(false);
        fListener.reset();
        
        fViewer.setInput(model.getRootElement());
        
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE)) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
    }
    
    /**
     * Replace an element that is not visible but filtered out.  With an element that is NOT filtered out.
     * Fire REPLACE delta.
     */
    public void testReplacedUnrealizedFilteredElement() throws InterruptedException {
        // Populate a view with a large model (only first 100 elements will be visible in virtual viewer).
        TestModel model = new TestModel();
        model.setRoot( new TestElement(model, "root", new TestElement[0] ) ); 
        model.setElementChildren(TreePath.EMPTY, TestModel.makeSingleLevelModelElements(model, 300, "model."));

        // Create filter for element 2
        ViewerFilter[] filters = new ViewerFilter[] { new TestViewerFilter("2") };
        fViewer.setFilters(filters);

        fListener.setFailOnRedundantUpdates(false);
        fListener.reset();

        // Populate the view (all elements containing a "2" will be filtered out.
        fViewer.setInput(model.getRootElement());
        
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE)) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        
        // Switch out element "201" which is filtered out, with a "replaced element" which should NOT be 
        // filtered out.
        TestElement replacedElement = new TestElement(model, "replaced element", new TestElement[0]);
        IModelDelta replaceDelta = model.replaceElementChild(TreePath.EMPTY, 200, replacedElement);
        fListener.reset();
        model.postDelta(replaceDelta);
        while (!fListener.isFinished(MODEL_CHANGED_COMPLETE)) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        // Reposition the viewer to make element 100 the top element, making the replaced element visible.
        fListener.reset();        
        ((IInternalTreeModelViewer) fViewer).reveal(TreePath.EMPTY, 100);
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE)) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        // Verify that the replaced element is in viewer now (i.e. it's not filtered out.
        TreePath[] replacedElementPaths = fViewer.getElementPaths(replacedElement);
        Assert.assertTrue(replacedElementPaths.length != 0);
    }

    
    /**
     * Replace an element that is not visible but filtered out.  With an element that is NOT filtered out.
     * Fire CONTENT delta on parent.
     */
    public void testRefreshUnrealizedFilteredElement() throws InterruptedException {
        // Populate a view with a large model (only first 100 elements will be visible in virtual viewer).
        TestModel model = new TestModel();
        model.setRoot( new TestElement(model, "root", new TestElement[0] ) ); 
        model.setElementChildren(TreePath.EMPTY, TestModel.makeSingleLevelModelElements(model, 300, "model."));

        // Create filter for element 2
        ViewerFilter[] filters = new ViewerFilter[] { new TestViewerFilter("2") };
        fViewer.setFilters(filters);

        fListener.setFailOnRedundantUpdates(false);
        fListener.reset();

        // Populate the view (all elements containing a "2" will be filtered out.
        fViewer.setInput(model.getRootElement());
        
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE)) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);
        
        // Switch out element "201" which is filtered out, with a "replaced element" which should NOT be 
        // filtered out.
        TestElement replacedElement = new TestElement(model, "replaced element", new TestElement[0]);
        model.replaceElementChild(TreePath.EMPTY, 200, replacedElement);
        fListener.reset();
        model.postDelta(new ModelDelta(model.getRootElement(), IModelDelta.CONTENT));
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE)) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        // Reposition the viewer to make element 100 the top element, making the replaced element visible.
        fListener.reset();        
        ((IInternalTreeModelViewer) fViewer).reveal(TreePath.EMPTY, 100);
        while (!fListener.isFinished(ALL_UPDATES_COMPLETE)) if (!fDisplay.readAndDispatch ()) Thread.sleep(0);

        // Verify that the replaced element is in viewer now (i.e. it's not filtered out.
        TreePath[] replacedElementPaths = fViewer.getElementPaths(replacedElement);
        Assert.assertTrue(replacedElementPaths.length != 0);
    }    
}
