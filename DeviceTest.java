import java.util.Arrays;

public class DeviceTest extends UserlandProcess{
    DeviceTest(){
        super();
    }
    public void main() throws InterruptedException {
        while(true){
            int fd = OS.Open("random 15");
            System.out.println("fd: " + fd);
            OS.Seek(fd, 1000000);
            OS.Write(fd, null);
            byte[] temp = OS.Read(fd, 5);
            System.out.println("Random READ: " + Arrays.toString(temp));
            OS.Close(fd);
            
            fd = OS.Open("file newFile");
            System.out.println("fd: " + fd);
            OS.Write(fd, temp);
            OS.Seek(fd, 0);
            temp = OS.Read(fd, 5);
            System.out.println("File READ: " + Arrays.toString(temp));
            OS.Close(fd);
            cooperate();
            Thread.sleep(200);
        }
    }
}