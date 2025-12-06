package emu.nebula.nbcommand.service.command.manager;

import emu.nebula.nbcommand.model.Command;
import emu.nebula.nbcommand.model.command.Syntax;
import emu.nebula.nbcommand.model.command.Syntax.FieldMode;
import emu.nebula.nbcommand.service.command.BaseCommandManager;

import java.util.List;
import java.util.function.Supplier;

public class ItemManager extends BaseCommandManager {
    /**
     * 获取类别所有指令
     *
     * @return 指令列表(函数接口)
     */
    @Override
    public List<Supplier<Command>> getCategoryCommands() {
        return List.of(
                this::createGiveCommand,
                this::createGiveAllCommand,
                this::createCleanSingleCommand,
                this::createCleanBatchCommand
        );
    }

    /**
     * give命令 - 给予单个物品
     */
    private Command createGiveCommand() {
        Syntax syntax = new Syntax().add("give")
                .add("items", getI18Name("param.item_id"), FieldMode.COMPLEX_RADIO)
                .add("x(amount)", getI18Name("param.amount"), FieldMode.SPECIAL_PREFIX, "x");

        return createCommand(
                "command.give.name",
                "command.give.description",
                "command.give.full_description",
                syntax
        );
    }

    /**
     * giveall命令 - 给予所有物品
     */
    private Command createGiveAllCommand() {
        Syntax syntax = new Syntax()
                .add("giveall")
                .add("{characters | discs | materials}", getI18Name("param.type"), FieldMode.SIMPLE_RADIO)
                .add("t(talent/crescendo level)", getI18Name("param.talent_level"), FieldMode.SPECIAL_PREFIX, "t")
                .add("s(skill level)", getI18Name("param.skill_level"), FieldMode.SPECIAL_PREFIX, "s");

        return createCommand(
                "command.giveall.name",
                "command.giveall.description",
                "command.giveall.full_description",
                syntax
        );
    }

    /**
     * clean命令 - 批量删除物品
     */
    private Command createCleanBatchCommand() {
        Syntax syntax = new Syntax()
                .add("clean")
                .add("{all | items | resources}", getI18Name("param.clean_type"), FieldMode.SIMPLE_RADIO);

        return createCommand(
                "command.clean_batch.name",
                "command.clean_batch.description",
                "command.clean_batch.full_description",
                syntax
        );
    }

    /**
     * clean命令 - 单独删除物品
     */
    private Command createCleanSingleCommand() {
        Syntax syntax = new Syntax()
                .add("clean")
                .add("[id]", getI18Name("param.id"));

        return createCommand(
                "command.clean_single.name",
                "command.clean_single.description",
                "command.clean_single.full_description",
                syntax
        );
    }
}