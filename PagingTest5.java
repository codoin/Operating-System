public class PagingTest5 extends UserlandProcess{
    PagingTest5(){
        super();
    }
    public void main() throws InterruptedException {
        //try to Acess Memory that it shouldn't
        while(true){
            System.out.println("PagingTest5");
            int allocateResult = OS.AllocateMemory(102400);
            System.out.println("Allocating 102400: " + allocateResult);
            OS.GetMapping(11);
            cooperate();
            OS.Exit();
        } 
    }
}