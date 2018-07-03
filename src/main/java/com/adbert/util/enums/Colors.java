package com.adbert.util.enums;

import android.graphics.Color;

public enum Colors {
	cpmBg("#CC000000"), cpmBgLight("#30000000"), endingCardBg("#50000000"), videoBg("#50000000");

	private String code;

	private Colors(String code) {
		this.code = code;
	}

	public int parseColor() {
		return Color.parseColor(code);
	}
}
