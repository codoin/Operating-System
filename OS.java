import java.util.ArrayList;

public class OS{
    private static Kernel kernel;
    public static enum CallType{create_process, switch_process, sleep, open, close, read, seek, write, get_pid, get_pid_name, send_message, wait_message, get_mapping, allocate_memory, free_memory, exit};
    public static CallType currentCall = null;
    public static ArrayList<Object> parameters = new ArrayList<>();
    private static Object returnValue = new Object();
    public static Object deviceReturn;
    public static Object messageReturn;
    public static Object memoryReturn;

    public static FakeFileSystem swapFile;
    public static int swapFileId;
    public static int trackPage;

    //For priorities, add a new version of CreateProcess to OS, 
    //Kernel and Scheduler that includes both UserlandProcess and a new enum for Priority. 
    //Overload it so that the old calls still work with a default to Interactive priority.
    public static int CreateProcess(UserlandProcess up, PCB.Priority priority) throws InterruptedException{    
        //resets parameters and adds new userland process to list
        parameters.clear();
        parameters.add(up);
        parameters.add(priority);

        //sets new callType to the "note"
        currentCall = CallType.create_process;
        
        SwitchToKernel();
        returnValue = up.getPid();
        return (int)returnValue;
    }
    public static int CreateProcess(UserlandProcess up) throws InterruptedException{
        //System.out.println("OS: Create" + up.getClass());
        
        //resets parameters and adds new userland process to list
        parameters.clear();
        parameters.add(up);
        parameters.add(PCB.Priority.interactive);

        //sets new callType to the "note"
        currentCall = CallType.create_process;

        SwitchToKernel();
        returnValue = up.getPid();
        return (int)returnValue;
    }

    //starts the kernel and creates new process
    public static void Startup(UserlandProcess init) throws InterruptedException{
        kernel = new Kernel();
        CreateProcess(init);
        Thread.sleep(10);
        CreateProcess(new IdleProcess());

        swapFile = new FakeFileSystem();
        //create swapFile (fake file system) and Open
        swapFileId = swapFile.Open("swapFile");
        //write to 0, then 1024, then 2048, then etc.
        trackPage = 0;
    }

    //runs the kernel then if currently running, stops the kernel
    static void SwitchToKernel() throws InterruptedException{
        kernel.start();
        //if nothing is running, kernel will sleep until current process runs
        if(kernel.getScheduler().CurrentlyRunning()){
            //stops the current user process
            kernel.getScheduler().currentProcess.stop();
        }
        else{
            while(true){
                Thread.sleep(10);
                if(!kernel.getScheduler().CurrentlyRunning()){
                    break;
                }
            }
        }
    }

    static void SwitchProcess() throws InterruptedException{
        //set callType to note
        currentCall = CallType.switch_process;

        kernel.start();
        //stops the current user process
        kernel.getScheduler().currentProcess.stop();
    }

    static void Sleep(int milliseconds) throws InterruptedException{
        //reset and add time parameter
        parameters.clear();
        parameters.add(milliseconds);

        //sets the note
        currentCall = CallType.sleep;
        
        kernel.start();
        //stops the current user process
        kernel.getScheduler().currentProcess.stop();
    }

    static int Open(String s) throws InterruptedException {
        //reset and add s parameter
        parameters.clear();
        parameters.add(s);

        //sets the note
        currentCall = CallType.open;
        
        kernel.start();
        //stops the current user process
        kernel.getScheduler().currentProcess.stop();

        return (int)deviceReturn;
    }
    static void Close(int id) throws InterruptedException {
        //reset and add id parameter
        parameters.clear();
        parameters.add(id);

        //sets the note
        currentCall = CallType.close;
        
        kernel.start();
        //stops the current user process
        kernel.getScheduler().currentProcess.stop();
    }
    static byte[] Read(int id, int size) throws InterruptedException {
        //reset and add id and size parameter
        parameters.clear();
        parameters.add(id);
        parameters.add(size);

        //sets the note
        currentCall = CallType.read;
        
        kernel.start();
        //stops the current user process
        kernel.getScheduler().currentProcess.stop();

        return (byte[])deviceReturn;
    }
    static void Seek(int id, int to) throws InterruptedException {
        //reset and add id and to parameter
        parameters.clear();
        parameters.add(id);
        parameters.add(to);

        //sets the note
        currentCall = CallType.seek;
        
        kernel.start();
        //stops the current user process
        kernel.getScheduler().currentProcess.stop();
    }
    static int Write(int id, byte[] data) throws InterruptedException {
        //reset and add id and data parameter
        parameters.clear();
        parameters.add(id);
        parameters.add(data);

        //sets the note
        currentCall = CallType.write;
        
        kernel.start();
        //stops the current user process
        kernel.getScheduler().currentProcess.stop();

        return (int)deviceReturn;
    }

