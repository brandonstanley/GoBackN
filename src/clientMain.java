import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class clientMain {
	
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
		
		DatagramSocket clientSocket;
		//default settings
		int clientPort=5558;
		int serverPort = 5555;
		String serverIP = "localhost";
		String fileName = "src/supermarket.txt";
		//override settings
		if (args.length==4){
			serverIP=args[0];
			serverPort=Integer.parseInt(args[1]);
			clientPort= Integer.parseInt(args[2]);
			fileName=args[3];
		}else{
			System.out.println("Usage: java clientMain <Server IP> <Server Port> <Client Port> <Filename>");
		}
		int bufferLength=124;
		int sequenceSize=256;
		int windowSize=2;
		byte [] byteArray=new byte[124];
		File file = new File(fileName);
		int numBytes=(int) file.length()-1;
		System.out.println("here is the file size"+numBytes);
//		Map<Integer ,Boolean> windowACKS=new HashMap<Integer,Boolean>();
		ArrayList<Boolean> windowACKS=new ArrayList<Boolean>();
		ArrayList<DatagramPacket>currentWindowDataGrams =new ArrayList<DatagramPacket>();
		
//		try {		

			clientSocket=new DatagramSocket(clientPort);
			clientSocket.setSoTimeout(100);
    		InetAddress host=InetAddress.getByName(serverIP);

    		
			int seqNumber=shared.getExpectedSequenceNumber();
			FileInputStream in = new FileInputStream("src/supermarket.txt");
			int currentByte=0;
			int bytesLeft;
			boolean resendPacket=false;
//			arrayListIsAllTrue(windowACKS);
			while(currentByte<=(numBytes)&&!(arrayListIsAllTrue(windowACKS))){
				//if this is the first time and we're not resending the first packet
				if(currentByte==0&&!resendPacket){
					seqNumber=shared.getExpectedSequenceNumber();
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
						
						String clientMsg=new String(byteArray);
						System.out.println(clientMsg);

						DatagramPacket request=new DatagramPacket(byteArray,byteArray.length,host,serverPort);

						System.out.println("Sending Packet: "+seqNumber);
						clientSocket.send(request);
						windowACKS.add(false);
						currentWindowDataGrams.add(request);
						
						seqNumber=(seqNumber+1)%sequenceSize;
						c++;
					}
				//if a timeout was triggered
				}else if(resendPacket){
					for(DatagramPacket packet: currentWindowDataGrams){
						
//						DatagramPacket request=new DatagramPacket(byteArray,byteArray.length,host,serverPort);
						clientSocket.send(packet);
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
						shared.incrementExpectedSequenceNumber();
						c++;
					}
					currentWindowDataGrams=new ArrayList<DatagramPacket>(newCurrentWindowDataGrams);
					windowACKS=new ArrayList<Boolean>(newWindowACKS);
					
					
					

					seqNumber=(shared.getExpectedSequenceNumber()+c)%sequenceSize;

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
						
						String clientMsg=new String(byteArray);
						System.out.println(clientMsg);

						DatagramPacket request=new DatagramPacket(byteArray,byteArray.length,host,serverPort);

						System.out.println("Sending Packet: "+seqNumber);
						clientSocket.send(request);
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
					clientSocket.receive(response);
					int responseACK=response.getData()[0]&0xff;
					System.out.println("Receiving ACK: "+ responseACK);
//					resendPacket=responseACK==shared.getExpectedSequenceNumber()?false:true;
					if(responseACK<(shared.getExpectedSequenceNumber()+windowSize)){
						resendPacket=false;
//						shared.incrementExpectedSequenceNumber();
//						System.out.println("expected seq num"+shared.getExpectedSequenceNumber());
//						System.out.println("changing"+(responseACK-shared.getExpectedSequenceNumber()));
						windowACKS.set(responseACK-shared.getExpectedSequenceNumber(), true);
						
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
			 					            
			clientSocket.close();
//		} catch (Exception e) {
			// TODO Auto-generated catch block
//			System.out.println("Timeout reached!");
//			e.printStackTrace();
//		} 
		

	}

}
