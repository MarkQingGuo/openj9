/*******************************************************************************
 * Copyright (c) 2018, 2019 IBM Corp. and others
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution and is available at https://www.eclipse.org/legal/epl-2.0/
 * or the Apache License, Version 2.0 which accompanies this distribution and
 * is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * This Source Code may also be made available under the following
 * Secondary Licenses when the conditions for such availability set
 * forth in the Eclipse Public License, v. 2.0 are satisfied: GNU
 * General Public License, version 2 with the GNU Classpath
 * Exception [1] and GNU General Public License, version 2 with the
 * OpenJDK Assembly Exception [2].
 *
 * [1] https://www.gnu.org/software/classpath/license.html
 * [2] http://openjdk.java.net/legal/assembly-exception.html
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 OR LicenseRef-GPL-2.0 WITH Assembly-exception
 *******************************************************************************/
package org.openj9.test.lworld;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import sun.misc.Unsafe;

import org.testng.Assert;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/*
 * Instructions to run this test:
 * 
 * 1) recompile the JVM with J9VM_OPT_VALHALLA_VALUE_TYPES flag turned on in j9cfg.h.ftl (or j9cfg.h.in when cmake is turned on)
 * 2) cd [openj9-openjdk-dir]/openj9/test/TestConfig
 * 3) export JAVA_BIN=[openj9-openjdk-dir]/build/linux-x86_64-normal-server-release/images/jdk/bin
 * 4) export PATH=$JAVA_BIN:$PATH
 * 5) export JDK_VERSION=Valhalla
 * 6) export SPEC=linux_x86-64_cmprssptrs
 * 7) export BUILD_LIST=functional/Valhalla
 * 8) make -f run_configure.mk && make compile && make _sanity
 */

@Test(groups = { "level.sanity" })
public class ValueTypeTests {
	//order by class
	static Lookup lookup = MethodHandles.lookup();
	static Unsafe myUnsafe = createUnsafeInstance();
	//point2DClass: make sure point2DClass is not null, getX is updated
	static Class point2DClass = null;
	static MethodHandle makePoint2D = null;	
	static MethodHandle getX = null;
	static MethodHandle getY = null;
	//line2DClass
	static Class line2DClass = null;
	static MethodHandle makeLine2D = null;
	static MethodHandle getSt = null;
	static MethodHandle getEn = null;
	//flattenLine2DClass
	static Class flattenedLine2DClass = null;
	static MethodHandle makeFlattenedLine2D = null;
	static MethodHandle getStGeneric = null;
	static MethodHandle getEnGeneric = null;
	//triangleClass
	static Class triangle2DClass = null;
	static MethodHandle makeTriangle2D = null;
	static MethodHandle getV1Generic = null;
	static MethodHandle getV2Generic = null;
	static MethodHandle getV3Generic = null;
	//valueLongClass
	static Class valueLongClass = null;
	static MethodHandle makeValueLong = null;
	static MethodHandle getLong = null;
	static MethodHandle withLong = null;
	//valueIntClass
	static Class valueIntClass = null;
	static MethodHandle makeValueInt = null;
	static MethodHandle getInt = null;
	static MethodHandle withInt = null;
	//valueDoubleClass
	static Class valueDoubleClass = null;
	static MethodHandle makeValueDouble = null;
	static MethodHandle getDouble = null;
	static MethodHandle withDouble = null;
	//valueFloatClass
	static Class valueFloatClass =null;
	static MethodHandle makeValueFloat = null;
	static MethodHandle getFloat = null;
	static MethodHandle withFloat = null;
	//valueObejctClass
	static Class valueObjectClass = null;
	static MethodHandle makeValueObject = null;
	static MethodHandle getObject = null;
	static MethodHandle withObject = null;


	/*
	 * Create a value type
	 * 
	 * value Point2D {
	 * 	int x;
	 * 	int y;
	 * }
	 */
	@Test(priority=1)
	static public void testCreatePoint2D() throws Throwable {
		String fields[] = {"x:I", "y:I"};
		point2DClass = ValueTypeGenerator.generateValueClass("Point2D", fields);
		
		makePoint2D = lookup.findStatic(point2DClass, "makeValue", MethodType.methodType(point2DClass, int.class, int.class));
		
		getX = generateGetter(point2DClass, "x", int.class);
		MethodHandle withX = generateWither(point2DClass, "x", int.class);
		getY = generateGetter(point2DClass, "y", int.class);
		MethodHandle withY = generateWither(point2DClass, "y", int.class);

		int x = 0xFFEEFFEE;
		int y = 0xAABBAABB;
		int xNew = 0x11223344;
		int yNew = 0x99887766;
		
		Object point2D = makePoint2D.invoke(x, y);
		
		assertEquals(getX.invoke(point2D), x);
		assertEquals(getY.invoke(point2D), y);
		
		point2D = withX.invoke(point2D, xNew);
		point2D = withY.invoke(point2D, yNew);
		
		assertEquals(getX.invoke(point2D), xNew);
		assertEquals(getY.invoke(point2D), yNew);
		
		Field xField = point2DClass.getDeclaredField("x");
		long xFieldOffset = myUnsafe.objectFieldOffset(xField);
		int value = myUnsafe.getInt(point2D, xFieldOffset);
		if (value != xNew) {
			throw new Error("Expected " + xNew + " from getInt() but got " + value);
		}
		//long temp = myFieldOffset;
		//myField = point2DClass.getDeclaredField("y");
		//myFieldOffset = myUnsafe.objectFieldOffset(myField);
		//temp = myFieldOffset - temp;
		value = myUnsafe.getInt(point2D, (4 + xFieldOffset));
		if (value != yNew) {
			throw new Error("Expected " + yNew + " from getInt() but got " + value);
		}
		//System.out.println("unflattened one distance " + (temp));
	}

