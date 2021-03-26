package com.buaa.blockchain.vm.program;

import com.buaa.blockchain.vm.DataWord;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2020/12/16
 * @since JDK1.8
 */
public class Stack {
    private java.util.Stack<DataWord> stack = new java.util.Stack<>();

    public synchronized DataWord pop() {
        return stack.pop();
    }

    public void push(DataWord item) {
        stack.push(item);
    }

    public void swap(int from, int to) {
        if (isAccessible(from) && isAccessible(to) && (from != to)) {
            DataWord tmp = stack.get(from);
            stack.set(from, stack.set(to, tmp));
        }
    }

    public DataWord peek() {
        return stack.peek();
    }

    public DataWord get(int index) {
        return stack.get(index);
    }

    public int size() {
        return stack.size();
    }

    private boolean isAccessible(int from) {
        return from >= 0 && from < stack.size();
    }

    public DataWord[] toArray() {
        return stack.toArray(new DataWord[0]);
    }
}
