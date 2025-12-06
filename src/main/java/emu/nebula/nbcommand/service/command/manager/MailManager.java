package emu.nebula.nbcommand.service.command.manager;

import emu.nebula.nbcommand.model.Command;
import emu.nebula.nbcommand.model.command.Syntax;
import emu.nebula.nbcommand.model.command.Syntax.FieldMode;
import emu.nebula.nbcommand.service.command.BaseCommandManager;

import java.util.List;
import java.util.function.Supplier;

public class MailManager extends BaseCommandManager {
    /**
     * 获取类别所有指令
     *
     * @return 指令列表(函数接口)
     */
    @Override
    public List<Supplier<Command>> getCategoryCommands() {
        return List.of(
                this::createMailCommand
        );
    }

    /**
     * mail命令 - 邮件系统
     */
    private Command createMailCommand() {
        Syntax syntax = new Syntax()
                .add("mail");

        return createCommand(
                "command.mail.name",
                "command.mail.description",
                "command.mail.full_description",
                syntax
        );
    }
}