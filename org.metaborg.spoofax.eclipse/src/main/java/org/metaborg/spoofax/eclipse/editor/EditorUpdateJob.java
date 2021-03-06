package org.metaborg.spoofax.eclipse.editor;

import java.util.concurrent.CancellationException;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IEditorInput;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.analysis.IAnalysisService;
import org.metaborg.core.analysis.IAnalyzeResult;
import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.analysis.IAnalyzeUnitUpdate;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.IdentifiedResource;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageFactory;
import org.metaborg.core.messages.MessageType;
import org.metaborg.core.outline.IOutline;
import org.metaborg.core.outline.IOutlineService;
import org.metaborg.core.processing.analyze.IAnalysisResultUpdater;
import org.metaborg.core.processing.parse.IParseResultUpdater;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.style.ICategorizerService;
import org.metaborg.core.style.IRegionCategory;
import org.metaborg.core.style.IRegionStyle;
import org.metaborg.core.style.IStylerService;
import org.metaborg.core.syntax.IInputUnit;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.core.syntax.ISyntaxService;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.core.unit.IInputUnitService;
import org.metaborg.spoofax.core.style.CategorizerValidator;
import org.metaborg.spoofax.eclipse.job.ThreadKillerJob;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.metaborg.spoofax.eclipse.util.MarkerUtils;
import org.metaborg.spoofax.eclipse.util.Nullable;
import org.metaborg.spoofax.eclipse.util.StatusUtils;
import org.metaborg.util.concurrent.IClosableLock;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.Sets;

