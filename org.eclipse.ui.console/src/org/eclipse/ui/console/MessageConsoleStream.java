/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.console;

import java.io.IOException;

/**
 * Used to write messages to a message console. A message console may have more
 * than one stream connected to it. Each stream may be displayed in a different
 * color.
 * <p>
 * Instances are created via a {@link org.eclipse.ui.console.MessageConsole}.
 * </p>
 * <p>
 * Clients should avoid writing large amounts of output to this stream in the UI
 * thread. The console needs to process the output in the UI thread and if the client
 * hogs the UI thread writing output to the console, the console will not be able
 * to process the output.
 * </p>
 * <p>
 * Since 3.1, this class extends {@link org.eclipse.ui.console.IOConsoleOutputStream}.
 * </p>
 * @since 3.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class MessageConsoleStream extends IOConsoleOutputStream {
    
    private MessageConsole fMessageConsole;
    
	/**
	 * Constructs a new stream connected to the given console.
	 * 
	 * @param console the console to write messages to
	 */
	public MessageConsoleStream(MessageConsole console) {
	    super(console);
        fMessageConsole = console;
	}
	
	/**
	 * Appends the specified message to this stream.
	 * 
	 * @param message message to append
	 */
	public void print(String message) {
		try {
            write(message);
        } catch (IOException e) {
            ConsolePlugin.log(e);
        }
	}
	
	
	/**
	 * Appends a line separator string to this stream.
	 */
	public void println() {
		try {
            write("\n"); //$NON-NLS-1$
        } catch (IOException e) {
            ConsolePlugin.log(e);
        }
	}	
	
	/**
	 * Appends the specified message to this stream, followed by a line
	 * separator string.
	 * 
	 * @param message message to print
	 */
	public void println(String message) {
		print(message + "\n"); //$NON-NLS-1$
	}	
    
    /**
     * Returns the console this stream is connected to.
     * 
     * @return the console this stream is connected to
     */
    public MessageConsole getConsole() {
        return fMessageConsole;
    }    
}
