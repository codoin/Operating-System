public class Main {
    public static void main(String[] args) throws InterruptedException{
        OS.Startup(new HelloWorld());
        OS.CreateProcess(new GoodbyeWorld());

        //testing virtual memory by allocating 2K of memory
        for(int i = 0; i < 20; i++){
            OS.CreateProcess(new VirtualMemoryTest());
        }

        OS.CreateProcess(new PagingTest());
        Thread.sleep(1000);
        OS.CreateProcess(new PagingTest5());
    }
}