import dev.zwazel.GameWorld;
import dev.zwazel.bot.BotInterface;
import dev.zwazel.internal.PublicGameWorld;

public class MyBot implements BotInterface {
    public static void main(String[] args) {
        GameWorld.startGame(new MyBot());
    }

    @Override
    public void processTick(PublicGameWorld publicGameWorld) {
        System.out.println("Hello, world!");
    }
}
