package pl.mjedynak.idea.plugins.builder.gui;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiPackage;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.RecentsManager;
import com.intellij.ui.ReferenceEditorComboWithBrowseButton;
import pl.mjedynak.idea.plugins.builder.config.DialogConfig;
import pl.mjedynak.idea.plugins.builder.factory.ReferenceEditorComboWithBrowseButtonFactory;
import pl.mjedynak.idea.plugins.builder.factory.PackageChooserDialogFactory;
import pl.mjedynak.idea.plugins.builder.gui.helper.GuiHelper;
import pl.mjedynak.idea.plugins.builder.psi.PsiHelper;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class CreateBuilderDialog extends DialogWrapper {

    static final String RECENTS_KEY = "CreateBuilderDialog.RecentsKey";
    private static final int WIDTH = 40;

    private final String targetClassName;
    private final DialogConfig dialogConfig;

    private PsiHelper psiHelper;
    private GuiHelper guiHelper;
    private Project project;
    private PsiDirectory targetDirectory;
    private PsiClass sourceClass;
    private JTextField targetClassNameField;
    private JTextField targetMethodPrefix;
    private JCheckBox innerBuilder;
    private JCheckBox simpleBuilderName;
    private JCheckBox butMethod;
    private JCheckBox fromMethod;
    private JCheckBox builderMethodInSourceClass;

    private JCheckBox createPrivateConstructorInSourceClass;
    private JCheckBox createGetterInSourceClass;
    private JCheckBox createToBuilderInSourceClass;

    private ReferenceEditorComboWithBrowseButton targetPackageField;

    public CreateBuilderDialog(Project project,
                               String title,
                               PsiClass sourceClass,
                               String targetClassName,
                               String methodPrefix,
                               PsiPackage targetPackage,
                               PsiHelper psiHelper,
                               GuiHelper guiHelper,
                               ReferenceEditorComboWithBrowseButtonFactory referenceEditorComboWithBrowseButtonFactory,
                               DialogConfig dialogConfig) {
        super(project, true);
        this.dialogConfig = dialogConfig;
        this.psiHelper = psiHelper;
        this.guiHelper = guiHelper;
        this.project = project;
        this.sourceClass = sourceClass;
        this.targetClassName = targetClassName;
        targetClassNameField = new JTextField(targetClassName);
        targetMethodPrefix = new JTextField(methodPrefix);
        setPreferredSize(targetClassNameField);
        setPreferredSize(targetMethodPrefix);

        String suffixPackage = dialogConfig.getTargetPackageSuffix() == null
                ? ""
                : dialogConfig.getTargetPackageSuffix();

        String sourceTargetPackage = ((targetPackage != null) ? targetPackage.getQualifiedName() : "")
                + suffixPackage;
        targetPackageField = referenceEditorComboWithBrowseButtonFactory.getReferenceEditorComboWithBrowseButton(project, sourceTargetPackage, RECENTS_KEY);
        targetPackageField.addActionListener(new ChooserDisplayerActionListener(targetPackageField, new PackageChooserDialogFactory(), project));
        setTitle(title);
    }

    @Override
    public void show() {
        super.init();
        super.show();
    }

    private void setPreferredSize(JTextField field) {
        Dimension size = field.getPreferredSize();
        FontMetrics fontMetrics = field.getFontMetrics(field.getFont());
        size.width = fontMetrics.charWidth('a') * WIDTH;
        field.setPreferredSize(size);
    }

    protected Action[] createActions() {
        return new Action[]{getOKAction(), getCancelAction(), getHelpAction()};
    }

    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbConstraints = new GridBagConstraints();

        panel.setBorder(IdeBorderFactory.createBorder());

        // Class name
        gbConstraints.insets = new Insets(4, 8, 4, 8);
        gbConstraints.gridx = 0;
        gbConstraints.weightx = 0;
        gbConstraints.gridwidth = 1;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Class name"), gbConstraints);

        gbConstraints.insets = new Insets(4, 8, 4, 8);
        gbConstraints.gridx = 1;
        gbConstraints.weightx = 1;
        gbConstraints.gridwidth = 1;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.WEST;
        panel.add(targetClassNameField, gbConstraints);
        targetClassNameField.getDocument().addDocumentListener(new DocumentAdapter() {
            protected void textChanged(DocumentEvent e) {
                getOKAction().setEnabled(JavaPsiFacade.getInstance(project).getNameHelper().isIdentifier(getClassName()));
            }
        });
        // Class name

        // Method prefix
        gbConstraints.insets = new Insets(4, 8, 4, 8);
        gbConstraints.gridx = 0;
        gbConstraints.gridy = 2;
        gbConstraints.weightx = 0;
        gbConstraints.gridwidth = 1;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Method prefix"), gbConstraints);

        gbConstraints.insets = new Insets(4, 8, 4, 8);
        gbConstraints.gridx = 1;
        gbConstraints.weightx = 1;
        gbConstraints.gridwidth = 1;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.WEST;
        panel.add(targetMethodPrefix, gbConstraints);
        // Method prefix

        // Destination package
        gbConstraints.gridx = 0;
        gbConstraints.gridy = 3;
        gbConstraints.weightx = 0;
        gbConstraints.gridwidth = 1;
        panel.add(new JLabel(CodeInsightBundle.message("dialog.create.class.destination.package.label")), gbConstraints);

        gbConstraints.gridx = 1;
        gbConstraints.weightx = 1;

        AnAction clickAction = new AnAction() {
            public void actionPerformed(AnActionEvent e) {
                targetPackageField.getButton().doClick();
            }
        };
        clickAction.registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK)),
                targetPackageField.getChildComponent());

        addInnerPanelForDestinationPackageField(panel, gbConstraints);
        // Destination package

        // Inner builder
        gbConstraints.insets = new Insets(4, 8, 4, 8);
        gbConstraints.gridx = 0;
        gbConstraints.weightx = 0;
        gbConstraints.gridy = 4;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Inner builder"), gbConstraints);

        gbConstraints.insets = new Insets(4, 8, 4, 8);
        gbConstraints.gridx = 1;
        gbConstraints.weightx = 1;
        gbConstraints.gridwidth = 1;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.WEST;

        innerBuilder = new JCheckBox();
        innerBuilder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                innerBuilderAction();
            }
        });
        innerBuilder.setSelected(dialogConfig.getInnerBuilder());
        targetPackageField.setEnabled(!innerBuilder.isSelected());
        panel.add(innerBuilder, gbConstraints);
        // Inner builder

        // simple builder name
        gbConstraints.insets = new Insets(4, 32, 4, 8);
        gbConstraints.gridx = 0;
        gbConstraints.weightx = 0;
        gbConstraints.gridy = 6;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("'Builder' name"), gbConstraints);

        gbConstraints.insets = new Insets(4, 32, 4, 8);
        gbConstraints.gridx = 1;
        gbConstraints.weightx = 1;
        gbConstraints.gridwidth = 1;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.WEST;
        simpleBuilderName = new JCheckBox();
        simpleBuilderName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                simpleBuilderAction();
            }
        });
        simpleBuilderName.setEnabled(innerBuilder.isSelected());
        simpleBuilderName.setSelected(dialogConfig.getSimpleBuilderName());
        panel.add(simpleBuilderName, gbConstraints);
        // simple builder name

        // builder method in source class
        gbConstraints.insets = new Insets(4, 32, 4, 8);
        gbConstraints.gridx = 0;
        gbConstraints.weightx = 0;
        gbConstraints.gridy = 7;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("'builder' method in source class"), gbConstraints);

        gbConstraints.insets = new Insets(4, 32, 4, 8);
        gbConstraints.gridx = 1;
        gbConstraints.weightx = 1;
        gbConstraints.gridwidth = 1;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.WEST;
        builderMethodInSourceClass = new JCheckBox();
        builderMethodInSourceClass.setSelected(dialogConfig.getBuilderMethodInSourceClass());
        builderMethodInSourceClass.setEnabled(innerBuilder.isSelected());
        panel.add(builderMethodInSourceClass, gbConstraints);
        // builder method in source class

        // but method
        gbConstraints.insets = new Insets(4, 8, 4, 8);
        gbConstraints.gridx = 0;
        gbConstraints.weightx = 0;
        gbConstraints.gridy = 8;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("'but' method"), gbConstraints);

        gbConstraints.insets = new Insets(4, 8, 4, 8);
        gbConstraints.gridx = 1;
        gbConstraints.weightx = 1;
        gbConstraints.gridwidth = 1;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.WEST;
        butMethod = new JCheckBox();
        butMethod.setSelected(dialogConfig.getButMethod());
        panel.add(butMethod, gbConstraints);
        // but method


        // from method
        gbConstraints.insets = new Insets(4, 8, 4, 8);
        gbConstraints.gridx = 0;
        gbConstraints.weightx = 0;
        gbConstraints.gridy = 9;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("'from' method"), gbConstraints);

        gbConstraints.insets = new Insets(4, 8, 4, 8);
        gbConstraints.gridx = 1;
        gbConstraints.weightx = 1;
        gbConstraints.gridwidth = 1;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.WEST;
        fromMethod = new JCheckBox();
        fromMethod.setSelected(dialogConfig.getFromMethod());
        panel.add(fromMethod, gbConstraints);
        // from method


        // create private constructor
        gbConstraints.insets = new Insets(4, 8, 4, 8);
        gbConstraints.gridx = 0;
        gbConstraints.weightx = 0;
        gbConstraints.gridy = 10;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Create private constructor"), gbConstraints);

        gbConstraints.insets = new Insets(4, 8, 4, 8);
        gbConstraints.gridx = 1;
        gbConstraints.weightx = 1;
        gbConstraints.gridwidth = 1;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.WEST;
        createPrivateConstructorInSourceClass = new JCheckBox();
        createPrivateConstructorInSourceClass.setSelected(dialogConfig.getCreatePrivateConstructor());
        panel.add(createPrivateConstructorInSourceClass, gbConstraints);
        // create private constructor


        // create getter in source class
        gbConstraints.insets = new Insets(4, 8, 4, 8);
        gbConstraints.gridx = 0;
        gbConstraints.weightx = 0;
        gbConstraints.gridy = 11;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Create getter"), gbConstraints);

        gbConstraints.insets = new Insets(4, 8, 4, 8);
        gbConstraints.gridx = 1;
        gbConstraints.weightx = 1;
        gbConstraints.gridwidth = 1;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.WEST;
        createGetterInSourceClass = new JCheckBox();
        createGetterInSourceClass.setSelected(dialogConfig.getCreateGetter());
        panel.add(createGetterInSourceClass, gbConstraints);
        // create getter in source class


        // create toBuilder in source class
        gbConstraints.insets = new Insets(4, 8, 4, 8);
        gbConstraints.gridx = 0;
        gbConstraints.weightx = 0;
        gbConstraints.gridy = 12;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Create toBuilder"), gbConstraints);

        gbConstraints.insets = new Insets(4, 8, 4, 8);
        gbConstraints.gridx = 1;
        gbConstraints.weightx = 1;
        gbConstraints.gridwidth = 1;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        gbConstraints.anchor = GridBagConstraints.WEST;
        createToBuilderInSourceClass = new JCheckBox();
        createToBuilderInSourceClass.setSelected(dialogConfig.getCreateToBuilder());
        panel.add(createToBuilderInSourceClass, gbConstraints);
        // create toBuilder in source class

        innerBuilderAction();
        simpleBuilderAction();

        return panel;
    }

    private void innerBuilderAction() {
        targetPackageField.setEnabled(!innerBuilder.isSelected());
        simpleBuilderName.setEnabled(innerBuilder.isSelected());
        builderMethodInSourceClass.setEnabled(innerBuilder.isSelected());
        changeBuilderName();
    }

    private void simpleBuilderAction(){
        changeBuilderName();
    }

    private void changeBuilderName() {
        if(innerBuilder.isSelected() && simpleBuilderName.isSelected()) {
            targetClassNameField.setText("Builder");
        } else {
            targetClassNameField.setText(targetClassName);
        }
    }

    private void addInnerPanelForDestinationPackageField(JPanel panel, GridBagConstraints gbConstraints) {
        JPanel innerPanel = createInnerPanelForDestinationPackageField();
        panel.add(innerPanel, gbConstraints);
    }

    private JPanel createInnerPanelForDestinationPackageField() {
        JPanel innerPanel = new JPanel(new BorderLayout());
        innerPanel.add(targetPackageField, BorderLayout.CENTER);
        return innerPanel;
    }

    protected void doOKAction() {
        registerEntry(RECENTS_KEY, targetPackageField.getText());
        Module module = psiHelper.findModuleForPsiClass(sourceClass, project);
        if (module == null) {
            throw new IllegalStateException("Cannot find module for class " + sourceClass.getName());
        }
        if (!isInnerBuilder()) {
            SelectDirectory selectDirectory = new SelectDirectory(this, psiHelper, guiHelper, project, module, getPackageName(), getClassName());
            executeCommand(selectDirectory);
        }
        callSuper();
    }

    void registerEntry(String key, String entry) {
        RecentsManager.getInstance(project).registerRecentEntry(key, entry);
    }

    void callSuper() {
        super.doOKAction();
    }

    void executeCommand(SelectDirectory selectDirectory) {
        CommandProcessor.getInstance().executeCommand(project, selectDirectory, CodeInsightBundle.message("create.directory.command"), null);
    }

    private String getPackageName() {
        String name = targetPackageField.getText();
        return (name != null) ? name.trim() : "";
    }

    public JComponent getPreferredFocusedComponent() {
        return targetClassNameField;
    }

    public String getClassName() {
        return targetClassNameField.getText();
    }

    public String getMethodPrefix() {
        return targetMethodPrefix.getText();
    }

    public boolean isInnerBuilder() {
        return innerBuilder.isSelected();
    }

    public boolean hasButMethod() {
        return butMethod.isSelected();
    }

    public boolean hasFromMethod() {
        return fromMethod.isSelected();
    }

    public boolean hasBuilderMethodInSourceClass() {
        return innerBuilder.isSelected() && builderMethodInSourceClass.isSelected();
    }

    public boolean isCreatePrivateConstructor() {
        return createPrivateConstructorInSourceClass.isSelected();
    }

    public boolean isCreateGetter() {
        return createGetterInSourceClass.isSelected();
    }

    public boolean isCreateToBuilder() {
        return createToBuilderInSourceClass.isSelected();
    }

    public PsiDirectory getTargetDirectory() {
        return targetDirectory;
    }

    public void setTargetDirectory(PsiDirectory targetDirectory) {
        this.targetDirectory = targetDirectory;
    }
}
