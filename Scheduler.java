import java.time.Clock;
import java.time.temporal.ChronoField;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class Scheduler {
    private Timer timer = null;
    private Clock clock = null;
    PCB currentProcess = null;
    LinkedList<PCB> realList = null;
    LinkedList<PCB> interactiveList = null;
    LinkedList<PCB> backgroundList = null;
    LinkedList<PCB> sleepList = null;
    LinkedList<PCB> waitList = null;

    Kernel kernel;

    Scheduler(){
        realList = new LinkedList<>();
        interactiveList = new LinkedList<>();
        backgroundList = new LinkedList<>();
        sleepList = new LinkedList<>();
        waitList = new LinkedList<>();
        clock = Clock.systemDefaultZone();
        timer = new Timer(true);

        //Schedules an interrupt to stop the current process
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run(){
                if(currentProcess != null){
                    currentProcess.requestStop();
                }
            }
        }, 0, 250); //250ms
    }

    //Creates a new kerneland, adds to the list, returns pid 
    public int CreateProcess(UserlandProcess up, PCB.Priority priority) throws InterruptedException{
        //userland process is added to list
        PCB process = new PCB(up, priority);
        process.setSched(this);
        addToList(process);

        //switch process if nothing else is running
        if(!CurrentlyRunning()){
            SwitchProcess();
        }
        return up.getPid();
    }

    //Gets the first process from the list, runs the process
    public void SwitchProcess() throws InterruptedException{
        //unsleep task in sleeplist based on time
        unsleepProcess();

        //if it is not start up or if the process is alive
        if(CurrentlyRunning()){
            if(!currentProcess.isDone()){
                //hits switch process instead of sleep so it times out
                //if process times out more than 5 times, demote
                currentProcess.timeout++;
                demote();

                //clear TLB on task switch
                currentProcess.ULP.getTLB()[0][0] = -1;
                currentProcess.ULP.getTLB()[0][1] = -1;
                currentProcess.ULP.getTLB()[1][0] = -1;
                currentProcess.ULP.getTLB()[1][1] = -1;

                //put current running process to the end of process list
                addToList(currentProcess);
            }
            else{
                //close all of its open devices
                int[] temp = getCurrentlyRunning().processIds;
                for(int i = 0; i < temp.length; i++){
                    if(temp[i] != -1){
                        kernel.Close(i);
                    }
                }

                //free all of the process memory
                VirtualToPhysicalMapping[] map = getCurrentlyRunning().MemoryMap;
                for(int j = 0; j < map.length; j++){
                    map[j] = null;
                }
            }
        }
        Thread.sleep(10);

        //next process in list starts to run
        popProcess();
    }
    //returns true is there is a current process
    boolean CurrentlyRunning(){
        return currentProcess != null;
    }
    PCB getCurrentlyRunning(){
        return currentProcess;
    }
    void setCurrentlyRunning(PCB next){
        currentProcess = next;
    }

    //change the currently running process and put the process in a separate list for sleeping processes. 
    //Sleep() is not a guarantee that we will wake up right then, just not before.
    void Sleep(int milliseconds) throws InterruptedException{
        //call sleep then reset timeout
        currentProcess.timeout = 0;

        //if all the priority list are empty sleep to unsleep the process 
        if(realList.isEmpty() && interactiveList.isEmpty() && backgroundList.isEmpty()){
            Thread.sleep(milliseconds);
            addToList(currentProcess);
            currentProcess = null;
        }
        else{
           //sets the time the process should unsleep and add process to sleep list
            currentProcess.time = milliseconds + getTime();
            sleepList.add(currentProcess); 
        }
        
        //change currently running process
        unsleepProcess();
        popProcess();
    }

    //puts the process to the list with correct priority
    void addToList(PCB process){
        switch (process.getPriority()) {
            case real_time:
                realList.add(process);
                break;
            case interactive:
                interactiveList.add(process);
                break;
            case background:
                backgroundList.add(process);
                break; 
        }
    }

    void popProcess(){
        //represent the probabilistic model of choosing which process to run next
        int random = (int)(Math.random()*10)+1;
        // 6/10 real-time
        if(random >= 1 && random <= 6 && !realList.isEmpty()){
            currentProcess = realList.pop();
        }
        // 3/10 interactive
        else if(random > 6 && random <= 9 && !interactiveList.isEmpty()){
            currentProcess = interactiveList.pop();
        }
        // 1/10 background
        else if(random == 10 && !backgroundList.isEmpty()){
            currentProcess = backgroundList.pop();
        }
        else{
            //if this list is empty try the other lists
            if(!realList.isEmpty()){
                currentProcess = realList.pop();
            }
            else if(!interactiveList.isEmpty()){
                currentProcess = interactiveList.pop();
            }
            else{
                currentProcess = backgroundList.pop();
            }
        }
    }

    void unsleepProcess(){        
        for(PCB process : sleepList){
            //if the current time has passed the process sleep time
            if(getTime() >= process.time){
                //add back to original list and remove process from sleep list
                addToList(sleepList.pop());
            }
            else{
                break;
            }
        }
    }

    void demote(){
        //if the current process switches process (times out) more than 5 times
        if(currentProcess.timeout > 5){
            //demote the process (real-time -> interactive -> background)
            if(currentProcess.getPriority() == PCB.Priority.real_time){
                currentProcess.setPriority(PCB.Priority.interactive);
            }
            else{
                currentProcess.setPriority(PCB.Priority.background);
            }
        }
    }

    //returns the time in millisecond
    int getTime(){
        return (int)clock.instant().get(ChronoField.MILLI_OF_SECOND);
    }

    //helper method for testing, shows all the list
    String ToString(){
        return " " + "R" + realList.toString() + " I" + interactiveList.toString() + " B" + backgroundList.toString() + " S" + sleepList.toString() + " W" + waitList.toString();
    }

    //scheduler hold reference to the kernel
    void setKernel(Kernel kern){
        kernel = kern;
    }

    //returns the current process’ pid
    int GetPid(){
        return currentProcess.getPid();
    }
    //returns the pid of a process with that name
    int GetPidByName(String s){
        //goes through all the list to find process with matching name, then return the process' name
        for(PCB p:realList){
            if(s.equals(p.getName())){
                return p.getPid();
            }
        }
        for(PCB p:interactiveList){
            if(s.equals(p.getName())){
                return p.getPid();
            }
        }
        for(PCB p:backgroundList){
            if(s.equals(p.getName())){
                return p.getPid();
            }
        }
        for(PCB p:sleepList){
            if(s.equals(p.getName())){
                return p.getPid();
            }
        }
        for(PCB p:waitList){
            if(s.equals(p.getName())){
                return p.getPid();
            }
        }
        //return -1 if process with name doesn't exist in the lists
        return -1;
    }
    //calls PCB GetMapping()
    void GetMapping(int virtualPageNumber, boolean[] inUseArray) throws InterruptedException{
        currentProcess.GetMapping(virtualPageNumber, inUseArray);
    }
    void Exit(){
        //unsleep task in sleeplist based on time
        unsleepProcess();

        System.out.println("Exiting Process (Free All Memory)");

        //free all of the process memory
        kernel.FreeMemory(0,102400);

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //next process in list starts to run
        popProcess();
    }

    //puts all processes into a list
    public LinkedList<PCB> getAllProcess(){
        LinkedList<PCB> list = realList;
        list.addAll(interactiveList);
        list.addAll(backgroundList);
        list.addAll(sleepList);
        list.addAll(waitList);
        list.add(currentProcess);
        return list;
    }

    //returns a random Process
    public PCB getRandomProcess() throws InterruptedException{
        LinkedList<PCB> list = getAllProcess();
        int random = (int)(Math.random()*list.size());
        return list.get(random);
    }
}
