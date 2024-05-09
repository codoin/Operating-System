public class PingTest extends UserlandProcess{
    PingTest(){
        super();
    }
    public void main() throws InterruptedException{
        //tells current thread and pid
        System.out.println("I am PING: " + Thread.currentThread().getId() + ", pong = " + OS.GetPid());
        int what = 0;
        int targetPid = OS.GetPidByName("PongTest");
        int senderPid = OS.GetPid();
        while(true){
            //sends a message to PongTest
            byte[] temp = {(byte) what++};
            OS.SendMessage(new KernelMessage(senderPid, targetPid, 0, temp));
            Thread.sleep(100);

            //recieves and prints message
            KernelMessage response = OS.WaitForMessage();
            System.out.println("PONG: " + response.ToString());
            cooperate();
            Thread.sleep(100);            
        }
    }
}