package emu.nebula.nbcommand.service.command;

import emu.nebula.nbcommand.model.Command;
import emu.nebula.nbcommand.model.ServerRspData;
import emu.nebula.nbcommand.model.command.Syntax;
import emu.nebula.nbcommand.service.command.MultiSelectDataHelper;
import emu.nebula.nbcommand.ui.MultiSelectContainerManager;
import emu.nebula.nbcommand.ui.MultiSelectWithCountContainerManager;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CommandExecutor {
    private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

    private String serverAddress;
    private String authToken;
    private Map<String, MultiSelectContainerManager> multiSelectManagers;
    private Map<String, MultiSelectWithCountContainerManager> multiSelectWithCountManagers;

    public CommandExecutor(String serverAddress, String authToken) {
        this.serverAddress = serverAddress;
        this.authToken = authToken;
    }

    /**
     * 更新配置
     */
    public void updateConfiguration(String serverAddress, String authToken) {
        this.serverAddress = serverAddress;
        this.authToken = authToken;
    }

    /**
     * 设置多选管理器映射
     */
    public void setMultiSelectManagers(Map<String, MultiSelectContainerManager> multiSelectManagers) {
        this.multiSelectManagers = multiSelectManagers;
    }
    
    /**
     * 设置带数量的多选管理器映射
     */
    public void setMultiSelectWithCountManagers(Map<String, MultiSelectWithCountContainerManager> multiSelectWithCountManagers) {
        this.multiSelectWithCountManagers = multiSelectWithCountManagers;
    }

    /**
     * 构建要发送的命令文本
     */
    public String buildCommandText(Command command, Map<String, Control> parameterControls) {
        StringBuilder commandText = new StringBuilder();
        
        // 遍历语法定义中的字段
        for (Syntax.Field field : command.syntax().getFields()) {
            String originalName = field.getOriginalName();
            
            // 第一个字段作为命令名称
            if (commandText.isEmpty()) {
                commandText.append(originalName);
                continue;
            }
            
            // 特殊处理多选容器字段
            if (field.getFieldMode() == Syntax.FieldMode.MULTI_SELECT_CONTAINER && multiSelectManagers != null) {
                List<String> selectedItems = MultiSelectDataHelper.getSelectedItems(multiSelectManagers, originalName);
                if (selectedItems != null && !selectedItems.isEmpty()) {
                    commandText.append(" ");
                    for (int i = 0; i < selectedItems.size(); i++) {
                        String item = selectedItems.get(i);

                        // 对于包含" - "的值（如"10001 - 物品名"），只取ID部分
                        if (item.contains(" - ")) {
                            String idPart = item.substring(0, item.indexOf(" - "));
                            commandText.append(idPart);
                        } else {
                            commandText.append(item);
                        }

                        // 如果不是最后一个元素，添加分隔符
                        if (i != selectedItems.size() - 1) {
                            String delimiter = field.getSpecialPrefix();
                            if (delimiter != null && !delimiter.isEmpty())
                                commandText.append(delimiter);
                            else
                                commandText.append(",");
                        }
                    }
                }
                continue;
            }
            
            // 特殊处理带数量的多选容器字段
            if (field.getFieldMode() == Syntax.FieldMode.MULTI_SELECT_CONTAINER_WITH_COUNT && multiSelectWithCountManagers != null) {
                Map<String, Integer> selectedItemsWithCount = MultiSelectDataHelper.getSelectedItemsWithCount(multiSelectWithCountManagers, originalName);
                if (selectedItemsWithCount != null && !selectedItemsWithCount.isEmpty()) {
                    int index = 0;

                    commandText.append(" ");
                    for (Map.Entry<String, Integer> entry : selectedItemsWithCount.entrySet()) {
                        String item = entry.getKey();
                        Integer count = entry.getValue();

                        // 对于包含" - "的值（如"10001 - 物品名"），只取ID部分
                        if (item.contains(" - ")) {
                            String idPart = item.substring(0, item.indexOf(" - "));
                            commandText.append(idPart);
                        } else {
                            commandText.append(item);
                        }
                        
                        // 添加数量前缀
                        commandText.append(":").append(count);

                        // 如果不是最后一个元素，添加分隔符
                        if (index != selectedItemsWithCount.size() - 1) {
                            String delimiter = field.getSpecialPrefix();
                            if (delimiter != null && !delimiter.isEmpty())
                                commandText.append(delimiter);
                            else
                                commandText.append(",");
                        }
                        index++;
                    }
                }
                continue;
            }
            
            // 查找对应控件的值
            Control control = parameterControls.get(originalName);
            
            if (control != null) {
                String value = "";
                if (control instanceof TextField) {
                    value = ((TextField) control).getText();
                } else if (control instanceof ComboBox) {
                    String selected = (String) ((ComboBox<?>) control).getSelectionModel().getSelectedItem();
                    String input = ((ComboBox<?>) control).getEditor().getText();
                    value = selected != null ? selected : input != null ? input : "";
                }

                // 只有当值非空时才添加到命令中
                if (!value.isEmpty()) {
                    commandText.append(" ");
                    // 对于包含" - "的值（如"10001 - 物品名"），只取ID部分
                    if (value.contains(" - ")) {
                        value = value.substring(0, value.indexOf(" - "));
                    }

                    if (field.getFieldMode() == Syntax.FieldMode.SPECIAL_PREFIX) {
                        // 使用实际的前缀字符，而不是固定的"x"
                        commandText.append(field.getSpecialPrefix()).append(value);
                    } else {
                        commandText.append(value);
                    }
                }
            }
        }

        return commandText.toString();
    }

    /**
     * 发送命令到服务器的通用方法
     */
    public HttpResponse<String> sendCommandToServer(String commandText) {
        // 发送POST请求到服务器
        try {
            try (HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build()) {

                String jsonBody = "{\"token\": \"" + authToken +
                        "\", \"command\": \"" + commandText + "\"}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(serverAddress + "/api/command"))
                        .timeout(Duration.ofSeconds(30))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                return client.send(request, HttpResponse.BodyHandlers.ofString());
            }
        } catch (Exception e) {
            logger.error("发送命令时发生异常", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行命令
     */
    public void executeCommand(String uid, String commandText, Consumer<String> historyConsumer) {
        try {
            if (uid != null && !uid.isEmpty()) {
                commandText += " @" + uid;
            }

            HttpResponse<String> response = sendCommandToServer(commandText);

            String message = extractMessageFromResponse(response.body());

            if (response.statusCode() == 200) {
                historyConsumer.accept("> " + commandText + "\n" + message);
                logger.info("命令执行成功: {}; 服务端返回: {}", commandText, message);
            } else {
                historyConsumer.accept(response.statusCode() + " - " + message);
                logger.error("命令执行失败: {} - {}", response.statusCode(), message);
            }
        } catch (Exception e) {
            historyConsumer.accept("command sends exceptions: " + e.getMessage());
            logger.error("发送命令时发生异常", e);

            if (e.getMessage().equals("java.net.ConnectException"))
                historyConsumer.accept("Please check if the remote server is online");
        }
    }

    /**
     * 从服务器响应中提取消息
     */
    private String extractMessageFromResponse(String responseBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ServerRspData response = mapper.readValue(responseBody, ServerRspData.class);
            return response.getMsg() != null ? response.getMsg() : responseBody;
        } catch (Exception e) {
            logger.warn("解析服务器响应失败: {}, 返回原始响应", e.getMessage());
            return responseBody;
        }
    }
}