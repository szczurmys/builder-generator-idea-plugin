package pl.mjedynak.idea.plugins.builder.action.handler;

import com.intellij.codeInsight.generation.PsiElementClassMember;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiPackage;
import pl.mjedynak.idea.plugins.builder.config.DialogConfig;
import pl.mjedynak.idea.plugins.builder.factory.CreateBuilderDialogFactory;
import pl.mjedynak.idea.plugins.builder.factory.MemberChooser;
import pl.mjedynak.idea.plugins.builder.factory.MemberChooserDialogFactory;
import pl.mjedynak.idea.plugins.builder.factory.PsiFieldsForBuilderFactory;
import pl.mjedynak.idea.plugins.builder.gui.CreateBuilderDialog;
import pl.mjedynak.idea.plugins.builder.psi.PsiFieldSelector;
import pl.mjedynak.idea.plugins.builder.psi.PsiHelper;
import pl.mjedynak.idea.plugins.builder.psi.model.PsiFieldsForBuilder;
import pl.mjedynak.idea.plugins.builder.writer.BuilderContext;
import pl.mjedynak.idea.plugins.builder.writer.BuilderWriter;

import java.util.List;

public class DisplayChoosers {

    private PsiClass psiClassFromEditor;
    private Project project;
    private Editor editor;
    private PsiHelper psiHelper;
    private CreateBuilderDialogFactory createBuilderDialogFactory;
    private PsiFieldSelector psiFieldSelector;
    private MemberChooserDialogFactory memberChooserDialogFactory;
    private BuilderWriter builderWriter;
    private PsiFieldsForBuilderFactory psiFieldsForBuilderFactory;

    public DisplayChoosers(PsiHelper psiHelper, CreateBuilderDialogFactory createBuilderDialogFactory,
                           PsiFieldSelector psiFieldSelector, MemberChooserDialogFactory memberChooserDialogFactory,
                           BuilderWriter builderWriter, PsiFieldsForBuilderFactory psiFieldsForBuilderFactory) {
        this.psiHelper = psiHelper;
        this.createBuilderDialogFactory = createBuilderDialogFactory;
        this.psiFieldSelector = psiFieldSelector;
        this.memberChooserDialogFactory = memberChooserDialogFactory;
        this.builderWriter = builderWriter;
        this.psiFieldsForBuilderFactory = psiFieldsForBuilderFactory;
    }

    @SuppressWarnings("rawtypes")
    public void run(PsiClass existingBuilder) {
        PsiDirectory srcDir = psiHelper.getPsiFileFromEditor(editor, project).getContainingDirectory();
        PsiPackage srcPackage = psiHelper.getPackage(srcDir);
        CreateBuilderDialog createBuilderDialog = showDialog(existingBuilder, srcPackage);
        if (createBuilderDialog.isOK()) {
            PsiDirectory targetDirectory = createBuilderDialog.getTargetDirectory();
            String className = createBuilderDialog.getClassName();
            String methodPrefix = createBuilderDialog.getMethodPrefix();
            boolean innerBuilder = createBuilderDialog.isInnerBuilder();
            boolean useSingleField = createBuilderDialog.useSingleField();
            boolean hasButMethod = createBuilderDialog.hasButMethod();
            List<PsiElementClassMember> fieldsToDisplay = getFieldsToIncludeInBuilder(psiClassFromEditor, innerBuilder, useSingleField, hasButMethod);
            MemberChooser<PsiElementClassMember> memberChooserDialog = memberChooserDialogFactory.getMemberChooserDialog(fieldsToDisplay, project);
            memberChooserDialog.show();
            writeBuilderIfNecessary(targetDirectory, className,
                    methodPrefix, memberChooserDialog,
                    createBuilderDialog, existingBuilder, srcPackage);
        }
    }

