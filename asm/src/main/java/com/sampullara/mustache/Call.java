package com.sampullara.mustache;

/**
 * The call interface
 */
public interface Call {
  Object invoke(Object o, Scope scope);
}
