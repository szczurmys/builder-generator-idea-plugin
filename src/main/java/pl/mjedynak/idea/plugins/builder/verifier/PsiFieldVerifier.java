package pl.mjedynak.idea.plugins.builder.verifier;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import org.apache.commons.lang.WordUtils;
import pl.mjedynak.idea.plugins.builder.settings.CodeStyleSettings;

import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang.StringUtils.EMPTY;

public class PsiFieldVerifier {

    static final String PRIVATE_MODIFIER = "private";
    static final String SET_PREFIX = "set";
    static final String[] GET_PREFIX = {"get", "is"};

    private CodeStyleSettings codeStyleSettings = new CodeStyleSettings();

    public boolean isSetInConstructor(PsiField psiField, PsiClass psiClass) {
        boolean result = false;
        PsiMethod[] constructors = psiClass.getConstructors();
        for (int i = 0; i < constructors.length && !result; i++) {
            result = checkConstructor(psiField, constructors[i]);
        }
        return result;
    }

    public boolean checkConstructor(PsiField psiField, PsiMethod constructor) {
        PsiParameterList parameterList = constructor.getParameterList();
        PsiParameter[] parameters = parameterList.getParameters();
        return iterateOverParameters(psiField, parameters);
    }

    private boolean iterateOverParameters(PsiField psiField, PsiParameter[] parameters) {
        boolean result = false;
        for (int i = 0; i < parameters.length && !result; i++) {
            result = checkParameter(psiField, parameters[i]);
        }
        return result;
    }

    private boolean checkParameter(PsiField psiField, PsiParameter parameter) {
        boolean result = false;
        if (areNameAndTypeEqual(psiField, parameter)) {
            result = true;
        }
        return result;
    }

    public boolean areNameAndTypeEqual(PsiField psiField, PsiParameter parameter) {
        String parameterNamePrefix = codeStyleSettings.getParameterNamePrefix();
        String parameterName = parameter.getName();
        String parameterNameWithoutPrefix = parameterName.replace(parameterNamePrefix, "");
        String fieldNamePrefix = codeStyleSettings.getFieldNamePrefix();
        String fieldName = psiField.getName();
        String fieldNameWithoutPrefix = fieldName.replaceFirst(fieldNamePrefix, "");
        return parameterNameWithoutPrefix.equals(fieldNameWithoutPrefix) && parameter.getType().equals(psiField.getType());
    }

    public boolean isSetInSetterMethod(PsiField psiField, PsiClass psiClass) {
        return methodIsNotPrivateAndHasProperPrefixAndProperName(psiField, psiClass, Arrays.asList(SET_PREFIX));
    }

    public boolean hasGetterMethod(PsiField psiField, PsiClass psiClass) {
        return methodIsNotPrivateAndHasProperPrefixAndProperName(psiField, psiClass, Arrays.asList(GET_PREFIX));
    }

    private boolean methodIsNotPrivateAndHasProperPrefixAndProperName(PsiField psiField, PsiClass psiClass, List<String> prefixes) {
        boolean result = false;
        for (PsiMethod method : psiClass.getAllMethods()) {
            if (methodIsNotPrivate(method) && methodHaProperPrefixAndProperName(psiField, method, prefixes)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private boolean methodIsNotPrivate(PsiMethod method) {
        PsiModifierList modifierList = method.getModifierList();
        return modifierListHasNoPrivateModifier(modifierList);
    }

    private boolean methodHaProperPrefixAndProperName(PsiField psiField, PsiMethod method, List<String> prefixes) {
        String fieldNamePrefix = codeStyleSettings.getFieldNamePrefix();
        String fieldNameWithoutPrefix = psiField.getName().replace(fieldNamePrefix, EMPTY);

        return prefixes.stream()
                .anyMatch(prefix -> method.getName().equals(prefix + WordUtils.capitalize(fieldNameWithoutPrefix)));
    }

    private boolean modifierListHasNoPrivateModifier(PsiModifierList modifierList) {
        return !modifierList.hasExplicitModifier(PRIVATE_MODIFIER);
    }

}
