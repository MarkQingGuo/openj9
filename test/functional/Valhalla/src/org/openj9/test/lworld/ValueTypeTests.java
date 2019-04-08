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
import java.lang.reflect.Array;
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
 * 8) export AUTO_DETECT=off
 * 9) export JDK_IMPL=openj9
 * 10) make -f run_configure.mk && make compile && make _sanity
 */

@Test(groups = { "level.sanity" })
public class ValueTypeTests {
	static Lookup lookup = MethodHandles.lookup();
	static Unsafe myUnsafe = null;
	/* point2D */
	static Class point2DClass = null;
	static MethodHandle makePoint2D = null;	
	static MethodHandle getX = null;
	static MethodHandle getY = null;
	/* line2D */
	static Class line2DClass = null;
	static MethodHandle makeLine2D = null;
	static MethodHandle getSt = null;
	static MethodHandle getEn = null;
	/* flattenedLine2D */
	static Class flattenedLine2DClass = null;
	static MethodHandle makeFlattenedLine2D = null;
	static MethodHandle getFlatSt = null;
	static MethodHandle getFlatEn = null;
	static MethodHandle withFlatSt = null;
	static MethodHandle withFlatEn = null;
	/* triangle2D */
	static Class triangle2DClass = null;
	static MethodHandle makeTriangle2D = null;
	static MethodHandle getV1 = null;
	static MethodHandle getV2 = null;
	static MethodHandle getV3 = null;
	/* valueLong */
	static Class valueLongClass = null;
	static MethodHandle makeValueLong = null;
	static MethodHandle getLong = null;
	static MethodHandle withLong = null;
	/* valueInt */
	static Class valueIntClass = null;
	static MethodHandle makeValueInt = null;
	static MethodHandle getInt = null;
	static MethodHandle withInt = null;
	/* valueDouble */
	static Class valueDoubleClass = null;
	static MethodHandle makeValueDouble = null;
	static MethodHandle getDouble = null;
	static MethodHandle withDouble = null;
	/* valueFloat */
	static Class valueFloatClass =null;
	static MethodHandle makeValueFloat = null;
	static MethodHandle getFloat = null;
	static MethodHandle withFloat = null;
	/* valueObject */
	static Class valueObjectClass = null;
	static MethodHandle makeValueObject = null;
	static MethodHandle getObject = null;
	static MethodHandle withObject = null;
	/* largeObject */
	static Class largeObjectValueClass = null;
	static MethodHandle makeLargeObjectValue = null;
	
