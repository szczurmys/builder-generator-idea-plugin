package pl.mjedynak.idea.plugins.builder.helper.impl;

import com.intellij.ide.util.EditSourceUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.util.PsiUtilBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import pl.mjedynak.idea.plugins.builder.helper.PsiHelper;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JavaPsiFacade.class, EditSourceUtil.class, PsiUtilBase.class})
public class PsiHelperImplTest {

    private PsiHelper psiHelper;

    @Mock
    private Project project;

    @Mock
    private Editor editor;

    @Mock
    private PsiClass psiClass;

    @Before
    public void setUp() {
        psiHelper = new PsiHelperImpl();
    }

    @Test
    public void shouldGetShortNamesCacheUsingPsiJavaFacade() {
        // given
        mockStatic(JavaPsiFacade.class);
        JavaPsiFacade javaPsiFacadeInstance = mock(JavaPsiFacade.class);
        when(JavaPsiFacade.getInstance(project)).thenReturn(javaPsiFacadeInstance);

        // when
        psiHelper.getPsiShortNamesCache(project);

        // then
        verify(javaPsiFacadeInstance).getShortNamesCache();
    }

    @Test
    public void shouldGetNavigatableObjectAndInvokeNavigateOnIt() {
        // given
        mockStatic(EditSourceUtil.class);
        Navigatable navigatable = mock(Navigatable.class);
        when(EditSourceUtil.getDescriptor(psiClass)).thenReturn(navigatable);

        // when
        psiHelper.navigateToClass(psiClass);

        // then
        verify(navigatable).navigate(true);
    }

    @Test
    public void shouldGetPsiClassFromEditorWhenPsiFileIsPsiClassOwner() {
        // given
        mockStatic(PsiUtilBase.class);
        PsiClassOwner psiFile = mock(PsiClassOwner.class);
        when(PsiUtilBase.getPsiFileInEditor(editor, project)).thenReturn(psiFile);
        PsiClass[] classes = {psiClass};
        when(psiFile.getClasses()).thenReturn(classes);

        // when
        PsiClass psiClassFromEditor = psiHelper.getPsiClassFromEditor(editor, project);

        // then
        assertThat(psiClassFromEditor, is(psiClass));
    }
}