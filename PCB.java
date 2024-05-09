import java.util.Arrays;
import java.util.LinkedList;

public class PCB {
    public static enum Priority{real_time, interactive, background};
    static int nextPid;
    int pid = 0; //processId
    int time = 0;
    int timeout = 0;
    UserlandProcess ULP = null;
    private Priority currentPriority = null;
    int[] processIds; 
    int signal;
    String name;
    LinkedList<KernelMessage> messageQueue;
    VirtualToPhysicalMapping[] MemoryMap;

    Scheduler scheduler;

    //stores the process, sets current pid
    public PCB(UserlandProcess up, Priority currPriority){
        ULP = up;
        this.currentPriority = currPriority;
        pid = up.getPid();
        processIds = new int[]{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};//initialize an array of "empty" ids
        name = up.getClass().getSimpleName();
        messageQueue = new LinkedList<>();
        MemoryMap = new VirtualToPhysicalMapping[100];
    }
    public boolean isDone(){
        return ULP.isDone();
    }
    //calls Userland Process's Stop method and sleeps until the process is stopped
    public void stop() throws InterruptedException{
        ULP.stop();
        //sleeps process until ULP is done
        while(!ULP.isStopped()){
            Thread.sleep(10);
        }
    }
    public void requestStop(){
        ULP.requestStop();
    }
    public void run(){
        ULP.start();
    }
    public void setPriority(Priority currPriority){
        this.currentPriority = currPriority;
    }
    public Priority getPriority(){
        return currentPriority;
    }
    //puts the id into an empty spot in processIds array
    public void addProcessId(int id){
        for(int i = 0; i < processIds.length; i++){
            if(processIds[i] == -1){
                processIds[i] = id;
            }
        }
    }
    public int getPid(){
        return pid;
    }
    public String getName(){
        return name;
    }
    //returns queue of processes waiting for a message
    public LinkedList<KernelMessage> getWaitQueue(){
        return messageQueue;
    }

    //update random TLB entry to new virtual page and physical page in PCB's MemoryMap
    void GetMapping(int virtualPageNumber, boolean[] inUseArray) throws InterruptedException{
        int random = (int)Math.random()*2;
        //if trying to access memory you shouldn't be able to
        if(MemoryMap[virtualPageNumber] == null){
            try {
                System.out.println("ACCESSING UNAVAILABLE MEMORY: " + virtualPageNumber);
                System.out.println(Arrays.toString(MemoryMap));
                System.out.println(Arrays.toString(inUseArray));
                OS.Exit();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }  
        }

        //If the physical page is -1, find an available physical page in the “in use” array and assign it. 
        //If there isn’t one available, that’s when we have to do a page swap to free one up. 
        if(MemoryMap[virtualPageNumber].getPhysicalPageNumber() == -1){
            int count = 0;
            for(boolean page:inUseArray){
                //if page is available
                if(page == false){
                    //sets the Physical Page Address
                    MemoryMap[virtualPageNumber].setPhysicalPageNumber(count);
                    //update random TLB entry to new virtual page and physical page in PCB's MemoryMap
                    ULP.getTLB()[random][1] = MemoryMap[virtualPageNumber].getPhysicalPageNumber();
                    ULP.getTLB()[random][0] = virtualPageNumber;
                    //marks the InUseArray
                    inUseArray[count] = true;
                    return;
                }
                count++;
            }

            //if no page is available
            //do page swap to free
            //loop until randomProcess with space is found
            while(true){
                int page = 0;
                PCB pageSwap = scheduler.getRandomProcess(); //PCB with victim page
                //find place the victim page
                for(VirtualToPhysicalMapping v:pageSwap.MemoryMap){
                    if(v != null){
                        //read and write victim page to swapfile
                        byte value = (byte)pageSwap.ULP.Read(page * 1024);
                        //write victim page's data to swapfile at position OS.trackPage
                        OS.swapFile.Write(OS.swapFileId,new byte[]{value});

                        //sets this page to victim's old page value
                        MemoryMap[virtualPageNumber].setPhysicalPageNumber(v.getPhysicalPageNumber());

                        //"soft" remove
                        //set page as "borrowed"
                        v.setPhysicalPageNumber(-1);
                        v.setDiskPageNumber(OS.trackPage); //this is the "offset" in the disk (NOT old page)

                        //update random TLB
                        ULP.getTLB()[random][1] = MemoryMap[virtualPageNumber].getPhysicalPageNumber();
                        ULP.getTLB()[random][0] = virtualPageNumber;
                        return;
                    }
                    page++;
                }
            }
            
        }
        //If we got a new physical page 
        //TLB didn’t have the data we needed
        else{
            //if data was previously written to disk (the on disk page number is not -1)
            //then we have to load the old data in and populate the physical page
            if(MemoryMap[virtualPageNumber].getDiskPageNumber() != -1){
                //set and read swapFile position - diskPageNumber (the offset)
                OS.swapFile.Seek(OS.swapFileId, MemoryMap[virtualPageNumber].getDiskPageNumber() * 1024);
                byte[] value = OS.swapFile.Read(OS.swapFileId, 1024);
                //reset offset
                MemoryMap[virtualPageNumber].setDiskPageNumber(-1);
                //rewrite data to MemoryMap
                ULP.Write(MemoryMap[virtualPageNumber].getPhysicalPageNumber(), Byte.parseByte(new String(value)));
            }
            //If no data was ever written to disk, we have to populate the memory with 0’s
            else{
                byte[] value = new byte[1024];
                OS.swapFile.Write(OS.swapFileId, value);
            }

            OS.trackPage++;

            //update random TLB entry to new virtual page and physical page in PCB's MemoryMap
            ULP.getTLB()[random][1] = MemoryMap[virtualPageNumber].getPhysicalPageNumber();
            ULP.getTLB()[random][0] = virtualPageNumber;
        }        
    }

    //mutator to use schedueler's getRandomProcess()
    void setSched(Scheduler s){
        scheduler = s;
    }
}