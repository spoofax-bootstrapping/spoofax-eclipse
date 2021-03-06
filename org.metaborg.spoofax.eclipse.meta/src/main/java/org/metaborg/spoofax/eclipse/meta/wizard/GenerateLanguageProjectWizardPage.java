package org.metaborg.spoofax.eclipse.meta.wizard;

import java.util.Collection;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.meta.core.wizard.CreateLanguageSpecWizard;
import org.metaborg.spoofax.meta.core.generator.language.AnalysisType;
import org.metaborg.spoofax.meta.core.generator.language.SyntaxType;
import org.metaborg.spoofax.meta.core.wizard.CreateSpoofaxLanguageSpecWizard;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

public class GenerateLanguageProjectWizardPage extends WizardNewProjectCreationPage {
    private static final ILogger logger = LoggerUtils.logger(GenerateLanguageProjectWizardPage.class);

    private LanguageIdentifierControls languageIdentifierControls;
    private CreateSpoofaxLanguageSpecWizard createLanguageSpecWizard;

    private boolean extensionsModified = false;
    private Text extensionsInput;
    private boolean syntaxTypeModified = false;
    private Combo syntaxTypeInput;
    private boolean analysisTypeModified = false;
    private Combo analysisTypeInput;

    private boolean ignoreEvents = false;


    public GenerateLanguageProjectWizardPage() {
        super("page1");

        setTitle("Create Spoofax language specification project");
        setDescription("This wizard creates a Spoofax language specification project");
    }

    public String languageName() {
        return createLanguageSpecWizard.languageName();
    }

    public LanguageIdentifier languageIdentifier() {
        return createLanguageSpecWizard.languageIdentifier();
    }

    public Collection<String> extensions() {
        return createLanguageSpecWizard.extensions();
    }

    public SyntaxType syntaxType() {
        return createLanguageSpecWizard.syntaxType();
    }

    public AnalysisType analysisType() {
        return createLanguageSpecWizard.analysisType();
    }


