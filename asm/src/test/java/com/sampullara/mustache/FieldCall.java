package com.sampullara.mustache;

/**
 * ASM example
 */
public class FieldCall implements Call {
  public Object invoke(Object test, Scope scope) {
    return ((TestCall) test).test;
  }
}
