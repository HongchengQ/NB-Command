package emu.nebula.nbcommand.model.command;

import java.util.ArrayList;
import java.util.List;

/**
 * 表示命令语法的类，支持多语言参数显示
 */
public class Syntax {
    private final List<Field> fields = new ArrayList<>();

    /**
     * 添加一个字段到语法中
     * @param originalName 原始名称（用于发送到服务器）
     * @return Syntax对象本身，支持链式调用
     */
    public Syntax add(String originalName) {
        fields.add(new Field(originalName));
        return this;
    }

    /**
     * 添加一个字段到语法中
     * @param originalName 原始名称（用于发送到服务器）
     * @param fieldMode 字段模式
     * @return Syntax对象本身，支持链式调用
     */
    public Syntax add(String originalName, FieldMode fieldMode) {
        fields.add(new Field(originalName, fieldMode));
        return this;
    }

    /**
     * 添加一个字段到语法中，并指定显示名称
     * @param originalName 原始名称（用于发送到服务器）
     * @param currentName 当前显示名称（用于界面显示，支持多语言）
     * @return Syntax对象本身，支持链式调用
     */
    public Syntax add(String originalName, String currentName) {
        fields.add(new Field(originalName, currentName));
        return this;
    }

    /**
     * 添加一个字段到语法中，并指定显示名称
     * @param originalName 原始名称（用于发送到服务器）
     * @param currentName 当前显示名称（用于界面显示，支持多语言）
     * @param fieldMode 字段模式
     * @return Syntax对象本身，支持链式调用
     */
    public Syntax add(String originalName, String currentName, FieldMode fieldMode) {
        fields.add(new Field(originalName, currentName, fieldMode));
        return this;
    }

    /**
     * 添加一个字段到语法中，并指定显示名称
     * @param originalName 原始名称（用于发送到服务器）
     * @param currentName 当前显示名称（用于界面显示，支持多语言）
     * @param fieldMode 字段模式
     * @param prefix 特殊前缀
     * @return Syntax对象本身，支持链式调用
     */
    public Syntax add(String originalName, String currentName, FieldMode fieldMode, String prefix) {
        fields.add(new Field(originalName, currentName, fieldMode, prefix));
        return this;
    }
    
    /**
     * 获取所有字段
     * @return 字段列表
     */
    public List<Field> getFields() {
        return fields;
    }

    /**
     * 语法中的字段表示
     */
    public static class Field {
        String originalName;
        String currentName;
        FieldMode fieldMode;
        boolean isRequired; // 必填参数 todo 这里不重要以后再写

        String specialPrefix; // 当mode为SPECIAL_PREFIX时才生效

        /**
         * 构造函数，原始名称和显示名称相同
         * @param originalName 原始名称
         */
        public Field(String originalName) {
            this.originalName = originalName;
            this.currentName = originalName;
            this.fieldMode = FieldMode.NONE;
        }

        /**
         * 构造函数，原始名称和显示名称相同
         * @param originalName 原始名称
         * @param fieldMode 字段模式
         */
        public Field(String originalName, FieldMode fieldMode) {
            this.originalName = originalName;
            this.currentName = originalName;
            this.fieldMode = fieldMode;
        }

        /**
         * 构造函数，可以分别指定原始名称和显示名称
         * @param originalName 原始名称
         * @param currentName 显示名称
         */
        public Field(String originalName, String currentName) {
            this.originalName = originalName;
            this.currentName = currentName;
            this.fieldMode = FieldMode.NONE;
        }

        /**
         * 构造函数，可以分别指定原始名称和显示名称
         * @param originalName 原始名称
         * @param currentName 显示名称
         * @param fieldMode 字段模式
         */
        public Field(String originalName, String currentName, FieldMode fieldMode) {
            this.originalName = originalName;
            this.currentName = currentName;
            this.fieldMode = fieldMode;
        }

        /**
         * 构造函数，可以分别指定原始名称和显示名称
         * @param originalName 原始名称
         * @param currentName 显示名称
         * @param fieldMode 字段模式
         * @param prefix 特殊前缀
         */
        public Field(String originalName, String currentName, FieldMode fieldMode, String prefix) {
            this.originalName = originalName;
            this.currentName = currentName;
            this.fieldMode = fieldMode;
            this.specialPrefix = prefix;
        }

        /**
         * 获取原始名称
         * @return 原始名称
         */
        public String getOriginalName() {
            return this.originalName;
        }
        
        /**
         * 获取当前显示名称
         * @return 显示名称
         */
        public String getCurrentName() {
            return this.currentName;
        }

        /**
         * 获取字段模式
         * @return 字段模式
         */
        public FieldMode getFieldMode() {
            return this.fieldMode;
        }

        /**
         * 获取是否为必选项
         * @return 是否为必选项
         */
        public boolean isRequired() {
            return this.isRequired;
        }

        public String getSpecialPrefix() {
            return this.specialPrefix;
        }
    }

    public enum FieldMode {
        NONE,                               // 常规默认
        SIMPLE_RADIO,                       // 简单单选 硬编码 不可翻译 {create | delete}
        COMPLEX_RADIO,                      // 固定单选 使用json配置 不可翻译
        MULTI_SELECT_CONTAINER,             // 多选容器 可以容纳多个选择项
        MULTI_SELECT_CONTAINER_WITH_COUNT,  // 多选容器带数量 可以为每项设置数量 项可重复
        SPECIAL_PREFIX,                     // 用户界面隐藏前缀
        REALLY_HIDDEN                       // 完全隐藏
    }
}