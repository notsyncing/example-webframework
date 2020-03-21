package org.example.webframework.lesson9;

public class Handler1 implements Handler {
    @Override
    public void handle(Context context, HandlerChain chain) {
        System.out.println("Handler 1 enter!");

        chain.nextHandler(context);

        System.out.println("Handler 1 exit!");
    }
}
