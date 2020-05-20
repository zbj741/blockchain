package com.buaa.blockchain.test;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Test {



    public static void main(String[] args) {
        TreeSet<String> treeSet = new TreeSet<>();
        treeSet.add("233");
        treeSet.add("hitty");
        treeSet.add("666");
        treeSet.add("blockchain");
        System.out.println(treeSet);
        Iterator iterator = treeSet.iterator();
        for(int i = 0;i < treeSet.size();i++){
            System.out.println((String)iterator.next());
        }


    }
    public static void back(int first,ArrayList<Integer> nums){
        if(first == nums.size()){
            System.out.println(nums);
            return;
        }
        for(int i = first;i < nums.size();i++){
            //System.out.println(i+","+first);
            Collections.swap(nums,i,first);
            back(first+1,nums);
            Collections.swap(nums,i,first);
        }

    }



}
