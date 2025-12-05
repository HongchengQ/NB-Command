package emu.nebula.nbcommand.service.command;

import emu.nebula.nbcommand.ui.MultiSelectContainerManager;
import emu.nebula.nbcommand.ui.MultiSelectWithCountContainerManager;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;

import java.util.List;
import java.util.Map;

/**
 * 辅助类，用于从多选容器中提取数据
 */
public class MultiSelectDataHelper {
    
    /**
     * 从多选容器管理器映射中获取指定参数的已选项
     * 
     * @param multiSelectManagers 多选容器管理器映射
     * @param parameterName 参数名称
     * @return 已选项列表，如果未找到则返回null
     */
    public static List<String> getSelectedItems(Map<String, MultiSelectContainerManager> multiSelectManagers, 
                                               String parameterName) {
        MultiSelectContainerManager manager = multiSelectManagers.get(parameterName);
        if (manager != null) {
            return manager.getSelectedItems();
        }
        return null;
    }
    
    /**
     * 从带数量的多选容器管理器映射中获取指定参数的已选项及数量
     * 
     * @param multiSelectWithCountManagers 带数量的多选容器管理器映射
     * @param parameterName 参数名称
     * @return 已选项及数量映射，如果未找到则返回null
     */
    public static Map<String, Integer> getSelectedItemsWithCount(Map<String, MultiSelectWithCountContainerManager> multiSelectWithCountManagers, 
                                                               String parameterName) {
        MultiSelectWithCountContainerManager manager = multiSelectWithCountManagers.get(parameterName);
        if (manager != null) {
            return manager.getSelectedItemsWithCount();
        }
        return null;
    }
    
    /**
     * 从控件中获取值，特别处理ComboBox的情况
     * 
     * @param control 控件
     * @return 控件的值
     */
    public static String getValueFromControl(Control control) {
        if (control instanceof ComboBox) {
            String selected = (String) ((ComboBox<?>) control).getSelectionModel().getSelectedItem();
            String input = ((ComboBox<?>) control).getEditor().getText();
            return selected != null ? selected : input != null ? input : "";
        }
        return "";
    }
}