	/*
	 * Create a value type with double slot primative members
	 * 
	 * value Point2DComplex {
	 * 	double d;
	 * 	long j;
	 * }
	 */
	@Test(priority=1)
	static public void testCreatePoint2DComplex() throws Throwable {
		String fields[] = {"d:D", "j:J"};
		Class point2DComplexClass = ValueTypeGenerator.generateValueClass("Point2DComplex", fields);
		
		MethodHandle makePoint2DComplex = lookup.findStatic(point2DComplexClass, "makeValue", MethodType.methodType(point2DComplexClass, double.class, long.class));

		MethodHandle getD = generateGetter(point2DComplexClass, "d", double.class);
		MethodHandle withD = generateWither(point2DComplexClass, "d", double.class);
		MethodHandle getJ = generateGetter(point2DComplexClass, "j", long.class);
		MethodHandle withJ = generateWither(point2DComplexClass, "j", long.class);
		
		double d = Double.MAX_VALUE;
		long j = Long.MAX_VALUE;
		double dNew = Long.MIN_VALUE;
		long jNew = Long.MIN_VALUE;
		Object point2D = makePoint2DComplex.invoke(d, j);
		
		assertEquals(getD.invoke(point2D), d);
		assertEquals(getJ.invoke(point2D), j);
		
		point2D = withD.invoke(point2D, dNew);
		point2D = withJ.invoke(point2D, jNew);
		assertEquals(getD.invoke(point2D), dNew);
		assertEquals(getJ.invoke(point2D), jNew);


		MethodHandle getDGeneric = generateGenericGetter(point2DComplexClass, "d");
		MethodHandle withDGeneric = generateGenericWither(point2DComplexClass, "d");
		MethodHandle getJGeneric = generateGenericGetter(point2DComplexClass, "j");
		MethodHandle withJGeneric = generateGenericWither(point2DComplexClass, "j");
		
		point2D = withDGeneric.invoke(point2D, d);
		point2D = withJGeneric.invoke(point2D, j);
		assertEquals(getDGeneric.invoke(point2D), d);
		assertEquals(getJGeneric.invoke(point2D), j);
		
		Field myField = point2DComplexClass.getDeclaredField("d");
		long myFieldOffset = myUnsafe.objectFieldOffset(myField);
		double dValue = myUnsafe.getDouble(point2D, myFieldOffset);
		if (dValue != d) {
			throw new Error("Expected " + d + " from getDouble() but got " + dValue);
		}
		//myField = point2DComplexClass.getDeclaredField("j");
		//myFieldOffset = myUnsafe.objectFieldOffset(myField);
		long jValue = myUnsafe.getLong(point2D, 8 + myFieldOffset);
		if (jValue != j) {
			throw new Error("Expected " + j + " from getLong() but got " + jValue);
		}
	}

	/*
	 * Test with nested values in reference type
	 * 
	 * value Line2D {
	 * 	Point2D st;
	 * 	Point2D en;
	 * }
	 * 
	 */
	@Test(priority=2)
	static public void testCreateLine2D() throws Throwable {
		String fields[] = {"st:LPoint2D;:value", "en:LPoint2D;:value"};
		line2DClass = ValueTypeGenerator.generateValueClass("Line2D", fields);
		
		makeLine2D = lookup.findStatic(line2DClass, "makeValue", MethodType.methodType(line2DClass, point2DClass, point2DClass));
		
		getSt = generateGetter(line2DClass, "st", point2DClass);
 		MethodHandle withSt = generateWither(line2DClass, "st", point2DClass);
 		getEn = generateGetter(line2DClass, "en", point2DClass);
 		MethodHandle withEn = generateWither(line2DClass, "en", point2DClass);
 		
		int x = 0xFFEEFFEE;
		int y = 0xAABBAABB;
		int xNew = 0x11223344;
		int yNew = 0x99887766;
		int x2 = 0xCCDDCCDD;
		int y2 = 0xAAFFAAFF;
		int x2New = 0x55337799;
		int y2New = 0x88662244;
		
		Object st = makePoint2D.invoke(x, y);
		Object en = makePoint2D.invoke(x2, y2);
		
		assertEquals(getX.invoke(st), x);
		assertEquals(getY.invoke(st), y);
		assertEquals(getX.invoke(en), x2);
		assertEquals(getY.invoke(en), y2);
		
		Object line2D = makeLine2D.invoke(st, en);
		
		assertEquals(getX.invoke(getSt.invoke(line2D)), x);
		assertEquals(getY.invoke(getSt.invoke(line2D)), y);
		assertEquals(getX.invoke(getEn.invoke(line2D)), x2);
		assertEquals(getY.invoke(getEn.invoke(line2D)), y2);
		
		Object stNew = makePoint2D.invoke(xNew, yNew);
		Object enNew = makePoint2D.invoke(x2New, y2New);
		
		line2D = withSt.invoke(line2D, stNew);
		line2D = withEn.invoke(line2D, enNew);
		
		assertEquals(getX.invoke(getSt.invoke(line2D)), xNew);
		assertEquals(getY.invoke(getSt.invoke(line2D)), yNew);
		assertEquals(getX.invoke(getEn.invoke(line2D)), x2New);
		assertEquals(getY.invoke(getEn.invoke(line2D)), y2New);
	
		Field myField = line2DClass.getDeclaredField("st");
		long myFieldOffset = myUnsafe.objectFieldOffset(myField);
		Object value = myUnsafe.getObject(line2D, myFieldOffset);
		if (value != stNew) {
			throw new Error("Different st");
		}
	//	int temp = myUnsafe.getInt(line2D, myFieldOffset);
	//	System.out.println("o " + temp);	
		/*
		Field tempField = point2DClass.getDeclaredField("x");
		long tempOffset = myUnsafe.objectFieldOffset(tempField);
		int temp = myUnsafe.getInt(getSt.invoke(line2D), myFieldOffset);
		System.out.println("temp is " + temp);
		System.out.println("distance is " + (tempOffset = myFieldOffset));
		*/
		
		myField = line2DClass.getDeclaredField("en");
		myFieldOffset = myUnsafe.objectFieldOffset(myField);
		value = myUnsafe.getObject(line2D, myFieldOffset);
		if (value != enNew) {
			throw new Error("Different en");
		}
	}
	
