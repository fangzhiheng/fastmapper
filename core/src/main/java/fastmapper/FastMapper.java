/*
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package fastmapper;

import net.sf.cglib.core.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.security.ProtectionDomain;
import java.util.*;

import static net.sf.cglib.core.Constants.*;

/**
 * A quick method to convert the map to a Object.
 * <p>
 * NOTES:
 * <ul>
 *     <li>When you use resolver, you must use a unique resolver to avoid memory leak (Too much dynamic class created).</li>
 *     <li>You'd better to cache the instance.</li>
 *     <li>Recommended use the {@link Alias} annotation.</li>
 * </ul>
 *
 * @author iefan
 * @version 1.0
 */
public abstract class FastMapper {

    private static final Signature VALUE_OF_LONG = new Signature("valueOf", TYPE_LONG, new Type[]{TYPE_STRING});

    private static final Signature VALUE_OF_DOUBLE = new Signature("valueOf", TYPE_DOUBLE, new Type[]{TYPE_STRING});

    private static final Signature VALUE_OF_INTEGER = new Signature("valueOf", TYPE_INTEGER, new Type[]{TYPE_STRING});

    private static final Signature VALUE_OF_FLOAT = new Signature("valueOf", TYPE_FLOAT, new Type[]{TYPE_STRING});

    private static final Signature VALUE_OF_SHORT = new Signature("valueOf", TYPE_SHORT, new Type[]{TYPE_STRING});

    private static final Signature VALUE_OF_BYTE = new Signature("valueOf", TYPE_BYTE, new Type[]{TYPE_STRING});

    private static final Signature VALUE_OF_BOOLEAN = new Signature("valueOf", TYPE_BOOLEAN, new Type[]{TYPE_STRING});

    private static final Signature PARSE_LONG = new Signature("parseLong", Type.LONG_TYPE, new Type[]{TYPE_STRING});

    private static final Signature PARSE_DOUBLE = new Signature("parseDouble", Type.DOUBLE_TYPE, new Type[]{TYPE_STRING});

    private static final Signature PARSE_INTEGER = new Signature("parseInt", Type.INT_TYPE, new Type[]{TYPE_STRING});

    private static final Signature PARSE_FLOAT = new Signature("parseFloat", Type.FLOAT_TYPE, new Type[]{TYPE_STRING});

    private static final Signature PARSE_SHORT = new Signature("parseShort", Type.SHORT_TYPE, new Type[]{TYPE_STRING});

    private static final Signature PARSE_BYTE = new Signature("parseByte", Type.BYTE_TYPE, new Type[]{TYPE_STRING});

    private static final Signature PARSE_BOOLEAN = new Signature("parseBoolean", Type.BOOLEAN_TYPE, new Type[]{TYPE_STRING});

    private static final Map<Class<?>, Signature> PRIMITIVE_PARSE_MAPPING;

    static {
        Map<Class<?>, Signature> mapping = new HashMap<>();
        mapping.put(Long.class, VALUE_OF_LONG);
        mapping.put(Long.TYPE, PARSE_LONG);
        mapping.put(Double.class, VALUE_OF_DOUBLE);
        mapping.put(Double.TYPE, PARSE_DOUBLE);
        mapping.put(Integer.class, VALUE_OF_INTEGER);
        mapping.put(Integer.TYPE, PARSE_INTEGER);
        mapping.put(Float.class, VALUE_OF_FLOAT);
        mapping.put(Float.TYPE, PARSE_FLOAT);
        mapping.put(Short.class, VALUE_OF_SHORT);
        mapping.put(Short.TYPE, PARSE_SHORT);
        mapping.put(Byte.class, VALUE_OF_BYTE);
        mapping.put(Byte.TYPE, PARSE_BYTE);
        mapping.put(Boolean.class, VALUE_OF_BOOLEAN);
        mapping.put(Boolean.TYPE, PARSE_BOOLEAN);
        PRIMITIVE_PARSE_MAPPING = Collections.unmodifiableMap(mapping);
    }

    /*------------------------------------------* Type Declaration ------------------------------------------*/
    private static final Type TYPE_MAP_BEAN = Type.getType(FastMapper.class);

    private static final Type TYPE_MAP = Type.getType(Map.class);

    private static final Type TYPE_SET = Type.getType(Set.class);

    private static final Type TYPE_ITERATOR = Type.getType(Iterator.class);

    private static final Type TYPE_ENTRY = Type.getType(Map.Entry.class);

    /*------------------------------------------* Signature Declaration -------------------------------------*/
//    private static final Signature OBJECT_GET_CLASS = new Signature("getClass", TYPE_CLASS, new Type[0]);
    private static final Signature OBJECT_TO_STRING = new Signature("toString", TYPE_STRING, new Type[0]);

