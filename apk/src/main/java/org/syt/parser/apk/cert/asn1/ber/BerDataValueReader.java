/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.syt.parser.apk.cert.asn1.ber;

import org.syt.parser.apk.cert.asn1.ber.BerDataValue;
import org.syt.parser.apk.cert.asn1.ber.BerDataValueFormatException;

/**
 * Reader of ASN.1 Basic Encoding Rules (BER) data values.
 *
 * <p>BER data value reader returns data values, one by one, from a source. The interpretation of
 * data values (e.g., how to obtain a numeric value from an INTEGER data value, or how to extract
 * the elements of a SEQUENCE value) is left to clients of the reader.
 */
public interface BerDataValueReader {

    /**
     * Returns the next data value or {@code null} if end of input has been reached.
     *
     * @throws org.syt.parser.apk.cert.asn1.ber.BerDataValueFormatException if the value being read is malformed.
     */
    BerDataValue readDataValue() throws BerDataValueFormatException;
}