	/*
	 * Test with nested values in reference type
	 * 
	 * value FlattenedLine2D {
	 * 	flattened Point2D st;
	 * 	flattened Point2D en;
	 * }
	 * 
	 */
	@Test(priority=2)
	static public void testCreateFlattenedLine2D() throws Throwable {
		String fields[] = {"st:QPoint2D;:value", "en:QPoint2D;:value"};
		flattenedLine2DClass = ValueTypeGenerator.generateValueClass("FlattenedLine2D", fields);
				
		makeFlattenedLine2D  = lookup.findStatic(flattenedLine2DClass, "makeValueGeneric", MethodType.methodType(flattenedLine2DClass, Object.class, Object.class));
		
		getSt = generateGenericGetter(flattenedLine2DClass, "st");
 		MethodHandle withSt = generateGenericWither(flattenedLine2DClass, "st");
 		getEn = generateGenericGetter(flattenedLine2DClass, "en");
 		MethodHandle withEn = generateGenericWither(flattenedLine2DClass, "en");
 		
		int x = 0xFFEEFFEE;
		int y = 0xAABBAABB;
		int xNew = 0x11223344;
		int yNew = 0x99887766;
		int x2 = 0xCCDDCCDD;
		int y2 = 0xAAFFAAFF;
		int x2New = 0x55337799;
		int y2New = 0x88662244;
		
		Object st = makePoint2D.invoke(x, y);
		Object en = makePoint2D.invoke(x2, y2);
		
		assertEquals(getX.invoke(st), x);
		assertEquals(getY.invoke(st), y);
		assertEquals(getX.invoke(en), x2);
		assertEquals(getY.invoke(en), y2);
		
		Object line2D = makeFlattenedLine2D.invoke(st, en);
		
		assertEquals(getX.invoke(getSt.invoke(line2D)), x);
		assertEquals(getY.invoke(getSt.invoke(line2D)), y);
		assertEquals(getX.invoke(getEn.invoke(line2D)), x2);
		assertEquals(getY.invoke(getEn.invoke(line2D)), y2);
		
		Object stNew = makePoint2D.invoke(xNew, yNew);
		Object enNew = makePoint2D.invoke(x2New, y2New);
		
		line2D = withSt.invoke(line2D, stNew);
		line2D = withEn.invoke(line2D, enNew);
		
		assertEquals(getX.invoke(getSt.invoke(line2D)), xNew);
		assertEquals(getY.invoke(getSt.invoke(line2D)), yNew);
		assertEquals(getX.invoke(getEn.invoke(line2D)), x2New);
		assertEquals(getY.invoke(getEn.invoke(line2D)), y2New);
		
		Field myField = flattenedLine2DClass.getDeclaredField("st");
		long myFieldOffset = myUnsafe.objectFieldOffset(myField);
	        long temp = myFieldOffset;
	
		int stXValue = myUnsafe.getInt(line2D, myFieldOffset);
		if (xNew != stXValue) {
			throw new Error("different x inside st");
		}
		int stYValue = myUnsafe.getInt(line2D, (4 + myFieldOffset));
		if (yNew != stYValue) {
			throw new Error("different y inside st");
		}

		myField = flattenedLine2DClass.getDeclaredField("en");
		myFieldOffset = myUnsafe.objectFieldOffset(myField);
		long temp2 = myFieldOffset;		

		int enXValue = myUnsafe.getInt(line2D, myFieldOffset);
		if (x2New != enXValue) {
			throw new Error("different x inside en");
		}
		int enYValue = myUnsafe.getInt(line2D, (4 + myFieldOffset));
		if (y2New != enYValue) {
			throw new Error("different y inside en");
		}
		//System.out.println(temp2 - temp);
	}

	/*
	 * Test with nested values
	 * 
	 * value InvalidField {
	 * 	flattened Point2D st;
	 * 	flattened Invalid x;
	 * }
	 * 
	 */
	@Test(priority=3)
	static public void testInvalidNestedField() throws Throwable {
		String fields[] = {"st:QPoint2D;:value", "x:QInvalid;:value"};

		try {
			Class<?> invalidField = ValueTypeGenerator.generateValueClass("InvalidField", fields);
			Assert.fail("should throw error. Nested class doesn't exist!");
		} catch (NoClassDefFoundError e) {}
	}
	
	/*
	 * Test with none value Qtype
	 * 
	 * value NoneValueQType {
	 * 	flattened Point2D st;
	 * 	flattened Object o;
	 * }
	 * 
	 */
	@Test(priority=3)
	static public void testNoneValueQTypeAsNestedField() throws Throwable {
		String fields[] = {"st:QPoint2D;:value", "o:Qjava/lang/Object;:value"};
		try {
			Class<?> noneValueQType = ValueTypeGenerator.generateValueClass("NoneValueQType", fields);
			Assert.fail("should throw error. j.l.Object is not a qtype!");
		} catch (IncompatibleClassChangeError e) {}
	}
	
