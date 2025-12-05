package emu.nebula.nbcommand.ui;

import emu.nebula.nbcommand.service.I18nManager;
import emu.nebula.nbcommand.model.Command;
import emu.nebula.nbcommand.model.command.Syntax;
import emu.nebula.nbcommand.service.command.CommandExecutor;
import emu.nebula.nbcommand.service.TypedDataManager;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
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
    private Command currentCommand; // 保存当前命令的引用
    // 管理带类型的ComboBox控件
    private final Map<ComboBox<String>, TypedComboBoxManager> comboBoxManagers = new HashMap<>();
    // 管理多选容器控件
    private final Map<String, MultiSelectContainerManager> multiSelectManagers = new HashMap<>();
    // 管理带数量的多选容器控件
    private final Map<String, MultiSelectWithCountContainerManager> multiSelectWithCountManagers = new HashMap<>();

    public UIController(TypedDataManager typedDataManager,
                        Map<String, Control> parameterControls,
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
        // 设置多选管理器映射，以便CommandExecutor可以访问
        this.commandExecutor.setMultiSelectManagers(multiSelectManagers);
        this.commandExecutor.setMultiSelectWithCountManagers(multiSelectWithCountManagers);
    }

    public void showCommandDetails(Command command, String commandName, String commandFullDescription) {
        this.commandDetailsConsumer.accept(commandFullDescription);
        this.selectedCommandConsumer.accept(commandName);
        this.currentCommand = command; // 保存当前命令的引用

        // 清空参数容器和参数控件映射
        paramContainer.getChildren().clear();
        parameterControls.clear();
        comboBoxManagers.clear();
        multiSelectManagers.clear();
        multiSelectWithCountManagers.clear();

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

            if (field.getFieldMode() == Syntax.FieldMode.SIMPLE_RADIO) {
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
            } else if (field.getFieldMode() == Syntax.FieldMode.COMPLEX_RADIO) {
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
            } else if (field.getFieldMode() == Syntax.FieldMode.MULTI_SELECT_CONTAINER) {
                // 处理多选容器模式
                addMultiSelectContainerControl(originalParam, param);
            } else if (field.getFieldMode() == Syntax.FieldMode.MULTI_SELECT_CONTAINER_WITH_COUNT) {
                // 处理带数量的多选容器模式
                addMultiSelectWithCountContainerControl(originalParam, param);
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
     * 创建多选容器控件
     */
    private void addMultiSelectContainerControl(String originalParam, String currParam) {
        VBox containerVBox = new VBox(4); // 减小间距
        
        // 创建选择区域
        HBox selectBox = new HBox(4); // 减小间距
        
        // 创建数据选择下拉框
        ComboBox<String> dataComboBox = new ComboBox<>();
        dataComboBox.setPromptText(currParam);
        dataComboBox.setItems(typedDataManager.getDataList(originalParam, "all"));
        dataComboBox.setEditable(true);
        
        // 创建ComboBox管理器
        TypedComboBoxManager comboBoxManager = new TypedComboBoxManager(dataComboBox, typedDataManager, originalParam);
        comboBoxManagers.put(dataComboBox, comboBoxManager);
        
        // 创建添加按钮
        Button addButton = new Button("+");
        addButton.setMinWidth(Region.USE_PREF_SIZE);
        
        // 创建清除按钮
        Button clearButton = new Button(i18n.getString("ui.clear"));
        clearButton.setMinWidth(Region.USE_PREF_SIZE);
        clearButton.setOnAction(event -> {
            dataComboBox.getEditor().clear();
            dataComboBox.getSelectionModel().clearSelection();
            // 重新加载所有项
            dataComboBox.setItems(typedDataManager.getDataList(comboBoxManager.getDataIdentifier(), "all"));
            // 隐藏下拉列表
            dataComboBox.hide();
        });
        
        // 创建搜索按钮
        Button searchButton = new Button(i18n.getString("ui.search"));
        searchButton.setMinWidth(Region.USE_PREF_SIZE);
        searchButton.setOnAction(event -> comboBoxManager.updateFilter());
        
        // 设置选择区域的组件
        selectBox.getChildren().addAll(dataComboBox, addButton, clearButton, searchButton);
        HBox.setHgrow(dataComboBox, Priority.ALWAYS);
        
        // 创建已选项目容器 (使用FlowPane支持双列显示)
        FlowPane selectedItemsPane = createSelectedItemsPane();
        
        // 创建已选项目标签
        Label selectedItemsLabel = new Label(i18n.getString("ui.selected_items") + ":");
        selectedItemsLabel.setStyle("-fx-font-size: 11px;");
        
        // 不再使用ScrollPane，直接将FlowPane添加到容器中
        VBox.setVgrow(selectedItemsPane, Priority.ALWAYS);
        
        // 创建多选容器管理器
        MultiSelectContainerManager multiSelectManager = new MultiSelectContainerManager(
                dataComboBox, selectedItemsPane, typedDataManager, originalParam);
        multiSelectManagers.put(originalParam, multiSelectManager);
        
        // 设置项目变更回调以更新命令预览
        multiSelectManager.setOnItemsChanged(() -> updateCommandPreview(currentCommand));
        
        // 设置添加按钮事件
        addButton.setOnAction(event -> multiSelectManager.addItem());
        
        // 添加控件到容器
        containerVBox.getChildren().addAll(new Label(currParam + ":"), selectBox, selectedItemsLabel, selectedItemsPane);
        
        paramContainer.getChildren().add(containerVBox);
        VBox.setMargin(containerVBox, new javafx.geometry.Insets(0, 0, 10, 0));
        
        // 将容器添加到参数控件映射中
        parameterControls.put(originalParam, dataComboBox);
    }
    
    /**
     * 创建带数量的多选容器控件
     */
    private void addMultiSelectWithCountContainerControl(String originalParam, String currParam) {
        VBox containerVBox = new VBox(4); // 减小间距
        
        // 创建选择区域
        HBox selectBox = new HBox(4); // 减小间距
        
        // 创建数据选择下拉框
        ComboBox<String> dataComboBox = new ComboBox<>();
        dataComboBox.setPromptText(currParam);
        dataComboBox.setItems(typedDataManager.getDataList(originalParam, "all"));
        dataComboBox.setEditable(true);
        
        // 创建ComboBox管理器
        TypedComboBoxManager comboBoxManager = new TypedComboBoxManager(dataComboBox, typedDataManager, originalParam);
        comboBoxManagers.put(dataComboBox, comboBoxManager);
        
        // 创建添加按钮
        Button addButton = new Button("+");
        addButton.setMinWidth(Region.USE_PREF_SIZE);
        
        // 创建清除按钮
        Button clearButton = new Button(i18n.getString("ui.clear"));
        clearButton.setMinWidth(Region.USE_PREF_SIZE);
        clearButton.setOnAction(event -> {
            dataComboBox.getEditor().clear();
            dataComboBox.getSelectionModel().clearSelection();
            // 重新加载所有项
            dataComboBox.setItems(typedDataManager.getDataList(comboBoxManager.getDataIdentifier(), "all"));
            // 隐藏下拉列表
            dataComboBox.hide();
        });
        
        // 创建搜索按钮
        Button searchButton = new Button(i18n.getString("ui.search"));
        searchButton.setMinWidth(Region.USE_PREF_SIZE);
        searchButton.setOnAction(event -> comboBoxManager.updateFilter());
        
        // 设置选择区域的组件
        selectBox.getChildren().addAll(dataComboBox, addButton, clearButton, searchButton);
        HBox.setHgrow(dataComboBox, Priority.ALWAYS);
        
        // 创建已选项目容器 (使用FlowPane支持双列显示)
        FlowPane selectedItemsPane = createSelectedItemsPane();
        
        // 创建已选项目标签
        Label selectedItemsLabel = new Label(i18n.getString("ui.selected_items") + ":");
        selectedItemsLabel.setStyle("-fx-font-size: 11px;");
        
        // 不再使用ScrollPane，直接将FlowPane添加到容器中
        VBox.setVgrow(selectedItemsPane, Priority.ALWAYS);
        
        // 创建带数量的多选容器管理器
        MultiSelectWithCountContainerManager multiSelectWithCountManager = new MultiSelectWithCountContainerManager(
                dataComboBox, selectedItemsPane, typedDataManager, originalParam);
        multiSelectWithCountManagers.put(originalParam, multiSelectWithCountManager);
        
        // 设置项目变更回调以更新命令预览
        multiSelectWithCountManager.setOnItemsChanged(() -> updateCommandPreview(currentCommand));
        
        // 设置添加按钮事件
        addButton.setOnAction(event -> multiSelectWithCountManager.addItem());
        
        // 添加控件到容器
        containerVBox.getChildren().addAll(new Label(currParam + ":"), selectBox, selectedItemsLabel, selectedItemsPane);
        
        paramContainer.getChildren().add(containerVBox);
        VBox.setMargin(containerVBox, new javafx.geometry.Insets(0, 0, 10, 0));
        
        // 将容器添加到参数控件映射中
        parameterControls.put(originalParam, dataComboBox);
    }
    
    /**
     * 创建已选项目容器
     * @return 已选项目容器
     */
    private FlowPane createSelectedItemsPane() {
        FlowPane selectedItemsPane = new FlowPane();
        selectedItemsPane.setHgap(2); // 设置水平间距
        selectedItemsPane.setVgap(2); // 设置垂直间距
        selectedItemsPane.setStyle("-fx-border-color: gray; -fx-border-width: 1px; -fx-padding: 2;");
        selectedItemsPane.prefWrapLengthProperty().bind(selectedItemsPane.widthProperty().subtract(20)); // 绑定换行宽度到容器宽度
        return selectedItemsPane;
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
            } else if (control instanceof ComboBox<?> comboBox) {
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
        
        // 更新所有多选容器管理器
        for (Map.Entry<String, MultiSelectContainerManager> entry : multiSelectManagers.entrySet()) {
            MultiSelectContainerManager manager = entry.getValue();
            manager.reloadData();
        }
        
        // 更新所有带数量的多选容器管理器
        for (Map.Entry<String, MultiSelectWithCountContainerManager> entry : multiSelectWithCountManagers.entrySet()) {
            MultiSelectWithCountContainerManager manager = entry.getValue();
            manager.reloadData();
        }
    }
    
    /**
     * 获取多选容器管理器映射
     * @return 多选容器管理器映射
     */
    public Map<String, MultiSelectContainerManager> getMultiSelectManagers() {
        return multiSelectManagers;
    }
    
    /**
     * 获取带数量的多选容器管理器映射
     * @return 带数量的多选容器管理器映射
     */
    public Map<String, MultiSelectWithCountContainerManager> getMultiSelectWithCountManagers() {
        return multiSelectWithCountManagers;
    }
}