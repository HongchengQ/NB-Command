package emu.nebula.nbcommand.service.command.manager;

import emu.nebula.nbcommand.model.Command;
import emu.nebula.nbcommand.model.command.Syntax;
import emu.nebula.nbcommand.model.command.Syntax.FieldMode;
import emu.nebula.nbcommand.service.command.BaseCommandManager;

import java.util.List;
import java.util.function.Supplier;

public class CharacterManager extends BaseCommandManager {
    /**
     * 获取类别所有指令
     *
     * @return 指令列表(函数接口)
     */
    @Override
    public List<Supplier<Command>> getCategoryCommands() {
        return List.of(
                this::createCharacterAllCommand,
                this::createCharacterSingleCommand
        );
    }

    /**
     * character all命令 - 获取所有角色
     */
    private Command createCharacterAllCommand() {
        Syntax syntax = new Syntax()
                .add("character all")
                .add("lv(level)", getI18Name("param.level"), FieldMode.SPECIAL_PREFIX, "lv")
                .add("a(ascension)", getI18Name("param.ascension"), FieldMode.SPECIAL_PREFIX, "a")
                .add("s(skill level)", getI18Name("param.skill_level"), FieldMode.SPECIAL_PREFIX, "s")
                .add("t(talent level)", getI18Name("param.talent_level"), FieldMode.SPECIAL_PREFIX, "t")
                .add("f(affinity level)", getI18Name("param.affinity_level"), FieldMode.SPECIAL_PREFIX, "f");

        return createCommand(
                "command.character_all.name",
                "command.character_all.description",
                "command.character_all.full_description",
                syntax
        );
    }

    /**
     * character命令 - 获取单个角色
     */
    private Command createCharacterSingleCommand() {
        Syntax syntax = new Syntax()
                .add("character")
                .add("characters", getI18Name("param.character_id"), FieldMode.COMPLEX_RADIO)
                .add("lv(level)", getI18Name("param.level"), FieldMode.SPECIAL_PREFIX, "lv")
                .add("a(ascension)", getI18Name("param.ascension"), FieldMode.SPECIAL_PREFIX, "a")
                .add("s(skill level)", getI18Name("param.skill_level"), FieldMode.SPECIAL_PREFIX, "s")
                .add("t(talent level)", getI18Name("param.talent_level"), FieldMode.SPECIAL_PREFIX, "t")
                .add("f(affinity level)", getI18Name("param.affinity_level"), FieldMode.SPECIAL_PREFIX, "f");

        return createCommand(
                "command.character_single.name",
                "command.character_single.description",
                "command.character_single.full_description",
                syntax
        );
    }
}