	/*
	 * Test defaultValue with ref type
	 * 
	 * class DefaultValueWithNoneValueType {
	 * 	Object f1;
	 * 	Object f1;
	 * }
	 * 
	 */
	@Test(priority=3)
	static public void testDefaultValueWithNonValueType() throws Throwable {
		String fields[] = {"f1:Ljava/lang/Object;:value", "f2:Ljava/lang/Object;:value"};
		Class<?> defaultValueWithNonValueType = ValueTypeGenerator.generateRefClass("DefaultValueWithNonValueType", fields);
		MethodHandle makeDefaultValueWithNonValueType = lookup.findStatic(defaultValueWithNonValueType, "makeValue", MethodType.methodType(defaultValueWithNonValueType, Object.class, Object.class));
		try {
			makeDefaultValueWithNonValueType.invoke(null, null);
			Assert.fail("should throw error. Default value must be used with ValueType");
		} catch (IncompatibleClassChangeError e) {}
	}
	
	/*
	 * Test withField on non Value Type
	 * 
	 * class TestWithFieldOnNonValueType {
	 *  long longField
	 * }
	 */
	@Test(priority=1)
	static public void testWithFieldOnNonValueType() throws Throwable {
		String fields[] = {"longField:J"};
		Class<?> testWithFieldOnNonValueType = ValueTypeGenerator.generateRefClass("TestWithFieldOnNonValueType", fields);
		MethodHandle withFieldOnNonValueType = lookup.findStatic(testWithFieldOnNonValueType, "testWithFieldOnNonValueType", MethodType.methodType(Object.class));
		try {
			withFieldOnNonValueType.invoke();
			Assert.fail("should throw error. WithField must be used with ValueType");
		} catch (IncompatibleClassChangeError e) {}
	}
	
	/*
	 * Test withField on non Null type
	 * 
	 * class TestWithFieldOnNull {
	 *  long longField
	 * }
	 */
	@Test(priority=1)
	static public void testWithFieldOnNull() throws Throwable {
		String fields[] = {"longField:J"};
		Class<?> testWithFieldOnNull = ValueTypeGenerator.generateRefClass("TestWithFieldOnNull", fields);
		
		MethodHandle withFieldOnNull = lookup.findStatic(testWithFieldOnNull, "testWithFieldOnNull", MethodType.methodType(Object.class));
		try {
			withFieldOnNull.invoke();
			Assert.fail("should throw error. Objectref cannot be null");
		} catch (NullPointerException e) {}
	}
	
	/*
	 * Test withField on non existent class
	 * 
	 * class TestWithFieldOnNonExistentClass {
	 *  long longField
	 * }
	 */
	@Test(priority=1)
	static public void testWithFieldOnNonExistentClass() throws Throwable {
		String fields[] = {"longField:J"};
		Class<?> testWithFieldOnNonExistentClass = ValueTypeGenerator.generateRefClass("TestWithFieldOnNonExistentClass", fields);
		MethodHandle withFieldOnNonExistentClass = lookup.findStatic(testWithFieldOnNonExistentClass, "testWithFieldOnNonExistentClass", MethodType.methodType(Object.class));
		try {
			withFieldOnNonExistentClass.invoke();
			Assert.fail("should throw error. Class does not exist");
		} catch (NoClassDefFoundError e) {}
	}

