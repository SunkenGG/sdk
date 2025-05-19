package gg.sunken.sdk.command;


import com.github.benmanes.caffeine.cache.Caffeine;
import gg.sunken.sdk.guice.GuiceServiceLoader;
import gg.sunken.sdk.utils.ReflectionUtil;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.processors.cache.CaffeineCache;
import org.incendo.cloud.processors.confirmation.ConfirmationConfiguration;
import org.incendo.cloud.processors.confirmation.ConfirmationManager;
import org.incendo.cloud.processors.confirmation.ImmutableConfirmationConfiguration;
import org.incendo.cloud.processors.cooldown.CooldownConfiguration;
import org.incendo.cloud.processors.cooldown.CooldownManager;
import org.incendo.cloud.processors.cooldown.CooldownRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The global command handler, this interfaces with Cloud to handle annotation based
 * commands. Each plugin should call {@link #registerCommands(ClassLoader)} to register
 * their commands.
 *
 * @author santio
 */
@SuppressWarnings({"WeakerAccess", "UnstableApiUsage"})
public class CommandRegistry {

    private final CommandManager<Source> commandManager;
    private final AnnotationParser<Source> annotationParser;
    private final ConfirmationManager<Source> confirmationManager;
    private final CooldownManager<Source> cooldownManager;

    public CommandRegistry(JavaPlugin plugin) {
        commandManager = PaperCommandManager.builder(PaperSimpleSenderMapper.simpleSenderMapper())
                .executionCoordinator(ExecutionCoordinator.asyncCoordinator())
                .buildOnEnable(plugin);

        annotationParser = new AnnotationParser<>(commandManager, Source.class);

        ImmutableConfirmationConfiguration<Source> configuration = ConfirmationConfiguration.<Source>builder()
                .cache(CaffeineCache.of(Caffeine.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).build()))
                .noPendingCommandNotifier(src -> {
                    //TODO
                })
                .confirmationRequiredNotifier((src, context) -> {
                    //TODO
                })
                .build();

        confirmationManager = ConfirmationManager.confirmationManager(configuration);

        CooldownRepository<Source> cooldownRepository = CooldownRepository.forMap(new HashMap<>());
        CooldownConfiguration<Source> cooldownConfiguration = CooldownConfiguration.<Source>builder()
                .repository(cooldownRepository)
                .bypassCooldown(srcCtx -> srcCtx.sender().source().hasPermission(srcCtx.command().commandPermission().permissionString() + ".bypasscooldown"))
                //TODO add other builder stuff
                .build();

        this.cooldownManager = CooldownManager.cooldownManager(
                cooldownConfiguration
        );
    }

    /**
     * Register commands found in the given class loader
     *
     * @param classLoader the class loader to search for commands
     */
    public void registerCommands(ClassLoader classLoader) {
        final GuiceServiceLoader<BaseCommand> loader = GuiceServiceLoader.load(
                BaseCommand.class,
                classLoader
        );

        for (Class<? extends BaseCommand> command : loader) {
            Collection<@NonNull Command<Source>> parsed = annotationParser.parse(command);
            for (Command<Source> cmd : parsed) {
                handleConfirmation(cmd);
            }
        }

        final GuiceServiceLoader<StaffCommand> staffLoader = GuiceServiceLoader.load(
                StaffCommand.class,
                classLoader
        );

        for (Class<? extends StaffCommand> command : staffLoader) {
            Collection<@NonNull Command<Source>> parsed = annotationParser.parse(command);
            for (Command<Source> cmd : parsed) {
                injectRoot(cmd, "s");
                handleConfirmation(cmd);
            }
        }

        final GuiceServiceLoader<TestCommand> testLoader = GuiceServiceLoader.load(
                TestCommand.class,
                classLoader
        );

        for (Class<? extends TestCommand> command : testLoader) {
            Collection<@NonNull Command<Source>> parsed = annotationParser.parse(command);
            for (Command<Source> cmd : parsed) {
                injectRoot(cmd, "t");
                handleConfirmation(cmd);
            }
        }
    }

    /**
     * Injects the root command into the command manager
     * @param command the command to inject
     * @param root the root command
     */
    private void injectRoot(Command<Source> command, String root) {
        List<@NonNull CommandComponent<Source>> components = (List<@NonNull CommandComponent<Source>>) ReflectionUtil.getPrivateField(command, "components");

        CommandComponent<Source> component = components.get(0);
        CommandComponent<Source> clone;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(component);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            clone = (CommandComponent<Source>) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to deep copy command component", e);
        }

        components.addFirst(clone);
        ReflectionUtil.setPrivateField(command, "components", components);
    }

    private void handleConfirmation(Command<Source> cmd) {
        if (!cmd.commandMeta().contains(ConfirmationManager.META_CONFIRMATION_REQUIRED)) {
            return;
        }

        boolean confirm = cmd.commandMeta().get(ConfirmationManager.META_CONFIRMATION_REQUIRED);

        if (!confirm) {
            return;
        }

        Command.Builder<Source> builder = null;

        for (CommandComponent<Source> argument : cmd.nonFlagArguments()) {
            if (argument.type() != CommandComponent.ComponentType.LITERAL) {
                break;
            }

            if (builder == null) {
                builder = commandManager.commandBuilder(argument.name())
                        .permission(cmd.commandPermission())
                        .handler(confirmationManager.createExecutionHandler());
                continue;
            }

            builder = builder.literal(argument.name());
        }

        builder = builder.literal("confirm");

        commandManager.command(builder.build());
    }

    public void unregisterCommands() {

    }
}