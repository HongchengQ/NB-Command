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
     * build 命令 - 构建角色和光锥配置
     */
    private Command createBuildCommand() {
        Syntax syntax = new Syntax()
                .add("build")
                .add("characters", getI18Name("param.character_id"), Syntax.FieldMode.MULTI_SELECT_CONTAINER, " ")
                .add("discs", getI18Name("param.disc_id"), Syntax.FieldMode.MULTI_SELECT_CONTAINER, " ")
                .add("potentials", getI18Name("param.potential_id"), Syntax.FieldMode.MULTI_SELECT_CONTAINER_WITH_COUNT, " ")
                .add("subNoteSkills", getI18Name("param.melody_id"), Syntax.FieldMode.MULTI_SELECT_CONTAINER_WITH_COUNT, " ")
                ;

        return createCommand(
                "command.build.name",
                "command.build.description",
                "command.build.full_description",
                syntax
        );
    }
}