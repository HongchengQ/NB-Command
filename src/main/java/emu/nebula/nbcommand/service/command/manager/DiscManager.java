package emu.nebula.nbcommand.service.command.manager;

import emu.nebula.nbcommand.model.Command;
import emu.nebula.nbcommand.model.command.Syntax;
import emu.nebula.nbcommand.service.command.BaseCommandManager;

import java.util.List;
import java.util.function.Supplier;

public class DiscManager extends BaseCommandManager {
    /**
     * 获取类别所有指令
     *
     * @return 指令列表(函数接口)
     */
    @Override
    public List<Supplier<Command>> getCategoryCommands() {
        return List.of(
                this::createDiscAllCommand,
                this::createDiscCommand
        );
    }

    /**
     * disc all命令 - 获取所有秘纹
     */
    private Command createDiscAllCommand() {
        Syntax syntax = new Syntax()
                .add("disc all")
                .add("lv(level)", getI18Name("param.level"), Syntax.FieldMode.SPECIAL_PREFIX, "lv")
                .add("a(ascension)", getI18Name("param.ascension"), Syntax.FieldMode.SPECIAL_PREFIX, "a")
                .add("c(crescendo level)", getI18Name("param.talent_level"), Syntax.FieldMode.SPECIAL_PREFIX, "c");

        return createCommand(
                "command.disc_all.name",
                "command.disc_all.description",
                "command.disc_all.full_description",
                syntax
        );
    }

    /**
     * disc 命令 - 获取单一秘纹
     */
    private Command createDiscCommand() {
        Syntax syntax = new Syntax()
                .add("disc")
                .add("discs", getI18Name("param.id"), Syntax.FieldMode.COMPLEX_RADIO)
                .add("lv(level)", getI18Name("param.level"), Syntax.FieldMode.SPECIAL_PREFIX, "lv")
                .add("a(ascension)", getI18Name("param.ascension"), Syntax.FieldMode.SPECIAL_PREFIX, "a")
                .add("c(crescendo level)", getI18Name("param.talent_level"), Syntax.FieldMode.SPECIAL_PREFIX, "c");

        return createCommand(
                "command.disc_single.name",
                "command.disc_single.description",
                "command.disc_single.full_description",
                syntax
        );
    }
}