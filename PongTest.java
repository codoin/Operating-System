public class PongTest extends UserlandProcess{
    PongTest(){
        super();
    }
    public void main() throws InterruptedException{
        //tells curent pid
        System.out.println("I am PONG: " + Thread.currentThread().getId() + ", ping = " + OS.GetPid());
        int targetPid = OS.GetPidByName("PingTest");
        int senderPid = OS.GetPid();
        while(true){
            //receive and print message
            KernelMessage response = OS.WaitForMessage();
            System.out.println("PING: " + response.ToString());
            Thread.sleep(100);

            //sends message to Pingtest with same data
            OS.SendMessage(new KernelMessage(senderPid, targetPid, 0, response.getData()));
            cooperate();
            Thread.sleep(100);
        }
    }
}