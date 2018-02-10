package pl.mjedynak.idea.plugins.builder.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.Nullable;

@State(
        name = "builder.dialog.config",
        storages = {
                @Storage(id = "main", file = "$APP_CONFIG$/builder.dialog.config.xml")
        }
)
public class DialogConfig  implements PersistentStateComponent<DialogConfig> {
    @Tag("TargetPackageSuffix")
    private String targetPackageSuffix;

    @Tag("InnerBuilder")
    private Boolean innerBuilder;

    @Tag("SimpleBuilderName")
    private Boolean simpleBuilderName;

    @Tag("ButMethod")
    private Boolean butMethod;

    @Tag("FromMethod")
    private Boolean fromMethod;

    @Tag("BuilderMethodInSourceClass")
    private Boolean builderMethodInSourceClass;

    @Tag("CreatePrivateConstructor")
    private Boolean createPrivateConstructor;

    @Tag("CreateGetter")
    private Boolean createGetter;

    @Tag("CreateToBuilder")
    private Boolean createToBuilder;

    @Tag("UseSingleField")
    private Boolean useSingleField;

    public static DialogConfig getInstance() {
        return ServiceManager.getService(DialogConfig.class);
    }

    public DialogConfig() {
    }

    public String getTargetPackageSuffix() {
        return targetPackageSuffix;
    }

    public void setTargetPackageSuffix(String targetPackageSuffix) {
        this.targetPackageSuffix = targetPackageSuffix;
    }

    public Boolean getInnerBuilder() {
        return getValueOrFalse(innerBuilder);
    }

    public void setInnerBuilder(Boolean innerBuilder) {
        this.innerBuilder = innerBuilder;
    }

    public Boolean getSimpleBuilderName() {
        return getValueOrFalse(simpleBuilderName);
    }

    public void setSimpleBuilderName(Boolean simpleBuilderName) {
        this.simpleBuilderName = simpleBuilderName;
    }

    public Boolean getButMethod() {
        return getValueOrFalse(butMethod);
    }

    public void setButMethod(Boolean butMethod) {
        this.butMethod = butMethod;
    }

    public Boolean getFromMethod() {
        return getValueOrFalse(fromMethod);
    }

    public void setFromMethod(Boolean fromMethod) {
        this.fromMethod = fromMethod;
    }

    public Boolean getBuilderMethodInSourceClass() {
        return getValueOrFalse(builderMethodInSourceClass);
    }

    public void setBuilderMethodInSourceClass(Boolean builderMethodInSourceClass) {
        this.builderMethodInSourceClass = builderMethodInSourceClass;
    }

    public Boolean getCreatePrivateConstructor() {
        return getValueOrFalse(createPrivateConstructor);
    }

    public void setCreatePrivateConstructor(Boolean createPrivateConstructor) {
        this.createPrivateConstructor = createPrivateConstructor;
    }

    public Boolean getCreateGetter() {
        return getValueOrFalse(createGetter);
    }

    public void setCreateGetter(Boolean createGetter) {
        this.createGetter = createGetter;
    }

    public Boolean getCreateToBuilder() {
        return getValueOrFalse(createToBuilder);
    }

    public void setCreateToBuilder(Boolean createToBuilder) {
        this.createToBuilder = createToBuilder;
    }

    public Boolean getUseSingleField() {
        return getValueOrFalse(createToBuilder);
    }

    public void setUseSingleField(Boolean useSingleField) {
        this.useSingleField = useSingleField;
    }

    @Nullable
    @Override
    public DialogConfig getState() {
        return this;
    }

    @Override
    public void loadState(DialogConfig dialogConfig) {
        XmlSerializerUtil.copyBean(dialogConfig, this);
    }


    private Boolean getValueOrFalse(Boolean value) {
        return getValueOrDefault(value, false);
    }

    private <T> T getValueOrDefault(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }
}
