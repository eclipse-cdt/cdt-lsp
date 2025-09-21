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

package org.eclipse.cdt.lsp.clangd.format;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.lsp.clangd.plugin.ClangdPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests focus on public methods and especially the resource change listener.
 */
class ClangFormatFileMonitorTest {

	@Mock
	private IWorkspace mockWorkspace;

	@Mock
	private CLanguageServerCheckEnabledProvider mockProvider;

	@Mock
	private IResourceChangeEvent mockEvent;

	@Mock
	private IResourceDelta mockDelta;

	@Mock
	private IFile mockFile;

	@Mock
	private IProject mockProject;

	@Mock
	private IContainer mockContainer;

	@Mock
	private IResource mockResource;

	@Mock
	private IPath mockPath;

	private ClangFormatFileMonitor monitor;
	private AutoCloseable mocks;

	@BeforeEach
	void setUp() {
		mocks = MockitoAnnotations.openMocks(this);
		monitor = new ClangFormatFileMonitor(mockWorkspace, mockProvider);
	}

	@AfterEach
	void tearDown() throws Exception {
		if (mocks != null) {
			mocks.close();
		}
	}

	@Test
	@DisplayName("Given a ClangFormatFileMonitor, When start() is called, Then it should register the resource change listener")
	void testStart_RegistersResourceChangeListener() {
		// Given: A ClangFormatFileMonitor instance

		// When: start() is called
		ClangFormatFileMonitor result = monitor.start();

		// Then: The workspace should have the listener registered
		verify(mockWorkspace).addResourceChangeListener(any(IResourceChangeListener.class));
		assertEquals(monitor, result, "start() should return the same monitor instance");
	}

	@Test
	@DisplayName("Given a started ClangFormatFileMonitor, When stop() is called, Then it should unregister the resource change listener")
	void testStop_UnregistersResourceChangeListener() {
		// Given: A started ClangFormatFileMonitor
		monitor.start();
		ArgumentCaptor<IResourceChangeListener> listenerCaptor = ArgumentCaptor.forClass(IResourceChangeListener.class);
		verify(mockWorkspace).addResourceChangeListener(listenerCaptor.capture());
		IResourceChangeListener capturedListener = listenerCaptor.getValue();

		// When: stop() is called
		monitor.stop();

		// Then: The workspace should have the listener removed
		verify(mockWorkspace).removeResourceChangeListener(capturedListener);
	}

	@Test
	@DisplayName("Given a resource change event with .clang-format file addition, When resourceChanged is called, Then it should process the file")
	void testResourceChanged_ClangFormatFileAdded_ProcessesFile() throws CoreException {
		// Given: A resource change event with .clang-format file addition
		when(mockEvent.getDelta()).thenReturn(mockDelta);
		when(mockEvent.getType()).thenReturn(IResourceChangeEvent.POST_CHANGE);
		when(mockDelta.getKind()).thenReturn(IResourceDelta.ADDED);
		when(mockDelta.getResource()).thenReturn(mockFile);
		when(mockFile.getName()).thenReturn(ClangFormatFileMonitor.CLANG_FORMAT_FILE);
		when(mockFile.getProject()).thenReturn(mockProject);
		when(mockProject.hasNature(CProjectNature.C_NATURE_ID)).thenReturn(true);
		when(mockProvider.isEnabledFor(mockProject)).thenReturn(true);

		// Setup delta visitor to accept our delta
		doAnswer(invocation -> {
			IResourceDeltaVisitor visitor = invocation.getArgument(0);
			return visitor.visit(mockDelta);
		}).when(mockDelta).accept(any(IResourceDeltaVisitor.class));

		monitor.start();
		ArgumentCaptor<IResourceChangeListener> listenerCaptor = ArgumentCaptor.forClass(IResourceChangeListener.class);
		verify(mockWorkspace).addResourceChangeListener(listenerCaptor.capture());
		IResourceChangeListener listener = listenerCaptor.getValue();

		// When: resourceChanged is called
		listener.resourceChanged(mockEvent);

		// Then: The file should be processed (checkJob scheduled)
		verify(mockDelta).accept(any(IResourceDeltaVisitor.class));
		verify(mockProvider).isEnabledFor(mockProject);
	}

