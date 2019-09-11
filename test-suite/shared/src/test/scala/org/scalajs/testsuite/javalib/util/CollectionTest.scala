/*
 * Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package org.scalajs.testsuite.javalib.util

import java.{util => ju, lang => jl}

import org.junit.Test
import org.junit.Assert._

import scala.collection.JavaConverters._

import org.scalajs.testsuite.utils.AssertThrows._

import scala.reflect.ClassTag

trait CollectionTest {

  def factory: CollectionFactory

  def testCollectionApi(): Unit = {
    shouldStoreStrings()
    shouldStoreIntegers()
    shouldStoreDoubles()
    shouldStoreCustomObjects()
    shouldRemoveStoredElements()
    shouldRemoveStoredElementsOnDoubleCornerCases()
    shouldBeClearedWithOneOperation()
    shouldCheckContainedPresence()
    shouldCheckContainedPresenceForDoubleCornerCases()
    shouldGiveProperIteratorOverElements()
  }

  @Test def shouldStoreStrings(): Unit = {
    val coll = factory.empty[String]

    assertEquals(0, coll.size())
    coll.add("one")
    assertEquals(1, coll.size())

    coll.clear()
    assertEquals(0, coll.size())
    assertFalse(coll.addAll(Seq.empty[String].asJava))
    assertEquals(0, coll.size())

    assertTrue(coll.addAll(Seq("one").asJava))
    assertEquals(1, coll.size())

    coll.clear()
    assertTrue(coll.addAll(Seq("one", "two", "one").asJava))
    assertTrue(coll.size() >= 1)
  }

  @Test def shouldStoreIntegers(): Unit = {
    val coll = factory.empty[Int]

    assertEquals(0, coll.size())
    coll.add(1)
    assertEquals(1, coll.size())

    coll.clear()
    assertEquals(0, coll.size())
    assertFalse(coll.addAll(Seq.empty[Int].asJava))
    assertEquals(0, coll.size())

    assertTrue(coll.addAll(Seq(1).asJava))
    assertEquals(1, coll.size())

    coll.clear()
    assertTrue(coll.addAll(Seq(1, 2, 1).asJava))
    assertTrue(coll.size() >= 1)
  }

  @Test def shouldStoreDoubles(): Unit = {
    val coll = factory.empty[Double]

    assertEquals(0, coll.size())
    coll.add(1.234)
    assertEquals(1, coll.size())

    coll.clear()
    assertEquals(0, coll.size())
    assertFalse(coll.addAll(Seq.empty[Double].asJava))
    assertEquals(0, coll.size())

    assertTrue(coll.addAll(Seq(1.234).asJava))
    assertEquals(1, coll.size())

    coll.clear()
    assertTrue(coll.addAll(Seq(1.234, 2.345, 1.234).asJava))
    assertTrue(coll.size() >= 1)

    coll.clear()
    coll.add(+0.0)
    assertTrue(coll.contains(+0.0))
    assertFalse(coll.contains(-0.0))

    coll.clear()
    coll.add(-0.0)
    assertFalse(coll.contains(+0.0))
    assertTrue(coll.contains(-0.0))

    coll.clear()
    coll.add(Double.NaN)
    assertEquals(1, coll.size())
    assertTrue(coll.contains(Double.NaN))
  }

  @Test def shouldStoreCustomObjects(): Unit = {
    case class TestObj(num: Int) extends jl.Comparable[TestObj] {
      def compareTo(o: TestObj): Int =
        o.num.compareTo(num)
    }

    val coll = factory.empty[TestObj]

    coll.add(TestObj(100))
    assertEquals(1, coll.size())
    assertTrue(coll.contains(TestObj(100)))
    assertFalse(coll.contains(TestObj(200)))
  }

  @Test def shouldRemoveStoredElements(): Unit = {
    val coll = factory.empty[String]

    coll.add("one")
    coll.add("two")
    coll.add("three")
    coll.add("two")

    val initialSize = coll.size()
    assertFalse(coll.remove("four"))
    assertEquals(initialSize, coll.size())
    assertTrue(coll.remove("two"))
    assertEquals(initialSize - 1, coll.size())
    assertTrue(coll.remove("one"))
    assertEquals(initialSize - 2, coll.size())
  }

  @Test def shouldRemoveStoredElementsOnDoubleCornerCases(): Unit = {
    val coll = factory.empty[Double]

    coll.add(1.234)
    coll.add(2.345)
    coll.add(Double.NaN)
    coll.add(+0.0)
    coll.add(-0.0)

    // coll == ArrayCollection(1.234, 2.345, NaN, +0.0, -0.0)
    assertTrue(coll.remove(Double.NaN))
    // coll == ArrayCollection(1.234, 2.345, +0.0, -0.0)
    assertEquals(4, coll.size())
    assertTrue(coll.remove(2.345))
    // coll == ArrayCollection(1.234, +0.0, -0.0)
    assertEquals(3, coll.size())
    assertTrue(coll.remove(1.234))
    // coll == ArrayCollection(+0.0, -0.0)
    assertEquals(2, coll.size())
    assertTrue(coll.remove(-0.0))
    // coll == ArrayCollection(NaN, +0.0)
    assertEquals(1, coll.size())

    coll.clear()

    assertTrue(coll.isEmpty)
  }

  @Test def shouldBeClearedWithOneOperation(): Unit = {
    val coll = factory.empty[String]

    coll.add("one")
    coll.add("two")
    assertEquals(2, coll.size)
    coll.clear()
    assertEquals(0, coll.size)
  }

  @Test def shouldCheckContainedPresence(): Unit = {
    val coll = factory.empty[String]

    coll.add("one")
    assertTrue(coll.contains("one"))
    assertFalse(coll.contains("two"))
    if (factory.allowsNullElementQuery) {
      assertFalse(coll.contains(null))
    } else {
      expectThrows(classOf[Exception], coll.contains(null))
    }
  }

  @Test def shouldCheckContainedPresenceForDoubleCornerCases(): Unit = {
    val coll = factory.empty[Double]

    coll.add(-0.0)
    assertTrue(coll.contains(-0.0))
    assertFalse(coll.contains(+0.0))

    coll.clear()

    coll.add(+0.0)
    assertFalse(coll.contains(-0.0))
    assertTrue(coll.contains(+0.0))
  }

  @Test def shouldGiveProperIteratorOverElements(): Unit = {
    val coll = factory.empty[String]
    coll.add("one")
    coll.add("two")
    coll.add("three")
    coll.add("three")
    coll.add("three")

    assertEquals(coll.iterator().asScala.toSet, Set("one", "two", "three"))
  }

  @Test def removeIf(): Unit = {
    val coll = factory.fromElements[Int](42, 50, 12, 0, -45, 102, 32, 75)
    assertEquals(8, coll.size())

    assertTrue(coll.removeIf(new java.util.function.Predicate[Int] {
      def test(x: Int): Boolean = x >= 50
    }))
    assertEquals(5, coll.size())
    assertEquals(coll.iterator().asScala.toSet, Set(-45, 0, 12, 32, 42))

    assertFalse(coll.removeIf(new java.util.function.Predicate[Int] {
      def test(x: Int): Boolean = x >= 45
    }))
    assertEquals(5, coll.size())
    assertEquals(coll.iterator().asScala.toSet, Set(-45, 0, 12, 32, 42))
  }

  @Test def toStringShouldConvertEmptyCollection(): Unit = {
    val coll = factory.empty[Double]

    val expected = "[]"
    val result = coll.toString()
    assertEquals(expected, result)
  }

  @Test def toStringShouldConvertOneElementCollection(): Unit = {
    val coll = factory.fromElements[Double](1.01)

    val expected = "[1.01]"
    val result = coll.toString()
    assertEquals(expected, result)
  }

  @Test def toStringShouldUseCommaSpace(): Unit = {
    // Check/change regex pattern below if making changes here.
    val coll = factory.fromElements[Double](88.42, -23.36, 60.173)

    val result = coll.toString()

    // The major interest here is if the Java 8 comma-space idiom
    // is used correctly. 

    // As long as this test is being run, might as well check that
    // the output elements look the floating point numbers given
    // to the factory.

    // The order of the elements in the output is not specified by
    // AbstractCollection. Some sub-classes, such as Deque & TreeSet,
    // do define orders. HashMap is a concrete class which does not.

    val element = "-?\\d{2}\\.\\d{2,3}"
    val commaSpace = ", "

    val pattern = "\\[" +
        element + commaSpace +
        element + commaSpace +
        element + "\\]"

    val resultIsValid =  result.matches(pattern)

    val msg = "result '" + result +
        "' did not match expected pattern '" + pattern + "'"

    assertTrue(msg, resultIsValid)
  }

}

object CollectionFactory {
  def allFactories: Iterator[CollectionFactory] =
    ListFactory.allFactories ++ SetFactory.allFactories
}

trait CollectionFactory {
  def implementationName: String
  def empty[E: ClassTag]: ju.Collection[E]
  def allowsMutationThroughIterator: Boolean = true
  def allowsNullElementQuery: Boolean = true

  def fromElements[E: ClassTag](elems: E*): ju.Collection[E] = {
    val coll = empty[E]
    coll.addAll(elems.asJavaCollection)
    coll
  }
}
