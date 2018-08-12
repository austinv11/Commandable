package commandable.api.impl;

import commandable.api.*;
import commandable.util.HelpPage;
import commandable.util.MiscUtil;
import discord4j.command.util.CommandException;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CoreCommandPlugin implements CommandPlugin {

    final Collection<CommandGroup> group = Collections.singleton(new CoreCommandGroup());
    final Set<CommandPlugin> plugins;
    final HelpPageRenderer renderer;

    public CoreCommandPlugin(Set<CommandPlugin> plugins, HelpPageRenderer renderer) {
        this.plugins = plugins;
        plugins.add(this);
        this.renderer = renderer;
    }

    @Override
    public String name() {
        return "Commandable Core Plugin";
    }

    @Override
    public String version() {
        return "1.0";
    }

    @Override
    public Collection<CommandGroup> getCommandGroups() {
        return group;
    }

    public class CoreCommandGroup extends CommandGroup {

        public CoreCommandGroup() {
            super();
            this.addCommand(new HelpCommand());
        }

        @Override
        public String name() {
            return "Commandable Core Group";
        }
    }

    public class HelpCommand implements Command {

        final HelpPage[] help;

        public HelpCommand() {
            this.help = new HelpPage[1];
            help[0] = new HelpPage("Help", "Lists the help descriptions of all commands", List.of(Collections.emptyList(), List.of(new HelpPage.Argument("String", false, "command"))));
        }

        @Override
        public String name() {
            return "help";
        }

        @Override
        public HelpPage[] help() {
            return help;
        }

        @Override
        public Mono<Void> checkPermissions(CommandContext context) {
            return Mono.empty();
        }

        @Override
        public discord4j.command.Command forDiscord4J(Activation activation, Map<Class<?>, TokenConverter<?>> tokenConverters, CommandContext context) {
            return (e, c) -> {
                final AtomicInteger currPage = new AtomicInteger(0);
                if (activation.getDetectedCommandInput().isPresent()) {
                    return Flux.fromIterable(plugins)
                            .flatMap(CommandPlugin::helpPages)
                            .filter(page -> page.getCommandName().equalsIgnoreCase(activation.getDetectedCommandInput().get()))
                            .next()
                            .flatMap(page -> context.getChannel().flatMap(ch -> ch.createMessage(renderer.handleNew(page))))
                            .switchIfEmpty(Mono.error(() -> new CommandException("Command not found!")))
                            .then();
                } else {
                    return Flux.fromIterable(plugins)
                            .flatMap(CommandPlugin::helpPages)
                            .distinct()
                            .collectList()
                            .map(l -> Tuples.of(l, renderer.handleNew(l)))
                            .flatMap(pages ->
                                    context.getChannel()
                                            .flatMap(ch -> ch.createMessage(pages.getT2().get(0)))
                                            .flatMap(m -> m.addReaction(ReactionEmoji.unicode("⬅")).then(Mono.just(m)))
                                            .flatMap(m -> m.addReaction(ReactionEmoji.unicode("➡")))
                                            .then(Mono.just(renderer.handleEdit(pages.getT1()))))
                            .flatMap(pages -> {
                                return MiscUtil.listenToReactions(context.getMessage()).doOnNext(re -> {
                                    if (re.getEmoji().asUnicodeEmoji().isPresent()) {
                                        ReactionEmoji.Unicode u = re.getEmoji().asUnicodeEmoji().get();
                                        if (u.getRaw().equals("⬅")) {
                                            currPage.getAndUpdate(i -> {
                                                int newI;
                                                if (i == 0) {
                                                    newI = pages.size()-1;
                                                } else {
                                                    newI = i - 1;
                                                }
                                                re.getMessage().flatMap(m -> m.edit(pages.get(newI))).subscribe();
                                                return newI;
                                            });
                                        } else if (u.getRaw().equals("➡")) {
                                            currPage.getAndUpdate(i -> {
                                                int newI;
                                                if (i == pages.size()-1) {
                                                    newI = 0;
                                                } else {
                                                    newI = i + 1;
                                                }
                                                re.getMessage().flatMap(m -> m.edit(pages.get(newI))).subscribe();
                                                return newI;
                                            });
                                        }
                                    }
                                }).then();
                            });
                }
            };
        }
    }
}
