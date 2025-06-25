// SPDX-FileCopyrightText: 2015-2017 C. Ramakrishnan / Illposed Software
// SPDX-FileCopyrightText: 2021 Robin Vobruba <hoijui.quaero@gmail.com>
//
// SPDX-License-Identifier: BSD-3-Clause

package com.illposed.osc;

import java.io.Serializable;

/**
 * Holds (meta-)info about a message.
 * This might be used by some implementations of {@link MessageSelector}, for example.
 */
public class OSCMessageInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private final CharSequence argumentTypeTags;

	// Public API
	/**
	 * @param argumentTypeTags the <i>Arguments Type Tags</i> string, for example "iiscdi[fff]h"
	 */
	@SuppressWarnings({"WeakerAccess", "SpellCheckingInspection"})
	public OSCMessageInfo(final CharSequence argumentTypeTags) {
		this.argumentTypeTags = argumentTypeTags;
	}

	/**
	 * Returns the <i>Arguments Type Tags</i> string.
	 * @return for example "iiscdi[fff]h"
	 */
	@SuppressWarnings("SpellCheckingInspection")
	public CharSequence getArgumentTypeTags() {
		return argumentTypeTags;
	}
}
