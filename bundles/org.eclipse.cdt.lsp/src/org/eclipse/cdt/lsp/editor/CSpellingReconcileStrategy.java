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

package org.eclipse.cdt.lsp.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.eclipse.cdt.internal.ui.text.spelling.CSpellingService;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector;
import org.eclipse.ui.texteditor.spelling.SpellingAnnotation;
import org.eclipse.ui.texteditor.spelling.SpellingContext;
import org.eclipse.ui.texteditor.spelling.SpellingProblem;
import org.eclipse.ui.texteditor.spelling.SpellingReconcileStrategy;
import org.eclipse.ui.texteditor.spelling.SpellingService;

/**
 * Copied from {@link SpellingReconcileStrategy} since we cannot extend it, because have no viewer available at construction time.
 */
public class CSpellingReconcileStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

	/**
	 * Spelling problem collector.
	 */
	private class SpellingProblemCollector implements ISpellingProblemCollector {

		/** Annotations to add. */
		private Map<Annotation, Position> annotationsToAdd;

		/** Lock object for modifying the annotations. */
		private Object lockObject;

		/**
		 * Initializes this collector with the given annotation model.
		 *
		 * @param annotationModel the annotation model
		 */
		public SpellingProblemCollector() {
			if (annotationModel instanceof ISynchronizable)
				lockObject = ((ISynchronizable) annotationModel).getLockObject();
			else
				lockObject = annotationModel;
		}

		@Override
		public void accept(SpellingProblem problem) {
			annotationsToAdd.put(new SpellingAnnotation(problem),
					new Position(problem.getOffset(), problem.getLength()));
		}

		@Override
		public void beginCollecting() {
			annotationsToAdd = new HashMap<>();
		}

		@Override
		public void endCollecting() {
			if (annotationModel == null) {
				return;
			}

			List<Annotation> toRemove = new ArrayList<>();

			synchronized (lockObject) {
				Iterator<Annotation> iter = annotationModel.getAnnotationIterator();
				while (iter.hasNext()) {
					Annotation annotation = iter.next();
					if (SpellingAnnotation.TYPE.equals(annotation.getType()))
						toRemove.add(annotation);
				}
				Annotation[] annotationsToRemove = toRemove.toArray(new Annotation[toRemove.size()]);

				if (annotationModel instanceof IAnnotationModelExtension)
					((IAnnotationModelExtension) annotationModel).replaceAnnotations(annotationsToRemove,
							annotationsToAdd);
				else {
					for (Annotation element : annotationsToRemove) {
						annotationModel.removeAnnotation(element);
					}
					for (Entry<Annotation, Position> entry : annotationsToAdd.entrySet()) {
						annotationModel.addAnnotation(entry.getKey(), entry.getValue());
					}
				}
			}

			annotationsToAdd = null;
		}
	}

	/** Text content type */
	private static final IContentType TEXT_CONTENT_TYPE = Platform.getContentTypeManager()
			.getContentType(IContentTypeManager.CT_TEXT);

	/** The document to operate on. */
	private IDocument document;

	/** The progress monitor. */
	private IProgressMonitor progressMonitor;

	private SpellingService spellingService;

	/** The spelling context containing the Java source content type. */
	private SpellingContext spellingContext;

	private ISpellingProblemCollector spellingProblemCollector;

	/** Annotation model. */
	private IAnnotationModel annotationModel;

	/**
	 * Region array, used to prevent us from creating a new array on each reconcile pass.
	 */
	private IRegion[] regions = new IRegion[1];

	/**
	 * Creates a new comment reconcile strategy.
	 *
	 * @param viewer the source viewer
	 * @param spellingService the spelling service to use
	 */
	public CSpellingReconcileStrategy() {
		spellingService = CSpellingService.getInstance();
		spellingContext = new SpellingContext();
		spellingContext.setContentType(getContentType());
	}

	@Override
	public void initialReconcile() {
		reconcile(new Region(0, document.getLength()));
	}

	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		try {
			IRegion startLineInfo = document.getLineInformationOfOffset(subRegion.getOffset());
			IRegion endLineInfo = document
					.getLineInformationOfOffset(subRegion.getOffset() + Math.max(0, subRegion.getLength() - 1));
			if (startLineInfo.getOffset() == endLineInfo.getOffset())
				subRegion = startLineInfo;
			else
				subRegion = new Region(startLineInfo.getOffset(),
						endLineInfo.getOffset() + Math.max(0, endLineInfo.getLength() - 1) - startLineInfo.getOffset());

		} catch (BadLocationException e) {
			subRegion = new Region(0, document.getLength());
		}
		reconcile(subRegion);
	}

	@Override
	public void reconcile(IRegion region) {
		if (annotationModel == null || spellingProblemCollector == null)
			return;

		regions[0] = region;
		spellingService.check(document, regions, spellingContext, spellingProblemCollector, progressMonitor);
	}

	/**
	 * Returns the content type of the underlying editor input.
	 *
	 * @return the content type of the underlying editor input or
	 *         <code>null</code> if none could be determined
	 */
	protected IContentType getContentType() {
		return TEXT_CONTENT_TYPE;
	}

	/**
	 * Returns the document which is spell checked.
	 *
	 * @return the document
	 */
	protected final IDocument getDocument() {
		return document;
	}

	@Override
	public void setDocument(IDocument document) {
		this.document = document;
		spellingProblemCollector = createSpellingProblemCollector(document);
	}

	/**
	 * Creates a new spelling problem collector.
	 *
	 * @return the collector or <code>null</code> if none is available
	 */
	protected ISpellingProblemCollector createSpellingProblemCollector(IDocument document) {
		var viewer = LSPEclipseUtils.findOpenEditorsFor(LSPEclipseUtils.toUri(document)).stream()
				.map(reference -> reference.getEditor(true)).filter(Objects::nonNull)
				.map(LSPEclipseUtils::getTextViewer).filter(Objects::nonNull).filter(ISourceViewer.class::isInstance)
				.map(ISourceViewer.class::cast).findFirst().orElse(null);

		if (viewer != null) {
			annotationModel = viewer.getAnnotationModel();
			spellingProblemCollector = new SpellingProblemCollector();
		} else {
			spellingProblemCollector = null;
		}
		return spellingProblemCollector;
	}

	@Override
	public final void setProgressMonitor(IProgressMonitor monitor) {
		progressMonitor = monitor;
	}

}
