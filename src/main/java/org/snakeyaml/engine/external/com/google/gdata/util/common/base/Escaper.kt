/*
 * Copyright (c) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.snakeyaml.engine.external.com.google.gdata.util.common.base

/**
 * An object that converts literal text into a format safe for inclusion in a particular context
 * (such as an XML document). Typically (but not always), the inverse process of "unescaping" the
 * text is performed automatically by the relevant scanner.
 *
 * For example, an XML escaper would convert the literal string `"Foo<Bar>"` into
 * `"Foo&lt;Bar&gt;"` to prevent `"<Bar>"` from being confused with an XML tag. When the
 * resulting XML document is parsed, the scanner API will return this text as the original literal
 * string `"Foo<Bar>"`.
 *
 * An `Escaper` instance is required to be stateless, and safe when used concurrently by
 * multiple threads.
 *
 * Several popular escapers are defined as constants in the class [CharEscapers]. To create
 * your own escapers, use [CharEscaperBuilder], or extend [CharEscaper] or
 * [UnicodeEscaper].
 */
interface Escaper {
  /**
   * Returns the escaped form of a given literal string.
   *
   * Note that this method may treat input characters differently depending on the specific escaper
   * implementation.
   *
   *  * [UnicodeEscaper] handles [UTF-16](http://en.wikipedia.org/wiki/UTF-16)
   * correctly, including surrogate character pairs. If the input is badly formed the escaper should
   * throw [IllegalArgumentException].
   *  * [CharEscaper] handles Java characters independently and does not verify the input for
   * well-formed characters. A CharEscaper should not be used in situations where input is not
   * guaranteed to be restricted to the Basic Multilingual Plane (BMP).
   *
   * @param string the literal string to be escaped
   * @return the escaped form of `string`
   * @throws NullPointerException if `string` is null
   * @throws IllegalArgumentException if `string` contains badly formed UTF-16 or cannot be
   * escaped for any other reason
   */
  fun escape(string: String): String
}
