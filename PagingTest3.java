public class PagingTest3 extends UserlandProcess{
    PagingTest3(){
        super();
    }
    public void main() throws InterruptedException {
        while(true){
            System.out.println("PagingTest3");
            int allocateResult;
            for(int i = 0; i < 10; i++){
                allocateResult = OS.AllocateMemory(1024);
                System.out.println("Allocating 1024: " + allocateResult);
            }
            System.out.println("Freeing 10240: " + OS.FreeMemory(0, 10240));
            cooperate();
            Thread.sleep(50);
            OS.Exit();
        } 
    }
}