import dev.zwazel.GameWorld;
import dev.zwazel.PropertyHandler;
import dev.zwazel.bot.BotInterface;
import dev.zwazel.internal.PublicGameWorld;
import dev.zwazel.internal.config.LobbyConfig;
import dev.zwazel.internal.config.LocalBotConfig;
import dev.zwazel.internal.debug.MapVisualiser;
import dev.zwazel.internal.game.lobby.TeamConfig;
import dev.zwazel.internal.game.state.ClientState;
import dev.zwazel.internal.game.tank.TankConfig;
import dev.zwazel.internal.game.tank.implemented.SelfPropelledArtillery;
import dev.zwazel.internal.game.transform.Quaternion;
import dev.zwazel.internal.game.transform.Vec3;
import dev.zwazel.internal.game.utils.Graph;
import dev.zwazel.internal.game.utils.Node;
import dev.zwazel.internal.message.data.GameConfig;

import java.util.LinkedList;
import java.util.Optional;

public class ArtilleryMaster implements BotInterface {
    private final PropertyHandler propertyHandler = PropertyHandler.getInstance();
    private MapVisualiser visualiser;

    public static void main(String[] args) {
        ArtilleryMaster bot = new ArtilleryMaster();

        // GameWorld.startGame(bot); // This starts the game with a LightTank, and immediately starts the game when connected
        GameWorld.connectToServer(bot); // This connects to the server with a LightTank, but does not immediately start the game
    }

    @Override
    public LocalBotConfig getLocalBotConfig() {
        return LocalBotConfig.builder()
                .debugMode(Optional.ofNullable(propertyHandler.getProperty("debug.mode"))
                        .map(GameWorld.DebugMode::valueOf))
                .botName(propertyHandler.getProperty("bot.name"))
                .tankType(SelfPropelledArtillery.class)
                .serverIp(propertyHandler.getProperty("server.ip"))
                .serverPort(Integer.parseInt(propertyHandler.getProperty("server.port")))
                .lobbyConfig(LobbyConfig.builder()
                        .lobbyName(propertyHandler.getProperty("lobby.name"))
                        .teamName(propertyHandler.getProperty("lobby.name"))
                        .teamName(propertyHandler.getProperty("lobby.team.name"))
                        .mapName(propertyHandler.getProperty("lobby.map.name"))
                        .spawnPoint(Optional.ofNullable(propertyHandler.getProperty("lobby.spawnPoint"))
                                .map(Integer::parseInt))
                        .fillEmptySlots(Boolean.parseBoolean(propertyHandler.getProperty("lobby.fillEmptySlots")))
                        .build()
                )
                .build();
    }

    @Override
    public void setup(PublicGameWorld world) {
        GameConfig config = world.getGameConfig();

        TeamConfig myTeamConfig = config.getMyTeamConfig();
        TeamConfig enemyTeamConfig = config.teamConfigs().values().stream()
                .filter(teamConfig -> !teamConfig.teamName().equals(myTeamConfig.teamName()))
                .findFirst()
                .orElseThrow();

        // Get all team members, excluding myself
        // Get all enemy team members

        // If in debug, add visualiser
        if (world.isDebug()) {
            // Add visualiser. By pressing space, you can switch between drawing modes.
            visualiser = new MapVisualiser(world);
            visualiser.setDrawingMode(MapVisualiser.DrawingMode.valueOf(propertyHandler.getProperty("debug.visualiser.mode").toUpperCase()));
            world.registerVisualiser(visualiser);
            visualiser.setMaxWindowHeight(1000);
            visualiser.setMaxWindowWidth(1200);
            visualiser.showMap();
        }
    }

    @Override
    public void processTick(PublicGameWorld world) {
        Graph graph = new Graph(world.getGameConfig().mapDefinition(), false);
        LinkedList<Node> path = new LinkedList<>(); // TODO: Implement pathfinding (optimally you would only calculate this every now and then, not every tick)

        if (visualiser != null) {
            // sets the path to be visualised
            visualiser.setPath(path);
            visualiser.setGraph(graph);
        }

        ClientState myClientState = world.getMyState();

        // If dead, do nothing. Early return.
        if (myClientState.state() == ClientState.PlayerState.DEAD) {
            System.out.println("I'm dead!");
            return;
        }

        if (world.isDebug()) {
            Vec3 myGridPosition = world.getGameConfig().mapDefinition().getClosestTileFromWorld(
                    myClientState.transformBody().getTranslation()
            );

            // System.out.println("My closest position on the grid: " + myGridPosition);
        }

        SelfPropelledArtillery tank = (SelfPropelledArtillery) world.getTank();
        TankConfig myTankConfig = tank.getConfig(world);

        // Get current pitch (up/down rotation) of the turret
        ClientState myState = world.getMyState();
        Quaternion myRot = myState.transformTurret().getRotation();
        double currentPitch = myRot.getPitch();

        double maxPitch = myTankConfig.turretMaxPitch();

        // Print current pitch and maximum pitch
        System.out.println("Current pitch: " + currentPitch + ", Maximum pitch: " + maxPitch + ", Difference: " + (maxPitch - currentPitch));

        double epsilon = 1e-6; // Acceptable error margin
        if (Math.abs(maxPitch - currentPitch) < epsilon) {
            // Shoot if the turret is at the maximum pitch
            tank.shoot(world);
        } else {
            // Rotate turret down until it reaches the minimum pitch, calculate the angle to rotate
            double angleToRotate = Math.min(currentPitch - myTankConfig.turretMinPitch(), myTankConfig.turretPitchRotationSpeed());
            tank.rotateTurretPitch(world, angleToRotate);
        }
    }
}
