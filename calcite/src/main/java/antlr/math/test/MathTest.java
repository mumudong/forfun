package antlr.math.test;

import antlr.math.rule.gen.MathBaseVisitor;
import antlr.math.rule.gen.MathLexer;
import antlr.math.rule.gen.MathParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class MathTest {
    public static void main(String[] args) {
        CharStream input = CharStreams.fromString("12*2+12\r\n");
        MathLexer lexer=new MathLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MathParser parser = new MathParser(tokens);
        ParseTree tree = parser.prog(); // parse

        MathBaseVisitor visitor = new MathBaseVisitor();
        visitor.visit(tree);
    }
}