	/*
	 * TODO: behaviour of the test between two valueTypes will depend on the new spec(not finialized)
	 * 
	 * Test ifacmp on value class
	 * 
	 * class TestIfacmpOnValueClass {}
	 *
	 *
	 * @Test(priority=2)
		 *	static public void TestIfacmpOnValueClass() throws Throwable {
		 *	int x = 0;
		 *	int y = 0;
		 *
		 *	Object valueType = makePoint2D.invoke(x, y);
		 *	Object refType = (Object) x;
		 *
		 *	Assert.assertFalse((valueType == refType), "An identity (==) comparison that contains a valueType should always return false");
		 *
		 *	Assert.assertFalse((refType == valueType), "An identity (==) comparison that contains a valueType should always return false");
		 *
		 *	Assert.assertFalse((valueType == valueType), "An identity (==) comparison that contains a valueType should always return false");
		 *
		 *	Assert.assertTrue((refType == refType), "An identity (==) comparison on the same refType should always return true");
		 *
		 *	Assert.assertTrue((valueType != refType), "An identity (!=) comparison that contains a valueType should always return true");
		 *
		 *	Assert.assertTrue((refType != valueType), "An identity (!=) comparison that contains a valueType should always return true");
		 *
		 *	Assert.assertTrue((valueType != valueType), "An identity (!=) comparison that contains a valueType should always return true");
		 *
		 *	Assert.assertFalse((refType != refType), "An identity (!=) comparison on the same refType should always return false");
		 *	}
		 */	
	
	
	/*
	 * Test valueType Triangle2D with faltten fields
	 * 
	 * 	flattened line2d v1,v2,v3
	 */
	/*
	@Test(priority=3)
	static public void testCreateTriangle2D() throws Throwable {
		String fields[] = {"v1:LFlattenedLine2D;:value", "v2:LFlattenedLine2D;:value", "v3:LFlattenedLine2D;:value"};
		Class triangle2DClass = ValueTypeGenerator.generateValueClass("Triangle2D", fields);
				
		MethodHandle makeTriangle2D = lookup.findStatic(triangle2DClass, "makeValueGeneric", MethodType.methodType(triangle2DClass, flattenedLine2DClass, flattenedLine2DClass, flattenedLine2DClass));
		
		MethodHandle getV1 = generateGetter(triangle2DClass, "v1", flattenedLine2DClass);
 		MethodHandle withV1 = generateWither(triangle2DClass, "v1", flattenedLine2DClass);
 		MethodHandle getV2 = generateGetter(triangle2DClass, "v2", flattenedLine2DClass);
 		MethodHandle withV2 = generateWither(triangle2DClass, "v2", flattenedLine2DClass);
 		MethodHandle getV3 = generateGetter(triangle2DClass, "v3", flattenedLine2DClass);
 		MethodHandle withV3 = generateWither(triangle2DClass, "v3", flattenedLine2DClass);
 		
 		/*
 		 * Todo: xNews and yNews
 		 */
	/*
		int x_st = 0xFFEEFFEE;
		int y_st = 0xAABBAABB;
		int x_en = 0xEEFFEEFF;
		int y_en = 0xBBAABBAA;
		int xNew = 0x11223344;
		int yNew = 0x99887766;
		int x2_st = 0xCCDDCCDD;
		int y2_st = 0xAAFFAAFF;
		int x2_en = 0xDDCCDDCC;
		int y2_en = 0xFFAAFFAA;
		int x2New = 0x55337799;
		int y2New = 0x88662244;
		int x3_st = 0xBBAABBDD;
		int y3_st = 0xEECCEECC;
		int x3_en = 0xCCEECCEE;
		int y3_en = 0xDDAADDAA;
		int x3New = 0x22886644;
		int y3New = 0x77223399;
		
		Object v1 = makeFlattenedLine2D.invoke(makePoint2D.invoke(x_st, y_st), makePoint2D.invoke(x_en, y_en));
		Object v2 = makeFlattenedLine2D.invoke(makePoint2D.invoke(x2_st, y2_st), makePoint2D.invoke(x2_en, y2_en));
		Object v3 = makeFlattenedLine2D.invoke(makePoint2D.invoke(x3_st, y3_st), makePoint2D.invoke(x3_en, y3_en));
		
		assertEquals(getX.invoke(getSt.invoke(v1)), x_st);
		assertEquals(getY.invoke(getSt.invoke(v1)), y_st);
		assertEquals(getX.invoke(getEn.invoke(v1)), x_en);
		assertEquals(getY.invoke(getEn.invoke(v1)), y_en);
		assertEquals(getX.invoke(getSt.invoke(v2)), x2_st);
		assertEquals(getY.invoke(getSt.invoke(v2)), y2_st);
		assertEquals(getX.invoke(getEn.invoke(v2)), x2_en);
		assertEquals(getY.invoke(getEn.invoke(v2)), y2_en);
		assertEquals(getX.invoke(getSt.invoke(v3)), x3_st);
		assertEquals(getY.invoke(getSt.invoke(v3)), y3_st);
		assertEquals(getX.invoke(getEn.invoke(v3)), x3_en);
		assertEquals(getY.invoke(getEn.invoke(v3)), y3_en);
		
		Object triangle2D = makeTriangle2D.invoke(v1, v2, v3);
		
		assertEquals(getX.invoke(getSt.invoke(getV1.invoke(triangle2D))), x_st);
		assertEquals(getY.invoke(getSt.invoke(getV1.invoke(triangle2D))), y_st);
		assertEquals(getX.invoke(getEn.invoke(getV1.invoke(triangle2D))), x_en);
		assertEquals(getY.invoke(getEn.invoke(getV1.invoke(triangle2D))), y_en);
		assertEquals(getX.invoke(getSt.invoke(getV2.invoke(triangle2D))), x2_st);
		assertEquals(getY.invoke(getSt.invoke(getV2.invoke(triangle2D))), y2_st);
		assertEquals(getX.invoke(getEn.invoke(getV2.invoke(triangle2D))), x2_en);
		assertEquals(getY.invoke(getEn.invoke(getV2.invoke(triangle2D))), y2_en);
		assertEquals(getX.invoke(getSt.invoke(getV3.invoke(triangle2D))), x3_st);
		assertEquals(getY.invoke(getSt.invoke(getV3.invoke(triangle2D))), y3_st);
		assertEquals(getX.invoke(getEn.invoke(getV3.invoke(triangle2D))), x3_en);
		assertEquals(getY.invoke(getEn.invoke(getV3.invoke(triangle2D))), y3_en);
		
		Object v1New = makeFlattenedLine2D.invoke(makePoint2D.invoke(xNew_st, yNew_st), makePoint2D.invoke(xNew_en, yNew_en));
		Object v2New = makeFlattenedLine2D.invoke(makePoint2D.invoke(x2New_st, y2New_st), makePoint2D.invoke(x2New_en, y2New_en));
		Object v3New = makeFlattenedLine2D.invoke(makePoint2D.invoke(x3New_st, y3New_st), makePoint2D.invoke(x3New_en, y3New_en));
		
		triangle2D = withV1.invoke(triangle, v1New);
		triangle2D = withV2.invoke(triangle, v2New);
		triangle2D = withV3.invoke(triangle, v3New);
		
		assertEquals(getX.invoke(getSt.invoke(getV1.invoke(triangle2D))), xNew_st);
		assertEquals(getY.invoke(getSt.invoke(getV1.invoke(triangle2D))), yNew_st);
		assertEquals(getX.invoke(getEn.invoke(getV1.invoke(triangle2D))), xNew_en);
		assertEquals(getY.invoke(getEn.invoke(getV1.invoke(triangle2D))), yNew_en);
		assertEquals(getX.invoke(getSt.invoke(getV2.invoke(triangle2D))), x2New_st);
		assertEquals(getY.invoke(getSt.invoke(getV2.invoke(triangle2D))), y2New_st);
		assertEquals(getX.invoke(getEn.invoke(getV2.invoke(triangle2D))), x2New_en);
		assertEquals(getY.invoke(getEn.invoke(getV2.invoke(triangle2D))), y2New_en);
		assertEquals(getX.invoke(getSt.invoke(getV3.invoke(triangle2D))), x3New_st);
		assertEquals(getY.invoke(getSt.invoke(getV3.invoke(triangle2D))), y3New_st);
		assertEquals(getX.invoke(getEn.invoke(getV3.invoke(triangle2D))), x3New_en);
		assertEquals(getY.invoke(getEn.invoke(getV3.invoke(triangle2D))), y3New_en);
		
		/*
		 * TODO: generic ones,change name remove _
		 */
	//}
	