    @SuppressWarnings("rawtypes")
    private void writeBuilderIfNecessary(PsiDirectory targetDirectory,
                                         String className,
                                         String methodPrefix,
                                         MemberChooser<PsiElementClassMember> memberChooserDialog,
                                         CreateBuilderDialog createBuilderDialog,
                                         PsiClass existingBuilder,
                                         PsiPackage srcPackage) {
        if (memberChooserDialog.isOK()) {
            List<PsiElementClassMember> selectedElements = memberChooserDialog.getSelectedElements();
            PsiFieldsForBuilder psiFieldsForBuilder = psiFieldsForBuilderFactory.createPsiFieldsForBuilder(selectedElements, psiClassFromEditor);
            BuilderContext context = new BuilderContext(
                    project, psiFieldsForBuilder, targetDirectory,
                    className, psiClassFromEditor, methodPrefix,
                    createBuilderDialog.isInnerBuilder(), createBuilderDialog.hasButMethod(),
                    createBuilderDialog.useSingleField(),
                    createBuilderDialog.hasFromMethod(),
                    createBuilderDialog.hasBuilderMethodInSourceClass(),
                    createBuilderDialog.isCreatePrivateConstructor(),
                    createBuilderDialog.isCreateGetter(),
                    createBuilderDialog.isCreateToBuilder());
            builderWriter.writeBuilder(context, existingBuilder);
            saveConfig(context, srcPackage);
        }
    }

    private void saveConfig(BuilderContext builderContext, PsiPackage srcPackage) {
        DialogConfig dialogConfig = createBuilderDialogFactory.getDialogConfig();

        dialogConfig.setButMethod(builderContext.hasButMethod());
        dialogConfig.setInnerBuilder(builderContext.isInner());
        dialogConfig.setFromMethod(builderContext.hasFromMethod());
        dialogConfig.setBuilderMethodInSourceClass(builderContext.hasBuilderMethodInSourceClass());

        dialogConfig.setCreatePrivateConstructor(builderContext.isCreatePrivateConstructor());
        dialogConfig.setCreateGetter(builderContext.isCreateGetter());
        dialogConfig.setCreateToBuilder(builderContext.isCreateToBuilder());

        dialogConfig.setUseSingleField(builderContext.useSingleField());

        dialogConfig.setSimpleBuilderName("Builder".equals(builderContext.getClassName()));

        if (!builderContext.isInner()) {
            PsiDirectory builderTargetDirectory = builderContext.getTargetDirectory();
            PsiPackage builderPackage = builderTargetDirectory == null ? null : psiHelper.getPackage(builderTargetDirectory);
            String builderPackageName = builderPackage == null ? "" : builderPackage.getQualifiedName();
            String sourcePackageName = srcPackage == null ? "" : srcPackage.getQualifiedName();
            if (builderPackageName != null
                    && !builderPackageName.isEmpty()
                    && builderPackageName.startsWith(sourcePackageName)) {
                String suffix = builderPackageName.substring(sourcePackageName.length(), builderPackageName.length());
                if (!suffix.equals(".")) {
                    dialogConfig.setTargetPackageSuffix(suffix);
                }
            }
        }
    }

    private CreateBuilderDialog showDialog(PsiClass existingBuilder, PsiPackage srcPackage) {
        CreateBuilderDialog dialog = createBuilderDialogFactory.createBuilderDialog(psiClassFromEditor, project, srcPackage, existingBuilder);
        dialog.show();
        return dialog;
    }

    @SuppressWarnings("rawtypes")
    private List<PsiElementClassMember> getFieldsToIncludeInBuilder(PsiClass clazz, boolean innerBuilder, boolean useSingleField, boolean hasButMethod) {
        return psiFieldSelector.selectFieldsToIncludeInBuilder(clazz, innerBuilder, useSingleField, hasButMethod);
    }

    public void setPsiClassFromEditor(PsiClass psiClassFromEditor) {
        this.psiClassFromEditor = psiClassFromEditor;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setEditor(Editor editor) {
        this.editor = editor;
    }
}
