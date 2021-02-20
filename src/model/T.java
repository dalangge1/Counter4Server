package model;

public class T {

    interface Callback {
        void run();
    }


    public static void main(String[] args) {

        doSomething(new Callback() {
            @Override
            public void run() {
                System.out.println("做完事情了，收到通知");
            }
        });

    }


    private static void doSomething (Callback callback) {
        // doSomething
        System.out.println("做事情");
        // 通知已经做完事情
        callback.run();

    }


}
