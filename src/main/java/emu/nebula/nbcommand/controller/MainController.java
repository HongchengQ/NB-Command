package emu.nebula.nbcommand.controller;

import emu.nebula.nbcommand.Launcher;
import emu.nebula.nbcommand.service.I18nManager;
import emu.nebula.nbcommand.model.Command;
import emu.nebula.nbcommand.ui.UIController;
import emu.nebula.nbcommand.viewmodel.MainViewModel;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class MainController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML
    private Menu optionsMenu;
    @FXML
    private MenuItem checkUpdateMenuItem;
    @FXML
    private MenuItem aboutMenuItem;
    @FXML
    private Menu languageMenu;

    // Toolbar
    @FXML
    private Label serverAddressLabel;
    @FXML
    private TextField serverAddressField;
    @FXML
    private Label tokenLabel;
    @FXML
    private PasswordField authTokenField;
    @FXML
    private Button saveButton;
    @FXML
    private Label savedServerLabel;
    @FXML
    private Label savedAddressLabel;
    @FXML
    private Label savedTokenLabel;
    @FXML
    private Label savedAuthTokenLabel;

    // Left Panel
    @FXML
    private Label categoriesLabel;
    @FXML
    private ListView<String> categoryList;
    @FXML
    private Label historyLabel;
    @FXML
    private TextArea historyArea;

    // Middle Panel
    @FXML
    private Label commandsLabel;
    @FXML
    private TableView<Command> commandTable;
    @FXML
    private TableColumn<Command, String> commandNameColumn;
    @FXML
    private TableColumn<Command, String> commandDescColumn;
    @FXML
    private Label detailsLabel;
    @FXML
    private TextArea commandDetailArea;

    // Right Panel
    @FXML
    private Label executeLabel;
    @FXML
    private Label selectedCommandLabel;
    @FXML
    private TextField selectedCommandField;
    @FXML
    private Label parametersLabel;
    @FXML
    private VBox paramContainer;
    @FXML
    private Label previewLabel;
    @FXML
    private TextField commandPreviewField;
    @FXML
    private Label uidLabel;
    @FXML
    private TextField uidField;
    @FXML
    private Button executeButton;
    @FXML
    private Label customLabel;
    @FXML
    private TextArea customCommandArea;
    @FXML
    private Button sendCustomButton;

    // Bottom
    @FXML
    private Label developerLabel;
    @FXML
    private Label versionLabel;

    private final Map<String, ObservableList<Command>> commandsByCategory = new LinkedHashMap<>();

    // 存储参数输入控件的映射
    private final Map<String, Control> parameterControls = new HashMap<>();

    private MainViewModel viewModel;
    private UIController uiController;
    private final I18nManager i18n = I18nManager.getInstance();
    private String lastSelectedCategory = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize ViewModel
        viewModel = new MainViewModel();

        // Setup UI controller
        uiController = new UIController(
                viewModel.getTypedDataManager(),
                parameterControls,
                commandPreviewField::setText,
                commandDetailArea::setText,
                selectedCommandField::setText,
                paramContainer,
                viewModel.getCommandExecutor()
        );

        // Bind UI elements to ViewModel properties
        serverAddressField.textProperty().bindBidirectional(viewModel.serverAddressProperty());
        authTokenField.textProperty().bindBidirectional(viewModel.authTokenProperty());
        // 注意这里是saved开头 不是server
        savedAddressLabel.textProperty().bind(viewModel.serverAddressProperty());
        savedAuthTokenLabel.textProperty().bind(viewModel.maskedAuthTokenProperty());
        // uid
        uidField.textProperty().bindBidirectional(viewModel.uidProperty());


        // 设置历史记录监听器，自动滚动到底部
        setupHistoryAutoScroll();

        // 初始化命令分类列表
        categoryList.setItems(getCategoryList());

        // 设置命令表格列
        commandNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        commandDescColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());

        // 初始化各类别的命令数据
        viewModel.loadCommandsByCategory(commandsByCategory);

        // 监听分类选择变化
        categoryList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    lastSelectedCategory = newValue;
                    updateCommandTable(newValue);
                });

        // 监听命令选择变化
        commandTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        viewModel.setSelectedCommand(newValue);
                        uiController.showCommandDetails(newValue, newValue.name(), newValue.fullDescription());
                    }
                });

        // 关于菜单项的点击事件
        aboutMenuItem.setOnAction(event -> aboutMenuItem());
        checkUpdateMenuItem.setOnAction(event -> checkForUpdates());

        updateUIText();
        updateCategories();

        // 添加到历史记录
        viewModel.addToHistory(i18n.getString("message.app_started"));
        logger.info("应用启动完成");
        
        // 启动时检查更新
        checkForUpdatesInBackground();
    }

    /**
     * 设置历史记录自动滚动到底部
     */
    private void setupHistoryAutoScroll() {
        // 稍后执行滚动操作，确保文本已经更新
        ChangeListener<String> historyChangeListener = (observable, oldValue, newValue) -> {
            // 稍后执行滚动操作，确保文本已经更新
            javafx.application.Platform.runLater(() -> {
                historyArea.positionCaret(historyArea.getText().length());
            });
        };

        // 绑定属性并添加监听器
        historyArea.textProperty().bind(viewModel.historyProperty());
        viewModel.historyProperty().addListener(historyChangeListener);
    }

    /**
     * 显示关于对话框
     */
    @FXML
    private void aboutMenuItem() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/emu/nebula/nbcommand/about-dialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(i18n.getString("menu.about"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(historyArea.getScene().getWindow());

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.show();
        } catch (Exception e) {
            logger.error("无法加载关于对话框", e);
            viewModel.addToHistory("无法打开关于对话框: " + e.getMessage());
        }
    }

    /**
     * 获取当前语言的分类列表
     * @return 当前语言下的分类列表
     */
    private ObservableList<String> getCategoryList() {
        // 获取所有分类名称
        Set<String> categoryNames = commandsByCategory.keySet();
        return javafx.collections.FXCollections.observableArrayList(categoryNames);
    }

    /**
     * 根据选择的分类更新命令表格
     */
    private void updateCommandTable(String category) {
        if (category != null && commandsByCategory.containsKey(category)) {
            commandTable.setItems(commandsByCategory.get(category));
        } else {
            commandTable.setItems(javafx.collections.FXCollections.observableArrayList());
            // 只有当category不是null时才添加到历史记录
            if (category != null) {
                viewModel.addToHistory(i18n.getString("message.category_not_found") + category);
                logger.warn("未找到命令分类: {}", category);
            }
        }
    }

    /**
     * 保存配置
     */
    @FXML
    private void handleSaveConfig() {
        viewModel.saveConfiguration();
    }

    @FXML
    private void handleExecuteCommand() {
        viewModel.executeCommand(commandPreviewField.getText());
    }

    @FXML
    private void handleSendCustomCommand() {
        viewModel.executeCustomCommand(customCommandArea.getText());
    }

    /**
     * 切换到中文界面
     */
    @FXML
    private void switchToChinese() {
        switchLanguage(new java.util.Locale("zh", "CN"));
    }

    /**
     * 切换到英文界面
     */
    @FXML
    private void switchToEnglish() {
        switchLanguage(new java.util.Locale("en", "US"));
    }

    /**
     * 切换到日文界面
     */
    @FXML
    private void switchToJapanese() {
        switchLanguage(new java.util.Locale("ja", "JP"));
    }

    /**
     * 切换到韩文界面
     */
    @FXML
    private void switchToKorean() {
        switchLanguage(new java.util.Locale("ko", "KR"));
    }

    /**
     * 通用语言切换方法
     */
    private void switchLanguage(Locale locale) {
        // 检查是否切换到相同的语言
        if (locale.equals(i18n.getCurrentLocale())) {
            return;
        }

        i18n.setLocale(locale);
        updateUIText();
        
        // 重新加载命令数据
        commandsByCategory.clear();
        viewModel.loadCommandsByCategory(commandsByCategory);

        // 更新分类列表
        updateCategories();

        // 重新加载类型化数据并更新UI控件
        viewModel.getTypedDataManager().reloadData();
        uiController.updateTypedDataControls();
    }

    /**
     * 更新分类列表
     */
    private void updateCategories() {
        categoryList.setItems(getCategoryList());
        // 尝试选择之前选中的分类，如果没有则选择第一个
        if (lastSelectedCategory != null && commandsByCategory.containsKey(lastSelectedCategory)) {
            categoryList.getSelectionModel().select(lastSelectedCategory);
        } else {
            categoryList.getSelectionModel().selectFirst();
        }
    }

    /**
     * 更新界面文本
     */
    private void updateUIText() {
        // 菜单
        optionsMenu.setText(i18n.getString("menu.options"));
        checkUpdateMenuItem.setText(i18n.getString("menu.check_update"));
        aboutMenuItem.setText(i18n.getString("menu.about"));
        languageMenu.setText(i18n.getString("menu.language"));

        // 工具栏
        serverAddressLabel.setText(i18n.getString("toolbar.server_address"));
        tokenLabel.setText(i18n.getString("toolbar.token"));
        saveButton.setText(i18n.getString("toolbar.save"));
        savedServerLabel.setText(i18n.getString("toolbar.saved_server"));
        savedTokenLabel.setText(i18n.getString("toolbar.saved_token"));
        authTokenField.setPromptText(i18n.getString("toolbar.token"));

        // 左侧面板
        categoriesLabel.setText(i18n.getString("panel.categories"));
        historyLabel.setText(i18n.getString("panel.history"));

        // 中侧面板
        commandsLabel.setText(i18n.getString("panel.commands"));
        commandNameColumn.setText(i18n.getString("panel.command_name"));
        commandDescColumn.setText(i18n.getString("panel.description"));
        detailsLabel.setText(i18n.getString("panel.details"));

        // 右侧面板
        executeLabel.setText(i18n.getString("panel.execute"));
        selectedCommandLabel.setText(i18n.getString("panel.selected_command"));
        parametersLabel.setText(i18n.getString("panel.parameters"));
        previewLabel.setText(i18n.getString("panel.preview"));
        uidLabel.setText(i18n.getString("panel.uid"));
        executeButton.setText(i18n.getString("panel.run"));
        customLabel.setText(i18n.getString("panel.custom"));
        sendCustomButton.setText(i18n.getString("panel.send_custom"));
        customCommandArea.setPromptText(i18n.getString("panel.custom_prompt"));

        // 底部
        developerLabel.setText("by: 战意电竞丶圆头奶龙仙人");
        versionLabel.setText(i18n.getString("label.name") + " " + Launcher.version);
    }

    /**
     * 手动检查更新
     */
    @FXML
    private void checkForUpdates() {
        checkUpdate(false);
    }
    
    /**
     * 在后台检查更新
     */
    private void checkForUpdatesInBackground() {
        Thread updateThread = new Thread(() -> {
            try {
                Thread.sleep(5000); // 延迟几秒再检查更新，确保UI已加载
                checkUpdate(true);
            } catch (Exception e) {
                logger.warn("后台检查更新时发生异常: {}", e.getMessage());
            }
        });

        updateThread.setDaemon(true);
        updateThread.start();
    }

    private void checkUpdate(boolean isInBackground) {
        try {
            // 创建HTTP客户端
            HttpResponse<String> response;
            try (HttpClient client = HttpClient.newHttpClient()) {

                // 构建请求
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.github.com/repos/HongchengQ/NB-Command/releases/latest"))
                        .header("Accept", "application/vnd.github.v3+json")
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();

                // 发送请求并获取响应
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            }

            if (response.statusCode() == 200) {
                // 解析JSON响应
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(response.body());

                String latestVersion = rootNode.path("tag_name").asText();
                String releaseUrl = rootNode.path("html_url").asText();
                String _releaseNotes = rootNode.path("body").asText();

                // 比较版本
                if (!latestVersion.equals(Launcher.version)) {
                    // 显示更新对话框
                    showUpdateDialog(latestVersion, releaseUrl);
                } else if (!isInBackground) {
                    // 显示已是最新版本
                    showAlert(i18n.getString("dialog.no_updates_title"),
                            i18n.getString("dialog.no_updates_message"));
                }
            } else if (!isInBackground) {
                logger.error("检查更新失败，HTTP状态码: {}", response.statusCode());
                showAlert(i18n.getString("dialog.update_check_failed_title"),
                        i18n.getString("dialog.update_check_failed_message"));
            }
        } catch (Exception e) {
            logger.error("检查更新时发生异常", e);

            if (isInBackground) return;

            showAlert(i18n.getString("dialog.update_check_failed_title"),
                    i18n.getString("dialog.update_check_failed_message") + ": " + e.getMessage());
        }
    }
    
    /**
     * 显示更新对话框
     */
    private void showUpdateDialog(String latestVersion, String releaseUrl) {
        // 确保在JavaFX应用程序线程中执行
        if (!javafx.application.Platform.isFxApplicationThread()) {
            javafx.application.Platform.runLater(() -> showUpdateDialog(latestVersion, releaseUrl));
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(i18n.getString("dialog.update_available_title"));
        alert.setHeaderText(i18n.getString("dialog.update_available_header"));
        
        // 设置内容文本
        String contentText = i18n.getString("dialog.current_version") + Launcher.version + "\n" +
                           i18n.getString("dialog.latest_version") + latestVersion;
        alert.setContentText(contentText);
        
        // 添加自定义按钮
        ButtonType goToUpdateButtonType = new ButtonType(i18n.getString("dialog.go_to_update"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(i18n.getString("dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        
        alert.getButtonTypes().setAll(goToUpdateButtonType, cancelButtonType);
        
        // 等待用户响应
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == goToUpdateButtonType) {
                try {
                    java.awt.Desktop.getDesktop().browse(new URI(releaseUrl));
                } catch (Exception ex) {
                    logger.error("无法打开浏览器", ex);
                    showAlert(i18n.getString("dialog.update_open_failed_title"), 
                              i18n.getString("dialog.update_open_failed_message"));
                }
            }
        });
    }
    
    /**
     * 显示简单提示对话框
     */
    private void showAlert(String title, String message) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}