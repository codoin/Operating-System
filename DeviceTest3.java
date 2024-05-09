public class DeviceTest3 extends UserlandProcess{
    DeviceTest3(){
        super();
    }
    public void main() throws InterruptedException {
        int id = OS.Open("random 12002");
        OS.Close(id);
        while(true){
            cooperate();
            Thread.sleep(200);
        }
    }
}
