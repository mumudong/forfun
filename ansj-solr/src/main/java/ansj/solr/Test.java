package ansj.solr;

import org.ansj.library.DicLibrary;
import org.ansj.lucene.util.AnsjTokenizer;
import org.ansj.splitWord.analysis.IndexAnalysis;
import org.nlpcn.commons.lang.tire.domain.Forest;

/**
 * Created by Administrator on 2018/8/17.
 */
public class Test {
    public static String print(){
        String result = "a";
        try{
           result = "b";
           int c = 1/0;
           return result;
        }catch(Exception e){
            result = "e";
            return result;
        } finally{
            result = "c";
        }
    }
    public static void main(String[] args) {
        System.out.println(print());
    }
}
