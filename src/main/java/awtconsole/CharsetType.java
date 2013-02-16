/*
 * Copyright (C) 2011 René Jeschke <rene_jeschke@yahoo.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package awtconsole;

/**
 * Enumeration of different charsets. Charsets are codepage 850 standard DOS/Windows
 * console charsets. 
 * @author René 'Neotec' Jeschke
 */
public enum CharsetType
{
	/** Standard, 4x6 pixels */
	CHARS_4x6,
	/** Standard, 5x12 pixels */
	CHARS_5x12,
	/** Standard, 6x8 pixels */
	CHARS_6x8,
	/** Standard, 7x12 pixels */
	CHARS_7x12,
	/** Standard, 8x8 pixels */
	CHARS_8x8,
	/** Standard, 8x12 pixels */
	CHARS_8x12,
	/** Standard, 8x16 pixels */
	CHARS_8x16,
	/** Standard, 10x18 pixels */
	CHARS_10x18,
	/** Standard, 12x16 pixels */
	CHARS_12x16,
	/** Standard, 16x8 pixels */
	CHARS_16x8,
	/** Standard, 16x12 pixels */
	CHARS_16x12;
	
	public static CharsetType fromString(String value)
	{
		final CharsetType[] all = CharsetType.values();
		for(CharsetType c : all)
		{
			if(c.toString().toUpperCase().equals(value.toUpperCase()))
				return c;
		}
		return CHARS_8x12;
	}
}
