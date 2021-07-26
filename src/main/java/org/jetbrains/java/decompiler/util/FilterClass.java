package org.jetbrains.java.decompiler.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


//modify
public class FilterClass {

  public static boolean FILTER_FLAG = true;
  private static boolean USE_WHITE = false;
  private static List<String> whiteList;
  private static List<String> blackList;

  static {
    try {
      whiteList = getFileList("white");
      blackList = getFileList("black");
      if (whiteList != null&&whiteList.size()>0) {
        USE_WHITE = true;
      }
      if (blackList == null||blackList.size()<1) {
        FILTER_FLAG = false;
      }
      System.out.println(FILTER_FLAG);
      System.out.println(USE_WHITE);
    } catch (Exception e) {
      e.printStackTrace();
      FILTER_FLAG = false;
    }
  }

  public static void main(String[] args) {
    String jarPath = getJarPath();
    System.out.println(whiteList.size());
    System.out.println(blackList.size());

    System.out.println(new File(jarPath).exists());
  }

  public static boolean judge(String className) {
    //System.out.println(className);
    if (FILTER_FLAG) {
      //判断是否使用白名单，白名单启用的话，黑名单失效
      if (USE_WHITE) {
        return findArray(className, whiteList);
      } else {
        return !findArray(className, blackList);
      }
    }
    return false;
  }

  private static boolean findArray(String str, List<String> list) {
    //System.out.println("查找class："+str);
    for (String temp : list) {
      if (str.contains(temp)) {
        return true;
      }
    }
    return false;
  }


  private static List<String> getFileList(String type) throws IOException {
    String jarPath = getJarPath();
    String filePath = jarPath + "." + type + ".txt";
    File file = new File(filePath);
    if (file.exists()) {
      ArrayList<String> list = new <String>ArrayList<String>();
      BufferedReader br = new BufferedReader(new FileReader(file));
      String temp;
      while ((temp = br.readLine()) != null) {
        list.add(temp);
      }
      br.close();
      return list;
    }
    return null;
  }

  private static String getJarPath() {
    String path = FilterClass.class.getProtectionDomain().getCodeSource().toString();
    path = path.replaceAll("file:", "");
    path = path.replaceAll(" <no signer certificates>\\)", "");
    path = path.replaceAll("\\(", "");
    return path;
  }
}
