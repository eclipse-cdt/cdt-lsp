/*******************************************************************************
 * Copyright (c) 2024 Bachmann electronic GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal.clangd;

import java.util.Optional;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;

public class FileUtils {

	private FileUtils() {
		// do not instantiate
	}

	/**
	 * Loads a files document for temporary usage. {@link FileUtils#disconnectTemporaryLoadedFile(IFile)} has to be called on the file after usage!
	 * @param file
	 * @return temporary loaded document for the given file or null.
	 */
	public static IDocument loadFileTemporary(IFile file) {
		if (file == null) {
			return null;
		}
		IDocument document = null;

		if (file.getType() == IResource.FILE) {
			var bufferManager = getBufferManager();
			if (bufferManager == null)
				return document;
			try {
				bufferManager.connect(file.getFullPath(), LocationKind.IFILE, new NullProgressMonitor());
			} catch (CoreException e) {
				Platform.getLog(FileUtils.class).error(e.getMessage(), e);
				return document;
			}

			ITextFileBuffer buffer = bufferManager.getTextFileBuffer(file.getFullPath(), LocationKind.IFILE);
			if (buffer != null) {
				document = buffer.getDocument();
			}
		}

		return document;
	}

	/**
	 * When a files document has been obtained via {@link FileUtils#loadFileTemporary(IFile)}, then the file has to be disconnected from it's buffer manager.
	 * @param file
	 */
	public static void disconnectTemporaryLoadedFile(IFile file) {
		Optional.ofNullable(getBufferManager()).ifPresent(bm -> {
			try {
				bm.disconnect(file.getFullPath(), LocationKind.IFILE, new NullProgressMonitor());
			} catch (CoreException e) {
				Platform.getLog(FileUtils.class).error(e.getMessage(), e);
			}
		});
	}

	/**
	 * Tries to fetch the document for the given file. Returns the document when the file is already in the text file buffer or <code>null</code> if not.
	 * @param file
	 * @return document for the given file or <code>null</code>
	 */
	public static IDocument getDocumentFromBuffer(IFile file) {
		if (file == null) {
			return null;
		}
		return Optional.ofNullable(getBufferManager())
				.map(bm -> bm.getTextFileBuffer(file.getFullPath(), LocationKind.IFILE)).map(b -> b.getDocument())
				.orElse(null);
	}

	private static ITextFileBufferManager getBufferManager() {
		return FileBuffers.getTextFileBufferManager();
	}

}
