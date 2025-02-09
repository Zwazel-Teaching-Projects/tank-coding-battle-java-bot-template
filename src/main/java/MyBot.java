import dev.zwazel.GameWorld;
import dev.zwazel.bot.BotInterface;
import dev.zwazel.internal.PublicGameWorld;
import dev.zwazel.internal.messages.MessageContainer;
import dev.zwazel.internal.messages.data.SimpleTextMessage;

import static dev.zwazel.internal.messages.MessageTarget.Type.TEAM;

public class MyBot implements BotInterface {
    public void start() {
        GameWorld.startGame(this);
    }

    @Override
    public void processTick(PublicGameWorld publicGameWorld) {
        System.out.println("Hello, world! " + publicGameWorld.getGameState().tick());

        publicGameWorld.send(new MessageContainer(TEAM.get(), new SimpleTextMessage("Hello from MyBot!")));
    }
}
