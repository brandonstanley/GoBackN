import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Timer;
import java.util.TimerTask;

public class serverMain {
	
	

	public static void main(String[] args) {

		DatagramSocket serverSocket;
		int serverSocketPort=5555;
		int bufferLength=124;
		int reliabilityNumber=3;
		int numOfPacketsReceived=0;
		int expectedPacketNumber=0;
		int sequenceSize=256;
		
		try{
			serverSocket=new DatagramSocket(serverSocketPort);
			System.out.println("Server is running...");

			byte [] buffer=new byte[bufferLength];
			while(true){
				
				DatagramPacket request =new DatagramPacket(buffer,buffer.length);
				serverSocket.receive(request);
				numOfPacketsReceived++;
				
				String clientMsg=new String(request.getData());
				System.out.println(clientMsg);

				int seqNum=request.getData()[0]&0xff;
				
				
				int remainder=reliabilityNumber==0?-1:numOfPacketsReceived%reliabilityNumber;

				if(reliabilityNumber==0||remainder!=0){
					if(expectedPacketNumber==seqNum){
						System.out.println("Sending ACK:"+seqNum);
						byte [] responseMsg=new byte[1];
						responseMsg[0]=request.getData()[0];
						DatagramPacket response=new DatagramPacket(responseMsg,responseMsg.length,request.getAddress(),request.getPort());
						serverSocket.send(response);
						expectedPacketNumber=(expectedPacketNumber+1)%sequenceSize;
					}else{
						System.out.println(seqNum+" not expected, expecting seqNum: "+expectedPacketNumber);
					}
					
				}else{
					System.out.println("Dropping packet because this is packet #: "+numOfPacketsReceived);
				}
				System.out.println();
				
			}
			
		}catch(Exception e){
			System.out.println(e);
		}
	}

}
