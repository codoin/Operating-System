public class VirtualToPhysicalMapping {
    private int physicalPageNumber;
    private int diskPageNumber; //the "offset" into the disk (multiply by 1024)

    VirtualToPhysicalMapping(){
        physicalPageNumber = -1;
        diskPageNumber = -1;
    }

    public int getPhysicalPageNumber(){
        return physicalPageNumber;
    }

    public int getDiskPageNumber(){
        return diskPageNumber;
    }
    
    public void setPhysicalPageNumber(int pageNumber){
        physicalPageNumber = pageNumber;
    }

    public void setDiskPageNumber(int pageNumber){
        diskPageNumber = pageNumber;
    }

    public String toString(){
        return "[Physical: " + physicalPageNumber + " Disk: " + diskPageNumber + "]"; 
    }
    //now when allocate memory is -1 then it means we have swapped
}
