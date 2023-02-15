# Eclipse CDT LSP - LSP based C/C++ Editor

**Target audience** are Eclipse plugin developers who wants to use/develope a LSP based C/C++ Editor.

This plugin is based on the [LSP4E](https://github.com/eclipse/lsp4e) and [TM4E](https://github.com/eclipse/tm4e) Eclipse projects. The editor is based on the `ExtensionBasedTextEditor` in Eclipse.

The `org.eclipse.cdt.lsp` is the core plugin. C/C++ IDE Developers can use the `serverProvider` extension point to define a C/C++ language server. If there is no extension defined, the LLVM [clangd](https://clangd.llvm.org/) language server will be used and searched on the PATH environement variable. Clangd searches for a `compile_commands.json` file in the source file folder and its parents. It's needed to determine the compile informations. In the default clangd configuration the PATH will be searched for a `gcc` compiler to determine the default compiler include paths.

The editor is basically the `ExtensionBasedTextEditor`. The language grammar comes from [TM4E](https://github.com/eclipse/tm4e). 

![image](https://user-images.githubusercontent.com/123444711/219040973-554e6bf7-09f8-41ac-9434-48647557fe2d.png)


The Editors features depends on the support on client ([LSP4E](https://github.com/eclipse/lsp4e)) and server ([clangd](https://clangd.llvm.org/)) side.
Currently these feature are supported (clangd 15.0.3) and current LSP4E:

- Auto completion
- Hovering
- Formatting
- Go to Declaration
- Find References
- Code actions (Declare implicit copy/move members, Extract to function/variable, rename)
- Quick Fix (Ctrl+1)

Not supported (yet):
- Type hierarchy
- Call hierarchy
- Include browser (Eclipse CDT speciality)

The `org.eclipse.cdt.lsp.editor.ui` plugin provides an activation for the LSP based C/C++ Editor on project and workspace level:

![image](https://user-images.githubusercontent.com/123444711/219040726-75207ad7-2dbe-465f-9a65-160e537e8bbf.png)


If enabled on workspace level, newly created C/C++ projects use the LSP based C/C++ editor as default. This can be changed in the project properties:

![image](https://user-images.githubusercontent.com/123444711/219040315-b11dd8e2-f7ba-437e-9b51-ac4d22f14e53.png)

Different C/C++ projects using the old and new editor as default can be mixed in one workspace. The linked include files will be opened with the same editor.

To use these plugins import them in your CDT sources.

**TODO:**
- Support type/call hierarchy and include browser
- Fetch outline informations from language server
- Remove indexer from projects using LSP
...
