package emu.nebula.nbcommand.service.command.manager;

import emu.nebula.nbcommand.model.Command;
import emu.nebula.nbcommand.model.command.Syntax;
import emu.nebula.nbcommand.service.command.BaseCommandManager;

import java.util.List;
import java.util.function.Supplier;

public class BuildManager extends BaseCommandManager {
    /**
     * 获取类别所有指令
     *
     * @return 指令列表(函数接口)
     */
    @Override
    public List<Supplier<Command>> getCategoryCommands() {
        return List.of(
                this::createBuildCommand
        );
    }

    /**
     * build 命令 - ???
     * todo ids 多选框实现
     */
    private Command createBuildCommand() {
        Syntax syntax = new Syntax()
                .add("build")
                .add("char ids", getI18Name("param.ids"))
                .add("char ids", getI18Name("param.ids"))
                .add("disc ids", getI18Name("param.ids"))
                .add("potential ids", getI18Name("param.ids"))
                .add("melody ids", getI18Name("param.ids"));

        return createCommand(
                "command.build.name",
                "command.build.description",
                "command.build.full_description",
                syntax
        );
    }
}