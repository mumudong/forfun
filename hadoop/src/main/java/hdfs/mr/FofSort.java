package hdfs.mr;

import org.apache.hadoop.io.WritableComparator;

public class FofSort extends WritableComparator {

	public FofSort() {
		super(Friend.class, true);
	}
	
	public int compare(Object a, Object b) {
		Friend f1 = (Friend) a;
		Friend f2 = (Friend) b;
        //friend类有排序方法，此方法不再执行
        System.out.println("sort------->"+f1.getString()+"<----->"+f2.getString());
		int c1 = f1.getFriend1().compareTo(f2.getFriend1());
		
		if(c1 == 0) {
			return f1.getCount()-f2.getCount();
		}
		
		return c1;
	}
}
