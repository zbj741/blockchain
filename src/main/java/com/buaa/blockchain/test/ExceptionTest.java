package com.buaa.blockchain.test;

public class ExceptionTest {
    public static void main(String[] args) {
        System.out.println(div(6,2)+"");
        System.out.println(div(4,0)+"");
    }
    public static int div(int a, int b){
        int c;
        try{
            c = a/b;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
        System.out.println("safety...");
        return c;
    }
}
