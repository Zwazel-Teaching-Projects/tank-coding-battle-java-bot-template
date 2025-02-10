import dev.zwazel.GameWorld;
import dev.zwazel.bot.BotInterface;
import dev.zwazel.internal.PublicGameWorld;
import dev.zwazel.internal.client.ConnectedClientConfig;
import dev.zwazel.internal.message.MessageContainer;
import dev.zwazel.internal.message.data.GameConfig;
import dev.zwazel.internal.message.data.SimpleTextMessage;

import java.util.Arrays;
import java.util.List;

import static dev.zwazel.internal.message.MessageTarget.Type.CLIENT;

public class MyBot implements BotInterface {
    GameConfig config;

    public void start() {
        GameWorld.startGame(this);
    }

    @Override
    public void setup(PublicGameWorld world, GameConfig config) {
        this.config = config;
    }

    @Override
    public void processTick(PublicGameWorld world) {
        System.out.println("Hello, world! " + world.getGameState().tick());

        List<MessageContainer> messages = world.getMessages();
        for (MessageContainer message : messages) {
            System.out.println("Received message:\n\t" + message);
        }

        // Sending out a message to all other clients (not myself)
        Arrays.stream(config.connectedClients())
                .map(ConnectedClientConfig::clientId)
                .filter(l -> l != config.clientId())
                .forEach(target -> world.send(new MessageContainer(CLIENT.get(target), new SimpleTextMessage("Hello from MyBot!"))));
    }
}
