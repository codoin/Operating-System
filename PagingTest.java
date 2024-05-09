public class PagingTest extends UserlandProcess{
    PagingTest(){
        super();
    }
    public void main() throws InterruptedException {
        int count = 0;
        byte value = 10;
        while(true){
            System.out.println("PagingTest");
            for(int i = 0; i < 7; i++){
                count = OS.AllocateMemory(1024) * 1024;
                Thread.sleep(10);

                System.out.println("Writing: " + value + " to " + count + " ");
                Write(count, value);

                byte result = Read(count);
                System.out.println("Reading: " + result + " at " + count);
                System.out.println("-------------------------");
                value++;
            }
            OS.FreeMemory(0, 1024*7);
            count = 0;
            cooperate();
            Thread.sleep(50);
        }
    }
}