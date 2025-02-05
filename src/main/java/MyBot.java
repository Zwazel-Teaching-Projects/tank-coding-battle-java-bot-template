import dev.zwazel.GameWorld;
import dev.zwazel.bot.BotInterface;
import dev.zwazel.internal.PublicGameWorld;

public class MyBot implements BotInterface {
    public void start() {
        GameWorld.startGame(this);
    }

    @Override
    public void processTick(PublicGameWorld publicGameWorld) {
        System.out.println("Hello, world!");
    }
}
