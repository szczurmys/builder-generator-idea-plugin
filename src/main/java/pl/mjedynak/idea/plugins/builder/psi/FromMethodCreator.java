package pl.mjedynak.idea.plugins.builder.psi;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class FromMethodCreator {
    private static final String FROM_PARAMETER_NAME = "from";

    private PsiElementFactory elementFactory;

    public FromMethodCreator(PsiElementFactory elementFactory) {
        this.elementFactory = elementFactory;
    }

    public PsiMethod fromMethod(String builderClassName, List<PsiField> allSelectedPsiFields, PsiClass srcClass,
                                boolean innerBuilder, boolean useSingleField, String srcClassFieldName) {
        StringBuilder text = new StringBuilder("public " + builderClassName + " from(" + srcClass.getQualifiedName() + " " + FROM_PARAMETER_NAME + ") { ");
        for (PsiField field : allSelectedPsiFields) {
                appendField(text, field, srcClass, innerBuilder, useSingleField, srcClassFieldName);
        }
        text.append("return this;}");
        return elementFactory.createMethodFromText(text.toString(), srcClass);
    }

    private void appendField(StringBuilder text, PsiField field, PsiClass srcClass,
                             boolean innerBuilder, boolean useSingleField, String srcClassFieldName) {

        if(useSingleField) {
            text.append("this.").append(srcClassFieldName)
                    .append(".")
                    .append("set")
                    .append(StringUtils.capitalize(field.getName()))
                    .append("(")
                    .append(FROM_PARAMETER_NAME).append(".").append(getFieldValue(field, srcClass, innerBuilder))
                    .append(");");
        } else {
            text.append("this.").append(field.getName()).append(" = ").append(FROM_PARAMETER_NAME).append(".")
                    .append(getFieldValue(field, srcClass, innerBuilder))
                    .append(";");
        }


    }

    private String getFieldValue(PsiField field, PsiClass srcClass, boolean innerBuilder) {
        if(innerBuilder || isPublic(field)) {
            return field.getName();
        }
        return getGetter(field, srcClass);
    }

    private static boolean isPublic(final PsiMember psiField) {
        return psiField.hasModifierProperty(PsiModifier.PUBLIC);
    }

    private static String getGetter(final PsiField field, final PsiClass srcClass) {
        String fieldNameWithUpperFirstLeter = upperFirstLetter(field.getName());

        PsiMethod[] methods = srcClass.findMethodsByName("get" + fieldNameWithUpperFirstLeter, true);
        for(PsiMethod method : methods) {
            if(isValidGetterMethod(method)) {
                return method.getName() + "()";
            }
        }
        methods = srcClass.findMethodsByName("is" + fieldNameWithUpperFirstLeter, true);
        for(PsiMethod method : methods) {
            if(isValidGetterMethod(method)) {
                return method.getName() + "()";
            }
        }
        methods = srcClass.findMethodsByName(field.getName(), true);
        for(PsiMethod method : methods) {
            if(isValidGetterMethod(method)) {
                return method.getName() + "()";
            }
        }
        return field.getName();
    }

    private static boolean isValidGetterMethod(PsiMethod method) {
        return isPublic(method) && method.getParameterList().getParametersCount() == 0;
    }

    private static String upperFirstLetter(String value) {
        if(value == null) {
            return null;
        }
        if(value.length() <= 1) {
            return value.toUpperCase();
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }
}
