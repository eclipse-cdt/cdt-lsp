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
		private Map<Annotation, Position> fAddAnnotations;

		/** Lock object for modifying the annotations. */
		private Object fLockObject;

		/**
		 * Initializes this collector with the given annotation model.
		 *
		 * @param annotationModel the annotation model
		 */
		public SpellingProblemCollector() {
			if (fAnnotationModel instanceof ISynchronizable)
				fLockObject = ((ISynchronizable) fAnnotationModel).getLockObject();
			else
				fLockObject = fAnnotationModel;
		}

		@Override
		public void accept(SpellingProblem problem) {
			fAddAnnotations.put(new SpellingAnnotation(problem),
					new Position(problem.getOffset(), problem.getLength()));
		}

		@Override
		public void beginCollecting() {
			fAddAnnotations = new HashMap<>();
		}

		@Override
		public void endCollecting() {

			List<Annotation> toRemove = new ArrayList<>();

			synchronized (fLockObject) {
				Iterator<Annotation> iter = fAnnotationModel.getAnnotationIterator();
				while (iter.hasNext()) {
					Annotation annotation = iter.next();
					if (SpellingAnnotation.TYPE.equals(annotation.getType()))
						toRemove.add(annotation);
				}
				Annotation[] annotationsToRemove = toRemove.toArray(new Annotation[toRemove.size()]);

				if (fAnnotationModel instanceof IAnnotationModelExtension)
					((IAnnotationModelExtension) fAnnotationModel).replaceAnnotations(annotationsToRemove,
							fAddAnnotations);
				else {
					for (Annotation element : annotationsToRemove) {
						fAnnotationModel.removeAnnotation(element);
					}
					for (Entry<Annotation, Position> entry : fAddAnnotations.entrySet()) {
						fAnnotationModel.addAnnotation(entry.getKey(), entry.getValue());
					}
				}
			}

			fAddAnnotations = null;
		}

		public IAnnotationModel getAnnotationModel() {
			return fAnnotationModel;
		}
	}

	/** Text content type */
	private static final IContentType TEXT_CONTENT_TYPE = Platform.getContentTypeManager()
			.getContentType(IContentTypeManager.CT_TEXT);

	/** The text editor to operate on. */
	//private ISourceViewer fViewer;

	/** The document to operate on. */
	private IDocument fDocument;

	/** The progress monitor. */
	private IProgressMonitor fProgressMonitor;

	private SpellingService fSpellingService;

	private ISpellingProblemCollector fSpellingProblemCollector;

	/** The spelling context containing the Java source content type. */
	private SpellingContext fSpellingContext;

	/** Annotation model. */
	private IAnnotationModel fAnnotationModel;

	/**
	 * Region array, used to prevent us from creating a new array on each reconcile pass.
	 * @since 3.4
	 */
	private IRegion[] fRegions = new IRegion[1];

	/**
	 * Creates a new comment reconcile strategy.
	 *
	 * @param viewer the source viewer
	 * @param spellingService the spelling service to use
	 */
	public CSpellingReconcileStrategy() {
		fSpellingService = CSpellingService.getInstance();
		fSpellingContext = new SpellingContext();
		fSpellingContext.setContentType(getContentType());
	}

	@Override
	public void initialReconcile() {
		reconcile(new Region(0, fDocument.getLength()));
	}

	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		try {
			IRegion startLineInfo = fDocument.getLineInformationOfOffset(subRegion.getOffset());
			IRegion endLineInfo = fDocument
					.getLineInformationOfOffset(subRegion.getOffset() + Math.max(0, subRegion.getLength() - 1));
			if (startLineInfo.getOffset() == endLineInfo.getOffset())
				subRegion = startLineInfo;
			else
				subRegion = new Region(startLineInfo.getOffset(),
						endLineInfo.getOffset() + Math.max(0, endLineInfo.getLength() - 1) - startLineInfo.getOffset());

		} catch (BadLocationException e) {
			subRegion = new Region(0, fDocument.getLength());
		}
		reconcile(subRegion);
	}

	@Override
	public void reconcile(IRegion region) {
		if (getAnnotationModel() == null || fSpellingProblemCollector == null)
			return;

		fRegions[0] = region;
		fSpellingService.check(fDocument, fRegions, fSpellingContext, fSpellingProblemCollector, fProgressMonitor);
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
		return fDocument;
	}

	@Override
	public void setDocument(IDocument document) {
		fDocument = document;
		fSpellingProblemCollector = createSpellingProblemCollector(document);
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

		//		var editor = LSPEclipseUtils.findOpenEditorsFor(LSPEclipseUtils.toUri(document)).stream().findFirst()
		//				.orElse(null);
		//
		//		if (editor != null) {
		//			var editorPart = editor.getEditor(true);
		//			var e = editorPart.getAdapter(ITextEditor.class);
		//			if (e != null) {
		//				var p = e.getDocumentProvider();
		//				System.out.print(p.toString());
		//			}
		//		}

		if (viewer != null) {
			fAnnotationModel = viewer.getAnnotationModel();
			fSpellingProblemCollector = new SpellingProblemCollector();
		}
		return fSpellingProblemCollector;
	}

	@Override
	public final void setProgressMonitor(IProgressMonitor monitor) {
		fProgressMonitor = monitor;
	}

	/**
	 * Returns the annotation model to be used by this reconcile strategy.
	 *
	 * @return the annotation model of the underlying editor input or
	 *         <code>null</code> if none could be determined
	 */
	protected IAnnotationModel getAnnotationModel() {
		return fAnnotationModel;
	}

}
