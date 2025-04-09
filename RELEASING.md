This is the Release plan and TODO list for CDT LSP.

## Steps for Release

Items at the beginning of development

- [ ] Create an Endgame Issue to track the release. As a starting point use [RELEASING.md](https://github.com/eclipse-cdt/cdt-lsp/blob/main/RELEASING.md).
    - [ ] Add the [Endgame](https://github.com/eclipse-cdt/cdt-lsp/labels/endgame) label
- [ ] Create a [New milestone](https://github.com/eclipse-cdt/cdt-lsp/milestones/new) for the release, and if available add a due date 
    - [ ] Apply the milestone to the endgame issue
- [ ] Ensure all previous [Endgame issues](https://github.com/eclipse-cdt/cdt-lsp/labels/endgame) are done.
    - [ ] Make sure any previous edits made to [Endgame issues](https://github.com/eclipse-cdt/cdt-lsp/labels/endgame) of previous releases are updated in [RELEASING.md](https://github.com/eclipse-cdt/cdt-lsp/blob/main/RELEASING.md)
- [ ] Update version numbers on main branch (after the release branch was created), this is generally the next minor version (or major version if that is what the committers on the project agree) and applies to the following types of files:
    - [ ] `feature.xml` version. For example `3.1.0.qualifier` becomes `3.2.0.qualifier`.
    - [ ] `pom.xml` version. For example `3.1.0-SNAPSHOT` becomes `3.2.0-SNAPSHOT`.
    - [ ] `MANIFEST.MF` version, which follow [API rules](https://github.com/eclipse-cdt/cdt/blob/main/POLICY.md#api).
          This generally means updating the patch segment of the version by 100. For example `3.0.200.qualifier` becomes `3.0.300.qualifier`.
    - [ ] [`cdt-baseline.target`](https://github.com/eclipse-cdt/cdt/blob/889a5f1db5f0795a99c319e57982071de43924ec/releng/org.eclipse.cdt.target/cdt-baseline.target) baseline version, in coordination with the main CDT repo.
    - [ ] [`CDT.setup`](https://github.com/eclipse-cdt/cdt/blob/56581db808f5a8df1dea71654ce90351848291ce/releng/CDT.setup) baseline version, in coordination with the main CDT repo.
- [ ] Ensure [the CI build](https://ci.eclipse.org/cdt/job/cdt-lsp/job/main/) is stable - it is always better to release a "Green Dot" build

Items in the days ahead of Release day:

- [ ] Create release on [PMI](https://projects.eclipse.org/projects/tools.cdt) (e.g. `1.0.0 (CDT LSP)`)
    - [ ] Fill in the *Review Documentation* -> *New & Noteworthy URL* with the [CHANGELOG.md](https://github.com/eclipse-cdt/cdt-lsp/blob/main/CHANGELOG.md)
- [ ] Check [CHANGELOG.md](https://github.com/eclipse-cdt/cdt-lsp/blob/main/CHANGELOG.md) is up to date. The changelog should have a version entry, release date, API Breakages and other information consistent with current entries in the changelog.
- [ ] Check [README.md](https://github.com/eclipse-cdt/cdt-lsp/blob/main/README.md) is up to date, in particular:
    - [ ] the planned release and which versions of main dependencies are supported in the version support table
    - [ ] screenshots are up to date and consistent
    - [ ] try it out steps are correct and where suitable versions are up to date
- [ ] Check all closed PRs and Issues to make sure their milestone is set. [This search may be useful to identify such closed issues](https://github.com/eclipse-cdt/cdt-lsp/issues?q=is%3Aclosed)
- [ ] Create a branch for the release
- [ ] Create the endgame for the next scheduled release right away and update the versions on the main branch

Items on Release day:

- [ ] Run [the CI build](https://ci.eclipse.org/cdt/job/cdt-lsp/) for the branch
- [ ] Mark the build as Keep Forever and add the version to the description
- [ ] Tag the release. Example: `git tag -a CDT_LSP_1_0_0 HEAD -m"CDT LSP 1.0.0" && git push origin CDT_LSP_1_0_0`
- [ ] [Promote a cdt build from jenkins](https://ci.eclipse.org/cdt/job/promote-a-build/) to releases
- [ ] Add description to the promote-a-build job and the job it promoted.
- [ ] Update or create [composites](https://github.com/eclipse-cdt/cdt/tree/main/releng/download/releases) in preparation for going public on release day
  - [ ] Include the update to latest URL https://download.eclipse.org/tools/cdt/releases/cdt-lsp-latest to point to latest release
  - See https://github.com/eclipse-cdt/cdt/pull/1136 for a past of example of what needs to be done
- [ ] Test the build (before making it visible on the next step).
      In addition to normal smoke tests, make sure that updates work by adding the new p2 URL to available update sites in an instance of the last released Eclipse for C/C++ Developers and running Check for Updates. Since the composites have not been published yet, you need to add the p2 URL for the specific build, e.g. https://download.eclipse.org/tools/cdt/releases/cdt-lsp-3.1/cdt-lsp-3.1.0/
- [ ] Run [the job](https://ci.eclipse.org/cdt/job/promote-files-to-download/) that updates the composites on https://download.eclipse.org/tools/cdt/releases/
- [ ] Test the Check for Updates on a clean install to make sure that the https://download.eclipse.org/tools/cdt/releases/cdt-lsp-latest/ URL works.
      Caching on download.eclipse.org and p2 within Eclipse can sometimes really interfere at this stage as you can get different results to the web browser than in Eclipse, leading to confusion such as seeing the correct values in the browser, but no updates available in the IDE. Try leaving it for 30 minutes and restart Eclipse to see if it resolves itself, if it doesn't you may need to raised a [IT helpdesk ticket](https://gitlab.eclipse.org/eclipsefdn/helpdesk/-/issues/new) for assistance.
- [ ] Update the SimRel cdt.aggrcon file if appropriate. See [this PR](https://github.com/eclipse-simrel/simrel.build/pull/813) for a past example and https://github.com/eclipse-simrel/ for instructions on contributing to SimRel.
- [ ] Unmark as keep all old Milestone and RC jobs
- [ ] Create a [release page on github](https://github.com/eclipse-cdt/cdt-lsp/releases/new) using past releases pages as a guide of what to include.
    - Check all the links in the release page to make sure they work
- [ ] Check the "Create a discussion for this release" and click Publish the GitHub release page
- [ ] Forward the GitHub release page email to cdt-dev
