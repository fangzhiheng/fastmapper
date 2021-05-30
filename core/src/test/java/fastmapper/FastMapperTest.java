package fastmapper;

import net.sf.cglib.core.DebuggingClassWriter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * FastMapper Test Case.
 *
 * @author iefan
 * @version 1.0
 */
class FastMapperTest {

//    Look generated class.
//    static {
//        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "./cglib-classes");
//    }

    @Test
    void testSimple() {

        Map<String, Object> map = new HashMap<>();
        map.put("name", "Jane");
        map.put("age", 10);
        map.put("address", "China");
        map.put("licenses", null);

        final Person person = new Person();
        for (int i = 0; i < 100; i++) {
            final FastMapper mapper = FastMapper.create(Person.class);
            mapper.populate(map, person);
        }
        assertEquals("Jane", person.getName());
        assertEquals(10, person.getAge());
        assertEquals("China", person.getAddress());
        assertNull(person.getLicenses());

    }

    @Test
    void testNameResolver() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Jane");
        map.put("age", 10);
        map.put("address", "China");
        map.put("granted", 10);

        final FastMapper mapper = FastMapper.create(Person.class, FastMapper.NameResolver.combined(FastMapper.AS_IS, new FastMapper.NameResolver() {
            @Override
            public String[] resolve(String propertyName) {
                if ("licenses".equals(propertyName)) {
                    return new String[]{"granted"};
                }
                return new String[]{propertyName};
            }
        }));
        final Person person = new Person();
        mapper.populate(map, person);
        assertEquals("Jane", person.getName());
        assertEquals(10, person.getAge());
        assertEquals("China", person.getAddress());
        assertEquals(10, person.getLicenses());
    }

    @Test
    void testAnnotation() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Jane");
        map.put("age", 10);
        map.put("address", "China");
        map.put("granted2", 10);

        final FastMapper mapper = FastMapper.create(Person2.class);
        final Person2 person = new Person2();
        mapper.populate(map, person);
        assertEquals("Jane", person.getName());
        assertEquals(10, person.getAge());
        assertEquals("China", person.getAddress());
        assertEquals(10, person.getLicenses());
    }

    @Test
    void testComplexObject() {
        FastMapper.create(SmallObject.class);
        FastMapper.create(LargeObject.class);
    }


    public static class Person {

        String name;

        int age;

        String address;

        Integer licenses;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public Integer getLicenses() {
            return licenses;
        }

        public void setLicenses(Integer licenses) {
            this.licenses = licenses;
        }
    }

    public static class Person2 {

        String name;

        int age;

        String address;

        @Alias({"licenses", "granted2"})
        Integer licenses;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public Integer getLicenses() {
            return licenses;
        }

        public void setLicenses(Integer licenses) {
            this.licenses = licenses;
        }
    }
}