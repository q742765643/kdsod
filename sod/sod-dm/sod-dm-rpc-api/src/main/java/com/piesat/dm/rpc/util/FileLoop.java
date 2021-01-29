package com.piesat.dm.rpc.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileLoop<main> {

    public static List<File> fileList = new ArrayList<>();

    /**
     * 循环获取指定文件夹下的所有文件
     * @param path
     */
    private void loopReadDir(String path){
        File filePath = new File(path);
        File[] list = filePath.listFiles();
        if(list!=null && list.length>0){
            for(int i=0; i<list.length; i++){
                File f = list[i];
                if(f.isDirectory() && !f.isHidden()){
                    fileList.add(f);
                    loopReadDir(f.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 调用静态方法
     * @param path
     * @return
     */
    public static List<File> fileReadLoop(String path) {
        FileLoop fileCon = new FileLoop();
        fileCon.loopReadDir(path);
        return fileList;
    }

    public static void main(String[] args) {
        List<File> list = fileReadLoop("d:/maven");
        System.out.println();
        for(File file : list){
            if(file.isDirectory()){
                System.out.print(file.length()+" ");
            }
//            File fileNew = new File(file.getAbsolutePath());
//            System.out.print(fileNew.length()+" ");
        }

        File dFile = new File("d:/maven");
        System.out.println(dFile.length());
    }
}
