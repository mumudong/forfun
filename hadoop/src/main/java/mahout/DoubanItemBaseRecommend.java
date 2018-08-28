package mahout;

import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.precompute.FileSimilarItemsWriter;
import org.apache.mahout.cf.taste.impl.similarity.precompute.MultithreadedBatchItemSimilarities;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.precompute.BatchItemSimilarities;

import java.io.File;
import java.util.List;

public class DoubanItemBaseRecommend {
    public static void main(String[] args) throws Exception {
        String base = "D:\\ksdler\\git_repository\\forfun\\hadoop\\data\\";
        File file = new File(base + "move.csv");
        DataModel model = new FileDataModel(file);

        //http://www.cnphp6.com/archives/84955
        //曼哈顿相似度
        //UserSimilarity similarity = new org.apache.mahout.cf.taste.impl.similarity.CityBlockSimilarity(model);
        //欧几里德相似度
        //UserSimilarity similarity = new org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity(model);
        //对数似然相似度
        ItemSimilarity similarity = new org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity(model);
        System.out.println(similarity.itemSimilarity(105,107));
        //斯皮尔曼相似度
        //UserSimilarity similarity = new org.apache.mahout.cf.taste.impl.similarity.SpearmanCorrelationSimilarity(model);
        //Tanimoto 相似度
        //UserSimilarity similarity = new org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity(model)
        //Cosine相似度
        //UserSimilarity similarity = new org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity();

        //皮尔逊相似度
//        ItemSimilarity similarity = new PearsonCorrelationSimilarity(model);
        ItemBasedRecommender recommender = new GenericItemBasedRecommender(model, similarity);
//迭代获取用户的id
        LongPrimitiveIterator it = model.getUserIDs();

        while(it.hasNext()){
            long uid = it.nextLong();
            //获得推荐结果，给userID推荐howMany个Item
            List<RecommendedItem> list = recommender.recommend(uid, 3);
            System.out.printf("uid:%s",uid);
            for(RecommendedItem items : list){
                System.out.printf("(%s,%f)",items.getItemID(),items.getValue());
            }
            System.out.println();
        }


//        BatchItemSimilarities batch = new MultithreadedBatchItemSimilarities(recommender, 3);
//        int numSimilarities = batch.computeItemSimilarities(Runtime.getRuntime().availableProcessors(), 1, new FileSimilarItemsWriter(new File(base + "item_result.csv")));
//
//        System.out.println("Computed " + numSimilarities + " similarities for " + model.getNumItems() + " items " + "and saved them to file " + base + "item_result.csv");
    }
}
