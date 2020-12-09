package javalearn.compress;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.*;

public class ZipTest {
    public static void main(String[] args) throws Exception{
        writeZipFile();
        readZipFile();
    }
    public static void writeZipFile()throws Exception{
        FileOutputStream outputStream = new FileOutputStream("d:\\test.zip");
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(outputStream), Charset.forName("UTF-8"));
        ZipEntry zipEntry = new ZipEntry("a.txt");
        byte[] dataa = "this is a.txt".getBytes();
        zipEntry.setSize(dataa.length);
        out.putNextEntry(zipEntry);
        out.write(dataa);

        ZipEntry zipEntryb = new ZipEntry("b.txt");
        out.putNextEntry(zipEntryb);
        byte[] datab = "this is b.txt".getBytes();
        zipEntry.setSize(datab.length);
        out.write(datab);
        out.close();
        outputStream.close();
        Deflater def = new Deflater();
        byte[] output = new byte[100];
        def.setInput(dataa);
        def.finish();
        int compressedDataLength = def.deflate(output);
        def.end();
        System.out.println(compressedDataLength);

        System.out.println(ZipTest.class.getClassLoader().getResource(""));
        System.out.println(System.getProperty("user.dir"));
    }
    public static void readZipFile()throws Exception{
        String path = "d:\\test.zip";
        InputStream in = new BufferedInputStream(new FileInputStream(path));
        ZipInputStream zin = new ZipInputStream(in,Charset.forName("UTF-8"));
        ZipFile zf = new ZipFile(path);
        System.out.println("zipFile size:" + new File(path).length());
        Enumeration<? extends ZipEntry> entries = zf.entries();
        ZipEntry ze;
        while(entries.hasMoreElements()){
            ze = entries.nextElement();
            if(ze.toString().endsWith("txt")){
                System.out.println("读取到文件:"+ze.getName() + " , size = " + ze.getCompressedSize());
                InputStream inputStream = zf.getInputStream(ze);
                byte[] data = IOUtils.toByteArray(inputStream,ze.getSize());
                String line = new String(data,Charset.forName("UTF-8"));
                System.out.println(line);
                inputStream.close();
            }
            System.out.println();
        }
        zin.closeEntry();

    }
}
