
package hdfs.mr;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.log4j.Logger;


public class FofGroup extends WritableComparator {

	public FofGroup() {
		super(Friend.class, true);
	}
	static Logger logger = Logger.getLogger(FofGroup.class);
    //使用object的compare方法会不起作用!!!
    public int compare(WritableComparable a, WritableComparable b) {
		Friend f1 = (Friend) a;
		Friend f2 = (Friend) b;
        System.out.println("group------->"+f1.getString()+"<----->"+f2.getString());
        int c1 = f1.getFriend1().compareTo(f2.getFriend1());
		
		if(c1 == 0) {
			return f1.getFriend2().compareTo(f2.getFriend2());
		}
		
		return c1;
	}
}
