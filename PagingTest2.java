public class PagingTest2 extends UserlandProcess{
    PagingTest2(){
        super();
    }
    public void main() throws InterruptedException {
        //allocate 10 pages then free the 10 pages
        while(true){
            System.out.println("PagingTest2");
            int allocateResult;
            boolean freeResult;

            allocateResult = OS.AllocateMemory(10240);
            System.out.println("Allocating 10240: " + allocateResult);

            freeResult = OS.FreeMemory(allocateResult,10240);
            System.out.println("Freeing: " + freeResult);

            cooperate();
            Thread.sleep(50);
        }
    }
}