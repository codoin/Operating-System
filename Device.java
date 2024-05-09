public interface Device {
    //open file for read/write/both
    //can also create file if doesnt exist
    //returns file descriptor (id) or -1 if fail
    int Open(String s);

    //tells OS you are done with file descriptor
    //closes file pointed by file descriptor
    //id is the file descriptor
    void Close(int id);

    //read file by file descriptor
    //read size amount of bytes
    //returns array of bytes
    byte[] Read(int id, int size);

    //sets the position in the file by id
    void Seek(int id, int to);

    //writes bytes from data to the file by id
    //returns number of bytes written/ 0 if EOF/ -1 on error
    int Write(int id, byte[] data);
}