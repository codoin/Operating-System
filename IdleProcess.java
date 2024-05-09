public class IdleProcess extends UserlandProcess{
    IdleProcess(){
        super();
    }
    public void main() throws InterruptedException{
        //prints "IDLE" then OS checks if request stop
        while(true){
            System.out.println(Thread.currentThread().getId() + " IDLE");
            cooperate();
            Thread.sleep(100);
        }
    }
}
