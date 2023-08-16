/*******************************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/

package org.eclipse.cdt.lsp.editor;

import java.util.ConcurrentModificationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.cdt.lsp.LspUtils;
import org.eclipse.cdt.lsp.server.ICLanguageServerProvider;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.MultiTextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.operations.format.LSPFormatter;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

public final class LspDocumentProvider extends TextFileDocumentProvider {
	private final LSPFormatter formatter = new LSPFormatter();
	private final ICLanguageServerProvider cLanguageServerProvider = LspPlugin.getDefault()
			.getCLanguageServerProvider();

	@Override
	protected DocumentProviderOperation createSaveOperation(final Object element, final IDocument document,
			final boolean overwrite) throws CoreException {
		final FileInfo info = getFileInfo(element);
		if (info != null) {

			if (info.fTextFileBuffer.getDocument() != document) {
				// the info exists, but not for the given document
				// -> saveAs was executed with a target that is already open
				// in another editor
				// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=85519
				Status status = new Status(IStatus.WARNING, LspPlugin.PLUGIN_ID, IStatus.OK,
						"Target file is already open in another editor.", null); //$NON-NLS-1$
				throw new CoreException(status);
			}

			if (!isLanguageServerEnabledFor(info, document)) {
				return super.createSaveOperation(element, document, overwrite);
			}

			return new DocumentProviderOperation() {
				@Override
				protected void execute(IProgressMonitor monitor) throws CoreException {
					formatAndCommit(monitor, element, info, overwrite);
				}

				@Override
				public ISchedulingRule getSchedulingRule() {
					if (info.fElement instanceof IFileEditorInput fileEditorInput) {
						return computeSchedulingRule(fileEditorInput.getFile());
					}
					return null;
				}
			};
		}
		return super.createSaveOperation(element, document, overwrite);
	}

	private boolean isLanguageServerEnabledFor(FileInfo info, IDocument document) {
		if (cLanguageServerProvider != null) {
			var file = getFile(info, document);
			if (file != null) {
				try {
					return LspUtils.isCElement(info.fTextFileBuffer.getContentType())
							&& cLanguageServerProvider.isEnabledFor(file.getProject());
				} catch (CoreException e) {
					Platform.getLog(getClass()).error(e.getMessage(), e);
				}
			}
		}
		return false;
	}

	private IFile getFile(FileInfo info, IDocument document) {
		if (info.fElement instanceof IFileEditorInput fileEditorInput) {
			var file = fileEditorInput.getFile();
			if (file != null) {
				return file;
			}
		}
		return LSPEclipseUtils.getFile(document);
	}

	private void formatAndCommit(IProgressMonitor monitor, Object element, FileInfo info, boolean overwrite)
			throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		try {
			if (shouldFormatCode()) {
				formatCode(progress.split(50), info.fTextFileBuffer);
			}
			commitFileBuffer(progress.split(50), info, overwrite);
		} catch (RuntimeException x) {
			// Inform about the failure
			fireElementStateChangeFailed(element);
			throw x;
		} finally {
			monitor.done();
		}
	}

	private void formatCode(IProgressMonitor monitor, ITextFileBuffer buffer) {
		var document = buffer.getDocument();
		if (document != null) {
			try {
				IRegion[] changedRegions = isLimitedFormatCode()
						? EditorUtility.calculateChangedLineRegions(buffer, monitor)
						: new IRegion[] { new Region(0, document.getLength()) };
				var textSelection = new MultiTextSelection(document, changedRegions);
				formatter.requestFormatting(document, textSelection).get(1000, TimeUnit.MILLISECONDS)
						.ifPresent(edits -> {
							try {
								edits.apply();
							} catch (final ConcurrentModificationException ex) {
								//								ServerMessageHandler.showMessage(Messages.LSPFormatHandler_DiscardedFormat,
								//										new MessageParams(MessageType.Error,
								//												Messages.LSPFormatHandler_DiscardedFormatResponse));
							} catch (BadLocationException e) {
								Platform.getLog(getClass()).error(e.getMessage(), e);
							}
						});
			} catch (BadLocationException | CoreException | InterruptedException | ExecutionException
					| TimeoutException e) {
				Platform.getLog(getClass()).error(e.getMessage(), e);
			}
		}
	}

	private static boolean shouldFormatCode() {
		return PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.FORMAT_SOURCE_CODE);
	}

	private static boolean isLimitedFormatCode() {
		return PreferenceConstants.getPreferenceStore()
				.getBoolean(PreferenceConstants.FORMAT_SOURCE_CODE_LIMIT_TO_EDITED_LINES);
	}
}
