import java.util.Arrays;
import java.util.concurrent.Semaphore;

public class Kernel implements Runnable, Device{
    private Scheduler scheduler;
    private Thread thread;
    private Semaphore sem;
    VFS vfs;

    static boolean[] inUseArray; //checks if page in Physical Memory is available

    //initializes the thread, semaphore, scheduler, starts the thread, vfs
    //passes instance of kernel to scheduler
    public Kernel(){
        this.thread = new Thread(this);
        this.sem = new Semaphore(0);
        this.scheduler = new Scheduler();
        this.thread.start();
        vfs = new VFS();
        scheduler.setKernel(this);
        inUseArray = new boolean[1024];
    }
    protected void start() throws InterruptedException{
        sem.release();
    }
    //acquires, checks calls, runs next process, and repeats
    public void run(){
        while(true){
            //stops the kernel 
            try {
                sem.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //checks for the correct callType Process
            switch(OS.currentCall){
                case create_process:
                    try {
                        CreateProcess((UserlandProcess) OS.parameters.get(0), (PCB.Priority) OS.parameters.get(1));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case switch_process:
                    try {
                        Thread.sleep(10);
                        SwitchProcess();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case sleep:
                    try {
                        Sleep((int)OS.parameters.get(0));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                //if the device call has a return value, stores into OS deviceReturn variable
                case open:
                    OS.deviceReturn = Open((String)OS.parameters.get(0));
                    break;
                case close:
                    Close((int)OS.parameters.get(0));
                    break;
                case read:
                    OS.deviceReturn = Read((int)OS.parameters.get(0),(int)OS.parameters.get(1));
                    break;
                case seek:
                    Seek((int)OS.parameters.get(0),(int)OS.parameters.get(1));
                    break;
                case write:
                    OS.deviceReturn = Write((int)OS.parameters.get(0),(byte[])OS.parameters.get(1));
                    break;
                case send_message:
                    SendMessage((KernelMessage)OS.parameters.get(0));
                    break;
                case wait_message:
                    OS.messageReturn = WaitForMessage();
                    break;
                case get_pid:
                    OS.messageReturn = GetPid();
                    break;
                case get_pid_name:
                    OS.messageReturn = GetPidByName((String)OS.parameters.get(0));
                    break;
                case get_mapping:
                    try {
                        GetMapping((int)OS.parameters.get(0));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case allocate_memory:
                    OS.memoryReturn = AllocateMemory((int)OS.parameters.get(0));
                    break;
                case free_memory:
                    OS.memoryReturn = FreeMemory((int)OS.parameters.get(0),(int)OS.parameters.get(1));
                    break;
                case exit:
                    Exit();
                    break;
                default:
                    System.out.println("Current Call Error");
                    break;
            }
            //run the next process
            getScheduler().currentProcess.run();
        }
    }

    //calls the Schedulers Create Process
    //returns the pid
    protected int CreateProcess(UserlandProcess up, PCB.Priority priority) throws InterruptedException{
        getScheduler().CreateProcess(up, priority);
        return up.getPid();
    }

    //calls the Schedulers Switch Process
    protected void SwitchProcess() throws InterruptedException{
        getScheduler().SwitchProcess();
    }

    protected Scheduler getScheduler(){
        return scheduler;
    }

    //call the Schedulers Sleep method
    protected void Sleep(int milliseconds) throws InterruptedException{
        getScheduler().Sleep(milliseconds);
    }

    //passes the string s to the VFS open method and store the id into the PCB's array of ids
    //returns the fd where the device was store if works, else returns -1 on error
    public int Open(String s) {
        int[] temp = scheduler.getCurrentlyRunning().processIds;
        //System.out.println("PCB: " + Arrays.toString(temp));
        int id;
        for(int i = 0; i < temp.length; i++){
            if(temp[i] == -1){
                id = vfs.Open(s);
                if(id == -1){
                    return -1;
                }
                temp[i] = id;
                return id;
            }
        }
        return -1;
    }
    //after converting id to VFS id, closes device 
    //then sets process to empty (-1)
    public void Close(int id) {
        int vfsId = scheduler.getCurrentlyRunning().processIds[id];
        //if the id is empty, do nothing
        if(vfsId == -1){
            return;
        }
        vfs.Close(vfsId);
        scheduler.getCurrentlyRunning().processIds[id] = -1;
    }
    //after converting id to VFS id, uses the device's read method
    //return byte array if works, else returns -1 on fail 
    public byte[] Read(int id, int size) {
        int vfsId = scheduler.getCurrentlyRunning().processIds[id];
        return vfs.Read(vfsId, size);
    }
    //after converting id to VFS id, uses the device's seek method
    public void Seek(int id, int to) {
        int vfsId = scheduler.getCurrentlyRunning().processIds[id];
        vfs.Seek(vfsId, to);
    }
    //after converting id to VFS id, uses the device's write method
    //returns the size of the write
    public int Write(int id, byte[] data) {
        int vfsId = scheduler.getCurrentlyRunning().processIds[id];
        return vfs.Write(vfsId, data);
    }

    //returns the current process’ pid
    int GetPid(){
        return scheduler.GetPid();
    }
    //returns the pid of a process with that name
    int GetPidByName(String s){
        return scheduler.GetPidByName(s);
    }

    //copies original message, set the sender’s pid, adds to message queue, removes process from wait
    void SendMessage(KernelMessage km){
        //copy kernel message
        KernelMessage copy = new KernelMessage(km);
        //sets sending pid
        copy.setSender(GetPid());

        //finds target
        //put message into target's wait queue
        for(PCB p:getScheduler().realList){
            if(copy.getTarget() == p.getPid()){
                p.getWaitQueue().add(copy);
                break;
            }
        }
        for(PCB p:getScheduler().interactiveList){
            if(copy.getTarget() == p.getPid()){
                p.getWaitQueue().add(copy);
                break;
            }
        }
        for(PCB p:getScheduler().backgroundList){
            if(copy.getTarget() == p.getPid()){
                p.getWaitQueue().add(copy);
                break;
            }
        }
        for(PCB p:getScheduler().sleepList){
            if(copy.getTarget() == p.getPid()){
                p.getWaitQueue().add(copy);
                break;
            }
        }
        for(PCB p:getScheduler().waitList){
            if(copy.getTarget() == p.getPid()){
                getScheduler().waitList.remove(p);
                p.getWaitQueue().add(copy);
                getScheduler().addToList(p);
            }
        }
    }
    //check if current process has a message, if so take off queue and return it. if not, add to hold queue
    KernelMessage WaitForMessage(){
        //check if current process has a message, if so take off queue and return it.
        if(!getScheduler().getCurrentlyRunning().getWaitQueue().isEmpty()){
            return getScheduler().getCurrentlyRunning().getWaitQueue().pop();
        }
        //if no message waiting, add current to wait list, set next process, return null
        getScheduler().waitList.add(getScheduler().getCurrentlyRunning());
        getScheduler().popProcess();
        return null;        
    }
    //passes virtualPageNumber to scheduler's method
    void GetMapping(int virtualPageNumber) throws InterruptedException{
        getScheduler().GetMapping(virtualPageNumber, inUseArray);
    }
    //returns the start virtual address
    int AllocateMemory(int size){
        //find number of pages to add
        int page = size/1024;
        int count = 0;
        //finds enough available memory in virtual space
        for(int i = 0; i < inUseArray.length; i++){
            //if available
            if(inUseArray[i] == false){
                //mark physical pages as in use
                count++;
            }
            //if the empty space equals number of pages needed
            if(count == page){
                count = 0;
                //looking for space in PCB memory map
                for(int j = 0; j < 100; j++){
                    //has to be contiguous virtual memory
                    if(getScheduler().getCurrentlyRunning().MemoryMap[j] == null){
                        count++;
                    }
                    else{
                        count = 0;
                    }

                    //if enough space, set "promises" in MemoryMap in PCB
                    if(count == page){
                        //System.out.println("Index:" + j + " Pages:" + count);
                        for(int k = count-1; k >= 0; k--){
                            getScheduler().getCurrentlyRunning().MemoryMap[j-k] = new VirtualToPhysicalMapping();
                        }
                        //System.out.println("Allocating (Memory Map): " + Arrays.toString(getScheduler().getCurrentlyRunning().MemoryMap));
                        //System.out.println(Arrays.toString(freeList));

                        //the starting virtual address
                        System.out.println("ALLOCATE Virtual Address:" + (j - (count - 1)));
                        return j - (count - 1);
                    }
                }
                //if fail to find space in PCB memory map
                return -1;
            }
        }
        //if fail to find enough space in free memory
        System.out.println("ALLOCATE NOT ENOUGH SPACE IN PHYSICAL MEMORY");
        return -1;
    }
    //takes virtual address as pointer and size to free
    boolean FreeMemory(int pointer, int size){
        System.out.println("Memory Map: " + Arrays.toString(getScheduler().getCurrentlyRunning().MemoryMap));
        int physicalAddress;
        for(int i = 0; i < size/1024; i++){
            //if MemoryMap is not empty
            if(getScheduler().getCurrentlyRunning().MemoryMap[pointer + i] != null){
                //gets physical address from virtual address
                physicalAddress = getScheduler().getCurrentlyRunning().MemoryMap[pointer + i].getPhysicalPageNumber();
                            
                //if "owed" but not used get rid of "promise" (VTPM) but keep inUseArray boolean same
                if(physicalAddress == -1){
                    getScheduler().getCurrentlyRunning().MemoryMap[pointer + i] = null;
                }
                //else just remove both "promise" and inUseArray mapping
                else{
                    //removes mapping in PCB
                    getScheduler().getCurrentlyRunning().MemoryMap[pointer + i] = null;
                    //marks physical pages as not in use
                    inUseArray[physicalAddress] = false;
                }
            }
        }
        System.out.println("Freeing (Memory Map): " + Arrays.toString(getScheduler().getCurrentlyRunning().MemoryMap));
        return true;
    }

    //closes a process
    void Exit(){
        getScheduler().Exit();
    }
}