	/*
	 * Create a value type with a long primitive member
	 * 
	 * value ValueLong {
	 * 	long j;
	 * }
	 */
	@Test(priority=1)
	static public void testCreateValueLong() throws Throwable {
		String fields[] = {"j:J"};
		valueLongClass = ValueTypeGenerator.generateValueClass("ValueLong", fields);
		makeValueLong = lookup.findStatic(valueLongClass, "makeValue", MethodType.methodType(valueLongClass, long.class));

		getLong = generateGetter(valueLongClass, "j", long.class);
		withLong = generateWither(valueLongClass, "j", long.class);

		long j = Long.MAX_VALUE;
		long jNew = Long.MIN_VALUE;
		Object valueLong = makeValueLong.invoke(j);
		
		assertEquals(getLong.invoke(valueLong), j);
	
		valueLong = withLong.invoke(valueLong, jNew);
		assertEquals(getLong.invoke(valueLong), jNew);
	
		Field myField = valueLongClass.getDeclaredField("j");
		long myFieldOffset = myUnsafe.objectFieldOffset(myField);
		long value = myUnsafe.getLong(valueLong, myFieldOffset);
		if (value != jNew) {
			throw new Error("Expected " + jNew + " from getLong() but got " + value);
		}
	}	

	/*
	 * Create a value type with a int primitive member
	 * 
	 * value valueInt {
	 * 	int i;
	 * }
	 */
	@Test(priority=1)
	static public void testCreatevalueInt() throws Throwable {
		String fields[] = {"i:I"};
		valueIntClass = ValueTypeGenerator.generateValueClass("valueInt", fields);
		
		makeValueInt = lookup.findStatic(valueIntClass, "makeValue", MethodType.methodType(valueIntClass, int.class));

		getInt = generateGetter(valueIntClass, "i", int.class);
		withInt = generateWither(valueIntClass, "i", int.class);

		int i = Integer.MAX_VALUE;
		int iNew = Integer.MIN_VALUE;
		Object valueInt = makeValueInt.invoke(i);
		
		assertEquals(getInt.invoke(valueInt), i);
	
		valueInt = withInt.invoke(valueInt, iNew);
		assertEquals(getInt.invoke(valueInt), iNew);

		Field myField = valueIntClass.getDeclaredField("i");
		long myFieldOffset = myUnsafe.objectFieldOffset(myField);
		int value = myUnsafe.getInt(valueInt, myFieldOffset);
		if (value != iNew) {
			throw new Error("Expected " + iNew + " from getInt() but got " + value);
		}
	}		
	
	/*
	 * Create a value type with a double primitive member
	 * 
	 * value ValueDouble {
	 * 	double d;
	 * }
	 */
	@Test(priority=1)
	static public void testCreateValueDouble() throws Throwable {
		String fields[] = {"d:D"};
		valueDoubleClass = ValueTypeGenerator.generateValueClass("ValueDouble", fields);
		
		makeValueDouble = lookup.findStatic(valueDoubleClass, "makeValue", MethodType.methodType(valueDoubleClass, double.class));

		getDouble = generateGetter(valueDoubleClass, "d", double.class);
		withDouble = generateWither(valueDoubleClass, "d", double.class);

		double d = Double.MAX_VALUE;
		double dNew = Double.MIN_VALUE;
		Object valueDouble = makeValueDouble.invoke(d);
		
		assertEquals(getDouble.invoke(valueDouble), d);
	
		valueDouble = withDouble.invoke(valueDouble, dNew);
		assertEquals(getDouble.invoke(valueDouble), dNew);

		Field myField = valueDoubleClass.getDeclaredField("d");
		long myFieldOffset = myUnsafe.objectFieldOffset(myField);
		double value = myUnsafe.getDouble(valueDouble, myFieldOffset);
		if (value != dNew) {
			throw new Error("Expected " + dNew + " from getDouble() but got " + value);
		}
	}

	/*
	 * Create a value type with a float primitive member
	 * 
	 * value ValueFloat {
	 * 	float f;
	 * }
	 */
	@Test(priority=1)
	static public void testCreateValueFloat() throws Throwable {
		String fields[] = {"f:F"};
		valueFloatClass = ValueTypeGenerator.generateValueClass("ValueFloat", fields);
		
		makeValueFloat = lookup.findStatic(valueFloatClass, "makeValue", MethodType.methodType(valueFloatClass, float.class));

		getFloat = generateGetter(valueFloatClass, "f", float.class);
		withFloat = generateWither(valueFloatClass, "f", float.class);

		float f = Float.MAX_VALUE;
		float fNew = Float.MIN_VALUE;
		Object valueFloat = makeValueFloat.invoke(f);
		
		assertEquals(getFloat.invoke(valueFloat), f);
	
		valueFloat = withFloat.invoke(valueFloat, fNew);
		assertEquals(getFloat.invoke(valueFloat), fNew);
	
		Field myField = valueFloatClass.getDeclaredField("f");
		long myFieldOffset = myUnsafe.objectFieldOffset(myField);
		float value = myUnsafe.getFloat(valueFloat, myFieldOffset);
		if (value != fNew) {
			throw new Error("Expected " + fNew + " from getFloat() but got " + value);
		}
	}
	
