package com.sampullara.mustache;

/**
 * ASM example
 */
public class Method3Call implements Call {
  public Object invoke(Object test, Scope scope) {
    return ((TestCall) test).test3();
  }
}
