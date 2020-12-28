package test;

public class TestOne {
    public static void main(String[] args) {
        StringBuilder builder = new StringBuilder();
        builder.append("123456789");
        StringBuilder abcdefghi = builder.replace(0, 1, "abcdefghi");
        System.out.println(abcdefghi);
    }
}