	/*
	 * Create a value type with an Object member
	 * 
	 * value ValueObject {
	 * 	Object val;
	 * }
	 */
	@Test(priority=1)
	static public void testCreateValueObject() throws Throwable {
		String fields[] = {"val:Ljava/lang/Object;:value"};

		valueObjectClass = ValueTypeGenerator.generateValueClass("ValueObject", fields);
		
		makeValueObject = lookup.findStatic(valueObjectClass, "makeValue", MethodType.methodType(valueObjectClass, Object.class));

		Object val = (Object) 0xEEFFEEFF;
		Object valNew = (Object) 0xFFEEFFEE;
		
		getObject = generateGetter(valueObjectClass, "val", Object.class);
		withObject = generateWither(valueObjectClass, "val", Object.class);

		Object valueObject = makeValueObject.invoke(val);
		
		assertEquals(getObject.invoke(valueObject), val);
	
		valueObject = withObject.invoke(valueObject, valNew);
		assertEquals(getObject.invoke(valueObject), valNew);

/*
		MethodHandle getValGeneric = generateGenericGetter(valueObjectClass, "val");
		MethodHandle withValGeneric = generateGenericWither(valueObjectClass, "val");
		
		valueObject = withValGeneric.invoke(valueObject, val);
		assertEquals(getValGeneric.invoke(valueObject), val);
*/		
		Field myField = valueObjectClass.getDeclaredField("val");
		long myFieldOffset = myUnsafe.objectFieldOffset(myField);
		Object value = myUnsafe.getObject(valueObject, myFieldOffset);
		if (value != valNew) {
			throw new Error("different object from getObject()");
		}
	}
	
