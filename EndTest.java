public class EndTest extends UserlandProcess{
    EndTest(){
        super();
    }
    public void main() throws InterruptedException{
        //prints "Hello Word" then OS checks if request stop
        System.out.println(Thread.currentThread().getId() + "YOOOOOOOOOOOOOOOOOOOOOOOOOO");
        System.out.println(Thread.currentThread().getId() + "YOOOOOOOOOOOOOOOOOOOOOOOOOO");
        System.out.println(Thread.currentThread().getId() + "YOOOOOOOOOOOOOOOOOOOOOOOOOO");
        System.out.println(Thread.currentThread().getId() + "YOOOOOOOOOOOOOOOOOOOOOOOOOO");
        System.out.println(Thread.currentThread().getId() + "YOOOOOOOOOOOOOOOOOOOOOOOOOO");
        cooperate();
        Thread.sleep(100);
        OS.Sleep(10);
    }
}
