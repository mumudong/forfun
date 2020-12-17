package javalearn.stream;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamOne {
    public static void main(String[] args) {
        int printLength = 46;
        List<Person> personList = new ArrayList<>();
        personList.add(new Person("Tom -- 男", 20, "美国", 'M'));
        personList.add(new Person("jerry", 30, "英国", 'F'));
        personList.add(new Person("向天笑 -- 男", 40, "中国", 'M'));
        personList.add(new Person("向地笑 -- 男", 50, "中国", 'M'));
        personList.add(new Person("向左笑", 35, "中国", 'F'));
        personList.add(new Person("向右笑", 25, "中国", 'F'));
        personList.add(new Person("一直笑 -- 男", 15, "中国", 'M'));
        personList.add(new Person("一直笑 -- 男", 15, "中国", 'M'));
        print("小于18岁", printLength);
        //年龄小于18的人
        personList.stream().filter(x -> x.getAge() < 18).forEach(System.out::println);
        print("中国人有多少个?", printLength);
        long count = personList.stream().filter(x -> "中国".equals(x.getCountry())).count();
        System.out.println(count);
        print("测试skip操作", printLength);
        personList.stream().filter(x -> 'M' == x.getSex()).skip(1).forEach(System.out::println);
        print("测试limit操作", printLength);
        personList.stream().filter(x -> 'M' == x.getSex()).limit(1).forEach(System.out::println);
        print("测试distinct操作", printLength);
        personList.stream().filter(x -> 'M' == x.getSex()).distinct().forEach(System.out::println);
        print("map操作,返回名字", printLength);
        personList.stream().map(x -> {
            PersonCountry personCountry = new PersonCountry();
            personCountry.setCountry(x.getCountry());
            return personCountry;
        }).distinct().forEach(System.out::println);

        List<String> list = Arrays.asList("aaa", "bbb", "ccc", "ddd", "ddd");
        print("---",printLength);
        Stream<Stream<Character>> streamStream = list.stream().map(StreamOne::getCharacterByString);
        streamStream.forEach(x -> x.forEach(System.out::println));
        print("flatmap操作",printLength);
        Stream<Character> characterStream = list.stream().flatMap(StreamOne::getCharacterByString);
        characterStream.forEach(System.out::println);
        print("sort操作",printLength);
        personList.stream().sorted((x1,x2)->{
            if (x1.getAge().equals(x2.getAge())) {
                return x1.getName().compareTo(x2.getName());
            }else {
                return x1.getAge().compareTo(x2.getAge());
            }
        }).forEach(System.out::println);
        print("allMatch操作",printLength);
        System.out.println("是否都大于18岁: " + personList.stream().allMatch(x -> x.getAge() > 18));
        System.out.println("是否都是中国人: " + personList.stream().allMatch(x -> "中国".equals(x.getCountry())));
        print("最大年龄的人",printLength);
        Optional<Person> max = personList.stream().max((x1, x2) -> x1.getAge().compareTo(x2.getAge()));
        System.out.println(max.get());
        print("年龄总和",printLength);
        Optional<Integer> reduce = personList.stream().map(x -> x.getAge()).reduce(Integer::sum);
        System.out.println("年龄总和: " + reduce.get());
        print("collect操作",printLength);
        List<String> collect = personList.stream().map(x -> x.getCountry()).distinct().collect(Collectors.toList());
        System.out.println(collect);
        print("平均年龄",printLength);
        System.out.println(personList.stream().map(x -> x.getAge()).collect(Collectors.averagingInt(x->x.intValue())));
        print("最小年龄",printLength);
        try (Stream<Integer> integerStream = personList.stream().map(Person::getAge)) {
            System.out.println(integerStream.collect(Collectors.minBy(Integer::compare)).get());
        }


    }

    public static Stream<Character> getCharacterByString(String str) {
        List<Character> characterList = new ArrayList<>();
        for (Character character : str.toCharArray()) {
            characterList.add(character);
        }
        return characterList.stream();
    }

    public static void print(String words, int length) {
        int len = (length - words.length()) >> 1;
        for (int i = 0; i < length; i++) {
            System.out.print("--");
            if (i == len) {
                System.out.print(words);
            }
        }
        System.out.println();
    }
}

@Data
class PersonCountry {
    private String country;
}

@Data
class Person {
    private String name;
    private Integer age;
    private String country;
    private char sex;

    public Person(String name, Integer age, String country, char sex) {
        this.name = name;
        this.age = age;
        this.country = country;
        this.sex = sex;
    }
}