	@Test
	@DisplayName("Given a resource change event with .clang-format file content change, When resourceChanged is called, Then it should process the file")
	void testResourceChanged_ClangFormatFileContentChanged_ProcessesFile() throws CoreException {
		// Given: A resource change event with .clang-format file content change
		when(mockEvent.getDelta()).thenReturn(mockDelta);
		when(mockEvent.getType()).thenReturn(IResourceChangeEvent.POST_CHANGE);
		when(mockDelta.getKind()).thenReturn(IResourceDelta.CHANGED);
		when(mockDelta.getFlags()).thenReturn(IResourceDelta.CONTENT);
		when(mockDelta.getResource()).thenReturn(mockFile);
		when(mockFile.getName()).thenReturn(ClangFormatFileMonitor.CLANG_FORMAT_FILE);
		when(mockFile.getProject()).thenReturn(mockProject);
		when(mockProject.hasNature(CProjectNature.C_NATURE_ID)).thenReturn(true);
		when(mockProvider.isEnabledFor(mockProject)).thenReturn(true);

		doAnswer(invocation -> {
			IResourceDeltaVisitor visitor = invocation.getArgument(0);
			return visitor.visit(mockDelta);
		}).when(mockDelta).accept(any(IResourceDeltaVisitor.class));

		monitor.start();
		ArgumentCaptor<IResourceChangeListener> listenerCaptor = ArgumentCaptor.forClass(IResourceChangeListener.class);
		verify(mockWorkspace).addResourceChangeListener(listenerCaptor.capture());
		IResourceChangeListener listener = listenerCaptor.getValue();

		// When: resourceChanged is called
		listener.resourceChanged(mockEvent);

		// Then: The file should be processed
		verify(mockDelta).accept(any(IResourceDeltaVisitor.class));
		verify(mockProvider).isEnabledFor(mockProject);
	}

	@Test
	@DisplayName("Given a resource change event with non-.clang-format file, When resourceChanged is called, Then it should not process the file")
	void testResourceChanged_NonClangFormatFile_DoesNotProcessFile() throws CoreException {
		// Given: A resource change event with a non-.clang-format file
		when(mockEvent.getDelta()).thenReturn(mockDelta);
		when(mockEvent.getType()).thenReturn(IResourceChangeEvent.POST_CHANGE);
		when(mockDelta.getKind()).thenReturn(IResourceDelta.ADDED);
		when(mockDelta.getResource()).thenReturn(mockFile);
		when(mockFile.getName()).thenReturn("other-file.txt");

		doAnswer(invocation -> {
			IResourceDeltaVisitor visitor = invocation.getArgument(0);
			return visitor.visit(mockDelta);
		}).when(mockDelta).accept(any(IResourceDeltaVisitor.class));

		monitor.start();
		ArgumentCaptor<IResourceChangeListener> listenerCaptor = ArgumentCaptor.forClass(IResourceChangeListener.class);
		verify(mockWorkspace).addResourceChangeListener(listenerCaptor.capture());
		IResourceChangeListener listener = listenerCaptor.getValue();

		// When: resourceChanged is called
		listener.resourceChanged(mockEvent);

		// Then: The language server provider should not be checked
		verify(mockProvider, never()).isEnabledFor(any());
	}

	@Test
	@DisplayName("Given a .clang-format file with no project, When resourceChanged is called, Then it should not process the file")
	void testResourceChanged_FileWithoutProject_DoesNotProcessFile() throws CoreException {
		// Given: A .clang-format file with no project
		when(mockEvent.getDelta()).thenReturn(mockDelta);
		when(mockEvent.getType()).thenReturn(IResourceChangeEvent.POST_CHANGE);
		when(mockDelta.getKind()).thenReturn(IResourceDelta.ADDED);
		when(mockDelta.getResource()).thenReturn(mockFile);
		when(mockFile.getName()).thenReturn(ClangFormatFileMonitor.CLANG_FORMAT_FILE);
		when(mockFile.getProject()).thenReturn(null);

		doAnswer(invocation -> {
			IResourceDeltaVisitor visitor = invocation.getArgument(0);
			return visitor.visit(mockDelta);
		}).when(mockDelta).accept(any(IResourceDeltaVisitor.class));

		monitor.start();
		ArgumentCaptor<IResourceChangeListener> listenerCaptor = ArgumentCaptor.forClass(IResourceChangeListener.class);
		verify(mockWorkspace).addResourceChangeListener(listenerCaptor.capture());
		IResourceChangeListener listener = listenerCaptor.getValue();

		// When: resourceChanged is called
		listener.resourceChanged(mockEvent);

		// Then: The language server provider should not be called
		verify(mockProvider, never()).isEnabledFor(any());
	}

