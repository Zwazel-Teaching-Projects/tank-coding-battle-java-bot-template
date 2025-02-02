import dev.zwazel.GameWorld;
import dev.zwazel.bot.BotInterface;
import dev.zwazel.internal.PublicGameWorld;

public class MyBot implements BotInterface {
    public static void main(String[] args) {
        // This is the entry point of your bot
        // You can use this method to initialize your bot
        // and start the game loop
        GameWorld.startGame(new MyBot());
    }

    @Override
    public void processTick(PublicGameWorld publicGameWorld) {

    }
}
