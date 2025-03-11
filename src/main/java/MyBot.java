import dev.zwazel.GameWorld;
import dev.zwazel.PropertyHandler;
import dev.zwazel.bot.BotInterface;
import dev.zwazel.internal.PublicGameWorld;
import dev.zwazel.internal.game.state.ClientState;
import dev.zwazel.internal.game.tank.TankConfig;
import dev.zwazel.internal.game.tank.implemented.SelfPropelledArtillery;
import dev.zwazel.internal.game.transform.Quaternion;

public class MyBot implements BotInterface {
    private final PropertyHandler propertyHandler = PropertyHandler.getInstance();

    public void start() {
        GameWorld.connectToServer(this, SelfPropelledArtillery.class); // This connects to the server with a LightTank, but does not immediately start the game
    }

    @Override
    public void setup(PublicGameWorld world) {
    }

    @Override
    public void processTick(PublicGameWorld world) {
        ClientState myState = world.getMyState();
        SelfPropelledArtillery tank = (SelfPropelledArtillery) world.getTank();
        TankConfig myTankConfig = tank.getConfig(world);

        // Get current pitch (up/down rotation) of the turret
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
