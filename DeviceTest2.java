import java.util.Arrays;

public class DeviceTest2 extends UserlandProcess{
    DeviceTest2(){
        super();
    }
    public void main() throws InterruptedException {
        while(true){
            int fd1 = OS.Open("random 15");
            System.out.println("fd: " + fd1);
            byte[] temp = OS.Read(fd1, 5);
            System.out.println("Random READ: " + Arrays.toString(temp));

            int fd2 = OS.Open("random 100");
            System.out.println("fd: " + fd2);
            OS.Write(fd2, temp);
            OS.Seek(fd2, 0);
            temp = OS.Read(fd2, 5);
            System.out.println("Random2 READ: " + Arrays.toString(temp));
            
            int fd3 = OS.Open("random 63");
            System.out.println("fd: " + fd3);
            OS.Write(fd3, temp);
            OS.Seek(fd3, 0);
            temp = OS.Read(fd3, 5);
            System.out.println("Random2 READ: " + Arrays.toString(temp));
            
            OS.Close(fd1);
            OS.Close(fd2);
            OS.Close(fd3);

            cooperate();
            Thread.sleep(200);
        }
    }
}
