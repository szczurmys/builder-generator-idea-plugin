package pl.mjedynak.idea.plugins.builder.psi;

import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import org.apache.commons.lang.StringUtils;
import pl.mjedynak.idea.plugins.builder.settings.CodeStyleSettings;
import pl.mjedynak.idea.plugins.builder.writer.BuilderContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BuilderPsiClassBuilder {

    private static final String PRIVATE_STRING = "private";
    private static final String SPACE = " ";
    private static final String SEMICOLON = ",";
    static final String STATIC_MODIFIER = "static";
    static final String FINAL_MODIFIER = "final";

    private PsiHelper psiHelper = new PsiHelper();
    private PsiFieldsModifier psiFieldsModifier = new PsiFieldsModifier();
    private CodeStyleSettings codeStyleSettings = new CodeStyleSettings();
    private ButMethodCreator butMethodCreator;
    private FromMethodCreator fromMethodCreator;
    private MethodCreator methodCreator;

    private PsiClass srcClass = null;
    private String builderClassName = null;

    private List<PsiField> psiFieldsForSetters = null;
    private List<PsiField> psiFieldsForConstructor = null;
    private List<PsiField> allSelectedPsiFields = null;

    private PsiClass builderClass = null;
    private PsiElementFactory elementFactory = null;
    private String srcClassName = null;
    private String srcClassFieldName = null;

    public BuilderPsiClassBuilder aBuilder(BuilderContext context) {
        initializeFields(context);
        JavaDirectoryService javaDirectoryService = psiHelper.getJavaDirectoryService();
        builderClass = javaDirectoryService.createClass(context.getTargetDirectory(), builderClassName);
        PsiModifierList modifierList = builderClass.getModifierList();
        modifierList.setModifierProperty(FINAL_MODIFIER, true);
        return this;
    }

    public BuilderPsiClassBuilder anInnerBuilder(BuilderContext context) {
        initializeFields(context);
        builderClass = elementFactory.createClass(builderClassName);
        PsiModifierList modifierList = builderClass.getModifierList();
        modifierList.setModifierProperty(FINAL_MODIFIER, true);
        modifierList.setModifierProperty(STATIC_MODIFIER, true);
        return this;
    }

    private void initializeFields(BuilderContext context) {
        JavaPsiFacade javaPsiFacade = psiHelper.getJavaPsiFacade(context.getProject());
        elementFactory = javaPsiFacade.getElementFactory();
        srcClass = context.getPsiClassFromEditor();
        builderClassName = context.getClassName();
        srcClassName = context.getPsiClassFromEditor().getName();
        srcClassFieldName = StringUtils.uncapitalize(srcClassName);
        psiFieldsForSetters = context.getPsiFieldsForBuilder().getFieldsForSetters();
        psiFieldsForConstructor = context.getPsiFieldsForBuilder().getFieldsForConstructor();
        allSelectedPsiFields = context.getPsiFieldsForBuilder().getAllSelectedFields();
        methodCreator = new MethodCreator(elementFactory, builderClassName);
        butMethodCreator = new ButMethodCreator(elementFactory);
        fromMethodCreator = new FromMethodCreator(elementFactory);
    }

    public BuilderPsiClassBuilder withFields() {
        if (isInnerBuilder(builderClass)) {
            psiFieldsModifier.modifyFieldsForInnerClass(allSelectedPsiFields, builderClass);
        } else {
            psiFieldsModifier.modifyFields(psiFieldsForSetters, psiFieldsForConstructor, builderClass);
        }
        return this;
    }

    public BuilderPsiClassBuilder withPrivateConstructor() {
        PsiMethod constructor = elementFactory.createConstructor();
        constructor.getModifierList().setModifierProperty(PRIVATE_STRING, true);
        builderClass.add(constructor);
        return this;
    }

    public BuilderPsiClassBuilder withInitializingMethod(boolean inSrcClass) {
        PsiMethod staticMethod = elementFactory.createMethodFromText(
                "public static " + builderClassName + " builder() { return new " + builderClassName + "();}", srcClass);
        if(inSrcClass) {
            srcClass.add(staticMethod);
        } else {
            builderClass.add(staticMethod);
        }
        return this;
    }

    public BuilderPsiClassBuilder withToBuilderMethod(boolean initializerIsInSrcClass) {

        String initializerBaseClass = "";
        if(!initializerIsInSrcClass) {
            initializerBaseClass = builderClassName + ".";
        }

        PsiMethod toBuilderMethod = elementFactory.createMethodFromText(
        "public " + builderClassName + " toBuilder() { return " + initializerBaseClass + "builder().from(this);}", srcClass);

        srcClass.add(toBuilderMethod);

        return this;
    }

    public BuilderPsiClassBuilder withSetMethods(String methodPrefix) {
        if (isInnerBuilder(builderClass)) {
            for (PsiField psiFieldForAssignment : allSelectedPsiFields) {
                createAndAddMethod(psiFieldForAssignment, methodPrefix);
            }
        } else {
            for (PsiField psiFieldForSetter : psiFieldsForSetters) {
                createAndAddMethod(psiFieldForSetter, methodPrefix);
            }
            for (PsiField psiFieldForConstructor : psiFieldsForConstructor) {
                createAndAddMethod(psiFieldForConstructor, methodPrefix);
            }
        }
        return this;
    }

    private boolean isInnerBuilder(PsiClass aClass) {
        return aClass.hasModifierProperty("static");
    }

    public BuilderPsiClassBuilder withButMethod() {
        PsiMethod method = butMethodCreator.butMethod(builderClassName, builderClass, srcClass);
        builderClass.add(method);
        return this;
    }

    public BuilderPsiClassBuilder withFromMethod(boolean innerBuilder) {
        PsiMethod method = fromMethodCreator.fromMethod(builderClassName, allSelectedPsiFields, srcClass, innerBuilder);
        builderClass.add(method);
        return this;
    }

    public BuilderPsiClassBuilder withPrivateConstructorInSourceClass() {
        PsiMethod[] constructors = srcClass.getConstructors();
        for(PsiMethod method : constructors) {
            if(method.getParameterList().getParametersCount() == 0) {
                return this;
            }
        }
        PsiMethod constructor = elementFactory.createConstructor();
        constructor.getModifierList().setModifierProperty(PRIVATE_STRING, true);
        srcClass.add(constructor);
        return this;
    }

    public BuilderPsiClassBuilder withGetterInSourceClass() {
        String methodFormat = "public %s %s() { return %s; }";
        for(PsiField psiField : allSelectedPsiFields) {
            String type = psiField.getType().getCanonicalText();
            String fieldName = psiField.getName();
            String methodName = getGetterName(type, fieldName);
            if(!containsGetter(methodName)) {
                String method = String.format(methodFormat, type, methodName, fieldName);
                PsiMethod psiMethod = elementFactory.createMethodFromText(method, srcClass);
                srcClass.add(psiMethod);
            }
        }
        return this;
    }
    private boolean containsGetter(String methodName) {
        PsiMethod[] methodsByName = srcClass.findMethodsByName(methodName, true);
        for(PsiMethod  method : methodsByName) {
            if(method.getParameterList().getParametersCount() == 0) {
                return true;
            }
        }
        return false;
    }

    private String getGetterName(String type, String fieldName) {
        String prefix = "get";
        if(type.equals(boolean.class.getCanonicalName())) {
            prefix = "is";
        }
        return prefix + upperFirstLetter(fieldName);
    }

    private String upperFirstLetter(String name) {
        if(name.length() <= 1) {
            return name.toUpperCase();
        }
        return name.substring(0, 1).toUpperCase()
                + name.substring(1, name.length());
    }


    private void createAndAddMethod(PsiField psiField, String methodPrefix) {
        builderClass.add(methodCreator.createMethod(psiField, methodPrefix));
    }

    public PsiClass build() {
        StringBuilder buildMethodText = new StringBuilder();
        appendConstructor(buildMethodText);
        appendSetMethodsOrAssignments(buildMethodText);
        buildMethodText.append("return ").append(srcClassFieldName).append(";}");
        PsiMethod buildMethod = elementFactory.createMethodFromText(buildMethodText.toString(), srcClass);
        builderClass.add(buildMethod);
        return builderClass;
    }

    private void appendConstructor(StringBuilder buildMethodText) {
        String constructorParameters = createConstructorParameters();
        buildMethodText.append("public ").append(srcClassName).append(" build() { ").append(srcClassName).append(SPACE)
                .append(srcClassFieldName).append(" = new ").append(srcClassName).append("(").append(constructorParameters).append(");");
    }

    private void appendSetMethodsOrAssignments(StringBuilder buildMethodText) {
        appendSetMethods(buildMethodText, psiFieldsForSetters);
        if (isInnerBuilder(builderClass)) {
            Set<PsiField> fieldsSetViaAssignment = new HashSet<PsiField>(allSelectedPsiFields);
            fieldsSetViaAssignment.removeAll(psiFieldsForSetters);
            fieldsSetViaAssignment.removeAll(psiFieldsForConstructor);
            appendAssignments(buildMethodText, fieldsSetViaAssignment);
        }
    }

    private void appendSetMethods(StringBuilder buildMethodText, Collection<PsiField> fieldsBeSetViaSetter) {
        for (PsiField psiFieldsForSetter : fieldsBeSetViaSetter) {
            String fieldNamePrefix = codeStyleSettings.getFieldNamePrefix();
            String fieldName = psiFieldsForSetter.getName();
            String fieldNameWithoutPrefix = fieldName.replaceFirst(fieldNamePrefix, "");
            String fieldNameUppercase = StringUtils.capitalize(fieldNameWithoutPrefix);
            buildMethodText.append(srcClassFieldName).append(".set").append(fieldNameUppercase).append("(").append(fieldName).append(");");
        }
    }

    private void appendAssignments(StringBuilder buildMethodText, Collection<PsiField> fieldsSetViaAssignment) {
        for (PsiField field : fieldsSetViaAssignment) {
            buildMethodText.append(srcClassFieldName).append(".")
                    .append(field.getName()).append("=").append("this.")
                    .append(field.getName()).append(";");
        }
    }

    private String createConstructorParameters() {
        StringBuilder sb = new StringBuilder();
        for (PsiField psiField : psiFieldsForConstructor) {
            sb.append(psiField.getName()).append(SEMICOLON);
        }
        removeLastSemicolon(sb);
        return sb.toString();
    }

    private void removeLastSemicolon(StringBuilder sb) {
        if (sb.toString().endsWith(SEMICOLON)) {
            sb.deleteCharAt(sb.length() - 1);
        }
    }
}
