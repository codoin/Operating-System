public class HelloWorld extends UserlandProcess{
    HelloWorld(){
        super();
    }
    public void main() throws InterruptedException{
        //prints "Hello Word" then OS checks if request stop
        while(true){
            System.out.println(Thread.currentThread().getId() + " Hello World");
            cooperate();
            Thread.sleep(100);
        }
    }
}