    @Override public void createControl(Composite parent) {
        // Create a container for this page.
        final Composite container = new Composite(parent, SWT.NONE);
        final GridLayout layout = new GridLayout();
        container.setLayout(layout);

        // Create new project creation control, with as parent our container.
        super.createControl(container);
        // HACK: fix wrong layout data of the parent control
        getControl().setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

        // Create language identifier controls, with as parent our container.
        languageIdentifierControls = new LanguageIdentifierControls("", "", "", "", container, new ModifyListener() {
            @Override public void modifyText(ModifyEvent e) {
                if(ignoreEvents) {
                    return;
                }

                boolean valid = validatePage();
                setPageComplete(valid);
            }
        });

        // Add extensions, with as parent the language identifier controls.
        new Label(languageIdentifierControls.container, SWT.NONE).setText("&Extensions:");
        extensionsInput = new Text(languageIdentifierControls.container, SWT.BORDER | SWT.SINGLE);
        extensionsInput.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        extensionsInput.addModifyListener(new ModifyListener() {
            @Override public void modifyText(ModifyEvent e) {
                if(ignoreEvents) {
                    return;
                }
                extensionsModified = true;
            }
        });

        // Add language options in a new container
        final Group optionsContainer = new Group(container, SWT.NONE);
        final GridLayout optionsLayout = new GridLayout();
        optionsLayout.numColumns = 2;
        optionsContainer.setLayout(optionsLayout);
        optionsContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        optionsContainer.setText("Language options");

        // Syntax type
        new Label(optionsContainer, SWT.NONE).setText("&Syntax type:");
        syntaxTypeInput = new Combo(optionsContainer, SWT.DROP_DOWN | SWT.BORDER | SWT.SINGLE);
        syntaxTypeInput.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        for(String syntaxType : SyntaxType.mapping().keySet()) {
            syntaxTypeInput.add(syntaxType);
        }
        syntaxTypeInput.addModifyListener(new ModifyListener() {
            @Override public void modifyText(ModifyEvent e) {
                if(ignoreEvents) {
                    return;
                }
                syntaxTypeModified = true;
            }
        });

        // Analysis type
        new Label(optionsContainer, SWT.NONE).setText("&Analysis type:");
        analysisTypeInput = new Combo(optionsContainer, SWT.DROP_DOWN | SWT.BORDER | SWT.SINGLE);
        analysisTypeInput.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        for(String analysisType : AnalysisType.mapping().keySet()) {
            analysisTypeInput.add(analysisType);
        }
        analysisTypeInput.addModifyListener(new ModifyListener() {
            @Override public void modifyText(ModifyEvent e) {
                if(ignoreEvents) {
                    return;
                }
                analysisTypeModified = true;
            }
        });


        // Instantiate and implement 'create language specification' wizard helper.
        createLanguageSpecWizard = new CreateSpoofaxLanguageSpecWizard() {
            @Override protected boolean inputProjectNameModified() {
                // HACK: always return false, let parent control handle validation of project name.
                return false;
            }

            @Override protected String inputProjectName() {
                return getProjectName();
            }

            @Override protected boolean inputNameModified() {
                return languageIdentifierControls.nameModified();
            }

            @Override protected String inputName() {
                return languageIdentifierControls.name();
            }

            @Override protected boolean inputIdModified() {
                return languageIdentifierControls.idModified();
            }

            @Override protected String inputId() {
                return languageIdentifierControls.id();
            }

            @Override protected boolean inputGroupIdModified() {
                return languageIdentifierControls.groupIdModified();
            }

            @Override protected String inputGroupId() {
                return languageIdentifierControls.groupId();
            }

            @Override protected boolean inputVersionModified() {
                return languageIdentifierControls.versionModified();
            }

            @Override protected String inputVersion() {
                return languageIdentifierControls.version();
            }

            @Override protected boolean inputExtensionsModified() {
                return extensionsModified;
            }

            @Override protected String inputExtensions() {
                return extensionsInput.getText();
            }


            @Override protected void setName(String name) {
                setIgnoreEvents(true);
                languageIdentifierControls.inputName.setText(name);
                setIgnoreEvents(false);
            }

            @Override protected void setId(String id) {
                setIgnoreEvents(true);
                languageIdentifierControls.inputId.setText(id);
                setIgnoreEvents(false);
            }

            @Override protected void setGroupId(String groupId) {
                setIgnoreEvents(true);
                languageIdentifierControls.inputGroupId.setText(groupId);
                setIgnoreEvents(false);
            }

            @Override protected void setVersion(String version) {
                setIgnoreEvents(true);
                languageIdentifierControls.inputVersion.setText(version);
                setIgnoreEvents(false);
            }

            @Override protected void setExtensions(String extensions) {
                setIgnoreEvents(true);
                extensionsInput.setText(extensions);
                setIgnoreEvents(false);
            }


            @Override protected boolean inputSyntaxTypeModified() {
                return syntaxTypeModified;
            }

            @Override protected String inputSyntaxTypeString() {
                return syntaxTypeInput.getText();
            }

            @Override protected void setSyntaxType(String syntaxTypeString) {
                syntaxTypeInput.setText(syntaxTypeString);
            }

            @Override protected boolean inputAnalysisTypeModified() {
                return analysisTypeModified;
            }

            @Override protected String inputAnalysisTypeString() {
                return analysisTypeInput.getText();
            }

            @Override protected void setAnalysisType(String analysisTypeString) {
                analysisTypeInput.setText(analysisTypeString);
            }
        };
        createLanguageSpecWizard.setDefaults();

        // Modifying the project name fills in language name, language id, and extensions.
        try {
            // HACK: get project name input field via reflection because it is package private.
            final Text projectNameInput = (Text) FieldUtils.readField(this, "projectNameField", true);
            projectNameInput.addModifyListener(new ModifyListener() {
                @Override public void modifyText(ModifyEvent e) {
                    if(ignoreEvents) {
                        return;
                    }
                    createLanguageSpecWizard.distributeProjectName();
                }
            });
        } catch(IllegalAccessException e) {
            logger.debug(
                "Could not get project name input field via reflection, cannot automatically set names via the project name",
                e);
        }

        // Modifying the language name fills in extensions.
        languageIdentifierControls.inputName.addModifyListener(new ModifyListener() {
            @Override public void modifyText(ModifyEvent e) {
                if(ignoreEvents) {
                    return;
                }
                createLanguageSpecWizard.distributeLanguageName();
            }
        });

        // Set the current control to our container.
        setControl(container);
    }

    @Override protected boolean validatePage() {
        // Validation can be executed before control has been made, check for null to confirm.
        if(createLanguageSpecWizard == null) {
            return super.validatePage();
        }

        final CreateLanguageSpecWizard.ValidationResult result = createLanguageSpecWizard.validate();
        if(result.complete) {
            return super.validatePage();
        }

        if(!result.errors.isEmpty()) {
            final String error = result.errors.get(0);
            setErrorMessage(error);
        } else {
            setErrorMessage(null);
        }

        return false;
    }


    private void setIgnoreEvents(boolean ignoreEvents) {
        this.ignoreEvents = ignoreEvents;
        languageIdentifierControls.setIgnoreEvents(ignoreEvents);
    }
}
