package pl.mjedynak.idea.plugins.builder.writer;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import pl.mjedynak.idea.plugins.builder.psi.model.PsiFieldsForBuilder;

public class BuilderContext {

    private final Project project;
    private final PsiFieldsForBuilder psiFieldsForBuilder;
    private final PsiDirectory targetDirectory;
    private final String className;
    private final PsiClass psiClassFromEditor;
    private final String methodPrefix;
    private final boolean isInner;
    private final boolean hasButMethod;
    private final boolean hasFromMethod;
    private final boolean hasBuilderMethodInSourceClass;
    private final boolean createPrivateConstructor;
    private final boolean createGetter;
    private final boolean createToBuilder;

    private final boolean useSingleField;

    public BuilderContext(Project project, PsiFieldsForBuilder psiFieldsForBuilder,
                          PsiDirectory targetDirectory, String className, PsiClass psiClassFromEditor,
                          String methodPrefix, boolean isInner, boolean hasButMethod, boolean useSingleField,
                          boolean hasFromMethod,
                          boolean hasBuilderMethodInSourceClass, boolean createPrivateConstructor, boolean createGetter,
                          boolean createToBuilder) {
        this.project = project;
        this.psiFieldsForBuilder = psiFieldsForBuilder;
        this.targetDirectory = targetDirectory;
        this.className = className;
        this.psiClassFromEditor = psiClassFromEditor;
        this.methodPrefix = methodPrefix;
        this.isInner = isInner;
        this.hasButMethod = hasButMethod;
        this.hasFromMethod = hasFromMethod;
        this.hasBuilderMethodInSourceClass = hasBuilderMethodInSourceClass;
        this.createPrivateConstructor = createPrivateConstructor;
        this.createGetter = createGetter;
        this.createToBuilder = createToBuilder;
        this.useSingleField = useSingleField;
    }

    public Project getProject() {
        return project;
    }

    public PsiFieldsForBuilder getPsiFieldsForBuilder() {
        return psiFieldsForBuilder;
    }

    public PsiDirectory getTargetDirectory() {
        return targetDirectory;
    }

    public String getClassName() {
        return className;
    }

    public PsiClass getPsiClassFromEditor() {
        return psiClassFromEditor;
    }

    public String getMethodPrefix() {
        return methodPrefix;
    }

    public boolean isInner() {
        return isInner;
    }

    public boolean hasButMethod() {
        return hasButMethod;
    }

    public boolean hasFromMethod() {
        return hasFromMethod;
    }

    public boolean hasBuilderMethodInSourceClass() {
        return hasBuilderMethodInSourceClass;
    }

    public boolean isCreatePrivateConstructor() {
        return createPrivateConstructor;
    }

    public boolean isCreateGetter() {
        return createGetter;
    }

    public boolean isCreateToBuilder() {
        return createToBuilder;
    }

    public boolean useSingleField() {
        return useSingleField;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(project, psiFieldsForBuilder, targetDirectory, className, psiClassFromEditor, methodPrefix,
                createPrivateConstructor, createGetter, createToBuilder, useSingleField);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BuilderContext other = (BuilderContext) obj;
        return Objects.equal(this.project, other.project)
                && Objects.equal(this.psiFieldsForBuilder, other.psiFieldsForBuilder)
                && Objects.equal(this.targetDirectory, other.targetDirectory)
                && Objects.equal(this.className, other.className)
                && Objects.equal(this.psiClassFromEditor, other.psiClassFromEditor)
                && Objects.equal(this.methodPrefix, other.methodPrefix)
                && Objects.equal(this.createPrivateConstructor, other.createPrivateConstructor)
                && Objects.equal(this.createGetter, other.createGetter)
                && Objects.equal(this.createToBuilder, other.createToBuilder)
                && Objects.equal(this.useSingleField, other.useSingleField);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("project", project)
                .add("psiFieldsForBuilder", psiFieldsForBuilder)
                .add("targetDirectory", targetDirectory)
                .add("className", className)
                .add("psiClassFromEditor", psiClassFromEditor)
                .add("methodPrefix", methodPrefix)
                .add("isInner", isInner)
                .add("hasButMethod", hasButMethod)
                .add("hasFromMethod", hasFromMethod)
                .add("hasBuilderMethodInSourceClass", hasBuilderMethodInSourceClass)
                .add("createPrivateConstructor", createPrivateConstructor)
                .add("createGetter", createGetter)
                .add("createToBuilder", createToBuilder)
                .add("useSingleField", useSingleField)
                .toString();
    }
}
