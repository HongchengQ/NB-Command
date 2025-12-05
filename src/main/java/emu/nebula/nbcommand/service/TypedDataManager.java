package emu.nebula.nbcommand.service;

import emu.nebula.nbcommand.model.TypedData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 类型化数据管理器
 * 用于管理和过滤带类型的数据，如物品、角色等
 */
public class TypedDataManager {
    private static final Logger logger = LoggerFactory.getLogger(TypedDataManager.class);
    
    // 存储所有类型化数据的映射
    // 键为数据标识符(如"item id")，值为按类型分组的数据列表
    private final Map<String, Map<String, ObservableList<TypedData>>> typedDataMaps = new HashMap<>();
    
    // 记录上次加载的语言
    private Locale lastLoadedLocale = null;
    
    /**
     * 加载物品数据
     */
    public void loadItemData() {
        loadItemDataBasedOnLanguage();
    }

    /**
     * 根据当前语言加载物品数据
     */
    public void loadItemDataBasedOnLanguage() {
        // 获取当前语言
        Locale currentLocale = I18nManager.getInstance().getCurrentLocale();
        
        // 如果语言未改变，则不重新加载
        if (lastLoadedLocale != null && lastLoadedLocale.equals(currentLocale)) {
            return;
        }
        
        // 清除现有数据
        typedDataMaps.clear();

        try {
            String languageCode = I18nManager.getLanguageCode();
            
            // 加载物品数据
            loadTypedDataFromJson("/handbook/" + languageCode + "/Items.json", "items", "items", "title", "type");
            // 加载角色数据
            loadTypedDataFromJson("/handbook/" + languageCode + "/Characters.json", "characters", "characters", "name", "element");
            // 加载秘纹数据
            loadTypedDataFromJson("/handbook/" + languageCode + "/Discs.json", "discs", "discs", "name", "element");

            // 更新最后加载的语言
            lastLoadedLocale = currentLocale;
            
            logger.info("已加载 {} 语言的数据", languageCode);
        } catch (Exception e) {
            logger.error("加载手册数据时出错", e);
        }
    }
    
    /**
     * 强制重新加载当前语言的数据
     */
    public void reloadData() {
        // 清除语言标记，强制重新加载
        lastLoadedLocale = null;
        loadItemDataBasedOnLanguage();
    }
    

    
    /**
     * 从JSON文件加载类型化数据的通用方法
     * 
     * @param resourcePath JSON资源路径
     * @param arrayFieldName JSON数组字段名
     * @param dataIdentifier 数据标识符 - 与命令执行框显示的参数一致
     * @param nameFieldName 名称字段名 - 字符(多种语言 对应游戏显示内容)
     * @param typeFieldName 类型字段名 - 过滤器用
     */
    private void loadTypedDataFromJson(String resourcePath, String arrayFieldName,
                                       String dataIdentifier, String nameFieldName, String typeFieldName) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            
            try (InputStream stream = getClass().getResourceAsStream(resourcePath)) {
                if (stream != null) {
                    JsonNode root = mapper.readTree(stream);
                    JsonNode dataArray = root.get(arrayFieldName);
                    
                    // 创建不同类型的列表
                    Map<String, ObservableList<TypedData>> typedLists = new HashMap<>();
                    typedLists.put("all", FXCollections.observableArrayList());
                    
                    if (dataArray != null && dataArray.isArray()) {
                        for (JsonNode node : dataArray) {
                            String id = String.valueOf(node.get("id").asInt());
                            String name = node.get(nameFieldName).asText();
                            String type = node.has(typeFieldName) ? node.get(typeFieldName).asText() : "Unknown";
                            
                            TypedData data = new TypedData(id, name, type);
                            
                            // 添加到主列表
                            typedLists.get("all").add(data);
                            
                            // 添加到类型列表
                            if (!typedLists.containsKey(type)) {
                                typedLists.put(type, FXCollections.observableArrayList());
                            }
                            typedLists.get(type).add(data);
                        }
                    }
                    
                    typedDataMaps.put(dataIdentifier, typedLists);
                }
            }
        } catch (Exception e) {
            logger.error("加载数据时出错: resourcePath={}, arrayFieldName={}, dataIdentifier={}", 
                         resourcePath, arrayFieldName, dataIdentifier, e);
        }
    }
    
    /**
     * 获取指定数据标识符的所有类型
     * @param dataIdentifier 数据标识符，如"item id"
     * @return 类型集合
     */
    public Set<String> getTypes(String dataIdentifier) {
        if (typedDataMaps.containsKey(dataIdentifier)) {
            return typedDataMaps.get(dataIdentifier).keySet();
        }
        return Set.of();
    }
    
    /**
     * 获取指定数据标识符和类型的数据显示列表
     * @param dataIdentifier 数据标识符，如"item id"
     * @param type 类型，如"Res"，"all"表示所有类型
     * @return 数据显示列表
     */
    public ObservableList<String> getDataList(String dataIdentifier, String type) {
        ObservableList<String> result = FXCollections.observableArrayList();
        
        if (typedDataMaps.containsKey(dataIdentifier)) {
            Map<String, ObservableList<TypedData>> typeMap = typedDataMaps.get(dataIdentifier);
            if (typeMap.containsKey(type)) {
                for (TypedData data : typeMap.get(type)) {
                    result.add(data.toString());
                }
            }
        }
        
        return result;
    }
    
    /**
     * 根据显示字符串获取原始ID
     * @param displayString 显示字符串，如"10001 - 物品名称"
     * @return 原始ID，如"10001"
     */
    public String extractIdFromDisplayString(String displayString) {
        if (displayString != null && displayString.contains(" - ")) {
            return displayString.substring(0, displayString.indexOf(" - "));
        }
        return displayString;
    }
    
    /**
     * 获取所有数据
     * @return 类型化数据映射
     */
    public Map<String, Map<String, ObservableList<TypedData>>> getAllData() {
        return typedDataMaps;
    }
}