package hdfs.mr;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

public class Friend implements WritableComparable<Friend> {
	
	private String friend1;
	private String friend2;
	private int count;
	
	public Friend() {
		super();
	}

	public Friend(String friend1, String friend2, int count) {
		this.friend1 = friend1;
		this.friend2 = friend2;
		this.count = count;
	}

	public String getFriend1() {
		return friend1;
	}

	public void setFriend1(String friend1) {
		this.friend1 = friend1;
	}

	public String getFriend2() {
		return friend2;
	}

	public void setFriend2(String friend2) {
		this.friend2 = friend2;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void write(DataOutput out) throws IOException {
		out.writeUTF(friend1);
		out.writeUTF(friend2);
		out.writeInt(count);
	}

	public void readFields(DataInput in) throws IOException {
		this.friend1 = in.readUTF();
		this.friend2 = in.readUTF();
		this.count = in.readInt();
	}
    //本类的排序，map和reduce过程中都会执行
	public int compareTo(Friend friend) {
        System.out.println("friend------->>"+friend.getString());
        int c1 = this.friend1.compareTo(friend.getFriend1());
		if(c1 == 0) {
			return count-friend.getCount();
		}
		return c1;
	}
    public String getString(){
	    return this.friend1 + "\t" + this.friend2 + "\t" + this.count;
    }

}
