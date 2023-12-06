package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.DynamicJavaCompileAndExecute.PLCLangExec;
import edu.ufl.cise.cop4020fa23.ast.AST;
import edu.ufl.cise.cop4020fa23.ast.ASTVisitor;
import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.SyntaxException;
import edu.ufl.cise.cop4020fa23.exceptions.TypeCheckException;
import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO;
import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
import edu.ufl.cise.cop4020fa23.runtime.PixelOps;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.Duration;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import static edu.ufl.cise.cop4020fa23.Kind.NUM_LIT;
import static edu.ufl.cise.cop4020fa23.Kind.STRING_LIT;
import static org.junit.jupiter.api.Assertions.*;

class HW5Tests {

    String packageName = "edu.ufl.cise.cop4020fa23";
    String testURL = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d3/Statue_of_Liberty%2C_NY.jpg/1280px-Statue_of_Liberty%2C_NY.jpg";
    String owl = "https://pocket-syndicated-images.s3.amazonaws.com/622ad94833741.png";

    @AfterEach
    public void separatingLine() {
        show("----------------------------------------------");
    }

    // makes it easy to turn output on and off (and less typing than
    // System.out.println)
    static final boolean VERBOSE = true;
    static final boolean WAIT_FOR_INPUT = false;

    void show(Object obj) {
        if (VERBOSE) {
            System.out.println(obj);
        }
    }

    void showPixel(int p) {
        if (VERBOSE) {
            System.out.println(Integer.toHexString(p));
        }
    }

    /**
     * Displays the given image on the screen. If WAIT_FOR_INPUT, a prompt to enter
     * a char is displayed on the console, and execution waits until some character
     * is entered. This is to ensure that displayed images are not immediately
     * closed by Junit before you have a chance to view them.
     *
     * @param image
     * @throws IOException
     */
    void show(BufferedImage image) throws IOException {
        if (VERBOSE) {
            ConsoleIO.displayImageOnScreen(image);
            if (WAIT_FOR_INPUT) {
                System.out.println("Enter a char");
                int ch = System.in.read();
            }
        }

    }

    void compareImages(BufferedImage image0, BufferedImage image1) {
        assertEquals(image0.getWidth(), image1.getWidth(), "widths not equal");
        assertEquals(image0.getHeight(), image1.getHeight(), "heights not equal");
        for (int y = 0; y < image0.getHeight(); y++)
            for (int x = 0; x < image0.getWidth(); x++) {
                int p0 = image0.getRGB(x, y);
                int p1 = image1.getRGB(x, y);
                assertEquals(p0, p1, "pixels at [" + x + "," + y + "], expected: " + Integer.toHexString(p0)
                        + ", but was: " + Integer.toHexString(p1));
            }
    }



