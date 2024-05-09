public class PagingTest4 extends UserlandProcess{
    PagingTest4(){
        super();
    }
    public void main() throws InterruptedException {
        //killing a process clears the memory
        while(true){
            System.out.println("PagingTest4");
            int allocateResult = OS.AllocateMemory(102400);
            System.out.println("Allocating 102400: " + allocateResult);
            cooperate();
            OS.Exit();
        } 
    }
}