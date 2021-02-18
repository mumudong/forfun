package javalearn.basic_alth;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

/**
 * 求最长子序列
 */
public class ShortLIS {
    public static void main(String[] args){
        int[] arr = new int[10];
        Random random = new Random();
        for (int i = 0; i < arr.length; i++) {
            arr[i] = random.nextInt(100);
        }
        findLongest(arr,arr.length-1);

        System.out.println("数组"+ Arrays.toString(arr));
        LIS lis = new LIS(arr);

        long time = System.currentTimeMillis();
        System.out.println("结果1: "+lis.foo());
        System.out.println("耗时1: "+(System.currentTimeMillis()-time));

        time = System.currentTimeMillis();
        System.out.println("结果2: "+fun(arr, arr.length-1));
        System.out.println("耗时2: "+(System.currentTimeMillis()-time));
    }
    // 最长递增子序列 longest increasing subsequence
    private static class LIS{
        int[] arr;
        HashMap<Integer,Integer> values = new HashMap<>();

        LIS(int[]arr){
            this.arr = arr;
        }
        int foo(){
            return foo(arr,arr.length-1);
        }
        private int foo(int[] arr,int end){
            Integer value = values.get(end);
            if (value != null) {
                return value;
            }
            if (end==0) {
                values.put(0,1);
                return 1;
            }
            int len = 0;
            for (int i = 0; i < end; i++) {
                int temp = foo(arr,i);
                len = Math.max(len,arr[end]>arr[i]?temp+1:temp);
            }
            values.put(end,len);
            return len;
        }

    }
    private static int fun(int[] arr,int end){
        if (end==0) {
            return 1;
        }
        int len = 0;
        for (int i = 0; i < end; i++) {
            int temp = fun(arr,i);
            len = Math.max(len,arr[end]>arr[i]?temp+1:temp);
        }
        return len;
    }

    public static int findLongest(int[] A, int n) {
        int[] B = new int[n+1]; B[1] = A[0];
        int len=1,start=0,end=len,mid;
        for(int i = 1;i<n;i++){
            if(A[i]>B[len]) {len++;B[len] = A[i];}
            else{
                start=1;end=len;
                while(start<=end){
                    mid=(start+end)/2;
                    if(B[mid]<A[i]) start=mid+1;
                    else end=mid-1;
                } B[start] = A[i];
            }
        }
        System.out.println();
        for(int i = 0;i < n;i++){
            System.out.print(B[i] + "    ");
        }
        System.out.println();
        return len;
    }

}