	static {
		try {
			Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			myUnsafe = (Unsafe) f.get(null);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

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
		withX = generateWither(point2DClass, "x", int.class);
		getY = generateGetter(point2DClass, "y", int.class);
		withY = generateWither(point2DClass, "y", int.class);

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
	}

	/*
	 * Test array of Point2D
	 */
	@Test(priority=2)
	static public void testCreateArrayPoint2D() throws Throwable {
		int x1 = 0xFFEEFFEE;
		int y1 = 0xAABBAABB;
		int x2 = 0x00000011;
		int y2 = 0xABCDEF00;
		
		Object point2D_1 = makePoint2D.invoke(x1, y1);
		Object point2D_2 = makePoint2D.invoke(x2, y2);

		Object arrayObject = Array.newInstance(point2DClass, 3);
		Array.set(arrayObject, 1, point2D_1);
		Array.set(arrayObject, 2, point2D_2);

		Object point2D_1_check = Array.get(arrayObject, 1);
		Object point2D_2_check = Array.get(arrayObject, 2);
		assertEquals(getX.invoke(point2D_1_check), getX.invoke(point2D_1));
		assertEquals(getY.invoke(point2D_1_check), getY.invoke(point2D_1));
		assertEquals(getX.invoke(point2D_2_check), getX.invoke(point2D_2));
		assertEquals(getY.invoke(point2D_2_check), getY.invoke(point2D_2));
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
				
		makeFlattenedLine2D = lookup.findStatic(flattenedLine2DClass, "makeValueGeneric", MethodType.methodType(flattenedLine2DClass, Object.class, Object.class));
		
		getFlatSt = generateGenericGetter(flattenedLine2DClass, "st");
 		withFlatSt = generateGenericWither(flattenedLine2DClass, "st");
 		getFlatEn = generateGenericGetter(flattenedLine2DClass, "en");
 		withFlatEn = generateGenericWither(flattenedLine2DClass, "en");
 		
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

		assertEquals(getX.invoke(getFlatSt.invoke(line2D)), x);
		assertEquals(getY.invoke(getFlatSt.invoke(line2D)), y);
		assertEquals(getX.invoke(getFlatEn.invoke(line2D)), x2);
		assertEquals(getY.invoke(getFlatEn.invoke(line2D)), y2);
		
		Object stNew = makePoint2D.invoke(xNew, yNew);
		Object enNew = makePoint2D.invoke(x2New, y2New);
		
		line2D = withFlatSt.invoke(line2D, stNew);
		line2D = withFlatEn.invoke(line2D, enNew);

		assertEquals(getX.invoke(getFlatSt.invoke(line2D)), xNew);
		assertEquals(getY.invoke(getFlatSt.invoke(line2D)), yNew);
		assertEquals(getX.invoke(getFlatEn.invoke(line2D)), x2New);
		assertEquals(getY.invoke(getFlatEn.invoke(line2D)), y2New);
	}

	/*
	 * Test array of FlattenedLine2D
	 * 
	 * value FlattenedLine2D {
	 * 	flattened Point2D st;
	 * 	flattened Point2D en;
	 * }
	 * 
	 */
	@Test(priority=3)
	static public void testCreateArrayFlattenedLine2D() throws Throwable {
		int x = 0xFFEEFFEE;
		int y = 0xAABBAABB;
		int x2 = 0xCCDDCCDD;
		int y2 = 0xAAFFAAFF;
		int x3 = 0xFFABFFCD;
		int y3 = 0xBBAABBAA;
		int x4 = 0xCCBBAADD;
		int y4 = 0xAABBAACC;

		Object st1 = makePoint2D.invoke(x, y);
		Object en1 = makePoint2D.invoke(x2, y2);
		Object line2D_1 = makeFlattenedLine2D.invoke(st1, en1);		

		Object st2 = makePoint2D.invoke(x3, y3);
		Object en2 = makePoint2D.invoke(x4, y4);
		Object line2D_2 = makeFlattenedLine2D.invoke(st2, en2);

		Object arrayObject = Array.newInstance(flattenedLine2DClass, 3);
		Array.set(arrayObject, 1, line2D_1);
		Array.set(arrayObject, 2, line2D_2);

		Object line2D_1_check = Array.get(arrayObject, 1);
		Object line2D_2_check = Array.get(arrayObject, 2);

		assertEquals(getX.invoke(getFlatSt.invoke(line2D_1_check)), getX.invoke(getFlatSt.invoke(line2D_1)));
		assertEquals(getX.invoke(getFlatSt.invoke(line2D_2_check)), getX.invoke(getFlatSt.invoke(line2D_2)));
		assertEquals(getY.invoke(getFlatSt.invoke(line2D_1_check)), getY.invoke(getFlatSt.invoke(line2D_1)));
		assertEquals(getY.invoke(getFlatSt.invoke(line2D_2_check)), getY.invoke(getFlatSt.invoke(line2D_2)));
		assertEquals(getX.invoke(getFlatEn.invoke(line2D_1_check)), getX.invoke(getFlatEn.invoke(line2D_1)));
		assertEquals(getX.invoke(getFlatEn.invoke(line2D_2_check)), getX.invoke(getFlatEn.invoke(line2D_2)));
		assertEquals(getY.invoke(getFlatEn.invoke(line2D_1_check)), getY.invoke(getFlatEn.invoke(line2D_1)));
		assertEquals(getY.invoke(getFlatEn.invoke(line2D_2_check)), getY.invoke(getFlatEn.invoke(line2D_2)));
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
	 *	@Test(priority=2)
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
	 * Test monitorEnter on valueType
	 * 
	 * class TestMonitorEnterOnValueType {
	 *  long longField
	 * }
	 */
	@Test(priority=2)
	static public void testMonitorEnterOnValueType() throws Throwable {
		int x = 0;
		int y = 0;
		Object valueType = makePoint2D.invoke(x, y);

		String fields[] = {"longField:J"};
		Class<?> testMonitorEnterOnValueType = ValueTypeGenerator.generateRefClass("TestMonitorEnterOnValueType", fields);
		MethodHandle monitorEnterOnValueType = lookup.findStatic(testMonitorEnterOnValueType, "testMonitorEnterOnObject", MethodType.methodType(void.class, Object.class));
		try {
			monitorEnterOnValueType.invoke(valueType);
			Assert.fail("should throw exception. MonitorEnter cannot be used with ValueType");
		} catch (IllegalMonitorStateException e) {}
	}

	/*
	 * Test monitorExit on valueType
	 * 
	 * class TestMonitorExitOnValueType {
	 *  long longField
	 * }
	 */
	@Test(priority=2)
	static public void testMonitorExitOnValueType() throws Throwable {
		int x = 1;
		int y = 1;
		Object valueType = makePoint2D.invoke(x, y);

		String fields[] = {"longField:J"};
		Class<?> testMonitorExitOnValueType = ValueTypeGenerator.generateRefClass("TestMonitorExitOnValueType", fields);
		MethodHandle monitorExitOnValueType = lookup.findStatic(testMonitorExitOnValueType, "testMonitorExitOnObject", MethodType.methodType(void.class, Object.class));
		try {
			monitorExitOnValueType.invoke(valueType);
			Assert.fail("should throw exception. MonitorExit cannot be used with ValueType");
		} catch (IllegalMonitorStateException e) {}
	}

	/*
	 * Test monitorEnter with refType
	 * 
	 * class TestMonitorEnterWithRefType {
	 *  long longField
	 * }
	 */
	@Test(priority=1)
	static public void testMonitorEnterWithRefType() throws Throwable {
		int x = 0;
		Object refType = (Object) x;
		
		String fields[] = {"longField:J"};
		Class<?> testMonitorEnterWithRefType = ValueTypeGenerator.generateRefClass("TestMonitorEnterWithRefType", fields);
		MethodHandle monitorEnterWithRefType = lookup.findStatic(testMonitorEnterWithRefType, "testMonitorEnterOnObject", MethodType.methodType(void.class, Object.class));
		try {
			monitorEnterWithRefType.invoke(refType);
		} catch (IllegalMonitorStateException e) { 
			Assert.fail("shouldn't throw exception. MonitorEnter should be used with refType");
		}
	}

	/*
	 * Test monitorExit with refType
	 * 
	 * class TestMonitorExitWithRefType {
	 *  long longField
	 * }
	 */
	@Test(priority=1)
	static public void testMonitorExitWithRefType() throws Throwable {
		int x = 1;
		Object refType = (Object) x;
		
		String fields[] = {"longField:J"};
		Class<?> testMonitorExitWithRefType = ValueTypeGenerator.generateRefClass("TestMonitorExitWithRefType", fields);
		MethodHandle monitorExitWithRefType = lookup.findStatic(testMonitorExitWithRefType, "testMonitorExitOnObject", MethodType.methodType(void.class, Object.class));
		try {
			monitorExitWithRefType.invoke(refType);
			Assert.fail("should throw exception. MonitorExit doesn't have a matching MonitorEnter");
		} catch (IllegalMonitorStateException e) {}
	}

	/*
	 * Test monitorEnterAndExit with refType
	 * 
	 * class TestMonitorEnterAndExitWithRefType {
	 *  long longField
	 * }
	 */
	@Test(priority=1)
	static public void testMonitorEnterAndExitWithRefType() throws Throwable {
		int x = 2;
		Object refType = (Object) x;
		
		String fields[] = {"longField:J"};
		Class<?> testMonitorEnterAndExitWithRefType = ValueTypeGenerator.generateRefClass("TestMonitorEnterAndExitWithRefType", fields);
		MethodHandle monitorEnterAndExitWithRefType = lookup.findStatic(testMonitorEnterAndExitWithRefType, "testMonitorEnterAndExitWithRefType", MethodType.methodType(void.class, Object.class));
		try {
			monitorEnterAndExitWithRefType.invoke(refType);
		} catch (IllegalMonitorStateException e) {
			Assert.fail("shouldn't throw exception. MonitorEnter and MonitorExit should be used with refType");
		}
	}
	
	/*	
	 * Create a valueType with three valueType members
	 * 
	 * value Triangle2D {
	 *  flattened Line2D v1;
	 *  flattened Line2D v2;
	 *  flattened Line2D v3;
	 * }
	 */
	@Test(priority = 3)
	static public void testCreateTriangle2D() throws Throwable {
		String fields[] = { "v1:QFlattenedLine2D;:value", "v2:QFlattenedLine2D;:value", "v3:QFlattenedLine2D;:value" };
		triangle2DClass = ValueTypeGenerator.generateValueClass("Triangle2D", fields);

		makeTriangle2D = lookup.findStatic(triangle2DClass, "makeValueGeneric",
				MethodType.methodType(triangle2DClass, Object.class, Object.class, Object.class));

		getV1 = generateGenericGetter(triangle2DClass, "v1");
		MethodHandle withV1 = generateGenericWither(triangle2DClass, "v1");
		getV2 = generateGenericGetter(triangle2DClass, "v2");
		MethodHandle withV2 = generateGenericWither(triangle2DClass, "v2");
		getV3 = generateGenericGetter(triangle2DClass, "v3");
		MethodHandle withV3 = generateGenericWither(triangle2DClass, "v3");

		int[][] v1Positions = { { 0xFFEEFFEE, 0xAABBAABB }, { 0xEEFFEEFF, 0xBBAABBAA } };
		int[][] v2Positions = { { 0xCCDDCCDD, 0xAAFFAAFF }, { 0xDDCCDDCC, 0xFFAAFFAA } };
		int[][] v3Positions = { { 0xBBAABBDD, 0xEECCEECC }, { 0xCCEECCEE, 0xDDAADDAA } };
		int[][][] positions = { v1Positions, v2Positions, v3Positions };

		Object v1 = makeFlattenedLine2D.invoke(makePoint2D.invoke(v1Positions[0][0], v1Positions[0][1]),
				makePoint2D.invoke(v1Positions[1][0], v1Positions[1][1]));
		Object v2 = makeFlattenedLine2D.invoke(makePoint2D.invoke(v2Positions[0][0], v2Positions[0][1]),
				makePoint2D.invoke(v2Positions[1][0], v2Positions[1][1]));
		Object v3 = makeFlattenedLine2D.invoke(makePoint2D.invoke(v3Positions[0][0], v3Positions[0][1]),
				makePoint2D.invoke(v3Positions[1][0], v3Positions[1][1]));

		checkEqualLine2D(v1, v1Positions, true);
		checkEqualLine2D(v2, v2Positions, true);
		checkEqualLine2D(v3, v3Positions, true);

		Object triangle2D = makeTriangle2D.invoke(v1, v2, v3);

		checkEqualTriangle2D(triangle2D, positions);

		int[][] v1PositionsNew = { { 0xFFEEAABB, 0xAABBCCDD }, { 0xEEFFAABB, 0xBBAADDCC } };
		int[][] v2PositionsNew = { { 0xCCDDAABB, 0xAAFFEEEE }, { 0xDDCCDDEE, 0xFFAAAACC } };
		int[][] v3PositionsNew = { { 0xBBAABBEE, 0xEECCEEAA }, { 0xCCEECCDD, 0xDDAADDCC } };
		int[][][] positionsNew = { v1PositionsNew, v2PositionsNew, v3PositionsNew };

		Object v1New = makeFlattenedLine2D.invoke(makePoint2D.invoke(v1PositionsNew[0][0], v1PositionsNew[0][1]),
				makePoint2D.invoke(v1PositionsNew[1][0], v1PositionsNew[1][1]));
		Object v2New = makeFlattenedLine2D.invoke(makePoint2D.invoke(v2PositionsNew[0][0], v2PositionsNew[0][1]),
				makePoint2D.invoke(v2PositionsNew[1][0], v2PositionsNew[1][1]));
		Object v3New = makeFlattenedLine2D.invoke(makePoint2D.invoke(v3PositionsNew[0][0], v3PositionsNew[0][1]),
				makePoint2D.invoke(v3PositionsNew[1][0], v3PositionsNew[1][1]));

		triangle2D = withV1.invoke(triangle2D, v1New);
		triangle2D = withV2.invoke(triangle2D, v2New);
		triangle2D = withV3.invoke(triangle2D, v3New);

		checkEqualTriangle2D(triangle2D, positionsNew);
	}

	/*
	 * Create a value type with a long primitive member
	 * 
	 * value ValueLong {
	 * 	long j;
	 * }
	 */
	@Test(priority = 1)
	static public void testCreateValueLong() throws Throwable {
		String fields[] = { "j:J" };
		valueLongClass = ValueTypeGenerator.generateValueClass("ValueLong", fields);
		makeValueLong = lookup.findStatic(valueLongClass, "makeValue",
				MethodType.methodType(valueLongClass, long.class));

		getLong = generateGetter(valueLongClass, "j", long.class);
		withLong = generateWither(valueLongClass, "j", long.class);

		long j = Long.MAX_VALUE;
		long jNew = Long.MIN_VALUE;
		Object valueLong = makeValueLong.invoke(j);

		assertEquals(getLong.invoke(valueLong), j);

		valueLong = withLong.invoke(valueLong, jNew);
		assertEquals(getLong.invoke(valueLong), jNew);
	}

	/*
	 * Create a value type with a int primitive member
	 * 
	 * value valueInt {
	 * 	int i;
	 * }
	 */
	@Test(priority = 1)
	static public void testCreatevalueInt() throws Throwable {
		String fields[] = { "i:I" };
		valueIntClass = ValueTypeGenerator.generateValueClass("ValueInt", fields);

		makeValueInt = lookup.findStatic(valueIntClass, "makeValue", MethodType.methodType(valueIntClass, int.class));

		getInt = generateGetter(valueIntClass, "i", int.class);
		withInt = generateWither(valueIntClass, "i", int.class);

		int i = Integer.MAX_VALUE;
		int iNew = Integer.MIN_VALUE;
		Object valueInt = makeValueInt.invoke(i);

		assertEquals(getInt.invoke(valueInt), i);

		valueInt = withInt.invoke(valueInt, iNew);
		assertEquals(getInt.invoke(valueInt), iNew);
	}

	/*
	 * Create a value type with a double primitive member
	 * 
	 * value ValueDouble {
	 * 	double d;
	 * }
	 */
	@Test(priority = 1)
	static public void testCreateValueDouble() throws Throwable {
		String fields[] = { "d:D" };
		valueDoubleClass = ValueTypeGenerator.generateValueClass("ValueDouble", fields);

		makeValueDouble = lookup.findStatic(valueDoubleClass, "makeValue",
				MethodType.methodType(valueDoubleClass, double.class));

		getDouble = generateGetter(valueDoubleClass, "d", double.class);
		withDouble = generateWither(valueDoubleClass, "d", double.class);

		double d = Double.MAX_VALUE;
		double dNew = Double.MIN_VALUE;
		Object valueDouble = makeValueDouble.invoke(d);

		assertEquals(getDouble.invoke(valueDouble), d);

		valueDouble = withDouble.invoke(valueDouble, dNew);
		assertEquals(getDouble.invoke(valueDouble), dNew);
	}

	/*
	 * Create a value type with a float primitive member
	 * 
	 * value ValueFloat {
	 * 	float f;
	 * }
	 */
	@Test(priority = 1)
	static public void testCreateValueFloat() throws Throwable {
		String fields[] = { "f:F" };
		valueFloatClass = ValueTypeGenerator.generateValueClass("ValueFloat", fields);

		makeValueFloat = lookup.findStatic(valueFloatClass, "makeValue",
				MethodType.methodType(valueFloatClass, float.class));

		getFloat = generateGetter(valueFloatClass, "f", float.class);
		withFloat = generateWither(valueFloatClass, "f", float.class);

		float f = Float.MAX_VALUE;
		float fNew = Float.MIN_VALUE;
		Object valueFloat = makeValueFloat.invoke(f);

		assertEquals(getFloat.invoke(valueFloat), f);

		valueFloat = withFloat.invoke(valueFloat, fNew);
		assertEquals(getFloat.invoke(valueFloat), fNew);
	}

	/*
	 * Create a value type with a reference member
	 * 
	 * value ValueObject {
	 * 	Object val;
	 * }
	 */
	@Test(priority = 1)
	static public void testCreateValueObject() throws Throwable {
		String fields[] = { "val:Ljava/lang/Object;:value" };

		valueObjectClass = ValueTypeGenerator.generateValueClass("ValueObject", fields);

		makeValueObject = lookup.findStatic(valueObjectClass, "makeValue",
				MethodType.methodType(valueObjectClass, Object.class));

		Object val = (Object)0xEEFFEEFF;
		Object valNew = (Object)0xFFEEFFEE;

		getObject = generateGetter(valueObjectClass, "val", Object.class);
		withObject = generateWither(valueObjectClass, "val", Object.class);

		Object valueObject = makeValueObject.invoke(val);

		assertEquals(getObject.invoke(valueObject), val);

		valueObject = withObject.invoke(valueObject, valNew);
		assertEquals(getObject.invoke(valueObject), valNew);
	}

	/*
	 * Create an assorted value type with long alignment
	 * 
	 * value AssortedValueWithLongAlignment {
	 *	flattened Point2D point;
	 *	flattened Line2D line;
	 *	flattened ValueObject o;
	 *	flattened ValueLong l;
	 *	flattened ValueDouble d;
	 *	flattened ValueInt i;
	 *	flattened Triangle2D tri;
	 * }
	 */
	@Test(priority = 4)
	static public void testCreateAssortedValueWithLongAlignment() throws Throwable {
		String fields[] = {
				"point:QPoint2D;:value",
				"line:QFlattenedLine2D;:value",
				"o:QValueObject;:value",
				"l:QValueLong;:value",
				"d:QValueDouble;:value",
				"i:QValueInt;:value",
				"tri:QTriangle2D;:value" };
		Class assortedValueWithLongAlignmentClass = ValueTypeGenerator
				.generateValueClass("AssortedValueWithLongAlignment", fields);

		MethodHandle makeAssortedValueWithLongAlignment = lookup.findStatic(assortedValueWithLongAlignmentClass,
				"makeValueGeneric", MethodType.methodType(assortedValueWithLongAlignmentClass, Object.class,
						Object.class, Object.class, Object.class, Object.class, Object.class, Object.class));

		/*
		 * Getters are created in array getterAndWither[i][0] according to the order of fields i
		 * Withers are created in array getterAndWither[i][1] according to the order of fields i
		 */
		MethodHandle[][] getterAndWither = generateGenericGetterAndWither(assortedValueWithLongAlignmentClass, fields);

		/* point */
		int[] pointFields = { 0xFFEEFFEE, 0xAABBAABB };
		Object point = makePoint2D.invoke(pointFields[0], pointFields[1]);
		checkEqualPoint2D(point, pointFields);

		/* line */
		int[][] lineFields = { { 0xFFEEFFEE, 0xAABBAABB }, { 0xCCAFFAAA, 0xBBAAAACC } };
		Object line = createFlattenedLine2D(lineFields);
		checkEqualLine2D(line, lineFields, true);

		/* triangle */
		int[][][] triangleFields = {
				{ { 0xAADDAADD, 0xAACCAACC }, { 0xEEAAEEEE, 0xAAAAAAAA } },
				{ { 0xBBBCCCAA, 0xEEAAEEEA }, { 0xFFFFFAAA, 0xFFFFFAAA } },
				{ { 0xCCCCCCCA, 0xEEEEEEEC }, { 0xAAAAADDD, 0xDDDDBBBB } } };
		Object tri = createTriangle2D(triangleFields);
		checkEqualTriangle2D(tri, triangleFields);

		/* o */
		Object oValue = (Object)0xEEFAEEFF;
		Object o = makeValueObject.invoke(oValue);
		assertEquals(getObject.invoke(o), oValue);

		/* l */
		long lValue = 2121212121L;
		Object l = makeValueLong.invoke(lValue);
		assertEquals(getLong.invoke(l), lValue);

		/* d */
		double dValue = 123.43555;
		Object d = makeValueDouble.invoke(dValue);
		assertEquals(getDouble.invoke(d), dValue);

		/* i */
		int iValue = 0xABCDDCBA;
		Object i = makeValueInt.invoke(iValue);
		assertEquals(getInt.invoke(i), iValue);

		Object assortedValueWithLongAlignment = makeAssortedValueWithLongAlignment.invoke(point, line, o, l, d, i, tri);

		checkEqualPoint2D(getterAndWither[0][0].invoke(assortedValueWithLongAlignment), pointFields);
		checkEqualLine2D(getterAndWither[1][0].invoke(assortedValueWithLongAlignment), lineFields, true);
		assertEquals(getObject.invoke(getterAndWither[2][0].invoke(assortedValueWithLongAlignment)), oValue);
		assertEquals(getLong.invoke(getterAndWither[3][0].invoke(assortedValueWithLongAlignment)), lValue);
		assertEquals(getDouble.invoke(getterAndWither[4][0].invoke(assortedValueWithLongAlignment)), dValue);
		assertEquals(getInt.invoke(getterAndWither[5][0].invoke(assortedValueWithLongAlignment)), iValue);
		checkEqualTriangle2D(getterAndWither[6][0].invoke(assortedValueWithLongAlignment), triangleFields);

		/* point */
		int[] pointFieldsNew = { 0xFFEEFFAA, 0xFFEEFFAA };
		Object pointNew = makePoint2D.invoke(pointFieldsNew[0], pointFieldsNew[1]);
		
		/* line */
		int[][] lineFieldsNew = { { 0x22EE22EE, 0x11441144 }, { 0x33122111, 0x44111133 } };
		Object lineNew = createFlattenedLine2D(lineFieldsNew);
		
		/* triangle */
		int[][][] triangleFieldsNew = {
				{ { 0x11DD11DD, 0x11331133 }, { 0xEE11EEEE, 0x11111111 } },
				{ { 0x44433311, 0xEE11EEE1 }, { 0x22222111, 0x11111113 } },
				{ { 0x33333331, 0xEEEEEEE3 }, { 0x11111DDD, 0xDDDD4444 } } };
		Object triNew = createTriangle2D(triangleFieldsNew);
		
		/* o */
		Object oValueNew = (Object)0xEECAEEFF;
		Object oNew = makeValueObject.invoke(oValueNew);
		
		/* l */
		long lValueNew = 2133213321L;
		Object lNew = makeValueLong.invoke(lValueNew);
		
		/* d */
		double dValueNew = 123.42222;
		Object dNew = makeValueDouble.invoke(dValueNew);
		
		/* i */
		int iValueNew = 0x123DDCBA;
		Object iNew = makeValueInt.invoke(iValueNew);

		assortedValueWithLongAlignment = getterAndWither[0][1].invoke(assortedValueWithLongAlignment, pointNew);
		assortedValueWithLongAlignment = getterAndWither[1][1].invoke(assortedValueWithLongAlignment, lineNew);
		assortedValueWithLongAlignment = getterAndWither[2][1].invoke(assortedValueWithLongAlignment, oNew);
		assortedValueWithLongAlignment = getterAndWither[3][1].invoke(assortedValueWithLongAlignment, lNew);
		assortedValueWithLongAlignment = getterAndWither[4][1].invoke(assortedValueWithLongAlignment, dNew);
		assortedValueWithLongAlignment = getterAndWither[5][1].invoke(assortedValueWithLongAlignment, iNew);
		assortedValueWithLongAlignment = getterAndWither[6][1].invoke(assortedValueWithLongAlignment, triNew);

		checkEqualPoint2D(getterAndWither[0][0].invoke(assortedValueWithLongAlignment), pointFieldsNew);

		checkEqualLine2D(getterAndWither[1][0].invoke(assortedValueWithLongAlignment), lineFieldsNew, true);
		assertEquals(getObject.invoke(getterAndWither[2][0].invoke(assortedValueWithLongAlignment)), oValueNew);
		assertEquals(getLong.invoke(getterAndWither[3][0].invoke(assortedValueWithLongAlignment)), lValueNew);
		assertEquals(getDouble.invoke(getterAndWither[4][0].invoke(assortedValueWithLongAlignment)), dValueNew);
		assertEquals(getInt.invoke(getterAndWither[5][0].invoke(assortedValueWithLongAlignment)), iValueNew);

		checkEqualTriangle2D(getterAndWither[6][0].invoke(assortedValueWithLongAlignment), triangleFieldsNew);
	}

	/*
	 * Create an assorted refType with long alignment
	 * 
	 * class AssortedReftWithLongAlignment {
	 *	flattened Point2D point;
	 *	flattened Line2D line;
	 *	flattened ValueObject o;
	 *	flattened ValueLong l;
	 *	flattened ValueDouble d;
	 *	flattened ValueInt i;
	 *	flattened Triangle2D tri;
	 * }
	 */
	@Test(priority = 4)
	static public void testCreateAssortedRefWithLongAlignment() throws Throwable {
		String fields[] = {
				"point:QPoint2D;:value",
				"line:QFlattenedLine2D;:value",
				"o:QValueObject;:value",
				"l:QValueLong;:value",
				"d:QValueDouble;:value",
				"i:QValueInt;:value",
				"tri:QTriangle2D;:value" };
		Class assortedRefWithLongAlignmentClass = ValueTypeGenerator.generateRefClass("AssortedRefWithLongAlignment", fields);

		MethodHandle makeAssortedRefWithLongAlignment = lookup.findStatic(assortedRefWithLongAlignmentClass, "makeRefGeneric",
				MethodType.methodType(assortedRefWithLongAlignmentClass, Object.class, Object.class, Object.class, Object.class,
						Object.class, Object.class, Object.class));

		/*
		 * Getters are created in array getterAndSetter[i][0] according to the order of fields i
		 * Setters are created in array getterAndSetter[i][1] according to the order of fields i
		 */
		MethodHandle[][] getterAndSetter = generateGenericGetterAndSetter(assortedRefWithLongAlignmentClass, fields);

		/* point */
		int[] pointFields = { 0xFFEEFFEE, 0xAABBAABB };
		Object point = makePoint2D.invoke(pointFields[0], pointFields[1]);
		checkEqualPoint2D(point, pointFields);

		/* line */
		int[][] lineFields = { { 0xFFEEFFEE, 0xAABBAABB }, { 0xCCAFFAAA, 0xBBAAAACC } };
		Object line = createFlattenedLine2D(lineFields);
		checkEqualLine2D(line, lineFields, true);
		
		/* triangle */
		int[][][] triangleFields = {
				{ { 0xAADDAADD, 0xAACCAACC }, { 0xEEAAEEEE, 0xAAAAAAAA } },
				{ { 0xBBBCCCAA, 0xEEAAEEEA }, { 0xFFFFFAAA, 0xFFFFFAAA } },
				{ { 0xCCCCCCCA, 0xEEEEEEEC }, { 0xAAAAADDD, 0xDDDDBBBB } } };
		Object tri = createTriangle2D(triangleFields);
		checkEqualTriangle2D(tri, triangleFields);
		
		/* o */
		Object oValue = (Object)0xEEFAEEFF;
		Object o = makeValueObject.invoke(oValue);
		assertEquals(getObject.invoke(o), oValue);
		
		/* l */
		long lValue = 2121212121L;
		Object l = makeValueLong.invoke(lValue);
		assertEquals(getLong.invoke(l), lValue);
		
		/* d */
		double dValue = 123.43555;
		Object d = makeValueDouble.invoke(dValue);
		assertEquals(getDouble.invoke(d), dValue);
		
		/* i */
		int iValue = 0xABCDDCBA;
		Object i = makeValueInt.invoke(iValue);
		assertEquals(getInt.invoke(i), iValue);

		Object assortedRefWithLongAlignment = makeAssortedRefWithLongAlignment.invoke(point, line, o, l, d, i, tri);

		checkEqualPoint2D(getterAndSetter[0][0].invoke(assortedRefWithLongAlignment), pointFields);
		checkEqualLine2D(getterAndSetter[1][0].invoke(assortedRefWithLongAlignment), lineFields, true);
		assertEquals(getObject.invoke(getterAndSetter[2][0].invoke(assortedRefWithLongAlignment)), oValue);
		assertEquals(getLong.invoke(getterAndSetter[3][0].invoke(assortedRefWithLongAlignment)), lValue);
		assertEquals(getDouble.invoke(getterAndSetter[4][0].invoke(assortedRefWithLongAlignment)), dValue);
		assertEquals(getInt.invoke(getterAndSetter[5][0].invoke(assortedRefWithLongAlignment)), iValue);
		checkEqualTriangle2D(getterAndSetter[6][0].invoke(assortedRefWithLongAlignment), triangleFields);

		/* point */
		int[] pointFieldsNew = { 0xFFEEFFAA, 0xFFEEFFAA };
		Object pointNew = makePoint2D.invoke(pointFieldsNew[0], pointFieldsNew[1]);
		
		/* line */
		int[][] lineFieldsNew = { { 0x22EE22EE, 0x11441144 }, { 0x33122111, 0x44111133 } };
		Object lineNew = createFlattenedLine2D(lineFieldsNew);
		
		/* triangle */
		int[][][] triangleFieldsNew = {
				{ { 0x11DD11DD, 0x11331133 }, { 0xEE11EEEE, 0x11111111 } },
				{ { 0x44433311, 0xEE11EEE1 }, { 0x22222111, 0x11111113 } },
				{ { 0x33333331, 0xEEEEEEE3 }, { 0x11111DDD, 0xDDDD4444 } } };
		Object triNew = createTriangle2D(triangleFieldsNew);
		
		/* o */
		Object oValueNew = (Object)0xEECAEEFF;
		Object oNew = makeValueObject.invoke(oValueNew);
		
		/* l */
		long lValueNew = 2133213321L;
		Object lNew = makeValueLong.invoke(lValueNew);
		
		/* d */
		double dValueNew = 123.42222;
		Object dNew = makeValueDouble.invoke(dValueNew);
		
		/* i */
		int iValueNew = 0x123DDCBA;
		Object iNew = makeValueInt.invoke(iValueNew);

		getterAndSetter[0][1].invoke(assortedRefWithLongAlignment, pointNew);
		getterAndSetter[1][1].invoke(assortedRefWithLongAlignment, lineNew);
		getterAndSetter[2][1].invoke(assortedRefWithLongAlignment, oNew);
		getterAndSetter[3][1].invoke(assortedRefWithLongAlignment, lNew);
		getterAndSetter[4][1].invoke(assortedRefWithLongAlignment, dNew);
		getterAndSetter[5][1].invoke(assortedRefWithLongAlignment, iNew);
		getterAndSetter[6][1].invoke(assortedRefWithLongAlignment, triNew);

		checkEqualPoint2D(getterAndSetter[0][0].invoke(assortedRefWithLongAlignment), pointFieldsNew);

		checkEqualLine2D(getterAndSetter[1][0].invoke(assortedRefWithLongAlignment), lineFieldsNew, true);
		assertEquals(getObject.invoke(getterAndSetter[2][0].invoke(assortedRefWithLongAlignment)), oValueNew);
		assertEquals(getLong.invoke(getterAndSetter[3][0].invoke(assortedRefWithLongAlignment)), lValueNew);
		assertEquals(getDouble.invoke(getterAndSetter[4][0].invoke(assortedRefWithLongAlignment)), dValueNew);
		assertEquals(getInt.invoke(getterAndSetter[5][0].invoke(assortedRefWithLongAlignment)), iValueNew);
		checkEqualTriangle2D(getterAndSetter[6][0].invoke(assortedRefWithLongAlignment), triangleFieldsNew);
	}

	/*
	 * Create an assorted value type with object alignment 
	 * 
	 * value AssortedValueWithObjectAlignment {
	 * 	flattened Triangle2D tri;
	 * 	flattened Point2D point;
	 * 	flattened Line2D line;
	 * 	flattened ValueObject o;
	 * 	flattened ValueInt i;
	 * 	flattened ValFloat f;
	 * 	flattened Triangle2D tri2;
	 * }
	 */
	@Test(priority = 4)
	static public void testCreateAssortedValueWithObjectAlignment() throws Throwable {
		String fields[] = {
				"tri:QTriangle2D;:value",
				"point:QPoint2D;:value",
				"line:QFlattenedLine2D;:value",
				"o:QValueObject;:value",
				"i:QValueInt;:value",
				"f:QValueFloat;:value",
				"tri2:QTriangle2D;:value" };
		Class assortedValueWithObjectAlignmentClass = ValueTypeGenerator.generateValueClass("AssortedValueWithObjectAlignment", fields);

		MethodHandle makeAssortedValueWithObjectAlignment = lookup.findStatic(assortedValueWithObjectAlignmentClass, "makeValueGeneric",
				MethodType.methodType(assortedValueWithObjectAlignmentClass, Object.class, Object.class, Object.class, Object.class,
						Object.class, Object.class, Object.class));
		/*
		 * Getters are created in array getterAndSetter[i][0] according to the order of fields i
		 * Setters are created in array getterAndSetter[i][1] according to the order of fields i
		 */
		MethodHandle[][] getterAndWither = generateGenericGetterAndWither(assortedValueWithObjectAlignmentClass, fields);

		/* tri1 */
		int[][][] triangleFields = {
				{ { 0xAADDAADD, 0xAACCAACC }, { 0xEEAAEEEE, 0xAAAAAAAA } },
				{ { 0xBBBCCCAA, 0xEEAAEEEA }, { 0xFFFFFAAA, 0xFFFFFAAA } },
				{ { 0xCCCCCCCA, 0xEEEEEEEC }, { 0xAAAAADDD, 0xDDDDBBBB } } };
		Object tri = createTriangle2D(triangleFields);
		checkEqualTriangle2D(tri, triangleFields);
		
		/* point */
		int[] pointFields = { 0xFFEEFFEE, 0xAABBAABB };
		Object point = makePoint2D.invoke(pointFields[0], pointFields[1]);
		checkEqualPoint2D(point, pointFields);

		/* line */
		int[][] lineFields = { { 0xFFEEFFEE, 0xAABBAABB }, { 0xCCAFFAAA, 0xBBAAAACC } };
		Object line = createFlattenedLine2D(lineFields);
		checkEqualLine2D(line, lineFields, true);
		
		/* tri2 */
		int[][][] triangle2Fields = {
				{ { 0xAADDAADD, 0xAACCAACC }, { 0xEEAAEEEE, 0xAAAAAAAA } },
				{ { 0xBBBBBBAA, 0xEEAAEEEA }, { 0xFFFFFAAA, 0xFFFFFAAA } },
				{ { 0xCCAACCCA, 0xEE22EEEC }, { 0xAAAAADDD, 0x1111BBBB } } };
		Object tri2 = createTriangle2D(triangle2Fields);
		checkEqualTriangle2D(tri2, triangle2Fields);
		
		/* o */
		Object oValue = (Object)0xEEFAEEFF;
		Object o = makeValueObject.invoke(oValue);
		assertEquals(getObject.invoke(o), oValue);
		
		/* f */
		float fValue = Float.MAX_VALUE;
		Object f = makeValueFloat.invoke(fValue);
		assertEquals(getFloat.invoke(f), fValue);

		/* i */
		int iValue = 0xABCDDCBA;
		Object i = makeValueInt.invoke(iValue);
		assertEquals(getInt.invoke(i), iValue);

		Object assortedValueWithObjectAlignment = makeAssortedValueWithObjectAlignment.invoke(tri, point, line, o, i, f, tri2);

		checkEqualTriangle2D(getterAndWither[0][0].invoke(assortedValueWithObjectAlignment), triangleFields);
		checkEqualPoint2D(getterAndWither[1][0].invoke(assortedValueWithObjectAlignment), pointFields);
		checkEqualLine2D(getterAndWither[2][0].invoke(assortedValueWithObjectAlignment), lineFields, true);
		assertEquals(getObject.invoke(getterAndWither[3][0].invoke(assortedValueWithObjectAlignment)), oValue);
		assertEquals(getInt.invoke(getterAndWither[4][0].invoke(assortedValueWithObjectAlignment)), iValue);
		assertEquals(getFloat.invoke(getterAndWither[5][0].invoke(assortedValueWithObjectAlignment)), fValue);
		checkEqualTriangle2D(getterAndWither[6][0].invoke(assortedValueWithObjectAlignment), triangle2Fields);
		
		/* point */
		int[] pointFieldsNew = { 0xFFEEFFAA, 0xAABBAADD };
		Object pointNew = makePoint2D.invoke(pointFieldsNew[0], pointFieldsNew[1]);
		
		/* line */
		int[][] lineFieldsNew = { { 0x22EE22EE, 0x11441144 }, { 0x33122111, 0x44111133 } };
		Object lineNew = createFlattenedLine2D(lineFieldsNew);
		
		/* tri1 */
		int[][][] triangleFieldsNew = {
				{ { 0x11DD11DD, 0x11331133 }, { 0xEE11AAEE, 0x11111111 } },
				{ { 0x44433311, 0xEE1144E1 }, { 0x11222111, 0x11111113 } },
				{ { 0x33333331, 0xEEEE2EE3 }, { 0x11111DDD, 0xDDDD4444 } } };
		Object tri1New = createTriangle2D(triangleFieldsNew);
		
		/* o */
		Object oValueNew = (Object)0xEECAEEFF;
		Object oNew = makeValueObject.invoke(oValueNew);
		
		/* f */
		float fValueNew = 5.356f;
		Object fNew = makeValueFloat.invoke(fValueNew);
		
		/* i */
		int iValueNew = 0x123DDCBA;
		Object iNew = makeValueInt.invoke(iValueNew);
		
		/* tri2 */
		int[][][] triangle2FieldsNew = {
				{ { 0x11DD11DD, 0x11331133 }, { 0xEE11AAEE, 0xBBB11111 } },
				{ { 0x44433311, 0xEE114421 }, { 0x11222111, 0x2222A113 } },
				{ { 0x33333331, 0xEEEE2CC3 }, { 0x11111DDD, 0xDDDD3444 } } };
		Object tri2New = createTriangle2D(triangle2FieldsNew);

		assortedValueWithObjectAlignment = getterAndWither[0][1].invoke(assortedValueWithObjectAlignment, tri1New);
		assortedValueWithObjectAlignment = getterAndWither[1][1].invoke(assortedValueWithObjectAlignment, pointNew);
		assortedValueWithObjectAlignment = getterAndWither[2][1].invoke(assortedValueWithObjectAlignment, lineNew);
		assortedValueWithObjectAlignment = getterAndWither[3][1].invoke(assortedValueWithObjectAlignment, oNew);
		assortedValueWithObjectAlignment = getterAndWither[4][1].invoke(assortedValueWithObjectAlignment, iNew);
		assortedValueWithObjectAlignment = getterAndWither[5][1].invoke(assortedValueWithObjectAlignment, fNew);
		assortedValueWithObjectAlignment = getterAndWither[6][1].invoke(assortedValueWithObjectAlignment, tri2New);

		checkEqualTriangle2D(getterAndWither[0][0].invoke(assortedValueWithObjectAlignment), triangleFieldsNew);
		checkEqualPoint2D(getterAndWither[1][0].invoke(assortedValueWithObjectAlignment), pointFieldsNew);
		checkEqualLine2D(getterAndWither[2][0].invoke(assortedValueWithObjectAlignment), lineFieldsNew, true);
		assertEquals(getObject.invoke(getterAndWither[3][0].invoke(assortedValueWithObjectAlignment)), oValueNew);
		assertEquals(getInt.invoke(getterAndWither[4][0].invoke(assortedValueWithObjectAlignment)), iValueNew);
		assertEquals(getFloat.invoke(getterAndWither[5][0].invoke(assortedValueWithObjectAlignment)), fValueNew);
		checkEqualTriangle2D(getterAndWither[6][0].invoke(assortedValueWithObjectAlignment), triangle2FieldsNew);
	}

	/*
	 * Create an assorted refType with object alignment 
	 * 
	 * class AssortedRefWithObjectAlignment {
	 * 	flattened Triangle2D tri;
	 * 	flattened Point2D point;
	 * 	flattened Line2D line;
	 * 	flattened ValueObject o;
	 * 	flattened ValueInt i;
	 * 	flattened ValFloat f;
	 * 	flattened Triangle2D tri2;
	 * }
	 */
	@Test(priority = 4)
	static public void testCreateAssortedRefWithObjectAlignment() throws Throwable {
		String fields[] = {
				"tri:QTriangle2D;:value",
				"point:QPoint2D;:value",
				"line:QFlattenedLine2D;:value",
				"o:QValueObject;:value",
				"i:QValueInt;:value",
				"f:QValueFloat;:value",
				"tri2:QTriangle2D;:value" };
		Class assortedRefWithObjectAlignmentClass = ValueTypeGenerator.generateRefClass("AssortedRefWithObjectAlignment", fields);

		MethodHandle makeAssortedRefWithObjectAlignment = lookup.findStatic(assortedRefWithObjectAlignmentClass, "makeRefGeneric",
				MethodType.methodType(assortedRefWithObjectAlignmentClass, Object.class, Object.class, Object.class, Object.class,
						Object.class, Object.class, Object.class));
		/*
		 * Getters are created in array getterAndSetter[i][0] according to the order of fields i
		 * Setters are created in array getterAndSetter[i][1] according to the order of fields i
		 */
		MethodHandle[][] getterAndSetter = generateGenericGetterAndSetter(assortedRefWithObjectAlignmentClass, fields);

		/* tri1 */
		int[][][] triangleFields = {
				{ { 0xAADDAADD, 0xAACCAACC }, { 0xEEAAEEEE, 0xAAAAAAAA } },
				{ { 0xBBBCCCAA, 0xEEAAEEEA }, { 0xFFFFFAAA, 0xFFFFFAAA } },
				{ { 0xCCCCCCCA, 0xEEEEEEEC }, { 0xAAAAADDD, 0xDDDDBBBB } } };
		Object tri = createTriangle2D(triangleFields);
		checkEqualTriangle2D(tri, triangleFields);
		
		/* point */
		int[] pointFields = { 0xFFEEFFEE, 0xAABBAABB };
		Object point = makePoint2D.invoke(pointFields[0], pointFields[1]);
		checkEqualPoint2D(point, pointFields);

		/* line */
		int[][] lineFields = { { 0xFFEEFFEE, 0xAABBAABB }, { 0xCCAFFAAA, 0xBBAAAACC } };
		Object line = createFlattenedLine2D(lineFields);
		checkEqualLine2D(line, lineFields, true);
		
		/* tri2 */
		int[][][] triangle2Fields = {
				{ { 0xAADDAADD, 0xAACCAACC }, { 0xEEAAEEEE, 0xAAAAAAAA } },
				{ { 0xBBBBBBAA, 0xEEAAEEEA }, { 0xFFFFFAAA, 0xFFFFFAAA } },
				{ { 0xCCAACCCA, 0xEE22EEEC }, { 0xAAAAADDD, 0x1111BBBB } } };
		Object tri2 = createTriangle2D(triangle2Fields);
		checkEqualTriangle2D(tri2, triangle2Fields);
		
		/* o */
		Object oValue = (Object)0xEEFAEEFF;
		Object o = makeValueObject.invoke(oValue);
		assertEquals(getObject.invoke(o), oValue);
		
		/* f */
		float fValue = Float.MAX_VALUE;
		Object f = makeValueFloat.invoke(fValue);
		assertEquals(getFloat.invoke(f), fValue);

		/* i */
		int iValue = 0xABCDDCBA;
		Object i = makeValueInt.invoke(iValue);
		assertEquals(getInt.invoke(i), iValue);

		Object assortedRefWithObjectAlignment = makeAssortedRefWithObjectAlignment.invoke(tri, point, line, o, i, f, tri2);

		checkEqualTriangle2D(getterAndSetter[0][0].invoke(assortedRefWithObjectAlignment), triangleFields);
		checkEqualPoint2D(getterAndSetter[1][0].invoke(assortedRefWithObjectAlignment), pointFields);
		checkEqualLine2D(getterAndSetter[2][0].invoke(assortedRefWithObjectAlignment), lineFields, true);
		assertEquals(getObject.invoke(getterAndSetter[3][0].invoke(assortedRefWithObjectAlignment)), oValue);
		assertEquals(getInt.invoke(getterAndSetter[4][0].invoke(assortedRefWithObjectAlignment)), iValue);
		assertEquals(getFloat.invoke(getterAndSetter[5][0].invoke(assortedRefWithObjectAlignment)), fValue);
		checkEqualTriangle2D(getterAndSetter[6][0].invoke(assortedRefWithObjectAlignment), triangle2Fields);

		/* point */
		int[] pointFieldsNew = { 0xFFEEFFAA, 0xAABBAADD };
		Object pointNew = makePoint2D.invoke(pointFieldsNew[0], pointFieldsNew[1]);
		
		/* line */
		int[][] lineFieldsNew = { { 0x22EE22EE, 0x11441144 }, { 0x33122111, 0x44111133 } };
		Object lineNew = createFlattenedLine2D(lineFieldsNew);
		
		/* tri1 */
		int[][][] triangleFieldsNew = {
				{ { 0x11DD11DD, 0x11331133 }, { 0xEE11AAEE, 0x11111111 } },
				{ { 0x44433311, 0xEE1144E1 }, { 0x11222111, 0x11111113 } },
				{ { 0x33333331, 0xEEEE2EE3 }, { 0x11111DDD, 0xDDDD4444 } } };
		Object tri1New = createTriangle2D(triangleFieldsNew);
		
		/* o */
		Object oValueNew = (Object)0xEECAEEFF;
		Object oNew = makeValueObject.invoke(oValueNew);
		
		/* f */
		float fValueNew = 5.356f;
		Object fNew = makeValueFloat.invoke(fValueNew);
		
		/* i */
		int iValueNew = 0x123DDCBA;
		Object iNew = makeValueInt.invoke(iValueNew);
		
		/* tri2 */
		int[][][] triangle2FieldsNew = {
				{ { 0x11DD11DD, 0x11331133 }, { 0xEE11AAEE, 0xBBB11111 } },
				{ { 0x44433311, 0xEE114421 }, { 0x11222111, 0x2222A113 } },
				{ { 0x33333331, 0xEEEE2CC3 }, { 0x11111DDD, 0xDDDD3444 } } };
		Object tri2New = createTriangle2D(triangle2FieldsNew);

		getterAndSetter[0][1].invoke(assortedRefWithObjectAlignment, tri1New);
		getterAndSetter[1][1].invoke(assortedRefWithObjectAlignment, pointNew);
		getterAndSetter[2][1].invoke(assortedRefWithObjectAlignment, lineNew);
		getterAndSetter[3][1].invoke(assortedRefWithObjectAlignment, oNew);
		getterAndSetter[4][1].invoke(assortedRefWithObjectAlignment, iNew);
		getterAndSetter[5][1].invoke(assortedRefWithObjectAlignment, fNew);
		getterAndSetter[6][1].invoke(assortedRefWithObjectAlignment, tri2New);

		checkEqualTriangle2D(getterAndSetter[0][0].invoke(assortedRefWithObjectAlignment), triangleFieldsNew);
		checkEqualPoint2D(getterAndSetter[1][0].invoke(assortedRefWithObjectAlignment), pointFieldsNew);
		checkEqualLine2D(getterAndSetter[2][0].invoke(assortedRefWithObjectAlignment), lineFieldsNew, true);
		assertEquals(getObject.invoke(getterAndSetter[3][0].invoke(assortedRefWithObjectAlignment)), oValueNew);
		assertEquals(getInt.invoke(getterAndSetter[4][0].invoke(assortedRefWithObjectAlignment)), iValueNew);
		assertEquals(getFloat.invoke(getterAndSetter[5][0].invoke(assortedRefWithObjectAlignment)), fValueNew);
		checkEqualTriangle2D(getterAndSetter[6][0].invoke(assortedRefWithObjectAlignment), triangle2FieldsNew);
	}

	/*
	 * Create an assorted value type with single alignment 
	 * 
	 * value AssortedValueWithSingleAlignment {
	 * 	flattened Triangle2D tri;
	 * 	flattened Point2D point;
	 * 	flattened Line2D line;
	 * 	flattened ValueInt i;
	 * 	flattened ValFloat f;
	 * 	flattened Triangle2D tri2;
	 * }
	 */
	@Test(priority = 4)
	static public void testCreateAssortedValueWithSingleAlignment() throws Throwable {
		String fields[] = {
				"tri:QTriangle2D;:value",
				"point:QPoint2D;:value",
				"line:QFlattenedLine2D;:value",
				"i:QValueInt;:value",
				"f:QValueFloat;:value",
				"tri2:QTriangle2D;:value" };
		Class assortedValueWithSingleAlignmentClass = ValueTypeGenerator.generateValueClass("AssortedValueWithSingleAlignment", fields);

		MethodHandle makeAssortedValueWithSingleAlignment = lookup.findStatic(assortedValueWithSingleAlignmentClass, "makeValueGeneric",
				MethodType.methodType(assortedValueWithSingleAlignmentClass, Object.class, Object.class, Object.class, Object.class,
						Object.class, Object.class));
		/*
		 * Getters are created in array getterAndSetter[i][0] according to the order of fields i
		 * Setters are created in array getterAndSetter[i][1] according to the order of fields i
		 */
		MethodHandle[][] getterAndWither = generateGenericGetterAndWither(assortedValueWithSingleAlignmentClass, fields);

		/* tri1 */
		int[][][] triangleFields = {
				{ { 0xAADDAADD, 0xAACCAACC }, { 0xEEAAEEEE, 0xAAAAAAAA } },
				{ { 0xBBBCCCAA, 0xEEAAEEEA }, { 0xFFFFFAAA, 0xFFFFFAAA } },
				{ { 0xCCCCCCCA, 0xEEEEEEEC }, { 0xAAAAADDD, 0xDDDDBBBB } } };
		Object tri = createTriangle2D(triangleFields);
		checkEqualTriangle2D(tri, triangleFields);
		
		/* point */
		int[] pointFields = { 0xFFEEFFEE, 0xAABBAABB };
		Object point = makePoint2D.invoke(pointFields[0], pointFields[1]);
		checkEqualPoint2D(point, pointFields);

		/* line */
		int[][] lineFields = { { 0xFFEEFFEE, 0xAABBAABB }, { 0xCCAFFAAA, 0xBBAAAACC } };
		Object line = createFlattenedLine2D(lineFields);
		checkEqualLine2D(line, lineFields, true);
		
		/* tri2 */
		int[][][] triangle2Fields = {
				{ { 0xAADDAADD, 0xAACCAACC }, { 0xEEAAEEEE, 0xAAAAAAAA } },
				{ { 0xBBBBBBAA, 0xEEAAEEEA }, { 0xFFFFFAAA, 0xFFFFFAAA } },
				{ { 0xCCAACCCA, 0xEE22EEEC }, { 0xAAAAADDD, 0x1111BBBB } } };
		Object tri2 = createTriangle2D(triangle2Fields);
		checkEqualTriangle2D(tri2, triangle2Fields);

		/* f */
		float fValue = Float.MAX_VALUE;
		Object f = makeValueFloat.invoke(fValue);
		assertEquals(getFloat.invoke(f), fValue);

		/* i */
		int iValue = 0xABCDDCBA;
		Object i = makeValueInt.invoke(iValue);
		assertEquals(getInt.invoke(i), iValue);

		Object assortedValueWithSingleAlignment = makeAssortedValueWithSingleAlignment.invoke(tri, point, line, i, f, tri2);

		checkEqualTriangle2D(getterAndWither[0][0].invoke(assortedValueWithSingleAlignment), triangleFields);
		checkEqualPoint2D(getterAndWither[1][0].invoke(assortedValueWithSingleAlignment), pointFields);
		checkEqualLine2D(getterAndWither[2][0].invoke(assortedValueWithSingleAlignment), lineFields, true);
		assertEquals(getInt.invoke(getterAndWither[3][0].invoke(assortedValueWithSingleAlignment)), iValue);
		assertEquals(getFloat.invoke(getterAndWither[4][0].invoke(assortedValueWithSingleAlignment)), fValue);
		checkEqualTriangle2D(getterAndWither[5][0].invoke(assortedValueWithSingleAlignment), triangle2Fields);

		/* point */
		int[] pointFieldsNew = { 0xFFEEFFAA, 0xAABBAADD };
		Object pointNew = makePoint2D.invoke(pointFieldsNew[0], pointFieldsNew[1]);
		
		/* line */
		int[][] lineFieldsNew = { { 0x22EE22EE, 0x11441144 }, { 0x33122111, 0x44111133 } };
		Object lineNew = createFlattenedLine2D(lineFieldsNew);
		
		/* tri1 */
		int[][][] triangleFieldsNew = {
				{ { 0x11DD11DD, 0x11331133 }, { 0xEE11AAEE, 0x11111111 } },
				{ { 0x44433311, 0xEE1144E1 }, { 0x11222111, 0x11111113 } },
				{ { 0x33333331, 0xEEEE2EE3 }, { 0x11111DDD, 0xDDDD4444 } } };
		Object tri1New = createTriangle2D(triangleFieldsNew);
		
		/* o */
		Object oValueNew = (Object)0xEECAEEFF;
		Object oNew = makeValueObject.invoke(oValueNew);
		
		/* f */
		float fValueNew = 5.356f;
		Object fNew = makeValueFloat.invoke(fValueNew);
		
		/* i */
		int iValueNew = 0x123DDCBA;
		Object iNew = makeValueInt.invoke(iValueNew);
		
		/* tri2 */
		int[][][] triangle2FieldsNew = {
				{ { 0x11DD11DD, 0x11331133 }, { 0xEE11AAEE, 0xBBB11111 } },
				{ { 0x44433311, 0xEE114421 }, { 0x11222111, 0x2222A113 } },
				{ { 0x33333331, 0xEEEE2CC3 }, { 0x11111DDD, 0xDDDD3444 } } };
		Object tri2New = createTriangle2D(triangle2FieldsNew);

		assortedValueWithSingleAlignment = getterAndWither[0][1].invoke(assortedValueWithSingleAlignment, tri1New);
		assortedValueWithSingleAlignment = getterAndWither[1][1].invoke(assortedValueWithSingleAlignment, pointNew);
		assortedValueWithSingleAlignment = getterAndWither[2][1].invoke(assortedValueWithSingleAlignment, lineNew);
		assortedValueWithSingleAlignment = getterAndWither[3][1].invoke(assortedValueWithSingleAlignment, iNew);
		assortedValueWithSingleAlignment = getterAndWither[4][1].invoke(assortedValueWithSingleAlignment, fNew);
		assortedValueWithSingleAlignment = getterAndWither[5][1].invoke(assortedValueWithSingleAlignment, tri2New);

		checkEqualTriangle2D(getterAndWither[0][0].invoke(assortedValueWithSingleAlignment), triangleFieldsNew);
		checkEqualPoint2D(getterAndWither[1][0].invoke(assortedValueWithSingleAlignment), pointFieldsNew);
		checkEqualLine2D(getterAndWither[2][0].invoke(assortedValueWithSingleAlignment), lineFieldsNew, true);
		assertEquals(getInt.invoke(getterAndWither[3][0].invoke(assortedValueWithSingleAlignment)), iValueNew);
		assertEquals(getFloat.invoke(getterAndWither[4][0].invoke(assortedValueWithSingleAlignment)), fValueNew);
		checkEqualTriangle2D(getterAndWither[5][0].invoke(assortedValueWithSingleAlignment), triangle2FieldsNew);
	}

	/*
	 * Create an assorted refType with single alignment 
	 * 
	 * class AssortedRefWithSingleAlignment {
	 * 	flattened Triangle2D tri;
	 * 	flattened Point2D point;
	 * 	flattened Line2D line;
	 * 	flattened ValueInt i;
	 * 	flattened ValFloat f;
	 * 	flattened Triangle2D tri2;
	 * }
	 */
	@Test(priority = 4)
	static public void testCreateAssortedRefWithSingleAlignment() throws Throwable {
		String fields[] = {
				"tri:QTriangle2D;:value",
				"point:QPoint2D;:value",
				"line:QFlattenedLine2D;:value",
				"i:QValueInt;:value",
				"f:QValueFloat;:value",
				"tri2:QTriangle2D;:value" };
		Class assortedRefWithSingleAlignmentClass = ValueTypeGenerator.generateRefClass("AssortedRefWithSingleAlignment", fields);

		MethodHandle makeAssortedRefWithSingleAlignment = lookup.findStatic(assortedRefWithSingleAlignmentClass, "makeRefGeneric", MethodType.methodType(
				assortedRefWithSingleAlignmentClass, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class));
		/*
		 * Getters are created in array getterAndSetter[i][0] according to the order of fields i
		 * Setters are created in array getterAndSetter[i][1] according to the order of fields i
		 */
		MethodHandle[][] getterAndSetter = generateGenericGetterAndSetter(assortedRefWithSingleAlignmentClass, fields);

		/* tri1 */
		int[][][] triangleFields = {
				{ { 0xAADDAADD, 0xAACCAACC }, { 0xEEAAEEEE, 0xAAAAAAAA } },
				{ { 0xBBBCCCAA, 0xEEAAEEEA }, { 0xFFFFFAAA, 0xFFFFFAAA } },
				{ { 0xCCCCCCCA, 0xEEEEEEEC }, { 0xAAAAADDD, 0xDDDDBBBB } } };
		Object tri = createTriangle2D(triangleFields);
		checkEqualTriangle2D(tri, triangleFields);
		
		/* point */
		int[] pointFields = { 0xFFEEFFEE, 0xAABBAABB };
		Object point = makePoint2D.invoke(pointFields[0], pointFields[1]);
		checkEqualPoint2D(point, pointFields);

		/* line */
		int[][] lineFields = { { 0xFFEEFFEE, 0xAABBAABB }, { 0xCCAFFAAA, 0xBBAAAACC } };
		Object line = createFlattenedLine2D(lineFields);
		checkEqualLine2D(line, lineFields, true);
		
		/* tri2 */
		int[][][] triangle2Fields = {
				{ { 0xAADDAADD, 0xAACCAACC }, { 0xEEAAEEEE, 0xAAAAAAAA } },
				{ { 0xBBBBBBAA, 0xEEAAEEEA }, { 0xFFFFFAAA, 0xFFFFFAAA } },
				{ { 0xCCAACCCA, 0xEE22EEEC }, { 0xAAAAADDD, 0x1111BBBB } } };
		Object tri2 = createTriangle2D(triangle2Fields);
		checkEqualTriangle2D(tri2, triangle2Fields);

		/* f */
		float fValue = Float.MAX_VALUE;
		Object f = makeValueFloat.invoke(fValue);
		assertEquals(getFloat.invoke(f), fValue);

		/* i */
		int iValue = 0xABCDDCBA;
		Object i = makeValueInt.invoke(iValue);
		assertEquals(getInt.invoke(i), iValue);

		Object assortedRefWithSingleAlignment = makeAssortedRefWithSingleAlignment.invoke(tri, point, line, i, f, tri2);

		checkEqualTriangle2D(getterAndSetter[0][0].invoke(assortedRefWithSingleAlignment), triangleFields);
		checkEqualPoint2D(getterAndSetter[1][0].invoke(assortedRefWithSingleAlignment), pointFields);
		checkEqualLine2D(getterAndSetter[2][0].invoke(assortedRefWithSingleAlignment), lineFields, true);
		assertEquals(getInt.invoke(getterAndSetter[3][0].invoke(assortedRefWithSingleAlignment)), iValue);
		assertEquals(getFloat.invoke(getterAndSetter[4][0].invoke(assortedRefWithSingleAlignment)), fValue);
		checkEqualTriangle2D(getterAndSetter[5][0].invoke(assortedRefWithSingleAlignment), triangle2Fields);

		/* point */
		int[] pointFieldsNew = { 0xFFEEFFAA, 0xAABBAADD };
		Object pointNew = makePoint2D.invoke(pointFieldsNew[0], pointFieldsNew[1]);
		
		/* line */
		int[][] lineFieldsNew = { { 0x22EE22EE, 0x11441144 }, { 0x33122111, 0x44111133 } };
		Object lineNew = createFlattenedLine2D(lineFieldsNew);
		
		/* tri1 */
		int[][][] triangleFieldsNew = {
				{ { 0x11DD11DD, 0x11331133 }, { 0xEE11AAEE, 0x11111111 } },
				{ { 0x44433311, 0xEE1144E1 }, { 0x11222111, 0x11111113 } },
				{ { 0x33333331, 0xEEEE2EE3 }, { 0x11111DDD, 0xDDDD4444 } } };
		Object tri1New = createTriangle2D(triangleFieldsNew);
		
		/* o */
		Object oValueNew = (Object)0xEECAEEFF;
		Object oNew = makeValueObject.invoke(oValueNew);
		
		/* f */
		float fValueNew = 5.356f;
		Object fNew = makeValueFloat.invoke(fValueNew);
		
		/* i */
		int iValueNew = 0x123DDCBA;
		Object iNew = makeValueInt.invoke(iValueNew);
		
		/* tri2 */
		int[][][] triangle2FieldsNew = {
				{ { 0x11DD11DD, 0x11331133 }, { 0xEE11AAEE, 0xBBB11111 } },
				{ { 0x44433311, 0xEE114421 }, { 0x11222111, 0x2222A113 } },
				{ { 0x33333331, 0xEEEE2CC3 }, { 0x11111DDD, 0xDDDD3444 } } };
		Object tri2New = createTriangle2D(triangle2FieldsNew);

		getterAndSetter[0][1].invoke(assortedRefWithSingleAlignment, tri1New);
		getterAndSetter[1][1].invoke(assortedRefWithSingleAlignment, pointNew);
		getterAndSetter[2][1].invoke(assortedRefWithSingleAlignment, lineNew);
		getterAndSetter[3][1].invoke(assortedRefWithSingleAlignment, iNew);
		getterAndSetter[4][1].invoke(assortedRefWithSingleAlignment, fNew);
		getterAndSetter[5][1].invoke(assortedRefWithSingleAlignment, tri2New);
		
		checkEqualTriangle2D(getterAndSetter[0][0].invoke(assortedRefWithSingleAlignment), triangleFieldsNew);
		checkEqualPoint2D(getterAndSetter[1][0].invoke(assortedRefWithSingleAlignment), pointFieldsNew);
		checkEqualLine2D(getterAndSetter[2][0].invoke(assortedRefWithSingleAlignment), lineFieldsNew, true);
		assertEquals(getInt.invoke(getterAndSetter[3][0].invoke(assortedRefWithSingleAlignment)), iValueNew);
		assertEquals(getFloat.invoke(getterAndSetter[4][0].invoke(assortedRefWithSingleAlignment)), fValueNew);
		checkEqualTriangle2D(getterAndSetter[5][0].invoke(assortedRefWithSingleAlignment), triangle2FieldsNew);
	}

	/*
	 * Create a large valueType with 16 valuetypes as its members
	 * Create a mega valueType with 16 large valuetypes as its members 
	 *
	 * value LargeObject {
	 *	flattened ValObject val1;
	 *	flattened ValObject val2;
	 *	...
	 *	flattened ValObject val16;
	 * }
	 *
	 * value MegaObject {
	 *	flattened LargeObject val1;
	 *	... 
	 *	flattened LargeObject val16;
	 * }
	 */
	@Test(priority = 2)
	static public void testCreateLargeObjectAndMegaValue() throws Throwable {
		String largeFields[] = {
				"val1:QValueObject;:value",
				"val2:QValueObject;:value",
				"val3:QValueObject;:value",
				"val4:QValueObject;:value",
				"val5:QValueObject;:value",
				"val6:QValueObject;:value",
				"val7:QValueObject;:value",
				"val8:QValueObject;:value",
				"val9:QValueObject;:value",
				"val10:QValueObject;:value",
				"val11:QValueObject;:value",
				"val12:QValueObject;:value",
				"val13:QValueObject;:value",
				"val14:QValueObject;:value",
				"val15:QValueObject;:value",
				"val16:QValueObject;:value" };

		largeObjectValueClass = ValueTypeGenerator.generateValueClass("LargeObject", largeFields);
		makeLargeObjectValue = lookup.findStatic(largeObjectValueClass, "makeValueGeneric",
				MethodType.methodType(largeObjectValueClass, Object.class, Object.class, Object.class, Object.class,
						Object.class, Object.class, Object.class, Object.class, Object.class, Object.class,
						Object.class, Object.class, Object.class, Object.class, Object.class, Object.class));
		/*
		 * Getters are created in array getterAndWither[i][0] according to the order of fields i
		 * Withers are created in array getterAndWither[i][1] according to the order of fields i
		 */
		MethodHandle[][] getterAndWither = generateGenericGetterAndWither(largeObjectValueClass, largeFields);
		
		/*
		 * create field objects
		 */
		Object[] originObjects = new Object[16];
		Object[] valueObjects = new Object[16];
		for (int i = 0; i < 16; i++) {
			originObjects[i] = (Object)(Integer.MIN_VALUE + i);
			valueObjects[i] = makeValueObject.invoke(originObjects[i]);
		}
		Object largeObjectValue = makeLargeObjectValue.invoke(valueObjects[0], valueObjects[1], valueObjects[2], valueObjects[3],
				valueObjects[4], valueObjects[5], valueObjects[6], valueObjects[7], valueObjects[8], valueObjects[9],
				valueObjects[10], valueObjects[11], valueObjects[12], valueObjects[13], valueObjects[14],
				valueObjects[15]);

		for (int i = 0; i < 16; i++) {
			assertEquals(getObject.invoke(getterAndWither[i][0].invoke(largeObjectValue)), originObjects[i]);
		}

		/*
		 * create new objects
		 */
		Object[] originObjectsNew = new Object[16];
		Object[] valueObjectsNew = new Object[16];
		for (int i = 0; i < 16; i++) {
			originObjectsNew[i] = (Object)(Integer.MAX_VALUE - i);
			valueObjectsNew[i] = makeValueObject.invoke(originObjectsNew[i]);
			largeObjectValue = getterAndWither[i][1].invoke(largeObjectValue, valueObjectsNew[i]);
			assertEquals(getObject.invoke(getterAndWither[i][0].invoke(largeObjectValue)), originObjectsNew[i]);
		}

		/*
		 * create MegaObject
		 */
		String megaFields[] = {
				"val1:QLargeObject;:value",
				"val2:QLargeObject;:value",
				"val3:QLargeObject;:value",
				"val4:QLargeObject;:value",
				"val5:QLargeObject;:value",
				"val6:QLargeObject;:value",
				"val7:QLargeObject;:value",
				"val8:QLargeObject;:value",
				"val9:QLargeObject;:value",
				"val10:QLargeObject;:value",
				"val11:QLargeObject;:value",
				"val12:QLargeObject;:value",
				"val13:QLargeObject;:value",
				"val14:QLargeObject;:value",
				"val15:QLargeObject;:value",
				"val16:QLargeObject;:value" };
		Class megaObjectClass = ValueTypeGenerator.generateValueClass("MegaObject", megaFields);
		MethodHandle makeMega = lookup.findStatic(megaObjectClass, "makeValueGeneric",
				MethodType.methodType(megaObjectClass, Object.class, Object.class, Object.class, Object.class,
						Object.class, Object.class, Object.class, Object.class, Object.class, Object.class,
						Object.class, Object.class, Object.class, Object.class, Object.class, Object.class));

		/*
		 * Getters are created in array getterAndWither[i][0] according to the order of fields i
		 * Withers are created in array getterAndWither[i][1] according to the order of fields i
		 */
		MethodHandle[][] megaGetterAndWither = generateGenericGetterAndWither(megaObjectClass, megaFields);
		
		/*
		 * create field objects
		 */
		Object[][] values = new Object[16][16];
		Object[] largeObjects = new Object[16];
		for (int i = 0; i < 16; i++) {
			Object[] valObjects = new Object[16];
			for (int j = 0; j < 16; j++) {
				values[i][j] = (Object)(Integer.MIN_VALUE + i * j);
				valObjects[j] = makeValueObject.invoke(values[i][j]);
			}
			largeObjects[i] = makeLargeObjectValue.invoke(valObjects[0], valObjects[1], valObjects[2], valObjects[3],
					valObjects[4], valObjects[5], valObjects[6], valObjects[7], valObjects[8], valObjects[9],
					valObjects[10], valObjects[11], valObjects[12], valObjects[13], valObjects[14], valObjects[15]);
		}

		Object megaObject = makeMega.invoke(largeObjects[0], largeObjects[1], largeObjects[2], largeObjects[3],
				largeObjects[4], largeObjects[5], largeObjects[6], largeObjects[7], largeObjects[8], largeObjects[9],
				largeObjects[10], largeObjects[11], largeObjects[12], largeObjects[13], largeObjects[14],
				largeObjects[15]);

		for (int i = 0; i < megaFields.length; i++) {
			for (int j = 0; j < largeFields.length; j++) {
				Object objectFromGetter = getObject
						.invoke(getterAndWither[j][0].invoke(megaGetterAndWither[i][0].invoke(megaObject)));
				assertEquals(objectFromGetter, values[i][j]);
			}
		}
		Object[][] valuesNew = new Object[16][16];
		Object[] largeObjectsNew = new Object[16];
		for (int i = 0; i < 16; i++) {
			Object[] valObjects = new Object[16];
			for (int j = 0; j < 16; j++) {
				valuesNew[i][j] = (Object)(Integer.MAX_VALUE - i * j);
				valObjects[j] = makeValueObject.invoke(valuesNew[i][j]);
			}
			largeObjectsNew[i] = makeLargeObjectValue.invoke(valObjects[0], valObjects[1], valObjects[2], valObjects[3],
					valObjects[4], valObjects[5], valObjects[6], valObjects[7], valObjects[8], valObjects[9],
					valObjects[10], valObjects[11], valObjects[12], valObjects[13], valObjects[14], valObjects[15]);
			megaObject = megaGetterAndWither[i][1].invoke(megaObject, largeObjectsNew[i]);
		}

		for (int i = 0; i < megaFields.length; i++) {
			for (int j = 0; j < largeFields.length; j++) {
				Object objectFromGetter = getObject
						.invoke(getterAndWither[j][0].invoke(megaGetterAndWither[i][0].invoke(megaObject)));
				assertEquals(objectFromGetter, valuesNew[i][j]);
			}
		}
	}

	/*
	 * Create a large refType with 16 valuetypes as its members
	 * Create a mega refType with 16 large valuetypes as its members 
	 *
	 * class LargeRef {
	 *	flattened ValObject val1;
	 *	flattened ValObject val2;
	 *	...
	 *	flattened ValObject val16;
	 * }
	 *
	 * class MegaObject {
	 *	flattened LargeObject val1;
	 *	... 
	 *	flattened LargeObject val16;
	 * }
	 */
	@Test(priority = 3)
	static public void testCreateLargeObjectAndMegaRef() throws Throwable {
		String largeFields[] = {
				"val1:QValueObject;:value",
				"val2:QValueObject;:value",
				"val3:QValueObject;:value",
				"val4:QValueObject;:value",
				"val5:QValueObject;:value",
				"val6:QValueObject;:value",
				"val7:QValueObject;:value",
				"val8:QValueObject;:value",
				"val9:QValueObject;:value",
				"val10:QValueObject;:value",
				"val11:QValueObject;:value",
				"val12:QValueObject;:value",
				"val13:QValueObject;:value",
				"val14:QValueObject;:value",
				"val15:QValueObject;:value",
				"val16:QValueObject;:value" };

		Class largeRefClass = ValueTypeGenerator.generateRefClass("LargeRef", largeFields);
		MethodHandle makeLargeObjectRef = lookup.findStatic(largeRefClass, "makeRefGeneric",
				MethodType.methodType(largeRefClass, Object.class, Object.class, Object.class, Object.class,
						Object.class, Object.class, Object.class, Object.class, Object.class, Object.class,
						Object.class, Object.class, Object.class, Object.class, Object.class, Object.class));
		/*
		 * Getters are created in array getterAndSetter[i][0] according to the order of fields i
		 * Setters are created in array getterAndSetter[i][1] according to the order of fields i
		 */
		MethodHandle[][] getterAndSetter = new MethodHandle[largeFields.length][2];
		for (int i = 0; i < largeFields.length; i++) {
			String field = (largeFields[i].split(":"))[0];
			getterAndSetter[i][0] = generateGenericGetter(largeRefClass, field);
			getterAndSetter[i][1] = generateGenericSetter(largeRefClass, field);
		}

		/*
		 * field objects
		 */
		Object[] originObjects = new Object[16];
		Object[] valueObjects = new Object[16];
		for (int i = 0; i < 16; i++) {
			originObjects[i] = (Object)(Integer.MIN_VALUE + i);
			valueObjects[i] = makeValueObject.invoke(originObjects[i]);
		}

		Object largeObjectRef = makeLargeObjectRef.invoke(valueObjects[0], valueObjects[1], valueObjects[2], valueObjects[3],
				valueObjects[4], valueObjects[5], valueObjects[6], valueObjects[7], valueObjects[8], valueObjects[9],
				valueObjects[10], valueObjects[11], valueObjects[12], valueObjects[13], valueObjects[14],
				valueObjects[15]);

		for (int i = 0; i < 16; i++) {
			assertEquals(getObject.invoke(getterAndSetter[i][0].invoke(largeObjectRef)), originObjects[i]);
		}
		/*
		 * create new field objects
		 */
		Object[] originObjectsNew = new Object[16];
		Object[] valueObjectsNew = new Object[16];
		for (int i = 0; i < 16; i++) {
			originObjectsNew[i] = (Object)(Integer.MAX_VALUE - i);
			valueObjectsNew[i] = makeValueObject.invoke(originObjectsNew[i]);
			getterAndSetter[i][1].invoke(largeObjectRef, valueObjectsNew[i]);

			assertEquals(getObject.invoke(getterAndSetter[i][0].invoke(largeObjectRef)), originObjectsNew[i]);
		}

		/*
		 * create MegaObject
		 */
		String megaFields[] = {
				"val1:QLargeObject;:value",
				"val2:QLargeObject;:value",
				"val3:QLargeObject;:value",
				"val4:QLargeObject;:value",
				"val5:QLargeObject;:value",
				"val6:QLargeObject;:value",
				"val7:QLargeObject;:value",
				"val8:QLargeObject;:value",
				"val9:QLargeObject;:value",
				"val10:QLargeObject;:value",
				"val11:QLargeObject;:value",
				"val12:QLargeObject;:value",
				"val13:QLargeObject;:value",
				"val14:QLargeObject;:value",
				"val15:QLargeObject;:value",
				"val16:QLargeObject;:value" };
		Class megaObjectClass = ValueTypeGenerator.generateRefClass("MegaRef", megaFields);
		MethodHandle makeMegaObjectRef = lookup.findStatic(megaObjectClass, "makeRefGeneric",
				MethodType.methodType(megaObjectClass, Object.class, Object.class, Object.class, Object.class,
						Object.class, Object.class, Object.class, Object.class, Object.class, Object.class,
						Object.class, Object.class, Object.class, Object.class, Object.class, Object.class));

		MethodHandle[][] getterAndWither = new MethodHandle[largeFields.length][2];
		for (int i = 0; i < largeFields.length; i++) {
			String field = (largeFields[i].split(":"))[0];
			getterAndWither[i][0] = generateGenericGetter(largeObjectValueClass, field);
			getterAndWither[i][1] = generateGenericWither(largeObjectValueClass, field);
		}

		/*
		 * Getters are created in array getterAndSetter[i][0] according to the order of fields i
		 * Setters are created in array getterAndSetter[i][1] according to the order of fields i
		 */
		MethodHandle[][] megaGetterAndSetter = new MethodHandle[megaFields.length][2];
		for (int i = 0; i < megaFields.length; i++) {
			String field = (megaFields[i].split(":"))[0];
			megaGetterAndSetter[i][0] = generateGenericGetter(megaObjectClass, field);
			megaGetterAndSetter[i][1] = generateGenericSetter(megaObjectClass, field);
		}

		/*
		 * create field objects
		 */
		Object[][] values = new Object[16][16];
		Object[] largeObjects = new Object[16];
		for (int i = 0; i < 16; i++) {
			Object[] valObjects = new Object[16];
			for (int j = 0; j < 16; j++) {
				values[i][j] = (Object)(Integer.MIN_VALUE + i * j);
				valObjects[j] = makeValueObject.invoke(values[i][j]);
			}
			largeObjects[i] = makeLargeObjectValue.invoke(valObjects[0], valObjects[1], valObjects[2], valObjects[3],
					valObjects[4], valObjects[5], valObjects[6], valObjects[7], valObjects[8], valObjects[9],
					valObjects[10], valObjects[11], valObjects[12], valObjects[13], valObjects[14], valObjects[15]);
		}

		Object megaObject = makeMegaObjectRef.invoke(largeObjects[0], largeObjects[1], largeObjects[2], largeObjects[3],
				largeObjects[4], largeObjects[5], largeObjects[6], largeObjects[7], largeObjects[8], largeObjects[9],
				largeObjects[10], largeObjects[11], largeObjects[12], largeObjects[13], largeObjects[14],
				largeObjects[15]);

		for (int i = 0; i < megaFields.length; i++) {
			for (int j = 0; j < largeFields.length; j++) {
				Object objectFromGetter = getObject
						.invoke(getterAndWither[j][0].invoke(megaGetterAndSetter[i][0].invoke(megaObject)));
				assertEquals(objectFromGetter, values[i][j]);
			}
		}
		Object[][] valuesNew = new Object[16][16];
		Object[] largeObjectsNew = new Object[16];
		for (int i = 0; i < 16; i++) {
			Object[] valObjects = new Object[16];
			for (int j = 0; j < 16; j++) {
				valuesNew[i][j] = (Object)(Integer.MAX_VALUE - i * j);
				valObjects[j] = makeValueObject.invoke(valuesNew[i][j]);
			}
			largeObjectsNew[i] = makeLargeObjectValue.invoke(valObjects[0], valObjects[1], valObjects[2], valObjects[3],
					valObjects[4], valObjects[5], valObjects[6], valObjects[7], valObjects[8], valObjects[9],
					valObjects[10], valObjects[11], valObjects[12], valObjects[13], valObjects[14], valObjects[15]);
			megaGetterAndSetter[i][1].invoke(megaObject, largeObjectsNew[i]);
		}

		for (int i = 0; i < megaFields.length; i++) {
			for (int j = 0; j < largeFields.length; j++) {
				Object objectFromGetter = getObject
						.invoke(getterAndWither[j][0].invoke(megaGetterAndSetter[i][0].invoke(megaObject)));
				assertEquals(objectFromGetter, valuesNew[i][j]);
			}
		}
	}
	
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

	static long getFieldOffset(Class clazz, String field) {
		try {
			Field f = clazz.getDeclaredField(field);
			return myUnsafe.objectFieldOffset(f);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	static MethodHandle[][] generateGenericGetterAndWither(Class clazz, String[] fields) {
		MethodHandle[][] getterAndWither = new MethodHandle[fields.length][2];
		for (int i = 0; i < fields.length; i++) {
			String field = (fields[i].split(":"))[0];
			getterAndWither[i][0] = generateGenericGetter(clazz, field);
			getterAndWither[i][1] = generateGenericWither(clazz, field);
		}
		return getterAndWither;
	}

	static MethodHandle[][] generateGenericGetterAndSetter(Class clazz, String[] fields) {
		MethodHandle[][] getterAndSetter = new MethodHandle[fields.length][2];
		for (int i = 0; i < fields.length; i++) {
			String field = (fields[i].split(":"))[0];
			getterAndSetter[i][0] = generateGenericGetter(clazz, field);
			getterAndSetter[i][1] = generateGenericSetter(clazz, field);
		}
		return getterAndSetter;
	}

	static Object createFlattenedLine2D(int[][] positions) throws Throwable {
		return makeFlattenedLine2D.invoke(makePoint2D.invoke(positions[0][0], positions[0][1]),
				makePoint2D.invoke(positions[1][0], positions[1][1]));
	}

	static Object createTriangle2D(int[][][] positions) throws Throwable {
		return makeTriangle2D.invoke(
				makeFlattenedLine2D.invoke(makePoint2D.invoke(positions[0][0][0], positions[0][0][1]),
						makePoint2D.invoke(positions[0][1][0], positions[0][1][1])),
				makeFlattenedLine2D.invoke(makePoint2D.invoke(positions[1][0][0], positions[1][0][1]),
						makePoint2D.invoke(positions[1][1][0], positions[1][1][1])),
				makeFlattenedLine2D.invoke(makePoint2D.invoke(positions[2][0][0], positions[2][0][1]),
						makePoint2D.invoke(positions[2][1][0], positions[2][1][1])));
	}

	static void checkEqualPoint2D(Object point, int[] positions) throws Throwable {
		assertEquals(getX.invoke(point), positions[0]);
		assertEquals(getY.invoke(point), positions[1]);
	}

	static void checkEqualLine2D(Object line, int[][] positions, boolean flatten) throws Throwable {
		if (flatten == true) {
			checkEqualPoint2D(getFlatSt.invoke(line), positions[0]);
			checkEqualPoint2D(getFlatEn.invoke(line), positions[1]);
		} else {
			checkEqualPoint2D(getSt.invoke(line), positions[0]);
			checkEqualPoint2D(getEn.invoke(line), positions[1]);
		}
	}

	static void checkEqualTriangle2D(Object triangle, int[][][] positions) throws Throwable {
		checkEqualLine2D(getV1.invoke(triangle), positions[0], true);
		checkEqualLine2D(getV2.invoke(triangle), positions[1], true);
		checkEqualLine2D(getV3.invoke(triangle), positions[2], true);
	}
}