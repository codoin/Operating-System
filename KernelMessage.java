import java.util.Arrays;

public class KernelMessage {
    int senderPid, targetPid;
    int message;
    byte[] data;

    //constructor to set the pids, messagem and data
    public KernelMessage(int senderPid, int targetPid, int message, byte[] data){
        this.senderPid = senderPid;
        this.targetPid = targetPid;
        this.message = message;
        this.data = data;
    }
    //copy constructor to pass copy of KernelMessage
    public KernelMessage(KernelMessage km){
        this.senderPid = km.getSender();
        this.targetPid = km.getTarget();
        this.message = km.getMessage();
        this.data = km.getData();
    }
    public int getSender(){
        return senderPid;
    }
    public int getTarget(){
        return targetPid;
    }
    public int getMessage(){
        return message;
    }
    public byte[] getData(){
        return data;
    }
    public void setSender(int pid){
        senderPid = pid;
    }
    public void setTarget(int pid){
        targetPid = pid;
    }
    public void setMessage(int message){
        this.message = message;
    }
    public void setData(byte[] data){
        this.data = data;
    }

    String ToString(){
        return "Message: " + message + " From: " + senderPid + " To: " + targetPid + " Data: " + Arrays.toString(data);
    }
}
