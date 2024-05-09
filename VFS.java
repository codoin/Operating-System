import java.util.Arrays;

public class VFS implements Device{
    Device[] devices = new Device[10];
    int[] idArray = new int[]{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};

    //maps other devices' "open" into parallel arrays 
    //returns the index where the device is stored
    public int Open(String s) {
        //System.out.println("Open: " + s + arraysToString());

        //splits s into two parts: type of device and parameter
        String[] param = s.split(" ");
        Device temp = null;
        int id;
        //checks type of device then creates one
        if(param[0].equals("random")){
            temp = new RandomDevice();
        }
        else if(param[0].equals("file")){
            temp = new FakeFileSystem();
        }
        //if type of device doesn't match (error)
        else{
            return -1;
        }

        //fills empty spot in devices array
        for(int i = 0; i < 10; i++){
            //if this index is empty
            if(devices[i]==null){
                //stores the device
                devices[i] = temp;
                //open the device with param if exist else just open
                if(param.length > 1){
                    id = devices[i].Open(param[1]);
                }
                else{
                    id = devices[i].Open("");
                }
                //stores the fd into the idArray
                idArray[i] = id;

                //System.out.println("Open Now: " + arraysToString());
                return i;
            }
        }
        return -1;
    }
    //removes the device and id entries in the arrays 
    //calles the the device's, associated id, 'close'
    public void Close(int id) {
        //System.out.println("Close: " + devices[id].toString() + arraysToString());
        devices[id].Close(idArray[id]);
        devices[id] = null;
        idArray[id] = -1;
        //System.out.println("Close Now: " + arraysToString());
    }
    //called the 'read' method from the device associated with id
    public byte[] Read(int id, int size) {
        return devices[id].Read(idArray[id], size);
    }
    //called the 'seek' method from the device associated with id
    public void Seek(int id, int to) {
        devices[id].Seek(idArray[id], to);
    }
    //called the 'write' method from the device associated with id
    public int Write(int id, byte[] data) {
        return devices[id].Write(idArray[id], data);
    }

    @SuppressWarnings("unused")
    private String arraysToString(){
        return " Devices: " + Arrays.toString(devices) + " Ids: " + Arrays.toString(idArray);
    }
}