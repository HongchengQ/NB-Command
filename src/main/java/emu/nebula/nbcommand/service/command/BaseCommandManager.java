package emu.nebula.nbcommand.service.command;

import emu.nebula.nbcommand.model.Command;
import emu.nebula.nbcommand.model.command.Syntax;
import emu.nebula.nbcommand.service.I18nManager;

import java.util.List;
import java.util.function.Supplier;

public abstract class BaseCommandManager {
    /**
     * 获取国际化名称
     * @param str 需要国际化的字符串键
     * @return 国际化后的字符串
     */
    protected static String getI18Name(String str) {
        return I18nManager.getInstance().getString(str);
    }

    /**
     * 通用创建命令方法
     * @param nameKey 命令名称键
     * @param descriptionKey 命令描述键
     * @param syntax 指令需要的参数
     * @param fullDescriptionKey 命令完整描述键
     */
    protected static Command createCommand(String nameKey, String descriptionKey, String fullDescriptionKey, Syntax syntax) {
        return new Command(
                getI18Name(nameKey),
                getI18Name(descriptionKey),
                syntax,
                getI18Name(fullDescriptionKey)
        );
    }

    /**
     * 获取类别所有指令
     *
     * @return 指令列表(函数接口)
     */
    public abstract List<Supplier<Command>> getCategoryCommands();
}