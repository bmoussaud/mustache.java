package com.sampullara.mustache;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * TODO: Edit this
 * <p/>
 * User: sam
 * Date: 10/8/11
 * Time: 6:47 PM
 */
public class TestCall {
  public String test(Scope scope) {
    return "";
  }

  public String test3() {
    return "";
  }

  public boolean test4() {
    return true;
  }

  public String test2() {
    return "";
  }

  String test;

  public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    int TOTAL = 100000000;
    TestCall test = new TestCall();
    ObjectHandler objectHandler6 = new ObjectHandler6();
    Scope scope = new Scope();
    objectHandler6.handleObject(test, scope, "test");
    Method method = TestCall.class.getMethod("test", Scope.class);
    Call call = new MethodCall();

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 3; i++) {
      long start = now();
      for (int j = 0; j < TOTAL; j++) {
        sb.append(objectHandler6.handleObject(test, scope, "test"));
      }
      long end = now();
      System.out.println("Lookup: " + (end - start));
      start = now();
      for (int j = 0; j < TOTAL; j++) {
        sb.append(method.invoke(test, scope));
      }
      end = now();
      System.out.println("Method: " + (end - start));
      start = end;
      for (int j = 0; j < TOTAL; j++) {
        sb.append(call.invoke(test, scope));
      }
      end = now();
      System.out.println("Called: " + (end - start));
      start = end;
      for (int j = 0; j < TOTAL; j++) {
        sb.append(test.test(scope));
      }
      end = now();
      System.out.println("Direct: " + (end - start));
      System.out.println(sb.toString());
    }

    Call call2 = new Call() {
      @Override
      public Object invoke(Object test, Scope scope) {
        return ((TestCall)test).test2();
      }
    };
    Method method2 = TestCall.class.getMethod("test2");
    objectHandler6.handleObject(test, scope, "test2");
    for (int i = 0; i < 3; i++) {
      long start = now();
      for (int j = 0; j < TOTAL; j++) {
        sb.append(objectHandler6.handleObject(test, scope, "test2"));
      }
      long end = now();
      System.out.println("Lookup: " + (end - start));
      start = now();
      for (int j = 0; j < TOTAL; j++) {
        sb.append(method2.invoke(test));
      }
      end = now();
      System.out.println("Method: " + (end - start));
      start = end;
      for (int j = 0; j < TOTAL; j++) {
        sb.append(call2.invoke(test, scope));
      }
      end = now();
      System.out.println("Called: " + (end - start));
      start = end;
      for (int j = 0; j < TOTAL; j++) {
        sb.append(test.test2());
      }
      end = now();
      System.out.println("Direct: " + (end - start));
      System.out.println(sb.toString());
    }
  }

  private static long now() {
    return System.currentTimeMillis();
  }

}
