package gg.sunken.sdk.command;

import gg.sunken.sdk.utils.CommandUtil;
import gg.sunken.sdk.utils.ServerUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class SdkBukkitCommand extends BukkitCommand {

    private final Plugin plugin;
    private final List<SdkBukkitCommand> subCommands = new ArrayList<>();
    @Setter
    private SdkBukkitCommand parent;

    public SdkBukkitCommand(@NotNull String... aliases) {
        super(aliases[0]);

        this.plugin = ServerUtils.getCallingPlugin(); // TODO: this might need to be 1 higher in call stack to get the correct plugin
        assert plugin != null;

        this.parent = null;

        this.description = "";
        this.usageMessage = "/" + aliases[0];

        this.setAliases(new ArrayList<>());
        for (String alias : aliases) {
            this.getAliases().add(alias.toLowerCase());
        }

        StringBuilder parentPath = new StringBuilder();
        while (parent != null) {
            parentPath.insert(0, parent.getName() + ".");
            parent = parent.getParent();
        }

        this.setPermission("sdk." + plugin.getName().toLowerCase() + ".cmd." + parentPath + aliases[0]);
    }

    public void addSubCommand(@NotNull SdkBukkitCommand command) {
        subCommands.add(command);
        command.setParent(this);
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String label, @NotNull String[] args) {
        if (!testPermission(commandSender)) {
            return true;
        }

        if (args.length == 0) {
            executeCommand(commandSender, label, args);
            return true;
        }

        if (subCommands.isEmpty()) {
            executeCommand(commandSender, label, args);
            return true;
        }

        for (SdkBukkitCommand subCommand : subCommands) {
            if (subCommand.getAliases().contains(args[0].toLowerCase())) {
                String[] newArgs = new String[args.length - 1];
                System.arraycopy(args, 1, newArgs, 0, newArgs.length);

                if (!subCommand.testPermission(commandSender)) {
                    return true;
                }

                subCommand.execute(commandSender, label, newArgs);
                return true;
            }
        }

        executeCommand(commandSender, label, args);
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (!testPermission(sender)) {
            return List.of();
        }

        if (args.length == 0) {
            return this.executeTabComplete(sender, alias, args);
        }

        if (subCommands.isEmpty()) {
            return this.executeTabComplete(sender, alias, args);
        }

        for (SdkBukkitCommand subCommand : subCommands) {
            if (subCommand.getAliases().contains(args[0].toLowerCase())) {
                if (!subCommand.testPermissionSilent(sender)) {
                    return List.of();
                }

                String[] newArgs = new String[args.length - 1];
                System.arraycopy(args, 1, newArgs, 0, newArgs.length);

                return subCommand.tabComplete(sender, alias, newArgs);
            }
        }

        return this.executeTabComplete(sender, alias, args);
    }

    public boolean testArgPermission(@NotNull CommandSender sender, String arg) {
        return sender.hasPermission(getPermission() + "." + arg);
    }

    public void register() {
        SdkBukkitCommand command = this;
        while (command.parent != null) {
            command = command.parent;
        }
        CommandUtil.registerCommand(plugin, this);
    }

    public void unregister() {
        SdkBukkitCommand command = this;
        while (command.parent != null) {
            command = command.parent;
        }
        CommandUtil.unregisterCommand(command);
    }

    public abstract void executeCommand(@NotNull CommandSender commandSender, @NotNull String label, @NotNull String[] args);

    public abstract @NotNull List<String> executeTabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException;
}
