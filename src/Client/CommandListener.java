package Client;

public class CommandListener implements Runnable {

    @Override
    public void run() {
        int i = 0;
        System.out.println("Listener online");
        /*
        while(true){
            i++;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Still alive");
        }
        */
    }
}
