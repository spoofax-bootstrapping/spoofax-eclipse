package org.metaborg.spoofax.eclipse.meta.build;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.eclipse.job.GlobalSchedulingRules;
import org.metaborg.spoofax.eclipse.meta.SpoofaxMetaPlugin;
import org.metaborg.spoofax.eclipse.meta.language.LoadLanguageJob;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.metaborg.spoofax.meta.core.SpoofaxMetaBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

public class SpoofaxPostJavaMetaProjectBuilder extends IncrementalProjectBuilder {
    public static final String id = SpoofaxMetaPlugin.id + ".builder.postjava";

    private static final Logger logger = LoggerFactory.getLogger(SpoofaxPostJavaMetaProjectBuilder.class);

    private final IEclipseResourceService resourceService;
    private final ILanguageDiscoveryService languageDiscoveryService;

    private final GlobalSchedulingRules globalSchedulingRules;

    private final SpoofaxMetaBuilder builder;


    public SpoofaxPostJavaMetaProjectBuilder() {
        final Injector injector = SpoofaxMetaPlugin.injector();
        this.resourceService = injector.getInstance(IEclipseResourceService.class);
        this.languageDiscoveryService = injector.getInstance(ILanguageDiscoveryService.class);
        this.globalSchedulingRules = injector.getInstance(GlobalSchedulingRules.class);
        this.builder = injector.getInstance(SpoofaxMetaBuilder.class);
    }


    @Override protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor)
        throws CoreException {
        try {
            if(kind != AUTO_BUILD) {
                build(getProject(), monitor);
            }
        } catch(IOException e) {
            logger.error("Cannot build language project", e);
        } finally {
            // Always forget last build state to force a full build next time.
            forgetLastBuiltState();
        }
        return null;
    }

    @Override protected void clean(IProgressMonitor monitor) throws CoreException {
        try {
            clean(getProject(), monitor);
        } catch(IOException e) {
            logger.error("Cannot clean language project", e);
        } finally {
            // Always forget last build state to force a full build next time.
            forgetLastBuiltState();
        }
    }

    private void clean(IProject project, IProgressMonitor monitor) throws CoreException, IOException {
        logger.debug("Cleaning language project {}", project);
    }

    private void build(IProject project, IProgressMonitor monitor) throws CoreException, IOException {
        logger.debug("Building language project {}", project);


        final FileObject projectResource = resourceService.resolve(project);
        final Job languageLoadJob = new LoadLanguageJob(languageDiscoveryService, projectResource);
        languageLoadJob.setRule(new MultiRule(new ISchedulingRule[] { globalSchedulingRules.startupReadLock(),
            globalSchedulingRules.languageServiceLock() }));
        languageLoadJob.schedule();
    }
}