	@Test
	@DisplayName("Given a .clang-format file with language server disabled, When resourceChanged is called, Then it should not process the file")
	void testResourceChanged_LanguageServerDisabled_DoesNotProcessFile() throws CoreException {
		// Given: A .clang-format file with language server disabled for the project
		when(mockEvent.getDelta()).thenReturn(mockDelta);
		when(mockEvent.getType()).thenReturn(IResourceChangeEvent.POST_CHANGE);
		when(mockDelta.getKind()).thenReturn(IResourceDelta.ADDED);
		when(mockDelta.getResource()).thenReturn(mockFile);
		when(mockFile.getName()).thenReturn(ClangFormatFileMonitor.CLANG_FORMAT_FILE);
		when(mockFile.getProject()).thenReturn(mockProject);
		when(mockProject.hasNature(CProjectNature.C_NATURE_ID)).thenReturn(true);
		when(mockProvider.isEnabledFor(mockProject)).thenReturn(false);

		doAnswer(invocation -> {
			IResourceDeltaVisitor visitor = invocation.getArgument(0);
			return visitor.visit(mockDelta);
		}).when(mockDelta).accept(any(IResourceDeltaVisitor.class));

		monitor.start();
		ArgumentCaptor<IResourceChangeListener> listenerCaptor = ArgumentCaptor.forClass(IResourceChangeListener.class);
		verify(mockWorkspace).addResourceChangeListener(listenerCaptor.capture());
		IResourceChangeListener listener = listenerCaptor.getValue();

		// When: resourceChanged is called
		listener.resourceChanged(mockEvent);

		// Then: The provider should be checked but file not processed further
		verify(mockProvider).isEnabledFor(mockProject);
	}

	@Test
	@DisplayName("Given a .clang-format file in a non C/C++ project, When resourceChanged is called, Then it should not process the file")
	void testResourceChanged_NoCproject_DoesNotProcessFile() throws CoreException {
		// Given: A .clang-format file in a non C/C++ project
		when(mockEvent.getDelta()).thenReturn(mockDelta);
		when(mockEvent.getType()).thenReturn(IResourceChangeEvent.POST_CHANGE);
		when(mockDelta.getKind()).thenReturn(IResourceDelta.ADDED);
		when(mockDelta.getResource()).thenReturn(mockFile);
		when(mockFile.getName()).thenReturn(ClangFormatFileMonitor.CLANG_FORMAT_FILE);
		when(mockFile.getProject()).thenReturn(mockProject);
		when(mockProject.hasNature(CProjectNature.C_NATURE_ID)).thenReturn(false);
		when(mockProvider.isEnabledFor(mockProject)).thenReturn(true);

		doAnswer(invocation -> {
			IResourceDeltaVisitor visitor = invocation.getArgument(0);
			return visitor.visit(mockDelta);
		}).when(mockDelta).accept(any(IResourceDeltaVisitor.class));

		monitor.start();
		ArgumentCaptor<IResourceChangeListener> listenerCaptor = ArgumentCaptor.forClass(IResourceChangeListener.class);
		verify(mockWorkspace).addResourceChangeListener(listenerCaptor.capture());
		IResourceChangeListener listener = listenerCaptor.getValue();

		// When: resourceChanged is called
		listener.resourceChanged(mockEvent);

		// Then: The provider should not be called since it's not a C/C++ project
		verify(mockProvider, never()).isEnabledFor(any());
	}

