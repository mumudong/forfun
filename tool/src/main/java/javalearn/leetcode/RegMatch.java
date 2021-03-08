package javalearn.leetcode;

/**
 * 动态规划匹配字符串
 *   f[i][j] =  if pattern[j] == '*'
 *                  if str[i] == pattern[j-1]
 *                     f[i-1][j] || f[i][j-2]
 *                  else
 *                     f[i][j-2]
 *              else
 *                  if str[i] == pattern[j]
 *                    f[i-1][j-1]
 *
 */
public class RegMatch {
    public static void main(String[] args) {
        String str = "aabbcde";
        String pattern = "aab*cde";
        System.out.println(match(str,pattern));
    }
    public static boolean match(String str,String pattern){
        int m = str.length();
        int n = pattern.length();
        boolean[][] f = new boolean[m+1][n+1];
        f[0][0] = true;
        for(int i = 0;i <= m;i++){
            for(int j = 1;j <= n;j++){
                //pattern中第j个字符是*
                if(pattern.charAt(j-1) == '*'){
                    f[i][j] = f[i][j-2];
                    //str[i] = pattern[j-1]
                    if(isMatch(str,pattern,i,j-1)){
                        f[i][j] = f[i][j] || f[i-1][j];
                    }
                }else{
                    if(isMatch(str,pattern,i,j)){
                        f[i][j] = f[i-1][j-1];
                    }
                }
            }
        }
        return f[m][n];
    }

    /**
     *  str中第i个字符和pattern中第j个字符匹配
     */
    public static boolean isMatch(String str,String pattern,int i,int j){
        if(i == 0){
            return false;
        }
        if(pattern.charAt(j-1) == '.'){
            return true;
        }
        return str.charAt(i-1) == pattern.charAt(j-1);
    }
}
