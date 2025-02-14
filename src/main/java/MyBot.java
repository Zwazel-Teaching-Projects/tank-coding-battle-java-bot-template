import dev.zwazel.GameWorld;
import dev.zwazel.PropertyHandler;
import dev.zwazel.internal.PublicGameWorld;
import dev.zwazel.internal.connection.client.ConnectedClientConfig;
import dev.zwazel.internal.game.tank.LightTank;
import dev.zwazel.internal.message.MessageContainer;
import dev.zwazel.internal.message.data.GameConfig;
import dev.zwazel.internal.message.data.SimpleTextMessage;

import java.util.Arrays;
import java.util.List;

import static dev.zwazel.internal.message.MessageTarget.Type.CLIENT;

public class MyBot implements LightTank {
    GameConfig config;
    PropertyHandler propertyHandler;

    public void start() {
        GameWorld.startGame(this);
        propertyHandler = PropertyHandler.getInstance();
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
                .forEach(target -> world.send(new MessageContainer(
                        CLIENT.get(target),
                        new SimpleTextMessage("Hello from " +
                                propertyHandler.getProperty("bot.name") + "!"
                        )
                )));
    }
}
