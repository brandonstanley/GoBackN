import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Timer;
import java.util.TimerTask;

public class receiverMain {
	
	

	public static void main(String[] args) {

		DatagramSocket receiverSocket;
		
		int receiverSocketPort=5555;
		int senderSocketPort=5558;
		String senderIP="localhost";
		String fileName = "receiverWrite";
		int bufferLength=124;
		int reliabilityNumber=3;
		int numOfPacketsReceived=0;
		int expectedPacketNumber=0;
		int sequenceSize=256;
		if (args.length==5){
			senderIP = args[0];
			senderSocketPort = Integer.parseInt(args[1]);
			receiverSocketPort = Integer.parseInt(args[2]);
			fileName = args[3];
			reliabilityNumber = Integer.parseInt(args[4]);
		}else{
			System.out.println("Usage: java receiverMain <Sender IP> <Sender Port> <Receiver Port> <Filename> <Reliability Number>");
		}
		
		try{
			receiverSocket=new DatagramSocket(receiverSocketPort);
			System.out.println("receiver is running...");

			byte [] buffer=new byte[bufferLength];
			while(true){
				
				DatagramPacket request =new DatagramPacket(buffer,buffer.length);
				receiverSocket.receive(request);
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
						receiverSocket.send(response);
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