public class EditorUpdateJob<I extends IInputUnit, P extends IParseUnit, A extends IAnalyzeUnit, AU extends IAnalyzeUnitUpdate, F>
    extends Job {
    private static final ILogger logger = LoggerUtils.logger(EditorUpdateJob.class);
    private static final long interruptTimeMillis = 5000;
    private static final long killTimeMillis = 10000;

    private final IEclipseResourceService resourceService;
    private final ILanguageIdentifierService languageIdentifierService;
    private final IContextService contextService;
    private final IProjectService projectService;
    private final IInputUnitService<I> unitService;
    private final ISyntaxService<I, P> syntaxService;
    private final IAnalysisService<P, A, AU> analyzer;
    private final ICategorizerService<P, A, F> categorizer;
    private final IStylerService<F> styler;
    private final IOutlineService<P, A> outlineService;
    private final IParseResultUpdater<P> parseResultProcessor;
    private final IAnalysisResultUpdater<P, A> analysisResultProcessor;

    private final IEclipseEditor<F> editor;
    private final IEditorInput input;
    private final @Nullable IResource eclipseResource;
    private final FileObject resource;
    private final String text;
    private final boolean instantaneous;

    private ThreadKillerJob threadKiller;


    public EditorUpdateJob(IEclipseResourceService resourceService,
        ILanguageIdentifierService languageIdentifierService, IContextService contextService,
        IProjectService projectService, IInputUnitService<I> unitService, ISyntaxService<I, P> syntaxService,
        IAnalysisService<P, A, AU> analyzer, ICategorizerService<P, A, F> categorizer, IStylerService<F> styler,
        IOutlineService<P, A> outlineService, IParseResultUpdater<P> parseResultProcessor,
        IAnalysisResultUpdater<P, A> analysisResultProcessor, IEclipseEditor<F> editor, IEditorInput input,
        @Nullable IResource eclipseResource, FileObject resource, String text, boolean instantaneous) {
        super("Updating Spoofax editor for " + resource.toString());
        setPriority(Job.SHORT);

        this.resourceService = resourceService;
        this.languageIdentifierService = languageIdentifierService;
        this.contextService = contextService;
        this.projectService = projectService;
        this.unitService = unitService;
        this.syntaxService = syntaxService;
        this.analyzer = analyzer;
        this.categorizer = categorizer;
        this.styler = styler;
        this.outlineService = outlineService;
        this.parseResultProcessor = parseResultProcessor;
        this.analysisResultProcessor = analysisResultProcessor;

        this.editor = editor;
        this.input = input;
        this.eclipseResource = eclipseResource;
        this.resource = resource;
        this.text = text;
        this.instantaneous = instantaneous;
    }


    @Override public boolean belongsTo(Object family) {
        return input.equals(family);
    }

    @Override protected IStatus run(final IProgressMonitor monitor) {
        logger.debug("Running editor update job for {}", resource);

        final IWorkspace workspace = ResourcesPlugin.getWorkspace();

        try {
            final IStatus status = update(workspace, monitor);
            if(threadKiller != null) {
                threadKiller.cancel();
            }
            return status;
        } catch(MetaborgRuntimeException | MetaborgException | CoreException e) {
            if(threadKiller != null) {
                threadKiller.cancel();
            }
            if(monitor.isCanceled()) {
                return StatusUtils.cancel();
            }

            if(eclipseResource != null) {
                try {
                    final IWorkspaceRunnable parseMarkerUpdater = new IWorkspaceRunnable() {
                        @Override public void run(IProgressMonitor workspaceMonitor) throws CoreException {
                            if(workspaceMonitor.isCanceled())
                                return;
                            MarkerUtils.clearAll(eclipseResource);
                            MarkerUtils.createMarker(eclipseResource,
                                MessageFactory.newErrorAtTop(resource,
                                    "Failed to update editor; see the console or error log for more information",
                                    MessageType.INTERNAL, e));
                        }
                    };
                    workspace.run(parseMarkerUpdater, eclipseResource, IWorkspace.AVOID_UPDATE, monitor);
                } catch(CoreException e2) {
                    final String message = logger.format("Failed to show internal error marker for {}", resource);
                    logger.error(message, e2);
                    return StatusUtils.silentError(message, e2);
                }
            }

            final String message = logger.format("Failed to update editor for {}", resource);
            logger.error(message, e);
            return StatusUtils.silentError(message, e);
        } catch(CancellationException e) {
            return StatusUtils.cancel();
        } catch(ThreadDeath e) {
            throw e;
        } catch(Throwable e) {
            final String message = logger.format("Failed to update editor for {}", resource);
            logger.error(message, e);
            return StatusUtils.silentError(message, e);
        } finally {
            monitor.done();
        }
    }

    @Override protected void canceling() {
        final Thread thread = getThread();
        if(thread == null) {
            return;
        }

        logger.debug("Cancelling editor update job for {}, interrupting in {}ms, killing in {}ms", resource,
            interruptTimeMillis, interruptTimeMillis + killTimeMillis);
        threadKiller = new ThreadKillerJob(thread, killTimeMillis);
        threadKiller.schedule(interruptTimeMillis);
    }


    private IStatus update(IWorkspace workspace, final IProgressMonitor progressMonitor)
        throws MetaborgException, CoreException {
        final SubMonitor monitor = SubMonitor.convert(progressMonitor, 11);

        monitor.subTask("Identifying language");
        final IProject project = projectService.get(resource);
        final IdentifiedResource identified = languageIdentifierService.identifyToResource(resource, project);
        if(identified == null) {
            throw new MetaborgException("Language could not be identified");
        }
        final ILanguageImpl langImpl = identified.language;
        monitor.worked(1);

        if(monitor.isCanceled())
            return StatusUtils.cancel();
        monitor.subTask("Parsing");
        final I input = unitService.inputUnit(resource, text, langImpl, identified.dialect);
        final P parseResult = parse(input);
        monitor.worked(1);

        if(parseResult.valid()) {
            if(monitor.isCanceled())
                return StatusUtils.cancel();
            monitor.subTask("Styling");
            style(monitor, langImpl, parseResult);
            monitor.worked(1);

            if(monitor.isCanceled())
                return StatusUtils.cancel();
            monitor.subTask("Creating outline");
            outline(monitor, langImpl, parseResult);
            monitor.worked(1);
        } else {
            monitor.worked(2);
        }

        // Just parse when eclipse resource is null, skip the rest. Analysis only works with a project context,
        // which is unavailable when the eclipse resource is null.
        if(eclipseResource == null) {
            return StatusUtils.success();
        }

        // Sleep before showing parse messages to prevent showing irrelevant messages while user is still typing.
        if(!instantaneous) {
            try {
                monitor.subTask("Waiting");
                Thread.sleep(300);
            } catch(InterruptedException e) {
                return StatusUtils.cancel();
            }
        }
        monitor.worked(1);

        if(monitor.isCanceled())
            return StatusUtils.cancel();
        monitor.subTask("Processing parse messages");
        parseMessages(workspace, monitor.newChild(1), parseResult);
        monitor.worked(1);

        // Stop if parsing produced an invalid result.
        if(!parseResult.valid()) {
            return StatusUtils.silentError();
        }

        // Stop if context or analysis is unavailable.
        if(!contextService.available(langImpl) || !analyzer.available(langImpl)) {
            return StatusUtils.success();
        }

        // Sleep before analyzing to prevent running many analyses when small edits are made in succession.
        if(!instantaneous) {
            try {
                monitor.subTask("Waiting");
                Thread.sleep(300);
            } catch(InterruptedException e) {
                return StatusUtils.cancel();
            }
        }
        monitor.worked(1);

        if(monitor.isCanceled())
            return StatusUtils.cancel();
        monitor.subTask("Analyzing");
        final IContext context = contextService.get(resource, project, langImpl);
        final IAnalyzeResult<A, AU> analysisResult = analyze(parseResult, context);
        monitor.worked(1);

        if(monitor.isCanceled())
            return StatusUtils.cancel();
        monitor.subTask("Processing analysis messages");
        analysisMessages(workspace, monitor.newChild(1), analysisResult);
        monitor.worked(1);

        return StatusUtils.success();
    }


    private P parse(I input) throws ParseException, ThreadDeath {
        final P parseResult;
        try {
            parseResultProcessor.invalidate(resource);
            parseResult = syntaxService.parse(input);
            parseResultProcessor.update(resource, parseResult);
        } catch(ParseException e) {
            parseResultProcessor.error(resource, e);
            throw e;
        } catch(ThreadDeath e) {
            parseResultProcessor.error(resource, new ParseException(input, "Editor update job killed", e));
            throw e;
        }
        return parseResult;
    }

    private void style(final IProgressMonitor monitor, ILanguageImpl language, P parseResult) {
        final Iterable<IRegionCategory<F>> categories =
            CategorizerValidator.validate(categorizer.categorize(language, parseResult));
        final Iterable<IRegionStyle<F>> styles = styler.styleParsed(language, categories);
        editor.setStyle(styles, text, monitor);
    }

    private void outline(final IProgressMonitor monitor, ILanguageImpl language, P parseResult)
        throws MetaborgException {
        if(!outlineService.available(language)) {
            return;
        }

        final IOutline outline = outlineService.outline(parseResult);
        if(outline == null) {
            return;
        }

        editor.setOutline(outline, monitor);
    }

    private void parseMessages(IWorkspace workspace, IProgressMonitor monitor, final P parseResult)
        throws CoreException {
        // Update markers atomically using a workspace runnable, to prevent flashing/jumping markers.
        final IWorkspaceRunnable parseMarkerUpdater = new IWorkspaceRunnable() {
            @Override public void run(IProgressMonitor workspaceMonitor) throws CoreException {
                if(workspaceMonitor.isCanceled())
                    return;
                MarkerUtils.clearInternal(eclipseResource);
                MarkerUtils.clearParser(eclipseResource);
                for(IMessage message : parseResult.messages()) {
                    MarkerUtils.createMarker(eclipseResource, message);
                }
            }
        };
        workspace.run(parseMarkerUpdater, eclipseResource, IWorkspace.AVOID_UPDATE, monitor);
    }

    private IAnalyzeResult<A, AU> analyze(P parseResult, IContext context) throws AnalysisException, ThreadDeath {
        final IAnalyzeResult<A, AU> analysisResult;
        try(IClosableLock lock = context.write()) {
            analysisResultProcessor.invalidate(parseResult.source());
            try {
                analysisResult = analyzer.analyze(parseResult, context);
            } catch(AnalysisException e) {
                analysisResultProcessor.error(resource, e);
                throw e;
            } catch(ThreadDeath e) {
                analysisResultProcessor.error(resource, new AnalysisException(context, "Editor update job killed", e));
                throw e;
            }
            analysisResultProcessor.update(analysisResult.result(), Sets.<FileName>newHashSet());
        }
        return analysisResult;
    }

    private void analysisMessages(IWorkspace workspace, IProgressMonitor monitor,
        final IAnalyzeResult<A, AU> analysisResult) throws CoreException {
        // Update markers atomically using a workspace runnable, to prevent flashing/jumping markers.
        final IWorkspaceRunnable analysisMarkerUpdater = new IWorkspaceRunnable() {
            @Override public void run(IProgressMonitor workspaceMonitor) throws CoreException {
                if(workspaceMonitor.isCanceled())
                    return;
                MarkerUtils.clearInternal(eclipseResource);
                MarkerUtils.clearAnalysis(eclipseResource);
                for(IMessage message : analysisResult.result().messages()) {
                    if(workspaceMonitor.isCanceled())
                        return;
                    MarkerUtils.createMarker(eclipseResource, message);
                }

                for(AU result : analysisResult.updates()) {
                    if(workspaceMonitor.isCanceled())
                        return;
                    final IResource messagesEclipseResource = resourceService.unresolve(result.source());
                    if(messagesEclipseResource == null) {
                        // In case the analysis sends an update for a resource that is not an eclipse resource; ignore.
                        logger.debug("Cannot perform analysis update for resource {}, it is not an Eclipse resource",
                            result.source());
                        continue;
                    }
                    if(!messagesEclipseResource.exists()) {
                        // In case the analysis sends an update for a resource that does not exist; ignore.
                        logger.debug("Cannot perform analysis update for resource {}, since it does not exist",
                            messagesEclipseResource);
                        continue;
                    }
                    MarkerUtils.clearAnalysis(messagesEclipseResource);
                    for(IMessage message : result.messages()) {
                        if(workspaceMonitor.isCanceled())
                            return;
                        MarkerUtils.createMarker(messagesEclipseResource, message);
                    }
                }
            }
        };
        workspace.run(analysisMarkerUpdater, eclipseResource, IWorkspace.AVOID_UPDATE, monitor);
    }
}