    /*
    package edu.ufl.cise.cop4020fa23;
import java.awt.image.BufferedImage;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;
public class test{
public static int apply(String s$1, int x$1, int y$1){
BufferedImage i$2=FileURLIO.readImage(s$1);
return ImageOps.getRGB(i$2,x$1,y$1);
}

}
     */
    @Test
    void test0() throws Exception {
        String source = """
                pixel test(string s, int x, int y) <:
                    image i = s;
                    ^i[x,y];
                :>
                """;
        Object result = PLCLangExec.runCode(packageName, source, owl, 1, 1);
        showPixel((int) result);
        BufferedImage image = FileURLIO.readImage(owl);
        int expectedVal = ImageOps.getRGB(image, 1, 1);
        assertEquals((int) result, expectedVal);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
public class test{
public static int apply(){
return 255;
}

}
     */
    @Test
    void test1() throws Exception {
        String source = """
                int test()<:
                  ^Z;
                :>
                """;
        Object result = PLCLangExec.runCode(packageName, source);
        show((int) result);
        assertEquals(255, (int) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import java.awt.image.BufferedImage;
import edu.ufl.cise.cop4020fa23.runtime.PixelOps;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;
public class test{
public static int apply(String s$1, int x$1, int y$1){
BufferedImage i$2=FileURLIO.readImage(s$1);
return PixelOps.red(ImageOps.getRGB(i$2,x$1,y$1));
}

}
     */
    @Test
    void test2() throws Exception {
        String source = """
                int test(string s, int x, int y) <:
                    image i = s;
                    ^i[x,y]:red;
                :>
                """;
        Object result = PLCLangExec.runCode(packageName, source, owl, 1, 1);
        showPixel((int) result);
        BufferedImage image = FileURLIO.readImage(owl);
        int expectedVal = PixelOps.red(ImageOps.getRGB(image, 1, 1));
        assertEquals((int) result, expectedVal);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import java.awt.image.BufferedImage;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;
public class test{
public static BufferedImage apply(String s$1){
BufferedImage i$2=FileURLIO.readImage(s$1);
return ImageOps.extractRed(i$2);
}

}
     */
    @Test
    void test3() throws Exception {
        String source = """
                image test(string s) <:
                    image i = s;
                    ^i:red;
                :>
                """;
        Object result = PLCLangExec.runCode(packageName, source, owl);
        BufferedImage image = FileURLIO.readImage(owl);
        image = ImageOps.extractRed(image);
        compareImages(image, (BufferedImage) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import java.awt.image.BufferedImage;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;
public class test{
public static BufferedImage apply(String s$1){
BufferedImage i$2=FileURLIO.readImage(s$1);
BufferedImage j$2=ImageOps.cloneImage(i$2);
;
return ImageOps.extractRed(j$2);
}

}
     */
    @Test
    void test4() throws Exception {
        String source = """
                image test(string s) <:
                    image i = s;
                    image j = i;
                    ^j:red;
                :>
                """;
        Object result = PLCLangExec.runCode(packageName, source, owl);
        BufferedImage image = FileURLIO.readImage(owl);
        image = ImageOps.extractRed(image);
        compareImages(image, (BufferedImage) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
public class LGRAYPixel{
public static int apply(){
return 0xffc0c0c0;
}

}
     */
    @Test
    void test5() throws Exception {
        String source = """
                pixel LGRAYPixel()<:
                           ^LIGHT_GRAY;
                         :>

                """;
        Object result = PLCLangExec.runCode(packageName, source);
        showPixel((int) result);
        assertEquals(Color.LIGHT_GRAY.getRGB(), (int) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
public class WHITEPixel{
public static int apply(){
return 0xffffffff;
}

}
     */
    @Test
    void test6() throws Exception {
        String source = """
                pixel WHITEPixel()<:
                           ^WHITE;
                         :>

                """;
        Object result = PLCLangExec.runCode(packageName, source);
        showPixel((int) result);
        assertEquals(Color.WHITE.getRGB(), (int) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
public class BLACKPixel{
public static int apply(){
return 0xff000000;
}

}
     */
    @Test
    void test7() throws Exception {
        String source = """
                pixel BLACKPixel()<:
                           ^BLACK;
                         :>

                """;
        Object result = PLCLangExec.runCode(packageName, source);
        showPixel((int) result);
        assertEquals(Color.BLACK.getRGB(), (int) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
public class BLACKPixel{
public static int apply(){
return 0xff000000;
}

}
     */
    @Test
    void test8() throws Exception {
        String source = """
                pixel BLACKPixel()<:
                           ^BLACK;
                         :>

                """;
        Object result = PLCLangExec.runCode(packageName, source);
        showPixel((int) result);
        assertEquals(Color.BLACK.getRGB(), (int) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
public class PixelSum{
public static int apply(){
int p0$2=0xff0000ff;
int p1$2=0xff000000;
return (ImageOps.binaryPackedPixelPixelOp(ImageOps.OP.PLUS,p0$2,p1$2));
}

}
     */
    @Test
    void test9() throws Exception {
        String source = """
                pixel PixelSum() <:
                  pixel p0 = BLUE;
                  pixel p1 = BLACK;
                  ^ p0 + p1;
                  :>
                """;
        Object result = PLCLangExec.runCode(packageName, source);
        showPixel(Color.BLUE.getRGB());
        showPixel(Color.BLACK.getRGB());
        showPixel((int) result);
        assertEquals(Color.BLUE.getRGB(), (int) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import edu.ufl.cise.cop4020fa23.runtime.PixelOps;
public class test{
public static int apply(){
int p$2=PixelOps.pack(1,2,3);
return PixelOps.red(p$2);
}

}
     */
    @Test
    void test10() throws Exception {
        String source = """
                int test()<:
                           pixel p = [1,2,3];
                           ^p:red;
                         :>

                """;
        Object result = PLCLangExec.runCode(packageName, source);
        int p = PixelOps.pack(1, 2, 3);
        int expected = PixelOps.red(p);
        assertEquals(expected, (int) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import edu.ufl.cise.cop4020fa23.runtime.PixelOps;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
public class test{
public static int apply(){
int p$2=PixelOps.pack(1,2,3);
p$2=(ImageOps.binaryPackedPixelPixelOp(ImageOps.OP.PLUS,p$2,PixelOps.pack(1,2,3)));
return p$2;
}

}
     */
    @Test
    void test11() throws Exception {
        String source = """
                pixel test()<:
                           pixel p = [1,2,3];
                           p = p + [1,2,3];
                           ^p;
                         :>

                """;
        Object result = PLCLangExec.runCode(packageName, source);
        int p = PixelOps.pack(1, 2, 3);
        p = ImageOps.binaryPackedPixelPixelOp(ImageOps.OP.PLUS, p, p);
        assertEquals(p, (int) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import edu.ufl.cise.cop4020fa23.runtime.PixelOps;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
public class test{
public static int apply(){
int p$2=PixelOps.pack(1,2,3);
p$2=(ImageOps.binaryPackedPixelIntOp(ImageOps.OP.TIMES,p$2,3));
return p$2;
}

}
     */
    @Test
    void test12() throws Exception {
        String source = """
                pixel test()<:
                           pixel p = [1,2,3];
                           p = p * 3;
                           ^p;
                         :>

                """;
        Object result = PLCLangExec.runCode(packageName, source);
        int p = PixelOps.pack(1, 2, 3);
        p = ImageOps.binaryPackedPixelIntOp(ImageOps.OP.TIMES, p, 3);
        assertEquals(p, (int) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import edu.ufl.cise.cop4020fa23.runtime.PixelOps;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
public class test{
public static int apply(){
int p$2=PixelOps.pack(1,2,3);
p$2=(ImageOps.binaryPackedPixelIntOp(ImageOps.OP.DIV,p$2,3));
return p$2;
}

}
     */
    @Test
    void test13() throws Exception {
        String source = """
                pixel test()<:
                           pixel p = [1,2,3];
                           p = p / 3;
                           ^p;
                         :>

                """;
        Object result = PLCLangExec.runCode(packageName, source);
        int p = PixelOps.pack(1, 2, 3);
        p = ImageOps.binaryPackedPixelIntOp(ImageOps.OP.DIV, p, 3);
        assertEquals(p, (int) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import java.awt.image.BufferedImage;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;
public class test{
public static BufferedImage apply(String url$1){
BufferedImage i$2=FileURLIO.readImage(url$1);
ImageOps.copyInto((ImageOps.binaryImageImageOp(ImageOps.OP.PLUS,i$2,i$2)),i$2);
return i$2;
}

}
     */
    @Test
    void test14() throws Exception {
        String source = """
                image test(string url)<:
                           image i = url;
                           i = i + i;
                           ^i;
                         :>

                """;
        Object result = PLCLangExec.runCode(packageName, source, testURL);
        BufferedImage image = FileURLIO.readImage(testURL);
        ImageOps.copyInto(ImageOps.binaryImageImageOp(ImageOps.OP.PLUS, image, image), image);
        compareImages(image, (BufferedImage) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import java.awt.image.BufferedImage;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;
public class test{
public static BufferedImage apply(String url$1){
BufferedImage i$2=FileURLIO.readImage(url$1);
ImageOps.copyInto((ImageOps.binaryImageScalarOp(ImageOps.OP.TIMES,i$2,3)),i$2);
return i$2;
}

}
     */
    @Test
    void test15() throws Exception {
        String source = """
                image test(string url)<:
                           image i = url;
                           i = i * 3;
                           ^i;
                         :>

                """;
        Object result = PLCLangExec.runCode(packageName, source, testURL);
        BufferedImage image = FileURLIO.readImage(testURL);
        ImageOps.copyInto(ImageOps.binaryImageScalarOp(ImageOps.OP.TIMES, image, 3), image);
        compareImages(image, (BufferedImage) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import java.awt.image.BufferedImage;
import edu.ufl.cise.cop4020fa23.runtime.PixelOps;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
public class test{
public static BufferedImage apply(){
int w$2=50;
int h$2=50;
final BufferedImage a$2=ImageOps.makeImage(w$2,h$2);
ImageOps.setAllPixels(a$2,PixelOps.pack(255,0,0));
final BufferedImage b$2=ImageOps.makeImage(w$2,h$2);
ImageOps.setAllPixels(b$2,PixelOps.pack(0,255,0));
BufferedImage result$2=ImageOps.cloneImage((ImageOps.binaryImageImageOp(ImageOps.OP.PLUS,a$2,b$2)));
;
return result$2;
}

}
     */
    @Test
    void test16() throws Exception {
        String source = """
                image test()<:
                           int w = 50;
                           int h = 50;
                           image[w,h] a;
                           a = [Z,0,0];
                           image[w,h] b;
                           b = [0,Z,0];
                           image result = a + b;
                           ^ result;
                         :>

                """;
        Object result = PLCLangExec.runCode(packageName, source);
        int w = 50, h = 50;
        BufferedImage a = ImageOps.makeImage(w, h);
        a = ImageOps.setAllPixels(a, PixelOps.pack(255, 0, 0));
        BufferedImage b = ImageOps.makeImage(w, h);
        b = ImageOps.setAllPixels(b, PixelOps.pack(0, 255, 0));
        BufferedImage image = ImageOps.binaryImageImageOp(ImageOps.OP.PLUS, a, b);
        compareImages(image, (BufferedImage) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import java.awt.image.BufferedImage;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;
public class test{
public static BufferedImage apply(String url$1){
BufferedImage i$2=FileURLIO.readImage(url$1);
return i$2;
}

}
     */
    @Test
    void test17() throws Exception {
        String source = """
                image test(string url)<:
                           image i = url;
                           ^i;
                         :>

                """;
        Object result = PLCLangExec.runCode(packageName, source, owl);
        BufferedImage image = FileURLIO.readImage(owl);
        compareImages(image, (BufferedImage) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import java.awt.image.BufferedImage;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;
public class test{
public static BufferedImage apply(String url$1, int w$1, int h$1){
BufferedImage i$2=FileURLIO.readImage(url$1,w$1,h$1);
return i$2;
}

}
     */
    @Test
    void test18() throws Exception {
        String source = """
                image test(string url, int w, int h)<:
                           image[w,h] i = url;
                           ^i;
                         :>

                """;
        int w = 50;
        int h = 50;
        Object result = PLCLangExec.runCode(packageName, source, owl, w, h);
        BufferedImage image = FileURLIO.readImage(owl, w, h);
        compareImages(image, (BufferedImage) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import java.awt.image.BufferedImage;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;
public class test{
public static BufferedImage apply(String url$1, int w$1, int h$1){
BufferedImage j$2=FileURLIO.readImage(url$1);
BufferedImage i$2=ImageOps.copyAndResize(j$2,w$1,h$1);
;
return i$2;
}

}
     */
    @Test
    void test19() throws Exception {
        String source = """
                image test(string url, int w, int h)<:
                           image j = url;
                           image[w,h] i = j;
                           ^i;
                         :>

                """;
        int w = 50;
        int h = 50;
        Object result = PLCLangExec.runCode(packageName, source, owl, w, h);
        BufferedImage image = FileURLIO.readImage(owl, w, h);
        compareImages(image, (BufferedImage) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import edu.ufl.cise.cop4020fa23.runtime.PixelOps;
public class test{
public static int apply(int r$1, int g$1, int b$1){
int p$2=PixelOps.pack(r$1,g$1,b$1);
return p$2;
}

}
     */
    @Test
    void test20() throws Exception {
        String source = """
                pixel test(int r, int g, int b)<:
                           pixel p = [r,g,b];
                           ^p;
                         :>

                """;
        int r = 50;
        int g = 50;
        int b = 100;
        Object result = PLCLangExec.runCode(packageName, source, r, g, b);
        int pixel = PixelOps.pack(r, g, b);
        assertEquals(pixel, (int) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import java.awt.image.BufferedImage;
import edu.ufl.cise.cop4020fa23.runtime.PixelOps;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
public class test{
public static BufferedImage apply(){
final BufferedImage i$2=ImageOps.makeImage(50,50);
ImageOps.setAllPixels(i$2,PixelOps.pack(255,0,0));
return i$2;
}

}
     */
    @Test
    void test21() throws Exception {
        String source = """
                image test()<:
                           image[50,50] i;
                            i = [Z,0,0];
                           ^i;
                         :>

                """;
        Object result = PLCLangExec.runCode(packageName, source);
        BufferedImage image = ImageOps.makeImage(50, 50);
        image = ImageOps.setAllPixels(image, PixelOps.pack(255, 0, 0));
        compareImages(image, (BufferedImage) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import java.awt.image.BufferedImage;
import edu.ufl.cise.cop4020fa23.runtime.PixelOps;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
public class test{
public static BufferedImage apply(){
final BufferedImage i$2=ImageOps.makeImage(50,50);
ImageOps.setAllPixels(i$2,PixelOps.pack(0,255,0));
return i$2;
}

}
     */
    @Test
    void test22() throws Exception {
        String source = """
                image test()<:
                           image[50,50] i;
                            i = [0,Z,0];
                           ^i;
                         :>

                """;
        Object result = PLCLangExec.runCode(packageName, source);
        BufferedImage image = ImageOps.makeImage(50, 50);
        image = ImageOps.setAllPixels(image, PixelOps.pack(0, 255, 0));
        compareImages(image, (BufferedImage) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import java.awt.image.BufferedImage;
import edu.ufl.cise.cop4020fa23.runtime.PixelOps;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
public class test{
public static BufferedImage apply(){
final BufferedImage i$2=ImageOps.makeImage(50,50);
ImageOps.setAllPixels(i$2,PixelOps.pack(0,0,255));
return i$2;
}

}
     */
    @Test
    void test23() throws Exception {
        String source = """
                image test()<:
                           image[50,50] i;
                            i = [0,0,Z];
                           ^i;
                         :>

                """;
        Object result = PLCLangExec.runCode(packageName, source);
        BufferedImage image = ImageOps.makeImage(50, 50);
        image = ImageOps.setAllPixels(image, PixelOps.pack(0, 0, 255));
        compareImages(image, (BufferedImage) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import java.awt.image.BufferedImage;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;
public class test{
public static BufferedImage apply(String url$1){
BufferedImage i$2=FileURLIO.readImage(url$1,50,50);
final BufferedImage j$2=ImageOps.makeImage(50,50);
for (int x$3=0; x$3<j$2.getWidth();x$3++){
for (int y$3=0; y$3<j$2.getHeight();y$3++){
ImageOps.setRGB(j$2,x$3,y$3,ImageOps.getRGB(i$2,y$3,x$3));
}
};
return j$2;
}

}
     */
    @Test
    void test24() throws Exception {
        String source = """
                image test(string url)<:
                           image[50,50] i = url;
                           image[50,50] j;
                           j[x,y] = i[y,x];
                           ^j;
                         :>

                """;
        Object result = PLCLangExec.runCode(packageName, source, owl);
        BufferedImage image = FileURLIO.readImage(owl, 50, 50);
        BufferedImage expected = ImageOps.makeImage(50, 50);
        for (int y = 0; y < 50; y++)
            for (int x = 0; x < 50; x++)
                expected.setRGB(x, y, image.getRGB(y, x));
        compareImages(expected, (BufferedImage) result);
    }

    /*
package edu.ufl.cise.cop4020fa23;
import java.awt.image.BufferedImage;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;
public class test{
public static BufferedImage apply(String url$1, int k$1, int l$1){
BufferedImage i$2=FileURLIO.readImage(url$1,50,50);
final BufferedImage j$2=ImageOps.makeImage(50,50);
ImageOps.setRGB(j$2,k$1,l$1,ImageOps.getRGB(i$2,l$1,k$1));;
return j$2;
}

}
     */
    @Test
    void test25() throws Exception {
        String source = """
                image test(string url, int k, int l)<:
                           image[50,50] i = url;
                           image[50,50] j;
                           j[k,l] = i[l,k];
                           ^j;
                         :>

                """;
        int k = 10, l = 20;
        Object result = PLCLangExec.runCode(packageName, source, owl, k, l);
        BufferedImage image = FileURLIO.readImage(owl, 50, 50);
        BufferedImage expected = ImageOps.makeImage(50, 50);
        expected.setRGB(k, l, image.getRGB(l, k));
        compareImages(expected, (BufferedImage) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import edu.ufl.cise.cop4020fa23.runtime.PixelOps;
import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO;
public class test{
public static void apply(int r$1, int g$1, int b$1){
int p$2=PixelOps.pack(r$1,g$1,b$1);
ConsoleIO.writePixel(p$2);
}

}
     */
    @Test
    void test27() throws Exception {
        String source = """
                void test(int r, int g, int b)<:
                           pixel p = [r,g,b];
                           write p;
                         :>

                """;
        int r = 10, g = 10, b = 10;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream test = new PrintStream(baos);
        ConsoleIO.setConsole(test);
        Object result = PLCLangExec.runCode(packageName, source, r, g, b);
        int p = PixelOps.pack(r, g, b);
        String pixel = Integer.toHexString(p);
        String output = baos.toString();
        assertTrue(output.equals(pixel + "\n") || output.equals(pixel + "\r\n"));
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import java.awt.image.BufferedImage;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;
public class test{
public static int apply(String url$1){
BufferedImage i$2=FileURLIO.readImage(url$1);
return (i$2.getWidth());
}

}
     */
    @Test
    void test28() throws Exception {
        String source = """
                int test(string url)<:
                           image i = url;
                           ^ width i;
                         :>

                """;
        int width = FileURLIO.readImage(owl).getWidth();
        Object result = PLCLangExec.runCode(packageName, source, owl);
        assertEquals(width, (int) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import java.awt.image.BufferedImage;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;
public class test{
public static int apply(String url$1){
BufferedImage i$2=FileURLIO.readImage(url$1);
return (i$2.getHeight());
}

}
     */
    @Test
    void test29() throws Exception {
        String source = """
                int test(string url)<:
                           image i = url;
                           ^ height i;
                         :>

                """;
        int height = FileURLIO.readImage(owl).getHeight();
        Object result = PLCLangExec.runCode(packageName, source, owl);
        assertEquals(height, (int) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import java.awt.image.BufferedImage;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
public class test{
public static BufferedImage apply(int w$1, int h$1){
final BufferedImage i$2=ImageOps.makeImage(w$1,h$1);
for (int x$3=0; x$3<i$2.getWidth();x$3++){
for (int y$3=0; y$3<i$2.getHeight();y$3++){
ImageOps.setRGB(i$2,x$3,y$3,0xff00ffff);
}
};
return i$2;
}

}
     */
    @Test
    void test30() throws Exception {
        String source = """
                image test(int w, int h)<:
                           image[w,h] i;
                           i[x,y] = CYAN;
                           ^i;
                         :>

                """;
        int w = 10, h = 10;
        Object result = PLCLangExec.runCode(packageName, source, w, h);
        BufferedImage image = ImageOps.makeImage(w, h);
        image = ImageOps.setAllPixels(image, Color.CYAN.getRGB());
        compareImages(image, (BufferedImage) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import edu.ufl.cise.cop4020fa23.runtime.PixelOps;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
public class test{
public static boolean apply(){
int p$2=PixelOps.pack(0,255,0);
int i$2=PixelOps.pack(0,255,0);
return (ImageOps.binaryPackedPixelBooleanOp(ImageOps.BoolOP.EQUALS,i$2,p$2));
}

}
     */
    @Test
    void test31() throws Exception {
        String source = """
                boolean test()<:
                           pixel p = [0,Z,0];
                           pixel i = [0,Z,0];
                           ^ i == p;
                         :>

                """;
        Object result = PLCLangExec.runCode(packageName, source);
        assertTrue((boolean) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import java.awt.image.BufferedImage;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;
public class test{
public static int apply(String url$1, int k$1, int l$1){
BufferedImage i$2=FileURLIO.readImage(url$1);
int p$2=ImageOps.getRGB(i$2,k$1,l$1);
return p$2;
}

}
     */
    @Test
    void test32() throws Exception {
        String source = """
                pixel test(string url, int k, int l)<:
                           image i = url;
                           pixel p = i[k,l];
                           ^ p;
                         :>

                """;
        int k = 5, l = 20;
        Object result = PLCLangExec.runCode(packageName, source, testURL, k, l);
        BufferedImage image = FileURLIO.readImage(testURL);
        int pixel = image.getRGB(k, l);
        assertEquals(pixel, (int) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import java.awt.image.BufferedImage;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;
public class test{
public static BufferedImage apply(String owl$1, String test$1, int w$1, int h$1){
BufferedImage i$2=FileURLIO.readImage(owl$1,w$1,h$1);
BufferedImage j$2=FileURLIO.readImage(test$1,w$1,h$1);
final BufferedImage average$2=ImageOps.makeImage(w$1,h$1);
ImageOps.copyInto((ImageOps.binaryImageScalarOp(ImageOps.OP.DIV,(ImageOps.binaryImageImageOp(ImageOps.OP.PLUS,i$2,j$2)),2)),average$2);
return average$2;
}

}
     */
    @Test
    void test33() throws Exception {
        String source = """
                image test(string owl, string test, int w, int h)<:
                           image[w,h] i = owl;
                           image[w,h] j = test;
                           image[w,h] average;
                           average = (i + j) / 2;
                           ^ average;
                         :>

                """;
        int w = 51, h = 50;
        BufferedImage i = FileURLIO.readImage(owl, w, h);
        BufferedImage j = FileURLIO.readImage(owl, w, h);
        Object result = PLCLangExec.runCode(packageName, source, owl, owl, w, h);
        BufferedImage expected = ImageOps.binaryImageScalarOp(ImageOps.OP.DIV, ImageOps.binaryImageImageOp(ImageOps.OP.PLUS, i, j), 2);
        compareImages(expected, (BufferedImage) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import java.awt.image.BufferedImage;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;
public class test{
public static BufferedImage apply(String t$1){
final BufferedImage i$2=ImageOps.makeImage(100,100);
ImageOps.copyInto(FileURLIO.readImage(t$1),i$2);
return i$2;
}

}
     */
    @Test
    void test34() throws Exception {
        String source = """
                image test(string t)<:
                           image[100, 100] i;
                           i = t;
                           ^ i;
                         :>

                """;
        BufferedImage expected = FileURLIO.readImage(testURL, 100, 100);
        Object result = PLCLangExec.runCode(packageName, source, testURL);
        compareImages(expected, (BufferedImage) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import java.awt.image.BufferedImage;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
public class test{
public static BufferedImage apply(int a$1, int b$1){
final BufferedImage i$2=ImageOps.makeImage(100,100);
for (int x$3=0; x$3<i$2.getWidth();x$3++){
for (int y$3=0; y$3<i$2.getHeight();y$3++){
ImageOps.setRGB(i$2,x$3,y$3,((int)Math.round(Math.pow(a$1,b$1))));
}
};
return i$2;
}

}
     */
    @Test
    void test35() throws Exception {
        String source = """
                image test(int a, int b)<:
                           image[100, 100] i;
                           i[x,y] = a ** b;
                           ^ i;
                         :>

                """;
        int a = 2, b = 3;
        BufferedImage expected = ImageOps.makeImage(100, 100);
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                expected.setRGB(i, j, (int) Math.round(Math.pow(a, b)));
            }
        }
        Object result = PLCLangExec.runCode(packageName, source, a, b);
        compareImages(expected, (BufferedImage) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
public class test{
public static boolean apply(int a$1, int b$1){
if(a$1>b$1){
return true;
}
else if(a$1<=b$1){
return false;
}
;
return false;
}

}
     */
    @Test
    void test36() throws Exception {
        String source = """
                boolean test(int a, int b)<:
                           if a > b -> <: ^TRUE; :>
                           [] a <= b -> <: ^FALSE; :>
                           fi;
                           ^FALSE;
                         :>

                """;
        int a = 2, b = 3;
        Object result = PLCLangExec.runCode(packageName, source, a, b);
        assertEquals(false, (boolean) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
public class test{
public static boolean apply(int a$1, int b$1){
if(a$1>b$1){
return true;
}
else if(a$1<=b$1){
return false;
}
;
return false;
}

}
     */
    @Test
    void test37() throws Exception {
        String source = """
                boolean test(int a, int b)<:
                           if a > b -> <: ^TRUE; :>
                           [] a <= b -> <: ^FALSE; :>
                           fi;
                           ^FALSE;
                         :>

                """;
        int a = 3, b = 2;
        Object result = PLCLangExec.runCode(packageName, source, a, b);
        assertEquals(true, (boolean) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
public class test{
public static int apply(int a$1, int b$1){
if(a$1>b$1){
return 0;
}
else if(a$1<b$1){
return 1;
}
else if(a$1==b$1){
return 2;
}
;
return 5;
}

}
     */
    @Test
    void test38() throws Exception {
        String source = """
                int test(int a, int b)<:
                           if a > b -> <: ^0; :>
                           [] a < b -> <: ^1; :>
                           [] a == b -> <: ^2; :>
                           fi;
                           ^5;
                         :>

                """;
        int a = 3, b = 3;
        Object result = PLCLangExec.runCode(packageName, source, a, b);
        assertEquals(2, (int) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
public class test{
public static int apply(int a$1){
{boolean continue$0= false;
while(!continue$0){
continue$0=true;
if(a$1>0){
continue$0 = false;
{
a$1=(a$1-1);
}
}
if(a$1<=0){
continue$0 = false;
{
return a$1;
}
}
}
};
return (-1);
}

}
     */
    @Test
    void test39() throws Exception {
        String source = """
                int test(int a)<:
                do a > 0 -> <: a = a - 1; :>
                [] a <= 0 -> <: ^a; :>
                od;
                ^ -1;
                :>

                """;
        int a = 3;
        Object result = PLCLangExec.runCode(packageName, source, a);
        assertEquals(0, (int) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
public class test{
public static int apply(int a$1){
{boolean continue$0= false;
while(!continue$0){
continue$0=true;
if(a$1>1){
continue$0 = false;
{
a$1=(a$1-1);
}
}
}
};
return a$1;
}

}
     */
    @Test
    void test40() throws Exception {
        String source = """
                int test(int a)<:
                do a > 1 -> <: a = a - 1; :>
                od;
                ^ a;
                :>

                """;
        int a = 3;
        Object result = PLCLangExec.runCode(packageName, source, a);
        assertEquals(1, (int) result);
    }

    /*
    package edu.ufl.cise.cop4020fa23;
import java.awt.image.BufferedImage;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;
public class test{
public static BufferedImage apply(String url$1){
BufferedImage i$2=FileURLIO.readImage(url$1);
for (int x$3=0; x$3<i$2.getWidth();x$3++){
for (int y$3=0; y$3<i$2.getHeight();y$3++){
ImageOps.setRGB(i$2,x$3,y$3,(x$3>y$3?ImageOps.getRGB(i$2,x$3,y$3):0xff000000));
}
};
return i$2;
}

}
     */
    @Test
    void test41() throws Exception {
        String source = """
                image test(string url)<:
                image i = url;
                i[x,y] = ? x > y -> i[x,y], BLACK;
                ^i;
                :>

                """;
        Object result = PLCLangExec.runCode(packageName, source, testURL);
        BufferedImage image = FileURLIO.readImage(testURL);
        BufferedImage expected = ImageOps.makeImage(image.getWidth(), image.getHeight());
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                expected.setRGB(i, j, i > j ? image.getRGB(i, j) : Color.BLACK.getRGB());
            }
        }
        compareImages(expected, (BufferedImage) result);
    }

    @Test
    void cg0() throws Exception {
        String input = "void test()<: int a = 2;:>";
        Object result = PLCLangExec.runCode(packageName, input);
        show(result);
        assertNull(result);
    }

    @Test
    void cg1() throws Exception {
        String input = """
                string test()<: ^ "hello";  :>
                """;
        Object result = PLCLangExec.runCode(packageName, input);
        assertEquals("hello", (String) result);
    }


    @Test
    void cg2() throws Exception {
        String input = """
                boolean test(boolean true) ##false is an identifier
                <: ^ true; 
                :>
                """;
        Object result = PLCLangExec.runCode(packageName, input, true);
        assertEquals(true, (boolean) result);
    }

    @Test
    void cg3() throws Exception {
        String input = """
                boolean test(boolean true)
                <: ^ true; 
                :>
                """;
        Object result = PLCLangExec.runCode(packageName, input, false);
        assertEquals(false, (boolean) result);
    }


    @Test
    void cg4() throws Exception {
        String input = """
                int test(int a, string Hello, boolean b)
                <: 
                write a;
                write Hello;
                write b;
                ^ a;
                :>
                """;
        Object[] params = {4, "hello", true};
        Object result = PLCLangExec.runCode(packageName, input, 4, "hello", true);
        show(result);
        assertEquals(4, result);
    }


    @Test
    void cg5() throws Exception {
        String input = """
                int test(int b)
                <: 
                write b;
                ^b+3;
                :>
                """;
        Object result = PLCLangExec.runCode(packageName, input, 7);
        assertEquals(10, (int) result);
    }

    @Test
    void cg6() throws Exception {
        String input = """
                int test(int one, int two)
                <:
                ^ two ** one;
                :>
                """;
        Object result = PLCLangExec.runCode(packageName, input, 3, 2);
        show(result);
        assertEquals(8, (int) result);
    }

    @Test
    void cg7() throws Exception {
        String input = """
                string test(string x, string y)
                <: 
                ^x+y;
                :>
                """;
        Object result = PLCLangExec.runCode(packageName, input, "4", "5");
        show(result);
        assertEquals("45", result);

    }

    @Test
    void cg8() throws Exception {
        String source = """
                int test(int b)
                <:
                ^ -b;
                :>
                """;
        Object result = PLCLangExec.runCode(packageName, source, -20);
        show(result);
        assertEquals(20, (int) result);
    }

    @Test
    void cg9() throws Exception {
        String source = """
                int f(int a)
                <:
                ^ -(a+10);
                :>
                """;
        Object result = PLCLangExec.runCode(packageName, source, -10);
        show(result);
        assertEquals(0, (int) result);
    }

    @Test
    void cg10() throws Exception {
        String source = """
                int test(int b)
                <:
                ^ -(-b-10);
                :>
                """;
        Object result = PLCLangExec.runCode(packageName, source, 10);
        show(result);
        assertEquals(20, (int) result);
    }

    @Test
    void cg11() throws Exception {
        String source = """
                boolean test(boolean b)
                <:
                ^ !b;
                :>
                """;
        Object result = PLCLangExec.runCode(packageName, source, false);
        show(result);
        assertEquals(true, (boolean) result);
    }

    @Test
    void cg12() throws Exception {
        String source = """
                boolean test(boolean b)
                <:
                ^ !!b;
                :>
                """;
        Object result = PLCLangExec.runCode(packageName, source, false);
        show(result);
        assertEquals(false, (boolean) result);
    }

    @Test
    void cg13() throws Exception {
        String source = """
                int test(int i)
                <:
                  int r = ? i>0 -> i , -i;
                  ^r;
                  :>
                  """;
        Object result = PLCLangExec.runCode(packageName, source, -45);
        show(result);
        assertEquals(45, (int) result);
    }

    @Test
    void cg14() throws Exception {
        String source = """
                string a(int i)
                <:
                  string r = ? i>0 -> "positive" , "negative";
                  ^r;
                  :>
                  """;
        Object result = PLCLangExec.runCode(packageName, source, -42);
        show(result);
        assertEquals("negative", result);
    }

    @Test
    void cg15() throws Exception {
        String source = """
                int test(int i)
                <:
                int j;
                j = i + 5;
                i = j;
                ^i;
                :>
                """;
        int val = 34;
        Object result = PLCLangExec.runCode(packageName, source, val);
        show(result);
        assertEquals(39, (int) result);
    }

    @Test
    void cg16() throws Exception {
        String source = """
                boolean test(boolean a)
                <:
                boolean b;
                b = !a;
                ^b;
                :>
                """;
        Object result = PLCLangExec.runCode(packageName, source, true);
        show(result);
        assertEquals(false, result);
    }

    @Test
    void cg17() throws Exception {
        String source = """
                boolean f()
                <:
                boolean b = TRUE;
                b = !b;
                ^b;
                :>
                """;
        Object result = PLCLangExec.runCode(packageName, source);
        show(result);
        assertEquals(false, (boolean) result);
    }

    @Test
    void cg18() throws Exception {
        String source = """
                int test()
                <:
                  int i = 1;
                  int j;
                  <: 
                     int i = 2;
                     <: 
                         int i = 3;
                         j=i;
                     :>;
                     j = i;
                  :>;
                  j = i;
                  ^j;
                :>

                """;
        Object result = PLCLangExec.runCode(packageName, source);
        show(result);
        assertEquals(1, (int) result);
    }

    @Test
    void cg19() throws Exception {
        String source = """
                int f()
                <:
                  int i = 1;
                  int j;
                  <: 
                     int i = 2;
                     <: 
                         i = 3;
                        
                     :>;
                      j=i;
                  :>;
                  ^j;
                :>

                """;
        Object result = PLCLangExec.runCode(packageName, source);
        show(result);
        assertEquals(3, (int) result);
    }

    @Test
    void cg20() throws Exception {
        String source = """
                int f()
                <:
                  int i = 1;
                  int j;
                  <: 
                     int i = 2;
                     <: 
                         int i = 3 * i;
                         j = i;
                                :>;
                                j = i * j;			      
                  :>;
                  ^j;
                :>

                """;
        Object result = PLCLangExec.runCode(packageName, source);
        show(result);
        assertEquals(12, (int) result);
    }

    @Test
    void cg21() throws Exception {
        String source = """
                string concatWithSpace(string a, string b)
                <:
                ^ a + " " + b;
                :>
                """;
        String a = "Go";
        String b = "Gators!";
        Object result = PLCLangExec.runCode(packageName, source, a, b);
        show(result);
        assertEquals(a + " " + b, result);
    }

    @Test
    void cg22() throws Exception {
        String source = """
                void output() <:
                write "hello";
                :>
                """;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream test = new PrintStream(baos);
        ConsoleIO.setConsole(test);
        Object result = PLCLangExec.runCode(packageName, source);
        show(result);
        String output = baos.toString();
        assertEquals(null, result);
        assertTrue(output.equals("hello\n") || output.equals("hello\r\n"));
    }

    @Test
    void cg23() throws Exception {
        String source = """
                void output() <:
                write 2;
                :>
                """;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream test = new PrintStream(baos);
        ConsoleIO.setConsole(test);
        Object result = PLCLangExec.runCode(packageName, source);
        show(result);
        String output = baos.toString();
        assertEquals(null, result);
        assertTrue(output.equals("2\n") || output.equals("2\r\n"));
    }

    @Test
    void cg24() throws Exception {
        String source = """
                void output(string a, string b) <:
                string c = a + b;
                write c;
                :>
                """;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream test = new PrintStream(baos);
        ConsoleIO.setConsole(test);
        Object result = PLCLangExec.runCode(packageName, source, "hello ", "world");
        show(result);
        String output = baos.toString();
        assertEquals(null, result);
        assertTrue(output.equals("hello world\n") || output.equals("hello world\r\n"));
    }

    @Test
    void cg25() throws Exception {
        String source = """
                boolean test(boolean a, boolean b) <:
                boolean c = a || b;
                ^ c;
                :>
                """;
        Object result = PLCLangExec.runCode(packageName, source, true, false);
        show(result);
        assertEquals(true, result);
    }

    @Test
    void cg26() throws Exception {
        String source = """
                boolean test(string a, string b) <:
                boolean c = a == b;
                ^ c;
                :>
                """;
        Object result = PLCLangExec.runCode(packageName, source, "hello", "hello");
        show(result);
        assertEquals(true, result);
    }

    @Test
    void cg27() throws Exception {
        String source = """
                boolean test(int a, int b) <:
                boolean c = a < b;
                ^ c;
                :>
                """;
        Object result = PLCLangExec.runCode(packageName, source, 5, 6);
        show(result);
        assertEquals(true, result);
    }

    @Test
    void cg28() throws Exception {
        String source = """
                boolean test(int a, int b) <:
                boolean c = a <= b;
                ^ c;
                :>
                """;
        Object result = PLCLangExec.runCode(packageName, source, 5, 5);
        show(result);
        assertEquals(true, result);
    }

    @Test
    void cg29() throws Exception {
        String source = """
                boolean test(int a, int b) <:
                boolean c = a == b;
                ^ c;
                :>
                """;
        Object result = PLCLangExec.runCode(packageName, source, 5, 5);
        show(result);
        assertEquals(true, result);
    }

    void checkNumLit(String numText, IToken t) {
        assertEquals(NUM_LIT, t.kind());
        assertEquals(numText, t.text());
    }

    @Test
    void hw0_1() throws LexicalException {
        String input = """
                23 9999999999999999999999999999999999999999
                """;
        ILexer lexer = ComponentFactory.makeLexer(input);
        checkNumLit("23", lexer.next());
        assertThrows(LexicalException.class, () -> {
            lexer.next();
        });
    }

    @Test
    void hw0_2() throws Exception {
        String input = """
                555 #
                """;
        ILexer lexer = ComponentFactory.makeLexer(input);
        checkNumLit("555", lexer.next());
        LexicalException e = assertThrows(LexicalException.class, () -> {
            lexer.next();
        });
        show(e.getMessage());
    }

    @Test
    void hw0_3() throws Exception {
        String input = """
                555 @
                """;
        ILexer lexer = ComponentFactory.makeLexer(input);
        checkNumLit("555", lexer.next());
        LexicalException e = assertThrows(LexicalException.class, () -> {
            lexer.next();
        });
        show(e.getMessage());
    }

    void checkString(String stringValue, IToken t) {
        assertEquals(STRING_LIT, t.kind());
        String s = t.text();
        assertEquals('\"', s.charAt(0));  //check that first char is "
        assertEquals('\"', s.charAt(s.length() - 1));
        assertEquals(stringValue, s.substring(1, s.length() - 1));
    }

    @Test
    void hw0_4() throws Exception {
        String input = """
                "@"
                ## @ is legal in a comment
                @
                """;
        ILexer lexer = ComponentFactory.makeLexer(input);
        checkString("@", lexer.next());
        LexicalException e = assertThrows(LexicalException.class, () -> {
            lexer.next();
        });
        show("Error message from test19: " + e.getMessage());
    }

    @Test
    void hw0_5() throws Exception {
        String input = "\"";
        ILexer lexer = ComponentFactory.makeLexer(input);
        LexicalException e = assertThrows(LexicalException.class, () -> {
            lexer.next();
        });
        show("Error message from testNotClosedString: " + e.getMessage());
    }

    static final int TIMEOUT_MILLIS = 1000;

    AST getAST(String input) throws PLCCompilerException {
        return ComponentFactory.makeParser(input).parse();
    }

    @Test
    void hw2_1() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
                    String input = """
                            int s() <:
                            int a = 2:GRAY;
                            :>
                            """;
                    assertThrows(SyntaxException.class, () -> {
                        @SuppressWarnings("unused")
                        AST ast = getAST(input);
                    });
                }
        );
    }

    @Test
    void hw2_2() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
                    String input = """
                            int s() <:
                            ^;
                            :>
                            """;
                    assertThrows(SyntaxException.class, () -> {
                        @SuppressWarnings("unused")
                        AST ast = getAST(input);
                    });
                }
        );
    }

    @Test
    void hw2_3() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
                    String input = """
                            int s() <:
                            do a -> <: b; :> [] od;
                            """;
                    assertThrows(SyntaxException.class, () -> {
                        @SuppressWarnings("unused")
                        AST ast = getAST(input);
                    });
                }
        );
    }

    @Test
    void hw2_4() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
                    String input = """
                            int s() <:
                            do a -><:;
                            :>
                            """;
                    assertThrows(SyntaxException.class, () -> {
                        @SuppressWarnings("unused")
                        AST ast = getAST(input);
                    });
                }
        );
    }

    @Test
    void hw2_5() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
                    String input = """
                            int s(int a; int b) <: :>
                            """;
                    assertThrows(SyntaxException.class, () -> {
                        @SuppressWarnings("unused")
                        AST ast = getAST(input);
                    });
                }
        );
    }

    @Test
    void hw2_6() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
                    String input = """
                            int s() <:
                            x =
                            :>
                            """;
                    assertThrows(SyntaxException.class, () -> {
                        @SuppressWarnings("unused")
                        AST ast = getAST(input);
                    });
                }
        );
    }

    @Test
    void hw2_7() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
                    String input = """
                            int s(int a = 2) <: :>
                            """;
                    assertThrows(SyntaxException.class, () -> {
                        @SuppressWarnings("unused")
                        AST ast = getAST(input);
                    });
                }
        );
    }

    @Test
    void hw2_8() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
                    String input = """
                            int s() <:
                            """;
                    assertThrows(SyntaxException.class, () -> {
                        @SuppressWarnings("unused")
                        AST ast = getAST(input);
                    });
                }
        );
    }

    @Test
    void hw2_9() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
                    String input = """
                            int
                            """;
                    assertThrows(SyntaxException.class, () -> {
                        @SuppressWarnings("unused")
                        AST ast = getAST(input);
                    });
                }
        );
    }

    @Test
    void hw2_10() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
                    String input = """
                            pixel ppp() <:
                            a = 3;
                            a[x,y] = 4;
                            a[x,y]:red = 5;
                            a:green = 5;
                            :>
                            trailing_stuff
                            """;
                    assertThrows(SyntaxException.class, () -> {
                        @SuppressWarnings("unused")
                        AST ast = getAST(input);
                    });
                }
        );
    }

    AST getASTType(String input) throws PLCCompilerException {
        AST ast = ComponentFactory.makeParser(input).parse();
        ASTVisitor typeChecker = ComponentFactory.makeTypeChecker();
        ast.visit(typeChecker, null);
        return ast;
    }

    @Test
    void hw3_1() throws PLCCompilerException {
        String input = """
                string x(string f,int f)<::>
                """;
        assertThrows(TypeCheckException.class, () -> {
            @SuppressWarnings("unused")
            AST ast = getASTType(input);
        });
    }

    @Test
    void hw3_2() throws PLCCompilerException {
        String input = """
                void f() <:
                  int x = y + 5;
                :>
                """;
        assertThrows(TypeCheckException.class, () -> {
            @SuppressWarnings("unused")
            AST ast = getASTType(input);
        });
    }

    @Test
    void hw3_3() throws PLCCompilerException {
        String input = """
                void s(void ff)<::>
                """;
        assertThrows(TypeCheckException.class, () -> {
            @SuppressWarnings("unused")
            AST ast = getASTType(input);
        });
    }

    @Test
    void hw3_4() throws PLCCompilerException {
        String input = """
                void f()<:
                  int aa = 2;
                  string zz = "hello";
                  image[100,100] xx = "url";
                  image[200,200] xxx = xx;
                  ^ii;
                  :>
                """;
        assertThrows(TypeCheckException.class, () -> {
            @SuppressWarnings("unused")
            AST ast = getASTType(input);
        });
    }

    @Test
    void hw3_5() throws PLCCompilerException {
        String input = """
                void x()<:
                  int ss = 2+ss;
                  :>
                """;
        assertThrows(TypeCheckException.class, () -> {
            @SuppressWarnings("unused")
            AST ast = getASTType(input);
        });
    }

    @Test
    void hw3_6() throws PLCCompilerException {
        String input = """
                string f(string a, string b, boolean c)<:
                ^ ? c -> c+1 , a + b ;
                :>
                """;
        assertThrows(TypeCheckException.class, () -> {
            @SuppressWarnings("unused")
            AST ast = getASTType(input);
        });
    }

    @Test
    void hw3_7() throws PLCCompilerException {
        String input = """
                int f() <:
                int j = 3;
                do j > 0 -> <:
                   string str = "World";
                   write str;
                   j = j - 1;
                :>
                od;
                j = 3;
                do j > 0 -> <:
                   write missingVar;
                   j = j - 1;
                :>
                od;
                ^ j;
                :>
                """;
        assertThrows(TypeCheckException.class, () -> {
            @SuppressWarnings("unused")
            AST ast = getASTType(input);
        });
    }

    @Test
    void hw3_8() throws PLCCompilerException {
        String input = """
                image generateImage() <:
                   <:
                   string url;
                   :>;
                   image img = url;
                   ^img;
                   :>
                """;
        assertThrows(TypeCheckException.class, () -> {
            @SuppressWarnings("unused")
            AST ast = getASTType(input);
        });
    }

    @Test
    void hw3_9() throws PLCCompilerException {
        String input = """
                void myFunction() <:
                  int alpha = 1;
                  int alpha = 2;
                :>
                """;
        assertThrows(TypeCheckException.class, () -> {
            @SuppressWarnings("unused")
            AST ast = getASTType(input);
        });
    }

    @Test
    void hw3_10() throws PLCCompilerException {
        String input = """
                void f() <:
                  int y = "hello world";
                :>
                """;
        assertThrows(TypeCheckException.class, () -> {
            @SuppressWarnings("unused")
            AST ast = getASTType(input);
        });
    }

    @Test
    void hw3_11() throws PLCCompilerException {
        String input = """
                void f() <:
                  int a = 1;
                  string b = "Basic Code";
                  int c = a + b;
                :>
                """;
        assertThrows(TypeCheckException.class, () -> {
            @SuppressWarnings("unused")
            AST ast = getASTType(input);
        });
    }

    @Test
    void hw3_12() throws PLCCompilerException {
        String input = """
                int f() <:
                  ^"string";
                :>
                """;
        assertThrows(TypeCheckException.class, () -> {
            @SuppressWarnings("unused")
            AST ast = getASTType(input);
        });
    }

    @Test
    void hw3_13() throws PLCCompilerException {
        String input = """
                boolean f() <:
                  int x = !5;
                  ^x;
                :>
                """;
        assertThrows(TypeCheckException.class, () -> {
            @SuppressWarnings("unused")
            AST ast = getASTType(input);
        });
    }

    @Test
    void hw3_14() throws PLCCompilerException {
        String input = """
                int myFunction() <:
                  ^y;
                :>
                """;
        assertThrows(TypeCheckException.class, () -> {
            @SuppressWarnings("unused")
            AST ast = getASTType(input);
        });
    }


}
