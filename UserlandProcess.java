import java.util.concurrent.Semaphore;

public abstract class UserlandProcess implements Runnable{
    private Thread thread = null;
    private Semaphore sem = null;
    private Boolean quantumExpire = null;
    static private int pid = 0;
    static int[][] TLB;
    static byte[] PhysicalMemory;

    public UserlandProcess(){
        this.thread = new Thread(this);
        this.sem = new Semaphore(0);
        this.quantumExpire = false;
        pid++;
        this.thread.start();
        TLB = new int[2][2];
        TLB[0][0] = -1;
        TLB[0][1] = -1;
        TLB[1][0] = -1;
        TLB[1][1] = -1;
        PhysicalMemory = new byte[1048576]; //1024 pages
    }

    //sets boolean that controls process swithching to true
    public void requestStop(){
        quantumExpire = true;
    }
    //test programs must implement a "main"
    public abstract void main() throws InterruptedException;
    public boolean isStopped(){
        return  sem.availablePermits() == 0;
    }
    public boolean isDone(){
        return !thread.isAlive();
    }
    public void start(){
        sem.release();
    }
    public void stop() throws InterruptedException{
        sem.acquire();
    }
    //stops other traffic and runs test programs' main
    public void run(){
        try {
            sem.acquire();
            main();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }        
    }
    //switches process when quantum timer expires
    public void cooperate() throws InterruptedException{
        if(quantumExpire){
            quantumExpire = false;
            OS.SwitchProcess();
        }
    }
    public int getPid(){
        return pid;
    }
    public int[][] getTLB(){
        return TLB;
    }
    public byte[] getMemory(){
        return PhysicalMemory;
    }

    //acesses Memory by virtualAddress
    //use virtual address to get virtual page index in TLB to get physical address of memory
    //if not in TLB calls GetMapping
    public byte Read(int virtualAddress) throws InterruptedException{
        if(virtualAddress%1024 != 0){
            try {
                throw new Exception("WRONG ADDRESS");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        int virtualPage = virtualAddress/1024;
        //if virtual mapping exists in TLB
        //return byte value in physical memory address
        while(true){
            if(TLB[0][0] == virtualPage){
                System.out.println("READ Page Found " + virtualPage + " ");
                int physicalPage = TLB[0][1] / 1024;
                int pageOffset = virtualAddress % 1024;
                return PhysicalMemory[physicalPage * 1024 + pageOffset];
            }
            else if(TLB[1][0] == virtualPage){
                System.out.println("READ Page Found " + virtualPage + " ");
                int physicalPage = TLB[1][1] / 1024;
                int pageOffset = virtualAddress % 1024;
                return PhysicalMemory[physicalPage * 1024 + pageOffset];
            }
            else{
                //else OS call GetMapping to 
                System.out.println("READ GetMapping " + virtualPage + " ");
                OS.GetMapping(virtualPage);
            }
        }
    }
    //acesses Memory by virtualAddress
    //use virtual address to get virtual page index in TLB to get physical address of memory
    //if not in TLB calls GetMapping
    public void Write(int virtualAddress, byte value) throws InterruptedException{
        if(virtualAddress%1024 != 0){
            try {
                throw new Exception("WRONG ADDRESS");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        int virtualPage = virtualAddress/1024;
        //if virtual mapping exists in TLB
        //write byte value in physical memory address
        while(true){
            if(TLB[0][0] == virtualPage){
                //System.out.println("WRITE Page Found " + virtualPage + " ");
                int physicalPage = TLB[0][1] / 1024;
                int pageOffset = virtualAddress % 1024;
                PhysicalMemory[physicalPage * 1024 + pageOffset] = value;
                return;
            }
            else if(TLB[1][0] == virtualPage){
                //System.out.println("WRITE Page Found " + virtualPage + " ");
                int physicalPage = TLB[1][1] / 1024;
                int pageOffset = virtualAddress % 1024;
                PhysicalMemory[physicalPage * 1024 + pageOffset] = value;
                return;
            }
            else{
                //else OS call GetMapping to 
                //System.out.println("WRITE GetMapping " + virtualPage + " ");
                OS.GetMapping(virtualPage);
            }
        }
    }
}