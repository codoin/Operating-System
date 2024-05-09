public class VirtualMemoryTest extends UserlandProcess{
    VirtualMemoryTest(){
        super();
    }
    public void main() throws InterruptedException {
        //process that used 100 * 1024 bytes 
        System.out.println("VirtualMemoryTest");
        OS.AllocateMemory(1024*100);
        while(true){
            cooperate();
            Thread.sleep(500);
        }
    }
}