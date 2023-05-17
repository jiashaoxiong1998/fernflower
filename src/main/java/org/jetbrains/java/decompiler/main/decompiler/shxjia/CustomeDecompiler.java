package org.jetbrains.java.decompiler.main.decompiler.shxjia;

import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CustomeDecompiler {
    private static String srcPath;


    private static boolean nameFlag;

    private static String[] flagArgs;

    static PrintStreamLogger logger = new PrintStreamLogger(System.out);

    public static void main(String[] args) throws IOException {

        args = new String[]{"/Users/cafebabe/shxjia/data/qaxAudit/tac/tac.compile/tac.zip",
                "com/qianxin", "com/qax"};
        if (args.length > 1) {
            srcPath = args[0];
            nameFlag = true;
            flagArgs = args;
            System.out.println("根据白名单路径反编译");
        } else if (args.length == 1) {
            srcPath = args[0];
            nameFlag = false;
            System.out.println("全量源码反编译");
        } else {
            System.out.println("没有输入反编译文件路径");
            System.out.println("程序用法:java-jar fernflower.jar /tmp/web.war org/apace ");
            System.exit(-1);

        }
        Fernflower decompiler = init();
        System.out.println("开始读取:" + srcPath);
        List<File> allSource = getAllSource(srcPath);
        System.out.println("加载所有源码文件完成,共找到:" + allSource.size());
        for (File f : allSource) {
            decompiler.addSource(f);
        }
        decompiler.decompileContext();
    }


    public static Fernflower init() {
        Map<String, Object> mapOptions = new HashMap<>();

        //重命名模棱两可（重报）类和类元素
        mapOptions.put("ren", "1");

        //反编译方法超时时间
        mapOptions.put("mpm", "1");

        //将lambda表达式反编译为匿名类
        mapOptions.put("lac", "0");


        return new Fernflower(new CustomeBytecodeProvider(), new CustomeResultSaver(), mapOptions, logger);

    }

    public static List<File> getAllSource(String srcPath) throws IOException {
        HashSet<File> sources = new HashSet<>();
        File src = new File(srcPath);
        if (src.isDirectory()) {
            //传递的是一个文件夹时
            getDirSource(sources, src);
        }
        if (src.isFile()) {
            //传递的是一个文件时
            getFileSource(sources, src);
        }
        return new ArrayList<>(sources);
    }

    public static void getDirSource(Set<File> sources, File src) throws IOException {
        Stream<Path> walk = Files.walk(src.toPath());
        List<Path> collect = walk.collect(Collectors.toList());
        for (Path p : collect) {
            if (Files.isRegularFile(p)) {
                File tmpFile = p.toFile();
                getFileSource(sources, tmpFile);
            }
        }
    }

    public static void getFileSource(Set<File> sources, File file) throws IOException {
        String filePath = file.getAbsolutePath();
        if (filePath.endsWith(".class")) {
            if (nameFlag) {
                for (int a = 1; a < flagArgs.length; a++) {
                    if (filePath.contains(flagArgs[a])) {
                        sources.add(file);
                        return;
                    }
                }
            } else {
                sources.add(file);
            }

        } else if (filePath.endsWith(".jar") || filePath.endsWith(".war") || filePath.endsWith(".zip")) {
            System.out.println("找到压缩包,进行解压缩:" + filePath);
            try {
                getDirSource(sources, unzip(filePath));
            } catch (Exception e) {
                System.err.println("解压缩失败,错误信息:" + e.getMessage());
            }
        }
    }


    public static File unzip(String zipFile) throws IOException {
        File outFile = new File(zipFile + ".src");
        ZipFile zipZipFile = new ZipFile(zipFile, Charset.defaultCharset());
        Enumeration<? extends ZipEntry> entries = zipZipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            String name = zipEntry.getName();

            File file = new File(outFile, name);
            String absolutePath = file.getAbsolutePath();
            if (nameFlag) {
                for (int a = 1; a < flagArgs.length; a++) {
                    if (absolutePath.contains(flagArgs[a])) {
                        break;
                    }
                }
                continue;
            }
            if (zipEntry.isDirectory()) {
                file.mkdirs();

            } else {
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                InputStream inputStream = zipZipFile.getInputStream(zipEntry);
                inputStreamToFile(file, inputStream);
            }
        }
        zipZipFile.close();
        return outFile;
    }

    private static void inputStreamToFile(File tempFile, InputStream inputStream) throws IOException {

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            byte[] bytes = new byte[512];
            int len;
            while ((len = inputStream.read(bytes)) > 0) {
                fos.write(bytes, 0, len);
                fos.flush();
            }
        }
    }
}