    //returns the current process’ pid
    static int GetPid() throws InterruptedException{
        //reset parameter
        parameters.clear();

        //sets the note
        currentCall = CallType.get_pid;
        
        kernel.start();
        //stops the current user process
        kernel.getScheduler().currentProcess.stop();
        return (int)messageReturn;
    }
    //returns the pid of a process with that name
    static int GetPidByName(String s) throws InterruptedException{
        //reset and add s parameter
        parameters.clear();
        parameters.add(s);

        //sets the note
        currentCall = CallType.get_pid_name;
        
        kernel.start();
        //stops the current user process
        kernel.getScheduler().currentProcess.stop();
        return (int)messageReturn;
    }
    //sends a copy of KernelMessage km to the message queue of target process
    static void SendMessage(KernelMessage km) throws InterruptedException{
        //reset and add km parameter
        parameters.clear();
        parameters.add(km);
        messageReturn = null;

        //sets the note
        currentCall = CallType.send_message;
        
        kernel.start();
        //stops the current user process
        kernel.getScheduler().currentProcess.stop();
    }
    //waits until process gets a message
    static KernelMessage WaitForMessage() throws InterruptedException{
        //reset parameter and messageReturn
        parameters.clear();
        messageReturn = null;

        //variable for this process' queue
        var waitQueue = kernel.getScheduler().getCurrentlyRunning().getWaitQueue();

        //sets the note
        currentCall = CallType.wait_message;
        
        kernel.start();
        //stops the current user process
        kernel.getScheduler().currentProcess.stop();

        //if there's no message currently, waits for message
        while(messageReturn == null){
            //if the process has a message
            if(!waitQueue.isEmpty()){
                //return the message
                messageReturn = waitQueue.pop();
            }
        }
        return (KernelMessage)messageReturn;
    }
    static void GetMapping(int virtualPageNumber) throws InterruptedException{
        //reset parameter
        parameters.clear();
        parameters.add(virtualPageNumber);

        //sets the note
        currentCall = CallType.get_mapping;
        
        kernel.start();
        //stops the current user process
        kernel.getScheduler().currentProcess.stop();
    }
    static int AllocateMemory(int size) throws InterruptedException{
        //reset parameter
        parameters.clear();
        parameters.add(size);
        memoryReturn = null;

        if(size % 1024 != 0){
            return -1;
        }

        //sets the note
        currentCall = CallType.allocate_memory;
        
        kernel.start();
        //stops the current user process
        kernel.getScheduler().currentProcess.stop();

        return (int)memoryReturn;
    }
    static boolean FreeMemory(int pointer, int size) throws InterruptedException{
        //reset parameter
        parameters.clear();
        parameters.add(pointer);
        parameters.add(size);
        memoryReturn = null;

        if(size % 1024 != 0){
            return false;
        }

        //sets the note
        currentCall = CallType.free_memory;
        
        kernel.start();
        //stops the current user process
        kernel.getScheduler().currentProcess.stop();

        return (boolean)memoryReturn;
    }
    static void Exit() throws InterruptedException{
        //reset parameter
        parameters.clear();

        //sets the note
        currentCall = CallType.exit;
        
        kernel.start();
        //stops the current user process
        kernel.getScheduler().currentProcess.stop();
    }
}