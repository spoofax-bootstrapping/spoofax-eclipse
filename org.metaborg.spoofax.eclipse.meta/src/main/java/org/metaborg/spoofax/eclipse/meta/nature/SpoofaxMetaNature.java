package org.metaborg.spoofax.eclipse.meta.nature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.metaborg.spoofax.eclipse.build.SpoofaxProjectBuilder;
import org.metaborg.spoofax.eclipse.meta.SpoofaxMetaPlugin;
import org.metaborg.spoofax.eclipse.meta.build.GenerateSourcesBuilder;
import org.metaborg.spoofax.eclipse.meta.build.PackageBuilder;
import org.metaborg.spoofax.eclipse.meta.build.CompileBuilder;
import org.metaborg.spoofax.eclipse.nature.SpoofaxNature;
import org.metaborg.spoofax.eclipse.util.BuilderUtils;
import org.metaborg.spoofax.eclipse.util.NatureUtils;
import org.metaborg.spoofax.eclipse.util.Nullable;

public class SpoofaxMetaNature implements IProjectNature {
    public static final String id = SpoofaxMetaPlugin.id + ".nature";

    private static final String javaNatureId = "org.eclipse.jdt.core.javanature";
    private static final String javaBuilderId = "org.eclipse.jdt.core.javabuilder";
    private static final String mavenNatureId = "org.eclipse.m2e.core.maven2Nature";
    private static final String mavenBuilderId = "org.eclipse.m2e.core.maven2Builder";

    private IProject project;


    @Override public void configure() throws CoreException {
        BuilderUtils.append(GenerateSourcesBuilder.id, project, null, IncrementalProjectBuilder.FULL_BUILD,
            IncrementalProjectBuilder.CLEAN_BUILD);
        BuilderUtils.append(CompileBuilder.id, project, null, IncrementalProjectBuilder.FULL_BUILD,
            IncrementalProjectBuilder.CLEAN_BUILD);
        BuilderUtils.append(PackageBuilder.id, project, null, IncrementalProjectBuilder.FULL_BUILD,
            IncrementalProjectBuilder.CLEAN_BUILD);
    }

    @Override public void deconfigure() throws CoreException {
        BuilderUtils.removeFrom(GenerateSourcesBuilder.id, project, null);
        BuilderUtils.removeFrom(CompileBuilder.id, project, null);
        BuilderUtils.removeFrom(PackageBuilder.id, project, null);
    }

    @Override public IProject getProject() {
        return project;
    }

    @Override public void setProject(IProject project) {
        this.project = project;
    }


    public static void add(IProject project, @Nullable IProgressMonitor monitor) throws CoreException {
        addDependencyNatures(project, monitor);
        addDependencyBuilders(project, monitor);
        NatureUtils.addTo(id, project, monitor);
        sortBuilders(project, monitor);
    }

    private static void addDependencyNatures(IProject project, @Nullable IProgressMonitor monitor)
        throws CoreException {
        NatureUtils.addTo(mavenNatureId, project, monitor);
        NatureUtils.addTo(javaNatureId, project, monitor);
        NatureUtils.addTo(SpoofaxNature.id, project, monitor);
    }

    private static void addDependencyBuilders(IProject project, @Nullable IProgressMonitor monitor)
        throws CoreException {
        // Ensure the Java and m2e builders are actually there, sometimes they are not, or have been removed by a user.
        BuilderUtils.append(javaBuilderId, project, monitor);
        BuilderUtils.append(mavenBuilderId, project, monitor);
    }

    private static void sortBuilders(IProject project, @Nullable IProgressMonitor monitor) throws CoreException {
        final String[] buildOrder = new String[] { mavenBuilderId, GenerateSourcesBuilder.id, SpoofaxProjectBuilder.id,
            CompileBuilder.id, javaBuilderId, PackageBuilder.id };
        BuilderUtils.sort(project, monitor, buildOrder);
    }

    public static void remove(IProject project, @Nullable IProgressMonitor monitor) throws CoreException {
        NatureUtils.removeFrom(id, project, monitor);
    }
}
