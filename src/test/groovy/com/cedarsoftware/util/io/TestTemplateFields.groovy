package com.cedarsoftware.util.io

import org.junit.Assert
import org.junit.Test

import java.awt.Point

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNull

/**
 * @author John DeRegnaucourt (jdereg@gmail.com)
 *         <br/>
 *         Copyright (c) Cedar Software LLC
 *         <br/><br/>
 *         Licensed under the Apache License, Version 2.0 (the "License")
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *         <br/><br/>
 *         http://www.apache.org/licenses/LICENSE-2.0
 *         <br/><br/>
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 */
class TestTemplateFields
{
    static class Test1
    {
        protected Test2<String, Object> internalMember = new Test2<>()
    }

    static class Test2<K, V>
    {
        protected Map<K, Collection<? extends V>> hsOne = new HashMap<>()
        protected Class<?> two = LinkedList.class
    }

    static class Person
    {
        String name
        int age

        Person(String n, int a) { name = n; age = a}
    }

    static class Test3
    {
        protected Test4<String> internalMember = new Test4<>()
    }

    static class Test4<V>
    {
        protected Person two = new Person('Jane', 45)
    }

    class Single<T>
    {
        private T field1;
        private T field2;

        public Single(T field1, T field2)
        {
            this.field1 = field1;
            this.field2 = field2;
        }
    }

    class UseSingle
    {
        private Single<String> single;

        public UseSingle(Single<String> single)
        {
            this.single = single;
        }
    }

    static class StaticSingle<T>
    {
        private T field1;

        public StaticSingle(T field1)
        {
            this.field1 = field1;
        }
    }

    static class StaticUseSingle
    {
        private StaticSingle<String> single;

        public StaticUseSingle(StaticSingle<String> single)
        {
            this.single = single;
        }
    }

    class TwoParam<T, V>
    {
        private T field1;
        private T field2;
        private V field3;

        public TwoParam(T field1, T field2, V field3)
        {
            this.field1 = field1;
            this.field2 = field2;
            this.field3 = field3;
        }
    }

    class UseTwoParam
    {
        private TwoParam twoParam;

        public UseTwoParam(TwoParam twoParam)
        {
            this.twoParam = twoParam;
        }
    }

    static class ThreeType<T, U, V>
    {
        T t;
        U u;
        V v;

        public ThreeType(T tt, U uu, V vv)
        {
            t = tt;
            u = uu;
            v = vv;
        }
    }

    static class GenericHolder
    {
        ThreeType<Point, String, Point> a;
    }

    // This test was provided by Github user: reuschling
    @Test
    void testTemplateClassField()
    {
        Test1 container = new Test1()
        Test1 container2 = new Test1()
        LinkedList<Test1> llList = new LinkedList<>()
        llList.add(container2)
        container.internalMember.hsOne.put("key", llList)
        String json = GroovyJsonWriter.objectToJson(container)

        // This would throw exception in the past
        GroovyJsonReader.jsonToGroovy(json)
    }

    @Test
    void testTemplateNonClassFields()
    {
        Test3 container = new Test3()
        String json = GroovyJsonWriter.objectToJson(container)
        // This would throw exception in the past
        Test3 reanimated = GroovyJsonReader.jsonToGroovy(json)
        assert reanimated.internalMember.two.name == 'Jane'
        assert reanimated.internalMember.two.age == 45
    }

    @Test
    void testSingle()
    {
        UseSingle useSingle = new UseSingle(new Single<String>("Steel", "Wood"))
        String json = GroovyJsonWriter.objectToJson(useSingle)
        UseSingle other = (UseSingle) GroovyJsonReader.jsonToGroovy(json)

        assertEquals("Steel", other.single.field1)
        assertEquals("Wood", other.single.field2)
    }

    @Test
    void testTwoParam()
    {
        UseTwoParam useTwoParam = new UseTwoParam(new TwoParam("Hello", "Goodbye", new Point(20, 40)))
        String json = GroovyJsonWriter.objectToJson(useTwoParam)
        UseTwoParam other = (UseTwoParam) GroovyJsonReader.jsonToGroovy(json)

        assertEquals("Hello", other.twoParam.field1)
        assertEquals("Goodbye", other.twoParam.field2)
        assertEquals(new Point(20, 40), other.twoParam.field3)

        useTwoParam = new UseTwoParam(new TwoParam(new Point(10, 30), new Point(20, 40), "Hello"))
        json = GroovyJsonWriter.objectToJson(useTwoParam)
        other = (UseTwoParam) GroovyJsonReader.jsonToGroovy(json)

        assertEquals(new Point(10, 30), other.twoParam.field1)
        assertEquals(new Point(20, 40), other.twoParam.field2)
        assertEquals("Hello", other.twoParam.field3)

        useTwoParam = new UseTwoParam(new TwoParam(50, 100, "Hello"))
        json = GroovyJsonWriter.objectToJson(useTwoParam)
        other = (UseTwoParam) GroovyJsonReader.jsonToGroovy(json)

        assertEquals(50, other.twoParam.field1)
        assertEquals(100, other.twoParam.field2)
        assertEquals("Hello", other.twoParam.field3)

        useTwoParam = new UseTwoParam(new TwoParam(new Point(10, 30), new Point(20, 40), new TestObject("Hello")))
        json = GroovyJsonWriter.objectToJson(useTwoParam)
        other = (UseTwoParam) GroovyJsonReader.jsonToGroovy(json)

        assertEquals(new Point(10, 30), other.twoParam.field1)
        assertEquals(new Point(20, 40), other.twoParam.field2)
        Assert.assertEquals(new TestObject("Hello"), other.twoParam.field3)
    }

    @Test
    void testStaticSingle()
    {
        StaticUseSingle useSingle = new StaticUseSingle(new StaticSingle<>("Boonies"))

        String json = GroovyJsonWriter.objectToJson(useSingle)
        //this will crash on ArrayIndexOutOfBoundsException
        StaticUseSingle other = (StaticUseSingle) GroovyJsonReader.jsonToGroovy(json)

        assertEquals("Boonies", other.single.field1)
    }

    @Test
    void test3TypeGeneric()
    {
        String json = '{"@type":"' + GenericHolder.class.getName() + '","a":{"t":{"x":1,"y":2},"u":"Sochi","v":{"x":10,"y":20}}}'
        GenericHolder gen = (GenericHolder) GroovyJsonReader.jsonToGroovy(json)
        assertEquals(new Point(1, 2), gen.a.t)
        assertEquals("Sochi", gen.a.u)
        assertEquals(new Point(10, 20), gen.a.v)

        json = '{"@type":"' + GenericHolder.class.getName() + '","a":{"t":null,"u":null,"v":null}}'
        gen = (GenericHolder) GroovyJsonReader.jsonToGroovy(json)
        assertNull(gen.a.t)
        assertNull(gen.a.u)
        assertNull(gen.a.v)
    }

}
