package emu.nebula.nbcommand.service.command;

import emu.nebula.nbcommand.model.Command;
import emu.nebula.nbcommand.service.I18nManager;
import emu.nebula.nbcommand.service.command.manager.*;
import emu.nebula.nbcommand.service.command.manager.CharacterManager;

import java.util.*;
import java.util.function.Supplier;

/**
 * 命令管理系统
 * 负责创建和管理所有游戏命令
 */
public class CommandRegistry {
    public static I18nManager i18n = I18nManager.getInstance();
    
    /**
     * 定义命令结构
     */
    private static Map<String, List<Supplier<Command>>> defineCommandStructure() {
        Map<String, List<Supplier<Command>>> structure = new LinkedHashMap<>();
        
        // 玩家管理类别
        structure.put("category.player_management", new PlayerManager().getCategoryCommands());
        
        // 物品管理类别
        structure.put("category.item_management", new ItemManager().getCategoryCommands());

        // 角色管理类别
        structure.put("category.character_management", new CharacterManager().getCategoryCommands());

        // 秘纹管理类别
        structure.put("category.disc_management", new DiscManager().getCategoryCommands());

        // 服务器控制类别
        structure.put("category.server_control", new ServerManager().getCategoryCommands());

        // 邮件系统类别
        structure.put("category.mail_system", new MailManager().getCategoryCommands());

        // 构建系统类别
        structure.put("category.build_system", new BuildManager().getCategoryCommands());
        
        return structure;
    }

    /**
     * 创建所有命令并按类别组织
     *
     * @return 按类别组织的所有命令映射
     */
    public static Map<String, List<Command>> createAllCommands() {
        Map<String, List<Command>> allCommands = new LinkedHashMap<>();

        // 定义所有命令类别和命令
        Map<String, List<Supplier<Command>>> commandStructure = defineCommandStructure();

        // 根据定义创建命令
        for (Map.Entry<String, List<Supplier<Command>>> entry : commandStructure.entrySet()) {
            String categoryKey = entry.getKey();
            List<Supplier<Command>> creators = entry.getValue();

            List<Command> commands = new ArrayList<>();
            for (Supplier<Command> creator : creators) {
                commands.add(creator.get());
            }

            allCommands.put(i18n.getString(categoryKey), commands);
        }

        return allCommands;
    }
}