    private static final Signature STRING_CHAR_AT = new Signature("charAt", Type.CHAR_TYPE, new Type[]{Type.INT_TYPE});

    private static final Signature MAP_ENTRY_SET = new Signature("entrySet", TYPE_SET, new Type[0]);

    private static final Signature SET_ITERATOR = new Signature("iterator", TYPE_ITERATOR, new Type[0]);

    private static final Signature ITERATOR_HAS_NEXT = new Signature("hasNext", Type.BOOLEAN_TYPE, new Type[0]);

    private static final Signature ITERATOR_NEXT = new Signature("next", TYPE_OBJECT, new Type[0]);

    private static final Signature ENTRY_GET_KEY = new Signature("getKey", TYPE_OBJECT, new Type[0]);

    private static final Signature ENTRY_GET_VALUE = new Signature("getValue", TYPE_OBJECT, new Type[0]);

    /*------------------------------------------* Static Declaration -----------------------------------------*/
    private static final MapBeanKey KEY_FACTORY = (MapBeanKey) KeyFactory.create(MapBeanKey.class);

    private static final Signature POPULATE4MAP = new Signature("populate", Type.VOID_TYPE, new Type[]{TYPE_MAP, TYPE_OBJECT});

    public static FastMapper create(Class<?> source) {
        return create(source, null);
    }

    public static FastMapper create(Class<?> source, NameResolver resolver) {
        Generator gen = new Generator(source, resolver);
        return gen.create();
    }

    public abstract void populate(Map<?, ?> map, Object object);

    public interface MapBeanKey {

        Object newInstance(Class<?> cls, NameResolver resolver);
    }

    static class Generator extends AbstractClassGenerator<Object> {

        private static final Source SOURCE = new Source(FastMapper.class.getName());

        private final Type targetType;

        private final Class<?> target;

        private final NameResolver resolver;

        Generator(Class<?> target, NameResolver resolver) {
            super(SOURCE);
            Objects.requireNonNull(target, "Target class cannot be null.");
            this.target = target;
            this.targetType = Type.getType(target);
            this.resolver = resolver;
        }

        @Override
        protected ClassLoader getDefaultClassLoader() {
            return target.getClassLoader();
        }

        @Override
        protected Object firstInstance(Class type) {
            return ReflectUtils.newInstance(type);
        }

        @Override
        protected Object nextInstance(Object instance) {
            return instance;
        }

        @Override
        protected ProtectionDomain getProtectionDomain() {
            return ReflectUtils.getProtectionDomain(target);
        }

        @Override
        public void generateClass(ClassVisitor v) {
            PropertyDescriptor[] setters = ReflectUtils.getBeanSetters(target);
            ClassEmitter ce = new ClassEmitter(v);
            ce.begin_class(V1_5,
                    ACC_PUBLIC,
                    getClassName(),
                    TYPE_MAP_BEAN,
                    null,
                    SOURCE_FILE);
            EmitUtils.null_constructor(ce);
            generatePopulate4MapMethod(ce, setters);
            ce.end_class();
        }

