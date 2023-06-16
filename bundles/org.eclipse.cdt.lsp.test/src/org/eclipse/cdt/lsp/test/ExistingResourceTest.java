/*******************************************************************************
 * Copyright (c) 2023 ArSysOp and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Alexander Fedorov (ArSysOp) - initial API
 *******************************************************************************/
package org.eclipse.cdt.lsp.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.eclipse.cdt.lsp.ExistingResource;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

public class ExistingResourceTest {

	@TempDir
	private File TEMPORARY;
	private IProject project;

	private final ExistingResource resource = new ExistingResource(ResourcesPlugin.getWorkspace());

	@BeforeEach
	void create(TestInfo info) throws CoreException {
		project = TestUtils.createCProject(info.getDisplayName());
		project.open(new NullProgressMonitor());
	}

	@AfterEach
	void delete() throws CoreException {
		TestUtils.deleteProject(project);
	}

	@Test
	public void testAbsentFile() {
		IFile given = project.getFile("unknow");
		Optional<IResource> found = resource.apply(given.getLocationURI());
		assertTrue(found.isEmpty());
	}

	@Test
	public void testExistingFile() {
		IFile given = project.getFile(".project");
		Optional<IResource> found = resource.apply(given.getLocationURI());
		assertTrue(found.isPresent());
		assertTrue(found.get() instanceof IFile);
		assertEquals(given, found.get());
	}

	@Test
	public void testAbsentFolder() {
		IFolder given = project.getFolder("src");
		Optional<IResource> found = resource.apply(given.getLocationURI());
		assertTrue(found.isEmpty());
	}

	@Test
	public void testExistingFolder() throws CoreException {
		IFolder given = project.getFolder("src");
		given.create(true, true, new NullProgressMonitor());
		Optional<IResource> found = resource.apply(given.getLocationURI());
		assertTrue(found.isPresent());
		assertTrue(found.get() instanceof IFolder);
		assertEquals(given, found.get());
	}

	@Test
	public void testProject() {
		IProject given = project;
		Optional<IResource> found = resource.apply(given.getLocationURI());
		assertTrue(found.isPresent());
		assertEquals(given, found.get());
	}

	@Test
	public void testExternal() throws IOException {
		File external = new File(TEMPORARY, "ExternalHeader.hpp");
		external.createNewFile();
		Optional<IResource> found = resource.apply(external.toURI());
		assertTrue(found.isEmpty());
	}
}
