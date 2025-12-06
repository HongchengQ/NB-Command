package emu.nebula.nbcommand.service.command.manager;

import emu.nebula.nbcommand.model.Command;
import emu.nebula.nbcommand.model.command.Syntax;
import emu.nebula.nbcommand.model.command.Syntax.FieldMode;
import emu.nebula.nbcommand.service.command.BaseCommandManager;

import java.util.*;
import java.util.function.Supplier;

public class PlayerManager extends BaseCommandManager {
    @Override
    public List<Supplier<Command>> getCategoryCommands() {
        return List.of(
                this::createAccountCommand,
                this::createSetLevelCommand
        );
    }

    /**
     * account命令 - 账户相关操作
     */
    private Command createAccountCommand() {
        Syntax syntax = new Syntax()
                .add("account")
                .add("{create | delete}", getI18Name("param.action"), FieldMode.SIMPLE_RADIO)
                .add("[email]", getI18Name("param.email"))
                .add("(uid)", getI18Name("param.uid"), FieldMode.SPECIAL_PREFIX, "");

        return createCommand(
                "command.account.name",
                "command.account.description",
                "command.account.full_description",
                syntax
        );
    }

    /**
     * setlevel命令 - 设置等级
     */
    private Command createSetLevelCommand() {
        Syntax syntax = new Syntax()
                .add("setlevel")
                .add("[level]", getI18Name("param.level"));

        return createCommand(
                "command.setlevel.name",
                "command.setlevel.description",
                "command.setlevel.full_description",
                syntax
        );
    }
}