package com.sampullara.mustache;

/**
 * ASM example
 */
public class Method4Call implements Call {
  public Object invoke(Object test, Scope scope) {
    return ((TestCall) test).test4();
  }
}
