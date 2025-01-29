/*******************************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.Platform;

public final class AsyncStreamPipe {

	/**
	 * Starts a pipe from @input to @output
	 * Returns a runnable that can stop the pipe
	 */
	public Runnable pipeTo(final String threadName, final InputStream input, final OutputStream output) {
		if (output != null) {
			final InputStream bufferedInput = new BufferedInputStream(input);
			final AtomicBoolean stop = new AtomicBoolean(false);
			final Runnable writer = () -> {
				try {
					final byte[] buffer = new byte[1024];
					int size = 0;
					do {
						if (stop.get()) {
							break;
						}
						size = bufferedInput.read(buffer);
						if (size > -1) {
							output.write(buffer, 0, size);
						}
					} while (size > -1 && !Thread.interrupted());
				} catch (IOException ioe) {
					if (!stop.get()) {
						Platform.getLog(getClass()).error(ioe.getMessage(), ioe);
					}
				}
			};
			final Thread writerThread = new Thread(writer, threadName);
			final Runnable stopper = () -> {
				stop.set(true);
			};
			writerThread.setDaemon(true);
			writerThread.start();
			return stopper;
		}
		final Runnable emptyRunner = () -> {
		};
		return emptyRunner;
	}
}
