import java.util.Random;

public class RandomDevice implements Device{
    static Random[] randomArray = new Random[10];

    //will create a new Random device and put it in an empty spot in the array. 
    //return index of stored random if works, else return -1 if error
    public int Open(String s) {
        //finds an empty space in the array
        for(int i = 0; i < 10; i++){
            if(randomArray[i] == null){
                //if s is not null or empty, s is the seed for the Random class
                if(s == null || s.isEmpty()){
                    randomArray[i] = new Random();
                }
                else{
                    randomArray[i] = new Random(Long.parseLong(s));
                }
                return i;
            }
        }
        return -1;
    }
    //set entry inside of array at index id to null to remove Random from array
    public void Close(int id) {
        System.out.println("RandomDevice Close FD: " + id);
        randomArray[id] = null;
    }
    //create an array with the specific device's seeded random numbers
    //returns the array
    public byte[] Read(int id, int size) {
        System.out.println("RandomDevice Read FD: " + id);
        byte[] temp = new byte[size];
        //fills array with the random integers
        for(int i = 0; i < size; i++){
            temp[i] = (byte) randomArray[id].nextInt();
        }
        return temp;
    }
    //read random bytes but doesn't return
    public void Seek(int id, int to) {
        System.out.println("RandomDevice Seek FD: " + id);
        randomArray[id].nextInt();
    }
    //does nothing
    //return 0
    public int Write(int id, byte[] data) {
        System.out.println("RandomDevice Write FD: " + id);
        return 0;
    }
}
