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

import java.io.InputStream;
import java.io.OutputStream;

@SuppressWarnings("all")
public final class AsyncStreamPipe {
	/**
	 * Starts a pipe from @input to @output
	 * Returns a runnable that can stop the pipe
	 */
	public Runnable pipeTo(final InputStream input, final OutputStream output) {
		if (output != null) {
			final Runnable[] cancelation = new Runnable[1];
			final Runnable writer = () -> {
				try {
					final byte[] buffer = new byte[1024];
					int size = 0;
					do {
						size = input.read(buffer);
						if (size > -1) {
							output.write(buffer, 0, size);
						}
					} while (size > -1 && !Thread.interrupted());
					final Runnable c = cancelation[0];
					if ((c != null)) {
						synchronized (c) {
							c.notify();
						}
					}
				} catch (Throwable t) {
					throw sneakyThrow(t);
				}
			};
			final Thread writerThread = new Thread(writer);
			final Runnable stopper = () -> {
				writerThread.interrupt();
			};
			cancelation[0] = stopper;
			writerThread.start();
			return cancelation[0];
		}
		final Runnable emptyRunner = () -> {
		};
		return emptyRunner;
	}

	private static RuntimeException sneakyThrow(Throwable t) {
		if (t == null)
			throw new NullPointerException("t");
		sneakyThrowT(t);
		return null;
	}

	@SuppressWarnings("unchecked")
	private static <T extends Throwable> void sneakyThrowT(Throwable t) throws T {
		throw (T) t;
	}
}
