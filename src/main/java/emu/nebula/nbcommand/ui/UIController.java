package emu.nebula.nbcommand.ui;

import emu.nebula.nbcommand.service.I18nManager;
import emu.nebula.nbcommand.model.Command;
import emu.nebula.nbcommand.model.command.Syntax;
import emu.nebula.nbcommand.service.command.CommandExecutor;
import emu.nebula.nbcommand.service.TypedDataManager;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class UIController {
    private static final Logger logger = LoggerFactory.getLogger(UIController.class);
    
    private final I18nManager i18n = I18nManager.getInstance();

    private final TypedDataManager typedDataManager;
    private final Map<String, Control> parameterControls;
    private final Consumer<String> commandPreviewConsumer;
    private final Consumer<String> commandDetailsConsumer;
    private final Consumer<String> selectedCommandConsumer;
    private final VBox paramContainer;
    private final CommandExecutor commandExecutor;
    // 管理带类型的ComboBox控件
    private final Map<ComboBox<String>, TypedComboBoxManager> comboBoxManagers = new HashMap<>();

    public UIController(TypedDataManager typedDataManager,
                        Map<String, Control> parameterControls,
                        Consumer<String> historyConsumer,
                        Consumer<String> commandPreviewConsumer,
                        Consumer<String> commandDetailsConsumer,
                        Consumer<String> selectedCommandConsumer,
                        VBox paramContainer,
                        CommandExecutor commandExecutor) {
        this.typedDataManager = typedDataManager;
        this.parameterControls = parameterControls;
        this.commandPreviewConsumer = commandPreviewConsumer;
        this.commandDetailsConsumer = commandDetailsConsumer;
        this.selectedCommandConsumer = selectedCommandConsumer;
        this.paramContainer = paramContainer;
        this.commandExecutor = commandExecutor;
    }

    public void showCommandDetails(Command command, String commandName, String commandDetails) {
        this.commandDetailsConsumer.accept(commandDetails);
        this.selectedCommandConsumer.accept(commandName);

        // 清空参数容器和参数控件映射
        paramContainer.getChildren().clear();
        parameterControls.clear();
        comboBoxManagers.clear();

        if (command == null) {
            commandPreviewConsumer.accept("");
            return;
        }

        // 根据命令语法生成参数输入框
        for (Syntax.Field field : command.syntax().getFields()) {
            // 跳过第一个字段，它是命令名称
            if (field == command.syntax().getFields().getFirst()) {
                continue;
            }
            
            String param = field.getCurrentName(); // 使用显示名称
            String originalParam = field.getOriginalName(); // 原始名称用于查找数据
            
            // 检查是否为多选参数
            // boolean isMultipleChoice = originalParam.contains("{") && originalParam.contains("}") && originalParam.contains("|");
            // 检查是否为必填参数
            // boolean isRequired = originalParam.contains("[") && originalParam.contains("]");

            if (field.getFieldMode() == Syntax.FieldMode.SIMPLE_MULTI_SELECT) {
                // 处理多选参数，如 {create | delete}
                String cleanParam = originalParam.replaceAll("[{}\\[\\]]", ""); // 移除大括号和中括号
                String[] options = cleanParam.split(" \\| "); // 用 | 分割
                ComboBox<String> comboBox = new ComboBox<>();
                javafx.collections.ObservableList<String> items = FXCollections.observableArrayList(options);
                comboBox.setItems(items);

                comboBox.setEditable(true);

                comboBox.setPromptText(param);
                Label paramLabel = new Label(param + (field.isRequired() ? "*" : "") + ":");
                paramLabel.setMinWidth(Region.USE_PREF_SIZE);
                paramContainer.getChildren().add(paramLabel);
                paramContainer.getChildren().add(comboBox);
                VBox.setMargin(comboBox, new javafx.geometry.Insets(0, 0, 10, 0));
                // 使用原始名称作为键来存储控件
                parameterControls.put(originalParam, comboBox);
            } else if (field.getFieldMode() == Syntax.FieldMode.COMPLEX_MULTI_SELECT) {
                // 特殊处理参数，使用ComboBox
                ComboBox<String> comboBox = new ComboBox<>();
                comboBox.setPromptText(param);
                // 对于大量数据，这里应该使用分页或过滤机制
                comboBox.setItems(typedDataManager.getDataList(originalParam, "all"));
                comboBox.setEditable(true); // 允许用户输入过滤
                
                // 创建ComboBox管理器
                TypedComboBoxManager comboBoxManager = new TypedComboBoxManager(comboBox, typedDataManager, originalParam);
                comboBoxManagers.put(comboBox, comboBoxManager);
                
                // 如果是类型化数据参数，则添加类型过滤控件和搜索按钮
                addTypedParameterControl(comboBox, originalParam, param);
                
                // 将ComboBox添加到参数控件映射中，以便设置监听器
                parameterControls.put(originalParam, comboBox);
            } else {
                // 普通输入框
                TextField textField = new TextField();
                textField.setPromptText(param + (field.isRequired() ? "*" : ""));
                
                // 添加清除按钮
                HBox textBox = createTextControl(textField);
                
                Label paramLabel = new Label(param + (field.isRequired() ? "*" : "") + ":");
                paramLabel.setMinWidth(Region.USE_PREF_SIZE);
                paramContainer.getChildren().add(paramLabel);
                paramContainer.getChildren().add(textBox);
                VBox.setMargin(textBox, new javafx.geometry.Insets(0, 0, 10, 0));
                parameterControls.put(originalParam, textField);
            }
        }

        // 监听参数输入变化，更新命令预览
        updateCommandPreview(command);
        setupParameterListeners(command);

        logger.debug("选择命令: {}", command.name());
    }

    /**
     * 添加带类型过滤和搜索按钮的参数控件
     */
    private void addTypedParameterControl(ComboBox<String> comboBox, String originalParam, String currParam) {
        VBox vbox = new VBox(5);
        
        // 创建类型选择下拉框
        HBox typeBox = createTypeControl(comboBox, originalParam);
        
        // 创建搜索按钮
        HBox searchBox = createSearchControl(comboBox);
        
        // 添加控件到界面
        Label typeLabel = new Label(currParam + " " + i18n.getString("ui.type") + ":");
        typeLabel.setMinWidth(Region.USE_PREF_SIZE);
        vbox.getChildren().addAll(typeLabel, typeBox, new Label(originalParam + ":"), searchBox);
        VBox.setMargin(typeBox, new javafx.geometry.Insets(0, 0, 5, 0));
        
        paramContainer.getChildren().add(vbox);
        VBox.setMargin(vbox, new javafx.geometry.Insets(0, 0, 10, 0));
    }
    
    /**
     * 创建带清除按钮的类型控件
     */
    private HBox createTypeControl(ComboBox<String> dataComboBox, String paramName) {
        HBox hbox = new HBox(5);
        
        // 创建类型选择下拉框
        ComboBox<String> typeComboBox = createTypeComboBox(dataComboBox, paramName);
        
        // 创建清除按钮
        Button clearButton = new Button(i18n.getString("ui.clear"));
        clearButton.setMinWidth(Region.USE_PREF_SIZE);
        clearButton.setOnAction(event -> {
            typeComboBox.setValue("all");
            // 重置数据框
            TypedComboBoxManager manager = comboBoxManagers.get(dataComboBox);
            if (manager != null) {
                manager.updateType("all");
            }
        });
        
        // 设置HBox中的组件
        hbox.getChildren().addAll(typeComboBox, clearButton);
        HBox.setHgrow(typeComboBox, Priority.ALWAYS);
        
        return hbox;
    }
    
    /**
     * 创建类型选择下拉框
     */
    private ComboBox<String> createTypeComboBox(ComboBox<String> dataComboBox, String dataIdentifier) {
        // 检查是否存在多种类型
        Set<String> types = typedDataManager.getTypes(dataIdentifier);
        
        // 创建类型选择下拉框
        ComboBox<String> typeComboBox = new ComboBox<>();
        
        if (types.size() > 1) {
            // 如果有多种类型，显示类型选择框
            javafx.collections.ObservableList<String> typeOptions = FXCollections.observableArrayList(types);
            typeComboBox.setItems(typeOptions);
            typeComboBox.setPromptText(i18n.getString("ui.select_type"));
            typeComboBox.setValue("all"); // 默认选择全部
            
            // 当类型选择改变时，更新数据列表
            typeComboBox.setOnAction(event -> {
                String selectedType = typeComboBox.getSelectionModel().getSelectedItem();
                if (selectedType != null) {
                    TypedComboBoxManager manager = comboBoxManagers.get(dataComboBox);
                    if (manager != null) {
                        manager.updateType(selectedType);
                    }
                }
            });
        } else {
            // 如果只有一种或没有类型，显示禁用的类型选择框
            typeComboBox.setItems(FXCollections.observableArrayList(i18n.getString("ui.all")));
            typeComboBox.setValue(i18n.getString("ui.all"));
            typeComboBox.setDisable(true);
        }
        
        return typeComboBox;
    }
    
    /**
     * 创建带搜索按钮的搜索控件
     */
    private HBox createSearchControl(ComboBox<String> comboBox) {
        HBox hbox = new HBox(5);
        
        // 创建清除按钮
        Button clearButton = new Button(i18n.getString("ui.clear"));
        clearButton.setMinWidth(Region.USE_PREF_SIZE);
        clearButton.setOnAction(event -> {
            comboBox.getEditor().clear();
            comboBox.getSelectionModel().clearSelection();
            // 重新加载所有项
            TypedComboBoxManager manager = comboBoxManagers.get(comboBox);
            if (manager != null) {
                comboBox.setItems(typedDataManager.getDataList(manager.getDataIdentifier(), "all"));
            }
            // 隐藏下拉列表
            comboBox.hide();
        });
        
        // 创建搜索按钮
        Button searchButton = new Button(i18n.getString("ui.search"));
        searchButton.setMinWidth(Region.USE_PREF_SIZE);
        searchButton.setOnAction(event -> {
            TypedComboBoxManager manager = comboBoxManagers.get(comboBox);
            if (manager != null) {
                manager.updateFilter();
            }
        });
        
        // 设置HBox中的组件
        hbox.getChildren().addAll(comboBox, clearButton, searchButton);
        HBox.setHgrow(comboBox, Priority.ALWAYS);
        
        return hbox;
    }

    /**
     * 创建带清除按钮的文本控件
     */
    private HBox createTextControl(TextField textField) {
        HBox hbox = new HBox(5);
        
        // 创建清除按钮
        Button clearButton = new Button(i18n.getString("ui.clear"));
        clearButton.setMinWidth(Region.USE_PREF_SIZE);
        clearButton.setOnAction(event -> {
            textField.clear();
        });
        
        // 设置HBox中的组件
        hbox.getChildren().addAll(textField, clearButton);
        HBox.setHgrow(textField, Priority.ALWAYS);
        
        return hbox;
    }

    /**
     * 设置参数输入监听器
     */
    private void setupParameterListeners(Command command) {
        for (Map.Entry<String, Control> entry : parameterControls.entrySet()) {
            Control control = entry.getValue();
            if (control instanceof TextField) {
                ((TextField) control).textProperty().addListener((obs, oldText, newText) -> updateCommandPreview(command));
            } else if (control instanceof ComboBox comboBox) {
                comboBox.getEditor().textProperty().addListener((obs, oldVal, newVal) -> updateCommandPreview(command));
                comboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateCommandPreview(command));
            }
        }
    }

    /**
     * 更新命令预览
     */
    private void updateCommandPreview(Command command) {
        if (command != null) {
            String commandText = commandExecutor.buildCommandText(command, parameterControls);
            commandPreviewConsumer.accept(commandText);
        } else {
            commandPreviewConsumer.accept("");
        }
    }
    
    /**
     * 更新所有类型化数据控件的内容
     * 当语言切换时调用此方法以更新ComboBox中的数据
     */
    public void updateTypedDataControls() {
        // 更新所有类型化数据ComboBox的内容
        for (Map.Entry<ComboBox<String>, TypedComboBoxManager> entry : comboBoxManagers.entrySet()) {
            ComboBox<String> comboBox = entry.getKey();
            TypedComboBoxManager manager = entry.getValue();
            
            // 更新ComboBox的数据源
            comboBox.setItems(typedDataManager.getDataList(manager.getDataIdentifier(), "all"));
            
            // 通知管理器更新其内部状态
            manager.reloadData();
        }
    }
}