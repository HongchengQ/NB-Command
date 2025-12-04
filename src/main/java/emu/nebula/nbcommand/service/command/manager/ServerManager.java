package emu.nebula.nbcommand.service.command.manager;

import emu.nebula.nbcommand.model.Command;
import emu.nebula.nbcommand.model.command.Syntax;
import emu.nebula.nbcommand.service.command.BaseCommandManager;

import java.util.List;
import java.util.function.Supplier;

public class ServerManager extends BaseCommandManager {
    /**
     * 获取类别所有指令
     *
     * @return 指令列表(函数接口)
     */
    @Override
    public List<Supplier<Command>> getCategoryCommands() {
        return List.of(
                this::createReloadCommand
        );
    }
    
    /**
     * reload命令 - 服务器重载
     */
    private Command createReloadCommand() {
        Syntax syntax = new Syntax()
                .add("reload");

        return createCommand(
                "command.reload.name",
                "command.reload.description",
                "command.reload.full_description",
                syntax
        );
    }

    /**
     * test666命令 - 测试
     */
    private Command createTestCommand() {
        Syntax syntax = new Syntax()
                .add("test666");

        return createCommand(
                "command.test666.name",
                "command.test666.description",
                "command.test666.full_description",
                syntax
        );
    }
}