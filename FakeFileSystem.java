import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FakeFileSystem implements Device{
    static RandomAccessFile[] fileArray = new RandomAccessFile[10];
    
    //creates and puts a RandomAccessFile into the array
    //return index of stored file if works, else return -1 on error
    public int Open(String s) {
        //throws exception if filename is empty or null
        if(s == null || s.isEmpty()){
            try {
                throw new Exception("Filename is Empty");
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }

        //finds empty spot in the array
        for(int i = 0; i < 10; i++){
            //create a new RandomAccessFile at empty index
            if(fileArray[i]==null){
                try {
                    fileArray[i] = new RandomAccessFile(s, "rwd");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return -1;
                }
                return i;
            }
        }
        return -1;
    }

    //closes the RandomAccessFile then clears the file from the array
    public void Close(int id) {
        System.out.println("File Close FD: " + id);
        try {
            fileArray[id].close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //sets file to null
        fileArray[id] = null;
    }

    //gets array of byte from file associated with id
    public byte[] Read(int id, int size) {
        //System.out.println("File Read FD: " + id);
        byte[] temp = new byte[size];
        try {
            //stores the values of read into temp array
            fileArray[id].read(temp,0,size);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return temp;
    }

    //moves the file pointer of the file associated with id by 'to'
    public void Seek(int id, int to) {
        //System.out.println("File Seek FD: " + id);
        try {
            fileArray[id].seek(to);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //stores data array into file associated with id
    //returns the size of the write
    public int Write(int id, byte[] data) {
        //System.out.println("File Write FD: " + id);
        try {
            fileArray[id].write(data);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return data.length;
    }
}