        private void generatePopulate4MapMethod(ClassEmitter ce, PropertyDescriptor[] setters) {
            final CodeEmitter e = ce.begin_method(ACC_PUBLIC, POPULATE4MAP, null);

            // Check and assign to local variable objLocal
            final Label start = e.make_label();
            e.mark(start);

            // Cast arg2 to target
            final Local objLocal = e.make_local(targetType);
            e.load_arg(1);
            e.checkcast(targetType);
            e.store_local(objLocal);

            // Declare Local Variables
            final Local itrLocal = e.make_local(TYPE_ITERATOR);
            final Local entryLocal = e.make_local(TYPE_ENTRY);
            final Local keyLocal = e.make_local(TYPE_STRING);
            final Local valLocal = e.make_local(TYPE_OBJECT);

            final Label itrAssignLabel = e.make_label();
            e.mark(itrAssignLabel);

            // Assign to iterator
            e.load_arg(0);
            e.invoke_interface(TYPE_MAP, MAP_ENTRY_SET);
            e.invoke_interface(TYPE_SET, SET_ITERATOR);
            e.store_local(itrLocal);

            // Start while-loop with iterator
            final Label startLoopLabel = e.make_label();
            e.mark(startLoopLabel);
            e.load_local(itrLocal);
            e.invoke_interface(TYPE_ITERATOR, ITERATOR_HAS_NEXT);

            final Label loopLabel = e.make_label();
            e.if_jump(CodeEmitter.EQ, loopLabel);

            final Label fetchNext = e.make_label();
            e.mark(fetchNext);
            e.load_local(itrLocal);
            e.invoke_interface(TYPE_ITERATOR, ITERATOR_NEXT);
            e.checkcast(TYPE_ENTRY);
            e.store_local(entryLocal);

            final Label retrieveKey = e.make_label();
            e.mark(retrieveKey);
            e.load_local(entryLocal);
            e.invoke_interface(TYPE_ENTRY, ENTRY_GET_KEY);
            e.invoke_virtual(TYPE_OBJECT, OBJECT_TO_STRING);
            e.store_local(keyLocal);

            final Label getValLabel = e.make_label();
            e.mark(getValLabel);
            e.load_local(entryLocal);
            e.invoke_interface(TYPE_ENTRY, ENTRY_GET_VALUE);
            e.store_local(valLocal);

            // Load key first.
            final Map<String, PropertyDescriptor> mapping = buildNamePropertyMapping(setters, resolver);
            if (!mapping.isEmpty()) {
                e.load_local(keyLocal);
                EmitUtils.string_switch(e, mapping.keySet().toArray(new String[0]), SWITCH_STYLE_HASH, new ObjectSwitchCallback() {
                    @Override
                    public void processCase(Object key, Label end) {
                        dispatchCase(e, mapping.get((String) key), startLoopLabel, objLocal, valLocal);
                        e.goTo(end);
                    }

                    private void dispatchCase(CodeEmitter e, PropertyDescriptor descriptor, Label startLoopLabel, Local objLocal, Local valLocal) {
                        final Class<?> propertyType = descriptor.getPropertyType();
                        final Type propertyTypeType = Type.getType(propertyType);
                        final Signature signature = PRIMITIVE_PARSE_MAPPING.get(propertyType);
                        if (signature != null) {
                            dispatchToPrimitive(e, descriptor, startLoopLabel, propertyTypeType, signature, objLocal, valLocal);
                        } else if (propertyType == String.class) {
                            dispatchToString(e, descriptor, startLoopLabel, propertyTypeType, objLocal, valLocal);
                        } else if (propertyType == Character.class || propertyType == Character.TYPE) {
                            dispatchToCharacter(e, descriptor, startLoopLabel, propertyType, propertyTypeType, objLocal, valLocal);
                        } else {
                            dispatchObject(e, descriptor, startLoopLabel, propertyTypeType, objLocal, valLocal);
                        }
                    }

                    private void dispatchToPrimitive(CodeEmitter e, PropertyDescriptor descriptor, Label startLoopLabel,
                                                     Type propertyTypeType, Signature signature, Local objLocal, Local valLocal) {
                        Type wrapperType = null;
                        e.load_local(valLocal);
                        final Label nonnullLabel = e.make_label();
                        e.ifnonnull(nonnullLabel);

                        final Label nullLabel = e.make_label();
                        e.mark(nullLabel);
                        if (!TypeUtils.isPrimitive(propertyTypeType)) {
                            e.load_local(objLocal);
                            e.aconst_null();
                            e.checkcast(propertyTypeType);
                            e.invoke(ReflectUtils.getMethodInfo(descriptor.getWriteMethod()));
                        } else {
                            wrapperType = TypeUtils.getBoxedType(propertyTypeType);
                        }
                        e.goTo(startLoopLabel);
                        e.mark(nonnullLabel);

                        e.load_local(objLocal);
                        e.load_local(valLocal);
                        e.invoke_virtual(TYPE_OBJECT, OBJECT_TO_STRING);
                        e.invoke_static(wrapperType == null ? propertyTypeType : wrapperType, signature, false);
                        e.invoke(ReflectUtils.getMethodInfo(descriptor.getWriteMethod()));
                    }

                    private void dispatchToString(CodeEmitter e, PropertyDescriptor descriptor, Label startLoopLabel,
                                                  Type propertyTypeType, Local objLocal, Local valLocal) {
                        // use string
                        e.load_local(valLocal);
                        final Label nonnullLabel = e.make_label();
                        e.ifnonnull(nonnullLabel);

                        final Label nullLabel = e.make_label();
                        e.mark(nullLabel);
                        e.load_local(objLocal);
                        e.aconst_null();
                        e.checkcast(propertyTypeType);
                        e.invoke(ReflectUtils.getMethodInfo(descriptor.getWriteMethod()));
                        e.goTo(startLoopLabel);
                        e.mark(nonnullLabel);

                        e.load_local(valLocal);
                        e.instance_of(TYPE_STRING);
                        final Label instanceSucceed = e.make_label();
                        e.if_jump(CodeEmitter.NE, instanceSucceed);

                        final Label instanceFailed = e.make_label();
                        e.mark(instanceFailed);
                        e.load_local(valLocal);
                        e.invoke_virtual(TYPE_OBJECT, OBJECT_TO_STRING);
                        e.store_local(valLocal);

                        e.mark(instanceSucceed);
                        e.load_local(objLocal);
                        e.load_local(valLocal);
                        e.checkcast(TYPE_STRING);
                        e.invoke(ReflectUtils.getMethodInfo(descriptor.getWriteMethod()));
                    }

                    private void dispatchToCharacter(CodeEmitter e, PropertyDescriptor descriptor, Label startLoopLabel,
                                                     Class<?> propertyType, Type propertyTypeType, Local objLocal, Local valLocal) {
                        // use char
                        e.load_local(valLocal);
                        final Label nonnullLabel = e.make_label();
                        e.ifnonnull(nonnullLabel);

                        final Label nullLabel = e.make_label();
                        e.mark(nullLabel);
                        if (propertyType == Character.class) {
                            e.load_local(objLocal);
                            e.aconst_null();
                            e.checkcast(propertyTypeType);
                            e.invoke(ReflectUtils.getMethodInfo(descriptor.getWriteMethod()));
                        }
                        e.goTo(startLoopLabel);
                        e.mark(nonnullLabel);

                        e.load_local(objLocal);
                        e.load_local(valLocal);
                        e.invoke_virtual(TYPE_OBJECT, OBJECT_TO_STRING);
                        e.push(0);
                        e.invoke_virtual(TYPE_STRING, STRING_CHAR_AT);
                        if (!propertyType.isPrimitive()) {
                            e.box(Type.CHAR_TYPE);
                        }
                        e.invoke(ReflectUtils.getMethodInfo(descriptor.getWriteMethod()));
                    }

                    private void dispatchObject(CodeEmitter e, PropertyDescriptor descriptor, Label startLoopLabel,
                                                Type propertyTypeType, Local objLocal, Local valLocal) {
                        e.load_local(valLocal);
                        final Label nonnullLabel = e.make_label();
                        e.ifnonnull(nonnullLabel);

                        final Label nullLabel = e.make_label();
                        e.mark(nullLabel);
                        e.load_local(objLocal);
                        e.aconst_null();
                        e.invoke(ReflectUtils.getMethodInfo(descriptor.getWriteMethod()));
                        e.goTo(startLoopLabel);
                        e.mark(nonnullLabel);

                        e.load_local(valLocal);
                        e.instance_of(propertyTypeType);
                        final Label instanceFailed = e.make_label();
                        e.if_jump(CodeEmitter.EQ, instanceFailed);

                        final Label instanceSucceed = e.make_label();
                        e.mark(instanceSucceed);
                        e.load_local(objLocal);
                        e.load_local(valLocal);
                        e.invoke(ReflectUtils.getMethodInfo(descriptor.getWriteMethod()));
                        e.mark(instanceFailed);
                    }

                    @Override
                    public void processDefault() {
                        e.goTo(startLoopLabel);
                    }
                });
            }
            final Label finishLoop = e.make_label();
            e.mark(finishLoop);
            e.goTo(startLoopLabel);
            e.mark(loopLabel);
            e.return_value();

            e.make_label();
            e.end_method();
        }

