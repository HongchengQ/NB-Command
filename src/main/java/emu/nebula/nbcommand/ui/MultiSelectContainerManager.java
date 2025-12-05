package emu.nebula.nbcommand.ui;

import emu.nebula.nbcommand.service.TypedDataManager;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理多选容器控件，处理添加、删除和维护多个选择项
 */
public class MultiSelectContainerManager {
    private static final String ITEM_BOX_STYLE = "-fx-background-color: #f0f0f0; -fx-padding: 1px 2px; -fx-alignment: center-left; -fx-spacing: 2px;";
    private static final String LABEL_STYLE = "-fx-font-size: 12px;";
    private static final String REMOVE_BUTTON_STYLE = "-fx-background-color: #ff6666; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 1 2 1 2;";

    private final ComboBox<String> dataComboBox;
    private final FlowPane selectedItemsContainer;
    private final TypedDataManager typedDataManager;
    private final String dataIdentifier;
    private final List<String> selectedItems = new ArrayList<>();
    private final TypedComboBoxManager comboBoxManager;
    private Runnable onItemsChanged; // 当项目变更时调用的回调函数

    public MultiSelectContainerManager(ComboBox<String> dataComboBox, 
                                     FlowPane selectedItemsContainer,
                                     TypedDataManager typedDataManager, 
                                     String dataIdentifier) {
        this.dataComboBox = dataComboBox;
        this.selectedItemsContainer = selectedItemsContainer;
        this.typedDataManager = typedDataManager;
        this.dataIdentifier = dataIdentifier;
        
        // 创建ComboBox管理器
        this.comboBoxManager = new TypedComboBoxManager(dataComboBox, typedDataManager, dataIdentifier);
    }

    /**
     * 设置项目变更回调
     * @param callback 回调函数
     */
    public void setOnItemsChanged(Runnable callback) {
        this.onItemsChanged = callback;
    }

    /**
     * 添加选中项到容器
     */
    public void addItem() {
        String selectedItem = dataComboBox.getEditor().getText();
        if (selectedItem != null && !selectedItem.isEmpty() && !selectedItems.contains(selectedItem)) {
            selectedItems.add(selectedItem);
            updateSelectedItemsView();
            // 清空选择框
            dataComboBox.getEditor().clear();
            dataComboBox.hide();
            
            // 触发项目变更回调
            if (onItemsChanged != null) {
                onItemsChanged.run();
            }
        }
    }

    /**
     * 从容器中移除指定项
     * @param item 要移除的项
     */
    public void removeItem(String item) {
        selectedItems.remove(item);
        updateSelectedItemsView();
        
        // 触发项目变更回调
        if (onItemsChanged != null) {
            onItemsChanged.run();
        }
    }

    /**
     * 更新已选项视图
     */
    private void updateSelectedItemsView() {
        // 清空当前显示
        selectedItemsContainer.getChildren().clear();
        
        // 为每个已选项创建显示控件
        for (String item : selectedItems) {
            HBox itemBox = createSelectedItemBox();
            Label itemLabel = new Label(item);
            itemLabel.setWrapText(false);
            itemLabel.setStyle(LABEL_STYLE);
            itemLabel.setEllipsisString("...");
            itemLabel.setMinWidth(50); // 确保即使在空间不足时也能看到部分内容
            
            // 删除按钮
            Button removeButton = new Button("×");
            removeButton.setStyle(REMOVE_BUTTON_STYLE);
            removeButton.setMinWidth(Region.USE_PREF_SIZE);
            removeButton.setPrefSize(16, 16);
            removeButton.setOnAction(event -> removeItem(item));
            
            // 使用Region作为弹性填充，使控件靠右对齐
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            HBox.setHgrow(itemLabel, Priority.ALWAYS);
            itemBox.getChildren().addAll(itemLabel, spacer, removeButton);
            
            selectedItemsContainer.getChildren().add(itemBox);
        }
        
        // 强制重新计算容器布局
        selectedItemsContainer.requestLayout();
    }

    /**
     * 创建已选项的显示框（包含删除按钮）
     * @return 包含选项和删除按钮的HBox
     */
    private HBox createSelectedItemBox() {
        HBox itemBox = new HBox(2);
        itemBox.setStyle(ITEM_BOX_STYLE);
        itemBox.setMinWidth(Region.USE_PREF_SIZE);
        return itemBox;
    }

    /**
     * 获取所有已选项
     * @return 已选项列表
     */
    public List<String> getSelectedItems() {
        return new ArrayList<>(selectedItems);
    }

    /**
     * 获取数据标识符
     */
    public String getDataIdentifier() {
        return dataIdentifier;
    }

    /**
     * 获取ComboBox管理器
     */
    public TypedComboBoxManager getComboBoxManager() {
        return comboBoxManager;
    }
    
    /**
     * 更新过滤器
     */
    public void updateFilter() {
        comboBoxManager.updateFilter();
    }
    
    /**
     * 更新类型
     */
    public void updateType(String type) {
        comboBoxManager.updateType(type);
    }
    
    /**
     * 重新加载数据
     * 当语言切换时调用此方法更新内部状态
     */
    public void reloadData() {
        comboBoxManager.reloadData();
    }
}