public class Testing extends UserlandProcess{
    Testing(){
        super();
    }
    public void main() throws InterruptedException{
        //prints "Test" then OS checks if request stop
        //should get demoted
        while(true){
            System.out.println("Test");
            cooperate();
            Thread.sleep(50);
            OS.Sleep(10);
        }
    }
}
