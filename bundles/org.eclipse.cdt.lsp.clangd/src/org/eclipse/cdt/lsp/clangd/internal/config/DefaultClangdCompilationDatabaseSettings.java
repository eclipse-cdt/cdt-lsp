package org.eclipse.cdt.lsp.clangd.internal.config;

import java.util.Optional;

import org.eclipse.cdt.lsp.clangd.ClangdCompilationDatabaseSettings;
import org.eclipse.cdt.lsp.plugin.LspPlugin;
import org.eclipse.core.resources.IProject;
import org.osgi.service.component.annotations.Component;

@Component(property = { "service.ranking:Integer=0" })
public class DefaultClangdCompilationDatabaseSettings implements ClangdCompilationDatabaseSettings {

	@Override
	public boolean enableSetCompilationDatabasePath(IProject project) {
		return Optional.ofNullable(LspPlugin.getDefault()).map(LspPlugin::getCLanguageServerProvider)
				.map(provider -> provider.isEnabledFor(project)).orElse(Boolean.FALSE);
	}

}
