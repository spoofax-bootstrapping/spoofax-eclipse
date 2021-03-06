package org.metaborg.spoofax.eclipse.meta.wizard;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.project.ProjectException;
import org.metaborg.spoofax.eclipse.meta.SpoofaxMetaPlugin;
import org.metaborg.spoofax.eclipse.meta.nature.SpoofaxMetaNature;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.metaborg.spoofax.eclipse.util.Nullable;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfigBuilder;
import org.metaborg.spoofax.meta.core.generator.eclipse.EclipseLangSpecGenerator;
import org.metaborg.spoofax.meta.core.generator.language.AnalysisType;
import org.metaborg.spoofax.meta.core.generator.language.ContinuousLanguageSpecGenerator;
import org.metaborg.spoofax.meta.core.generator.language.LanguageSpecGenerator;
import org.metaborg.spoofax.meta.core.generator.language.LanguageSpecGeneratorSettings;
import org.metaborg.spoofax.meta.core.generator.language.LanguageSpecGeneratorSettingsBuilder;
import org.metaborg.spoofax.meta.core.generator.language.SyntaxType;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.inject.Injector;

public class GenerateLanguageProjectWizard extends Wizard implements INewWizard {
    private static final ILogger logger = LoggerUtils.logger(GenerateLanguageProjectWizard.class);

    private final IEclipseResourceService resourceService;
    private final ISpoofaxLanguageSpecConfigBuilder configBuilder;

    private final GenerateLanguageProjectWizardPage page;


    public GenerateLanguageProjectWizard() {
        final Injector injector = SpoofaxMetaPlugin.injector();

        this.resourceService = injector.getInstance(IEclipseResourceService.class);
        this.configBuilder = injector.getInstance(ISpoofaxLanguageSpecConfigBuilder.class);

        this.page = new GenerateLanguageProjectWizardPage();

        addPage(this.page);
        setNeedsProgressMonitor(true);
    }


    @Override public void init(IWorkbench workbench, IStructuredSelection selection) {
    }


    @Override public boolean performFinish() {
        final IProject project = page.getProjectHandle();
        final URI projectLocation;
        if(page.useDefaults()) {
            projectLocation = null;
        } else {
            projectLocation = page.getLocationURI();
        }
        final String languageName = page.languageName();
        final LanguageIdentifier languageId = page.languageIdentifier();
        final Collection<String> extensions = page.extensions();
        final SyntaxType syntaxType = page.syntaxType();
        final AnalysisType analysisType = page.analysisType();

        final IRunnableWithProgress runnable = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                try {
                    createProject(monitor, languageId, languageName, extensions, syntaxType, analysisType, project,
                        projectLocation);
                } catch(Throwable e) {
                    throw new InvocationTargetException(e);
                } finally {
                    monitor.done();
                }
            }
        };

        try {
            getContainer().run(true, true, runnable);
        } catch(InterruptedException e) {
            return false;
        } catch(InvocationTargetException e) {
            final Throwable t = e.getTargetException();
            logger.error("Generating project failed", t);
            MessageDialog.openError(getShell(), "Error: " + t.getClass().getName(), t.getMessage());
            return false;
        }
        return true;
    }

    private void createProject(IProgressMonitor rootMonitor, LanguageIdentifier languageId, String languageName,
        Collection<String> extensions, SyntaxType syntaxType, AnalysisType analysisType, IProject eclipseProject,
        @Nullable URI projectLocation) throws ProjectException, IOException, CoreException {
        final SubMonitor monitor = SubMonitor.convert(rootMonitor, "Creating language specification project", 20);

        monitor.subTask("Creating project");
        if(projectLocation != null) {
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            final IProjectDescription description = workspace.newProjectDescription(eclipseProject.getName());
            description.setLocationURI(projectLocation);
            eclipseProject.create(description, monitor.newChild(1));
        } else {
            eclipseProject.create(monitor.newChild(1));
        }

        monitor.subTask("Opening project");
        eclipseProject.open(monitor.newChild(1));

        final FileObject location = resourceService.resolve(eclipseProject);

        // @formatter:off
        final LanguageSpecGeneratorSettingsBuilder settingsBuilder = new LanguageSpecGeneratorSettingsBuilder()
            .withGroupId(languageId.groupId)
            .withId(languageId.id)
            .withVersion(languageId.version)
            .withName(languageName)
            .withExtensions(extensions)
            .withSyntaxType(syntaxType)
            .withAnalysisType(analysisType)
            ;
        // @formatter:on

        final LanguageSpecGeneratorSettings settings = settingsBuilder.build(location, configBuilder);
        final LanguageSpecGenerator newGenerator = new LanguageSpecGenerator(settings);
        newGenerator.generateAll();
        final ContinuousLanguageSpecGenerator generator =
            new ContinuousLanguageSpecGenerator(settings.generatorSettings);
        generator.generateAll();
        final EclipseLangSpecGenerator eclipseGenerator = new EclipseLangSpecGenerator(settings.generatorSettings);
        eclipseGenerator.generateAll();

        monitor.subTask("Refreshing project");
        eclipseProject.refreshLocal(IResource.DEPTH_INFINITE, monitor.newChild(2));

        monitor.subTask("Adding Spoofax nature");
        SpoofaxMetaNature.add(eclipseProject, monitor.newChild(1));

        monitor.subTask("Building project");
        eclipseProject.build(IncrementalProjectBuilder.FULL_BUILD, monitor.newChild(15));

        rootMonitor.done();
    }
}