        private Map<String, PropertyDescriptor> buildNamePropertyMapping(PropertyDescriptor[] setters, NameResolver resolver) {
            Map<String, PropertyDescriptor> map = new HashMap<>();
            for (PropertyDescriptor setter : setters) {
                final String[] names = resolve(resolver, setter);
                for (String name : names) {
                    map.put(name, setter);
                }
            }
            return map;
        }

        private String[] resolve(NameResolver resolver, PropertyDescriptor setter) {
            if (resolver != null) {
                return resolver.resolve(setter.getName());
            }
            Alias alias = setter.getWriteMethod().getAnnotation(Alias.class);
            if (alias != null) {
                return alias.value();
            }
            try {
                final Field field = target.getDeclaredField(setter.getName());
                alias = field.getAnnotation(Alias.class);
                if (alias != null) {
                    return alias.value();
                }
            } catch (NoSuchFieldException ignored) {
                // Who care :)
            }
            return new String[]{setter.getName()};
        }

        public FastMapper create() {
            Object key = KEY_FACTORY.newInstance(target, resolver);
            return (FastMapper) super.create(key);
        }
    }

    /**
     * Only used for the class that you cannot change its structure.
     * If you can, please use {@link Alias} instead.
     */
    public interface NameResolver {

        /**
         * Resolve property's aliases
         *
         * @param propertyName property's name
         *
         * @return all aliases, indicate map's key
         */
        String[] resolve(String propertyName);

        static NameResolver combined(NameResolver... resolvers) {
            return new NameResolver() {
                @Override
                public String[] resolve(String propertyName) {
                    ArrayList<String> list = new ArrayList<>();
                    for (NameResolver resolver : resolvers) {
                        list.addAll(Arrays.asList(resolver.resolve(propertyName)));
                    }
                    return list.toArray(new String[0]);
                }
            };
        }
    }

    public static final NameResolver AS_IS = new NameResolver() {

        @Override
        public String[] resolve(String propertyName) {
            return new String[]{propertyName};
        }
    };
}
