package com.sampullara.mustache;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.sampullara.mustache.ObjectHandler6.getField;
import static com.sampullara.mustache.ObjectHandler6.getMethod;

/**
 * Uses ASM to create Call adapters for every method or field.
 */
public class ASMObjectHandler implements ObjectHandler, Opcodes {
  private static ClassLoader cl = ASMObjectHandler.class.getClassLoader();
  private static Logger logger = Logger.getLogger(Mustache.class.getName());

  protected static Map<Class, Map<String, Call>> cache = new ConcurrentHashMap<Class, Map<String, Call>>();

  private static Call NO_CALL = new Call() {
    public Object invoke(Object o, Scope scope) {
      return null;
    }
  };

  public Object handleObject(Object parent, Scope scope, String name) {
    Object value = null;
    Class aClass = parent.getClass();
    Map<String, Call> members;
    synchronized (Mustache.class) {
      // Don't overload methods in your contexts
      members = cache.get(aClass);
      if (members == null) {
        members = new ConcurrentHashMap<String, Call>();
        cache.put(aClass, members);
      }
    }
    Call member = members.get(name);
    if (member == NO_CALL) return null;
    try {
      if (member == null) {
        try {
          Field field = getField(name, aClass);
          members.put(name, member = createCall(field.getType(), aClass, name, false, true));
        } catch (NoSuchFieldException e) {
          // Not set
        }
      }
      if (member == null) {
        try {
          Method method = getMethod(name, aClass);
          members.put(name, member = createCall(method.getReturnType(), aClass, name, false, false));
        } catch (Exception e) {
          try {
            Method method = getMethod(name, aClass, Scope.class);
            members.put(name, member = createCall(method.getReturnType(), aClass, name, true, false));
          } catch (Exception e1) {
            String propertyname = name.substring(0, 1).toUpperCase() + (name.length() > 1 ? name.substring(1) : "");
            try {
              Method method = getMethod("get" + propertyname, aClass);
              members.put(name, member = createCall(method.getReturnType(), aClass, name, false, false));
            } catch (Exception e2) {
              try {
                Method method = getMethod("is" + propertyname, aClass);
                members.put(name, member = createCall(method.getReturnType(), aClass, name, false, false));
              } catch (Exception e3) {
                // Not set
              }
            }
          }
        }
      }
      if (member != null) {
        value = member.invoke(parent, scope);
      }
      if (value == null) {
        value = Scope.NULL;
      }
    } catch (Throwable e) {
      // Might be nice for debugging but annoying in practice
      logger.log(Level.WARNING, "Failed to get value for " + name, e);
    }
    if (member == null) {
      members.put(name, NO_CALL);
    }
    return value;
  }

  private static AtomicInteger classNum = new AtomicInteger(0);

  public static Call createCall(Class returnType, Class target, String name, boolean scope, boolean field) throws Exception {
    int num = classNum.incrementAndGet();
    String returnTypeDescriptor = Type.getDescriptor(returnType);

    String targetClassName = target.getName().replace(".", "/");
    String className = "_m_" + name.replaceAll("[^a-zA-Z0-9]", "_") + "_" + num;
    ClassWriter cw = new ClassWriter(0);
    MethodVisitor mv;

    String fullClassName = target.getPackage().getName().replace(".", "/") + "/" + className;
    cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, fullClassName, null, "java/lang/Object", new String[]{"com/sampullara/mustache/Call"});

    cw.visitSource(className + ".java", null);

    String fullClassRef = "L" + fullClassName + ";";
    {
      mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(7, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
      mv.visitInsn(RETURN);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLocalVariable("this", fullClassRef, null, l0, l1, 0);
      mv.visitMaxs(1, 1);
      mv.visitEnd();
    }
    {
      mv = cw.visitMethod(ACC_PUBLIC, "invoke", "(Ljava/lang/Object;Lcom/sampullara/mustache/Scope;)Ljava/lang/Object;", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(9, l0);
      mv.visitVarInsn(ALOAD, 1);
      mv.visitTypeInsn(CHECKCAST, targetClassName);
      if (field) {
        mv.visitFieldInsn(GETFIELD, targetClassName, name, returnTypeDescriptor);
      } else {
        if (scope) {
          mv.visitVarInsn(ALOAD, 2);
          mv.visitMethodInsn(INVOKEVIRTUAL, targetClassName, name, "(Lcom/sampullara/mustache/Scope;)" + returnTypeDescriptor);
        } else {
          mv.visitMethodInsn(INVOKEVIRTUAL, targetClassName, name, "()" + returnTypeDescriptor);
        }
      }
      if (returnType.isPrimitive()) {
        Type fieldType = Type.getType(returnType);
        switch (fieldType.getSort()) {
          case Type.BOOLEAN:
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
            break;
          case Type.BYTE:
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
            break;
          case Type.CHAR:
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
            break;
          case Type.SHORT:
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
            break;
          case Type.INT:
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
            break;
          case Type.FLOAT:
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
            break;
          case Type.LONG:
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
            break;
          case Type.DOUBLE:
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
            break;
        }
      }
      mv.visitInsn(ARETURN);
      Label l1 = new Label();
      mv.visitLabel(l1);
      mv.visitLocalVariable("this", fullClassRef, null, l0, l1, 0);
      mv.visitLocalVariable("test", "Ljava/lang/Object;", null, l0, l1, 1);
      mv.visitLocalVariable("scope", "Lcom/sampullara/mustache/Scope;", null, l0, l1, 2);
      mv.visitMaxs(scope ? 2 : 1, 3);
      mv.visitEnd();
    }
    cw.visitEnd();

    return (Call) loadClass(fullClassName.replace("/", "."), cw.toByteArray()).newInstance();
  }

  private static Class loadClass(String className, byte[] b) {
    //override classDefine (as it is protected) and define the class.
    Class clazz = null;
    try {
      ClassLoader loader = ASMObjectHandler.class.getClassLoader();
      Class cls = Class.forName("java.lang.ClassLoader");
      java.lang.reflect.Method method =
          cls.getDeclaredMethod("defineClass", new Class[]{String.class, byte[].class, int.class, int.class});

      // protected method invocaton
      method.setAccessible(true);
      try {
        Object[] args = new Object[]{className, b, 0, b.length};
        clazz = (Class) method.invoke(loader, args);
      } finally {
        method.setAccessible(false);
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    return clazz;
  }
}
