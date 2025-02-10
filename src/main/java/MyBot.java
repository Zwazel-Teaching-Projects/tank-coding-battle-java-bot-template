import dev.zwazel.GameWorld;
import dev.zwazel.bot.BotInterface;
import dev.zwazel.internal.PublicGameWorld;
import dev.zwazel.internal.message.MessageContainer;
import dev.zwazel.internal.message.data.SimpleTextMessage;

import static dev.zwazel.internal.message.MessageTarget.Type.SERVER_ONLY;

public class MyBot implements BotInterface {
    public void start() {
        GameWorld.startGame(this);
    }

    @Override
    public void processTick(PublicGameWorld publicGameWorld) {
        System.out.println("Hello, world! " + publicGameWorld.getGameState().tick());

        publicGameWorld.send(new MessageContainer(SERVER_ONLY.get(), new SimpleTextMessage("Hello from MyBot!")));
    }
}