	@Test
	@DisplayName("Given a resource change event with null delta, When resourceChanged is called, Then it should handle gracefully")
	void testResourceChanged_NullDelta_HandlesGracefully() {
		// Given: A resource change event with null delta
		when(mockEvent.getDelta()).thenReturn(null);
		when(mockEvent.getType()).thenReturn(IResourceChangeEvent.POST_CHANGE);

		monitor.start();
		ArgumentCaptor<IResourceChangeListener> listenerCaptor = ArgumentCaptor.forClass(IResourceChangeListener.class);
		verify(mockWorkspace).addResourceChangeListener(listenerCaptor.capture());
		IResourceChangeListener listener = listenerCaptor.getValue();

		// When: resourceChanged is called
		assertDoesNotThrow(() -> listener.resourceChanged(mockEvent));

		// Then: No exception should be thrown and no delta processing should occur
		assertDoesNotThrow(() -> verify(mockDelta, never()).accept(any()));
	}

	@Test
	@DisplayName("Given a resource change event with wrong type, When resourceChanged is called, Then it should not process")
	void testResourceChanged_WrongEventType_DoesNotProcess() {
		// Given: A resource change event with wrong type
		when(mockEvent.getDelta()).thenReturn(mockDelta);
		when(mockEvent.getType()).thenReturn(IResourceChangeEvent.PRE_BUILD);

		monitor.start();
		ArgumentCaptor<IResourceChangeListener> listenerCaptor = ArgumentCaptor.forClass(IResourceChangeListener.class);
		verify(mockWorkspace).addResourceChangeListener(listenerCaptor.capture());
		IResourceChangeListener listener = listenerCaptor.getValue();

		// When: resourceChanged is called
		listener.resourceChanged(mockEvent);

		// Then: Delta should not be processed
		assertDoesNotThrow(() -> verify(mockDelta, never()).accept(any()));
	}

	@Test
	@DisplayName("Given a delta visitor that throws CoreException, When resourceChanged is called, Then it should handle the exception gracefully")
	void testResourceChanged_CoreExceptionInVisitor_HandlesGracefully() throws CoreException {
		// Given: A delta visitor that throws CoreException
		when(mockEvent.getDelta()).thenReturn(mockDelta);
		when(mockEvent.getType()).thenReturn(IResourceChangeEvent.POST_CHANGE);
		doAnswer(invocation -> {
			throw new CoreException(new Status(IStatus.ERROR, ClangdPlugin.PLUGIN_ID, "Test exception"));
		}).when(mockDelta).accept(any(IResourceDeltaVisitor.class));

		monitor.start();
		ArgumentCaptor<IResourceChangeListener> listenerCaptor = ArgumentCaptor.forClass(IResourceChangeListener.class);
		verify(mockWorkspace).addResourceChangeListener(listenerCaptor.capture());
		IResourceChangeListener listener = listenerCaptor.getValue();

		// When: resourceChanged is called
		assertDoesNotThrow(() -> listener.resourceChanged(mockEvent));

		// Then: Exception should be caught and logged (no exception propagated)
		verify(mockDelta).accept(any(IResourceDeltaVisitor.class));
	}

	@Test
	@DisplayName("Given a resource change with resource that is not IFile, When resourceChanged is called, Then it should not process")
	void testResourceChanged_ResourceNotIFile_DoesNotProcess() throws CoreException {
		// Given: A resource change with resource that is not IFile
		when(mockEvent.getDelta()).thenReturn(mockDelta);
		when(mockEvent.getType()).thenReturn(IResourceChangeEvent.POST_CHANGE);
		when(mockDelta.getKind()).thenReturn(IResourceDelta.ADDED);
		when(mockDelta.getResource()).thenReturn(mockResource); // Not an IFile
		when(mockResource.getName()).thenReturn(ClangFormatFileMonitor.CLANG_FORMAT_FILE);

		doAnswer(invocation -> {
			IResourceDeltaVisitor visitor = invocation.getArgument(0);
			return visitor.visit(mockDelta);
		}).when(mockDelta).accept(any(IResourceDeltaVisitor.class));

		monitor.start();
		ArgumentCaptor<IResourceChangeListener> listenerCaptor = ArgumentCaptor.forClass(IResourceChangeListener.class);
		verify(mockWorkspace).addResourceChangeListener(listenerCaptor.capture());
		IResourceChangeListener listener = listenerCaptor.getValue();

		// When: resourceChanged is called
		listener.resourceChanged(mockEvent);

		// Then: Provider should not be called since resource is not IFile
		verify(mockProvider, never()).isEnabledFor(any());
	}
}
