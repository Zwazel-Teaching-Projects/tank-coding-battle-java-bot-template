public class Main {
    public static void main(String[] args) {
        Thread thread1 = new Thread(new MyBot());
        Thread thread2 = new Thread(new MyBot());

        thread1.start();
        thread2.start();
    }
}