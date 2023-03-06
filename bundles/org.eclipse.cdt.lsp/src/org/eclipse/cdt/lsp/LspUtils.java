package org.eclipse.cdt.lsp;

public class LspUtils {
	
	/**
	 * Checks if given ContentType id matches the content types for C/C++ files.
	 * 
	 * @param id ContentType id
	 * @return {@code true} if C/C++ content type
	 */
	public static boolean isCContentType(String id) {
		// TODO: The content type definition from TM4E "lng.cpp" can be omitted if either https://github.com/eclipse-cdt/cdt/pull/310 or 
		// https://github.com/eclipse/tm4e/pull/500 has been merged.
		return ( id.startsWith("org.eclipse.cdt.core.c") && (id.endsWith("Source") || id.endsWith("Header")) ) || "lng.cpp".equals(id);
	}

}
