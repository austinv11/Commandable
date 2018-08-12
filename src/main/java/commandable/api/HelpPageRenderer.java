package commandable.api;

import commandable.annotations.Service;
import commandable.util.HelpPage;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.MessageEditSpec;

import java.util.List;

@Service
public interface HelpPageRenderer {

    List<MessageCreateSpec> handleNew(List<HelpPage> pages);

    List<MessageEditSpec> handleEdit(List<HelpPage> pages);

    MessageCreateSpec handleNew(HelpPage page);

    MessageEditSpec handleEdit(HelpPage page);
}
