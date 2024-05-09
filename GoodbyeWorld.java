public class GoodbyeWorld extends UserlandProcess{
    GoodbyeWorld(){
        super();
    }
    public void main() throws InterruptedException{
        //prints "Goodbye Word" then OS checks if request stop
        while(true){
            System.out.println(Thread.currentThread().getId() + " GoodBye World");
            cooperate();
            Thread.sleep(100);
        }
    }
}