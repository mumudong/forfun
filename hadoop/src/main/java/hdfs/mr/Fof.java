package hdfs.mr;

import org.apache.hadoop.io.Text;

public class Fof extends Text {

	public Fof() {
		super();
	}
	
	public Fof(String friend1, String friend2) {
		super(getFof(friend1, friend2));
	}
	
	private static  String getFof(String friend1, String friend2) {
		int c1 = friend1.compareTo(friend2);
		if(c1 > 0) {
			return friend1 + "\t" + friend2;
		}
		return friend2 + "\t" + friend1;
	}
}
