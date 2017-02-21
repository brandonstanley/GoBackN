import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class senderMain {
	
	public static boolean arrayListIsAllTrue(ArrayList<Boolean> arrayList){
		if(arrayList.isEmpty()){
			return false;
		}
		for(Boolean bool:arrayList){
			if(!bool){
//				System.out.println("good");
				return false;
			}
		}
		return true;
	}
	

	public static void main(String[] args) throws IOException {
//		FileInputStream in = new FileInputStream(filename);
//		in.rea
		// TODO Auto-generated method stub
		
		DatagramSocket senderSocket;
		//default settings
		int senderPort=5558;
		int serverPort = 5555;
		String serverIP = "localhost";
		String fileName = "src/supermarket.txt";

		//override settings
		if (args.length==4){
			serverIP=args[0];
			serverPort=Integer.parseInt(args[1]);
			senderPort= Integer.parseInt(args[2]);
			fileName=args[3];
		}else{
			System.out.println(args.length);
			System.out.println("Usage: java senderMain <Server IP> <Server Port> <sender Port> <Filename>");
		}
		int bufferLength=124;
		int sequenceSize=256;
		int windowSize=2;
		byte [] byteArray=new byte[124];
		File file = new File(fileName);
		int numBytes=(int) file.length()-1;
		System.out.println("File size: "+numBytes);
//		Map<Integer ,Boolean> windowACKS=new HashMap<Integer,Boolean>();
		ArrayList<Boolean> windowACKS=new ArrayList<Boolean>();
		ArrayList<DatagramPacket>currentWindowDataGrams =new ArrayList<DatagramPacket>();
		
//		try {		

			senderSocket=new DatagramSocket(senderPort);
			senderSocket.setSoTimeout(100);
    		InetAddress host=InetAddress.getByName(serverIP);

    		
			int seqNumber=sequenceTracker.getExpectedSequenceNumber();
			FileInputStream in = new FileInputStream("src/supermarket.txt");
			int currentByte=0;
			int bytesLeft;
			boolean resendPacket=false;
//			arrayListIsAllTrue(windowACKS);
			while(currentByte<=(numBytes)&&!(arrayListIsAllTrue(windowACKS))){
				//if this is the first time and we're not resending the first packet
				if(currentByte==0&&!resendPacket){
					seqNumber=sequenceTracker.getExpectedSequenceNumber();
					int c=0;
					while(c<windowSize && currentByte<(numBytes-1)){
						byteArray=new byte[124];
						byteArray[0]=(byte) seqNumber;
						bytesLeft=numBytes-currentByte;
						if(bytesLeft>(bufferLength-1)){
							in.read(byteArray, 1, bufferLength-1);
							currentByte=currentByte+bufferLength-1;		
						}else{	
							in.read(byteArray, 1, bytesLeft);
							currentByte=currentByte+bytesLeft;
						}
						
						String senderMsg=new String(byteArray);
						System.out.println(senderMsg);

						DatagramPacket request=new DatagramPacket(byteArray,byteArray.length,host,serverPort);

						System.out.println("Sending Packet: "+seqNumber);
						senderSocket.send(request);
						windowACKS.add(false);
						currentWindowDataGrams.add(request);
						
						seqNumber=(seqNumber+1)%sequenceSize;
						c++;
					}
				//if a timeout was triggered
				}else if(resendPacket){
					for(DatagramPacket packet: currentWindowDataGrams){
						
//						DatagramPacket request=new DatagramPacket(byteArray,byteArray.length,host,serverPort);
						senderSocket.send(packet);
					}
				//if an ACK was received for the first element in the window
				}else if(windowACKS.get(0)){
//					System.out.println("aklsjdfhlkjasdhflkjadshfjk");
					ArrayList<Boolean> newWindowACKS=new ArrayList<Boolean>(windowACKS);
					ArrayList<DatagramPacket> newCurrentWindowDataGrams=new ArrayList<DatagramPacket>(currentWindowDataGrams);

					int c=0;
					while(c<windowSize && windowACKS.get(c)){
						newWindowACKS.remove(c);
						newCurrentWindowDataGrams.remove(c);
						sequenceTracker.incrementExpectedSequenceNumber();
						c++;
					}
					currentWindowDataGrams=new ArrayList<DatagramPacket>(newCurrentWindowDataGrams);
					windowACKS=new ArrayList<Boolean>(newWindowACKS);
					
					
					

					seqNumber=(sequenceTracker.getExpectedSequenceNumber()+c)%sequenceSize;

					while(c>0 && currentByte<(numBytes-1)){
						byteArray=new byte[124];
						byteArray[0]=(byte) seqNumber;
						bytesLeft=numBytes-currentByte;
						if(bytesLeft>bufferLength){
							in.read(byteArray, 1, bufferLength-1);
							currentByte=currentByte+bufferLength-1;		
						}else{	
							in.read(byteArray, 1, bytesLeft+1);
							currentByte=currentByte+bytesLeft;
						}
						
						String senderMsg=new String(byteArray);
						System.out.println(senderMsg);

						DatagramPacket request=new DatagramPacket(byteArray,byteArray.length,host,serverPort);

						System.out.println("Sending Packet: "+seqNumber);
						senderSocket.send(request);
						windowACKS.add(false);
						currentWindowDataGrams.add(request);
						c--;
						seqNumber=(seqNumber+1)%sequenceSize;
					}
				

					
//					System.out.println("here is the new windowACKS: "+windowACKS);

				}	
				
				//to receive a response from server
				byte [] buffer=new byte[bufferLength];
				DatagramPacket response=new DatagramPacket(buffer, buffer.length);
				
				try{
					senderSocket.receive(response);
					int responseACK=response.getData()[0]&0xff;
					System.out.println("Receiving ACK: "+ responseACK);
//					resendPacket=responseACK==sequenceTracker.getExpectedSequenceNumber()?false:true;
					if(responseACK<(sequenceTracker.getExpectedSequenceNumber()+windowSize)){
						resendPacket=false;
//						sequenceTracker.incrementExpectedSequenceNumber();
//						System.out.println("expected seq num"+sequenceTracker.getExpectedSequenceNumber());
//						System.out.println("changing"+(responseACK-sequenceTracker.getExpectedSequenceNumber()));
						windowACKS.set(responseACK-sequenceTracker.getExpectedSequenceNumber(), true);
						
					}else{
						resendPacket=true;
					}
//					System.out.println(windowACKS);
					
				}catch(SocketTimeoutException e){
					resendPacket=true;
					System.out.println("Timeout, set resendPacket flag to true");
				}
				
//				System.out.println(arrayListIsAllTrue(windowACKS));
//				System.out.println(currentByte);
//				System.out.println(numBytes);

//				System.out.println(currentByte<=(numBytes-1));
				System.out.println();
				
			}
			 					            
			senderSocket.close();
//		} catch (Exception e) {
			// TODO Auto-generated catch block
//			System.out.println("Timeout reached!");
//			e.printStackTrace();
//		} 
		

	}

}
