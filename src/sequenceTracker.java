
public class sequenceTracker {
	private static int expectedSequenceNumber=0;
	private static int sequenceSize=256;
	
	public static int getExpectedSequenceNumber(){
		return expectedSequenceNumber;
	}
	
	public static int incrementExpectedSequenceNumber(){
		return expectedSequenceNumber=(expectedSequenceNumber+1)%sequenceSize;
	}	
}
