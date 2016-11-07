package com.javarush.test.level30.lesson15.big01;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Михаил on 22.10.2016.
 */
public class ConsoleHelper
{
    private static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    public static void writeMessage(String message){
        System.out.println(message);
    }
    public static String readString(){
        String s="";
        try{
          s=  br.readLine();
        }catch (IOException e){
            writeMessage("Произошла ошибка при попытке ввода текста. Попробуйте еще раз.");
           s= readString();
        }
        return s;
        }

    public static int readInt(){
        int i=0;
        try{
            i=Integer.parseInt(readString());
        }catch (NumberFormatException e){
            writeMessage("Произошла ошибка при попытке ввода числа. Попробуйте еще раз.");
            i=readInt();
        }
        return i;
    }

}