	/*
	 * Create a assorted value type with long alignment
	 * 
	 *value AssortedValueWithLongAlignment {
	 *	flattened Point2D point;
	 *	flattened Line2D line;
	 *	flattened ValueObject o;
	 *	flattened ValueLong l;
	 *	flattened ValueDouble d;
	 *	flattened ValueInt i;
	 *	flattened Triangle2D tri;
	 * }
	 *
	 */
	/*
	@Test(priority=3)
	static public void testCreateAssortedValueWithLongAlignment() throws Throwable {
		/*
		 * TODO: change val to value
		 */
	/*
		String fields[] = {"point:QPoint2D;:value", "line:QLine2D;:value", "o:QValueObject;:value",
				"l:QValueLong;:value", "d:QValueDouble;:value", "i:QValueInt;:value", "tri:Triangle2D;:value"};

		Class assortedValueClass = ValueTypeGenerator.generateValueClass("AssortedValueWithLongAlignment", fields);
		
		MethodHandle makeAssorted = lookup.findStatic(assortedValueClass, "makeValueGeneric", MethodType.methodType(assortedValueClass, Object.class, Object.class,
				Object.class, Object.class, Object.class, Object.class, Object.class));

		MethodHandle[][] getterAndWither = new MethodHandle[fields.length][2];
		for(String field : fields) {
			//getter
			field = (field.split(":"))[0];
			getterAndWither[i][0] = generateGenericGetter(assortedValueClass, field);
			//wither
			getterAndWither[i][1] = generateGenericWither(assortedValueClass, field);
		}
		
		//create fields
		Object point = makePoint2D.invoke(a1, a2);
		Object line = makeFlattenedLine2D.invoke(b1, b2);
		Object o = makeValueObject.invoke(c1);
		Object l = makeValueLong.invoke(d1);
		Object d = makeValueDouble.invoke(e1);
		Object i = makeValueInt.invoke(f1);
		Object tri = makeTriangle2D.invoke(h1);
		
		
		//point
		int[] pointFields = {a1, a2};
		checkEqualPoint2D(point, pointFields);
		//line
		int[][] lineFields = {{b11, b12}, {b21, b22}};
		checkEqualLine2D(line, lineFields);
		//Object
		assertEquals(getObject.invoke(o), c1);
		//long
		assertEquals(getLong.invoke(l), d1);
		//double
		assertEquals(getDouble.invoke(d), e1);
		//int
		assertEquals(getInt.invoke(i), f1);
		//triangle
		int[][][] triangleFields = {{{F, F}, {f, f}}, {{f, f}, {f, f}}, {{f, f}, {f, f}}};
		checkEqualTriangle2D(tri, triangleFields);
		///////////////////////////
		Object assortedValue = makeAssorted(point, line, o, l, d, i, tri);
		//
		checkEqualPoint2D(getterAndWither[0][0].invoke(assortedValue), pointFields);
		checkEqualLine2D(getterAndWither[1][0].invoke(assortedValue), lineFields);
		assertEquals(getObject.invoke(getterAndWither[2][0].invoke(assortedValue)), c1);
		assertEquals(getLong.invoke(getterAndWither[3][0].invoke(assortedValue)), d1);
		assertEquals(getDouble.invoke(getterAndWither[4][0].invoke(assortedValue)), e1);
		assertEquals(getInt.invoke(getterAndWither[5][0].invoke(assortedValue)), f1);
		checkEqualTriangle2D(getterAndWither[6][0].invoke(assortedValue), h1);
		//
		Object pointNew = makePoint2D.invoke(a1, a2);
		Object lineNew = makeFlattenedLine2D.invoke(b1, b2);
		Object oNew = makeValueObject.invoke(c1);
		Object lNew = makeValueLong.invoke(d1);
		Object dNew = makeValueDouble.invoke(e1);
		Object iNew = makeValueInt.invoke(f1);
		Object triNew = makeTriangle2D.invoke(h1);
		
		assortedValue = getterAndWither[0][1].invoke(assortedValue, pointNew);
		assortedValue = getterAndWither[1][1].invoke(assortedValue, lineNew);
		assortedValue = getterAndWither[2][1].invoke(assortedValue, oNew);
		assortedValue = getterAndWither[3][1].invoke(assortedValue, lNew);
		assortedValue = getterAndWither[4][1].invoke(assortedValue, dNew);
		assortedValue = getterAndWither[5][1].invoke(assortedValue, iNew);
		assortedValue = getterAndWither[6][1].invoke(assortedValue, triNew);
		
		/*
		 * TODO: check equality
		 */
		/*
		//check offset
		for(int i = 0; i < fields.length; i++) {
			String field = (fields[i].split(":"))[0];
			Field myField = assortedValueClass.getDeclaredField(field);
			long myFieldOffset = myUnsafe.objectFieldOffset(myField);
			Object fieldFromOffset = myUnsafe.getObject(assortedValue, myFieldOffset);
			Object fieldFromGetter = getterAndWither[i][0].invoke(assortedValue);
			if (fieldFromOffset != fieldFromGetter) {
				throw new Error("Expected " + i);
			}			
		}
	}*/
	
	
	static MethodHandle generateGetter(Class<?> clazz, String fieldName, Class<?> fieldType) {
		try {
			return lookup.findVirtual(clazz, "get"+fieldName, MethodType.methodType(fieldType));
		} catch (IllegalAccessException | SecurityException | NullPointerException | NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static MethodHandle generateGenericGetter(Class<?> clazz, String fieldName) {
		try {
			return lookup.findVirtual(clazz, "getGeneric"+fieldName, MethodType.methodType(Object.class));
		} catch (IllegalAccessException | SecurityException | NullPointerException | NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static MethodHandle generateSetter(Class clazz, String fieldName, Class fieldType) {
		try {
			return lookup.findVirtual(clazz, "set"+fieldName, MethodType.methodType(void.class, fieldType));
		} catch (IllegalAccessException | SecurityException | NullPointerException | NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static MethodHandle generateGenericSetter(Class clazz, String fieldName) {
		try {
			return lookup.findVirtual(clazz, "setGeneric"+fieldName, MethodType.methodType(void.class, Object.class));
		} catch (IllegalAccessException | SecurityException | NullPointerException | NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static MethodHandle generateWither(Class clazz, String fieldName, Class fieldType) {
		try {
			return lookup.findVirtual(clazz, "with"+fieldName, MethodType.methodType(clazz, fieldType));
		} catch (IllegalAccessException | SecurityException | NullPointerException | NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}

	static MethodHandle generateGenericWither(Class clazz, String fieldName) {
		try {
			return lookup.findVirtual(clazz, "withGeneric"+fieldName, MethodType.methodType(clazz, Object.class));
		} catch (IllegalAccessException | SecurityException | NullPointerException | NoSuchMethodException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static Unsafe getUnsafeInstance() throws IllegalAccessException {
		Field[] staticFields = Unsafe.class.getDeclaredFields();
		for (Field field : staticFields) {
			if (field.getType() == Unsafe.class) {
				field.setAccessible(true);
				return (Unsafe)field.get(Unsafe.class);
				}
			}
		throw new Error("Unable to find an instance of Unsafe");
	}
	
	static Unsafe createUnsafeInstance() {
		try {
			Field[] staticFields = Unsafe.class.getDeclaredFields();
			for (Field field : staticFields) {
				if (field.getType() == Unsafe.class) {
					field.setAccessible(true);
					return (Unsafe)field.get(Unsafe.class);
					}
				}
		} catch(IllegalAccessException e) {
		}
		throw new Error("Unable to find an instance of Unsafe");
		//trivial object
	}
	
	static void checkEqualPoint2D(Object point, int[] positions) throws Throwable {
		if(point == null) {
			Assert.fail("Point Obejct is null!");
		}
		assertEquals(getX.invoke(point), positions[0]);
		assertEquals(getY.invoke(point), positions[1]);
	}
	
	static void checkEqualLine2D(Object line, int[][] positions, boolean flatten) throws Throwable {
		if(line == null) {
			throw new Error("Line2D Obejct is null!");
		}
		if(flatten == true) {
			checkEqualPoint2D(getSt.invoke(line), positions[0]);
			checkEqualPoint2D(getEn.invoke(line), positions[1]);
		} else {
			checkEqualPoint2D(getStGeneric.invoke(line), positions[0]);
			checkEqualPoint2D(getEnGeneric.invoke(line), positions[1]);
		}
	}
	
	static void checkEqualTriangle2D(Object triangle, int[][][] positions) throws Throwable {
		if(triangle == null) {
			throw new Error("Triangle Object is null!");
		}
		checkEqualLine2D(getV1Generic.invoke(triangle), positions[0], true);
		checkEqualLine2D(getV2Generic.invoke(triangle), positions[1], true);		
		checkEqualLine2D(getV3Generic.invoke(triangle), positions[2], true);		
	}
}

