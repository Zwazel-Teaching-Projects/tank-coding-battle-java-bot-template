import dev.zwazel.GameWorld;
import dev.zwazel.PropertyHandler;
import dev.zwazel.bot.BotInterface;
import dev.zwazel.internal.PublicGameWorld;
import dev.zwazel.internal.connection.client.ConnectedClientConfig;
import dev.zwazel.internal.game.lobby.TeamConfig;
import dev.zwazel.internal.game.state.ClientState;
import dev.zwazel.internal.game.tank.Tank;
import dev.zwazel.internal.game.tank.implemented.LightTank;
import dev.zwazel.internal.message.MessageContainer;
import dev.zwazel.internal.message.data.GameConfig;
import dev.zwazel.internal.message.data.SimpleTextMessage;

import java.util.List;
import java.util.Optional;

import static dev.zwazel.internal.message.MessageTarget.Type.CLIENT;

public class MyBot implements BotInterface {
    private final PropertyHandler propertyHandler = PropertyHandler.getInstance();
    private GameConfig config;

    private List<ConnectedClientConfig> teamMembers;
    private List<ConnectedClientConfig> enemyTeamMembers;

    public void start() {
        GameWorld.startGame(this, LightTank.class);
    }

    @Override
    public void setup(PublicGameWorld world, GameConfig config) {
        this.config = config;

        TeamConfig myTeamConfig = config.getMyTeamConfig();
        TeamConfig enemyTeamConfig = config.teamConfigs().values().stream()
                .filter(teamConfig -> !teamConfig.teamName().equals(myTeamConfig.teamName()))
                .findFirst()
                .orElseThrow();

        System.out.println("My team: " + myTeamConfig);
        System.out.println("Enemy team: " + enemyTeamConfig);

        // Get all team members, excluding myself
        teamMembers = config.getTeamMembers(myTeamConfig.teamName(), config.clientId());
        enemyTeamMembers = config.getTeamMembers(enemyTeamConfig.teamName());
    }

    @Override
    public void processTick(PublicGameWorld world, Tank tank) {
        LightTank lightTank = (LightTank) tank;

        // Get the closest enemy tank
        Optional<ClientState> closestEnemy = enemyTeamMembers.stream()
                .map(connectedClientConfig -> world.getClientState(connectedClientConfig.clientId()))
                // Filter out null states and states without a position
                .filter(clientState -> clientState != null && clientState.transform().getPosition() != null)
                .min((o1, o2) -> {
                    double distance1 = lightTank.getTransform(world).getPosition().distance(o1.transform().getPosition());
                    double distance2 = lightTank.getTransform(world).getPosition().distance(o2.transform().getPosition());
                    return Double.compare(distance1, distance2);
                });

        // Rotate towards the closest enemy, or move in a circle if no enemies are found
        closestEnemy.ifPresentOrElse(
                enemy -> {
                    lightTank.rotateBodyTowards(world, enemy.transform().getPosition());
                    lightTank.move(world, Tank.MoveDirection.FORWARD);

                    System.out.println("Found enemy at " + enemy.transform().getPosition());
                }
                ,
                () -> {
                    System.out.println("No enemies found! Moving in a circle.");
                    lightTank.rotateBody(world, Tank.RotationDirection.CLOCKWISE);
                    lightTank.move(world, Tank.MoveDirection.FORWARD);
                }
        );


        ClientState myState = world.getMyState();
        System.out.println("myState = " + myState);

        System.out.println("Hello, world! " + world.getGameState().tick());

        List<MessageContainer> messages = world.getIncomingMessages();
        for (MessageContainer message : messages) {
            System.out.println("Received message:\n\t" + message);
        }

        // Sending a nice message to all team members
        teamMembers
                .forEach(target -> world.send(new MessageContainer(
                        CLIENT.get(target.clientId()),
                        new SimpleTextMessage(
                                "Hello " + target.clientName() + " from " + config.getMyConfig().clientName() + "!"
                        )
                )));

        // Sending a less nice message to all enemy team members
        enemyTeamMembers
                .forEach(target -> world.send(new MessageContainer(
                        CLIENT.get(target.clientId()),
                        new SimpleTextMessage(
                                "You're going down, " + target.clientName() + "!"
                        )
                )));
    }
}
